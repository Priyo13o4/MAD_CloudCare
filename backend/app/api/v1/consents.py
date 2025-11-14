"""
Consents Router - Placeholder for consent management endpoints
"""

from fastapi import APIRouter

router = APIRouter(prefix="/consents")


@router.get("/")
async def get_consents():
    """Get consent requests - to be implemented."""
    return {"message": "Consents endpoints - implementation in progress"}
