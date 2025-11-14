"""
Authentication Router

Handles user registration, login, and token management.
"""

from fastapi import APIRouter, HTTPException, status, Depends
from fastapi.security import HTTPBearer, HTTPAuthorizationCredentials
import structlog

from app.models.auth import LoginRequest, RegisterPatientRequest, TokenResponse, UserResponse
from app.services.auth_service import AuthService
from app.services.aadhar_uid import AadharUIDService
from app.core.database import get_prisma

logger = structlog.get_logger(__name__)
router = APIRouter(prefix="/auth")
security = HTTPBearer()


@router.post("/register/patient", response_model=TokenResponse, status_code=status.HTTP_201_CREATED)
async def register_patient(request: RegisterPatientRequest):
    """
    Register a new patient.
    
    Creates user account and patient profile with Aadhar-based UID.
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
                detail=str(e)
            )
        
        # Check if Aadhar UID already exists
        existing_patient = await prisma.patient.find_unique(where={"aadharUid": aadhar_uid})
        if existing_patient:
            raise HTTPException(
                status_code=status.HTTP_400_BAD_REQUEST,
                detail="Patient with this Aadhar number already registered"
            )
        
        # Hash password
        password_hash = AuthService.hash_password(request.password)
        
        # Create user and patient in a transaction
        user = await prisma.user.create(
            data={
                "email": request.email,
                "passwordHash": password_hash,
                "role": "PATIENT",
                "patient": {
                    "create": {
                        "aadharUid": aadhar_uid,
                        "name": request.name,
                        "age": request.age,
                        "gender": request.gender,
                        "bloodType": request.blood_type,
                        "contact": request.contact,
                        "email": request.email,
                        "address": request.address,
                        "familyContact": request.family_contact,
                        "insuranceProvider": request.insurance_provider,
                        "insuranceId": request.insurance_id,
                        "occupation": request.occupation,
                    }
                }
            },
            include={"patient": True}
        )
        
        # Generate tokens
        tokens = await AuthService.create_tokens_for_user(user)
        
        logger.info("Patient registered successfully", user_id=user.id, email=user.email)
        return tokens
        
    except HTTPException:
        raise
    except Exception as e:
        logger.error("Registration failed", error=str(e))
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail="Registration failed. Please try again."
        )


@router.post("/login", response_model=TokenResponse)
async def login(request: LoginRequest):
    """
    Login with email and password.
    
    Returns JWT tokens for authenticated access.
    """
    user = await AuthService.authenticate_user(request.email, request.password)
    
    if not user:
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail="Invalid email or password",
            headers={"WWW-Authenticate": "Bearer"},
        )
    
    tokens = await AuthService.create_tokens_for_user(user)
    
    logger.info("User logged in", user_id=user.id, role=user.role)
    return tokens


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
    
    if not user or not user.isActive:
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail="User not found or inactive"
        )
    
    tokens = await AuthService.create_tokens_for_user(user)
    return tokens


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
        is_active=user.isActive,
        created_at=user.createdAt.isoformat()
    )
