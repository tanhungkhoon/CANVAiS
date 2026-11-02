# Quickstart Validation Guide: Context-Aware Naturally Vibe AI Sketcher (CANVAiS)

**Feature**: `001-vibe-map-sketcher` | **Date**: 2026-11-02

This guide provides end-to-end instructions for launching, validating, and testing CANVAiS capabilities across core map sessions, multimodal vibe sketching, and MCP integrations.

---

## 1. Prerequisites & Environment Setup

- **Java Development Kit**: JDK 21 (LTS)
- **Build Tool**: Gradle 8.x (wrapper provided: `./gradlew`)
- **Browser**: Modern Chromium/Firefox/Safari browser supporting HTML5 Canvas, WebGL, and MediaStream recording
- **Optional**: Model Context Protocol (MCP) server running locally (e.g. at `http://localhost:8081/mcp/sse`)

Verify your environment:
```bash
java -version
./gradlew --version
```

---

## 2. Build & Verification Gates

Run the complete build with Spock specification test suites, CodeNarc linting, and JaCoCo coverage verification:

```bash
# Execute unit and integration tests (Spock Specifications)
./gradlew test

# Verify minimum 80% line and branch coverage threshold
./gradlew jacocoTestCoverageVerification

# Run all quality checks (Checkstyle, CodeNarc, Tests, JaCoCo)
./gradlew check

# Launch application
./gradlew bootRun
```

The Spring Boot application starts on port `8080`. Swagger/OpenAPI documentation is accessible at:
`http://localhost:8080/swagger-ui.html`

---

## 3. End-to-End Validation Scenarios

### Scenario 1: Create Map Session & Custom Semantic Layer

Create a map session centered on a geographic coordinate and attach an operational layer:

```bash
# 1. Create a new Map Session
SESSION_ID=$(curl -s -X POST http://localhost:8080/api/v1/sessions \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Black Forest Search & Rescue",
    "description": "Wilderness coordinate grid for perimeter monitoring",
    "centerLat": 48.1800,
    "centerLng": 8.2000,
    "zoomLevel": 13
  }' | jq -r '.id')

echo "Session created with ID: $SESSION_ID"

# 2. Add a Layer to the Session
LAYER_ID=$(curl -s -X POST "http://localhost:8080/api/v1/sessions/$SESSION_ID/layers" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Evacuation Routes & Perimeters",
    "layerType": "STANDARD",
    "orderIndex": 1
  }' | jq -r '.id')

echo "Layer created with ID: $LAYER_ID"
```

**Expected Outcome**: Session and Layer created successfully with HTTP 201; UUIDs returned.

---

### Scenario 2: Plot a Standard Geographic Object

Plot a boundary polygon on the newly created layer:

```bash
curl -s -X POST "http://localhost:8080/api/v1/layers/$LAYER_ID/entities" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Primary Hazard Zone",
    "entityType": "HazardPerimeter",
    "geometry": {
      "type": "Polygon",
      "coordinates": [[
        [8.190, 48.175],
        [8.210, 48.175],
        [8.210, 48.185],
        [8.190, 48.185],
        [8.190, 48.175]
      ]]
    },
    "style": {
      "strokeColor": "#E63946",
      "fillColor": "#E6394633",
      "strokeWidth": 2.5
    },
    "semanticAttributes": {
      "threatLevel": "HIGH",
      "containmentStatus": "ACTIVE"
    }
  }' | jq .
```

**Expected Outcome**: Returns HTTP 201 with plotted `GraphicalEntity` payload.

---

### Scenario 3: Multimodal Vibe + Freehand Gesture Intent Fusion

Submit a combined textual vibe description with ordered gesture stroke coordinates to generate candidate entities:

```bash
PROPOSAL_RESP=$(curl -s -X POST http://localhost:8080/api/v1/vibe/propose \
  -H "Content-Type: application/json" \
  -d "{
    \"sessionId\": \"$SESSION_ID\",
    \"targetLayerId\": \"$LAYER_ID\",
    \"textVibe\": \"Establish an observation checkpoint on this ridgeline with a safe corridor heading east\",
    \"gestureStrokes\": [
      {\"lat\": 48.180, \"lng\": 8.195, \"timestampMs\": 100},
      {\"lat\": 48.182, \"lng\": 8.200, \"timestampMs\": 300},
      {\"lat\": 48.184, \"lng\": 8.215, \"timestampMs\": 600}
    ],
    \"viewportBounds\": {
      \"north\": 48.190,
      \"south\": 48.170,
      \"east\": 8.230,
      \"west\": 8.180
    }
  }")

PROPOSAL_ID=$(echo $PROPOSAL_RESP | jq -r '.proposalId')
echo "Proposal generated: $PROPOSAL_ID"
echo $PROPOSAL_RESP | jq .interpretedSummary

# Accept and commit proposal to layer
curl -s -X POST "http://localhost:8080/api/v1/vibe/proposals/$PROPOSAL_ID/accept" \
  -H "Content-Type: application/json" \
  -d '{}' | jq .
```

**Expected Outcome**: Returns HTTP 200 with proposal containing synthesized line and point objects; accepting the proposal commits entities into `$LAYER_ID`.

---

### Scenario 4: Model Context Protocol (MCP) Server Discovery

Register and inspect an MCP server providing custom entity schemas:

```bash
# Register MCP Server
curl -s -X POST http://localhost:8080/api/v1/integrations/mcp \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Disaster Response MCP Server",
    "transportType": "SSE",
    "endpointUri": "http://localhost:8081/mcp/sse"
  }' | jq .

# Discover available tools and entity schemas
curl -s -X POST http://localhost:8080/api/v1/integrations/mcp/disaster-response-mcp/discover | jq .
```

**Expected Outcome**: Server status transitions to `CONNECTED` and discovered tools/schemas are cataloged.
