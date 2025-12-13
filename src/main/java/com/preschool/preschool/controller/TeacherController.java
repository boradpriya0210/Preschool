package com.preschool.preschool.controller;

import java.util.HashMap;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.preschool.preschool.entity.Homework;
import com.preschool.preschool.entity.Student;
import com.preschool.preschool.entity.Teacher;
import com.preschool.preschool.entity.User;

import com.preschool.preschool.service.SchoolService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
// import org.springframework.ui.Model; // Removed unused import

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import com.preschool.preschool.entity.ClassFee;
import com.preschool.preschool.entity.FeePayment;
import com.preschool.preschool.dto.StudentFeeDTO;
import com.preschool.preschool.dto.RecentPaymentDTO;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/api/teacher")
public class TeacherController {

    private static final Logger logger = LoggerFactory.getLogger(TeacherController.class);

    @Autowired
    private SchoolService schoolService;

    @Autowired
    private org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

    @GetMapping("/dashboard")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> dashboard(Authentication authentication) {
        String username = authentication.getName();
        Teacher teacher = schoolService.getTeacherByUsername(username);
        Map<String, Object> response = new HashMap<>();
        if (teacher == null) {
            response.put("error", "TeacherNotFound");
            return ResponseEntity.status(404).body(response);
        }
        response.put("teacher", teacher);
        response.put("students", schoolService.getAllStudents());
        response.put("activities", schoolService.getAllActivities());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/activityPage")
    public ResponseEntity<Map<String, Object>> activityPage(Authentication authentication) {
        String username = authentication.getName();
        Teacher teacher = schoolService.getTeacherByUsername(username);
        Map<String, Object> response = new HashMap<>();
        response.put("teacher", teacher);
        response.put("activities", schoolService.getAllActivities());
        response.put("classNames", schoolService.getAllClassNames());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/attendancePage")
    public ResponseEntity<Map<String, Object>> attendancePage(Authentication authentication,
            @RequestParam(required = false) @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE) java.time.LocalDate date,
            @RequestParam(required = false) String className) {
        String username = authentication.getName();
        Teacher teacher = schoolService.getTeacherByUsername(username);
        Map<String, Object> response = new HashMap<>();
        response.put("teacher", teacher);

        // Populate class names for dropdown
        response.put("classNames", schoolService.getAllClassNames());

        if (className != null && !className.isEmpty()) {
            response.put("students", schoolService.getStudentsByClass(className));
            response.put("selectedClass", className);
        } else {
            response.put("students", java.util.Collections.emptyList());
        }

        java.time.LocalDate selectedDate = (date != null) ? date : java.time.LocalDate.now();
        response.put("selectedDate", selectedDate);

        java.util.List<com.preschool.preschool.entity.Attendance> attendanceList = schoolService
                .getAttendanceForDate(selectedDate);
        java.util.Map<Long, String> attendanceMap = new java.util.HashMap<>();
        for (com.preschool.preschool.entity.Attendance a : attendanceList) {
            attendanceMap.put(a.getStudent().getId(), a.isPresent() ? "Present" : "Absent");
        }
        response.put("attendanceMap", attendanceMap);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/markAttendance")
    @org.springframework.web.bind.annotation.ResponseBody
    public org.springframework.http.ResponseEntity<String> markAttendance(@RequestParam Long studentId,
            @RequestParam boolean present,
            @RequestParam @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE) java.time.LocalDate date) {
        com.preschool.preschool.entity.Attendance attendance = new com.preschool.preschool.entity.Attendance();
        attendance.setDate(date);
        attendance.setPresent(present);

        Student student = schoolService.getAllStudents().stream()
                .filter(s -> s.getId().equals(studentId))
                .findFirst()
                .orElse(null);

        if (student != null) {
            attendance.setStudent(student);
            schoolService.markAttendance(attendance);
            return org.springframework.http.ResponseEntity.ok("Attendance marked");
        } else {
            return org.springframework.http.ResponseEntity.badRequest().body("Student not found");
        }
    }

    @GetMapping("/marksPage")
    public ResponseEntity<Map<String, Object>> marksPage(Authentication authentication) {
        String username = authentication.getName();
        Teacher teacher = schoolService.getTeacherByUsername(username);
        Map<String, Object> response = new HashMap<>();
        response.put("teacher", teacher);
        response.put("students", schoolService.getAllStudents());
        response.put("examResults", schoolService.getAllExamResults());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/homeworkPage")
    public ResponseEntity<Map<String, Object>> homeworkPage(Authentication authentication) {
        String username = authentication.getName();
        Teacher teacher = schoolService.getTeacherByUsername(username);
        Map<String, Object> response = new HashMap<>();
        response.put("teacher", teacher);
        response.put("homeworkList", schoolService.getAllHomework());
        response.put("classNames", schoolService.getAllClassNames());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/getHomework")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getHomework(Authentication authentication) {
        String username = authentication.getName();
        Teacher teacher = schoolService.getTeacherByUsername(username);
        Map<String, Object> response = new HashMap<>();
        if (teacher == null) {
            response.put("error", "TeacherNotFound");
            return ResponseEntity.status(404).body(response);
        }
        response.put("teacher", teacher);
        response.put("homeworkList", schoolService.getAllHomework());
        response.put("classNames", schoolService.getAllClassNames());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/addStudentPage")
    public ResponseEntity<Map<String, Object>> addStudentPage(Authentication authentication) {
        String username = authentication.getName();
        Teacher teacher = schoolService.getTeacherByUsername(username);
        Map<String, Object> response = new HashMap<>();
        if (teacher == null) {
            response.put("error", "TeacherNotFound");
            return ResponseEntity.status(404).body(response);
        }
        response.put("teacher", teacher);
        response.put("students", schoolService.getAllStudents());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/addStudent")
    public ResponseEntity<Map<String, Object>> addStudent(@ModelAttribute Student student,
            @RequestParam(required = false) String password) {
        Map<String, Object> response = new HashMap<>();
        try {
            // Use Roll No as default password if not provided ?? NO, use null for
            // pre-registration
            String finalPassword = (password != null && !password.trim().isEmpty()) ? password : null;

            schoolService.addStudent(student, null, finalPassword);
            response.put("status", "success");
            response.put("message", "Student added successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", e.getMessage());
            return ResponseEntity.ok(response);
        }
    }

    @GetMapping("/editStudentPage")
    public ResponseEntity<Map<String, Object>> editStudentPage(@RequestParam Long id, Authentication authentication) {
        String username = authentication.getName();
        Teacher teacher = schoolService.getTeacherByUsername(username);
        Map<String, Object> response = new HashMap<>();
        response.put("teacher", teacher);

        Student student = schoolService.getAllStudents().stream()
                .filter(s -> s.getId().equals(id))
                .findFirst()
                .orElse(null);

        if (student == null) {
            response.put("error", "StudentNotFound");
            return ResponseEntity.status(404).body(response);
        }

        response.put("student", student);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/updateStudent")
    public ResponseEntity<Map<String, Object>> updateStudent(@ModelAttribute Student studentForm,
            @RequestParam(required = false) String password) {
        Map<String, Object> response = new HashMap<>();
        Student existingStudent = schoolService.getAllStudents().stream()
                .filter(s -> s.getId().equals(studentForm.getId()))
                .findFirst()
                .orElse(null);

        if (existingStudent != null) {
            existingStudent.setName(studentForm.getName());
            // Update Username if Roll No changes
            if (!existingStudent.getRollNo().equals(studentForm.getRollNo())) {
                // Check if new Roll No already exists
                if (schoolService.getStudentByUsername(studentForm.getRollNo()) != null) {
                    response.put("status", "error");
                    response.put("message", "RollNoAlreadyExists");
                    return ResponseEntity.ok(response);
                }
                existingStudent.getUser().setUsername(studentForm.getRollNo());
            }
            existingStudent.setRollNo(studentForm.getRollNo());
            existingStudent.setClassName(studentForm.getClassName());
            existingStudent.setAddress(studentForm.getAddress());
            existingStudent.setMotherName(studentForm.getMotherName());
            existingStudent.setMotherPhoneNumber(studentForm.getMotherPhoneNumber());
            existingStudent.setGender(studentForm.getGender());
            existingStudent.setJoiningDate(studentForm.getJoiningDate());

            // Update Password if provided
            if (password != null && !password.trim().isEmpty()) {
                User user = existingStudent.getUser();
                user.setPassword(passwordEncoder.encode(password));
            }

            schoolService.updateStudent(existingStudent);
            response.put("status", "success");
            response.put("message", "Student updated successfully");
            return ResponseEntity.ok(response);
        } else {
            response.put("status", "error");
            response.put("message", "StudentNotFound");
            return ResponseEntity.status(404).body(response);
        }
    }

    @PostMapping("/addHomework")
    public ResponseEntity<Map<String, Object>> addHomework(@ModelAttribute Homework homework) {
        Map<String, Object> response = new HashMap<>();
        homework.setAssignedDate(java.time.LocalDate.now());
        schoolService.addHomework(homework);
        response.put("status", "success");
        return ResponseEntity.ok(response);
    }

    @PostMapping("/addActivity")
    public ResponseEntity<Map<String, Object>> addActivity(
            @ModelAttribute com.preschool.preschool.entity.Activity activity) {
        Map<String, Object> response = new HashMap<>();
        activity.setDate(java.time.LocalDate.now());
        schoolService.addActivity(activity);
        response.put("status", "success");
        return ResponseEntity.ok(response);
    }

    @PostMapping("/addExamResult")
    public ResponseEntity<Map<String, Object>> addExamResult(
            @RequestParam String className,
            @RequestParam String rollNo,
            @ModelAttribute com.preschool.preschool.entity.ExamResult examResult) {

        Map<String, Object> response = new HashMap<>();

        com.preschool.preschool.entity.Student student = schoolService.getStudentByClassAndRoll(className, rollNo);
        if (student == null) {
            response.put("status", "error");
            response.put("message", "Student not found with Class: " + className + " and Roll No: " + rollNo);
            return ResponseEntity.badRequest().body(response);
        }

        examResult.setStudent(student);
        schoolService.addExamResult(examResult);

        response.put("status", "success");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/editExamResultPage")
    public ResponseEntity<Map<String, Object>> editExamResultPage(@RequestParam Long id,
            Authentication authentication) {
        String username = authentication.getName();
        Teacher teacher = schoolService.getTeacherByUsername(username);
        Map<String, Object> response = new HashMap<>();
        response.put("teacher", teacher);

        com.preschool.preschool.entity.ExamResult examResult = schoolService.getAllExamResults().stream()
                .filter(er -> er.getId().equals(id))
                .findFirst()
                .orElse(null);

        if (examResult == null) {
            response.put("error", "ResultNotFound");
            return ResponseEntity.status(404).body(response);
        }

        response.put("examResult", examResult);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/updateExamResult")
    public ResponseEntity<Map<String, Object>> updateExamResult(
            @ModelAttribute com.preschool.preschool.entity.ExamResult examResultForm) {
        Map<String, Object> response = new HashMap<>();
        com.preschool.preschool.entity.ExamResult existingResult = schoolService.getAllExamResults().stream()
                .filter(er -> er.getId().equals(examResultForm.getId()))
                .findFirst()
                .orElse(null);

        if (existingResult != null) {
            existingResult.setExamName(examResultForm.getExamName());
            existingResult.setSubject(examResultForm.getSubject());
            existingResult.setMarksObtained(examResultForm.getMarksObtained());
            existingResult.setTotalMarks(examResultForm.getTotalMarks());

            schoolService.addExamResult(existingResult); // Assuming addExamResult handles update (save)
            response.put("status", "success");
            return ResponseEntity.ok(response);
        } else {
            response.put("error", "ResultNotFound");
            return ResponseEntity.status(404).body(response);
        }
    }

    @GetMapping("/profilePage")
    public ResponseEntity<Map<String, Object>> profilePage(Authentication authentication) {
        String username = authentication.getName();
        Teacher teacher = schoolService.getTeacherByUsername(username);
        Map<String, Object> response = new HashMap<>();
        response.put("teacher", teacher);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/announcementPage")
    public ResponseEntity<Map<String, Object>> announcementPage(Authentication authentication) {
        String username = authentication.getName();
        Teacher teacher = schoolService.getTeacherByUsername(username);
        Map<String, Object> response = new HashMap<>();
        response.put("teacher", teacher);
        response.put("announcements", schoolService.getAllAnnouncements());

        // Get unique class names for the dropdown
        // Get unique class names for the dropdown
        java.util.List<String> classNames = schoolService.getAllClassNames();
        response.put("classNames", classNames);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/addAnnouncement")
    public ResponseEntity<Map<String, Object>> addAnnouncement(
            @ModelAttribute com.preschool.preschool.entity.Announcement announcement,
            Authentication authentication) {
        String username = authentication.getName();
        Teacher teacher = schoolService.getTeacherByUsername(username);
        Map<String, Object> response = new HashMap<>();

        announcement.setTeacher(teacher);
        announcement.setDate(java.time.LocalDate.now());
        schoolService.addAnnouncement(announcement);

        response.put("status", "success");
        return ResponseEntity.ok(response);
    }

    @PostMapping("/updateProfile")
    public ResponseEntity<Map<String, Object>> updateProfile(@ModelAttribute Teacher teacherForm,
            @RequestParam("profileImage") MultipartFile file,
            @RequestParam(required = false) String newPassword) {

        Map<String, Object> response = new HashMap<>();
        Authentication auth = org.springframework.security.core.context.SecurityContextHolder.getContext()
                .getAuthentication();
        String currentUsername = auth.getName();
        Teacher existingTeacher = schoolService.getTeacherByUsername(currentUsername);

        if (existingTeacher != null) {
            existingTeacher.setName(teacherForm.getName());
            existingTeacher.setSubject(teacherForm.getSubject());
            existingTeacher.setClassName(teacherForm.getClassName());
            existingTeacher.setPhoneNumber(teacherForm.getPhoneNumber());
            existingTeacher.setAddress(teacherForm.getAddress());
            existingTeacher.setEmail(teacherForm.getEmail());
            // School details are read-only, so we don't update them from form

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

                    existingTeacher.setPhoto("/uploads/" + fileName);
                    System.out.println("Photo saved to: " + filePath.toString());
                    System.out.println("DB photo path set to: " + existingTeacher.getPhoto());
                } catch (java.io.IOException e) {
                    e.printStackTrace(); // Handle error appropriately
                    System.err.println("Error saving photo: " + e.getMessage());
                }
            } else {
                System.out.println("File is empty, skipping photo update.");
            }

            // Update Password if provided
            if (newPassword != null && !newPassword.trim().isEmpty()) {
                User user = existingTeacher.getUser();
                user.setPassword(passwordEncoder.encode(newPassword));
            }

            schoolService.updateTeacher(existingTeacher);
            System.out.println("Teacher profile updated for: " + existingTeacher.getName());
            response.put("status", "success");
            return ResponseEntity.ok(response);
        } else {
            System.err.println("Teacher not found for username: " + currentUsername);
            response.put("error", "TeacherNotFound");
            return ResponseEntity.status(404).body(response);
        }
    }

    // FEES MANAGEMENT APIS

    @GetMapping("/getFees")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getFees(Authentication authentication,
            @RequestParam(required = false) String className,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            String username = authentication.getName();
            Teacher teacher = schoolService.getTeacherByUsername(username);
            Map<String, Object> response = new HashMap<>();

            if (teacher == null) {
                response.put("error", "TeacherNotFound");
                return ResponseEntity.status(404).body(response);
            }
            response.put("teacher", teacher);

            // Class Filter - Ensure fixed classes are always present
            Set<String> fixedClasses = new HashSet<>(Arrays.asList("Playgroup", "Nursery", "LKG", "UKG"));

            // Initialize default fees if not present
            for (String cn : fixedClasses) {
                if (schoolService.getClassFee(cn) == null) {
                    ClassFee newFee = new ClassFee();
                    newFee.setClassName(cn);
                    switch (cn) {
                        case "Playgroup":
                            newFee.setYearlyFee(6000.0);
                            break;
                        case "Nursery":
                            newFee.setYearlyFee(6500.0);
                            break;
                        case "LKG":
                            newFee.setYearlyFee(7000.0);
                            break;
                        case "UKG":
                            newFee.setYearlyFee(7500.0);
                            break;
                        default:
                            newFee.setYearlyFee(0.0);
                    }
                    schoolService.saveClassFee(newFee);
                }
            }

            List<String> classNames = schoolService.getAllClassNames();
            fixedClasses.addAll(classNames);
            List<String> allClassNames = new ArrayList<>(fixedClasses);
            Collections.sort(allClassNames);

            response.put("classNames", allClassNames);
            response.put("selectedClass", className);
            response.put("search", search);

            // Get all students and filter manually
            List<Student> allStudents = schoolService.getAllStudents();
            List<Student> filteredStudents = allStudents;

            if (className != null && !className.isEmpty()) {
                filteredStudents = filteredStudents.stream()
                        .filter(s -> s.getClassName() != null && s.getClassName().equals(className))
                        .collect(Collectors.toList());
            }

            if (search != null && !search.isEmpty()) {
                String searchLower = search.toLowerCase();
                filteredStudents = filteredStudents.stream()
                        .filter(s -> (s.getName() != null && s.getName().toLowerCase().contains(searchLower)) ||
                                (s.getRollNo() != null && s.getRollNo().toLowerCase().contains(searchLower)))
                        .collect(Collectors.toList());
            }

            // Pagination
            int start = Math.min(page * size, filteredStudents.size());
            int end = Math.min((start + size), filteredStudents.size());
            List<Student> pagedStudents = filteredStudents.subList(start, end);

            response.put("students", pagedStudents);
            response.put("currentPage", page);
            response.put("totalPages", (int) Math.ceil((double) filteredStudents.size() / size));
            response.put("totalItems", filteredStudents.size());

            // Calculate Fee Status
            List<ClassFee> classFees = schoolService.getAllClassFees();
            response.put("classFees", classFees);

            Map<String, Double> classFeeMap = new HashMap<>();
            for (ClassFee cf : classFees) {
                if (cf.getClassName() != null) {
                    classFeeMap.put(cf.getClassName(), cf.getYearlyFee());
                }
            }
            response.put("classFeeMap", classFeeMap);

            List<StudentFeeDTO> studentFeeDTOs = new ArrayList<>();

            // Calculate for PAGED students
            for (Student student : pagedStudents) {
                Double yearlyFee = 0.0;
                if (student.getClassName() != null) {
                    ClassFee cf = schoolService.getClassFee(student.getClassName());
                    if (cf != null && cf.getYearlyFee() != null) {
                        yearlyFee = cf.getYearlyFee();
                    }
                }

                Double totalPaid = schoolService.getTotalFeesPaidByStudent(student.getId());

                List<FeePayment> studentPayments = schoolService.getFeePaymentsForStudent(student.getId());
                LocalDate lastPaymentDate = null;
                for (FeePayment fp : studentPayments) {
                    if (fp.getPaymentDate() != null) {
                        if (lastPaymentDate == null || fp.getPaymentDate().isAfter(lastPaymentDate)) {
                            lastPaymentDate = fp.getPaymentDate();
                        }
                    }
                }

                Double pending = yearlyFee - totalPaid;
                studentFeeDTOs.add(new StudentFeeDTO(student, yearlyFee, totalPaid, pending, lastPaymentDate));
            }

            response.put("studentFeeDTOs", studentFeeDTOs);

            // Calculate Global/Filtered Summaries
            Double totalCollected = 0.0;
            Double totalPending = 0.0;

            for (Student student : filteredStudents) {
                Double yearlyFee = 0.0;
                if (student.getClassName() != null) {
                    ClassFee cf = schoolService.getClassFee(student.getClassName());
                    if (cf != null && cf.getYearlyFee() != null) {
                        yearlyFee = cf.getYearlyFee();
                    }
                }

                Double studentPaid = schoolService.getTotalFeesPaidByStudent(student.getId());
                totalCollected += studentPaid;
                totalPending += (yearlyFee - studentPaid);
            }

            response.put("totalCollected", totalCollected);
            response.put("totalPending", totalPending);

            // Recent Updates with Pending Amount Calculation
            List<FeePayment> recentPayments = schoolService.getRecentFeePayments();
            List<RecentPaymentDTO> recentPaymentDTOs = new ArrayList<>();

            for (FeePayment payment : recentPayments) {
                Student student = payment.getStudent();
                if (student == null) {
                    continue;
                }

                Double yearlyFee = 0.0;
                if (student.getClassName() != null) {
                    ClassFee cf = schoolService.getClassFee(student.getClassName());
                    if (cf != null && cf.getYearlyFee() != null) {
                        yearlyFee = cf.getYearlyFee();
                    }
                }
                Double totalPaid = schoolService.getTotalFeesPaidByStudent(student.getId());
                Double pending = payment.getPendingAmount();
                if (pending == null) {
                    pending = yearlyFee - totalPaid;
                }

                recentPaymentDTOs.add(new RecentPaymentDTO(
                        payment.getPaymentDate(),
                        student.getRollNo(),
                        student.getName(),
                        student.getClassName(),
                        payment.getAmount(),
                        pending,
                        payment.getRemarks()));
            }
            response.put("recentPayments", recentPaymentDTOs);

            logger.info("Fees API called successfully with {} students.", studentFeeDTOs.size());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error in getFees: ", e);
            Map<String, Object> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }

    @PostMapping("/saveClassFee")
    public ResponseEntity<Map<String, Object>> saveClassFee(@ModelAttribute ClassFee classFee) {
        Map<String, Object> response = new HashMap<>();
        schoolService.saveClassFee(classFee);
        response.put("status", "success");
        return ResponseEntity.ok(response);
    }

    @PostMapping("/addFeePayment")
    public ResponseEntity<Map<String, Object>> addFeePayment(@ModelAttribute FeePayment feePayment,
            @RequestParam("student") Long studentId) {
        Map<String, Object> response = new HashMap<>();
        Student student = schoolService.getStudentById(studentId);

        if (student != null) {
            feePayment.setStudent(student);
        } else {
            response.put("error", "StudentNotFound");
            return ResponseEntity.status(404).body(response);
        }

        if (feePayment.getPaymentDate() == null) {
            feePayment.setPaymentDate(LocalDate.now());
        }

        // Calculate Pending Amount
        if (student != null) {
            Double yearlyFee = 0.0;
            if (student.getClassName() != null) {
                ClassFee cf = schoolService.getClassFee(student.getClassName());
                if (cf != null && cf.getYearlyFee() != null) {
                    yearlyFee = cf.getYearlyFee();
                }
            }
            Double totalPaid = schoolService.getTotalFeesPaidByStudent(student.getId());
            Double pending = yearlyFee - (totalPaid + feePayment.getAmount());
            feePayment.setPendingAmount(pending);
        }

        schoolService.addFeePayment(feePayment);
        response.put("status", "success");
        return ResponseEntity.ok(response);
    }

    @PostMapping("/bulkAddFeePayment")
    public ResponseEntity<Map<String, Object>> bulkAddFeePayment(@RequestParam("studentIds") List<Long> studentIds,
            @RequestParam("amount") Double amount,
            @RequestParam("monthsPaid") String monthsPaid,
            @RequestParam(value = "remarks", required = false) String remarks) {

        Map<String, Object> response = new HashMap<>();

        if (studentIds != null && !studentIds.isEmpty()) {
            for (Long studentId : studentIds) {
                FeePayment feePayment = new FeePayment();

                Student student = schoolService.getStudentById(studentId);

                if (student != null) {
                    feePayment.setStudent(student);
                    feePayment.setAmount(amount);
                    feePayment.setMonthsPaid(monthsPaid);
                    feePayment.setRemarks(remarks);
                    feePayment.setStatus("Paid");
                    feePayment.setPaymentDate(LocalDate.now());

                    // Calculate Pending Amount
                    Double yearlyFee = 0.0;
                    if (student.getClassName() != null) {
                        ClassFee cf = schoolService.getClassFee(student.getClassName());
                        if (cf != null && cf.getYearlyFee() != null) {
                            yearlyFee = cf.getYearlyFee();
                        }
                    }
                    Double totalPaid = schoolService.getTotalFeesPaidByStudent(student.getId());
                    Double pending = yearlyFee - (totalPaid + amount);
                    feePayment.setPendingAmount(pending);

                    schoolService.addFeePayment(feePayment);
                }
            }
        }
        response.put("status", "success");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/editFeePaymentPage")
    public ResponseEntity<Map<String, Object>> editFeePaymentPage(@RequestParam Long id,
            Authentication authentication) {
        String username = authentication.getName();
        Teacher teacher = schoolService.getTeacherByUsername(username);
        Map<String, Object> response = new HashMap<>();
        response.put("teacher", teacher);

        FeePayment feePayment = schoolService.getAllFeePayments().stream()
                .filter(fp -> fp.getId().equals(id))
                .findFirst()
                .orElse(null);

        if (feePayment == null) {
            response.put("error", "PaymentNotFound");
            return ResponseEntity.status(404).body(response);
        }

        response.put("feePayment", feePayment);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/updateFeePayment")
    public ResponseEntity<Map<String, Object>> updateFeePayment(@ModelAttribute FeePayment feePaymentForm) {
        Map<String, Object> response = new HashMap<>();
        FeePayment existingPayment = schoolService.getAllFeePayments().stream()
                .filter(fp -> fp.getId().equals(feePaymentForm.getId()))
                .findFirst()
                .orElse(null);

        if (existingPayment != null) {
            // Calculate Pending Amount
            Student student = existingPayment.getStudent();
            if (student != null) {
                Double yearlyFee = 0.0;
                if (student.getClassName() != null) {
                    ClassFee cf = schoolService.getClassFee(student.getClassName());
                    if (cf != null && cf.getYearlyFee() != null) {
                        yearlyFee = cf.getYearlyFee();
                    }
                }
                // Total paid currently in DB includes the OLD amount of this payment
                Double currentTotalPaidInDb = schoolService.getTotalFeesPaidByStudent(student.getId());
                Double oldAmount = existingPayment.getAmount() != null ? existingPayment.getAmount() : 0.0;
                Double newAmount = feePaymentForm.getAmount() != null ? feePaymentForm.getAmount() : 0.0;

                Double pending = yearlyFee - (currentTotalPaidInDb - oldAmount + newAmount);
                existingPayment.setPendingAmount(pending);
            }

            existingPayment.setAmount(feePaymentForm.getAmount());
            existingPayment.setMonthsPaid(feePaymentForm.getMonthsPaid());
            existingPayment.setRemarks(feePaymentForm.getRemarks());
            existingPayment.setStatus(feePaymentForm.getStatus());

            schoolService.addFeePayment(existingPayment);
            response.put("status", "success");
            return ResponseEntity.ok(response);
        } else {
            response.put("error", "PaymentNotFound");
            return ResponseEntity.status(404).body(response);
        }
    }
}
