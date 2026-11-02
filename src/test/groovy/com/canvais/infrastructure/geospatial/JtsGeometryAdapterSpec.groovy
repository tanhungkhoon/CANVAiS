package com.canvais.infrastructure.geospatial

import com.canvais.SpecificationBase
import org.locationtech.jts.geom.Coordinate
import org.locationtech.jts.geom.LineString
import org.locationtech.jts.geom.Point
import org.locationtech.jts.geom.Polygon

/**
 * Unit specification verifying JTS Geometry construction and GeoJSON conversion.
 */
class JtsGeometryAdapterSpec extends SpecificationBase {

    JtsGeometryAdapter adapter

    def setup() {
        adapter = new JtsGeometryAdapter()
    }

    def "should create valid JTS Point and serialize to GeoJSON map"() {
        given: "latitude and longitude coordinates"
        double lat = 48.1800
        double lng = 8.2000

        when: "Point is created"
        Point point = adapter.createPoint(lat, lng)

        then: "coordinates match SRID 4326 standards"
        point.getSRID() == 4326
        isCloseTo(point.getY(), lat)
        isCloseTo(point.getX(), lng)

        when: "serialized to GeoJSON"
        Map<String, Object> geoJson = adapter.toGeoJsonMap(point)

        then: "map has Point type and matching coordinates"
        geoJson.type == "Point"
        geoJson.coordinates == [lng, lat]
    }

    def "should create valid JTS LineString and serialize to GeoJSON"() {
        given: "a list of coordinates"
        def coords = [
                new Coordinate(8.190, 48.180),
                new Coordinate(8.200, 48.182),
                new Coordinate(8.210, 48.185)
        ]

        when: "LineString is created"
        LineString line = adapter.createLineString(coords)

        then: "number of vertices matches"
        line.getNumPoints() == 3

        when: "serialized to GeoJSON"
        Map<String, Object> geoJson = adapter.toGeoJsonMap(line)

        then: "type is LineString"
        geoJson.type == "LineString"
        geoJson.coordinates.size() == 3
    }

    def "should create closed JTS Polygon and validate ring"() {
        given: "exterior ring vertices"
        def coords = [
                new Coordinate(8.190, 48.175),
                new Coordinate(8.210, 48.175),
                new Coordinate(8.210, 48.185),
                new Coordinate(8.190, 48.185)
        ]

        when: "Polygon is created"
        Polygon poly = adapter.createPolygon(coords)

        then: "polygon is valid and automatically closed"
        poly.isValid()
        poly.getExteriorRing().getNumPoints() == 5
    }

    def "should parse GeoJSON polygon map to JTS Polygon"() {
        given: "a GeoJSON Polygon map"
        Map<String, Object> geoJson = [
                type: "Polygon",
                coordinates: [[
                        [8.190, 48.175],
                        [8.210, 48.175],
                        [8.210, 48.185],
                        [8.190, 48.185],
                        [8.190, 48.175]
                ]]
        ]

        when: "parsed"
        def geom = adapter.parseGeoJson(geoJson)

        then: "JTS geometry is a valid Polygon"
        geom instanceof Polygon
        geom.isValid()
    }
}
