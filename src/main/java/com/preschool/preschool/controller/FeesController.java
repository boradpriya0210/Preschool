package com.preschool.preschool.controller;

import com.preschool.preschool.entity.ClassFee;
import com.preschool.preschool.entity.FeePayment;
import com.preschool.preschool.entity.Student;
import com.preschool.preschool.entity.Teacher;
import com.preschool.preschool.dto.StudentFeeDTO;
import com.preschool.preschool.dto.RecentPaymentDTO;
import com.preschool.preschool.service.SchoolService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Controller
@CrossOrigin(origins = "http://localhost:5500", allowCredentials = "true")
@RequestMapping("/teacher")
public class FeesController {

    @Autowired
    private SchoolService schoolService;

    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(FeesController.class);

    @GetMapping("/feesPage")
    public String feesPage(Model model, Authentication authentication,
            @RequestParam(required = false) String className,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            logger.info("Accessing feesPage. ClassName: {}, Search: {}, Page: {}, Size: {}", className, search, page,
                    size);

            String username = authentication.getName();
            Teacher teacher = schoolService.getTeacherByUsername(username);
            if (teacher == null) {
                logger.warn("Teacher not found for username: {}", username);
                return "redirect:/login?error=TeacherNotFound";
            }
            model.addAttribute("teacher", teacher);

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

            model.addAttribute("classNames", allClassNames);
            model.addAttribute("selectedClass", className);
            model.addAttribute("search", search);

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

            model.addAttribute("students", pagedStudents);
            model.addAttribute("currentPage", page);
            model.addAttribute("totalPages", (int) Math.ceil((double) filteredStudents.size() / size));
            model.addAttribute("totalItems", filteredStudents.size());

            // Calculate Fee Status
            List<ClassFee> classFees = schoolService.getAllClassFees();
            model.addAttribute("classFees", classFees);

            Map<String, Double> classFeeMap = new HashMap<>();
            for (ClassFee cf : classFees) {
                if (cf.getClassName() != null) {
                    classFeeMap.put(cf.getClassName(), cf.getYearlyFee());
                }
            }
            model.addAttribute("classFeeMap", classFeeMap);

            List<StudentFeeDTO> studentFeeDTOs = new ArrayList<>();

            // Calculate for PAGED students using OPTIMIZED queries
            for (Student student : pagedStudents) {
                Double yearlyFee = 0.0;
                if (student.getClassName() != null) {
                    ClassFee cf = schoolService.getClassFee(student.getClassName());
                    if (cf != null && cf.getYearlyFee() != null) {
                        yearlyFee = cf.getYearlyFee();
                    }
                }

                // OPTIMIZED: Use DB query instead of loading all payments
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

            model.addAttribute("studentFeeDTOs", studentFeeDTOs);

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

            model.addAttribute("totalCollected", totalCollected);
            model.addAttribute("totalPending", totalPending);

            // Recent Updates with Pending Amount Calculation
            List<FeePayment> recentPayments = schoolService.getRecentFeePayments();
            List<RecentPaymentDTO> recentPaymentDTOs = new ArrayList<>();

            for (FeePayment payment : recentPayments) {
                Student student = payment.getStudent();
                if (student == null) {
                    continue; // Skip orphaned payments
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
            model.addAttribute("recentPayments", recentPaymentDTOs);

            logger.info("FeesPage loaded successfully with {} students.", studentFeeDTOs.size());
            return "teacher/fees";
        } catch (Exception e) {
            logger.error("Error in feesPage: ", e);
            throw e;
        }
    }

}
