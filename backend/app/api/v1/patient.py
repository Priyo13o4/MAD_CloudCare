"""
Patient Router - Placeholder for patient-related endpoints
"""

from fastapi import APIRouter

router = APIRouter(prefix="/patient")


@router.get("/")
async def get_patient_info():
    """Get patient information - to be implemented."""
    return {"message": "Patient endpoints - implementation in progress"}
