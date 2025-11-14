"""
Authentication Service

Handles JWT token generation, validation, and password management.
Provides secure authentication for all user types (Patient, Doctor, Hospital Admin).
"""

from datetime import datetime, timedelta
from typing import Optional, Dict, Any
from jose import JWTError, jwt
from passlib.context import CryptContext
import structlog

from app.core.config import settings
from app.core.database import get_prisma

logger = structlog.get_logger(__name__)

# Password hashing configuration
pwd_context = CryptContext(schemes=["bcrypt"], deprecated="auto")


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
        return pwd_context.hash(password)
    
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
        return pwd_context.verify(plain_password, hashed_password)
    
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
    async def authenticate_user(email: str, password: str):
        """
        Authenticate a user by email and password.
        
        Args:
            email: User's email
            password: User's password
            
        Returns:
            User object if authentication successful, None otherwise
        """
        prisma = get_prisma()
        
        # Find user by email
        user = await prisma.user.find_unique(where={"email": email})
        
        if not user:
            logger.warning("Authentication failed - user not found", email=email)
            return None
        
        # Verify password
        if not AuthService.verify_password(password, user.passwordHash):
            logger.warning("Authentication failed - invalid password", email=email)
            return None
        
        # Check if user is active
        if not user.isActive:
            logger.warning("Authentication failed - user inactive", email=email)
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
