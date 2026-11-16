# Quickstart & Validation Guide: Local AI Chat Layer Sketching

**Feature**: Chat-Assisted Layer Sketching with Local AI  
**Branch**: `002-ollama-chat-layer`  
**Date**: 2026-11-09  

---

## Prerequisites

1. **Java Runtime**: JDK 21+ (`java -version`).
2. **Build Tool**: Gradle Wrapper (`./gradlew`).
3. **Local LLM Runtime**: [Ollama](https://ollama.ai) installed and running locally:
   ```bash
   # Verify Ollama is running
   ollama list

   # Pull default model (if not already downloaded)
   ollama pull llama3.2
   ```

---

## Setup & Startup

1. **Clone & Build**:
   ```bash
   ./gradlew clean build
   ```

2. **Launch Application**:
   ```bash
   ./gradlew bootRun
   ```
   The service starts on `http://localhost:8080`.
   OpenAPI documentation is available at `http://localhost:8080/swagger-ui.html`.

---

## Validation Scenarios

### Scenario 1: AI Service Health Check
Confirm that the Spring AI Ollama provider successfully detects the local Ollama instance.

- **Request**:
  ```bash
  curl -s -X GET http://localhost:8080/api/v1/chat/health
  ```
- **Expected Outcome**:
  HTTP 200 OK with:
  ```json
  {
    "status": "AVAILABLE",
    "activeModel": "llama3.2",
    "endpoint": "http://localhost:11434"
  }
  ```

---

### Scenario 2: Create Map Session & Initialize Chat Conversation
Initialize a map session and link a new chat conversation. The system automatically creates a dedicated overlay layer above the base map layer.

1. **Create Map Session**:
   ```bash
   SESSION_RESP=$(curl -s -X POST http://localhost:8080/api/v1/sessions \
     -H "Content-Type: application/json" \
     -d '{"sessionName":"Tactical Evaluation","baseMapProvider":"OpenStreetMap","centerLat":48.1800,"centerLng":8.2000,"zoomLevel":12}')
   MAP_SESSION_ID=$(echo $SESSION_RESP | grep -o '"id":"[^"]*' | head -1 | cut -d'"' -f4)
   ```

2. **Initialize Chat Session**:
   ```bash
   CHAT_RESP=$(curl -s -X POST http://localhost:8080/api/v1/chat/sessions \
     -H "Content-Type: application/json" \
     -d "{\"mapSessionId\":\"$MAP_SESSION_ID\",\"title\":\"Zone Sketching\"}")
   CHAT_SESSION_ID=$(echo $CHAT_RESP | grep -o '"id":"[^"]*' | head -1 | cut -d'"' -f4)
   OVERLAY_LAYER_ID=$(echo $CHAT_RESP | grep -o '"targetLayerId":"[^"]*' | head -1 | cut -d'"' -f4)
   ```
- **Expected Outcome**:
  HTTP 200 OK. `targetLayerId` is assigned to a dedicated overlay layer positioned above the base map layer.

---

### Scenario 3: Sketch an Observation Perimeter via Conversational Prompt
Directly prompt the local LLM to sketch a polygon entity onto the dedicated overlay layer.

- **Request**:
  ```bash
  curl -s -X POST "http://localhost:8080/api/v1/chat/sessions/$CHAT_SESSION_ID/messages" \
    -H "Content-Type: application/json" \
    -d '{
      "message": "Draw a secure perimeter around the central communications hub",
      "viewportBounds": {
        "north": 48.2000,
        "south": 48.1600,
        "east": 8.2300,
        "west": 8.1700
      },
      "centerCoordinates": {
        "lat": 48.1800,
        "lng": 8.2000
      }
    }'
  ```
- **Expected Outcome**:
  - HTTP 200 OK.
  - `actionType` is `"CREATE"`.
  - `affectedEntities` array contains a valid GeoJSON `Polygon` or `Point` entity anchored to the overlay layer (`layerId == $OVERLAY_LAYER_ID`).
  - Base map layer (`layerOrder == 0`) remains completely unmodified.

---

### Scenario 4: Ingest Recorded Vibe Input into Chat
Forward a captured voice/text vibe record directly into the chat session stream.

- **Request**:
  ```bash
  curl -s -X POST "http://localhost:8080/api/v1/chat/sessions/$CHAT_SESSION_ID/vibe" \
    -H "Content-Type: application/json" \
    -d '{
      "vibeTranscript": "Plot a transit corridor following the main ridge northward",
      "viewportBounds": {
        "north": 48.2200,
        "south": 48.1600,
        "east": 8.2500,
        "west": 8.1800
      }
    }'
  ```
- **Expected Outcome**:
  - HTTP 200 OK.
  - The vibe record is logged into the conversation history with role `USER`.
  - Local AI assistant responds with an explanation and generated `LineString` vector entity.

---

### Scenario 5: Multi-Turn Iterative Refinement
Send a follow-up refinement message in the same chat session to adjust an existing object.

- **Request**:
  ```bash
  curl -s -X POST "http://localhost:8080/api/v1/chat/sessions/$CHAT_SESSION_ID/messages" \
    -H "Content-Type: application/json" \
    -d '{
      "message": "Change the perimeter line color to red and rename it High Security Zone"
    }'
  ```
- **Expected Outcome**:
  - HTTP 200 OK.
  - `actionType` is `"UPDATE"`.
  - The previously created perimeter updates its style properties (`strokeColor == "#FF0000"`) without generating a duplicate entity.

---

### Scenario 6: Layer Isolation & Visibility Verification
Validate that clearing or toggling the overlay layer does not affect base map rendering.

1. **Verify Overlay Contains Entities**:
   ```bash
   curl -s -X GET "http://localhost:8080/api/v1/layers/$OVERLAY_LAYER_ID"
   ```
2. **Clear Overlay Layer**:
   ```bash
   curl -s -X DELETE "http://localhost:8080/api/v1/chat/sessions/$CHAT_SESSION_ID/layer"
   ```
- **Expected Outcome**:
  HTTP 200 OK. Only the sketched entities on the overlay layer are removed. The base map layer and other user-created layers remain untouched.

---

### Scenario 7: Offline Resilience Verification
Test system behavior when the local Ollama service is stopped.

1. **Stop Ollama** or simulate network interruption.
2. **Send Prompt**:
   ```bash
   curl -s -X POST "http://localhost:8080/api/v1/chat/sessions/$CHAT_SESSION_ID/messages" \
     -H "Content-Type: application/json" \
     -d '{"message":"Draw a marker at downtown square"}'
   ```
- **Expected Outcome**:
  - HTTP 200 OK (with graceful fallback or user-friendly status message).
  - The application logs a diagnostic notice and informs the user to check local Ollama availability.
  - No unhandled server exceptions (HTTP 500) or application crash.
