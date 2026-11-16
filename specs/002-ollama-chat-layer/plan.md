# Implementation Plan: Chat-Assisted Layer Sketching with Local AI

**Branch**: `002-ollama-chat-layer` | **Date**: 2026-11-09 | **Spec**: [specs/002-ollama-chat-layer/spec.md](file:///D:/projects/CANVAiS/git/CANVAiS/specs/002-ollama-chat-layer/spec.md)

**Input**: Feature specification from `specs/002-ollama-chat-layer/spec.md`

## Summary

This feature incorporates an interactive conversational chat interface into CANVAiS, powered by Spring AI and a local Ollama Large Language Model (e.g., `llama3.2`). The system translates conversational prompts and existing captured vibe record inputs into structured geospatial vector objects (points, perimeters, corridors, and markers) that are drawn onto an isolated overlay layer positioned directly above the base map layer. This architecture preserves base cartographic integrity, allows iterative multi-turn prompt refinement, and provides graceful offline degradation when the local model service is unavailable.

## Technical Context

**Language/Version**: Java 21 LTS, Groovy 4.0 (for Spock specifications and build logic)

**Primary Dependencies**:
- Spring Boot 3.3.5 (`spring-boot-starter-web`, `spring-boot-starter-data-jpa`, `spring-boot-starter-validation`)
- Spring AI (`spring-ai-starter-model-ollama` / `spring-ai-bom:1.0.0-M6` or compatible)
- JTS Topology Suite (`org.locationtech.jts:jts-core:1.20.0`) & `jackson-datatype-jts`
- Springdoc OpenAPI 2.6.0 (Swagger UI and OpenAPI 3.0 generation)
- Frontend Canvas: Leaflet / MapLibre GL JS with custom Chat UI overlay panel

**Storage**: In-memory H2 database for rapid development and testing; compatible with PostGIS / PostgreSQL schema for production persistence.

**Testing**: Spock Framework 2.4-groovy-4, JUnit 5, Spring Boot Test slices (`@WebMvcTest`, `@DataJpaTest`, `@SpringBootTest`), Mockito, and JaCoCo coverage verification (80% minimum line and branch coverage gate).

**Target Platform**: Cross-platform web application running on modern JVM runtime (Linux, macOS, Windows) interfacing with a local Ollama daemon on `localhost:11434`.

**Project Type**: Full-stack Spring Boot web application with embedded static frontend and RESTful APIs.

**Performance Goals**:
- Chat prompt submission to vector rendering on the canvas in < 6 seconds on standard local workstation hardware.
- Local AI health check probe latency < 100 ms.
- Test suite execution in under 45 seconds during CI pipeline runs.

**Constraints**:
- Strict adherence to Clean Architecture and SOLID principles.
- Zero critical Checkstyle or CodeNarc lint violations.
- Comprehensive Javadoc/GroovyDoc documentation on all public and protected classes/methods.
- Zero mutation of the base map layer (`layerOrder = 0`).

**Scale/Scope**: 4 prioritized user journeys, 10 functional requirements, 1 OpenAPI contract suite, 5 validation scenarios, and resilient local LLM offline fallback.

## Constitution Check

*GATE: Evaluated before Phase 0 and re-checked post Phase 1 design.*

| Principle | Requirement | Plan Status | Justification / Notes |
|---|---|---|---|
| **I. Code Quality & Clean Architecture** | Clean separation of concerns, SOLID, zero critical static analysis violations | **PASS** | Strict layer separation: `api` (controllers/DTOs), `domain` (models/repositories), `service` (business logic/session coordination), `infrastructure` (Spring AI Ollama adapter, JTS geospatial converter). |
| **II. Comprehensive Testing & Test-First Verification** | Spock BDD specifications, minimum 80% JaCoCo coverage, fast execution | **PASS** | Comprehensive Spock specifications (`ChatServiceSpec`, `ChatApiControllerSpec`, `OllamaClientAdapterSpec`) with `given-when-then` blocks and isolated mocks. JaCoCo gate enforced. |
| **III. Living Documentation** | Mandatory Javadoc/GroovyDoc, OpenAPI specs, ADRs, Quickstart guide | **PASS** | Full OpenAPI 3.0 contract in `contracts/chat-service-api.yaml`, complete `quickstart.md`, entity definitions in `data-model.md`, and mandatory docstrings. |
| **IV. Idiomatic Gradle & Groovy Tooling** | Gradle with Groovy plugin, reproducible build, centralized dependencies | **PASS** | Gradle build uses centralized Spring AI BOM, Checkstyle, CodeNarc, and JaCoCo plugins without conflicting dependencies. |
| **V. Extensibility & Modular Architecture** | AI integrations behind adapters; resilient error handling; structured logging | **PASS** | Local Ollama interaction encapsulated behind `AiModelClientAdapter` interface with circuit-breaker fallback for offline resilience. |

*Constitution Gate Status*: **ALL PASS**. Zero violations.

## Project Structure

### Documentation (this feature)

```text
specs/002-ollama-chat-layer/
├── spec.md              # Feature Specification
├── plan.md              # Implementation Plan (this document)
├── research.md          # Phase 0 Research (technology decisions & trade-offs)
├── data-model.md        # Phase 1 Data Model (entities, value objects, ER diagram)
├── quickstart.md        # Phase 1 Quickstart (runnable validation scenarios)
├── contracts/           # Phase 1 Interface Contracts
│   └── chat-service-api.yaml
└── checklists/
    └── requirements.md  # Specification quality checklist
```

### Source Code (repository root)

```text
CANVAiS/
├── build.gradle
├── settings.gradle
├── src/
│   ├── main/
│   │   ├── java/com/canvais/
│   │   │   ├── CanvaisApplication.java
│   │   │   ├── api/
│   │   │   │   ├── controller/
│   │   │   │   │   ├── ChatController.java
│   │   │   │   │   ├── MapLayerController.java
│   │   │   │   │   ├── MapSessionController.java
│   │   │   │   │   └── VibeIntentController.java
│   │   │   │   └── dto/
│   │   │   │       ├── request/
│   │   │   │       │   ├── CreateChatSessionRequest.java
│   │   │   │       │   ├── IngestVibeToChatRequest.java
│   │   │   │       │   └── SendChatMessageRequest.java
│   │   │   │       └── response/
│   │   │   │           ├── AiHealthResponse.java
│   │   │   │           ├── ChatMessageDto.java
│   │   │   │           ├── ChatMessageTurnResponse.java
│   │   │   │           ├── ChatSessionDetailResponse.java
│   │   │   │           └── ChatSessionResponse.java
│   │   │   ├── domain/
│   │   │   │   ├── model/
│   │   │   │   │   ├── ChatActionType.java
│   │   │   │   │   ├── ChatEntityAction.java
│   │   │   │   │   ├── ChatMessage.java
│   │   │   │   │   ├── ChatRole.java
│   │   │   │   │   ├── ChatSession.java
│   │   │   │   │   ├── GraphicalEntity.java
│   │   │   │   │   └── MapLayer.java
│   │   │   │   └── repository/
│   │   │   │       ├── ChatMessageRepository.java
│   │   │   │       └── ChatSessionRepository.java
│   │   │   ├── infrastructure/
│   │   │   │   ├── ai/
│   │   │   │   │   ├── AiModelClientAdapter.java
│   │   │   │   │   ├── OllamaChatModelAdapter.java
│   │   │   │   │   ├── SpatialReasoningAdapter.java
│   │   │   │   │   └── SpeechTranscriptionAdapter.java
│   │   │   │   └── geospatial/
│   │   │   │       ├── GeoJsonConverter.java
│   │   │   │       └── JtsGeometryAdapter.java
│   │   │   └── service/
│   │   │       ├── ChatService.java
│   │   │       ├── GraphicalEntityService.java
│   │   │       ├── LayerService.java
│   │   │       └── VibeIntentFusionService.java
│   │   └── resources/
│   │       ├── application.yml
│   │       └── static/
│   │           ├── index.html
│   │           ├── css/
│   │           │   └── styles.css
│   │           └── js/
│   │               ├── chat-panel.js
│   │               ├── map-renderer.js
│   │               └── vibe-audio.js
│   └── test/
│       └── groovy/com/canvais/
│           ├── api/
│           │   └── ChatControllerSpec.groovy
│           ├── infrastructure/
│           │   └── OllamaChatModelAdapterSpec.groovy
│           └── service/
│               └── ChatServiceSpec.groovy
```

**Structure Decision**: Retains the existing single-project Spring Boot architecture adhering to Clean Architecture layers (`api`, `domain`, `service`, `infrastructure`), adding domain entities (`ChatSession`, `ChatMessage`), service coordination (`ChatService`), infrastructure adapter (`OllamaChatModelAdapter`), and frontend chat widget (`chat-panel.js`).

## Complexity Tracking

> **Constitution Check has zero unjustified violations; all principles are satisfied.**

| Component / Pattern | Why Needed | Simpler Alternative Rejected Because |
|---|---|---|
| Dedicated `ChatSketchLayer` (Overlay Layer) | Protects base map cartography and user data layers from accidental AI corruption | Plotting directly to active layer risks overwriting user-curated data |
| `AiModelClientAdapter` Interface | Enables switching between Spring AI Ollama and heuristic fallback during model downtime | Direct coupling to Spring AI `ChatClient` causes hard failures when local daemon is stopped |
| GeoJSON Schema Constrained Prompting | Guarantees syntactically and topologically valid coordinates for JTS parser | Unstructured conversational text requires brittle regex parsing |
