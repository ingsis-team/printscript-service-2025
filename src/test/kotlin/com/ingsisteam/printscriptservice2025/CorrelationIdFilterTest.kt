package com.ingsisteam.printscriptservice2025
import com.ingsisteam.printscriptservice2025.server.CorrelationIdFilter
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.slf4j.MDC
import kotlin.test.assertNull

class CorrelationIdFilterTest {
    private lateinit var filter: CorrelationIdFilter
    private lateinit var request: HttpServletRequest
    private lateinit var response: HttpServletResponse
    private lateinit var filterChain: FilterChain

    @BeforeEach
    fun setup() {
        filter = CorrelationIdFilter()
        request = mock(HttpServletRequest::class.java)
        response = mock(HttpServletResponse::class.java)
        filterChain = mock(FilterChain::class.java)
    }

    @Test
    fun `should add correlation ID to MDC when header is not present`() {
        // Arrange
        `when`(request.getHeader(CorrelationIdFilter.CORRELATION_ID_HEADER)).thenReturn(null)

        // Act
        filter.doFilterInternal(request, response, filterChain)

        // Assert
        verify(filterChain).doFilter(request, response)
        assertNull(MDC.get(CorrelationIdFilter.CORRELATION_ID_KEY))
    }

    @Test
    fun `should use provided correlation ID from header`() {
        // Arrange
        val correlationId = "test-correlation-id"
        `when`(request.getHeader(CorrelationIdFilter.CORRELATION_ID_HEADER)).thenReturn(correlationId)

        // Act
        filter.doFilterInternal(request, response, filterChain)

        // Assert
        verify(filterChain).doFilter(request, response)
        assertNull(MDC.get(CorrelationIdFilter.CORRELATION_ID_KEY))
    }

    @Test
    fun `should remove correlation ID from MDC after processing`() {
        // Arrange
        `when`(request.getHeader(CorrelationIdFilter.CORRELATION_ID_HEADER)).thenReturn(null)

        // Act
        filter.doFilterInternal(request, response, filterChain)

        // Assert
        assertNull(MDC.get(CorrelationIdFilter.CORRELATION_ID_KEY))
        verify(filterChain).doFilter(request, response)
    }
}
