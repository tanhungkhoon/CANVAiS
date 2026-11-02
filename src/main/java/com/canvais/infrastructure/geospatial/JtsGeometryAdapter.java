package com.canvais.infrastructure.geospatial;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.LinearRing;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.geom.PrecisionModel;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Adapter encapsulating JTS Topology Suite operations and standard GeoJSON geometry transformations.
 */
@Component
public class JtsGeometryAdapter {

    private final GeometryFactory geometryFactory;

    /**
     * Initializes the JTS geometry factory with WGS84 (SRID 4326) precision model.
     */
    public JtsGeometryAdapter() {
        this.geometryFactory = new GeometryFactory(new PrecisionModel(), 4326);
    }

    /**
     * Creates a JTS Point from geographic latitude and longitude.
     *
     * @param lat latitude in degrees
     * @param lng longitude in degrees
     * @return initialized Point geometry
     */
    public Point createPoint(double lat, double lng) {
        return geometryFactory.createPoint(new Coordinate(lng, lat));
    }

    /**
     * Creates a JTS LineString from an ordered list of coordinates.
     *
     * @param coordinates coordinate sequence (lng, lat)
     * @return initialized LineString geometry
     */
    public LineString createLineString(List<Coordinate> coordinates) {
        if (coordinates == null || coordinates.size() < 2) {
            throw new IllegalArgumentException("LineString requires at least 2 coordinates");
        }
        return geometryFactory.createLineString(coordinates.toArray(new Coordinate[0]));
    }

    /**
     * Creates a closed JTS Polygon from an exterior ring coordinate list.
     *
     * @param coordinates exterior ring coordinates
     * @return initialized Polygon geometry
     */
    public Polygon createPolygon(List<Coordinate> coordinates) {
        if (coordinates == null || coordinates.size() < 3) {
            throw new IllegalArgumentException("Polygon requires at least 3 coordinates");
        }
        List<Coordinate> closed = new ArrayList<>(coordinates);
        if (!closed.get(0).equals2D(closed.get(closed.size() - 1))) {
            closed.add(new Coordinate(closed.get(0).x, closed.get(0).y));
        }
        if (closed.size() < 4) {
            throw new IllegalArgumentException("Polygon linear ring must close with at least 4 points");
        }
        LinearRing shell = geometryFactory.createLinearRing(closed.toArray(new Coordinate[0]));
        return geometryFactory.createPolygon(shell);
    }

    /**
     * Parses a GeoJSON geometry map representation into a JTS Geometry object.
     *
     * @param geoJson map representation of GeoJSON geometry
     * @return JTS Geometry
     */
    @SuppressWarnings("unchecked")
    public Geometry parseGeoJson(Map<String, Object> geoJson) {
        if (geoJson == null || !geoJson.containsKey("type") || !geoJson.containsKey("coordinates")) {
            throw new IllegalArgumentException("Invalid GeoJSON geometry payload");
        }
        String type = (String) geoJson.get("type");
        Object coordsObj = geoJson.get("coordinates");

        if ("Point".equalsIgnoreCase(type)) {
            List<Number> coords = (List<Number>) coordsObj;
            return geometryFactory.createPoint(new Coordinate(coords.get(0).doubleValue(), coords.get(1).doubleValue()));
        } else if ("LineString".equalsIgnoreCase(type)) {
            List<List<Number>> coords = (List<List<Number>>) coordsObj;
            List<Coordinate> jtsCoords = new ArrayList<>();
            for (List<Number> pt : coords) {
                jtsCoords.add(new Coordinate(pt.get(0).doubleValue(), pt.get(1).doubleValue()));
            }
            return createLineString(jtsCoords);
        } else if ("Polygon".equalsIgnoreCase(type)) {
            List<List<List<Number>>> rings = (List<List<List<Number>>>) coordsObj;
            List<Coordinate> jtsCoords = new ArrayList<>();
            for (List<Number> pt : rings.get(0)) {
                jtsCoords.add(new Coordinate(pt.get(0).doubleValue(), pt.get(1).doubleValue()));
            }
            return createPolygon(jtsCoords);
        }
        throw new UnsupportedOperationException("Unsupported geometry type: " + type);
    }

    /**
     * Converts a JTS Geometry into a GeoJSON geometry map.
     *
     * @param geometry JTS geometry
     * @return GeoJSON map
     */
    public Map<String, Object> toGeoJsonMap(Geometry geometry) {
        if (geometry == null) {
            return null;
        }
        if (geometry instanceof Point pt) {
            return Map.of("type", "Point", "coordinates", List.of(pt.getX(), pt.getY()));
        } else if (geometry instanceof LineString ls) {
            List<List<Double>> coords = new ArrayList<>();
            for (Coordinate c : ls.getCoordinates()) {
                coords.add(List.of(c.x, c.y));
            }
            return Map.of("type", "LineString", "coordinates", coords);
        } else if (geometry instanceof Polygon poly) {
            List<List<Double>> ring = new ArrayList<>();
            for (Coordinate c : poly.getExteriorRing().getCoordinates()) {
                ring.add(List.of(c.x, c.y));
            }
            return Map.of("type", "Polygon", "coordinates", List.of(ring));
        }
        return Map.of("type", geometry.getGeometryType(), "coordinates", List.of());
    }

    /**
     * Retrieves the internal GeometryFactory instance.
     *
     * @return GeometryFactory
     */
    public GeometryFactory getGeometryFactory() {
        return geometryFactory;
    }
}
