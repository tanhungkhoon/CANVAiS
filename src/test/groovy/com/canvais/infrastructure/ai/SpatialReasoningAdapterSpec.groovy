package com.canvais.infrastructure.ai

import com.canvais.SpecificationBase
import com.canvais.api.dto.request.BoundingBoxDto
import com.canvais.api.dto.request.GesturePointDto

/**
 * Spock specification testing spatial reasoning synthesis from vibe + gesture strokes.
 */
class SpatialReasoningAdapterSpec extends SpecificationBase {

    SpatialReasoningAdapter adapter = new SpatialReasoningAdapter()

    def "should synthesize polygon perimeter when vibe specifies perimeter and gesture has >= 3 points"() {
        given: "vibe text and 4 gesture coordinates"
        String vibe = "Establish a secure perimeter zone"
        def strokes = [
                new GesturePointDto(48.180, 8.190, 100),
                new GesturePointDto(48.185, 8.190, 200),
                new GesturePointDto(48.185, 8.210, 300),
                new GesturePointDto(48.180, 8.210, 400)
        ]
        def bounds = new BoundingBoxDto(48.190, 48.170, 8.220, 8.180)

        when: "synthesizeEntities is called"
        def proposals = adapter.synthesizeEntities(vibe, strokes, bounds)

        then: "proposal contains a closed Polygon"
        proposals.size() == 1
        proposals[0].entityType == "PerimeterZone"
        proposals[0].geometry.type == "Polygon"
        proposals[0].geometry.coordinates[0].size() == 5 // closed ring
    }

    def "should synthesize line corridor when vibe mentions corridor or route"() {
        given: "vibe text and 3 gesture coordinates"
        String vibe = "Plot evacuation corridor"
        def strokes = [
                new GesturePointDto(48.180, 8.190, 100),
                new GesturePointDto(48.182, 8.200, 200),
                new GesturePointDto(48.185, 8.215, 300)
        ]

        when: "synthesizing"
        def proposals = adapter.synthesizeEntities(vibe, strokes, null)

        then: "proposal contains a LineString"
        proposals.size() == 1
        proposals[0].entityType == "TransitCorridor"
        proposals[0].geometry.type == "LineString"
    }
}
