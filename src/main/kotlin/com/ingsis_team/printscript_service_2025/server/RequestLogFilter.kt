package com.ingsis_team.printscript_service_2025.server

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

@Component
@Order(2)
class RequestLogFilter : OncePerRequestFilter() {
    public override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain,
    ) {
        val uri = request.requestURI
        val method = request.method
        val prefix = "$method $uri"
        try {
            return filterChain.doFilter(request, response)
        } catch (e: Exception) {
            logger.error("Exception processing request", e)
            throw e
        } finally {
            val statusCode = response.status
            logger.info("$prefix - $statusCode")
        }
    }
}
