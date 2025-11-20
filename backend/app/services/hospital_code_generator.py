"""
Hospital Code Generator Service

Generates unique hospital codes for hospital registration.
Format: HC-XXXXXX (where X is alphanumeric)
"""

import random
import string
import structlog
from app.core.database import get_prisma

logger = structlog.get_logger(__name__)


class HospitalCodeGenerator:
    """
    Service for generating unique hospital codes.
    """
    
    @staticmethod
    async def generate_unique_code() -> str:
        """
        Generate a unique hospital code.
        
        Format: HC-XXXXXX (8 characters total)
        Example: HC-AB12CD
        
        Returns:
            str: Unique hospital code
        """
        prisma = get_prisma()
        max_attempts = 100
        
        for attempt in range(max_attempts):
            # Generate random 6-character alphanumeric code
            code_suffix = ''.join(random.choices(
                string.ascii_uppercase + string.digits,
                k=6
            ))
            code = f"HC-{code_suffix}"
            
            # Check if code already exists
            existing = await prisma.hospital.find_unique(
                where={"hospital_code": code}
            )
            
            if not existing:
                logger.info("Generated unique hospital code", code=code, attempts=attempt + 1)
                return code
        
        # If we couldn't generate a unique code after max_attempts
        logger.error("Failed to generate unique hospital code after max attempts")
        raise ValueError("Unable to generate unique hospital code. Please try again.")
    
    @staticmethod
    def validate_hospital_code_format(code: str) -> bool:
        """
        Validate hospital code format.
        
        Args:
            code: Hospital code to validate
            
        Returns:
            bool: True if valid format, False otherwise
        """
        if not code or len(code) != 9:
            return False
        
        if not code.startswith("HC-"):
            return False
        
        suffix = code[3:]
        if len(suffix) != 6:
            return False
        
        # Check if suffix is alphanumeric
        return all(c in string.ascii_uppercase + string.digits for c in suffix)
