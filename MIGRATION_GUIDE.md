# Migration Guide: ONNX to Python FastAPI

This document describes the architectural changes made to migrate from Java ONNX inference to Python FastAPI with the native GLiNER model.

## What Changed

### Architecture
- **Before**: Monolithic Java Spring Boot application with ONNX Runtime
- **After**: Microservices architecture with Python FastAPI (ML) + Java Spring Boot (API/Business Logic)

### Removed Components
1. **Java Services**:
   - `OnnxModelService.java` - ONNX model loading and inference
   - `TokenizerService.java` - Text tokenization for ONNX
   - `TokenizerData.java` - Tokenizer data model

2. **Dependencies**:
   - ONNX Runtime Java library
   - Model files: `model.onnx`, `tokenizer.json`

3. **Configuration**:
   - `gliner.model.path`
   - `gliner.model.tokenizer-path`
   - `gliner.model.max-length`

### Added Components
1. **Python FastAPI Service** (`python-service/`):
   - `main.py` - FastAPI application with GLiNER model
   - `requirements.txt` - Python dependencies
   - `Dockerfile` - Container configuration
   - `start.sh` - Local development startup script

2. **Configuration**:
   - `gliner.python.service.url` - Python service endpoint

3. **Deployment**:
   - `docker-compose.yml` - Multi-container orchestration

## Benefits

1. **Native Model Support**: Uses the original PyTorch GLiNER model from HuggingFace
2. **Better Accuracy**: No conversion artifacts from ONNX export
3. **Easier Updates**: Direct access to HuggingFace model hub
4. **Scalability**: Independent scaling of ML and API services
5. **Flexibility**: Easy to swap or upgrade ML models

## Running the Application

### Option 1: Docker Compose (Recommended)
```bash
docker-compose up
```

### Option 2: Local Development
Terminal 1 (Python service):
```bash
cd python-service
./start.sh
```

Terminal 2 (Java service):
```bash
mvn spring-boot:run
```

## API Compatibility

All existing API endpoints remain unchanged:
- `POST /api/v1/pii/detect`
- `POST /api/v1/pii/detect/batch`
- `POST /api/v1/redaction/redact`
- `POST /api/v1/redaction/remask`
- `GET /api/v1/entity-types`

## Configuration

Update `application.yml` to point to your Python service:
```yaml
gliner:
  python:
    service:
      url: http://localhost:5001  # or http://python-service:5000 in Docker
```

## Troubleshooting

### Python Service Not Starting
- Check Python 3.10+ is installed
- Ensure sufficient disk space for model download (~500MB)
- Check HuggingFace connectivity

### Java Service Cannot Connect
- Verify Python service is running: `curl http://localhost:5001/health`
- Check `gliner.python.service.url` configuration
- Ensure no firewall blocking port 5000

### Model Download Issues
- Model is cached in `~/.cache/huggingface/`
- Delete cache and restart to re-download
- Check internet connectivity to huggingface.co
