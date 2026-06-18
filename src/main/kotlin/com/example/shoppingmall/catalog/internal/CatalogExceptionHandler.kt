package com.example.shoppingmall.catalog.internal

import org.springframework.http.HttpStatus
import org.springframework.http.ProblemDetail
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

// Errors are RFC 9457 ProblemDetail. This maps entity guard failures
// (require(...)) to 400; ResponseStatusException (e.g. 404 on update) is
// rendered as ProblemDetail by Spring itself via spring.mvc.problemdetails.enabled.
@RestControllerAdvice
class CatalogExceptionHandler {
    @ExceptionHandler(IllegalArgumentException::class)
    fun handleIllegalArgument(e: IllegalArgumentException): ProblemDetail =
        ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, e.message ?: "invalid request")
}
