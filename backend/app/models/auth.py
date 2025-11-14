"""
Pydantic Models for Authentication

Data validation models for auth operations.
"""

from pydantic import BaseModel, EmailStr, Field
from typing import Optional
from enum import Enum


class UserRole(str, Enum):
    """User role enumeration."""
    PATIENT = "PATIENT"
    DOCTOR = "DOCTOR"
    HOSPITAL_ADMIN = "HOSPITAL_ADMIN"
    SYSTEM_ADMIN = "SYSTEM_ADMIN"


class LoginRequest(BaseModel):
    """Request model for user login."""
    email: EmailStr
    password: str = Field(..., min_length=8)


class RegisterPatientRequest(BaseModel):
    """Request model for patient registration."""
    email: EmailStr
    password: str = Field(..., min_length=8)
    name: str = Field(..., min_length=1, max_length=100)
    aadhar_number: str = Field(..., pattern=r"^\d{12}$", description="12-digit Aadhar number")
    age: int = Field(..., ge=0, le=150)
    gender: str = Field(..., pattern=r"^(Male|Female|Other)$")
    blood_type: str
    contact: str
    address: str
    family_contact: str
    insurance_provider: Optional[str] = None
    insurance_id: Optional[str] = None
    occupation: Optional[str] = None


class TokenResponse(BaseModel):
    """Response model for authentication tokens."""
    access_token: str
    refresh_token: str
    token_type: str = "bearer"


class UserResponse(BaseModel):
    """Response model for user data."""
    id: str
    email: str
    role: UserRole
    is_active: bool
    created_at: str
    
    class Config:
        from_attributes = True
