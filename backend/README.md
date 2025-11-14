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
   - API: http://localhost:8000
   - Docs: http://localhost:8000/docs
   - ReDoc: http://localhost:8000/redoc

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
│   ├── main.py                 # FastAPI application entry point
│   ├── core/
│   │   ├── config.py          # Configuration management
│   │   └── database.py        # Database connections
│   ├── api/
│   │   └── v1/                # API version 1
│   │       ├── auth.py        # Authentication endpoints
│   │       ├── wearables.py   # Wearables & health data
│   │       ├── patient.py     # Patient management
│   │       ├── doctor.py      # Doctor management
│   │       ├── hospital.py    # Hospital management
│   │       ├── consents.py    # Consent management
│   │       ├── documents.py   # Document requests
│   │       └── health.py      # Health data endpoints
│   ├── services/
│   │   ├── auth_service.py    # Authentication logic
│   │   ├── aadhar_uid.py      # Aadhar UID generation
│   │   └── wearables_service.py # Wearables data handling
│   └── models/
│       ├── auth.py            # Auth Pydantic models
│       └── wearables.py       # Wearables Pydantic models
├── prisma/
│   └── schema.prisma          # Database schema
├── docker-compose.yml         # Multi-container setup
├── Dockerfile                 # API container
├── requirements.txt           # Python dependencies
└── .env.example              # Environment template
```

## 🔑 Key Features

### 1. Aadhar-Based Patient Identification
- Universal patient UID generated from Aadhar number using HMAC-SHA256
- Enables cross-hospital patient identification
- Secure and irreversible UID generation

### 2. Dual Database Strategy
- **PostgreSQL**: Critical structured data (authentication, patient records, consents)
- **MongoDB**: High-throughput wearable data (health metrics, device data)

### 3. Real-time Health Monitoring
- Automatic health alerts based on wearable data
- Emergency alert system for critical vitals
- 24/7 health metrics tracking

### 4. Document Request System
- Request medical documents from other hospitals
- Consent-based access control
- Audit trail for all data access

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

### Wearables
- `POST /api/v1/wearables/devices` - Register device
- `GET /api/v1/wearables/devices` - Get devices
- `POST /api/v1/wearables/sync` - Sync health data
- `GET /api/v1/wearables/metrics/recent` - Get recent metrics
- `GET /api/v1/wearables/summary` - Get health summary

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
