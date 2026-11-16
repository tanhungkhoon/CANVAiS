# Phase 0 Research: Local AI Chat Layer Sketching (CANVAiS)

**Feature**: Incorporate Chat Interface using Spring AI with Local Ollama LLM to support drawing of graphical objects into a separate layer on top of the base map layer via the existing vibe record input.  
**Branch**: `002-ollama-chat-layer`  
**Date**: 2026-11-09  

---

## 1. Spring AI and Local Ollama Integration

### Decision
Integrate the Spring AI framework using `spring-ai-starter-model-ollama` (BOM version `1.0.0-M6` or compatible) and Spring AI's fluent `ChatClient` abstraction, connected to a locally hosted Ollama instance at `http://localhost:11434` with configurable default model (e.g., `llama3.2` / `mistral`).

### Rationale
- **Idiomatic Spring Boot Integration**: Spring AI provides native auto-configuration, robust retry/timeout interceptors, prompt templating, and declarative structured output converters that seamlessly fit Spring Boot 3.3.x.
- **Portability & Local Privacy**: Running Ollama locally eliminates cloud API expenses, prevents sensitive spatial/tactical data leakage, and works offline without public internet connectivity.
- **Standardized Client Abstraction**: Using `ChatClient.Builder` with Spring AI allows swapping local models (e.g., `llama3.2`, `phi3`, `mistral`, `gemma2`) via simple configuration without changing Java service code.

### Alternatives Considered
- *Direct HTTP calls via `RestClient` / `WebClient`*: Rejected because it requires reimplementing prompt engineering abstractions, structured JSON parsing, chat memory history, and retry policies already provided by Spring AI.
- *LangChain4j*: Evaluated, but Spring AI is the idiomatic Spring standard supported by VMware/Broadcom, aligning with Principle I and IV of the CANVAiS Constitution.
- *Cloud LLMs (OpenAI/Anthropic)*: Rejected to preserve user privacy, local deployment autonomy, and avoid recurring cost per drawing session.

---

## 2. Structured Geospatial Generation from LLM Output

### Decision
Enforce strict JSON Schema generation using Spring AI's `BeanOutputConverter` or system prompt schema definitions that specify a canonical GeoJSON-compatible output structure:
```json
{
  "assistantMessage": "Human-friendly reply describing what was drawn or modified",
  "action": "CREATE | UPDATE | DELETE | NONE",
  "targetEntityId": "optional-uuid-for-updates",
  "objects": [
    {
      "name": "Designated Area / Entity Name",
      "entityType": "PerimeterZone | TransitCorridor | ObservationPoint | Custom",
      "geometryType": "Point | LineString | Polygon",
      "coordinates": [[lon, lat], ...],
      "style": {
        "strokeColor": "#E76F51",
        "fillColor": "#E76F5133",
        "strokeWidth": 2.5,
        "dashArray": null,
        "icon": null
      },
      "semanticAttributes": {
        "status": "ACTIVE",
        "synthesizedBy": "Ollama-Chat"
      },
      "confidence": 0.95
    }
  ]
}
```

### Rationale
- **Deterministic Coordinate Binding**: Providing the LLM with the active map viewport bounding box (North, South, East, West) and center coordinate anchors ensures generated shapes fall within the user's visible geographic canvas rather than arbitrary coordinates.
- **Topological Integrity**: JTS Topology Suite validates geometries prior to persisting or transmitting to the client, automatically repairing self-intersecting polygons or clamping out-of-bound coordinates.
- **Multi-Object & Action Flexibility**: A unified response schema supports creating multiple objects at once, modifying an existing object, or explaining spatial context conversationally.

### Alternatives Considered
- *Unconstrained Natural Language with Regex Extraction*: Fragile, error-prone when coordinates are formatted inconsistently.
- *Function Calling / Tool Execution*: Ollama models (especially smaller 3B/7B parameters) vary in tool-calling reliability. Prompt-driven structured JSON response format is universally supported across lightweight local models (e.g. `llama3.2:3b`).

---

## 3. Dedicated Overlay Layer Architecture

### Decision
Automatically provision or bind to a dedicated `ChatSketchLayer` (with layer type `OVERLAY` and `layerOrder > 0`) located strictly above the base map layer (`layerOrder = 0`). The base map layer remains immutable and non-destructive.

### Rationale
- **Isolation of Concerns**: Users can toggle visibility, clear sketched entities, adjust opacity, or discard experimental AI drawings without altering base map cartographic tiles or other custom domain layers.
- **Integration with Existing Domain Model**: CANVAiS already features `MapLayer` and `GraphicalEntity` domain models. A dedicated `LayerType.OVERLAY` or `LayerType.CHAT_SKETCH` directly inherits existing styling, spatial querying, and GeoJSON export pipelines.
- **Non-Destructive Workflows**: Prevents user frustration if an AI prompt generates unexpected spatial geometries.

### Alternatives Considered
- *Plotting directly into the active user layer*: Risky because flawed LLM outputs could corrupt user-curated data layers.
- *Client-side-only transient rendering*: Prevents multi-turn persistence, layer export, and collaborative session synchronization.

---

## 4. Vibe Record Input Ingestion into Chat Stream

### Decision
Add a bidirectional bridge allowing existing vibe recordings (speech transcription results and text vibe inputs from `VibeIntentController` / `vibe-audio.js`) to be routed into the chat interface as first-class chat turns with an attached `vibeRecordId`.

### Rationale
- **Workflow Continuity**: Users already have the voice recording and gesture capture tools on the canvas. With this bridge, a user can speak a vibe, view the transcript, and click "Send to Chat Assistant" or talk directly within the chat panel.
- **Context Richness**: The chat turn retains metadata about the vibe capture (audio duration, transcription confidence, attached gesture strokes) to provide context for the local LLM.

### Alternatives Considered
- *Replacing the vibe recorder with the chat panel*: Destructive to existing multimodal workflows (e.g., simultaneous freehand gesture sketching + spoken vibe). Keeping both unified provides maximum versatility.

---

## 5. Offline Resilience & Service Degradation

### Decision
Implement a resilient circuit/fallback pattern in `OllamaChatService`. If the local Ollama daemon is offline, timed out, or unconfigured:
1. Detect connection failure (`ResourceAccessException` / `ConnectException`).
2. Return a clean, non-crashing response state (`OLLAMA_SERVICE_UNAVAILABLE`).
3. Offer an automatic fallback to the heuristic `SpatialReasoningAdapter` for basic geometric primitives (perimeters, routes, markers) based on keyword matching, along with a user prompt to start Ollama (`ollama run llama3.2`).

### Rationale
- **Zero Freeze / Crash Guarantee**: Satisfies constitutional requirement for robust boundary error handling and SC-005.
- **Failsafe Operability**: The user can continue sketching even before their local model runtime finishes booting.

### Alternatives Considered
- *Throwing HTTP 500 error*: Degrades user experience, causing UI toast errors.
- *Blocking application startup*: Completely unacceptable if Ollama is not yet launched.
