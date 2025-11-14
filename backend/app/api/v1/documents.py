"""
Documents Router - Placeholder for document request endpoints
"""

from fastapi import APIRouter

router = APIRouter(prefix="/documents")


@router.get("/")
async def get_documents():
    """Get document requests - to be implemented."""
    return {"message": "Documents endpoints - implementation in progress"}
