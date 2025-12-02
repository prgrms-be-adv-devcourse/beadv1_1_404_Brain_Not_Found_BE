package com.ll.core.tracing;

import com.fasterxml.uuid.Generators;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Component
public class CorrelationFilter implements Filter {

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest request = (HttpServletRequest) req;

        String correlationId = request.getHeader("X-Correlation-Id");

        if (correlationId == null || correlationId.isBlank()) {
            correlationId = "Internal_api-" + Generators.timeBasedGenerator().generate().toString();
        }

        CorrelationContext.set(correlationId);

        try {
            chain.doFilter(req, res);
        } finally {
            CorrelationContext.clear();
        }
    }
}