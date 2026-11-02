# Implementation Plan: Context-Aware Naturally Vibe AI Sketcher (CANVAiS)

**Branch**: `001-vibe-map-sketcher` | **Date**: 2026-11-02 | **Spec**: [specs/001-vibe-map-sketcher/spec.md](file:///D:/projects/CANVAiS/git/CANVAiS/specs/001-vibe-map-sketcher/spec.md)

**Input**: Feature specification from `specs/001-vibe-map-sketcher/spec.md`

## Summary

CANVAiS delivers an interactive multi-layer map sketching application powered by multimodal AI that fuses naturally spoken or typed language ("vibe") with freehand visual gestures. The platform allows users to navigate geographic maps, manage discrete semantic layers, plot standard vector shapes, and synthesize domain-accurate geospatial entities automatically. Extensibility is achieved via Model Context Protocol (MCP) server integrations and AI skill plugins that inject custom ontologies and specialized drawing tools.

The technical implementation utilizes Java 21 LTS with Spring Boot 3.x, Gradle with the Groovy plugin, JTS Topology Suite for rigorous geometry handling, and Spock Framework for behavioral specification testing, adhering strictly to the CANVAiS Constitution.

## Technical Context

**Language/Version**: Java 21 LTS, Groovy 4.0 (for Spock specifications and build logic)

**Primary Dependencies**:
- Spring Boot 3.3.x (`spring-boot-starter-web`, `spring-boot-starter-data-jpa`, `spring-boot-starter-validation`)
- JTS Topology Suite (`org.locationtech.jts:jts-core:1.20.0`) & `jackson-datatype-jts`
- Springdoc OpenAPI 2.6.x (Swagger UI & contract exposure)
- Model Context Protocol (MCP) Java Client SDK / JSON-RPC client
- Frontend Canvas: MapLibre GL JS / Leaflet vector map with HTML5 Canvas drawing overlay

**Storage**: In-memory H2 for development and test execution; PostgreSQL / PostGIS compatible schema for persistent production profiles.

**Testing**: Spock Framework 2.4-groovy-4, JUnit Platform, Spring Boot Test (`@WebMvcTest`, `@DataJpaTest`, `@SpringBootTest`), Mockito, and JaCoCo (80% minimum line & branch coverage gate).

**Target Platform**: Cross-platform web application running on modern JVM runtime (Linux/macOS/Windows) with browser clients (Chromium, Firefox, Safari).

**Project Type**: Full-stack Spring Boot web service with embedded interactive vector canvas frontend and REST/SSE APIs.

**Performance Goals**:
- Multimodal intent fusion response < 3 seconds from input completion.
- Interactive canvas rendering at 60 FPS with 20 active layers and 1,500 vector entities.
- Full test suite execution under 45 seconds in CI.

**Constraints**:
- Strict adherence to Clean Architecture and SOLID principles.
- Zero critical Checkstyle or CodeNarc lint violations.
- Comprehensive Javadoc/GroovyDoc documentation on all public and protected classes/methods.

**Scale/Scope**: Core platform release delivering 4 major user journeys, 13 functional requirements, 3 OpenAPI contract suites, and end-to-end MCP integration.

## Constitution Check

*GATE: Evaluated before Phase 0 and re-checked post Phase 1 design.*

| Principle | Requirement | Plan Status | Justification / Notes |
|---|---|---|---|
| **I. Code Quality & Clean Architecture** | Clean separation of concerns, SOLID, zero critical static analysis | **PASS** | Clear layer boundaries (`api`, `domain`, `service`, `infrastructure`); JTS adapter encapsulates geometry handling. |
| **II. Comprehensive Testing** | Spock BDD specifications, minimum 80% JaCoCo coverage, fast execution | **PASS** | Spock 2.4 configured with `given-when-then` specifications across unit, slice, and integration layers. Coverage verified via JaCoCo gate. |
| **III. Living Documentation** | Mandatory Javadoc/GroovyDoc, OpenAPI specs, ADRs, Quickstart guide | **PASS** | Complete OpenAPI 3.0 specs generated in `contracts/`, `quickstart.md` created, and code documentation rules enforced. |
| **IV. Idiomatic Gradle & Groovy Tooling** | Gradle with Groovy plugin, reproducible build, centralized dependencies | **PASS** | Standardized Gradle setup with Groovy plugin for tests, build tasks, and CodeNarc linting. |
| **V. Extensibility & Decoupled Architecture** | MCP server and AI plugin integrations behind adapters; structured logging | **PASS** | Dedicated `McpClientAdapter` and `PluginAdapter` interfaces isolate external protocols from core domain entities. |

*Constitution Gate Status*: **ALL PASS**. Zero violations.

## Project Structure

### Documentation (this feature)

```text
specs/001-vibe-map-sketcher/
├── plan.md              # Implementation Plan (this document)
├── research.md          # Phase 0 Research (architecture & technology decisions)
├── data-model.md        # Phase 1 Data Model (entities, value objects, ER diagram)
├── quickstart.md        # Phase 1 Quickstart (runnable validation scenarios)
├── contracts/           # Phase 1 Interface Contracts (OpenAPI specifications)
│   ├── map-sessions-api.yaml
│   ├── vibe-intent-api.yaml
│   └── mcp-plugins-api.yaml
└── checklists/
    └── requirements.md  # Specification quality checklist
```

### Source Code (repository root)

```text
CANVAiS/
├── build.gradle
├── settings.gradle
├── gradlew
├── gradlew.bat
├── src/
│   ├── main/
│   │   ├── java/com/canvais/
│   │   │   ├── CanvaisApplication.java
│   │   │   ├── api/
│   │   │   │   ├── controller/
│   │   │   │   │   ├── MapSessionController.java
│   │   │   │   │   ├── VibeIntentController.java
│   │   │   │   │   └── McpIntegrationController.java
│   │   │   │   └── dto/
│   │   │   │       ├── request/
│   │   │   │       │   ├── CreateSessionRequest.java
│   │   │   │       │   ├── CreateLayerRequest.java
│   │   │   │       │   ├── CreateEntityRequest.java
│   │   │   │       │   ├── VibeIntentProposeRequest.java
│   │   │   │       │   └── RegisterMcpServerRequest.java
│   │   │   │       └── response/
│   │   │   │           ├── MapSessionResponse.java
│   │   │   │           ├── MapLayerResponse.java
│   │   │   │           ├── GraphicalEntityResponse.java
│   │   │   │           ├── VibeIntentProposalResponse.java
│   │   │   │           └── McpServerStatusResponse.java
│   │   │   ├── domain/
│   │   │   │   ├── model/
│   │   │   │   │   ├── MapSession.java
│   │   │   │   │   ├── MapLayer.java
│   │   │   │   │   ├── GraphicalEntity.java
│   │   │   │   │   ├── EntityStyle.java
│   │   │   │   │   ├── SemanticSchema.java
│   │   │   │   │   ├── EntityTypeDefinition.java
│   │   │   │   │   ├── VibeIntentProposal.java
│   │   │   │   │   └── McpServerConfig.java
│   │   │   │   └── repository/
│   │   │   │       ├── MapSessionRepository.java
│   │   │   │       ├── MapLayerRepository.java
│   │   │   │       ├── GraphicalEntityRepository.java
│   │   │   │       ├── SemanticSchemaRepository.java
│   │   │   │       └── McpServerConfigRepository.java
│   │   │   ├── service/
│   │   │   │   ├── MapSessionService.java
│   │   │   │   ├── LayerService.java
│   │   │   │   ├── GraphicalEntityService.java
│   │   │   │   ├── VibeIntentFusionService.java
│   │   │   │   └── McpClientService.java
│   │   │   └── infrastructure/
│   │   │       ├── geospatial/
│   │   │       │   ├── JtsGeometryAdapter.java
│   │   │       │   └── GeoJsonConverter.java
│   │   │       ├── mcp/
│   │   │       │   ├── McpClientAdapter.java
│   │   │       │   └── SseTransportHandler.java
│   │   │       └── ai/
│   │   │           ├── SpeechTranscriptionAdapter.java
│   │   │           └── SpatialReasoningAdapter.java
│   │   └── resources/
│   │       ├── application.yml
│   │       └── static/
│   │           ├── index.html
│   │           ├── css/styles.css
│   │           └── js/
│   │               ├── app.js
│   │               ├── map-renderer.js
│   │               ├── gesture-recorder.js
│   │               └── vibe-audio.js
│   └── test/
│       └── groovy/com/canvais/
│           ├── api/
│           │   ├── MapSessionControllerSpec.groovy
│           │   ├── VibeIntentControllerSpec.groovy
│           │   └── McpIntegrationControllerSpec.groovy
│           ├── domain/
│           │   ├── GraphicalEntitySpec.groovy
│           │   └── SemanticSchemaSpec.groovy
│           ├── service/
│           │   ├── MapSessionServiceSpec.groovy
│           │   ├── VibeIntentFusionServiceSpec.groovy
│           │   └── McpClientServiceSpec.groovy
│           └── infrastructure/
│               ├── JtsGeometryAdapterSpec.groovy
│               └── McpClientAdapterSpec.groovy
```

**Structure Decision**: Clean single-project Spring Boot application hosting both the REST/SSE backend and static interactive vector map frontend. Adheres directly to Spring Boot and Gradle Groovy directory conventions.

## Complexity Tracking

> *No constitutional violations. All architecture decisions comply with the CANVAiS Constitution.*

| Violation | Why Needed | Simpler Alternative Rejected Because |
|---|---|---|
| *None* | N/A | N/A |
