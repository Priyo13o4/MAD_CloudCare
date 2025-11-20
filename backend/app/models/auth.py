"""
Pydantic Models for Authentication

Data validation models for auth operations.
"""

from pydantic import BaseModel, EmailStr, Field, model_validator
from email_validator import validate_email, EmailNotValidError
from typing import Optional, List
from enum import Enum
from datetime import datetime


class UserRole(str, Enum):
    """User role enumeration."""
    PATIENT = "PATIENT"
    DOCTOR = "DOCTOR"
    HOSPITAL_ADMIN = "HOSPITAL_ADMIN"
    SYSTEM_ADMIN = "SYSTEM_ADMIN"


class LoginRequest(BaseModel):
    """Request model for user login."""
    email: Optional[str] = None  # Email address
    aadhar: Optional[str] = None  # 12-digit Aadhar number
    password: str = Field(..., min_length=8)
    role: Optional[UserRole] = None  # Optional role filter
    
    @model_validator(mode='after')
    def email_or_aadhar(self):
        """Ensure either email or aadhar is provided, but not both."""
        if not self.email and not self.aadhar:
            raise ValueError('Either email or aadhar must be provided')
        
        if self.email and self.aadhar:
            raise ValueError('Provide only email or aadhar, not both')
        
        # Validate email format if provided
        if self.email:
            try:
                validate_email(self.email)
            except EmailNotValidError as e:
                raise ValueError(f'Invalid email: {str(e)}')
        
        # Validate aadhar format if provided
        if self.aadhar:
            # Remove spaces and check if 12 digits
            aadhar_clean = self.aadhar.replace(' ', '')
            if not aadhar_clean.isdigit() or len(aadhar_clean) != 12:
                raise ValueError('Aadhar must be a 12-digit number')
            # Store cleaned version
            self.aadhar = aadhar_clean
        
        return self


class RegisterPatientRequest(BaseModel):
    """Request model for patient registration."""
    # Authentication
    email: EmailStr
    password: str = Field(..., min_length=8)
    
    # Aadhar Information
    aadhar_number: str = Field(..., pattern=r"^\d{12}$", description="12-digit Aadhar number")
    
    # Personal Information
    first_name: str = Field(..., min_length=1, max_length=50)
    middle_name: Optional[str] = Field(None, max_length=50)
    last_name: str = Field(..., min_length=1, max_length=50)
    date_of_birth: str  # ISO format date string
    gender: str = Field(..., pattern=r"^(MALE|FEMALE|OTHER|PREFER_NOT_TO_SAY)$")
    blood_group: Optional[str] = None
    
    # Contact Information
    phone_primary: str = Field(..., pattern=r"^\+91\d{10}$")
    phone_secondary: Optional[str] = Field(None, pattern=r"^\+91\d{10}$")
    address_line1: str
    address_line2: Optional[str] = None
    city: str
    state: str
    postal_code: str = Field(..., pattern=r"^\d{6}$")
    
    # Emergency Contact
    emergency_contact_name: str
    emergency_contact_phone: str = Field(..., pattern=r"^\+91\d{10}$")
    emergency_contact_relation: str
    
    # Medical Profile (Optional)
    height_cm: Optional[float] = None
    weight_kg: Optional[float] = None
    allergies: Optional[List[str]] = None
    chronic_conditions: Optional[List[str]] = None
    current_medications: Optional[List[str]] = None
    
    # Insurance (Optional)
    insurance_provider: Optional[str] = None
    insurance_policy_no: Optional[str] = None
    insurance_valid_until: Optional[str] = None  # ISO format


class RegisterDoctorRequest(BaseModel):
    """Request model for doctor registration."""
    # Authentication
    email: EmailStr
    password: str = Field(..., min_length=8)
    
    # Professional Identification
    medical_license_no: str = Field(..., min_length=5)
    registration_year: Optional[int] = None
    registration_state: Optional[str] = None
    
    # Personal Information
    title: str = Field(default="Dr.", pattern=r"^(Dr\.|Prof\. Dr\.|Prof\.)$")
    first_name: str = Field(..., min_length=1, max_length=50)
    middle_name: Optional[str] = Field(None, max_length=50)
    last_name: str = Field(..., min_length=1, max_length=50)
    date_of_birth: Optional[str] = None
    gender: Optional[str] = None
    
    # Contact Information
    phone_primary: str = Field(..., pattern=r"^\+91\d{10}$")
    phone_secondary: Optional[str] = None
    email_professional: Optional[EmailStr] = None
    address_line1: Optional[str] = None
    address_line2: Optional[str] = None
    city: Optional[str] = None
    state: Optional[str] = None
    postal_code: Optional[str] = None
    
    # Professional Details
    specialization: str  # Comma-separated or single value
    sub_specialization: Optional[str] = None
    qualifications: List[str]  # ["MBBS", "MD", "DM"]
    experience_years: int = Field(default=0, ge=0)
    languages_spoken: Optional[List[str]] = None
    
    # Hospital Linking
    hospital_code: str = Field(..., min_length=8, max_length=10, description="Hospital code to link (HC-XXXXXX)")
    
    # Practice Information
    consultation_fee: Optional[float] = None
    available_for_emergency: bool = False
    telemedicine_enabled: bool = False


class RegisterHospitalRequest(BaseModel):
    """Request model for hospital registration."""
    # Authentication
    email: EmailStr
    password: str = Field(..., min_length=8)
    
    # Facility Information
    name: str = Field(..., min_length=3, max_length=200)
    registration_no: Optional[str] = None
    license_no: Optional[str] = None
    accreditation: Optional[str] = None
    facility_type: Optional[str] = None
    specializations: Optional[List[str]] = None
    
    # Contact Information
    phone_primary: str = Field(..., pattern=r"^\+91\d{10}$")
    phone_emergency: Optional[str] = None
    website: Optional[str] = None
    
    # Address
    address_line1: str
    address_line2: Optional[str] = None
    city: str
    state: str
    postal_code: str = Field(..., pattern=r"^\d{6}$")
    latitude: Optional[float] = None
    longitude: Optional[float] = None
    
    # Capacity
    total_beds: int = Field(default=0, ge=0)
    icu_beds: int = Field(default=0, ge=0)
    emergency_beds: int = Field(default=0, ge=0)
    operation_theatres: int = Field(default=0, ge=0)
    
    # Services
    has_emergency: bool = True
    has_ambulance: bool = False
    has_pharmacy: bool = False
    has_lab: bool = False
    has_blood_bank: bool = False
    telemedicine_enabled: bool = False


class AuthUserResponse(BaseModel):
    """User information in auth response."""
    id: str
    email: str
    role: UserRole
    name: Optional[str] = None
    patient_id: Optional[str] = None
    doctor_id: Optional[str] = None
    hospital_id: Optional[str] = None
    aadhar_uid: Optional[str] = None  # For patients
    hospital_code: Optional[str] = None  # For hospitals


class TokenResponse(BaseModel):
    """Response model for authentication tokens."""
    access_token: str
    refresh_token: str
    token_type: str = "bearer"
    user: AuthUserResponse


class UserResponse(BaseModel):
    """Response model for user data."""
    id: str
    email: str
    role: UserRole
    is_active: bool
    created_at: str
    
    class Config:
        from_attributes = True
