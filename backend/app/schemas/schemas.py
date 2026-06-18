from pydantic import BaseModel, Field, field_validator
from typing import Optional, List
from enum import Enum


class AssetStatus(str, Enum):
    AVAILABLE   = "AVAILABLE"
    BORROWED    = "BORROWED"
    MAINTENANCE = "MAINTENANCE"


class TransactionType(str, Enum):
    CHECK_OUT = "CHECK_OUT"
    CHECK_IN  = "CHECK_IN"


# ── Asset ─────────────────────────────────────────────────────────────────────

class AssetSchema(BaseModel):
    id:            str
    name:          str = Field(..., min_length=1, max_length=255)
    category:      str = Field(default="", max_length=100)
    serial_number: str = Field(..., min_length=1, max_length=100)
    description:   str = Field(default="")
    location:      str = Field(default="", max_length=200)
    status:        AssetStatus
    created_at:    int
    updated_at:    int

    @field_validator("serial_number")
    @classmethod
    def sn_not_empty(cls, v: str) -> str:
        v = v.strip()
        if not v:
            raise ValueError("serial_number cannot be blank")
        return v

    model_config = {"from_attributes": True}


class AssetBatchRequest(BaseModel):
    assets: List[AssetSchema] = Field(..., min_length=1, max_length=500)


class AssetResponse(AssetSchema):
    pass


# ── Transaction ───────────────────────────────────────────────────────────────

class TransactionSchema(BaseModel):
    id:                  str
    asset_id:            str
    asset_name:          str
    asset_serial_number: str
    type:                TransactionType
    recipient_name:      str = ""
    destination:         str = ""
    notes:               str = ""
    timestamp_ms:        int
    latitude:            Optional[float] = None
    longitude:           Optional[float] = None
    gps_accuracy_meters: Optional[float] = None
    evidence_filename:   Optional[str]   = None
    evidence_type:       Optional[str]   = None   # PHOTO | PDF

    model_config = {"from_attributes": True}


class TransactionBatchRequest(BaseModel):
    transactions: List[TransactionSchema] = Field(..., min_length=1, max_length=1000)


class TransactionResponse(TransactionSchema):
    pass


# ── Sync Response ─────────────────────────────────────────────────────────────

class SyncResponse(BaseModel):
    synced_count: int
    failed_ids:   List[str]
    message:      str


# ── Health ────────────────────────────────────────────────────────────────────

class HealthResponse(BaseModel):
    status:  str
    version: str
    db:      str
