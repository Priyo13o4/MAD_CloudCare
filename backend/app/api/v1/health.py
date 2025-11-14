"""
Health Router - Placeholder for general health data endpoints
"""

from fastapi import APIRouter

router = APIRouter(prefix="/health-data")


@router.get("/")
async def get_health_data():
    """Get health data - to be implemented."""
    return {"message": "Health data endpoints - implementation in progress"}
