# CloudCare Backend API

FastAPI-based backend for CloudCare healthcare management system with Aadhar-based patient identification.

## 🏗️ Architecture

- **FastAPI**: Modern Python web framework
- **PostgreSQL**: Structured data (users, patients, doctors, hospitals, consents)
- **MongoDB**: High-frequency data (wearable health metrics)
- **Redis**: Caching and session management
- **Prisma**: Type-safe ORM for PostgreSQL
- **Docker**: Containerized deployment

## 🚀 Quick Start

### Prerequisites

- Docker and Docker Compose
- Python 3.11+ (for local development)
- (Optional) Cloudflare Tunnel for stable public URL

### 🌐 Public Access via Cloudflare Tunnel

The backend is accessible via Cloudflare Tunnel at:
**https://cloudcare.pipfactor.com**

This provides:
- ✅ Stable URL that never changes (no dynamic IPs)
- ✅ Works from anywhere (not just local WiFi)
- ✅ HTTPS with automatic SSL certificates
- ✅ Perfect for iOS/Android app testing

**Tunnel Configuration** (`~/.cloudflared/config.yml`):
```yaml
ingress:
  - hostname: cloudcare.pipfactor.com
    service: http://localhost:8000
```

**Start/Restart Tunnel**:
```bash
pkill cloudflared
cloudflared tunnel run ai-trading-frontend
```

### Using Docker (Recommended)

1. **Copy environment variables:**
   ```bash
   cp .env.example .env
   ```
   Edit `.env` and set your secret keys (especially `SECRET_KEY` and `AADHAR_ENCRYPTION_KEY`)

2. **Start all services:**
   ```bash
   docker-compose up -d
   ```

3. **Run Prisma migrations:**
   ```bash
   docker-compose exec api prisma migrate dev
   ```

4. **Access the API:**
   - Local: http://localhost:8000
   - Public (Cloudflare Tunnel): https://cloudcare.pipfactor.com
   - API Docs: https://cloudcare.pipfactor.com/docs
   - ReDoc: https://cloudcare.pipfactor.com/redoc

### Local Development

1. **Create virtual environment:**
   ```bash
   python -m venv venv
   source venv/bin/activate  # On Windows: venv\Scripts\activate
   ```

2. **Install dependencies:**
   ```bash
   pip install -r requirements.txt
   ```

3. **Start supporting services (PostgreSQL, MongoDB, Redis):**
   ```bash
   docker-compose up -d postgres mongodb redis
   ```

4. **Generate Prisma client:**
   ```bash
   prisma generate
   ```

5. **Run migrations:**
   ```bash
   prisma migrate dev
   ```

6. **Start the server:**
   ```bash
   uvicorn app.main:app --reload
   ```

## 📁 Project Structure

```
backend/
├── app/
│   ├── main.py                      # FastAPI application entry point
│   ├── core/
│   │   ├── config.py               # Configuration management
│   │   └── database.py             # Database connections (PostgreSQL, MongoDB, Redis)
│   ├── api/
│   │   └── v1/                     # API version 1
│   │       ├── auth.py             # Authentication endpoints
│   │       ├── wearables.py        # Wearables & health data (iOS/Android)
│   │       ├── patient.py          # Patient management
│   │       ├── doctor.py           # Doctor management
│   │       ├── hospital.py         # Hospital management
│   │       ├── consents.py         # Consent management
│   │       ├── documents.py        # Document requests
│   │       └── health.py           # Health data endpoints
│   ├── services/
│   │   ├── auth_service.py         # Authentication logic
│   │   ├── aadhar_uid.py           # Aadhar UID generation (HMAC-SHA256)
│   │   ├── wearables_service.py    # Individual metrics storage & deduplication
│   │   └── apple_health_parser.py  # Apple Health JSON parser
│   └── models/
│       ├── auth.py                 # Auth Pydantic models
│       └── wearables.py            # Wearables Pydantic models
├── prisma/
│   └── schema.prisma               # PostgreSQL database schema
├── docker-compose.yml              # Multi-container setup (API, PostgreSQL, MongoDB, Redis)
├── Dockerfile                      # API container
├── requirements.txt                # Python dependencies
├── scripts/
│   └── start_local.sh             # Auto-detect WiFi IP for local testing
└── .env.example                   # Environment template
```

## 🔑 Key Features

### 1. Aadhar-Based Patient Identification
- Universal patient UID generated from Aadhar number using HMAC-SHA256
- Enables cross-hospital patient identification
- Secure and irreversible UID generation

### 2. Dual Database Strategy
- **PostgreSQL**: Critical structured data (authentication, patient records, consents)
- **MongoDB**: High-throughput wearable data (health metrics, device data)
- **Individual Metrics Storage**: Each health reading stored separately for AI/ML analysis
- **Time-Series Optimization**: Efficient indexing for temporal queries

### 3. Real-time Health Monitoring
- Automatic health alerts based on wearable data
- Emergency alert system for critical vitals
- 24/7 health metrics tracking
- **Apple Health Integration**: Direct import from iPhone/Apple Watch
- **Multi-level Deduplication**: App-level + Backend-level duplicate prevention

### 4. Document Request System
- Request medical documents from other hospitals
- Consent-based access control
- Audit trail for all data access

### 5. QR Code Device Pairing
- Secure pairing between iOS CloudSync app and Android app
- QR code generation with expiring tokens
- Link Apple Watch data to patient accounts

## 🔐 Security

- **JWT Authentication**: Secure token-based auth
- **Bcrypt Password Hashing**: Industry-standard password security
- **Aadhar Encryption**: HMAC-SHA256 for UID generation
- **CORS Protection**: Configurable origin whitelist
- **Role-Based Access Control**: Patient, Doctor, Hospital Admin roles

## 📊 API Endpoints

### Authentication
- `POST /api/v1/auth/register/patient` - Register new patient
- `POST /api/v1/auth/login` - Login
- `POST /api/v1/auth/refresh` - Refresh token
- `GET /api/v1/auth/me` - Get current user

### Wearables & Health Metrics
- `POST /api/v1/wearables/devices` - Register device
- `GET /api/v1/wearables/devices` - Get devices
- `GET /api/v1/wearables/devices/paired` - Get paired devices for user
- `POST /api/v1/wearables/devices/pair` - Pair iOS device to Android user (QR code)
- `POST /api/v1/wearables/sync` - Sync health data
- `POST /api/v1/wearables/import/apple-health` - Import Apple Health JSON (single)
- `POST /api/v1/wearables/import/apple-health/batch` - Import Apple Health JSON (batch)
- `GET /api/v1/wearables/metrics/recent?hours=24` - Get recent individual metrics
- `GET /api/v1/wearables/metrics/by-type?type=heart_rate` - Get specific metric type
- `GET /api/v1/wearables/summary/today` - Get today's health summary
- `GET /api/v1/wearables/summary` - Get aggregated health summary

### Health
- `GET /health` - Health check endpoint

*Full API documentation available at `/docs` when server is running.*

## 🛠️ Database Management

### Run Migrations
```bash
docker-compose exec api prisma migrate dev --name migration_name
```

### Generate Prisma Client
```bash
docker-compose exec api prisma generate
```

### Reset Database (Development Only)
```bash
docker-compose exec api prisma migrate reset
```

### View Data with Prisma Studio
```bash
docker-compose exec api prisma studio
```

## 📝 Logging

Structured logging using `structlog`:
- All requests logged with timestamps
- Error tracking with context
- Log level configurable via `LOG_LEVEL` env var
- Logs stored in `logs/app.log`

## 🐛 Error Handling

The API provides clear error messages:
- **400**: Bad Request (validation errors, invalid data)
- **401**: Unauthorized (invalid/expired token)
- **403**: Forbidden (insufficient permissions)
- **404**: Not Found (resource doesn't exist)
- **500**: Internal Server Error (logged for debugging)

All errors include:
- Clear error messages
- Error IDs for tracing in logs
- Validation details when applicable

## 🧪 Testing

```bash
pytest
```

## 📦 Docker Services

### Services Overview
- **api** (port 8000): FastAPI application
- **postgres** (port 5432): PostgreSQL 15
- **mongodb** (port 27017): MongoDB 6.0
- **redis** (port 6379): Redis 7

### Service Management
```bash
# Start all services
docker-compose up -d

# View logs
docker-compose logs -f api

# Stop all services
docker-compose down

# Remove volumes (data will be lost!)
docker-compose down -v
```

## 🔧 Environment Variables

Key variables in `.env`:

```bash
# Security (CHANGE THESE!)
SECRET_KEY=your-secret-key-change-this
AADHAR_ENCRYPTION_KEY=your-aadhar-key-change-this

# Database URLs (set by docker-compose)
DATABASE_URL=postgresql://cloudcare:cloudcare_password@postgres:5432/cloudcare_db
MONGODB_URL=mongodb://mongodb:27017
REDIS_URL=redis://redis:6379/0

# Public URL (Cloudflare Tunnel)
CLOUDFLARE_TUNNEL_URL=https://cloudcare.pipfactor.com

# CORS (add your frontend URLs)
CORS_ORIGINS=http://localhost:3000,http://localhost:8080
```

## 📈 Production Deployment

1. **Update environment variables** in `.env` for production
2. **Set DEBUG=False** and **ENVIRONMENT=production**
3. **Use strong secrets** for all keys
4. **Configure HTTPS** with reverse proxy (nginx/traefik)
5. **Set up monitoring** and log aggregation
6. **Enable automated backups** for PostgreSQL and MongoDB
7. **Configure CORS** for your production frontend domain

## 🤝 Contributing

1. Follow PEP 8 style guide
2. Add type hints to all functions
3. Include docstrings for all public APIs
4. Write tests for new features
5. Use structured logging for debugging

## 📚 Additional Resources

- [FastAPI Documentation](https://fastapi.tiangolo.com/)
- [Prisma Python Docs](https://prisma-client-py.readthedocs.io/)
- [Pydantic Documentation](https://docs.pydantic.dev/)

## 📄 License

Part of CloudCare Healthcare Management System
