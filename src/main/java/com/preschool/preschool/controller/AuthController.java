package com.preschool.preschool.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;

import java.util.HashMap;
import java.util.Map;

@RestController
public class AuthController {

    @Autowired
    private com.preschool.preschool.service.SchoolService schoolService;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private com.preschool.preschool.security.JwtUtil jwtUtil;

    @Autowired
    private com.preschool.preschool.service.CustomUserDetailsService userDetailsService;

    @GetMapping("/")
    public ResponseEntity<String> home() {
        return ResponseEntity.ok("Welcome to Preschool API");
    }

    // REST API Login endpoint for fetch requests
    @PostMapping("/api/login")
    public ResponseEntity<Map<String, Object>> apiLogin(
            @RequestParam String username,
            @RequestParam String password) {

        Map<String, Object> response = new HashMap<>();

        try {
            System.out.println("API Login attempt for user: " + username);

            // Authenticate user
            String normalizedUsername = username.trim().toLowerCase();
            UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(normalizedUsername,
                    password);

            Authentication authentication = authenticationManager.authenticate(authToken);

            // Set authentication in context (optional for stateless, but good practice)
            SecurityContextHolder.getContext().setAuthentication(authentication);

            // Generate JWT Token
            org.springframework.security.core.userdetails.UserDetails userDetails = userDetailsService
                    .loadUserByUsername(normalizedUsername);
            String token = jwtUtil.generateToken(userDetails);

            // Determine user role and return response
            String role = authentication.getAuthorities().stream()
                    .map(auth -> auth.getAuthority())
                    .findFirst()
                    .orElse("ROLE_USER");

            response.put("status", "success");
            response.put("message", "Login successful");
            response.put("token", token);
            response.put("role", role);
            response.put("username", username);

            if (role.contains("TEACHER")) {
                response.put("redirectUrl", "teacher/dashboard.html");
            } else if (role.contains("STUDENT")) {
                response.put("redirectUrl", "student/dashboard.html");
            } else {
                response.put("redirectUrl", "dashboard.html");
            }

            return ResponseEntity.ok(response);

        } catch (AuthenticationException e) {
            System.err.println("Login failed: " + e.getMessage());
            response.put("status", "error");
            response.put("message", "Invalid username or password");
            return ResponseEntity.status(401).body(response);
        } catch (Exception e) {
            System.err.println("Unexpected error: " + e.getMessage());
            e.printStackTrace();
            response.put("status", "error");
            response.put("message", "Server error: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    // Signup endpoint
    @PostMapping("/signup")
    public ResponseEntity<Map<String, Object>> registerUser(
            @RequestParam String name,
            @RequestParam String username,
            @RequestParam String password,
            @RequestParam String role,
            @RequestParam(required = false) String className,
            @RequestParam(required = false) String subject,
            @RequestParam(required = false) String teacherId,
            @RequestParam(required = false) String rollNo) {

        Map<String, Object> response = new HashMap<>();
        try {
            System.out.println("Signup request for user: " + username + ", role: " + role);

            // Normalize username
            String normalizedUsername = username.trim().toLowerCase();

            if ("STUDENT".equals(role)) {
                com.preschool.preschool.entity.Student student = new com.preschool.preschool.entity.Student();
                student.setName(name);
                student.setClassName(className);
                student.setRollNo(rollNo);
                schoolService.addStudent(student, normalizedUsername, password);
            } else if ("TEACHER".equals(role)) {
                com.preschool.preschool.entity.Teacher teacher = new com.preschool.preschool.entity.Teacher();
                teacher.setName(name);
                teacher.setSubject(subject);
                teacher.setTeacherId(teacherId);
                schoolService.addTeacher(teacher, normalizedUsername, password);
            }

            response.put("status", "success");
            response.put("message", "Signup successful");
            return ResponseEntity.ok(response);

        } catch (DataIntegrityViolationException e) {
            System.err.println("Signup failed: Duplicate entry - " + e.getMessage());
            response.put("status", "error");
            response.put("message", "Username already exists. Please choose a different username.");
            return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
        } catch (Exception e) {
            System.err.println("Signup failed: " + e.getMessage());
            e.printStackTrace();
            response.put("status", "error");
            response.put("message", "Signup failed: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
}
