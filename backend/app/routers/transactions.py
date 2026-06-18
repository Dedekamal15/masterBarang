import os
import shutil
from fastapi import APIRouter, Depends, UploadFile, File, Form, HTTPException
from sqlalchemy.ext.asyncio import AsyncSession
from sqlalchemy import select
from typing import List, Optional

from app.database import get_db
from app.models.models import Transaction, TransactionType
from app.schemas.schemas import TransactionBatchRequest, SyncResponse, TransactionResponse

router = APIRouter()

# Folder penyimpanan bukti di server
EVIDENCE_DIR = os.getenv("EVIDENCE_DIR", "/app/evidence")
os.makedirs(EVIDENCE_DIR, exist_ok=True)


@router.post("/transactions/batch", response_model=SyncResponse, status_code=200)
async def sync_transactions_batch(
    payload: TransactionBatchRequest,
    db: AsyncSession = Depends(get_db)
):
    """
    Batch sync transaksi dari Android.
    Idempotent — jika ID sudah ada, skip (tidak duplikat).
    """
    synced_ids: List[str] = []
    failed_ids: List[str] = []

    for tx_dto in payload.transactions:
        try:
            result = await db.execute(
                select(Transaction).where(Transaction.id == tx_dto.id)
            )
            if result.scalar_one_or_none():
                synced_ids.append(tx_dto.id)
                continue

            db.add(Transaction(
                id                  = tx_dto.id,
                asset_id            = tx_dto.asset_id,
                asset_name          = tx_dto.asset_name,
                asset_serial_number = tx_dto.asset_serial_number,
                type                = TransactionType(tx_dto.type.value),
                recipient_name      = tx_dto.recipient_name,
                destination         = tx_dto.destination,
                notes               = tx_dto.notes,
                timestamp_ms        = tx_dto.timestamp_ms,
                latitude            = tx_dto.latitude,
                longitude           = tx_dto.longitude,
                gps_accuracy_meters = tx_dto.gps_accuracy_meters,
                evidence_filename   = tx_dto.evidence_filename,
                evidence_type       = tx_dto.evidence_type,
            ))
            synced_ids.append(tx_dto.id)

        except Exception as e:
            failed_ids.append(tx_dto.id)

    await db.flush()

    return SyncResponse(
        synced_count=len(synced_ids),
        failed_ids=failed_ids,
        message=f"Synced {len(synced_ids)} transactions. {len(failed_ids)} failed."
    )


@router.post("/transactions/{transaction_id}/evidence")
async def upload_evidence(
    transaction_id: str,
    file: UploadFile = File(...),
    db: AsyncSession = Depends(get_db)
):
    """
    Upload file bukti (foto/PDF) untuk transaksi tertentu.
    Dipanggil terpisah setelah batch sync berhasil.
    """
    result = await db.execute(
        select(Transaction).where(Transaction.id == transaction_id)
    )
    tx = result.scalar_one_or_none()
    if not tx:
        raise HTTPException(status_code=404, detail="Transaction not found")

    # Validasi tipe file
    content_type = file.content_type or ""
    if not (content_type.startswith("image/") or content_type == "application/pdf"):
        raise HTTPException(status_code=400, detail="Only image or PDF files allowed")

    # Simpan file
    ext = ".pdf" if content_type == "application/pdf" else ".jpg"
    filename = f"{transaction_id}{ext}"
    file_path = os.path.join(EVIDENCE_DIR, filename)

    with open(file_path, "wb") as f:
        shutil.copyfileobj(file.file, f)

    # Update record
    tx.evidence_filename = filename
    tx.evidence_type = "PDF" if ext == ".pdf" else "PHOTO"
    await db.flush()

    return {
        "transaction_id": transaction_id,
        "filename": filename,
        "message": "Evidence uploaded successfully"
    }


@router.get("/transactions/{transaction_id}/evidence")
async def get_evidence(
    transaction_id: str,
    db: AsyncSession = Depends(get_db)
):
    """Download file bukti dari server."""
    from fastapi.responses import FileResponse

    result = await db.execute(
        select(Transaction).where(Transaction.id == transaction_id)
    )
    tx = result.scalar_one_or_none()
    if not tx or not tx.evidence_filename:
        raise HTTPException(status_code=404, detail="Evidence not found")

    file_path = os.path.join(EVIDENCE_DIR, tx.evidence_filename)
    if not os.path.exists(file_path):
        raise HTTPException(status_code=404, detail="Evidence file missing on server")

    media_type = "application/pdf" if tx.evidence_type == "PDF" else "image/jpeg"
    return FileResponse(file_path, media_type=media_type, filename=tx.evidence_filename)


@router.get("/transactions", response_model=List[TransactionResponse])
async def list_transactions(
    skip: int = 0,
    limit: int = 1000,
    db: AsyncSession = Depends(get_db)
):
    result = await db.execute(
        select(Transaction)
        .order_by(Transaction.timestamp_ms.desc())
        .offset(skip).limit(limit)
    )
    return result.scalars().all()
