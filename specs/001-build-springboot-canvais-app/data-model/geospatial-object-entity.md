# Entity: GeospatialObject

**Purpose**: Represents a user-drawn or AI-generated graphical object plotted on a geospatial map layer with semantic metadata.

---

## Attributes

### id (required)
- **Type**: String/UUID  
- **Description**: Unique identifier for the geospatial object  
- **Constraints**: Auto-generated, must be unique globally  
- **Validation**: UUID format required  

### coordinates (required)  
- **Type**: Point Object  
  - `latitude`: Float [0-180]  
  - `longitude`: Float [-180-180]  
- **Description**: Geographic position of the object on Earth's surface  
- **Constraints**: Within valid bounds for specified map projection  
- **Validation**: Coordinates must form a valid point  

### objectType (required)
- **Type**: String (enumeration)  
- **Values**: building, road, water_body, point_of_interest, vegetation, custom  
- **Description**: Classification category of the geospatial object  
- **Constraints**: Must match one of defined enumeration values  
- **Validation**: Enumerated value required  

### semantics (required)
- **Type**: SemanticMetadata Object  
- **Description**: Rich contextual metadata describing object properties and attributes  
- **Structure**: See below  
- **Validation**: Schema must conform to defined structure  

**SemanticMetadata Structure**:
```json
{
  "name": string,
  "category": string (enum),
  "tags": [string],
  "attributes": {
    "color": string (hex or named color),
    "size": number (float, meters),
    "orientation": number (degrees)
  },
  "source": string (e.g., "voice_input", "visual_sketch"),
  "confidence_score": float [0.0-1.0]
}
```

### layerAssignment (required)
- **Type**: LayerId Object  
  - `layerId`: String/UUID (reference to MapLayer)  
  - `visibilityRules`: Object (see MapLayer)  
- **Description**: Assignment of object to a specific map layer  
- **Constraints**: Object must exist in assigned layer's entityType set  
- **Validation**: Foreign key constraint with MapLayer table  

### createdAt (required)
- **Type**: Timestamp  
- **Description**: Creation timestamp for auditing and history tracking  
- **Constraints**: Must be ISO 8601 format, cannot be null  

---

## Relationships

| From | Relationship To | Cardinality | Description |
|------|-----------------|-------------|-------------|
| GeospatialObject | MapLayer | One-to-Many | Each object belongs to one layer; each layer can contain many objects |
| GeospatialObject | SemanticMetadata | One-to-One | Every object must have complete metadata (tags, attributes, confidence) |

---

## State Transitions

```
┌───────────┐     draw command     ┌───────────┐
│  DRAFT   │ ─────────────────────>│ PENDING   │
└───────────┘                      └───────────┘
     │                                      │
     │ processing complete                  │ processing complete
     ▼                                      ▼
┌───────────┐                      ┌───────────┐
│  ACTIVE   │ ◄──────────────────── │ PENDING   │
└───────────┘                      └───────────┘
```

**States**:
- `DRAFT`: Object created but not yet validated/plotted
- `PENDING`: Processing by AI/external services (e.g., speech-to-text, object detection)
- `ACTIVE`: Validated object placed on map with complete semantics  
- `DELETED`: User removed or deleted the object

---

## Validation Rules

### Required Fields Checklist
- [x] id - auto-generated UUID
- [x] coordinates - lat/lon point object
- [x] objectType - enumerated category
- [x] semantics - complete metadata structure
- [x] layerAssignment - valid MapLayer reference
- [x] createdAt - ISO 8601 timestamp

### Business Rules
- Object cannot be assigned to a layer that doesn't support its objectType
- Confidence score < 50% requires manual review before activation
- Coordinates must validate against map projection bounds

---

## Example JSON Representation

```json
{
  "id": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
  "coordinates": {
    "latitude": 40.7128,
    "longitude": -74.0060
  },
  "objectType": "building",
  "semantics": {
    "name": "Empire State Building",
    "category": "commercial_building",
    "tags": ["landmark", "skyscraper"],
    "attributes": {
      "color": "#FF0000",
      "size": 546,
      "orientation": 45.2
    },
    "source": "voice_input",
    "confidence_score": 0.92
  },
  "layerAssignment": {
    "layerId": "building-layer-uuid",
    "visibilityRules": {"alwaysVisible": true, "requiresAuth": false}
  },
  "createdAt": "2026-10-21T14:30:00Z"
}
```
