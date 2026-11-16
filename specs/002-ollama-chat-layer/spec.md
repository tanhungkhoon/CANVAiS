# Feature Specification: Chat-Assisted Layer Sketching with Local AI

**Feature Branch**: `002-ollama-chat-layer`

**Created**: 2026-11-09

**Status**: Draft

**Input**: User description: "incoporate chat interface using sping ai with local ollama llm to support drawing of graphical objects into a separate layer on top of the base map layer via the existing vibe record input"

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Conversational Sketching via Chat and Vibe Input (Priority: P1)

A map user or spatial planner opens the CANVAiS workspace, accesses an integrated conversational chat panel, and provides sketching prompts—either typed directly or fed through the existing vibe record input. A local language model interprets the spatial and semantic instructions, generating vector graphical objects (points, perimeters, routes, markers) directly onto a dedicated overlay layer positioned above the base map layer.

**Why this priority**: Core value proposition. Enables hands-free, natural-language sketching on top of existing base maps, connecting conversational AI with spatial visualization without compromising the underlying map data.

**Independent Test**: Can be fully tested by opening the workspace, entering a vibe or chat prompt (e.g., "draw a rectangular search perimeter around the central park area"), and verifying that a corresponding polygon entity appears on the dedicated overlay layer while the base map remains untouched.

**Acceptance Scenarios**:

1. **Given** an open map workspace and an active chat interface, **When** the user submits a natural-language description specifying a geographical feature, **Then** the local AI model parses the intent and generates the corresponding vector object on a dedicated overlay layer above the base map.
2. **Given** an existing recorded vibe input (voice or transcribed text), **When** the user sends the vibe record into the chat interface, **Then** the chat stream consumes the record as an input prompt and renders the proposed graphical objects onto the overlay layer.
3. **Given** a generated graphical object proposal, **When** the object appears on the map canvas, **Then** the chat interface displays a confirmation card showing the generated object summary with options to accept, adjust, or discard.

---

### User Story 2 - Iterative Modification and Follow-Up Refinement (Priority: P2)

After graphical objects have been sketched onto the overlay layer, the user converses with the chat assistant to refine, restyle, move, or remove specific objects (e.g., "extend the route 200 meters eastward" or "change the marker color to red and label it Evacuation Point A").

**Why this priority**: Conversational interfaces derive their greatest utility from multi-turn refinement. Users rarely get complex spatial layouts right in a single prompt.

**Independent Test**: Can be tested by generating an initial marker or polygon, submitting a follow-up refinement message in the chat session, and verifying the existing object updates its geometry or style in-place without duplicating entities.

**Acceptance Scenarios**:

1. **Given** one or more sketched objects already present on the overlay layer, **When** the user provides a contextual follow-up prompt referencing an existing object, **Then** the system modifies the target object's geometry, styling, or metadata accordingly.
2. **Given** an existing sketched object, **When** the user asks to delete or remove it via chat, **Then** the target object is removed from the overlay layer and confirmed in the chat feed.
3. **Given** an ambiguous follow-up instruction (e.g., "move it slightly"), **When** the local AI processes the request, **Then** the chat assistant asks a targeted clarifying question or proposes a default adjustment with a visual preview.

---

### User Story 3 - Layer Isolation and Visual Layer Controls (Priority: P2)

All objects produced through the chat interface are strictly contained within a distinct overlay layer positioned above the base map layer. The user can toggle visibility, modify layer opacity, rename the layer, clear objects, or export the sketched layer without affecting the base map layer or other domain layers.

**Why this priority**: Preserves architectural data integrity and clean separation of concerns. Prevents accidental destruction or pollution of base cartographic data and existing user layers.

**Independent Test**: Can be tested by creating several chat-drawn shapes, toggling the overlay layer's visibility off and on, adjusting opacity, and verifying that the base map continues to render independently.

**Acceptance Scenarios**:

1. **Given** active chat-generated objects, **When** the user inspects the layer hierarchy, **Then** the chat sketch layer appears as a distinct, individually manageable layer sitting above the base map.
2. **Given** the chat sketch layer with active objects, **When** the user hides or clears the layer, **Then** only the chat-sketched objects disappear or clear, leaving base map tiles and other layers intact.
3. **Given** a completed sketch session, **When** the user exports the project or layer, **Then** the chat sketch layer is exported as standard geospatial vector data with complete semantic attributes.

---

### User Story 4 - Local AI Connectivity and Offline Resilience (Priority: P3)

The chat assistant relies on a locally hosted AI runtime. If the local runtime is offline, starting up, or temporarily unresponsive, the chat interface provides clear, non-intrusive status indicators, actionable reconnection options, and fallback mechanisms for manual drawing.

**Why this priority**: Ensures robust reliability and graceful degradation when running on workstations with local language model dependencies.

**Independent Test**: Can be tested by stopping or pausing the local model service, entering a chat prompt, and verifying that the user receives an informative status alert with retry controls rather than an unhandled application freeze.

**Acceptance Scenarios**:

1. **Given** the local AI service is unavailable or disconnected, **When** the user opens the chat panel or submits an input, **Then** the system displays a friendly service status warning with a retry button.
2. **Given** a slow or heavy local inference task, **When** generation is underway, **Then** the chat interface displays a responsive progress indicator and allows the user to cancel the operation.
3. **Given** a restored local AI service connection, **When** the user retries a prompt, **Then** inference resumes normally without requiring a full application restart.

---

### Edge Cases

- **Service Disconnection Mid-Turn**: If the local AI service terminates or times out while processing a spatial prompt, the chat interface preserves the user's input in the compose box, displays a clear timeout notification, and leaves the existing overlay layer unaffected.
- **Geographic Coordinate Hallucination**: If the model suggests coordinates outside the valid geographic bounds or vastly distant from the current map view area, the system detects the anomaly, flags the coordinate discrepancy in the chat response, and offers to zoom to the target area or re-center the coordinates within the visible viewport.
- **Complex or Self-Intersecting Polygons**: When generated geometry has invalid spatial topology (e.g., bowtie polygons or inverted rings), the system applies automatic vector repair or flags the shape for user vertex adjustment before committing to the layer.
- **Concurrent Vibe and Manual Edits**: If a user is manually moving vertices on the map while a chat response returns a new object, the new object is staged non-destructively so it does not overwrite the user's active manual edits.
- **Empty or Inaudible Vibe Input**: When an existing vibe record contains no recognizable speech or empty text, the chat interface notifies the user and prompts for re-entry without dispatching empty prompts to the model.

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: System MUST provide an integrated conversational chat panel accessible adjacent to or docked on the map canvas.
- **FR-002**: System MUST allow users to transmit prompts to the chat assistant via direct text entry and via the existing vibe record input mechanism.
- **FR-003**: System MUST interface with a local language model service to interpret natural-language spatial instructions into structured geographical vector primitives (points, polylines, polygons, markers, and text annotations).
- **FR-004**: System MUST render all AI-synthesized graphical objects onto a dedicated, isolated overlay layer positioned on top of the base map layer.
- **FR-005**: System MUST maintain conversational session context across multiple turns to enable iterative references, modifications, relocations, or deletions of previously drawn objects.
- **FR-006**: System MUST present interactive confirmation affordances (accept, modify, discard) for newly generated graphical objects within the chat feed and canvas.
- **FR-007**: System MUST provide full layer lifecycle controls for the chat sketch overlay layer, including visibility toggle, opacity adjustment, layer renaming, clearing, and vector export.
- **FR-008**: System MUST display real-time connection status for the local model runtime and provide graceful error handling with retry capability when the service is unreachable.
- **FR-009**: System MUST validate all generated spatial coordinates against valid geographic coordinate systems and the active viewport context before rendering.
- **FR-010**: System MUST attach semantic metadata (e.g., category, prompt origin, timestamp, user notes) to every object created through the chat interface.

### Key Entities

- **Chat Conversation Session**: Represents the active sequence of user messages, vibe record references, and assistant responses, maintaining dialogue context for iterative sketching.
- **Vibe Record Input**: The audio transcript, voice intent capture, or text-vibe data structure originating from the existing vibe recording subsystem.
- **Chat Sketch Layer**: A dedicated geographic vector layer instance configured above the base map layer, managing the collection of AI-generated spatial entities.
- **Sketched Graphical Object**: A spatial entity residing on the overlay layer containing geometric data (GeoJSON-compatible point/line/polygon), visual symbology (color, stroke, fill), and semantic metadata attributes.
- **Local AI Provider State**: Represents the connectivity, model identifier, availability, and inference status of the local model runtime.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: Users can request and render a geographic object via chat or vibe input in under 6 seconds on standard local hardware.
- **SC-002**: At least 90% of basic spatial sketch requests (boundary perimeters, navigation paths, points of interest) produce geometrically valid vector primitives on the initial attempt.
- **SC-003**: 100% of chat-generated objects are isolated strictly to the dedicated overlay layer, guaranteeing zero unintended mutations or overwrites to the base map layer.
- **SC-004**: Users can successfully refine or alter an existing sketched object (color, shape, position) via conversational follow-up in at least 85% of multi-turn scenarios without starting over.
- **SC-005**: In the event of local model unavailability, 100% of failures are reported cleanly with informative guidance and zero unhandled application crashes or canvas freezes.

## Assumptions

- A local model execution environment (e.g., Ollama) is installed on the host machine and accessible via local network protocols.
- The application backend leverages Spring AI integration patterns for abstracting model interaction, prompt templating, and structured response parsing.
- The existing vibe record input produces accessible text transcripts or structured intent objects that can be directly passed into the chat interface.
- Standard geographic coordinate systems (e.g., WGS 84 / EPSG:4326 or Web Mercator / EPSG:3857) are utilized across the map canvas and overlay layer.
- Hardware specifications of the host machine satisfy minimum operational requirements for running local LLM inference concurrently with the interactive map UI.
