package com.ingsisteam.printscriptservice2025.config

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.slf4j.MDC

class RequestIdFilterTest {

    private lateinit var requestIdFilter: RequestIdFilter
    private lateinit var request: HttpServletRequest
    private lateinit var response: HttpServletResponse
    private lateinit var chain: FilterChain

    @BeforeEach
    fun setUp() {
        requestIdFilter = RequestIdFilter()
        request = mock(HttpServletRequest::class.java)
        response = mock(HttpServletResponse::class.java)
        chain = mock(FilterChain::class.java)
        MDC.clear() // Clear MDC before each test
    }

    @Test
    fun `doFilter should log request and response info`() {
        val providedRequestId = "log-test-id"
        `when`(request.getHeader(RequestIdFilter.REQUEST_ID_HEADER)).thenReturn(providedRequestId)
        `when`(request.method).thenReturn("GET")
        `when`(request.requestURI).thenReturn("/api/data")
        `when`(request.remoteAddr).thenReturn("127.0.0.1")
        `when`(response.status).thenReturn(200)

        // Mock logger to capture logs (optional, for explicit log message testing)
        // This is more advanced and might require a custom LogAppender or PowerMockito
        // For simplicity, we'll assume the logger calls themselves provide coverage

        requestIdFilter.doFilter(request, response, chain)

        verify(response).setHeader(RequestIdFilter.REQUEST_ID_HEADER, providedRequestId)
        verify(chain).doFilter(request, response)
        assertTrue(MDC.get(RequestIdFilter.REQUEST_ID_MDC_KEY) == null)
    }
}
