#!/bin/bash
# Database Reset Script
# This will delete all data and recreate the schema with snake_case fields

set -e

echo "🗑️  Resetting CloudCare database..."

# Generate new Prisma client
echo "📦 Generating Prisma client with snake_case..."
cd /Volumes/My\ Drive/Priyodip/college\ notes\ and\ stuff/sem5/MAD/Mad_project/backend
prisma generate

# Reset database (deletes all data!)
echo "⚠️  WARNING: This will delete ALL existing data!"
read -p "Are you sure? (yes/no): " confirm
if [ "$confirm" != "yes" ]; then
    echo "❌ Aborted"
    exit 1
fi

echo "🔄 Resetting database..."
prisma migrate reset --force

echo "✅ Database reset complete!"
echo "📊 All tables now use snake_case field names"
echo "🚀 Backend server needs to be restarted"
