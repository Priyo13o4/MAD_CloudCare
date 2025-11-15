"""
CloudCare Backend - Main Application Entry Point

This is the main FastAPI application that orchestrates all services and routers.
Designed for maintainability with clear separation of concerns.
"""

from fastapi import FastAPI, Request, status
from fastapi.middleware.cors import CORSMiddleware
from fastapi.middleware.gzip import GZipMiddleware
from fastapi.responses import JSONResponse
from fastapi.exceptions import RequestValidationError
from contextlib import asynccontextmanager
import structlog
from typing import AsyncGenerator

from app.core.config import settings
from app.core.database import init_databases, close_databases
from app.api.v1 import auth, patient, doctor, hospital, wearables, consents, documents, health

# Configure structured logging
logger = structlog.get_logger(__name__)


@asynccontextmanager
async def lifespan(app: FastAPI) -> AsyncGenerator:
    """
    Application lifespan manager - handles startup and shutdown events.
    
    This ensures proper database connection handling and resource cleanup.
    """
    # Startup
    logger.info("🚀 Starting CloudCare Backend API...")
    
    # Print network information banner immediately
    print("\n" + "="*80)
    print("🏥 CloudCare Backend API - Network Information")
    print("="*80)
    print(f"📍 Local URL:            http://localhost:{settings.PORT}")
    print(f"🌐 Public URL (Tunnel):  {settings.CLOUDFLARE_TUNNEL_URL}")
    print(f"")
    print("✅ FOR iPHONE/ANDROID: Use the Public URL above")
    print("   (Cloudflare Tunnel - stable, works from anywhere)")
    print("\n📚 API Documentation:")
    print(f"   Swagger UI:   {settings.CLOUDFLARE_TUNNEL_URL}/docs")
    print(f"   ReDoc:        {settings.CLOUDFLARE_TUNNEL_URL}/redoc")
    print("\n⌚ Wearables Endpoints (iOS/Android apps):")
    print(f"   Import Apple Health:  POST {settings.CLOUDFLARE_TUNNEL_URL}/api/{settings.API_VERSION}/wearables/import/apple-health")
    print(f"   Test Connection:      GET  {settings.CLOUDFLARE_TUNNEL_URL}/api/{settings.API_VERSION}/wearables/import/apple-health")
    print("="*80 + "\n")
    
    try:
        await init_databases()
        logger.info("✅ All databases initialized successfully")
        
    except Exception as e:
        logger.error(f"❌ Failed to initialize databases: {e}")
        raise
    
    yield
    
    # Shutdown
    logger.info("🛑 Shutting down CloudCare Backend API...")
    try:
        await close_databases()
        logger.info("✅ All databases closed successfully")
    except Exception as e:
        logger.error(f"❌ Error during shutdown: {e}")


# Initialize FastAPI application
app = FastAPI(
    title="CloudCare Backend API",
    description="Healthcare management system with Aadhar-based patient identification",
    version="1.0.0",
    docs_url="/docs",
    redoc_url="/redoc",
    lifespan=lifespan,
)


# ==================== Middleware Configuration ====================

# CORS Middleware - Allow frontend to access the API
app.add_middleware(
    CORSMiddleware,
    allow_origins=settings.CORS_ORIGINS,
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# GZip Middleware - Compress responses for better performance
app.add_middleware(GZipMiddleware, minimum_size=1000)


# ==================== Exception Handlers ====================

@app.exception_handler(RequestValidationError)
async def validation_exception_handler(request: Request, exc: RequestValidationError):
    """
    Custom validation error handler for better error messages.
    
    This makes debugging easier for both developers and API users.
    """
    logger.warning(
        "Validation error",
        path=request.url.path,
        errors=exc.errors(),
    )
    return JSONResponse(
        status_code=status.HTTP_422_UNPROCESSABLE_ENTITY,
        content={
            "detail": exc.errors(),
            "message": "Validation error - please check your request data",
        },
    )


@app.exception_handler(Exception)
async def general_exception_handler(request: Request, exc: Exception):
    """
    Catch-all exception handler to prevent server crashes.
    
    Logs the error for debugging while returning a safe message to the client.
    """
    logger.error(
        "Unhandled exception",
        path=request.url.path,
        error=str(exc),
        exc_info=True,
    )
    return JSONResponse(
        status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
        content={
            "message": "An internal error occurred. Please try again later.",
            "error_id": str(id(exc)),  # Can be used to trace in logs
        },
    )


# ==================== API Routes ====================

# Health check endpoint
@app.get("/", tags=["Health"])
async def root():
    """
    Root endpoint - confirms the API is running.
    """
    return {
        "message": "CloudCare Backend API",
        "version": "1.0.0",
        "status": "running",
        "docs": "/docs",
    }


@app.get("/health", tags=["Health"])
async def health_check():
    """
    Health check endpoint for monitoring and load balancers.
    
    Can be extended to check database connectivity and other services.
    """
    return {
        "status": "healthy",
        "environment": settings.ENVIRONMENT,
        "version": "1.0.0",
    }


# Include all API routers
app.include_router(auth.router, prefix=f"/api/{settings.API_VERSION}", tags=["Authentication"])
app.include_router(patient.router, prefix=f"/api/{settings.API_VERSION}", tags=["Patient"])
app.include_router(doctor.router, prefix=f"/api/{settings.API_VERSION}", tags=["Doctor"])
app.include_router(hospital.router, prefix=f"/api/{settings.API_VERSION}", tags=["Hospital"])
app.include_router(wearables.router, prefix=f"/api/{settings.API_VERSION}", tags=["Wearables"])
app.include_router(consents.router, prefix=f"/api/{settings.API_VERSION}", tags=["Consents"])
app.include_router(documents.router, prefix=f"/api/{settings.API_VERSION}", tags=["Documents"])
app.include_router(health.router, prefix=f"/api/{settings.API_VERSION}", tags=["Health Data"])


# ==================== Request Logging Middleware ====================

@app.middleware("http")
async def log_requests(request: Request, call_next):
    """
    Log all incoming requests for debugging and monitoring.
    
    Helps track API usage and identify issues in production.
    """
    logger.info(
        "Incoming request",
        method=request.method,
        path=request.url.path,
        client=request.client.host if request.client else "unknown",
    )
    
    response = await call_next(request)
    
    logger.info(
        "Request completed",
        method=request.method,
        path=request.url.path,
        status_code=response.status_code,
    )
    
    return response


if __name__ == "__main__":
    import uvicorn
    
    uvicorn.run(
        "app.main:app",
        host="0.0.0.0",
        port=settings.PORT,
        reload=settings.DEBUG,
        log_level=settings.LOG_LEVEL.lower(),
    )
