# GLiNER Python FastAPI Service

This service provides PII detection using the GLiNER model from HuggingFace.

## Model

- **Model**: `urchade/gliner_multi_pii-v1`
- **Source**: https://huggingface.co/urchade/gliner_multi_pii-v1
- **Local Storage**: `./models/gliner_multi_pii-v1/`

## Setup

### Option A: Fully Offline Setup (Recommended for Production)

For complete offline capability (no internet required after initial setup):

```bash
# 1. Download all Python packages (~2GB, one-time)
./setup_offline.sh

# 2. Download GLiNER model (~500MB, one-time)
source venv/bin/activate
python download_model.py
```

After this, the service runs **100% offline** with no internet dependency.

### Option B: Online Setup (Development)

```bash
# Activate virtual environment
source venv/bin/activate

# Install dependencies from PyPI
pip install -r requirements.txt

# Download model (required only once, ~500MB)
python download_model.py
```

## Running the Service

### Local Development

1. Create a virtual environment:
```bash
python3 -m venv venv
source venv/bin/activate
```

2. Install dependencies:
```bash
pip install -r requirements.txt
```

3. Run the service:
```bash
python main.py
```

Or use the startup script:
```bash
./start.sh
```

The service will start on `http://localhost:5001`

### Docker

Build and run with Docker:
```bash
docker build -t gliner-service .
docker run -p 5000:5000 gliner-service
```

## API Endpoints

### Health Check
```bash
GET /health
```

### Detect PII
```bash
POST /detect
Content-Type: application/json

{
  "text": "My email is john@example.com",
  "entity_types": ["EMAIL", "PERSON", "CREDIT_CARD"],
  "threshold": 0.5
}
```

Response:
```json
{
  "original_text": "My email is john@example.com",
  "entities": [
    {
      "text": "john@example.com",
      "type": "EMAIL",
      "start_index": 12,
      "end_index": 28,
      "confidence": 0.95
    }
  ],
  "entity_count": 1,
  "processing_time_ms": 45
}
```

## Model Storage

The GLiNER model is stored locally in `./models/gliner_multi_pii-v1/` for offline operation. 

**Offline Mode**: Once downloaded, the service runs completely offline without any HuggingFace dependencies.

**Fallback**: If the local model is not found, the service will attempt to download it from HuggingFace automatically (requires internet connection).

## Performance

- First request may take longer due to model loading
- Subsequent requests are fast (~50-100ms depending on text length)
- Model size: ~500MB
