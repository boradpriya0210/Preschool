package com.preschool.preschool.config;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import java.io.IOException;
import java.util.Collection;

@Component
public class CustomAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
            Authentication authentication) throws IOException, ServletException {

        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType("application/json");

        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        String role = "";
        String redirectUrl = "";

        for (GrantedAuthority authority : authorities) {
            if (authority.getAuthority().equals("ROLE_TEACHER")) {
                role = "TEACHER";
                redirectUrl = "/teacher/dashboard.html";
                break;
            } else if (authority.getAuthority().equals("ROLE_STUDENT")) {
                role = "STUDENT";
                redirectUrl = "/student/dashboard.html";
                break;
            } else if (authority.getAuthority().equals("ROLE_ADMIN")) {
                role = "ADMIN";
                redirectUrl = "/admin/dashboard.html";
                break;
            }
        }

        response.getWriter()
                .write("{\"status\":\"success\", \"role\":\"" + role + "\", \"redirectUrl\":\"" + redirectUrl + "\"}");
        response.getWriter().flush();
    }
}
