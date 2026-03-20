# GLiNER PII Multi-Detector

A Spring Boot application for detecting Personally Identifiable Information (PII) in text, with a focus on financial and trading data, using the GLiNER ONNX model.

## Features

- **PII Detection**: Detect various types of PII including financial, trading, and personal information
- **PII Redaction**: Mask detected PII with multiple strategies (FULL, PARTIAL, HASH, TOKEN)
- **PII Remasking**: Restore original values from redacted text using secure tokens
- **RESTful API**: JSON-based API endpoints for single and batch text processing
- **ONNX Runtime**: Efficient inference using ONNX Runtime with the GLiNER model
- **Pattern Matching**: Hybrid approach combining ML model with regex patterns for high accuracy
- **Configurable**: Customizable entity types and detection thresholds

## Supported Entity Types

### Financial
- CREDIT_CARD
- BANK_ACCOUNT
- ROUTING_NUMBER
- SWIFT_CODE
- IBAN
- TAX_ID
- SSN
- ACCOUNT_NUMBER

### Trading
- TICKER_SYMBOL
- CUSIP
- ISIN
- PORTFOLIO_ID
- TRADE_ID
- BROKER_ID

### Personal
- EMAIL
- PHONE_NUMBER
- NAME
- ADDRESS
- DATE_OF_BIRTH
- PASSPORT_NUMBER
- DRIVER_LICENSE

## Prerequisites

- Java 17 or higher
- Maven 3.6+
- GLiNER ONNX model files (already included):
  - `model.onnx`
  - `tokenizer.json`

## Installation

1. Clone the repository:
```bash
git clone <repository-url>
cd gliner-spring-boot-java
```

2. Build the project:
```bash
mvn clean install
```

3. Run the application:
```bash
mvn spring-boot:run
```

The application will start on `http://localhost:8080`

## API Endpoints

### 1. Detect PII in Single Text

**Endpoint**: `POST /api/v1/pii/detect`

**Request Body**:
```json
{
  "text": "My credit card is 4532-1234-5678-9010 and email is john.doe@example.com",
  "entityTypes": ["CREDIT_CARD", "EMAIL"],
  "threshold": 0.5
}
```

**Response**:
```json
{
  "originalText": "My credit card is 4532-1234-5678-9010 and email is john.doe@example.com",
  "entities": [
    {
      "text": "4532-1234-5678-9010",
      "type": "CREDIT_CARD",
      "startIndex": 18,
      "endIndex": 37,
      "confidence": 0.95
    },
    {
      "text": "john.doe@example.com",
      "type": "EMAIL",
      "startIndex": 51,
      "endIndex": 71,
      "confidence": 0.95
    }
  ],
  "entityCount": 2,
  "processingTimeMs": 45
}
```

### 2. Batch PII Detection

**Endpoint**: `POST /api/v1/pii/detect/batch`

**Request Body**:
```json
{
  "texts": [
    "Contact me at alice@company.com",
    "Account number: 123456789"
  ],
  "entityTypes": ["EMAIL", "BANK_ACCOUNT"],
  "threshold": 0.5
}
```

**Response**:
```json
{
  "results": [
    {
      "originalText": "Contact me at alice@company.com",
      "entities": [
        {
          "text": "alice@company.com",
          "type": "EMAIL",
          "startIndex": 14,
          "endIndex": 31,
          "confidence": 0.95
        }
      ],
      "entityCount": 1,
      "processingTimeMs": 23
    },
    {
      "originalText": "Account number: 123456789",
      "entities": [
        {
          "text": "123456789",
          "type": "BANK_ACCOUNT",
          "startIndex": 16,
          "endIndex": 25,
          "confidence": 0.95
        }
      ],
      "entityCount": 1,
      "processingTimeMs": 18
    }
  ],
  "totalTexts": 2,
  "totalEntitiesFound": 2,
  "totalProcessingTimeMs": 41
}
```

### 3. Get All Entity Types

**Endpoint**: `GET /api/v1/entity-types`

**Response**:
```json
{
  "categorized": {
    "financial": ["CREDIT_CARD", "BANK_ACCOUNT", "..."],
    "trading": ["TICKER_SYMBOL", "CUSIP", "..."],
    "personal": ["EMAIL", "PHONE_NUMBER", "..."]
  },
  "all": ["CREDIT_CARD", "BANK_ACCOUNT", "EMAIL", "..."],
  "count": 21
}
```

### 4. Get Financial Entity Types

**Endpoint**: `GET /api/v1/entity-types/financial`

### 5. Get Trading Entity Types

**Endpoint**: `GET /api/v1/entity-types/trading`

### 6. Get Personal Entity Types

**Endpoint**: `GET /api/v1/entity-types/personal`

### 7. Health Check

**Endpoint**: `GET /api/v1/pii/health`

**Response**:
```json
{
  "status": "UP",
  "message": "PII Detection Service is running"
}
```

### 8. Redact PII

**Endpoint**: `POST /api/v1/redaction/redact`

**Request Body**:
```json
{
  "text": "My credit card is 4532-1234-5678-9010 and SSN is 123-45-6789",
  "entityTypes": ["CREDIT_CARD", "SSN"],
  "threshold": 0.5,
  "maskCharacter": "*",
  "maskingStrategy": "PARTIAL"
}
```

**Masking Strategies**:
- `FULL`: Completely masks the value (e.g., `*******************`)
- `PARTIAL`: Shows first and last few characters (e.g., `4532****9010`)
- `HASH`: Replaces with hash value (e.g., `[HASH:A1B2C3D4]`)
- `TOKEN`: Replaces with entity type (e.g., `[CREDIT_CARD]`)

**Response**:
```json
{
  "originalText": "My credit card is 4532-1234-5678-9010 and SSN is 123-45-6789",
  "redactedText": "My credit card is 4532****9010 and SSN is 123-****6789",
  "redactedEntities": [
    {
      "originalValue": "4532-1234-5678-9010",
      "maskedValue": "4532****9010",
      "type": "CREDIT_CARD",
      "startIndex": 18,
      "endIndex": 30,
      "confidence": 0.95,
      "entityToken": "RDT-xxx-E18"
    }
  ],
  "entityCount": 2,
  "redactionToken": "RDT-xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx",
  "processingTimeMs": 52
}
```

### 9. Remask PII (Restore Original)

**Endpoint**: `POST /api/v1/redaction/remask`

**Request Body**:
```json
{
  "redactedText": "My credit card is 4532****9010 and SSN is 123-****6789",
  "redactionToken": "RDT-xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx"
}
```

**Response**:
```json
{
  "redactedText": "My credit card is 4532****9010 and SSN is 123-****6789",
  "restoredText": "My credit card is 4532-1234-5678-9010 and SSN is 123-45-6789",
  "entitiesRestored": 2,
  "success": true,
  "message": "Successfully restored original text"
}
```

### 10. Get Masking Strategies

**Endpoint**: `GET /api/v1/redaction/strategies`

**Response**:
```json
{
  "strategies": ["FULL", "PARTIAL", "HASH", "TOKEN"],
  "description": "FULL: Completely masks the value...\nPARTIAL: Shows first and last few characters...\nHASH: Replaces with a hash value...\nTOKEN: Replaces with entity type token..."
}
```

## Configuration

Edit `src/main/resources/application.yml` to customize:

```yaml
gliner:
  model:
    path: model.onnx
    tokenizer-path: tokenizer.json
    max-length: 384
    threshold: 0.5
  entities:
    financial: [...]
    trading: [...]
    personal: [...]
```

## Example Usage with cURL

### Single Text Detection
```bash
curl -X POST http://localhost:8080/api/v1/pii/detect \
  -H "Content-Type: application/json" \
  -d '{
    "text": "My SSN is 123-45-6789 and credit card is 4532-1234-5678-9010",
    "entityTypes": ["SSN", "CREDIT_CARD"]
  }'
```

### Batch Detection
```bash
curl -X POST http://localhost:8080/api/v1/pii/detect/batch \
  -H "Content-Type: application/json" \
  -d '{
    "texts": [
      "Email: john@example.com",
      "Phone: 555-123-4567"
    ]
  }'
```

### Get Entity Types
```bash
curl http://localhost:8080/api/v1/entity-types
```

### Redact PII
```bash
curl -X POST http://localhost:8080/api/v1/redaction/redact \
  -H "Content-Type: application/json" \
  -d '{
    "text": "My credit card is 4532-1234-5678-9010 and SSN is 123-45-6789",
    "entityTypes": ["CREDIT_CARD", "SSN"],
    "maskingStrategy": "PARTIAL"
  }'
```

### Remask PII
```bash
curl -X POST http://localhost:8080/api/v1/redaction/remask \
  -H "Content-Type: application/json" \
  -d '{
    "redactedText": "My credit card is 4532****9010 and SSN is 123-****6789",
    "redactionToken": "RDT-xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx"
  }'
```

### Get Masking Strategies
```bash
curl http://localhost:8080/api/v1/redaction/strategies
```

## Architecture

- **Controller Layer**: REST API endpoints for handling HTTP requests
- **Service Layer**: Business logic for PII detection
- **ONNX Service**: Model loading and inference using ONNX Runtime
- **Tokenizer Service**: Text tokenization for model input
- **Exception Handling**: Global exception handler for consistent error responses

## Model Information

This project uses the GLiNER multi-PII ONNX model from HuggingFace:
- Model: [onnx-community/gliner_multi_pii-v1](https://huggingface.co/onnx-community/gliner_multi_pii-v1)
- Framework: ONNX Runtime
- Task: Named Entity Recognition (NER) for PII detection

## Performance

- Single text processing: ~20-50ms
- Batch processing: Scales linearly with number of texts
- Pattern matching fallback for high reliability

## Development

### Build
```bash
mvn clean package
```

### Run Tests
```bash
mvn test
```

### Run Application
```bash
mvn spring-boot:run
```

## License

This project is provided as-is for educational and commercial use.

## Contributing

Contributions are welcome! Please submit pull requests or open issues for bugs and feature requests.
