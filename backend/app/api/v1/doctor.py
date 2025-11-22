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

class HospitalAssociation(BaseModel):
    id: str
    hospital_id: str
    hospital_name: str
    hospital_code: str
    is_primary: bool
    joined_at: datetime

class UpdateDoctorHospitalsRequest(BaseModel):
    hospital_ids: List[str]
    primary_hospital_id: Optional[str] = None


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

@router.get("/{doctor_id}/hospitals", response_model=List[HospitalAssociation])
async def get_doctor_hospitals(doctor_id: str):
    """
    Get all hospitals associated with a doctor.
    """
    prisma = get_prisma()
    try:
        associations = await prisma.doctorhospital.find_many(
            where={"doctor_id": doctor_id},
            include={"hospital": True}
        )
        
        result = []
        for assoc in associations:
            result.append(HospitalAssociation(
                id=assoc.id,
                hospital_id=assoc.hospital_id,
                hospital_name=assoc.hospital.name,
                hospital_code=assoc.hospital.hospital_code,
                is_primary=assoc.is_primary,
                joined_at=assoc.joined_at
            ))
        
        return result
    except Exception as e:
        logger.error("Failed to fetch doctor hospitals", error=str(e))
        raise HTTPException(status_code=500, detail=str(e))

@router.post("/{doctor_id}/hospitals")
async def update_doctor_hospitals(doctor_id: str, request: UpdateDoctorHospitalsRequest):
    """
    Update doctor's hospital associations.
    Creates new associations and removes old ones.
    """
    prisma = get_prisma()
    try:
        # Verify doctor exists
        doctor = await prisma.doctor.find_unique(where={"id": doctor_id})
        if not doctor:
            raise HTTPException(status_code=404, detail="Doctor not found")
        
        # Get current associations
        current_assocs = await prisma.doctorhospital.find_many(
            where={"doctor_id": doctor_id}
        )
        current_hospital_ids = {a.hospital_id for a in current_assocs}
        new_hospital_ids = set(request.hospital_ids)
        
        # Remove associations that are no longer needed
        to_remove = current_hospital_ids - new_hospital_ids
        if to_remove:
            await prisma.doctorhospital.delete_many(
                where={
                    "doctor_id": doctor_id,
                    "hospital_id": {"in": list(to_remove)}
                }
            )
        
        # Add new associations
        to_add = new_hospital_ids - current_hospital_ids
        for hospital_id in to_add:
            # Verify hospital exists
            hospital = await prisma.hospital.find_unique(where={"id": hospital_id})
            if not hospital:
                logger.warning(f"Hospital {hospital_id} not found, skipping")
                continue
            
            is_primary = (hospital_id == request.primary_hospital_id)
            await prisma.doctorhospital.create(
                data={
                    "doctor_id": doctor_id,
                    "hospital_id": hospital_id,
                    "is_primary": is_primary
                }
            )
        
        # Update primary hospital if specified
        if request.primary_hospital_id:
            # Clear all primary flags
            await prisma.doctorhospital.update_many(
                where={"doctor_id": doctor_id},
                data={"is_primary": False}
            )
            # Set new primary
            await prisma.doctorhospital.update_many(
                where={
                    "doctor_id": doctor_id,
                    "hospital_id": request.primary_hospital_id
                },
                data={"is_primary": True}
            )
            # Update doctor's hospital_id
            await prisma.doctor.update(
                where={"id": doctor_id},
                data={"hospital_id": request.primary_hospital_id}
            )
        
        return {"success": True, "message": "Hospital associations updated"}
    
    except HTTPException:
        raise
    except Exception as e:
        logger.error("Failed to update doctor hospitals", error=str(e))
        raise HTTPException(status_code=500, detail=str(e))


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
            
            # Access is granted if the DoctorPatient relationship status is ACTIVE
            # (set by consent approval process)
            access_granted = rel.status == "ACTIVE"
            
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
