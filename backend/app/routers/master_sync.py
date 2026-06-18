from fastapi import APIRouter, Depends
from sqlalchemy.ext.asyncio import AsyncSession
from sqlalchemy import select, func
from typing import List
from pydantic import BaseModel

from app.database import get_db
from app.models.models import Asset, Transaction
from app.schemas.schemas import AssetResponse, TransactionResponse

router = APIRouter()


class MasterSyncResponse(BaseModel):
    total_assets: int
    total_transactions: int
    assets: List[AssetResponse]
    transactions: List[TransactionResponse]
    server_timestamp_ms: int


@router.get("/master-sync", response_model=MasterSyncResponse)
async def get_master_sync(
    since_ms: int = 0,   # optional: hanya ambil data yang berubah sejak timestamp ini
    db: AsyncSession = Depends(get_db)
):
    """
    Endpoint utama untuk sinkronisasi data dari server ke device.
    
    Dipanggil saat:
    - App pertama kali online
    - WorkManager mendeteksi koneksi tersedia
    - User menekan tombol sync manual
    
    Parameter:
    - since_ms: epoch milliseconds. Jika 0, ambil semua data.
                Jika diisi, hanya ambil data yang updated_at >= since_ms
    """
    import time
    now_ms = int(time.time() * 1000)

    # Query assets
    asset_query = select(Asset).order_by(Asset.updated_at.desc())
    if since_ms > 0:
        asset_query = asset_query.where(Asset.updated_at >= since_ms)

    asset_result = await db.execute(asset_query)
    assets = asset_result.scalars().all()

    # Query transactions
    tx_query = select(Transaction).order_by(Transaction.timestamp_ms.desc())
    if since_ms > 0:
        tx_query = tx_query.where(Transaction.timestamp_ms >= since_ms)

    tx_result = await db.execute(tx_query)
    transactions = tx_result.scalars().all()

    # Count total di database
    total_assets = (await db.execute(select(func.count(Asset.id)))).scalar_one()
    total_transactions = (await db.execute(select(func.count(Transaction.id)))).scalar_one()

    return MasterSyncResponse(
        total_assets=total_assets,
        total_transactions=total_transactions,
        assets=assets,
        transactions=transactions,
        server_timestamp_ms=now_ms
    )
