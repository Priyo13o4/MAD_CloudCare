#!/bin/bash

# CloudCare Backend Setup Script
# This script helps you get the backend up and running quickly

set -e  # Exit on error

echo "🏥 CloudCare Backend Setup"
echo "=========================="
echo ""

# Check if we're in the right directory
if [ ! -f "docker-compose.yml" ]; then
    echo "❌ Error: docker-compose.yml not found"
    echo "Please run this script from the backend directory"
    exit 1
fi

# Step 1: Check Docker
echo "📦 Step 1: Checking Docker installation..."
if ! command -v docker &> /dev/null; then
    echo "❌ Docker not found. Please install Docker first:"
    echo "   https://docs.docker.com/get-docker/"
    exit 1
fi

if ! command -v docker-compose &> /dev/null; then
    echo "❌ Docker Compose not found. Please install Docker Compose first:"
    echo "   https://docs.docker.com/compose/install/"
    exit 1
fi

echo "✅ Docker and Docker Compose found"
echo ""

# Step 2: Setup environment
echo "📝 Step 2: Setting up environment variables..."
if [ ! -f ".env" ]; then
    if [ -f ".env.example" ]; then
        cp .env.example .env
        echo "✅ Created .env from .env.example"
        echo "⚠️  WARNING: Please update SECRET_KEY and AADHAR_ENCRYPTION_KEY in .env before production!"
        echo ""
    else
        echo "❌ .env.example not found"
        exit 1
    fi
else
    echo "✅ .env file already exists"
    echo ""
fi

# Step 3: Start services
echo "🚀 Step 3: Starting Docker services..."
echo "This will download images and start all containers..."
docker-compose up -d

echo ""
echo "⏳ Waiting for databases to be ready..."
sleep 10

# Step 4: Generate Prisma client
echo "🔧 Step 4: Generating Prisma client..."
docker-compose exec -T api prisma generate || {
    echo "⚠️  Warning: Prisma generate failed. This is normal on first run."
    echo "   Restarting API container..."
    docker-compose restart api
    sleep 5
    docker-compose exec -T api prisma generate
}

# Step 5: Run migrations
echo "🗄️  Step 5: Running database migrations..."
docker-compose exec -T api prisma migrate deploy || {
    echo "⚠️  Warning: Migrations failed. Creating initial migration..."
    docker-compose exec -T api prisma migrate dev --name init
}

echo ""
echo "✅ Setup complete!"
echo ""
echo "🎉 CloudCare Backend is now running!"
echo ""
echo "📍 Access points:"
echo "   - API:        http://localhost:8000"
echo "   - Docs:       http://localhost:8000/docs"
echo "   - ReDoc:      http://localhost:8000/redoc"
echo "   - PostgreSQL: localhost:5432"
echo "   - MongoDB:    localhost:27017"
echo "   - Redis:      localhost:6379"
echo ""
echo "🔍 Useful commands:"
echo "   - View logs:           docker-compose logs -f api"
echo "   - Stop services:       docker-compose down"
echo "   - Restart API:         docker-compose restart api"
echo "   - Access database:     docker-compose exec postgres psql -U cloudcare -d cloudcare_db"
echo "   - Prisma Studio:       docker-compose exec api prisma studio"
echo ""
echo "⚠️  IMPORTANT: Before deploying to production:"
echo "   1. Update SECRET_KEY in .env"
echo "   2. Update AADHAR_ENCRYPTION_KEY in .env"
echo "   3. Set DEBUG=False"
echo "   4. Set ENVIRONMENT=production"
echo "   5. Configure proper CORS_ORIGINS"
echo ""
