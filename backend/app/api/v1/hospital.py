"""
Hospital Router - Hospital profile and information endpoints
"""

from fastapi import APIRouter, HTTPException, status
from typing import Optional, List, Dict, Any
from datetime import datetime
import structlog

from app.core.database import get_prisma
from pydantic import BaseModel

logger = structlog.get_logger(__name__)
router = APIRouter(prefix="/hospitals")


class HospitalProfileResponse(BaseModel):
    id: str
    user_id: str
    name: str
    email: str
    phone_primary: Optional[str] = None
    phone_emergency: Optional[str] = None
    registration_no: Optional[str] = None
    hospital_code: Optional[str] = None
    address_line1: Optional[str] = None
    address_line2: Optional[str] = None
    city: Optional[str] = None
    state: Optional[str] = None
    postal_code: Optional[str] = None
    total_beds: int = 0
    available_beds: int = 0
    icu_beds: int = 0
    emergency_beds: int = 0
    total_doctors: int = 0
    
    # New Resources
    oxygen_cylinders: int = 0
    ventilators: int = 0
    ambulances: int = 0
    blood_bags: int = 0
    
    specializations: Optional[str] = None
    
    # Services
    has_emergency: bool = False
    has_ambulance: bool = False
    has_pharmacy: bool = False
    has_lab: bool = False
    has_blood_bank: bool = False
    telemedicine_enabled: bool = False
    
    created_at: datetime
    
    class Config:
        from_attributes = True

class HospitalDashboardStats(BaseModel):
    total_patients: int
    total_doctors: int
    emergency_cases: int
    available_beds: int
    total_beds: int
    occupancy_rate: float

class DoctorSummary(BaseModel):
    id: str
    name: str
    specialization: str
    is_available: bool
    phone: Optional[str]

class PatientSummary(BaseModel):
    id: str
    name: str
    age: int
    gender: str
    status: str  # Admitted, Appointment, etc.
    last_visit: Optional[datetime]

class ResourceUpdate(BaseModel):
    total_beds: Optional[int] = None
    available_beds: Optional[int] = None
    icu_beds: Optional[int] = None
    emergency_beds: Optional[int] = None
    oxygen_cylinders: Optional[int] = None
    ventilators: Optional[int] = None
    ambulances: Optional[int] = None
    blood_bags: Optional[int] = None

class AdmitPatientRequest(BaseModel):
    aadhar_number: str
    reason: Optional[str] = "Hospital Admission"

@router.get("/", response_model=List[HospitalProfileResponse])
async def list_hospitals():
    """
    List all registered hospitals.
    """
    prisma = get_prisma()
    try:
        hospitals = await prisma.hospital.find_many(
            where={"is_active": True}
        )
        
        results = []
        for h in hospitals:
            # Convert to dict and add total_doctors count (0 for list view to save query)
            h_data = h.model_dump() if hasattr(h, 'model_dump') else h.dict()
            h_data['total_doctors'] = 0 
            results.append(HospitalProfileResponse(**h_data))
            
        return results
    except Exception as e:
        logger.error("Failed to list hospitals", error=str(e))
        raise HTTPException(status_code=500, detail=str(e))

@router.get("/search", response_model=List[HospitalProfileResponse])
async def search_hospitals(query: Optional[str] = None):
    """
    Search hospitals by name or hospital code.
    If no query provided, returns all active hospitals.
    """
    prisma = get_prisma()
    try:
        where_clause = {"is_active": True}
        
        if query:
            # Search by name or hospital code (case-insensitive)
            where_clause = {
                "AND": [
                    {"is_active": True},
                    {
                        "OR": [
                            {"name": {"contains": query, "mode": "insensitive"}},
                            {"hospital_code": {"contains": query, "mode": "insensitive"}}
                        ]
                    }
                ]
            }
        
        hospitals = await prisma.hospital.find_many(
            where=where_clause,
            take=50  # Limit results
        )
        
        results = []
        for h in hospitals:
            h_data = h.model_dump() if hasattr(h, 'model_dump') else h.dict()
            h_data['total_doctors'] = 0
            results.append(HospitalProfileResponse(**h_data))
            
        return results
    except Exception as e:
        logger.error("Failed to search hospitals", error=str(e))
        raise HTTPException(status_code=500, detail=str(e))

@router.get("/{hospital_id}/dashboard", response_model=HospitalDashboardStats)
async def get_hospital_dashboard_stats(hospital_id: str):
    """
    Get dashboard statistics for the hospital.
    """
    prisma = get_prisma()
    try:
        hospital = await prisma.hospital.find_unique(
            where={"id": hospital_id},
            include={
                "emergency_cases": {"where": {"status": "IN_TREATMENT"}},
                "appointments": {"where": {"status": "SCHEDULED"}} 
            }
        )
        
        if not hospital:
            raise HTTPException(status_code=404, detail="Hospital not found")
            
        # Count doctors through DoctorHospital junction table
        doctor_count = await prisma.doctorhospital.count(
            where={"hospital_id": hospital_id}
        )
        
        # Calculate stats
        total_doctors = doctor_count
        emergency_cases = len(hospital.emergency_cases)
        
        # For total patients, we'll sum active appointments and emergency cases for now
        # In a real system, we'd query the DoctorPatient table for doctors in this hospital
        # But Prisma client limitations might make complex joins hard in one go.
        # Let's do a separate query for patients if needed, or just use the simple count.
        total_patients = len(hospital.appointments) + emergency_cases 
        
        available_beds = hospital.available_beds
        total_beds = hospital.total_beds
        occupancy_rate = ((total_beds - available_beds) / total_beds * 100) if total_beds > 0 else 0.0
        
        return HospitalDashboardStats(
            total_patients=total_patients,
            total_doctors=total_doctors,
            emergency_cases=emergency_cases,
            available_beds=available_beds,
            total_beds=total_beds,
            occupancy_rate=round(occupancy_rate, 1)
        )
        
    except Exception as e:
        logger.error("Failed to fetch dashboard stats", error=str(e))
        raise HTTPException(status_code=500, detail=str(e))

@router.get("/{hospital_id}/doctors", response_model=List[DoctorSummary])
async def get_hospital_doctors(hospital_id: str):
    """
    List all doctors assigned to the hospital.
    """
    prisma = get_prisma()
    try:
        # Get doctors through the DoctorHospital junction table
        doctor_associations = await prisma.doctorhospital.find_many(
            where={"hospital_id": hospital_id},
            include={"doctor": True}
        )
        
        return [
            DoctorSummary(
                id=assoc.doctor.id,
                name=f"{assoc.doctor.title} {assoc.doctor.first_name} {assoc.doctor.last_name}",
                specialization=assoc.doctor.specialization,
                is_available=assoc.doctor.is_active, # Using is_active as availability proxy
                phone=assoc.doctor.phone_primary
            ) for assoc in doctor_associations
        ]
    except Exception as e:
        logger.error("Failed to fetch doctors", error=str(e))
        raise HTTPException(status_code=500, detail=str(e))

class DischargePatientRequest(BaseModel):
    note: Optional[str] = None
    document_url: Optional[str] = None

@router.get("/{hospital_id}/patients", response_model=List[PatientSummary])
async def get_hospital_patients(hospital_id: str, status_filter: Optional[str] = None):
    """
    List patients associated with the hospital.
    Optional status_filter: 'active', 'scheduled', 'discharged'
    """
    prisma = get_prisma()
    try:
        # Get hospital details for name matching
        hospital = await prisma.hospital.find_unique(where={"id": hospital_id})
        if not hospital:
            raise HTTPException(status_code=404, detail="Hospital not found")

        patients_map = {}
        
        # Helper to calculate age
        def calculate_age(dob):
            if not dob: return 0
            today = datetime.now()
            return today.year - dob.year

        # 1. Active Patients (Emergency IN_TREATMENT, Appointments IN_PROGRESS)
        if not status_filter or status_filter == 'active':
            emergency_cases = await prisma.emergencycase.find_many(
                where={"hospital_id": hospital_id, "status": "IN_TREATMENT"},
                include={"patient": True}
            )
            
            active_appointments = await prisma.appointment.find_many(
                where={"hospital_id": hospital_id, "status": "IN_PROGRESS"},
                include={"patient": True}
            )
            
            pending_admissions = await prisma.consent.find_many(
                where={
                    "facility_name": hospital.name,
                    "request_type": "HOSPITAL_ADMISSION",
                    "status": "PENDING"
                },
                include={"patient": True}
            )

            for consent in pending_admissions:
                p = consent.patient
                patients_map[p.id] = PatientSummary(
                    id=p.id,
                    name=f"{p.first_name} {p.last_name}",
                    age=calculate_age(p.date_of_birth),
                    gender=p.gender,
                    status="Admission Pending",
                    last_visit=consent.requested_at
                )

            for case in emergency_cases:
                p = case.patient
                patients_map[p.id] = PatientSummary(
                    id=p.id,
                    name=f"{p.first_name} {p.last_name}",
                    age=calculate_age(p.date_of_birth),
                    gender=p.gender,
                    status="Emergency",
                    last_visit=case.updated_at
                )
                
            for appt in active_appointments:
                p = appt.patient
                patients_map[p.id] = PatientSummary(
                    id=p.id,
                    name=f"{p.first_name} {p.last_name}",
                    age=calculate_age(p.date_of_birth),
                    gender=p.gender,
                    status="Admitted",
                    last_visit=appt.date
                )

        # 2. Scheduled Patients (Appointments SCHEDULED)
        if not status_filter or status_filter == 'scheduled':
            scheduled_appointments = await prisma.appointment.find_many(
                where={"hospital_id": hospital_id, "status": "SCHEDULED"},
                include={"patient": True}
            )
            
            for appt in scheduled_appointments:
                p = appt.patient
                # Don't overwrite if already in active map (e.g. if fetching all)
                if p.id not in patients_map:
                    patients_map[p.id] = PatientSummary(
                        id=p.id,
                        name=f"{p.first_name} {p.last_name}",
                        age=calculate_age(p.date_of_birth),
                        gender=p.gender,
                        status="Appointment",
                        last_visit=appt.date
                    )

        # 3. Discharged Patients (Appointments COMPLETED, Emergency DISCHARGED)
        if not status_filter or status_filter == 'discharged':
            # Limit to last 50 for performance if no specific search
            completed_appointments = await prisma.appointment.find_many(
                where={"hospital_id": hospital_id, "status": "COMPLETED"},
                include={"patient": True},
                take=50,
                order={"date": "desc"}
            )
            
            discharged_emergency = await prisma.emergencycase.find_many(
                where={"hospital_id": hospital_id, "status": "DISCHARGED"},
                include={"patient": True},
                take=50,
                order={"updated_at": "desc"}
            )
            
            for case in discharged_emergency:
                p = case.patient
                if p.id not in patients_map:
                    patients_map[p.id] = PatientSummary(
                        id=p.id,
                        name=f"{p.first_name} {p.last_name}",
                        age=calculate_age(p.date_of_birth),
                        gender=p.gender,
                        status="Discharged (Emergency)",
                        last_visit=case.updated_at
                    )
            
            for appt in completed_appointments:
                p = appt.patient
                if p.id not in patients_map:
                    patients_map[p.id] = PatientSummary(
                        id=p.id,
                        name=f"{p.first_name} {p.last_name}",
                        age=calculate_age(p.date_of_birth),
                        gender=p.gender,
                        status="Discharged",
                        last_visit=appt.date
                    )
        
        return list(patients_map.values())
        
    except Exception as e:
        logger.error("Failed to fetch patients", error=str(e))
        raise HTTPException(status_code=500, detail=str(e))

@router.post("/{hospital_id}/patients/{patient_id}/discharge")
async def discharge_patient(hospital_id: str, patient_id: str, request: DischargePatientRequest):
    """
    Discharge a patient.
    Updates active appointments to COMPLETED and emergency cases to DISCHARGED.
    """
    prisma = get_prisma()
    try:
        # 1. Update Appointments
        await prisma.appointment.update_many(
            where={
                "hospital_id": hospital_id,
                "patient_id": patient_id,
                "status": "IN_PROGRESS"
            },
            data={"status": "COMPLETED", "notes": request.note} # Assuming notes field exists or we append
        )
        
        # 2. Update Emergency Cases
        await prisma.emergencycase.update_many(
            where={
                "hospital_id": hospital_id,
                "patient_id": patient_id,
                "status": "IN_TREATMENT"
            },
            data={"status": "DISCHARGED"}
        )
        
        return {"success": True, "message": "Patient discharged successfully"}
        
    except Exception as e:
        logger.error("Failed to discharge patient", error=str(e))
        raise HTTPException(status_code=500, detail=str(e))

@router.put("/{hospital_id}/resources")
async def update_hospital_resources(hospital_id: str, resources: ResourceUpdate):
    """
    Update hospital resources (beds).
    """
    prisma = get_prisma()
    try:
        data = resources.model_dump(exclude_unset=True)
        if not data:
            raise HTTPException(status_code=400, detail="No data provided")
            
        updated = await prisma.hospital.update(
            where={"id": hospital_id},
            data=data
        )
        
        return {"success": True, "message": "Resources updated", "data": updated}
    except Exception as e:
        logger.error("Failed to update resources", error=str(e))
        raise HTTPException(status_code=500, detail=str(e))

@router.get("/{hospital_id}/profile", response_model=HospitalProfileResponse)
async def get_hospital_profile(hospital_id: str):
    """
    Get hospital profile information by hospital ID.
    """
    prisma = get_prisma()
    
    try:
        hospital = await prisma.hospital.find_unique(
            where={"id": hospital_id},
            include={
                "doctors": True
            }
        )
        
        if not hospital:
            raise HTTPException(
                status_code=status.HTTP_404_NOT_FOUND,
                detail="Hospital not found"
            )
        
        # Convert to dict and add total_doctors count
        hospital_data = hospital.model_dump() if hasattr(hospital, 'model_dump') else hospital.dict()
        hospital_data['total_doctors'] = len(hospital.doctors) if hospital.doctors else 0
        
        return HospitalProfileResponse(**hospital_data)
        
    except HTTPException:
        raise
    except Exception as e:
        logger.error("Failed to fetch hospital profile", error=str(e))
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail="Failed to fetch hospital profile"
        )

@router.post("/{hospital_id}/admit")
async def admit_patient(hospital_id: str, request: AdmitPatientRequest):
    """
    Request to admit a patient by Aadhar number.
    Creates a consent request for HOSPITAL_ADMISSION.
    """
    prisma = get_prisma()
    try:
        # 1. Find Hospital
        hospital = await prisma.hospital.find_unique(where={"id": hospital_id})
        if not hospital:
            raise HTTPException(status_code=404, detail="Hospital not found")

        # 2. Find Patient by Aadhar (using UID service)
        from app.services.aadhar_uid import AadharUIDService
        try:
            uid = AadharUIDService.generate_uid(request.aadhar_number)
        except ValueError:
            raise HTTPException(status_code=400, detail="Invalid Aadhar number")
            
        patient = await prisma.patient.find_unique(where={"aadhar_uid": uid})
        if not patient:
            raise HTTPException(status_code=404, detail="Patient not found with this Aadhar")

        # 3. Check if already admitted (active appointment or emergency case)
        # For now, just check if there is a pending consent request
        existing_consent = await prisma.consent.find_first(
            where={
                "patient_id": patient.id,
                "facility_name": hospital.name,
                "request_type": "HOSPITAL_ADMISSION",
                "status": "PENDING"
            }
        )
        
        if existing_consent:
            return {"success": True, "message": "Admission request already pending", "consent_id": existing_consent.id}

        # 4. Create Consent Request
        from datetime import datetime, timedelta
        expires_at = datetime.now() + timedelta(days=1) # 1 day to approve admission
        
        consent = await prisma.consent.create(
            data={
                "patient_id": patient.id,
                "facility_name": hospital.name,
                "request_type": "HOSPITAL_ADMISSION",
                "description": f"Request for admission at {hospital.name}: {request.reason}",
                "status": "PENDING",
                "expires_at": expires_at
            }
        )
        
        return {"success": True, "message": "Admission request sent to patient", "consent_id": consent.id}

    except HTTPException:
        raise
    except Exception as e:
        logger.error("Failed to admit patient", error=str(e))
        raise HTTPException(status_code=500, detail=str(e))

