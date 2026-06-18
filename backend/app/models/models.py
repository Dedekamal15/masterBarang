from sqlalchemy import Column, String, BigInteger, Float, Boolean, Enum as SAEnum, Text, Index
from sqlalchemy.orm import relationship
import enum
from app.database import Base


class AssetStatus(str, enum.Enum):
    AVAILABLE   = "AVAILABLE"
    BORROWED    = "BORROWED"
    MAINTENANCE = "MAINTENANCE"


class TransactionType(str, enum.Enum):
    CHECK_OUT = "CHECK_OUT"
    CHECK_IN  = "CHECK_IN"


class Asset(Base):
    __tablename__ = "assets"

    id            = Column(String(36),  primary_key=True)
    name          = Column(String(255), nullable=False)
    category      = Column(String(100), nullable=False, default="")
    serial_number = Column(String(100), nullable=False, unique=True, index=True)
    description   = Column(Text,        nullable=False, default="")
    location      = Column(String(200), nullable=False, default="")
    status        = Column(SAEnum(AssetStatus), nullable=False, default=AssetStatus.AVAILABLE)
    created_at    = Column(BigInteger,  nullable=False)
    updated_at    = Column(BigInteger,  nullable=False)

    transactions  = relationship("Transaction", back_populates="asset", lazy="noload")

    __table_args__ = (
        Index("ix_assets_name", "name"),
        Index("ix_assets_status", "status"),
    )


class Transaction(Base):
    __tablename__ = "transactions"

    id                  = Column(String(36),  primary_key=True)
    asset_id            = Column(String(36),  nullable=False, index=True)
    asset_name          = Column(String(255), nullable=False)
    asset_serial_number = Column(String(100), nullable=False)
    type                = Column(SAEnum(TransactionType), nullable=False)
    recipient_name      = Column(String(255), nullable=False, default="")
    destination         = Column(String(200), nullable=False, default="")
    notes               = Column(Text,        nullable=False, default="")
    timestamp_ms        = Column(BigInteger,  nullable=False, index=True)
    latitude            = Column(Float,       nullable=True)
    longitude           = Column(Float,       nullable=True)
    gps_accuracy_meters = Column(Float,       nullable=True)
    # Bukti transaksi
    evidence_filename   = Column(String(255), nullable=True)
    evidence_type       = Column(String(10),  nullable=True)  # PHOTO | PDF

    asset = relationship(
        "Asset", back_populates="transactions", lazy="noload",
        foreign_keys=[asset_id],
        primaryjoin="Transaction.asset_id == Asset.id"
    )
