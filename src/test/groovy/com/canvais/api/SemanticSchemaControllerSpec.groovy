package com.canvais.api

import com.canvais.SpecificationBase
import com.canvais.api.controller.SemanticSchemaController
import com.canvais.api.dto.request.CreateSemanticSchemaRequest
import com.canvais.api.dto.response.SemanticSchemaResponse
import com.canvais.domain.model.EntityTypeDefinition
import com.canvais.domain.model.GeometryType
import com.canvais.service.SemanticSchemaService
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.setup.MockMvcBuilders

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

/**
 * WebMvc unit test specification verifying SemanticSchemaController endpoints.
 */
class SemanticSchemaControllerSpec extends SpecificationBase {

    MockMvc mockMvc
    SemanticSchemaService schemaService = Mock()
    ObjectMapper objectMapper = new ObjectMapper()

    def setup() {
        def controller = new SemanticSchemaController(schemaService)
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build()
    }

    def "POST /api/v1/integrations/schemas should register new schema and return 201"() {
        given: "a valid schema request"
        def request = new CreateSemanticSchemaRequest(
                name: "WildfireOps",
                domainCategory: "DisasterResponse",
                version: "1.0.0",
                entityTypes: [new EntityTypeDefinition("FireLine", "Fire Line", [GeometryType.LINE_STRING])]
        )
        def schemaId = UUID.randomUUID()
        def response = new SemanticSchemaResponse(
                id: schemaId,
                name: "WildfireOps",
                domainCategory: "DisasterResponse",
                version: "1.0.0"
        )

        schemaService.createSchema(_ as CreateSemanticSchemaRequest) >> response

        when: "POST request is executed"
        def result = mockMvc.perform(post("/api/v1/integrations/schemas")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))

        then: "status is 201 Created and JSON matches"
        result.andExpect(status().isCreated())
                .andExpect(jsonPath('$.id').value(schemaId.toString()))
                .andExpect(jsonPath('$.name').value("WildfireOps"))
    }

    def "GET /api/v1/integrations/schemas should list schemas"() {
        given: "schema list in service"
        def schema1 = new SemanticSchemaResponse(id: UUID.randomUUID(), name: "UrbanPlanning")
        schemaService.listSchemas() >> [schema1]

        when: "GET request executed"
        def result = mockMvc.perform(get("/api/v1/integrations/schemas"))

        then: "status is 200 OK with elements"
        result.andExpect(status().isOk())
                .andExpect(jsonPath('$.length()').value(1))
                .andExpect(jsonPath('$[0].name').value("UrbanPlanning"))
    }
}
