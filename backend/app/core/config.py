"""
Core Configuration Module

Centralized configuration management using Pydantic Settings.
All environment variables are validated and type-checked.
"""

from pydantic_settings import BaseSettings, SettingsConfigDict
from pydantic import field_validator
from typing import List, Union
import os


class Settings(BaseSettings):
    """
    Application settings loaded from environment variables.
    
    Provides type safety and validation for all configuration values.
    Makes it easy to change settings without modifying code.
    """
    
    # Server Configuration
    ENVIRONMENT: str = "development"
    DEBUG: bool = True
    API_VERSION: str = "v1"
    PORT: int = 8000
    
    # Security
    SECRET_KEY: str
    AADHAR_ENCRYPTION_KEY: str
    JWT_ALGORITHM: str = "HS256"
    ACCESS_TOKEN_EXPIRE_MINUTES: int = 30
    REFRESH_TOKEN_EXPIRE_DAYS: int = 7
    
    # PostgreSQL Database
    DATABASE_URL: str
    
    # MongoDB
    MONGODB_URL: str
    MONGODB_DB: str = "cloudcare_wearables"
    
    # Redis Cache
    REDIS_URL: str
    REDIS_CACHE_TTL: int = 3600
    
    # CORS Settings
    CORS_ORIGINS: Union[List[str], str] = ["http://localhost:3000", "http://localhost:8080"]
    
    @field_validator("CORS_ORIGINS", mode="before")
    @classmethod
    def parse_cors_origins(cls, v):
        """Parse CORS_ORIGINS from comma-separated string or list."""
        if isinstance(v, str):
            return [origin.strip() for origin in v.split(",") if origin.strip()]
        return v
    
    # Wearables Configuration
    WEARABLES_SYNC_INTERVAL_MINUTES: int = 15
    WEARABLES_DATA_RETENTION_DAYS: int = 90
    
    # File Upload
    MAX_UPLOAD_SIZE_MB: int = 10
    ALLOWED_FILE_TYPES: Union[List[str], str] = ["pdf", "jpg", "jpeg", "png", "doc", "docx"]
    
    # Public URL (Cloudflare Tunnel)
    CLOUDFLARE_TUNNEL_URL: str = "https://cloudcare.pipfactor.com"

    @field_validator("ALLOWED_FILE_TYPES", mode="before")
    @classmethod
    def parse_allowed_file_types(cls, v):
        """Parse ALLOWED_FILE_TYPES from comma-separated string or list."""
        if isinstance(v, str):
            return [ftype.strip() for ftype in v.split(",") if ftype.strip()]
        return v
    
    # Logging
    LOG_LEVEL: str = "INFO"
    LOG_FILE: str = "logs/app.log"
    
    model_config = SettingsConfigDict(
        env_file=".env",
        env_file_encoding="utf-8",
        case_sensitive=True,
        extra="ignore",  # Ignore extra environment variables not defined in the model
    )
    
    @property
    def is_development(self) -> bool:
        """Check if running in development mode."""
        return self.ENVIRONMENT.lower() == "development"
    
    @property
    def is_production(self) -> bool:
        """Check if running in production mode."""
        return self.ENVIRONMENT.lower() == "production"


# Initialize settings (singleton pattern)
settings = Settings()
