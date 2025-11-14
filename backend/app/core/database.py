"""
Database Connection Management

Handles initialization and lifecycle of all database connections:
- PostgreSQL (via Prisma)
- MongoDB (async driver)
- Redis (cache)

All connections are properly managed to prevent leaks and ensure clean shutdown.
"""

from prisma import Prisma
from motor.motor_asyncio import AsyncIOMotorClient
from redis.asyncio import Redis
import structlog

from app.core.config import settings

logger = structlog.get_logger(__name__)

# Global database instances
prisma_client: Prisma = Prisma()
mongodb_client: AsyncIOMotorClient = None
redis_client: Redis = None


async def init_databases():
    """
    Initialize all database connections.
    
    Called during application startup. Each connection is tested
    to ensure it's working properly before proceeding.
    """
    global mongodb_client, redis_client
    
    # Initialize Prisma (PostgreSQL)
    try:
        await prisma_client.connect()
        logger.info("✅ PostgreSQL connected via Prisma")
    except Exception as e:
        logger.error(f"❌ Failed to connect to PostgreSQL: {e}")
        raise
    
    # Initialize MongoDB
    try:
        mongodb_client = AsyncIOMotorClient(settings.MONGODB_URL)
        # Test connection
        await mongodb_client.admin.command('ping')
        logger.info("✅ MongoDB connected")
    except Exception as e:
        logger.error(f"❌ Failed to connect to MongoDB: {e}")
        raise
    
    # Initialize Redis
    try:
        redis_client = Redis.from_url(
            settings.REDIS_URL,
            encoding="utf-8",
            decode_responses=True
        )
        # Test connection
        await redis_client.ping()
        logger.info("✅ Redis connected")
    except Exception as e:
        logger.error(f"❌ Failed to connect to Redis: {e}")
        raise


async def close_databases():
    """
    Close all database connections gracefully.
    
    Called during application shutdown to ensure proper cleanup.
    """
    global mongodb_client, redis_client
    
    # Close Prisma
    try:
        await prisma_client.disconnect()
        logger.info("✅ PostgreSQL disconnected")
    except Exception as e:
        logger.error(f"❌ Error disconnecting PostgreSQL: {e}")
    
    # Close MongoDB
    if mongodb_client:
        try:
            mongodb_client.close()
            logger.info("✅ MongoDB disconnected")
        except Exception as e:
            logger.error(f"❌ Error disconnecting MongoDB: {e}")
    
    # Close Redis
    if redis_client:
        try:
            await redis_client.close()
            logger.info("✅ Redis disconnected")
        except Exception as e:
            logger.error(f"❌ Error disconnecting Redis: {e}")


def get_mongodb():
    """
    Get MongoDB database instance.
    
    Returns:
        AsyncIOMotorDatabase: MongoDB database instance
    """
    if mongodb_client is None:
        raise RuntimeError("MongoDB not initialized")
    return mongodb_client[settings.MONGODB_DB]


def get_redis():
    """
    Get Redis client instance.
    
    Returns:
        Redis: Redis client instance
    """
    if redis_client is None:
        raise RuntimeError("Redis not initialized")
    return redis_client


def get_prisma():
    """
    Get Prisma client instance.
    
    Returns:
        Prisma: Prisma client instance
    """
    if not prisma_client.is_connected():
        raise RuntimeError("Prisma not connected")
    return prisma_client
