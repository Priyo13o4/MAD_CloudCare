"""
Doctor Router - Doctor profile and information endpoints
"""

from fastapi import APIRouter, HTTPException, status, Depends
from typing import Optional, List
from datetime import datetime
import structlog

from app.core.database import get_prisma
from prisma import Prisma
from pydantic import BaseModel

logger = structlog.get_logger(__name__)
router = APIRouter(prefix="/doctors")


class DoctorProfileResponse(BaseModel):
    id: str
    user_id: str
    first_name: Optional[str] = None
    middle_name: Optional[str] = None
    last_name: Optional[str] = None
    title: Optional[str] = "Dr."
    specialization: str
    sub_specialization: Optional[str] = None
    medical_license_no: Optional[str] = None
    phone_primary: Optional[str] = None
    email_professional: Optional[str] = None
    qualifications: Optional[str] = None
    experience_years: Optional[int] = 0
    city: Optional[str] = None
    state: Optional[str] = None
    created_at: datetime
    
    class Config:
        from_attributes = True


@router.get("/{doctor_id}/profile", response_model=DoctorProfileResponse)
async def get_doctor_profile(doctor_id: str):
    """
    Get doctor profile information by doctor ID.
    """
    prisma = get_prisma()
    
    try:
        doctor = await prisma.doctor.find_unique(
            where={"id": doctor_id}
        )
        
        if not doctor:
            raise HTTPException(
                status_code=status.HTTP_404_NOT_FOUND,
                detail="Doctor not found"
            )
        
        return DoctorProfileResponse.model_validate(doctor)
        
    except HTTPException:
        raise
    except Exception as e:
        logger.error("Failed to fetch doctor profile", error=str(e))
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail="Failed to fetch doctor profile"
        )


class DoctorPatientResponse(BaseModel):
    """Doctor's patient with status and access level"""
    id: str
    patient_id: str
    patient_name: str
    status: str  # LOCKED, ACTIVE
    condition: str
    next_appointment: Optional[datetime] = None
    last_visit: Optional[datetime] = None
    emergency_flag: bool
    assigned_at: datetime
    access_granted: bool
    
    # Patient details (only if ACTIVE)
    patient_age: Optional[int] = None
    patient_gender: Optional[str] = None
    patient_blood_group: Optional[str] = None
    patient_phone: Optional[str] = None
    
    class Config:
        from_attributes = True


@router.get("/{doctor_id}/patients", response_model=List[DoctorPatientResponse])
async def get_doctor_patients(doctor_id: str, db: Prisma = Depends(get_prisma)):
    """
    Get all patients assigned to a doctor.
    - LOCKED patients: Doctor can see name and basic info but not detailed medical data
    - ACTIVE patients: Full access after consent approval
    """
    try:
        # Get all doctor-patient relationships
        relationships = await db.doctorpatient.find_many(
            where={"doctor_id": doctor_id},
            include={"patient": True},
            order={"assigned_at": "desc"}
        )
        
        result = []
        for rel in relationships:
            patient = rel.patient
            
            # Check for consent
            # In a real app, we would check if the consent is for this specific doctor/hospital
            consent = await db.consent.find_first(
                where={
                    "patient_id": rel.patient_id,
                    "status": "APPROVED"
                }
            )
            access_granted = consent is not None
            
            # Calculate age from date_of_birth
            age = None
            if patient and patient.date_of_birth:
                today = datetime.now()
                age = today.year - patient.date_of_birth.year - (
                    (today.month, today.day) < (patient.date_of_birth.month, patient.date_of_birth.day)
                )
            
            # Construct full name
            name_parts = []
            if patient:
                if patient.first_name:
                    name_parts.append(patient.first_name)
                if patient.middle_name:
                    name_parts.append(patient.middle_name)
                if patient.last_name:
                    name_parts.append(patient.last_name)
            full_name = " ".join(name_parts) if name_parts else "Unknown Patient"
            
            # Build response based on status
            patient_data = {
                "id": rel.id,
                "patient_id": rel.patient_id,
                "patient_name": full_name,
                "status": rel.status,
                "condition": rel.condition or "No condition specified",
                "next_appointment": rel.next_appointment,
                "last_visit": rel.last_visit,
                "emergency_flag": rel.emergency_flag,
                "assigned_at": rel.assigned_at,
                "access_granted": access_granted
            }
            
            # Only include detailed patient info if access is granted
            if access_granted and patient:
                patient_data.update({
                    "patient_age": age,
                    "patient_gender": patient.gender,
                    "patient_blood_group": patient.blood_group,
                    "patient_phone": patient.phone_primary
                })
            
            result.append(DoctorPatientResponse(**patient_data))
        
        return result
        
    except Exception as e:
        logger.error("Failed to fetch doctor patients", error=str(e))
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail=f"Failed to fetch doctor patients: {str(e)}"
        )


@router.delete("/{doctor_id}/patients/{patient_id}", status_code=status.HTTP_204_NO_CONTENT)
async def remove_patient(doctor_id: str, patient_id: str):
    """
    Remove a patient from doctor's patient list.
    This revokes the consent and updates the relationship status to LOCKED.
    """
    prisma = get_prisma()
    
    try:
        # Find the doctor-patient relationship
        relationship = await prisma.doctorpatient.find_first(
            where={
                "doctor_id": doctor_id,
                "patient_id": patient_id
            }
        )
        
        if not relationship:
            raise HTTPException(
                status_code=status.HTTP_404_NOT_FOUND,
                detail="Patient relationship not found"
            )
        
        # Find and revoke the consent
        consent = await prisma.consent.find_first(
            where={
                "patient_id": patient_id,
                "status": "APPROVED"
            }
        )
        
        if consent:
            await prisma.consent.update(
                where={"id": consent.id},
                data={
                    "status": "REVOKED",
                    "responded_at": datetime.now()
                }
            )
        
        # Update relationship to LOCKED
        await prisma.doctorpatient.update(
            where={"id": relationship.id},
            data={
                "status": "LOCKED",
                "condition": "Access removed by doctor"
            }
        )
        
        logger.info(
            "Doctor removed patient",
            doctor_id=doctor_id,
            patient_id=patient_id
        )
        
    except HTTPException:
        raise
    except Exception as e:
        logger.error("Failed to remove patient", error=str(e))
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail="Failed to remove patient"
        )
