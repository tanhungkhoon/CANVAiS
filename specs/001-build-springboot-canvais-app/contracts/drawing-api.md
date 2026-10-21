# REST API Contract: Geospatial Drawing Operations

**Service**: CANVAiS Geospatial Platform  
**Protocol**: REST / JSON  
**Base URL**: `/api/v1/geospatial`  
**Authentication**: Bearer Token (JWT) in Authorization header  

---

## Overview

This contract defines the RESTful interface for drawing and managing geospatial objects in the CANVAiS platform. The API accepts voice-transcribed commands, visual sketch data from frontend, and returns plotted objects with AI-generated semantics.

---

## Endpoints

### 1. Draw Geospatial Object (POST /draw)

**Purpose**: Submit drawing intent (voice transcription +/or sketch data) to create a new geospatial object.

#### Request

```http
POST /api/v1/geospatial/draw
Content-Type: application/json
Authorization: Bearer <JWT_TOKEN>
```

#### Request Body (Combined Input)

```json
{
  "voiceInput": {
    "text": string,                   // Transcribed voice command
    "confidence": float [0.0-1.0],    // NLP confidence score
    "keywords": ["building", "red"]   // Extracted keywords
  },
  "visualInput": {
    "strokes": [                       // Array of canvas point coordinates
      {"x": number, "y": number},
      {"x": number, "y": number}
    ],
    "boundingBox": {
      "x": number,
      "y": number,
      "width": number,
      "height": number
    }
  },
  "coordinates": {                     // Optional: geospatial point from voice context
    "latitude": float,
    "longitude": float
  }
}
```

#### Response (Success)

```http
HTTP/1.1 201 Created
Content-Type: application/json
{
  "objectId": "uuid-string",
  "status": "ACTIVE",
  "created": "2026-10-21T14:30:00Z"
}
```

#### Response (Validation Error)

```http
HTTP/1.1 400 Bad Request
Content-Type: application/json
{
  "error": "VALIDATION_ERROR",
  "message": "voiceInput and visualInput cannot both be empty",
  "field": "visualInput"
}
```

---

### 2. Draw Geospatial Object (POST /draw/voice-only)

**Purpose**: Create object using only voice transcription (visual input omitted).

#### Request Body (Voice Input Only)

```json
{
  "voiceInput": {
    "text": string,
    "confidence": float [0.0-1.0]
  }
}
```

#### Response (Success)

```http
HTTP/1.1 201 Created
Content-Type: application/json
{
  "objectId": "uuid-string",
  "status": "ACTIVE",
  "confidence": float [0.0-1.0]
}
```

---

### 3. Draw Geospatial Object (POST /draw/visual-only)

**Purpose**: Create object using only visual sketch (voice input omitted).

#### Request Body (Visual Input Only)

```json
{
  "visualInput": {
    "strokes": [
      {"x": number, "y": number},
      ...
    ],
    "boundingBox": {
      "x": number,
      "y": number,
      "width": number,
      "height": number
    }
  }
}
```

#### Response (Success)

```http
HTTP/1.1 201 Created
Content-Type: application/json
{
  "objectId": "uuid-string",
  "status": "ACTIVE",
  "confidence": float [0.0-1.0]
}
```

---

### 4. Get Objects for Map Layer (GET /layers/{layerId}/objects)

**Purpose**: Retrieve all geospatial objects within a specific map layer.

#### Request

```http
GET /api/v1/geospatial/layers/{layerId}/objects
Authorization: Bearer <JWT_TOKEN>
```

#### Response (Success)

```json
{
  "objects": [
    {
      "objectId": "uuid-string",
      "coordinates": {"latitude": float, "longitude": float},
      "objectType": string,
      "semantics": {
        "name": string,
        "tags": ["tag1", "tag2"],
        "attributes": {}
      },
      "createdAt": "2026-10-21T14:30:00Z"
    }
  ],
  "pagination": {
    "total": number,
    "page": number,
    "pageSize": number
  }
}
```

#### Response (Not Found)

```http
HTTP/1.1 404 Not Found
Content-Type: application/json
{
  "error": "NOT_FOUND",
  "message": "Layer with ID not found"
}
```

---

### 5. Update Object Semantics (PUT /objects/{objectId}/semantics)

**Purpose**: Modify the semantic metadata of an existing geospatial object.

#### Request Body

```json
{
  "name": string,              // New name/identifier
  "tags": ["tag1"],           // Updated tags
  "attributes": {
    "color": "blue"            // Additional attribute
  }
}
```

#### Response (Success)

```http
HTTP/1.1 200 OK
Content-Type: application/json
{
  "objectId": "uuid-string",
  "updatedAt": "2026-10-21T14:35:00Z"
}
```

---

### 6. Delete Object (DELETE /objects/{objectId})

**Purpose**: Remove a geospatial object from the map layer.

#### Request

```http
DELETE /api/v1/geospatial/objects/{objectId}
Authorization: Bearer <JWT_TOKEN>
```

#### Response (Success)

```http
HTTP/1.1 204 No Content
```

---

### 7. Search Objects by Criteria (GET /search)

**Purpose**: Query objects by semantic properties (not implementation details).

#### Request Parameters

| Parameter | Type | Description |
|-----------|------|-------------|
| objectType | string | Filter by object category |
| tag | string | Filter by specific tag |
| lat_range_min | float | Latitude minimum |
| lat_range_max | float | Latitude maximum  
| lon_range_min | float | Longitude minimum  
| lon_range_max | float | Longitude maximum  

#### Request

```http
GET /api/v1/geospatial/search?objectType=building&lat_range_min=40.7&lat_range_max=40.8
Authorization: Bearer <JWT_TOKEN>
```

#### Response (Success)

```json
{
  "total": number,
  "offset": number,
  "limit": number,
  "results": [
    {
      "objectId": "uuid-string",
      "coordinates": {"latitude": float, "longitude": float},
      "objectType": string,
      "semantics": {
        "name": string,
        "tags": ["tag1"]
      }
    }
  ]
}
```

---

### 8. List Available Layers (GET /layers)

**Purpose**: Retrieve all configured map layers.

#### Request

```http
GET /api/v1/geospatial/layers
Authorization: Bearer <JWT_TOKEN>
```

#### Response (Success)

```json
{
  "layers": [
    {
      "layerId": "uuid-string",
      "layerName": string,
      "entityType": string,
      "visibilityRules": {
        "alwaysVisible": boolean
      },
      "objectCount": number
    }
  ]
}
```

---

## Error Responses

### Common Errors

| HTTP Status | Code | Description |
|-------------|------|-------------|
| 400 | VALIDATION_ERROR | Request parameters failed validation |
| 401 | AUTHENTICATION_ERROR | Invalid or missing authentication token |
| 403 | PERMISSION_DENIED | User lacks authorization for operation |
| 404 | NOT_FOUND | Requested resource does not exist |
| 500 | INTERNAL_ERROR | Server error, contact support |

---

## Contract Rules

### 1. Voice Input Requirements
- Transcription must be non-empty string
- confidence score between 0.0 and 1.0 (inclusive)
- keywords array may be empty but voiceInput object required

### 2. Visual Input Requirements  
- strokes array must contain at least one point if provided
- boundingBox with x, y, width, height must all be non-negative numbers
- stroke points must form valid polygon/closed path shape

### 3. Object Creation Validation
- Voice confidence < 0.5 requires manual review flag set (auto-rejected)
- Visual sketch duration < 1 second flagged for quality review
- Objects with combined input have higher minimum confidence threshold (0.6 vs 0.5)

### 4. Update/Delete Rules
- Users can update semantics of objects they created within last 24 hours
- Admin users can modify/delete any object regardless of creation time  
- Deletion is final; no soft-delete mechanism in this contract version

---

## Data Privacy & Security Notes

1. **Audio Processing**: Voice input transcripts are processed via external NLP service; no local audio storage on backend  
2. **User Authorization**: All API operations require valid Bearer JWT token  
3. **Object Metadata**: Semantic metadata is stored with object; no personal identifiers unless user explicitly adds them  

---

## Compliance with CANVAiS Principles

| Principle | Contract Alignment |
|-----------|-------------------|
| Context-Aware Layered Sketching | Objects assigned to layers via layerAssignment endpoint  
| Multi-Modal Intent Fusion | POST /draw accepts both voiceInput and visualInput parameters  
| Semantic Annotation | Every object response includes complete semantics object  
| Customizable Layer Construction | GET /layers endpoint reveals all available entityTypes for customization  

---

**Status**: DRAFT - Ready for Phase 1 implementation review  
**Last Updated**: 2026-10-21
