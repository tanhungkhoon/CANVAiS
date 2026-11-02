package com.canvais.infrastructure.ai;

import com.canvais.api.dto.request.BoundingBoxDto;
import com.canvais.api.dto.request.GesturePointDto;
import com.canvais.api.dto.response.ProposedEntityDto;
import com.canvais.domain.model.EntityStyle;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Adapter fusing textual/spoken vibe intent with freehand gesture coordinates to synthesize candidate vector entities.
 */
@Component
public class SpatialReasoningAdapter {

    /**
     * Synthesizes candidate map entities based on spoken/typed vibe and drawing gesture points.
     *
     * @param textVibe natural language intent description
     * @param gestureStrokes freehand drawing points
     * @param bounds viewport bounding box
     * @return list of proposed vector entities
     */
    public List<ProposedEntityDto> synthesizeEntities(String textVibe,
                                                      List<GesturePointDto> gestureStrokes,
                                                      BoundingBoxDto bounds) {
        List<ProposedEntityDto> proposals = new ArrayList<>();
        String lowerVibe = textVibe != null ? textVibe.toLowerCase() : "";

        // If user gestured points, use them as geometric anchors
        if (gestureStrokes != null && gestureStrokes.size() >= 3 && (lowerVibe.contains("perimeter") || lowerVibe.contains("zone") || lowerVibe.contains("area") || lowerVibe.contains("boundary"))) {
            // Synthesize closed polygon boundary matching gesture footprint
            ProposedEntityDto polygonEntity = new ProposedEntityDto();
            polygonEntity.setName("Synthesized Perimeter");
            polygonEntity.setEntityType("PerimeterZone");

            List<List<Double>> ring = new ArrayList<>();
            for (GesturePointDto pt : gestureStrokes) {
                ring.add(List.of(pt.getLng(), pt.getLat()));
            }
            // Close ring if open
            if (!ring.isEmpty() && !ring.get(0).equals(ring.get(ring.size() - 1))) {
                ring.add(List.of(ring.get(0).get(0), ring.get(0).get(1)));
            }

            polygonEntity.setGeometry(Map.of("type", "Polygon", "coordinates", List.of(ring)));
            polygonEntity.setStyle(new EntityStyle("#E76F51", "#E76F5133", 2.5f, null, null));
            polygonEntity.setSemanticAttributes(Map.of("status", "ACTIVE", "synthesizedBy", "CANVAiS-Vibe"));
            polygonEntity.setConfidence(0.92);
            proposals.add(polygonEntity);
        } else if (gestureStrokes != null && gestureStrokes.size() >= 2 && (lowerVibe.contains("route") || lowerVibe.contains("path") || lowerVibe.contains("corridor") || lowerVibe.contains("line"))) {
            // Synthesize polyline corridor
            ProposedEntityDto lineEntity = new ProposedEntityDto();
            lineEntity.setName("Synthesized Corridor");
            lineEntity.setEntityType("TransitCorridor");

            List<List<Double>> coords = new ArrayList<>();
            for (GesturePointDto pt : gestureStrokes) {
                coords.add(List.of(pt.getLng(), pt.getLat()));
            }

            lineEntity.setGeometry(Map.of("type", "LineString", "coordinates", coords));
            lineEntity.setStyle(new EntityStyle("#2A9D8F", null, 3.0f, "4, 4", null));
            lineEntity.setSemanticAttributes(Map.of("clearance", "UNRESTRICTED", "synthesizedBy", "CANVAiS-Vibe"));
            lineEntity.setConfidence(0.88);
            proposals.add(lineEntity);
        } else {
            // Default anchor point / marker based on center of gesture or bounds
            double lat = 48.1800;
            double lng = 8.2000;
            if (gestureStrokes != null && !gestureStrokes.isEmpty()) {
                lat = gestureStrokes.get(0).getLat();
                lng = gestureStrokes.get(0).getLng();
            } else if (bounds != null) {
                lat = (bounds.getNorth() + bounds.getSouth()) / 2.0;
                lng = (bounds.getEast() + bounds.getWest()) / 2.0;
            }

            ProposedEntityDto pointEntity = new ProposedEntityDto();
            pointEntity.setName("Checkpoint / Marker");
            pointEntity.setEntityType("ObservationPoint");
            pointEntity.setGeometry(Map.of("type", "Point", "coordinates", List.of(lng, lat)));
            pointEntity.setStyle(new EntityStyle("#F4A261", "#F4A261", 2.0f, null, "pin"));
            pointEntity.setSemanticAttributes(Map.of("priority", "HIGH", "synthesizedBy", "CANVAiS-Vibe"));
            pointEntity.setConfidence(0.85);
            proposals.add(pointEntity);
        }

        return proposals;
    }
}
