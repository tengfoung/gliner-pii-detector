# Quick Start Guide

## Prerequisites Check

Ensure you have:
- ✅ Java 17+ installed (`java -version`)
- ✅ Maven 3.6+ installed (`mvn -version`)
- ✅ Model files present: `model.onnx` and `tokenizer.json`

## 1. Build the Project

```bash
mvn clean install
```

## 2. Run the Application

```bash
mvn spring-boot:run
```

The application will start on `http://localhost:8080`

## 3. Test the API

### Using cURL

**Health Check:**
```bash
curl http://localhost:8080/api/v1/pii/health
```

**Detect PII:**
```bash
curl -X POST http://localhost:8080/api/v1/pii/detect \
  -H "Content-Type: application/json" \
  -d '{
    "text": "My email is john@example.com and credit card is 4532-1234-5678-9010"
  }'
```

### Using the Example Requests

Open `examples/example-requests.http` in your IDE (IntelliJ IDEA, VS Code with REST Client extension) and run the requests directly.

## 4. Common Use Cases

### Financial Data Detection
```bash
curl -X POST http://localhost:8080/api/v1/pii/detect \
  -H "Content-Type: application/json" \
  -d '{
    "text": "Transfer from account 123456789 using routing 021000021",
    "entityTypes": ["BANK_ACCOUNT", "ROUTING_NUMBER"]
  }'
```

### Trading Data Detection
```bash
curl -X POST http://localhost:8080/api/v1/pii/detect \
  -H "Content-Type: application/json" \
  -d '{
    "text": "Buy 100 AAPL shares, ISIN: US0378331005",
    "entityTypes": ["TICKER_SYMBOL", "ISIN"]
  }'
```

### Batch Processing
```bash
curl -X POST http://localhost:8080/api/v1/pii/detect/batch \
  -H "Content-Type: application/json" \
  -d '{
    "texts": [
      "Email: alice@company.com",
      "Phone: 555-123-4567",
      "SSN: 123-45-6789"
    ]
  }'
```

## 5. View Available Entity Types

```bash
curl http://localhost:8080/api/v1/entity-types
```

## Troubleshooting

### Port Already in Use
Change the port in `src/main/resources/application.yml`:
```yaml
server:
  port: 8081
```

### Model Files Not Found
Ensure `model.onnx` and `tokenizer.json` are in the project root directory.

### Out of Memory
Increase JVM heap size:
```bash
export MAVEN_OPTS="-Xmx2g"
mvn spring-boot:run
```

## Next Steps

- Read the full [README.md](README.md) for detailed documentation
- Explore the example requests in `examples/example-requests.http`
- Customize entity types in `src/main/resources/application.yml`
- Add custom patterns in `PiiDetectionService.java`
