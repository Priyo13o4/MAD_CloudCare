"""
Patient Router - Patient profile and health data endpoints
"""

from fastapi import APIRouter, HTTPException, Depends
from prisma import Prisma
from app.core.database import get_prisma
from pydantic import BaseModel
from typing import List, Optional
from datetime import datetime
from app.services.aadhar_uid import AadharUIDService

router = APIRouter(prefix="/patients")


class RecordLookupRequest(BaseModel):
    hospital_id: str
    aadhar_number: str

class MedicalRecordSummary(BaseModel):
    id: str
    title: str
    date: datetime
    type: str
    description: str
    doctor_name: Optional[str] = None

@router.post("/lookup-records", response_model=List[MedicalRecordSummary])
async def lookup_patient_records(
    request: RecordLookupRequest,
    db: Prisma = Depends(get_prisma)
):
    """
    Look up old records by Hospital and Aadhar number.
    """
    try:
        # Generate UID from Aadhar
        try:
            uid = AadharUIDService.generate_uid(request.aadhar_number)
        except ValueError:
            raise HTTPException(status_code=400, detail="Invalid Aadhar number")

        # Find patient by UID
        patient = await db.patient.find_unique(where={"aadhar_uid": uid})
        if not patient:
            return [] # No patient found with this Aadhar

        # Find records for this patient in the specified hospital
        # We need to join MedicalRecord -> Facility -> Hospital
        # Prisma Client Python might not support deep nested filtering easily in find_many
        # So we fetch records for the patient and filter in python or use raw query if needed.
        # Let's try to use include and filter.
        
        records = await db.medicalrecord.find_many(
            where={
                "patient_id": patient.id,
                "facility": {
                    "is": {
                        "hospital_id": request.hospital_id
                    }
                }
            },
            include={
                "facility": True
            }
        )
        
        # Also check PatientRecord (created by doctors)
        # PatientRecord -> Doctor -> Hospital
        doctor_records = await db.patientrecord.find_many(
            where={
                "patient_id": patient.id,
                "doctor": {
                    "is": {
                        "hospital_id": request.hospital_id
                    }
                }
            },
            include={
                "doctor": True
            }
        )
        
        results = []
        for r in records:
            results.append(MedicalRecordSummary(
                id=r.id,
                title=r.title,
                date=r.date,
                type=r.record_type,
                description=r.description,
                doctor_name=None # MedicalRecord doesn't have doctor link directly usually
            ))
            
        for r in doctor_records:
            results.append(MedicalRecordSummary(
                id=r.id,
                title=r.title,
                date=r.date,
                type=r.record_type,
                description=r.description,
                doctor_name=f"Dr. {r.doctor.last_name}"
            ))
            
        return results

    except Exception as e:
        raise HTTPException(status_code=500, detail=f"Failed to lookup records: {str(e)}")

@router.get("/{patient_id}/profile")
async def get_patient_profile(patient_id: str, db: Prisma = Depends(get_prisma)):
    """
    Get patient profile by patient ID
    
    Returns complete patient information including:
    - Personal details (name, age, gender, blood type)
    - Contact information (email, phone, address)
    - Emergency contact details
    - Insurance information (if available)
    - Aadhar UID linking
    """
    try:
        # Fetch patient from database
        patient = await db.patient.find_unique(
            where={"id": patient_id}
        )
        
        if not patient:
            raise HTTPException(status_code=404, detail="Patient not found")
        
        # Calculate age from date_of_birth
        from datetime import datetime
        age = 0
        if patient.date_of_birth:
            today = datetime.now()
            age = today.year - patient.date_of_birth.year - (
                (today.month, today.day) < (patient.date_of_birth.month, patient.date_of_birth.day)
            )
        
        # Construct full name
        name_parts = [patient.first_name]
        if patient.middle_name:
            name_parts.append(patient.middle_name)
        name_parts.append(patient.last_name)
        full_name = " ".join(name_parts)
        
        # Construct full address
        address_parts = []
        if patient.address_line1:
            address_parts.append(patient.address_line1)
        if patient.address_line2:
            address_parts.append(patient.address_line2)
        if patient.city:
            address_parts.append(patient.city)
        if patient.state:
            address_parts.append(patient.state)
        if patient.postal_code:
            address_parts.append(patient.postal_code)
        full_address = ", ".join(address_parts) if address_parts else "Not provided"
        
        # Format response matching Android app model
        return {
            "success": True,
            "patient": {
                "id": patient.id,
                "user_id": patient.user_id,
                "aadhar_uid": patient.aadhar_uid,
                "name": full_name,
                "age": age,
                "gender": patient.gender,
                "blood_type": patient.blood_group or "Unknown",
                "contact": patient.phone_primary,
                "email": patient.email,
                "address": full_address,
                "family_contact": f"{patient.emergency_contact_name} ({patient.emergency_contact_relation}) - {patient.emergency_contact_phone}",
                "insurance_provider": patient.insurance_provider,
                "insurance_id": patient.insurance_policy_no,
                "emergency": False,  # Set based on emergency_alerts if needed
                "occupation": patient.occupation,
                "ai_analysis": patient.ai_analysis
            },
            "message": None
        }
        
    except HTTPException:
        raise
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"Failed to fetch patient profile: {str(e)}")


@router.post("/{patient_id}/profile")
async def update_patient_profile(
    patient_id: str,
    update_data: dict,
    db: Prisma = Depends(get_prisma)
):
    """
    Update patient profile
    
    Accepts partial updates for any patient fields
    """
    try:
        # Check if patient exists
        existing = await db.patient.find_unique(where={"id": patient_id})
        if not existing:
            raise HTTPException(status_code=404, detail="Patient not found")
        
        # Filter out None values and fields that shouldn't be updated
        update_fields = {k: v for k, v in update_data.items() if v is not None and k not in ["id", "userId"]}
        
        if not update_fields:
            raise HTTPException(status_code=400, detail="No valid fields to update")
        
        # Update patient
        updated_patient = await db.patient.update(
            where={"id": patient_id},
            data=update_fields
        )
        
        return {
            "success": True,
            "patient": {
                "id": updated_patient.id,
                "user_id": updated_patient.userId,
                "aadhar_uid": updated_patient.aadharUid,
                "name": updated_patient.name,
                "age": updated_patient.age,
                "gender": updated_patient.gender,
                "blood_type": updated_patient.bloodType,
                "contact": updated_patient.contact,
                "email": updated_patient.email,
                "address": updated_patient.address,
                "family_contact": updated_patient.familyContact,
                "insurance_provider": updated_patient.insuranceProvider,
                "insurance_id": updated_patient.insuranceId,
                "emergency": updated_patient.emergency,
                "occupation": updated_patient.occupation,
                "ai_analysis": updated_patient.aiAnalysis
            },
            "message": "Profile updated successfully"
        }
        
    except HTTPException:
        raise
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"Failed to update patient profile: {str(e)}")
