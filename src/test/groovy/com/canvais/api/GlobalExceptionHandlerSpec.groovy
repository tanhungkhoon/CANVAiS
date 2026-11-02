package com.canvais.api

import com.canvais.SpecificationBase
import com.canvais.api.exception.GlobalExceptionHandler
import org.springframework.core.MethodParameter
import org.springframework.http.HttpStatus
import org.springframework.validation.BeanPropertyBindingResult
import org.springframework.validation.FieldError
import org.springframework.web.bind.MethodArgumentNotValidException

/**
 * Specification verifying GlobalExceptionHandler mappings.
 */
class GlobalExceptionHandlerSpec extends SpecificationBase {

    GlobalExceptionHandler handler = new GlobalExceptionHandler()

    def "should map NoSuchElementException to 404"() {
        when:
        def resp = handler.handleNotFound(new NoSuchElementException("Item missing"))

        then:
        resp.statusCode == HttpStatus.NOT_FOUND
        resp.body.message == "Item missing"
    }

    def "should map IllegalArgumentException to 400"() {
        when:
        def resp = handler.handleIllegalArgument(new IllegalArgumentException("Bad input"))

        then:
        resp.statusCode == HttpStatus.BAD_REQUEST
        resp.body.message == "Bad input"
    }

    def "should map General Exception to 500"() {
        when:
        def resp = handler.handleGeneralException(new RuntimeException("Server error"))

        then:
        resp.statusCode == HttpStatus.INTERNAL_SERVER_ERROR
        resp.body.error == "Internal Server Error"
    }

    def "should map MethodArgumentNotValidException to 400 with field details"() {
        given:
        def target = new Object()
        def bindingResult = new BeanPropertyBindingResult(target, "testObject")
        bindingResult.addError(new FieldError("testObject", "title", "Title is required"))
        def method = this.getClass().getDeclaredMethods()[0]
        def param = new MethodParameter(method, -1)
        def ex = new MethodArgumentNotValidException(param, bindingResult)

        when:
        def resp = handler.handleValidationErrors(ex)

        then:
        resp.statusCode == HttpStatus.BAD_REQUEST
        resp.body.details.title == "Title is required"
    }
}
