import gzip
from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware
from fastapi.middleware.gzip import GZipMiddleware
from starlette.middleware.base import BaseHTTPMiddleware
from contextlib import asynccontextmanager

from app.database import engine, Base
from app.routers import assets, transactions, health

class GZipRequestMiddleware(BaseHTTPMiddleware):
    async def dispatch(self, request, call_next):
        if request.headers.get("Content-Encoding") == "gzip":
            body = await request.body()
            request._body = gzip.decompress(body)
        return await call_next(request)

@asynccontextmanager
async def lifespan(app: FastAPI):
    # Buat semua tabel saat startup
    async with engine.begin() as conn:
        await conn.run_sync(Base.metadata.create_all)
    yield
    await engine.dispose()


app = FastAPI(
    title="MasterBarang API",
    description="Backend MasterBarang - sistem manajemen barang offline-first dengan sync batch dan upload bukti",
    version="1.0.0",
    lifespan=lifespan
)

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)
app.add_middleware(GZipMiddleware, minimum_size=500)
app.add_middleware(GZipRequestMiddleware)

app.include_router(health.router,       prefix="/api/v1", tags=["Health"])
app.include_router(assets.router,       prefix="/api/v1", tags=["Assets"])
app.include_router(transactions.router, prefix="/api/v1", tags=["Transactions"])
