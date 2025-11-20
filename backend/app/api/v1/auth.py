"""
Authentication Router - Complete Redesign

Handles user registration (Patient, Doctor, Hospital), login, and token management.
"""

from fastapi import APIRouter, HTTPException, status, Depends
from fastapi.security import HTTPBearer, HTTPAuthorizationCredentials
import structlog
from datetime import datetime
import json

from app.models.auth import (
    LoginRequest, RegisterPatientRequest, RegisterDoctorRequest, RegisterHospitalRequest,
    TokenResponse, UserResponse, AuthUserResponse
)
from app.services.auth_service import AuthService
from app.services.aadhar_uid import AadharUIDService
from app.services.hospital_code_generator import HospitalCodeGenerator
from app.core.database import get_prisma

logger = structlog.get_logger(__name__)
router = APIRouter(prefix="/auth")
security = HTTPBearer()


@router.post("/signup/patient", response_model=TokenResponse, status_code=status.HTTP_201_CREATED)
async def signup_patient(request: RegisterPatientRequest):
    """
    Register a new patient with Aadhar-based UID.
    
    - Generates unique Aadhar UID (HMAC-SHA256)
    - Creates User + Patient records
    - Returns JWT tokens
    """
    prisma = get_prisma()
    
    try:
        # Check if email already exists
        existing_user = await prisma.user.find_unique(where={"email": request.email})
        if existing_user:
            raise HTTPException(
                status_code=status.HTTP_400_BAD_REQUEST,
                detail="Email already registered"
            )
        
        # Generate UID from Aadhar
        try:
            aadhar_uid = AadharUIDService.generate_uid(request.aadhar_number)
        except ValueError as e:
            raise HTTPException(
                status_code=status.HTTP_400_BAD_REQUEST,
                detail=f"Invalid Aadhar number: {str(e)}"
            )
        
        # Check if Aadhar UID already exists
        existing_patient = await prisma.patient.find_unique(where={"aadhar_uid": aadhar_uid})
        if existing_patient:
            raise HTTPException(
                status_code=status.HTTP_409_CONFLICT,
                detail="Patient with this Aadhar number already registered"
            )
        
        # Hash password (truncate to 72 bytes for bcrypt)
        password_hash = AuthService.hash_password(request.password[:72])
        
        # Parse date_of_birth
        try:
            dob = datetime.fromisoformat(request.date_of_birth.replace('Z', '+00:00'))
        except:
            raise HTTPException(
                status_code=status.HTTP_400_BAD_REQUEST,
                detail="Invalid date_of_birth format. Use ISO format (YYYY-MM-DD)"
            )
        
        # Parse insurance_valid_until if provided
        insurance_valid = None
        if request.insurance_valid_until:
            try:
                insurance_valid = datetime.fromisoformat(request.insurance_valid_until.replace('Z', '+00:00'))
            except:
                raise HTTPException(
                    status_code=status.HTTP_400_BAD_REQUEST,
                    detail="Invalid insurance_valid_until format"
                )
        
        # Prepare JSON arrays for lists
        allergies_json = json.dumps(request.allergies) if request.allergies else None
        conditions_json = json.dumps(request.chronic_conditions) if request.chronic_conditions else None
        medications_json = json.dumps(request.current_medications) if request.current_medications else None
        
        # Create user and patient in a transaction
        user = await prisma.user.create(
            data={
                "email": request.email,
                "password_hash": password_hash,
                "role": "PATIENT",
                "is_active": True,
                "patient": {
                    "create": {
                        "aadhar_uid": aadhar_uid,
                        "encrypted_aadhar": request.aadhar_number,  # TODO: Encrypt this
                        "aadhar_verified": False,
                        "first_name": request.first_name,
                        "middle_name": request.middle_name,
                        "last_name": request.last_name,
                        "date_of_birth": dob,
                        "gender": request.gender,
                        "blood_group": request.blood_group,
                        "phone_primary": request.phone_primary,
                        "phone_secondary": request.phone_secondary,
                        "email": request.email,
                        "address_line1": request.address_line1,
                        "address_line2": request.address_line2,
                        "city": request.city,
                        "state": request.state,
                        "postal_code": request.postal_code,
                        "country": "India",
                        "emergency_contact_name": request.emergency_contact_name,
                        "emergency_contact_phone": request.emergency_contact_phone,
                        "emergency_contact_relation": request.emergency_contact_relation,
                        "height_cm": request.height_cm,
                        "weight_kg": request.weight_kg,
                        "allergies": allergies_json,
                        "chronic_conditions": conditions_json,
                        "current_medications": medications_json,
                        "insurance_provider": request.insurance_provider,
                        "insurance_policy_no": request.insurance_policy_no,
                        "insurance_valid_until": insurance_valid,
                    }
                }
            },
            include={"patient": True}
        )
        
        # Generate tokens
        tokens = await AuthService.create_tokens_for_user(user)
        
        # Add user info to response
        auth_user = AuthUserResponse(
            id=user.id,
            email=user.email,
            role=user.role,
            name=f"{request.first_name} {request.last_name}",
            patient_id=user.patient.id if user.patient else None,
            aadhar_uid=user.patient.aadhar_uid if user.patient else None
        )
        
        logger.info("Patient registered successfully", user_id=user.id, email=user.email)
        return TokenResponse(
            access_token=tokens["access_token"],
            refresh_token=tokens["refresh_token"],
            token_type=tokens["token_type"],
            user=auth_user
        )
        
    except HTTPException:
        raise
    except Exception as e:
        logger.error("Patient registration failed", error=str(e))
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail=f"Registration failed: {str(e)}"
        )


@router.post("/signup/doctor", response_model=TokenResponse, status_code=status.HTTP_201_CREATED)
async def signup_doctor(request: RegisterDoctorRequest):
    """
    Register a new doctor.
    
    - Validates hospital_code and links to hospital
    - Creates User + Doctor records
    - Returns JWT tokens
    """
    prisma = get_prisma()
    
    try:
        # Check if email already exists
        existing_user = await prisma.user.find_unique(where={"email": request.email})
        if existing_user:
            raise HTTPException(
                status_code=status.HTTP_400_BAD_REQUEST,
                detail="Email already registered"
            )
        
        # Check if medical license already exists
        existing_doctor = await prisma.doctor.find_unique(where={"medical_license_no": request.medical_license_no})
        if existing_doctor:
            raise HTTPException(
                status_code=status.HTTP_409_CONFLICT,
                detail="Doctor with this medical license number already registered"
            )
        
        # Validate and find hospital by hospital_code
        hospital = await prisma.hospital.find_unique(where={"hospital_code": request.hospital_code})
        if not hospital:
            raise HTTPException(
                status_code=status.HTTP_404_NOT_FOUND,
                detail=f"Hospital with code '{request.hospital_code}' not found"
            )
        
        # Hash password (truncate to 72 bytes for bcrypt)
        password_hash = AuthService.hash_password(request.password[:72])
        
        # Parse date_of_birth if provided
        dob = None
        if request.date_of_birth:
            try:
                dob = datetime.fromisoformat(request.date_of_birth.replace('Z', '+00:00'))
            except:
                raise HTTPException(
                    status_code=status.HTTP_400_BAD_REQUEST,
                    detail="Invalid date_of_birth format"
                )
        
        # Prepare JSON arrays
        qualifications_json = json.dumps(request.qualifications) if request.qualifications else None
        languages_json = json.dumps(request.languages_spoken) if request.languages_spoken else None
        
        # Create user and doctor
        user = await prisma.user.create(
            data={
                "email": request.email,
                "password_hash": password_hash,
                "role": "DOCTOR",
                "is_active": True,
                "doctor": {
                    "create": {
                        "medical_license_no": request.medical_license_no,
                        "registration_year": request.registration_year,
                        "registration_state": request.registration_state,
                        "first_name": request.first_name,
                        "middle_name": request.middle_name,
                        "last_name": request.last_name,
                        "title": request.title,
                        "date_of_birth": dob,
                        "gender": request.gender,
                        "phone_primary": request.phone_primary,
                        "phone_secondary": request.phone_secondary,
                        "email_professional": request.email_professional,
                        "address_line1": request.address_line1,
                        "address_line2": request.address_line2,
                        "city": request.city,
                        "state": request.state,
                        "postal_code": request.postal_code,
                        "country": "India",
                        "specialization": request.specialization,
                        "sub_specialization": request.sub_specialization,
                        "qualifications": qualifications_json,
                        "experience_years": request.experience_years,
                        "languages_spoken": languages_json,
                        "consultation_fee": request.consultation_fee,
                        "available_for_emergency": request.available_for_emergency,
                        "telemedicine_enabled": request.telemedicine_enabled,
                        "hospital_code_input": request.hospital_code,
                        "hospital_id": hospital.id,
                        "is_verified": False,
                        "is_active": True,
                    }
                }
            },
            include={"doctor": True}
        )
        
        # Generate tokens
        tokens = await AuthService.create_tokens_for_user(user)
        
        # Add user info to response
        auth_user = AuthUserResponse(
            id=user.id,
            email=user.email,
            role=user.role,
            name=f"{request.title} {request.first_name} {request.last_name}",
            doctor_id=user.doctor.id if user.doctor else None,
            hospital_code=request.hospital_code
        )
        
        logger.info("Doctor registered successfully", user_id=user.id, email=user.email, hospital_code=request.hospital_code)
        return TokenResponse(
            access_token=tokens["access_token"],
            refresh_token=tokens["refresh_token"],
            token_type=tokens["token_type"],
            user=auth_user
        )
        
    except HTTPException:
        raise
    except Exception as e:
        logger.error("Doctor registration failed", error=str(e))
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail=f"Registration failed: {str(e)}"
        )


@router.post("/signup/hospital", response_model=TokenResponse, status_code=status.HTTP_201_CREATED)
async def signup_hospital(request: RegisterHospitalRequest):
    """
    Register a new hospital.
    
    - Auto-generates unique hospital_code (HC-XXXXXX)
    - Creates User + Hospital records
    - Returns JWT tokens + hospital_code
    """
    prisma = get_prisma()
    
    try:
        # Check if email already exists
        existing_user = await prisma.user.find_unique(where={"email": request.email})
        if existing_user:
            raise HTTPException(
                status_code=status.HTTP_400_BAD_REQUEST,
                detail="Email already registered"
            )
        
        # Generate unique hospital code
        hospital_code = await HospitalCodeGenerator.generate_unique_code()
        
        # Hash password (truncate to 72 bytes for bcrypt)
        password_hash = AuthService.hash_password(request.password[:72])
        
        # Prepare JSON arrays
        specializations_json = json.dumps(request.specializations) if request.specializations else None
        
        # Create user and hospital
        user = await prisma.user.create(
            data={
                "email": request.email,
                "password_hash": password_hash,
                "role": "HOSPITAL_ADMIN",
                "is_active": True,
                "hospital": {
                    "create": {
                        "name": request.name,
                        "registration_no": request.registration_no,
                        "license_no": request.license_no,
                        "accreditation": request.accreditation,
                        "hospital_code": hospital_code,
                        "facility_type": request.facility_type,
                        "specializations": specializations_json,
                        "phone_primary": request.phone_primary,
                        "phone_emergency": request.phone_emergency,
                        "email": request.email,
                        "website": request.website,
                        "address_line1": request.address_line1,
                        "address_line2": request.address_line2,
                        "city": request.city,
                        "state": request.state,
                        "postal_code": request.postal_code,
                        "country": "India",
                        "latitude": request.latitude,
                        "longitude": request.longitude,
                        "total_beds": request.total_beds,
                        "available_beds": request.total_beds,  # Initially all available
                        "icu_beds": request.icu_beds,
                        "emergency_beds": request.emergency_beds,
                        "operation_theatres": request.operation_theatres,
                        "has_emergency": request.has_emergency,
                        "has_ambulance": request.has_ambulance,
                        "has_pharmacy": request.has_pharmacy,
                        "has_lab": request.has_lab,
                        "has_blood_bank": request.has_blood_bank,
                        "telemedicine_enabled": request.telemedicine_enabled,
                        "is_verified": False,
                        "is_active": True,
                    }
                }
            },
            include={"hospital": True}
        )
        
        # Generate tokens
        tokens = await AuthService.create_tokens_for_user(user)
        
        # Add user info to response
        auth_user = AuthUserResponse(
            id=user.id,
            email=user.email,
            role=user.role,
            name=request.name,
            hospital_id=user.hospital.id if user.hospital else None,
            hospital_code=hospital_code
        )
        
        logger.info("Hospital registered successfully", user_id=user.id, email=user.email, hospital_code=hospital_code)
        return TokenResponse(
            access_token=tokens["access_token"],
            refresh_token=tokens["refresh_token"],
            token_type=tokens["token_type"],
            user=auth_user
        )
        
    except HTTPException:
        raise
    except Exception as e:
        logger.error("Hospital registration failed", error=str(e))
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail=f"Registration failed: {str(e)}"
        )


@router.post("/login", response_model=TokenResponse)
async def login(request: LoginRequest):
    """
    Login with email/aadhar and password.
    
    Accepts either email or aadhar (12-digit number) for authentication.
    Returns JWT tokens + role-specific user data.
    """
    prisma = get_prisma()
    
    try:
        # Authenticate using either email or aadhar
        user = await AuthService.authenticate_user(
            email=request.email,
            password=request.password,
            aadhar=request.aadhar
        )
        
        if not user:
            raise HTTPException(
                status_code=status.HTTP_401_UNAUTHORIZED,
                detail="Invalid credentials",
                headers={"WWW-Authenticate": "Bearer"},
            )
        
        # Check role filter if provided
        if request.role and user.role != request.role:
            raise HTTPException(
                status_code=status.HTTP_403_FORBIDDEN,
                detail=f"User is not a {request.role}"
            )
        
        # Load role-specific data
        patient_id = None
        doctor_id = None
        hospital_id = None
        aadhar_uid = None
        hospital_code = None
        name = None
        
        if user.role == "PATIENT":
            patient = await prisma.patient.find_unique(where={"user_id": user.id})
            if patient:
                patient_id = patient.id
                aadhar_uid = patient.aadhar_uid
                name = f"{patient.first_name} {patient.last_name}"
        
        elif user.role == "DOCTOR":
            doctor = await prisma.doctor.find_unique(where={"user_id": user.id})
            if doctor:
                doctor_id = doctor.id
                hospital_code = doctor.hospital_code_input
                name = f"{doctor.title} {doctor.first_name} {doctor.last_name}"
        
        elif user.role == "HOSPITAL_ADMIN":
            hospital = await prisma.hospital.find_unique(where={"user_id": user.id})
            if hospital:
                hospital_id = hospital.id
                hospital_code = hospital.hospital_code
                name = hospital.name
        
        # Generate tokens
        tokens = await AuthService.create_tokens_for_user(user)
        
        # Add user info to response
        auth_user = AuthUserResponse(
            id=user.id,
            email=user.email,
            role=user.role,
            name=name,
            patient_id=patient_id,
            doctor_id=doctor_id,
            hospital_id=hospital_id,
            aadhar_uid=aadhar_uid,
            hospital_code=hospital_code
        )
        
        logger.info("User logged in", user_id=user.id, role=user.role)
        return TokenResponse(
            access_token=tokens["access_token"],
            refresh_token=tokens["refresh_token"],
            token_type=tokens["token_type"],
            user=auth_user
        )
        
    except HTTPException:
        raise
    except Exception as e:
        logger.error("Login failed", error=str(e))
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail="Login failed"
        )


@router.post("/refresh", response_model=TokenResponse)
async def refresh_token(credentials: HTTPAuthorizationCredentials = Depends(security)):
    """
    Refresh access token using refresh token.
    """
    token = credentials.credentials
    payload = AuthService.decode_token(token)
    
    if not payload:
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail="Invalid or expired token",
            headers={"WWW-Authenticate": "Bearer"},
        )
    
    prisma = get_prisma()
    user = await prisma.user.find_unique(where={"id": payload["sub"]})
    
    if not user or not user.is_active:
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail="User not found or inactive"
        )
    
    tokens = await AuthService.create_tokens_for_user(user)
    
    # Load role-specific data
    patient_id = None
    doctor_id = None
    hospital_id = None
    aadhar_uid = None
    hospital_code = None
    name = None
    
    if user.role == "PATIENT":
        patient = await prisma.patient.find_unique(where={"user_id": user.id})
        if patient:
            patient_id = patient.id
            aadhar_uid = patient.aadhar_uid
            name = f"{patient.first_name} {patient.last_name}"
    
    elif user.role == "DOCTOR":
        doctor = await prisma.doctor.find_unique(where={"user_id": user.id})
        if doctor:
            doctor_id = doctor.id
            hospital_code = doctor.hospital_code_input
            name = f"{doctor.title} {doctor.first_name} {doctor.last_name}"
    
    elif user.role == "HOSPITAL_ADMIN":
        hospital = await prisma.hospital.find_unique(where={"user_id": user.id})
        if hospital:
            hospital_id = hospital.id
            hospital_code = hospital.hospital_code
            name = hospital.name
    
    auth_user = AuthUserResponse(
        id=user.id,
        email=user.email,
        role=user.role,
        name=name,
        patient_id=patient_id,
        doctor_id=doctor_id,
        hospital_id=hospital_id,
        aadhar_uid=aadhar_uid,
        hospital_code=hospital_code
    )
    
    return TokenResponse(
        access_token=tokens["access_token"],
        refresh_token=tokens["refresh_token"],
        token_type=tokens["token_type"],
        user=auth_user
    )


@router.get("/me", response_model=UserResponse)
async def get_current_user(credentials: HTTPAuthorizationCredentials = Depends(security)):
    """
    Get current authenticated user information.
    """
    token = credentials.credentials
    payload = AuthService.decode_token(token)
    
    if not payload:
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail="Invalid or expired token",
            headers={"WWW-Authenticate": "Bearer"},
        )
    
    prisma = get_prisma()
    user = await prisma.user.find_unique(where={"id": payload["sub"]})
    
    if not user:
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail="User not found"
        )
    
    return UserResponse(
        id=user.id,
        email=user.email,
        role=user.role,
        is_active=user.is_active,
        created_at=user.created_at.isoformat()
    )
