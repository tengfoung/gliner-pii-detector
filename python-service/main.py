from fastapi import FastAPI, HTTPException
from pydantic import BaseModel
from typing import List, Optional
from gliner import GLiNER
import logging
import time
import os

logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

app = FastAPI(title="GLiNER PII Detection Service", version="1.0.0")

model = None

class DetectionRequest(BaseModel):
    text: str
    entity_types: List[str]
    threshold: Optional[float] = 0.5

class DetectedEntity(BaseModel):
    text: str
    type: str
    start_index: int
    end_index: int
    confidence: float

class DetectionResponse(BaseModel):
    original_text: str
    entities: List[DetectedEntity]
    entity_count: int
    processing_time_ms: int

@app.on_event("startup")
async def load_model():
    global model
    local_model_path = "./models/gliner_multi_pii-v1"
    
    # Try to load from local path first, fallback to HuggingFace if not found
    if os.path.exists(local_model_path):
        logger.info(f"Loading GLiNER model from local path: {local_model_path}")
        try:
            model = GLiNER.from_pretrained(local_model_path)
            logger.info("GLiNER model loaded successfully from local storage")
        except Exception as e:
            logger.error(f"Failed to load local model: {e}")
            raise
    else:
        logger.warning(f"Local model not found at {local_model_path}")
        logger.info("Downloading model from HuggingFace (this will take a few minutes)...")
        logger.info("Run 'python download_model.py' to download the model for offline use")
        try:
            model = GLiNER.from_pretrained("urchade/gliner_multi_pii-v1")
            logger.info("GLiNER model loaded successfully from HuggingFace")
        except Exception as e:
            logger.error(f"Failed to load model: {e}")
            raise

@app.get("/health")
async def health_check():
    return {
        "status": "UP",
        "message": "GLiNER PII Detection Service is running",
        "model_loaded": model is not None
    }

@app.post("/detect", response_model=DetectionResponse)
async def detect_pii(request: DetectionRequest):
    if model is None:
        raise HTTPException(status_code=503, detail="Model not loaded")
    
    start_time = time.time()
    
    try:
        entities = model.predict_entities(
            request.text,
            request.entity_types,
            threshold=request.threshold
        )
        
        detected_entities = []
        for entity in entities:
            detected_entities.append(DetectedEntity(
                text=entity["text"],
                type=entity["label"],
                start_index=entity["start"],
                end_index=entity["end"],
                confidence=entity["score"]
            ))
        
        processing_time = int((time.time() - start_time) * 1000)
        
        return DetectionResponse(
            original_text=request.text,
            entities=detected_entities,
            entity_count=len(detected_entities),
            processing_time_ms=processing_time
        )
    
    except Exception as e:
        logger.error(f"Error during prediction: {e}")
        raise HTTPException(status_code=500, detail=str(e))

if __name__ == "__main__":
    import uvicorn
    uvicorn.run(app, host="0.0.0.0", port=5001)
