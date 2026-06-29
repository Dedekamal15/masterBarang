import time
import logging
from fastapi import APIRouter, Depends, HTTPException
from sqlalchemy.ext.asyncio import AsyncSession
from sqlalchemy import select, func
from typing import List
from pydantic import BaseModel

from app.database import get_db
from app.models.models import Asset, AssetStatus
from app.schemas.schemas import AssetBatchRequest, AssetResponse, SyncResponse, TransactionResponse

logger = logging.getLogger(__name__)

router = APIRouter()


# ── Pull: data dari server ke device ─────────────────────────────────────────

class MasterSyncResponse(BaseModel):
    total_assets: int
    total_transactions: int
    assets: List[AssetResponse]
    transactions: List[TransactionResponse]
    server_timestamp_ms: int


@router.get("/master-sync", response_model=MasterSyncResponse)
async def master_sync(
    since_ms: int = 0,
    db: AsyncSession = Depends(get_db)
):
    """
    Satu endpoint untuk tarik semua data server ke device.
    since_ms=0 → ambil semua. since_ms > 0 → hanya data yang berubah sejak timestamp itu.
    """
    from app.models.models import Transaction

    now_ms = int(time.time() * 1000)

    asset_q = select(Asset).order_by(Asset.updated_at.desc())
    if since_ms > 0:
        asset_q = asset_q.where(Asset.updated_at >= since_ms)
    assets = (await db.execute(asset_q)).scalars().all()

    tx_q = select(Transaction).order_by(Transaction.timestamp_ms.desc())
    if since_ms > 0:
        tx_q = tx_q.where(Transaction.timestamp_ms >= since_ms)
    transactions = (await db.execute(tx_q)).scalars().all()

    total_assets = (await db.execute(select(func.count(Asset.id)))).scalar_one()
    total_tx = (await db.execute(
        select(func.count()).select_from(Transaction)
    )).scalar_one()

    return MasterSyncResponse(
        total_assets=total_assets,
        total_transactions=total_tx,
        assets=assets,
        transactions=transactions,
        server_timestamp_ms=now_ms
    )


# ── Push: data dari device ke server ─────────────────────────────────────────

@router.post("/assets/batch", response_model=SyncResponse)
async def sync_assets_batch(
    payload: AssetBatchRequest,
    db: AsyncSession = Depends(get_db)
):
    synced_ids: List[str] = []
    failed_ids: List[str] = []

    # ── Single bulk fetch: get all existing assets by ID ──
    asset_ids = [a.id for a in payload.assets]
    existing_result = await db.execute(
        select(Asset).where(Asset.id.in_(asset_ids))
    )
    existing_map = {a.id: a for a in existing_result.scalars().all()}

    # ── Single bulk fetch: get all existing serial numbers ──
    sns = [a.serial_number for a in payload.assets]
    sn_result = await db.execute(
        select(Asset.serial_number).where(Asset.serial_number.in_(sns))
    )
    existing_sns: set[str] = set(sn_result.scalars().all())
    seen_sns: set[str] = set()          # track SNs within this batch

    for dto in payload.assets:
        try:
            existing = existing_map.get(dto.id)

            if existing:
                if dto.updated_at >= existing.updated_at:
                    existing.name          = dto.name
                    existing.category      = dto.category
                    existing.serial_number = dto.serial_number
                    existing.description   = dto.description
                    existing.location      = dto.location
                    existing.status        = AssetStatus(dto.status.value)
                    existing.updated_at    = dto.updated_at
            else:
                # Cek duplikat SN (server-wide + in-flight within batch)
                if dto.serial_number in existing_sns or dto.serial_number in seen_sns:
                    failed_ids.append(dto.id)
                    continue

                db.add(Asset(
                    id            = dto.id,
                    name          = dto.name,
                    category      = dto.category,
                    serial_number = dto.serial_number,
                    description   = dto.description,
                    location      = dto.location,
                    status        = AssetStatus(dto.status.value),
                    created_at    = dto.created_at,
                    updated_at    = dto.updated_at
                ))
                seen_sns.add(dto.serial_number)

            synced_ids.append(dto.id)
        except Exception as e:
            logger.exception("Failed to sync asset %s: %s", dto.id, e)
            failed_ids.append(dto.id)

    await db.flush()
    return SyncResponse(
        synced_count=len(synced_ids),
        failed_ids=failed_ids,
        message=f"Synced {len(synced_ids)} assets. {len(failed_ids)} failed."
    )


@router.get("/assets", response_model=List[AssetResponse])
async def list_assets(skip: int = 0, limit: int = 1000, db: AsyncSession = Depends(get_db)):
    result = await db.execute(
        select(Asset).order_by(Asset.updated_at.desc()).offset(skip).limit(limit)
    )
    return result.scalars().all()


@router.get("/assets/{asset_id}", response_model=AssetResponse)
async def get_asset(asset_id: str, db: AsyncSession = Depends(get_db)):
    result = await db.execute(select(Asset).where(Asset.id == asset_id))
    asset = result.scalar_one_or_none()
    if not asset:
        raise HTTPException(status_code=404, detail="Asset not found")
    return asset
