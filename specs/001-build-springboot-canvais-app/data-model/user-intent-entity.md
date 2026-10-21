# Entity: UserIntent

**Purpose**: Represents the AI interpretation of user input (voice and/or visual sketch) that creates a GeospatialObject.

---

## Attributes

### intentId (required)
- **Type**: String/UUID  
- **Description**: Unique identifier for the user intent record  
- **Constraints**: Auto-generated, globally unique  
- **Validation**: UUID format required  

### voiceInput (required)
- **Type**: AudioTranscript Object  
- **Description**: Natural language description from voice command  
- **Structure**: See below  

**AudioTranscript Structure**:
```json
{
  "rawText": string,                      // Full transcript
  "confidence": float [0.0-1.0],          // Transcription accuracy confidence
  "keywords": [string],                   // Extracted semantic keywords
  "coordinatesMentioned": [point]         // Georeference locations mentioned
}
```

### visualInput (required)
- **Type**: StrokeData Object  
- **Description**: Canvas stroke data from freehand drawing  
- **Structure**: See below  

**StrokeData Structure**:
```json
{
  "strokes": [                             // Array of stroke points
    {
      "pointX": number,
      "pointY": number
    }
  ],
  "startTime": timestamp,                  // Drawing start time
  "duration": number,                      // Total drawing duration (ms)
  "boundingBox": {                         // Enclosing box for shape detection
    "x": number,
    "y": number,
    "width": number,
    "height": number
  }
}
```

### interpretationResult (required)
- **Type**: GeospatialObject Object  
- **Description**: AI-generated object definition from combined input  
- **Constraints**: Must have valid confidence score >= 0.5  
- **Validation**: Full object structure complete  

### sourceType (required)
- **Type**: String (enumeration)  
- **Values**: "voice_only", "visual_only", "voice_visual_combined"  
- **Description**: Primary or combined input modality used  
- **Constraints**: Enumerated value required  

### createdAt (required)
- **Type**: Timestamp (ISO 8601)  
- **Description**: Intent creation timestamp  
- **Validation**: Required field  

---

## Relationships

| From | Relationship To | Cardinality | Description |
|------|-----------------|-------------|-------------|
| UserIntent | GeospatialObject | One-to-One | Each intent creates exactly one object (one-shot mapping)  
| UserIntent | AudioTranscript | One-to-One | Voice data included in intent record  
| UserIntent | StrokeData | One-to-One | Visual sketch data included in intent record  

---

## State Transitions

```
┌───────────┐     user action    ┌───────────┐
│  DRAFT   │ ───────────────────>│ PENDING   │
└───────────┘                    └───────────┘
     │                                      │
     │ AI processing complete               │ AI processing complete
     ▼                                      ▼
┌───────────┐                    ┌───────────┐
│ CONFIRMED │ ◄────────────────── │ PENDING   │
└───────────┘                     └───────────┘
     │                                      │
     │ object created, user approves        │ object created (auto-confirm)
     ▼                                      ▼
┌───────────┐                    ┌───────────┐
│  ACTIVE   │ ◄────────────────── │ PENDING   │
└───────────┘                     └───────────┘
```

**States**:
- `DRAFT`: Intent data captured but not yet processed  
- `PENDING`: AI/external services processing intent (STT, object detection)  
- `CONFIRMED`: User approved object creation or system auto-approved  
- `ACTIVE`: Object exists in system with complete semantics  

---

## Validation Rules

### Required Fields Checklist
- [x] intentId - auto-generated UUID
- [x] voiceInput - complete transcript with metadata
- [x] visualInput - stroke data with bounding box
- [x] interpretationResult - full GeospatialObject
- [x] sourceType - input modality classification  
- [x] createdAt - ISO 8601 timestamp

### Business Rules
- confidence score below 0.5 requires manual review
- Combined voice+visual inputs must have both components available
- Voice coordinates mentioned should match object coordinates within tolerance

---

## Example JSON Representation

```json
{
  "intentId": "intent-uuid-67890",
  "voiceInput": {
    "rawText": "Draw a red building at this location",
    "confidence": 0.94,
    "keywords": ["building", "red"],
    "coordinatesMentioned": []
  },
  "visualInput": {
    "strokes": [
      {"pointX": 100, "pointY": 200},
      {"pointX": 400, "pointY": 200},
      {"pointX": 400, "pointY": 350},
      {"pointX": 100, "pointY": 350}
    ],
    "startTime": 1697184600000,
    "duration": 2500,
    "boundingBox": {
      "x": 100,
      "y": 200,
      "width": 300,
      "height": 150
    }
  },
  "interpretationResult": {
    "id": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
    "coordinates": {"latitude": 40.7128, "longitude": -74.0060},
    "objectType": "building",
    "semantics": {
      "name": "building",
      "category": "custom_structure",
      "tags": ["red"],
      "attributes": {"color": "#FF0000"},
      "source": "voice_visual_combined",
      "confidence_score": 0.91
    }
  },
  "sourceType": "voice_visual_combined",
  "createdAt": "2026-10-21T14:30:00Z"
}
```

---

## Key Observations from Feature Requirements

| Requirement | Entity Impact | Data Flow |
|-------------|---------------|-----------|
| Voice-to-Semantics Conversion (Req 001) | voiceInput → interpretationResult | Audio capture → STT API → keywords extracted |
| Visual Sketch Integration (Req 002) | visualInput → boundingBox, strokes | Canvas tracking → shape detection |
| Multi-Layer Placement (Req 003) | GeospatialObject.layerAssignment | InterpretationResult assigned to MapLayer.zIndex |
| Semantic Annotation (Req 004) | GeospatialObject.semantics | Full object structure populated from combined inputs |
