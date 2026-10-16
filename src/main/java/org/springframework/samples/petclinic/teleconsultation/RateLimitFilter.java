package org.springframework.samples.petclinic.teleconsultation;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class RateLimitFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(RateLimitFilter.class);
    private static final int MAX_REQUESTS_PER_MINUTE = 10;
    private static final String TELE_CONSULTATION_PATH = "/api/teleconsultations/bookings";

    private final Map<String, RequestCounter> requestCounts = new ConcurrentHashMap<>();
    private final ObjectMapper objectMapper;

    public RateLimitFilter(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    protected void doFilterInternal(
        @NonNull HttpServletRequest request,
        @NonNull HttpServletResponse response,
        @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        if (!shouldRateLimit(request)) {
            filterChain.doFilter(request, response);
            return;
        }

        String clientId = extractClientId(request);
        RequestCounter counter = requestCounts.computeIfAbsent(clientId, k -> new RequestCounter());

        if (counter.isRateLimitExceeded()) {
            handleRateLimitExceeded(response, request.getRequestURI());
            return;
        }

        counter.increment();
        filterChain.doFilter(request, response);
    }

    private boolean shouldRateLimit(HttpServletRequest request) {
        return "POST".equals(request.getMethod()) && request.getRequestURI().endsWith(TELE_CONSULTATION_PATH);
    }

    private String extractClientId(HttpServletRequest request) {
        String ip = request.getRemoteAddr();
        String userAgent = request.getHeader("User-Agent");
        return ip + ":" + (userAgent != null ? userAgent.hashCode() : "unknown");
    }

    private void handleRateLimitExceeded(HttpServletResponse response, String path) throws IOException {
        logger.warn("Rate limit exceeded for path: {}", path);

        ErrorResponse error = new ErrorResponse(
            HttpStatus.TOO_MANY_REQUESTS.value(),
            "Rate limit exceeded",
            "Maximum " + MAX_REQUESTS_PER_MINUTE + " requests per minute allowed",
            path
        );

        response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write(objectMapper.writeValueAsString(error));
    }

    private static class RequestCounter {
        private final AtomicInteger count = new AtomicInteger(0);
        private volatile long windowStart = System.currentTimeMillis();

        boolean isRateLimitExceeded() {
            cleanupIfNeeded();
            return count.get() >= MAX_REQUESTS_PER_MINUTE;
        }

        void increment() {
            cleanupIfNeeded();
            count.incrementAndGet();
        }

        private void cleanupIfNeeded() {
            long now = System.currentTimeMillis();
            long elapsed = now - windowStart;

            if (elapsed > 60000) {
                windowStart = now;
                count.set(0);
            }
        }
    }
}