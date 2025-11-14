"""
Aadhar UID Generation Service

Implements secure UID generation from Aadhar numbers using HMAC-SHA256.
This ensures:
1. Same Aadhar always generates the same UID
2. UID cannot be reverse-engineered to get Aadhar
3. Universal patient identification across healthcare system
"""

import hmac
import hashlib
from typing import Optional
import structlog

from app.core.config import settings

logger = structlog.get_logger(__name__)


class AadharUIDService:
    """
    Service for generating and validating Aadhar-based UIDs.
    
    Uses HMAC-SHA256 to create deterministic but irreversible UIDs.
    """
    
    @staticmethod
    def generate_uid(aadhar_number: str) -> str:
        """
        Generate a unique patient ID from Aadhar number.
        
        Args:
            aadhar_number: 12-digit Aadhar number
            
        Returns:
            str: 64-character hexadecimal UID
            
        Raises:
            ValueError: If Aadhar number is invalid
        """
        # Validate Aadhar number format
        if not AadharUIDService.validate_aadhar(aadhar_number):
            raise ValueError("Invalid Aadhar number format")
        
        # Remove any spaces or dashes
        clean_aadhar = aadhar_number.replace(" ", "").replace("-", "")
        
        # Generate HMAC-SHA256
        uid = hmac.new(
            settings.AADHAR_ENCRYPTION_KEY.encode(),
            clean_aadhar.encode(),
            hashlib.sha256
        ).hexdigest()
        
        logger.info("Generated UID for Aadhar", uid_prefix=uid[:8])
        return uid
    
    @staticmethod
    def validate_aadhar(aadhar_number: str) -> bool:
        """
        Validate Aadhar number format.
        
        Args:
            aadhar_number: Aadhar number to validate
            
        Returns:
            bool: True if valid, False otherwise
        """
        # Remove spaces and dashes
        clean_aadhar = aadhar_number.replace(" ", "").replace("-", "")
        
        # Check if it's exactly 12 digits
        if not clean_aadhar.isdigit() or len(clean_aadhar) != 12:
            return False
        
        # Optionally: Add Verhoeff algorithm check for Aadhar validation
        # For now, basic format check is sufficient
        
        return True
    
    @staticmethod
    def verify_uid(aadhar_number: str, uid: str) -> bool:
        """
        Verify if a UID matches an Aadhar number.
        
        Args:
            aadhar_number: Aadhar number to check
            uid: UID to verify
            
        Returns:
            bool: True if UID matches Aadhar, False otherwise
        """
        try:
            generated_uid = AadharUIDService.generate_uid(aadhar_number)
            return hmac.compare_digest(generated_uid, uid)
        except ValueError:
            return False
