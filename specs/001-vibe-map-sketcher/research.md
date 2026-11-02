# Phase 0 Research: Context-Aware Naturally Vibe AI Sketcher (CANVAiS)

**Feature**: `001-vibe-map-sketcher` | **Date**: 2026-11-02

## 1. Multimodal Vibe + Freehand Gesture Fusion Architecture

### Context
CANVAiS requires fusing naturally spoken or typed language ("vibe") with freehand visual gestures on a geographic canvas to produce structured, semantically enriched vector graphics.

### Decision
Implement a decoupled two-stage multimodal intent pipeline:
1. **Input Normalization & Transcription**:
   - Audio input captured via browser `MediaRecorder` API is streamed or posted as an audio blob (WAV/WebM) to `/api/v1/vibe/transcribe` or transcribed using a Speech-to-Text adapter (e.g., OpenAI Whisper API or local whisper service).
   - Text vibe can be provided directly as a fallback.
   - Gesture coordinates are captured as an ordered sequence of geographic coordinates (`[{lat, lng, timestamp}]`) and bounding box / convex hull geometry.
2. **Spatial-Semantic Reasoning Engine**:
   - The backend `VibeIntentFusionService` packages the transcribed vibe, gesture geometry bounds, target layer's `SemanticSchema`, and existing surrounding entities into a structured spatial reasoning prompt.
   - The reasoning model generates standard GeoJSON features (Points, LineStrings, Polygons) with attributes populated according to the layer's schema.
   - The result is returned as a `VibeIntentProposal` with an acceptance token, allowing users to preview, refine, or commit the generated entities to the layer.

### Rationale
- Decoupling transcription from spatial reasoning allows modular replacement of speech-to-text engines and supports instant text input without speech dependencies.
- Supplying the target layer's `SemanticSchema` to the prompt guarantees the AI produces valid classifications and attributes matching the domain ontology.

### Alternatives Considered
- *Direct end-to-end audio-to-shape model*: Rejected because monolithic audio-to-vector models lack enterprise reproducibility, domain ontology schema guarantees, and modular debuggability.
- *Pure client-side heuristic gesture recognition*: Rejected because it cannot understand complex natural language vibe ("plot tactical triage zone along the river bank") or domain-specific semantic classifications.

---

## 2. Geospatial Engine & Geometry Modeling (Backend & Frontend)

### Context
CANVAiS must represent, validate, and manipulate real-world geographic coordinates, multi-layer collections, and spatial geometries (points, polylines, polygons) accurately.

### Decision
- **Backend Geometry Engine**: JTS Topology Suite (`org.locationtech.jts:jts-core`) alongside Jackson Datatype JTS / GeoJSON serialization.
  - Standard WGS84 (EPSG:4326) for persistence and GeoJSON interchange; Web Mercator (EPSG:3857) for distance and area calculations.
  - Clean domain models mapping GeoJSON features to `GraphicalEntity` entities.
- **Frontend Map & Canvas**: Modern vector map client (MapLibre GL JS or Leaflet) with a custom vector canvas overlay supporting freehand drawing gestures, high-frame-rate rendering (60 FPS with 1500+ entities), and multi-layer management.

### Rationale
- JTS is the industry standard for Java spatial computations, polygon clipping, buffer generation, and topological validations.
- GeoJSON provides a universally recognized, technology-agnostic standard for exchanging vector geospatial entities with AI models, frontends, and external GIS tools.

### Alternatives Considered
- *PostGIS-only geometry processing*: Requires heavy database dependencies during unit testing. JTS allows pure in-memory unit and integration testing with H2 or PostgreSQL.
- *Custom raw coordinate arrays*: High risk of projection and winding order bugs; violates clean code and maintainability principles.

---

## 3. Model Context Protocol (MCP) & AI Skill Plugin Integration

### Context
CANVAiS must support custom layers powered by external AI skill plugins or integrations with MCP servers (discovering custom tools, entity taxonomies, and domain validation).

### Decision
- Implement an MCP Client Adapter layer within the Spring Boot application conforming to the official Model Context Protocol (JSON-RPC 2.0 over SSE and Stdio transports).
- Define a unified `PluginAdapter` interface:
  ```java
  public interface PluginAdapter {
      String getPluginId();
      List<SemanticSchema> getSupportedSchemas();
      CompletableFuture<List<GraphicalEntity>> generateEntities(VibeIntentRequest request);
      ValidationResult validateEntity(GraphicalEntity entity, SemanticSchema schema);
  }
  ```
- The `McpClientService` connects to registered MCP servers, invokes `tools/list` and `resources/list`, maps exposed schemas to CANVAiS `SemanticSchema` models, and delegates specialized vibe plotting actions via `tools/call`.

### Rationale
- Conforms to the open MCP specification, enabling interoperability with any MCP server (e.g., weather data, defense symbology, urban zoning datasets).
- Isolates external connection lifecycle and protocol serialization from core domain logic (Constitution Principle V).

### Alternatives Considered
- *Hardcoded domain plugins*: Fails the requirement for open extensibility.
- *Direct browser-to-MCP connections*: Browser security sandboxes (CORS, lack of stdio transport) prevent connecting to local CLI tools or protected internal MCP servers.

---

## 4. Testing, Quality & Tooling Strategy (Constitution Alignment)

### Context
The project constitution mandates Spring Boot, Gradle with Groovy plugin, Spock Framework (BDD specifications with `given-when-then`), JaCoCo with minimum 80% coverage, and comprehensive Javadoc/GroovyDoc documentation.

### Decision
- **Build**: Gradle 8.x with `groovy` plugin, `java` toolchain configured for Java 21 LTS, and `org.springframework.boot` plugin (version 3.3.x).
- **Testing**:
  - Spock Framework 2.4-groovy-4 for unit, slice, and integration specifications.
  - Spring Boot Test slices (`@WebMvcTest`, `@DataJpaTest`) executed via Spock specifications.
  - JaCoCo plugin configured with strict `jacocoTestCoverageVerification` enforcing minimum 80% line and branch coverage.
  - CodeNarc for Groovy linting and Checkstyle for Java source verification.
- **Documentation**:
  - All public Java and Groovy classes and methods documented with full Javadoc/GroovyDoc.
  - Springdoc OpenAPI (Swagger UI) for automated contract verification and documentation at `/swagger-ui.html`.

### Rationale
- Fully satisfies all 5 core constitution principles without compromise.
- Spock specifications provide unmatched readability and behavioral clarity for complex geometric and multimodal test cases.
