package io.github.buildblocks.apiresponse.filter;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;
import java.util.regex.Pattern;

@Order(Ordered.HIGHEST_PRECEDENCE)
public class RequestIdFilter extends OncePerRequestFilter {

    public static final String REQUEST_ID_HEADER = "X-Request-ID";

    public static final String REQUEST_ID_ATTRIBUTE =
            RequestIdFilter.class.getName() + ".REQUEST_ID";

    private static final Pattern VALID_REQUEST_ID =
            Pattern.compile("[a-zA-Z0-9._-]{1,64}");

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain)
            throws ServletException, IOException {

        String requestId = request.getHeader(REQUEST_ID_HEADER);

        if (requestId == null || !VALID_REQUEST_ID.matcher(requestId).matches()) {
            requestId = "req-" + UUID.randomUUID();
        }

        request.setAttribute(REQUEST_ID_ATTRIBUTE, requestId);
        response.setHeader(REQUEST_ID_HEADER, requestId);

        filterChain.doFilter(request, response);
    }
}