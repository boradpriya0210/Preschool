package com.preschool.preschool.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.preschool.preschool.exception.ErrorResponse;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
            AuthenticationException authException)
            throws IOException, ServletException {

        response.setContentType("application/json");
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

        // Include timestamp if not handled by ObjectMapper autoconfig, but our manual
        // one needs it
        // ErrorResponse constructor handles timestamp = now()
        ErrorResponse errorResponse = new ErrorResponse(
                HttpServletResponse.SC_UNAUTHORIZED,
                "Unauthorized",
                authException.getMessage(),
                request.getRequestURI());
        // Ensure timestamp is serialized correctly (Java 8 date/time module might be
        // needed for ObjectMapper)
        // Spring Boot's injected ObjectMapper usually has it. New ObjectMapper() might
        // not.
        // To be safe, let's use toString or rely on Jackson dependency being present.
        // Assuming jackson-datatype-jsr310 is on classpath (standard in Spring Boot).
        // If not, we might need to register module.
        // Safer: `objectMapper.findAndRegisterModules();`
        objectMapper.findAndRegisterModules();

        objectMapper.writeValue(response.getOutputStream(), errorResponse);
    }
}
