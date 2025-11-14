"""
Doctor Router - Placeholder for doctor-related endpoints
"""

from fastapi import APIRouter

router = APIRouter(prefix="/doctor")


@router.get("/")
async def get_doctor_info():
    """Get doctor information - to be implemented."""
    return {"message": "Doctor endpoints - implementation in progress"}
