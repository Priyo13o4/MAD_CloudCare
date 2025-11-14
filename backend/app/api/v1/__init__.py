"""
API v1 Package Initializer
"""

from app.api.v1 import (
    auth,
    patient,
    doctor,
    hospital,
    wearables,
    consents,
    documents,
    health,
)

__all__ = [
    "auth",
    "patient",
    "doctor",
    "hospital",
    "wearables",
    "consents",
    "documents",
    "health",
]
