package io.github.buildblocks.apiresponse.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class RequestIdFilterTest {

    private final RequestIdFilter filter = new RequestIdFilter();

    @Test
    void shouldGenerateRequestIdWhenHeaderIsMissing()
            throws ServletException, IOException {

        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain filterChain = mock(FilterChain.class);

        filter.doFilter(request, response, filterChain);

        String requestId = response.getHeader(
                RequestIdFilter.REQUEST_ID_HEADER
        );

        assertNotNull(requestId);
        assertTrue(requestId.startsWith("req-"));
        assertEquals(
                requestId,
                request.getAttribute(RequestIdFilter.REQUEST_ID_ATTRIBUTE)
        );

        verify(filterChain).doFilter(request, response);
    }

    @Test
    void shouldPreserveValidIncomingRequestId()
            throws ServletException, IOException {

        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain filterChain = mock(FilterChain.class);

        request.addHeader(
                RequestIdFilter.REQUEST_ID_HEADER,
                "client-request-123"
        );

        filter.doFilter(request, response, filterChain);

        assertEquals(
                "client-request-123",
                response.getHeader(RequestIdFilter.REQUEST_ID_HEADER)
        );

        verify(filterChain).doFilter(request, response);
    }

    @Test
    void shouldGenerateNewIdWhenIncomingRequestIdIsInvalid()
            throws ServletException, IOException {

        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain filterChain = mock(FilterChain.class);

        request.addHeader(
                RequestIdFilter.REQUEST_ID_HEADER,
                "invalid request id!"
        );

        filter.doFilter(request, response, filterChain);

        String requestId = response.getHeader(
                RequestIdFilter.REQUEST_ID_HEADER
        );

        assertNotNull(requestId);
        assertTrue(requestId.startsWith("req-"));
        assertTrue(!requestId.equals("invalid request id!"));

        verify(filterChain).doFilter(request, response);
    }

    @Test
    void shouldGenerateDifferentIdsForRequestsWithoutHeaders()
            throws ServletException, IOException {

        MockHttpServletRequest firstRequest = new MockHttpServletRequest();
        MockHttpServletResponse firstResponse = new MockHttpServletResponse();

        MockHttpServletRequest secondRequest = new MockHttpServletRequest();
        MockHttpServletResponse secondResponse = new MockHttpServletResponse();

        FilterChain firstChain = mock(FilterChain.class);
        FilterChain secondChain = mock(FilterChain.class);

        filter.doFilter(firstRequest, firstResponse, firstChain);
        filter.doFilter(secondRequest, secondResponse, secondChain);

        String firstId = firstResponse.getHeader(
                RequestIdFilter.REQUEST_ID_HEADER
        );

        String secondId = secondResponse.getHeader(
                RequestIdFilter.REQUEST_ID_HEADER
        );
        assertNotNull(firstId);
        assertNotNull(secondId);
        assertTrue(!firstId.equals(secondId));

        verify(firstChain).doFilter(firstRequest, firstResponse);
        verify(secondChain).doFilter(secondRequest, secondResponse);
    }



    @Test
void shouldReturnRequestIdInResponseHeader() throws Exception {
    MockHttpServletRequest request = new MockHttpServletRequest();
    MockHttpServletResponse response = new MockHttpServletResponse();

    FilterChain filterChain = Mockito.mock(FilterChain.class);

    filter.doFilter(request, response, filterChain);

    String requestId = response.getHeader(
            RequestIdFilter.REQUEST_ID_HEADER
    );

    assertNotNull(requestId);
    assertTrue(requestId.startsWith("req-"));
    assertEquals(
            requestId,
            request.getAttribute(RequestIdFilter.REQUEST_ID_ATTRIBUTE)
    );
}

}