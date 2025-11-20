"""
Authentication Service

Handles JWT token generation, validation, and password management.
Provides secure authentication for all user types (Patient, Doctor, Hospital Admin).
"""

from datetime import datetime, timedelta
from typing import Optional, Dict, Any
from jose import JWTError, jwt
import bcrypt
import structlog

from app.core.config import settings
from app.core.database import get_prisma
from app.services.aadhar_uid import AadharUIDService

logger = structlog.get_logger(__name__)


class AuthService:
    """
    Authentication service for user management and token operations.
    """
    
    @staticmethod
    def hash_password(password: str) -> str:
        """
        Hash a password using bcrypt.
        
        Args:
            password: Plain text password
            
        Returns:
            str: Hashed password
        """
        # Truncate to 72 bytes (bcrypt limit) and encode
        password_bytes = password[:72].encode('utf-8')
        hashed = bcrypt.hashpw(password_bytes, bcrypt.gensalt())
        return hashed.decode('utf-8')
    
    @staticmethod
    def verify_password(plain_password: str, hashed_password: str) -> bool:
        """
        Verify a password against its hash.
        
        Args:
            plain_password: Plain text password to verify
            hashed_password: Hashed password to compare against
            
        Returns:
            bool: True if password matches, False otherwise
        """
        password_bytes = plain_password[:72].encode('utf-8')
        hashed_bytes = hashed_password.encode('utf-8')
        return bcrypt.checkpw(password_bytes, hashed_bytes)
    
    @staticmethod
    def create_access_token(data: Dict[str, Any], expires_delta: Optional[timedelta] = None) -> str:
        """
        Create a JWT access token.
        
        Args:
            data: Data to encode in the token (e.g., user_id, role)
            expires_delta: Custom expiration time (optional)
            
        Returns:
            str: JWT token
        """
        to_encode = data.copy()
        
        if expires_delta:
            expire = datetime.utcnow() + expires_delta
        else:
            expire = datetime.utcnow() + timedelta(minutes=settings.ACCESS_TOKEN_EXPIRE_MINUTES)
        
        to_encode.update({
            "exp": expire,
            "iat": datetime.utcnow(),
        })
        
        encoded_jwt = jwt.encode(
            to_encode,
            settings.SECRET_KEY,
            algorithm=settings.JWT_ALGORITHM
        )
        
        logger.info("Created access token", user_id=data.get("sub"), expires_at=expire)
        return encoded_jwt
    
    @staticmethod
    def create_refresh_token(data: Dict[str, Any]) -> str:
        """
        Create a JWT refresh token with longer expiration.
        
        Args:
            data: Data to encode in the token
            
        Returns:
            str: JWT refresh token
        """
        expires_delta = timedelta(days=settings.REFRESH_TOKEN_EXPIRE_DAYS)
        return AuthService.create_access_token(data, expires_delta)
    
    @staticmethod
    def decode_token(token: str) -> Optional[Dict[str, Any]]:
        """
        Decode and validate a JWT token.
        
        Args:
            token: JWT token to decode
            
        Returns:
            Optional[Dict]: Token payload if valid, None otherwise
        """
        try:
            payload = jwt.decode(
                token,
                settings.SECRET_KEY,
                algorithms=[settings.JWT_ALGORITHM]
            )
            return payload
        except JWTError as e:
            logger.warning("Token decode failed", error=str(e))
            return None
    
    @staticmethod
    async def authenticate_user(email: Optional[str] = None, password: str = "", aadhar: Optional[str] = None):
        """
        Authenticate a user by email or aadhar and password.
        
        Args:
            email: User's email (optional)
            password: User's password
            aadhar: User's Aadhar number (optional) - will be hashed to match aadhar_uid
            
        Returns:
            User object if authentication successful, None otherwise
        """
        prisma = get_prisma()
        
        user = None
        
        # Lookup by email if provided
        if email:
            user = await prisma.user.find_unique(where={"email": email})
            if not user:
                logger.warning("Authentication failed - user not found by email", email=email)
                return None
        
        # Lookup by aadhar if provided
        elif aadhar:
            # Hash the aadhar number to get aadhar_uid for lookup
            aadhar_uid = AadharUIDService.generate_uid(aadhar)
            
            # Find patient by aadhar_uid, then get associated user
            patient = await prisma.patient.find_unique(where={"aadhar_uid": aadhar_uid})
            if not patient:
                logger.warning("Authentication failed - patient not found by aadhar", aadhar_uid=aadhar_uid[:16])
                return None
            
            # Get user by patient's user_id
            user = await prisma.user.find_unique(where={"id": patient.user_id})
            if not user:
                logger.warning("Authentication failed - user not found for patient", patient_id=patient.id)
                return None
        
        if not user:
            return None
        
        # Verify password
        if not AuthService.verify_password(password, user.password_hash):
            logger.warning("Authentication failed - invalid password", user_id=user.id)
            return None
        
        # Check if user is active
        if not user.is_active:
            logger.warning("Authentication failed - user inactive", user_id=user.id)
            return None
        
        logger.info("User authenticated successfully", user_id=user.id, role=user.role)
        return user
    
    @staticmethod
    async def create_tokens_for_user(user) -> Dict[str, str]:
        """
        Create access and refresh tokens for a user.
        
        Args:
            user: User object
            
        Returns:
            Dict with access_token and refresh_token
        """
        token_data = {
            "sub": user.id,
            "email": user.email,
            "role": user.role,
        }
        
        access_token = AuthService.create_access_token(token_data)
        refresh_token = AuthService.create_refresh_token({"sub": user.id})
        
        return {
            "access_token": access_token,
            "refresh_token": refresh_token,
            "token_type": "bearer",
        }
