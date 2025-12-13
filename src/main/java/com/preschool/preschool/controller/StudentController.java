package com.preschool.preschool.controller;

import java.util.HashMap;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ResponseBody;

import com.preschool.preschool.entity.Student;
import com.preschool.preschool.service.SchoolService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/student")
public class StudentController {

    @Autowired
    private SchoolService schoolService;

    @GetMapping("/dashboard")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> dashboard(Authentication authentication) {
        String username = authentication.getName();
        Student student = schoolService.getStudentByUsername(username);
        Map<String, Object> response = new HashMap<>();
        if (student == null) {
            response.put("error", "StudentNotFound");
            return ResponseEntity.status(404).body(response);
        }
        response.put("student", student);
        response.put("announcements", schoolService.getAnnouncementsByClass(student.getClassName()));
        return ResponseEntity.ok(response);
    }

    @GetMapping("/activityPage")
    public String activityPage(Model model, Authentication authentication) {
        String username = authentication.getName();
        Student student = schoolService.getStudentByUsername(username);
        model.addAttribute("student", student);
        model.addAttribute("activities", schoolService.getActivitiesForClass(student.getClassName()));
        return "student/activity";
    }

    @GetMapping("/getActivity")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getActivity(Authentication authentication) {
        String username = authentication.getName();
        Student student = schoolService.getStudentByUsername(username);
        Map<String, Object> response = new HashMap<>();
        if (student == null) {
            response.put("error", "StudentNotFound");
            return ResponseEntity.status(404).body(response);
        }
        response.put("student", student);
        response.put("activities", schoolService.getActivitiesForClass(student.getClassName()));
        return ResponseEntity.ok(response);
    }

    @GetMapping("/attendancePage")
    public String attendancePage(Model model, Authentication authentication) {
        String username = authentication.getName();
        Student student = schoolService.getStudentByUsername(username);
        model.addAttribute("student", student);
        model.addAttribute("attendanceList", schoolService.getAttendanceForStudent(student.getId()));
        return "student/attendance";
    }

    @GetMapping("/getAttendance")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getAttendance(Authentication authentication) {
        String username = authentication.getName();
        Student student = schoolService.getStudentByUsername(username);
        Map<String, Object> response = new HashMap<>();
        if (student == null) {
            response.put("error", "StudentNotFound");
            return ResponseEntity.status(404).body(response);
        }
        response.put("student", student);
        response.put("attendanceList", schoolService.getAttendanceForStudent(student.getId()));
        return ResponseEntity.ok(response);
    }

    @GetMapping("/homeworkPage")
    public String homeworkPage(Model model, Authentication authentication) {
        String username = authentication.getName();
        Student student = schoolService.getStudentByUsername(username);
        model.addAttribute("student", student);
        model.addAttribute("homeworkList", schoolService.getHomeworkForClass(student.getClassName()));
        return "student/homework";
    }

    @GetMapping("/feesPage")
    public String feesPage(Model model, Authentication authentication) {
        String username = authentication.getName();
        Student student = schoolService.getStudentByUsername(username);
        model.addAttribute("student", student);

        // Calculate Fee Status
        Double yearlyFee = 0.0;
        if (student.getClassName() != null) {
            com.preschool.preschool.entity.ClassFee cf = schoolService.getClassFee(student.getClassName());
            if (cf != null && cf.getYearlyFee() != null) {
                yearlyFee = cf.getYearlyFee();
            }
        }
        model.addAttribute("yearlyFee", yearlyFee);

        java.util.List<com.preschool.preschool.entity.FeePayment> feePayments = schoolService
                .getFeePaymentsForStudent(student.getId());
        Double totalPaid = feePayments.stream()
                .filter(p -> p.getAmount() != null)
                .mapToDouble(com.preschool.preschool.entity.FeePayment::getAmount)
                .sum();
        model.addAttribute("totalPaid", totalPaid);
        model.addAttribute("pendingFee", yearlyFee - totalPaid);

        model.addAttribute("feePayments", feePayments);
        return "student/fees";
    }

    @GetMapping("/profilePage")
    public String profilePage(Model model, Authentication authentication) {
        String username = authentication.getName();
        Student student = schoolService.getStudentByUsername(username);
        model.addAttribute("student", student);
        return "student/profile";
    }

    @Autowired
    private org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

    @org.springframework.web.bind.annotation.PostMapping("/updateProfile")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> updateProfile(
            @org.springframework.web.bind.annotation.ModelAttribute Student studentForm,
            @org.springframework.web.bind.annotation.RequestParam("profileImage") org.springframework.web.multipart.MultipartFile file,
            @org.springframework.web.bind.annotation.RequestParam(required = false) String newPassword,
            @org.springframework.web.bind.annotation.RequestParam("name") String name,
            @org.springframework.web.bind.annotation.RequestParam("rollNo") String rollNo,
            @org.springframework.web.bind.annotation.RequestParam("username") String username) {

        Map<String, Object> response = new HashMap<>();
        Authentication auth = org.springframework.security.core.context.SecurityContextHolder.getContext()
                .getAuthentication();
        String currentUsername = auth.getName();
        Student existingStudent = schoolService.getStudentByUsername(currentUsername);

        if (existingStudent != null) {
            existingStudent.setName(name);

            // Update Roll No if changed
            if (!rollNo.equals(existingStudent.getRollNo())) {
                if (schoolService.getStudentByUsername(rollNo) != null && !rollNo.equals(existingStudent.getRollNo())) {
                    // Check logic... actually if another user exists with this rollNo (as username)
                }
                existingStudent.setRollNo(rollNo);
            }

            // Update Username if changed
            // Update Username if changed
            String normalizedUsername = username.trim().toLowerCase();
            boolean usernameChanged = !normalizedUsername.equals(currentUsername);
            if (usernameChanged) {
                if (schoolService.getStudentByUsername(normalizedUsername) != null) {
                    response.put("status", "error");
                    response.put("message", "Username already exists");
                    return ResponseEntity.ok(response);
                }
                existingStudent.getUser().setUsername(normalizedUsername);
            }

            // Handle File Upload
            if (!file.isEmpty()) {
                try {
                    String uploadDir = System.getProperty("user.dir") + "/uploads/";
                    java.io.File directory = new java.io.File(uploadDir);
                    if (!directory.exists()) {
                        directory.mkdirs();
                    }

                    String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
                    java.nio.file.Path filePath = java.nio.file.Paths.get(uploadDir + fileName);
                    java.nio.file.Files.write(filePath, file.getBytes());

                    existingStudent.setPhoto("/uploads/" + fileName);
                } catch (java.io.IOException e) {
                    e.printStackTrace();
                    response.put("status", "error");
                    response.put("message", "Failed to upload image: " + e.getMessage());
                    return ResponseEntity.ok(response);
                }
            }

            // Update Personal Details
            existingStudent.setMotherName(studentForm.getMotherName());
            existingStudent.setMotherPhoneNumber(studentForm.getMotherPhoneNumber());
            existingStudent.setAddress(studentForm.getAddress());

            // Update Password
            if (newPassword != null && !newPassword.trim().isEmpty()) {
                com.preschool.preschool.entity.User user = existingStudent.getUser();
                user.setPassword(passwordEncoder.encode(newPassword));
            }

            schoolService.updateStudent(existingStudent);

            if (usernameChanged) {
                response.put("status", "success");
                response.put("message", "logout");
                return ResponseEntity.ok(response);
            }

            response.put("status", "success");
            response.put("message", "Profile updated successfully");
            return ResponseEntity.ok(response);
        } else {
            response.put("status", "error");
            response.put("message", "Student not found");
            return ResponseEntity.status(404).body(response);
        }
    }

    @GetMapping("/getProfile")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getProfile(Authentication authentication) {
        String username = authentication.getName();
        Student student = schoolService.getStudentByUsername(username);
        Map<String, Object> response = new HashMap<>();
        if (student == null) {
            response.put("error", "StudentNotFound");
            return ResponseEntity.status(404).body(response);
        }
        response.put("student", student);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/getFees")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getFees(Authentication authentication) {
        String username = authentication.getName();
        Student student = schoolService.getStudentByUsername(username);
        Map<String, Object> response = new HashMap<>();
        if (student == null) {
            response.put("error", "StudentNotFound");
            return ResponseEntity.status(404).body(response);
        }
        response.put("student", student);

        // Calculate Fee Status
        Double yearlyFee = 0.0;
        if (student.getClassName() != null) {
            com.preschool.preschool.entity.ClassFee cf = schoolService.getClassFee(student.getClassName());
            if (cf != null && cf.getYearlyFee() != null) {
                yearlyFee = cf.getYearlyFee();
            }
        }
        response.put("yearlyFee", yearlyFee);

        java.util.List<com.preschool.preschool.entity.FeePayment> feePayments = schoolService
                .getFeePaymentsForStudent(student.getId());
        Double totalPaid = feePayments.stream()
                .filter(p -> p.getAmount() != null)
                .mapToDouble(com.preschool.preschool.entity.FeePayment::getAmount)
                .sum();
        response.put("totalPaid", totalPaid);
        response.put("pendingFee", yearlyFee - totalPaid);

        response.put("feePayments", feePayments);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/getHomework")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getHomework(Authentication authentication) {
        String username = authentication.getName();
        Student student = schoolService.getStudentByUsername(username);
        Map<String, Object> response = new HashMap<>();
        if (student == null) {
            response.put("error", "StudentNotFound");
            return ResponseEntity.status(404).body(response);
        }
        response.put("student", student);
        response.put("homeworkList", schoolService.getHomeworkForClass(student.getClassName()));
        return ResponseEntity.ok(response);
    }
}
