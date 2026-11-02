package com.canvais.infrastructure.geospatial;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Utility for constructing standard GeoJSON Feature and FeatureCollection payloads.
 */
@Component
public class GeoJsonConverter {

    /**
     * Constructs a single GeoJSON Feature map from an ID, geometry map, and properties.
     *
     * @param id feature identifier
     * @param geometryMap GeoJSON geometry map
     * @param properties feature attributes and metadata
     * @return GeoJSON Feature map
     */
    public Map<String, Object> toFeature(String id, Map<String, Object> geometryMap, Map<String, Object> properties) {
        Map<String, Object> feature = new HashMap<>();
        feature.put("type", "Feature");
        feature.put("id", id);
        feature.put("geometry", geometryMap);
        feature.put("properties", properties != null ? properties : new HashMap<>());
        return feature;
    }

    /**
     * Constructs a GeoJSON FeatureCollection from a list of Feature maps.
     *
     * @param features list of GeoJSON features
     * @return GeoJSON FeatureCollection map
     */
    public Map<String, Object> toFeatureCollection(List<Map<String, Object>> features) {
        Map<String, Object> collection = new HashMap<>();
        collection.put("type", "FeatureCollection");
        collection.put("features", features != null ? features : new ArrayList<>());
        return collection;
    }
}
