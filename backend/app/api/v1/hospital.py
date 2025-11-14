"""
Hospital Router - Placeholder for hospital-related endpoints
"""

from fastapi import APIRouter

router = APIRouter(prefix="/hospital")


@router.get("/")
async def get_hospital_info():
    """Get hospital information - to be implemented."""
    return {"message": "Hospital endpoints - implementation in progress"}
