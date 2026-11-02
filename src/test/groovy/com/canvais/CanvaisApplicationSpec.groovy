package com.canvais

import org.springframework.boot.test.context.SpringBootTest

/**
 * Integration specification testing Spring Boot application context load.
 */
@SpringBootTest
class CanvaisApplicationSpec extends SpecificationBase {

    def "context loads and application class can be instantiated"() {
        expect:
        new CanvaisApplication() != null
    }
}
