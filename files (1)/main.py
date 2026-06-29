from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware
from fastapi.middleware.gzip import GZipMiddleware
from contextlib import asynccontextmanager

from app.database import engine, Base
from app.routers import assets, transactions, health


@asynccontextmanager
async def lifespan(app: FastAPI):
    async with engine.begin() as conn:
        await conn.run_sync(Base.metadata.create_all)
    yield
    await engine.dispose()


app = FastAPI(
    title="MasterBarang API",
    description="Backend MasterBarang - sistem manajemen barang offline-first",
    version="1.0.0",
    lifespan=lifespan
)

# ✅ GZIP: kompres semua response JSON yang > 1000 bytes
app.add_middleware(GZipMiddleware, minimum_size=1000)

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

app.include_router(health.router,       prefix="/api/v1", tags=["Health"])
app.include_router(assets.router,       prefix="/api/v1", tags=["Assets"])
app.include_router(transactions.router, prefix="/api/v1", tags=["Transactions"])
