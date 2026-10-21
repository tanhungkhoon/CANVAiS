# Entity: MapLayer

**Purpose**: Represents a geospatial data layer for organizing and displaying objects on the CANVAiS drawing interface.

---

## Attributes

### layerId (required)
- **Type**: String/UUID  
- **Description**: Unique identifier for the map layer  
- **Constraints**: Auto-generated, globally unique  
- **Validation**: UUID format required  

### entityType (required)
- **Type**: String (enumeration)  
- **Values**: building, road, water_body, vegetation, point_of_interest, custom, mixed  
- **Description**: Type of geospatial entities represented in this layer  
- **Constraints**: Must match user-defined category or built-in enumeration  
- **Validation**: Enumerated value required  

### visibilityRules (required)
- **Type**: VisibilityRule Object  
- **Description**: Display and interaction policy for objects in this layer  
- **Structure**: See below  

**VisibilityRule Structure**:
```json
{
  "alwaysVisible": boolean,           // True = always shown to users
  "requiresAuth": boolean,            // True = shows only to authenticated users
  "layerPriority": number (int),      // Higher = shown above lower priority layers
  "filterCriteria": {                  // Optional advanced filters
    "categoryFilter": [string],
    "attributeConstraints": {object}
  }
}
```

### layerType (required)
- **Type**: LayerClassification Object  
- **Description**: Classification within the geospatial hierarchy  
- **Structure**: See below  

**LayerClassification Structure**:
```json
{
  "baseMap": boolean,                 // True = foundational tile provider layer
  "overlay": boolean,                 // True = additive features on base
  "custom": boolean                   // True = user-created layers
}
```

### zIndex (required)  
- **Type**: Number (integer)  
- **Description**: Drawing order on the screen  
- **Constraints**: Must be positive integer, must not conflict with existing layers
- **Validation**: Must satisfy `UNIQUE` constraint in system

### layerName (optional)
- **Type**: String  
- **Description**: Human-readable name for the layer  
- **Constraints**: Max 256 characters, non-empty for base layers  

### createdAt (required)
- **Type**: Timestamp (ISO 8601)  
- **Description**: Creation timestamp  
- **Validation**: Required field  

---

## Relationships

| From | Relationship To | Cardinality | Description |
|------|-----------------|-------------|-------------|
| MapLayer | GeospatialObject | One-to-Many | Each layer can contain many objects |
| MapLayer | VisibilityRule | One-to-One | Every layer has complete visibility policy  
| MapLayer | LayerClassification | One-to-One | Classification defines layer type  

---

## State Transitions

```
┌───────────┐     create request    ┌───────────┐
│ DRAFT    │ ───────────────────── >│ PENDING   │
└───────────┘                      └───────────┘
     │                                      │
     │ validation complete                │ validation complete
     ▼                                      ▼
┌───────────┐                      ┌───────────┐
│  ACTIVE   │ ◄──────────────────── │ PENDING   │
└───────────┘                      └───────────┘
```

**States**:
- `DRAFT`: Layer created but not fully configured  
- `PENDING`: Being processed/validated  
- `ACTIVE`: Fully configured and ready for use  

---

## Validation Rules

### Required Fields Checklist
- [x] layerId - auto-generated UUID
- [x] entityType - enumerated category
- [x] visibilityRules - complete rule structure
- [x] layerType - classification structure
- [x] zIndex - unique positive integer
- [x] createdAt - ISO 8601 timestamp

### Business Rules
- Layer cannot be created with conflicting zIndex (must be unique)
- entityType must support at least one predefined object type
- visibilityRules must have valid boolean fields

---

## Example JSON Representation

```json
{
  "layerId": "layer-uuid-12345",
  "entityType": "building",
  "visibilityRules": {
    "alwaysVisible": true,
    "requiresAuth": false,
    "layerPriority": 1,
    "filterCriteria": {}
  },
  "layerType": {
    "baseMap": false,
    "overlay": true,
    "custom": false
  },
  "zIndex": 5,
  "layerName": "Buildings Layer",
  "createdAt": "2026-10-21T14:30:00Z"
}
```
