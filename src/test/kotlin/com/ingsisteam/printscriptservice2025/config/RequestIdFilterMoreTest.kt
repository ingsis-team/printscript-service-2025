package com.ingsisteam.printscriptservice2025.config

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.ArgumentCaptor
import org.mockito.Mockito.eq
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.slf4j.MDC

class RequestIdFilterMoreTest {

    private lateinit var filter: RequestIdFilter
    private lateinit var request: HttpServletRequest
    private lateinit var response: HttpServletResponse
    private lateinit var chain: FilterChain

    @BeforeEach
    fun setup() {
        filter = RequestIdFilter()
        request = mock(HttpServletRequest::class.java)
        response = mock(HttpServletResponse::class.java)
        chain = mock(FilterChain::class.java)
        MDC.clear()
    }

    @Test
    fun `doFilter should generate request id when header missing and clear MDC`() {
        `when`(request.getHeader(RequestIdFilter.REQUEST_ID_HEADER)).thenReturn(null)
        `when`(request.method).thenReturn("POST")
        `when`(request.requestURI).thenReturn("/endpoint")
        `when`(request.remoteAddr).thenReturn("0.0.0.0")
        `when`(response.status).thenReturn(204)

        val headerCaptor = ArgumentCaptor.forClass(String::class.java)

        filter.doFilter(request, response, chain)

        verify(response).setHeader(eq(RequestIdFilter.REQUEST_ID_HEADER), headerCaptor.capture())
        val generated = headerCaptor.value
        assertNotNull(generated)
        assertTrue(generated.length >= 8) // substring(0, 8)

        verify(chain).doFilter(request, response)
        // MDC must be cleared at the end
        assertTrue(MDC.get(RequestIdFilter.REQUEST_ID_MDC_KEY) == null)
    }
}
