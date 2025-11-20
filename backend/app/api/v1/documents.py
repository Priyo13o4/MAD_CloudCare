"""
Documents Router - Medical Records Upload and Management
"""

from fastapi import APIRouter, HTTPException, status
from typing import List, Optional
from datetime import datetime
import structlog

from app.core.database import get_prisma
from pydantic import BaseModel

logger = structlog.get_logger(__name__)
router = APIRouter(prefix="/documents")


class MedicalRecordResponse(BaseModel):
    id: str
    patient_id: str
    facility_id: Optional[str]
    title: str
    description: str
    date: datetime
    record_type: str
    file_url: Optional[str]
    created_at: datetime
    
    class Config:
        from_attributes = True


class CreateMedicalRecordRequest(BaseModel):
    patient_id: str
    title: str
    description: str
    record_type: str
    facility_id: Optional[str] = None
    file_data: Optional[str] = None  # Base64 encoded file


@router.post("/upload", response_model=MedicalRecordResponse, status_code=status.HTTP_201_CREATED)
async def upload_document(record: CreateMedicalRecordRequest):
    """
    Upload a new medical record/document.
    File is stored as base64 in file_url field.
    """
    prisma = get_prisma()
    
    try:
        # Verify patient exists
        patient = await prisma.patient.find_unique(
            where={"id": record.patient_id}
        )
        
        if not patient:
            raise HTTPException(
                status_code=status.HTTP_404_NOT_FOUND,
                detail="Patient not found"
            )
        
        # Create medical record
        new_record = await prisma.medicalrecord.create(
            data={
                "patient_id": record.patient_id,
                "facility_id": record.facility_id,
                "title": record.title,
                "description": record.description,
                "date": datetime.now(),
                "record_type": record.record_type,
                "file_url": record.file_data,  # Store base64 data
            }
        )
        
        logger.info(
            "Created medical record",
            record_id=new_record.id,
            patient_id=record.patient_id,
            record_type=record.record_type
        )
        
        return MedicalRecordResponse.model_validate(new_record)
        
    except HTTPException:
        raise
    except Exception as e:
        logger.error("Failed to create medical record", error=str(e))
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail=f"Failed to create medical record: {str(e)}"
        )


@router.get("/{patient_id}", response_model=List[MedicalRecordResponse])
async def get_patient_documents(patient_id: str):
    """
    Get all medical records for a patient.
    """
    prisma = get_prisma()
    
    try:
        records = await prisma.medicalrecord.find_many(
            where={"patient_id": patient_id},
            order={"created_at": "desc"}
        )
        
        return [MedicalRecordResponse.model_validate(r) for r in records]
        
    except Exception as e:
        logger.error("Failed to fetch medical records", error=str(e))
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail="Failed to fetch medical records"
        )


@router.delete("/{record_id}", status_code=status.HTTP_204_NO_CONTENT)
async def delete_document(record_id: str):
    """
    Delete a medical record.
    """
    prisma = get_prisma()
    
    try:
        await prisma.medicalrecord.delete(
            where={"id": record_id}
        )
        
        logger.info("Deleted medical record", record_id=record_id)
        
    except Exception as e:
        logger.error("Failed to delete medical record", error=str(e))
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail="Failed to delete medical record"
        )

