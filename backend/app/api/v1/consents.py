"""
Consents Router - Patient consent and data access management
"""

from fastapi import APIRouter, HTTPException, status
from typing import List, Optional
from datetime import datetime, timedelta
import structlog

from app.core.database import get_prisma
from pydantic import BaseModel

logger = structlog.get_logger(__name__)
router = APIRouter(prefix="/consents")


class ConsentResponse(BaseModel):
    id: str
    patient_id: str
    facility_name: str
    request_type: str
    description: Optional[str] = None
    status: str
    requested_at: datetime
    responded_at: Optional[datetime] = None
    expires_at: Optional[datetime] = None
    
    class Config:
        from_attributes = True


class CreateConsentRequest(BaseModel):
    patient_id: str
    doctor_id: str
    facility_name: str  # Doctor's name or hospital name
    request_type: str = "DATA_ACCESS"
    description: Optional[str] = "Request to access your medical records"
    expires_in_days: int = 90  # Default 90 days validity


class UpdateConsentRequest(BaseModel):
    status: str  # APPROVED or DENIED


@router.post("/request", response_model=ConsentResponse, status_code=status.HTTP_201_CREATED)
async def create_consent_request(request: CreateConsentRequest):
    """
    Create a new consent request when doctor scans patient's QR code.
    Also adds patient to doctor's patient list with LOCKED status.
    """
    prisma = get_prisma()
    
    try:
        # Verify patient exists
        patient = await prisma.patient.find_unique(
            where={"id": request.patient_id}
        )
        
        if not patient:
            raise HTTPException(
                status_code=status.HTTP_404_NOT_FOUND,
                detail="Patient not found"
            )
        
        # Verify doctor exists
        doctor = await prisma.doctor.find_unique(
            where={"id": request.doctor_id}
        )
        
        if not doctor:
            raise HTTPException(
                status_code=status.HTTP_404_NOT_FOUND,
                detail="Doctor not found"
            )
            
        # Construct doctor's full name
        doctor_name = f"{doctor.title} {doctor.first_name} {doctor.last_name}"
        
        # Use doctor's name as facility_name if generic "Healthcare Professional" is provided
        # or if facility_name is empty
        final_facility_name = request.facility_name
        if not final_facility_name or final_facility_name == "Healthcare Professional":
            final_facility_name = doctor_name
        
        # Check if consent request already exists
        existing_consent = await prisma.consent.find_first(
            where={
                "patient_id": request.patient_id,
                "facility_name": final_facility_name,
                "status": "PENDING"
            }
        )
        
        if existing_consent:
            # ✅ FIX: Return existing consent instead of 409 error
            # This allows the app to show a toast instead of crashing
            logger.info(
                "Consent request already pending",
                patient_id=request.patient_id,
                facility_name=final_facility_name
            )
            return ConsentResponse.model_validate(existing_consent)
        
        # Create consent request
        expires_at = datetime.now() + timedelta(days=request.expires_in_days)
        
        consent = await prisma.consent.create(
            data={
                "patient_id": request.patient_id,
                "facility_name": final_facility_name,
                "request_type": request.request_type,
                "description": request.description,
                "status": "PENDING",
                "expires_at": expires_at
            }
        )
        
        # Add patient to doctor's patient list with LOCKED status
        # Check if relationship already exists
        existing_relationship = await prisma.doctorpatient.find_first(
            where={
                "doctor_id": request.doctor_id,
                "patient_id": request.patient_id
            }
        )
        
        if not existing_relationship:
            await prisma.doctorpatient.create(
                data={
                    "doctor_id": request.doctor_id,
                    "patient_id": request.patient_id,
                    "status": "LOCKED",  # Data is locked until consent approved
                    "condition": "Awaiting consent approval"
                }
            )
        
        logger.info(
            "Created consent request",
            consent_id=consent.id,
            patient_id=request.patient_id,
            doctor_id=request.doctor_id
        )
        
        return ConsentResponse.model_validate(consent)
        
    except HTTPException:
        raise
    except Exception as e:
        logger.error("Failed to create consent request", error=str(e))
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail=f"Failed to create consent request: {str(e)}"
        )


@router.get("/patient/{patient_id}", response_model=List[ConsentResponse])
async def get_patient_consents(patient_id: str, status_filter: Optional[str] = None):
    """
    Get all consent requests for a patient.
    Optional status_filter: PENDING, APPROVED, DENIED
    """
    prisma = get_prisma()
    
    try:
        where_clause = {"patient_id": patient_id}
        
        if status_filter:
            where_clause["status"] = status_filter
        
        consents = await prisma.consent.find_many(
            where=where_clause,
            order={"requested_at": "desc"}
        )
        
        return [ConsentResponse.model_validate(c) for c in consents]
        
    except Exception as e:
        logger.error("Failed to fetch patient consents", error=str(e))
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail="Failed to fetch consent requests"
        )


@router.patch("/{consent_id}", response_model=ConsentResponse)
async def update_consent_status(consent_id: str, update: UpdateConsentRequest):
    """
    Update consent request status (approve or deny).
    If approved, unlocks patient data for the doctor.
    """
    prisma = get_prisma()
    
    try:
        # Get consent request
        consent = await prisma.consent.find_unique(
            where={"id": consent_id}
        )
        
        if not consent:
            raise HTTPException(
                status_code=status.HTTP_404_NOT_FOUND,
                detail="Consent request not found"
            )
        
        if consent.status != "PENDING":
            # Allow revoking approved consents
            if not (consent.status == "APPROVED" and update.status == "REVOKED"):
                raise HTTPException(
                    status_code=status.HTTP_400_BAD_REQUEST,
                    detail="Consent request already processed"
                )
        
        # Validate status
        if update.status not in ["APPROVED", "DENIED", "REVOKED"]:
            raise HTTPException(
                status_code=status.HTTP_400_BAD_REQUEST,
                detail="Status must be APPROVED, DENIED, or REVOKED"
            )
        
        # Update consent status
        updated_consent = await prisma.consent.update(
            where={"id": consent_id},
            data={
                "status": update.status,
                "responded_at": datetime.now()
            }
        )
        
        # If approved, unlock patient data in doctor_patients relationship
        if update.status == "APPROVED":
            # Handle HOSPITAL_ADMISSION
            if consent.request_type == "HOSPITAL_ADMISSION":
                # Find hospital by name (facility_name)
                # This is a bit loose, but facility_name is what we have in Consent.
                # Ideally we should store facility_id in Consent, but schema doesn't have it.
                # We can try to find the hospital.
                hospital = await prisma.hospital.find_first(where={"name": consent.facility_name})
                
                if hospital:
                    # Create an Appointment to represent Admission
                    # Check if one already exists to avoid duplicates
                    existing_appt = await prisma.appointment.find_first(
                        where={
                            "patient_id": consent.patient_id,
                            "hospital_id": hospital.id,
                            "status": "IN_PROGRESS" # Active admission
                        }
                    )
                    
                    if not existing_appt:
                        # We need a doctor ID for appointment. 
                        # Let's pick the first available doctor or a default one, or make doctor_id optional in Appointment?
                        # Schema says doctor_id is required.
                        # Let's find a doctor in this hospital.
                        doctor = await prisma.doctor.find_first(where={"hospital_id": hospital.id})
                        
                        if doctor:
                            await prisma.appointment.create(
                                data={
                                    "patient_id": consent.patient_id,
                                    "doctor_id": doctor.id,
                                    "hospital_id": hospital.id,
                                    "date": datetime.now(),
                                    "time": datetime.now().strftime("%H:%M"),
                                    "type": "ADMISSION",
                                    "department": "General", # Default
                                    "reason": consent.description or "Hospital Admission",
                                    "status": "IN_PROGRESS" # Represents Admitted
                                }
                            )
                            logger.info("Created admission appointment", patient_id=consent.patient_id, hospital_id=hospital.id)
                        else:
                            logger.warning("No doctor found for hospital admission", hospital_id=hospital.id)
            
            # Find the doctor_patient relationship by facility_name (which contains doctor info)
            # We need to update the status from LOCKED to ACTIVE
            doctor_patients = await prisma.doctorpatient.find_many(
                where={
                    "patient_id": consent.patient_id,
                    "status": "LOCKED"
                }
            )
            
            # Update all locked relationships for this patient (in case of multiple pending)
            for dp in doctor_patients:
                await prisma.doctorpatient.update(
                    where={"id": dp.id},
                    data={
                        "status": "ACTIVE",
                        "condition": "Access granted"
                    }
                )
        
        # If revoked, lock patient data back
        elif update.status == "REVOKED":
            # Find the doctor_patient relationship and lock it
            doctor_patients = await prisma.doctorpatient.find_many(
                where={
                    "patient_id": consent.patient_id,
                    "status": "ACTIVE"
                }
            )
            
            for dp in doctor_patients:
                await prisma.doctorpatient.update(
                    where={"id": dp.id},
                    data={
                        "status": "LOCKED",
                        "condition": "Access revoked"
                    }
                )
        
        logger.info(
            "Updated consent status",
            consent_id=consent_id,
            new_status=update.status,
            patient_id=consent.patient_id
        )
        
        return ConsentResponse.model_validate(updated_consent)
        
    except HTTPException:
        raise
    except Exception as e:
        logger.error("Failed to update consent status", error=str(e))
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail="Failed to update consent status"
        )


@router.delete("/{consent_id}", status_code=status.HTTP_204_NO_CONTENT)
async def delete_consent(consent_id: str):
    """
    Delete a consent request (only for DENIED or expired requests).
    """
    prisma = get_prisma()
    
    try:
        consent = await prisma.consent.find_unique(
            where={"id": consent_id}
        )
        
        if not consent:
            raise HTTPException(
                status_code=status.HTTP_404_NOT_FOUND,
                detail="Consent request not found"
            )
        
        if consent.status == "APPROVED":
            raise HTTPException(
                status_code=status.HTTP_400_BAD_REQUEST,
                detail="Cannot delete approved consent"
            )
        
        await prisma.consent.delete(
            where={"id": consent_id}
        )
        
        logger.info("Deleted consent request", consent_id=consent_id)
        
    except HTTPException:
        raise
    except Exception as e:
        logger.error("Failed to delete consent", error=str(e))
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail="Failed to delete consent request"
        )


@router.delete("/cleanup/all", status_code=status.HTTP_204_NO_CONTENT)
async def cleanup_all_consents():
    """
    Delete all consent requests (for testing purposes).
    """
    prisma = get_prisma()
    
    try:
        # Delete all consents
        await prisma.consent.delete_many()
        
        # Reset all doctor-patient relationships to LOCKED
        await prisma.doctorpatient.update_many(
            where={},
            data={
                "status": "LOCKED",
                "condition": "Awaiting consent approval"
            }
        )
        
        logger.info("Cleaned up all consents and reset doctor-patient relationships")
        
    except Exception as e:
        logger.error("Failed to cleanup consents", error=str(e))
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail="Failed to cleanup consents"
        )

