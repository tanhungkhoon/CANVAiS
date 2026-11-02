package com.canvais.domain

import com.canvais.SpecificationBase
import com.canvais.domain.model.AttributeDefinition
import com.canvais.domain.model.DataType
import com.canvais.domain.model.EntityTypeDefinition
import com.canvais.domain.model.GeometryType
import com.canvais.domain.model.SemanticSchema
import com.canvais.domain.repository.SemanticSchemaRepository
import com.canvais.service.SemanticSchemaService

/**
 * Spock behavioral specification verifying SemanticSchema entity validation.
 */
class SemanticSchemaSpec extends SpecificationBase {

    SemanticSchemaRepository repository = Mock()
    SemanticSchemaService service

    def setup() {
        service = new SemanticSchemaService(repository)
    }

    def "should validate entity attributes against required schema definitions"() {
        given: "a schema requiring 'threatLevel' for 'HazardZone'"
        def schemaId = UUID.randomUUID()
        def schema = new SemanticSchema("HazardSchema", "DisasterRelief", "1.0.0")

        def attr = new AttributeDefinition("threatLevel", DataType.STRING, true, ["LOW", "HIGH"])
        def entityType = new EntityTypeDefinition("HazardZone", "Hazard Zone", [GeometryType.POLYGON])
        entityType.attributes = [attr]
        schema.entityTypes = [entityType]

        repository.findById(schemaId) >> Optional.of(schema)

        when: "validating attributes with required field present"
        service.validateEntityAttributes(schemaId, "HazardZone", [threatLevel: "HIGH", notes: "Volcano perimeter"])

        then: "no exception thrown"
        noExceptionThrown()

        when: "validating attributes missing required field"
        service.validateEntityAttributes(schemaId, "HazardZone", [notes: "Missing threat level"])

        then: "IllegalArgumentException is thrown"
        def ex = thrown(IllegalArgumentException)
        ex.message.contains("Missing required attribute 'threatLevel'")
    }

    def "should reject disallowed entity types"() {
        given: "a schema with only HazardZone"
        def schemaId = UUID.randomUUID()
        def schema = new SemanticSchema("HazardSchema", "DisasterRelief", "1.0.0")
        schema.entityTypes = [new EntityTypeDefinition("HazardZone", "Hazard Zone", [GeometryType.POLYGON])]
        repository.findById(schemaId) >> Optional.of(schema)

        when: "validating an unregistered entity type"
        service.validateEntityAttributes(schemaId, "UnknownType", [:])

        then: "exception thrown"
        thrown(IllegalArgumentException)
    }
}
