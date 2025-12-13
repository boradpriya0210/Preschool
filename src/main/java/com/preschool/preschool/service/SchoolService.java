package com.preschool.preschool.service;

import com.preschool.preschool.entity.*;
import com.preschool.preschool.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.List;

@Service
public class SchoolService {

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private TeacherRepository teacherRepository;

    @Autowired
    private HomeworkRepository homeworkRepository;

    @Autowired
    private AttendanceRepository attendanceRepository;

    @Autowired
    private ActivityRepository activityRepository;

    @Autowired
    private FeePaymentRepository feePaymentRepository;

    @Autowired
    private ExamResultRepository examResultRepository;

    @Autowired
    private AnnouncementRepository announcementRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private ClassFeeRepository classFeeRepository;

    public ClassFee saveClassFee(ClassFee classFee) {
        ClassFee existing = classFeeRepository.findByClassName(classFee.getClassName());
        if (existing != null) {
            existing.setYearlyFee(classFee.getYearlyFee());
            return classFeeRepository.save(existing);
        }
        return classFeeRepository.save(classFee);
    }

    public ClassFee getClassFee(String className) {
        return classFeeRepository.findByClassName(className);
    }

    public List<ClassFee> getAllClassFees() {
        return classFeeRepository.findAll();
    }

    public Student addStudent(Student student, String username, String password) {
        // Check if Student exists by Roll No
        java.util.Optional<Student> existingStudentOpt = studentRepository.findByRollNo(student.getRollNo());

        if (existingStudentOpt.isPresent()) {
            Student existingStudent = existingStudentOpt.get();

            // Validate Class Name match
            if (student.getClassName() != null
                    && !student.getClassName().equalsIgnoreCase(existingStudent.getClassName())) {
                throw new RuntimeException("Student with Roll No " + student.getRollNo() + " already exists in class "
                        + existingStudent.getClassName());
            }

            // Check if existing student is already registered (has a user with password)
            User user = existingStudent.getUser();
            boolean isRegistered = user != null && user.getPassword() != null && !user.getPassword().isEmpty();

            if (isRegistered) {
                // If we are trying to set a password (signup), and it's already registered ->
                // Error
                if (password != null && !password.isEmpty()) {
                    throw new DataIntegrityViolationException(
                            "Student with Roll No " + student.getRollNo() + " is already registered.");
                }
                // If we are NOT setting a password (e.g. teacher update), allowing update is
                // fine, or strict check?
                // For now, let's assume teacher update shouldn't break.
            }

            // Claim/Update Logic
            if (user == null) {
                user = new User();
                // Use provided username, or fallback to Roll No if null (e.g. added by Teacher)
                user.setUsername((username != null && !username.isEmpty()) ? username : student.getRollNo());
                user.setRole(User.Role.STUDENT);
            } else if (username != null && !username.isEmpty()) {
                user.setUsername(username);
            }

            // Update Password only if provided (This allows Claiming)
            if (password != null && !password.isEmpty()) {
                // We need to encode it here if we call userService.createUser,
                // BUT userService.createUser ALSO encodes it.
                // WAIT. My change to userService.createUser encodes if not null.
                // So I should pass raw password to userService.createUser.
                user.setPassword(password);
            }
            // If password is null, we leave it as is (if existing) or null (if new).
            // But wait, if checking `isRegistered` relied on `user.getPassword()` not being
            // null.
            // If I update an unregistered user with null password again, it stays null.

            User savedUser = userService.createUser(user);

            // Update Student Details
            existingStudent.setName(student.getName());
            existingStudent.setUser(savedUser);

            return studentRepository.save(existingStudent);

        } else {
            // New Student Logic
            // Use provided username, or fallback to Roll No
            String finalUsername = (username != null && !username.isEmpty()) ? username : student.getRollNo();

            User existingUser = userService.findByUsername(finalUsername);
            User user;

            if (existingUser != null) {
                // This case should ideally not happen for student signup unless collision on
                // username
                // If user exists but is not linked to student? (Unlikely with RollNo as
                // username)
                user = existingUser;
                if (password != null && !password.isEmpty()) {
                    user.setPassword(password);
                }
            } else {
                user = new User();
                user.setUsername(finalUsername);
                if (password != null && !password.isEmpty()) {
                    user.setPassword(password);
                }
                user.setRole(User.Role.STUDENT);
            }

            User savedUser = userService.createUser(user);

            student.setUser(savedUser);
            return studentRepository.save(student);
        }
    }

    public Teacher addTeacher(Teacher teacher, String username, String password) {
        User user = new User();
        user.setUsername(username);
        user.setPassword(password);
        user.setRole(User.Role.TEACHER);
        User savedUser = userService.createUser(user);

        teacher.setUser(savedUser);
        return teacherRepository.save(teacher);
    }

    public Homework addHomework(Homework homework) {
        return homeworkRepository.save(homework);
    }

    public List<Homework> getHomeworkForClass(String className) {
        return homeworkRepository.findByClassName(className);
    }

    public List<Homework> getAllHomework() {
        return homeworkRepository.findAll();
    }

    public Attendance markAttendance(Attendance attendance) {
        java.util.Optional<Attendance> existing = attendanceRepository.findByStudent_IdAndDate(
                attendance.getStudent().getId(), attendance.getDate());

        if (existing.isPresent()) {
            Attendance existingAttendance = existing.get();
            existingAttendance.setPresent(attendance.isPresent());
            return attendanceRepository.save(existingAttendance);
        } else {
            return attendanceRepository.save(attendance);
        }
    }

    public List<Attendance> getAttendanceForStudent(Long studentId) {
        return attendanceRepository.findByStudent_Id(studentId);
    }

    public List<Attendance> getAttendanceForDate(java.time.LocalDate date) {
        return attendanceRepository.findByDate(date);
    }

    public Student getStudentByUsername(String username) {
        return studentRepository.findByUser_Username(username).orElse(null);
    }

    public Student getStudentById(Long id) {
        return studentRepository.findById(id).orElse(null);
    }

    public Teacher getTeacherByUsername(String username) {
        return teacherRepository.findByUser_Username(username).orElse(null);
    }

    // New methods for Activity
    public Activity addActivity(Activity activity) {
        return activityRepository.save(activity);
    }

    public List<Activity> getAllActivities() {
        return activityRepository.findAll();
    }

    public List<Activity> getActivitiesForClass(String className) {
        return activityRepository.findByClassName(className);
    }

    // New methods for FeePayment
    public FeePayment addFeePayment(FeePayment feePayment) {
        return feePaymentRepository.save(feePayment);
    }

    public List<FeePayment> getFeePaymentsForStudent(Long studentId) {
        return feePaymentRepository.findByStudent_Id(studentId);
    }

    public List<FeePayment> getAllFeePayments() {
        return feePaymentRepository.findAll();
    }

    public List<FeePayment> getRecentFeePayments() {
        return feePaymentRepository.findTop20ByOrderByPaymentDateDescIdDesc();
    }

    public Double getTotalFeesPaidByStudent(Long studentId) {
        Double total = feePaymentRepository.getTotalFeesPaidByStudent(studentId);
        return total != null ? total : 0.0;
    }

    // New methods for ExamResult
    public ExamResult addExamResult(ExamResult examResult) {
        return examResultRepository.save(examResult);
    }

    public List<ExamResult> getExamResultsForStudent(Long studentId) {
        return examResultRepository.findByStudent_Id(studentId);
    }

    public List<ExamResult> getAllExamResults() {
        return examResultRepository.findAll();
    }

    public List<Student> getAllStudents() {
        return studentRepository.findAll();
    }

    @org.springframework.transaction.annotation.Transactional
    public void updateTeacher(Teacher teacher) {
        teacherRepository.saveAndFlush(teacher);
    }

    public void updateStudent(Student student) {
        studentRepository.save(student);
    }

    public org.springframework.data.domain.Page<Student> getStudentsPaginated(int page, int size) {
        return studentRepository.findAll(org.springframework.data.domain.PageRequest.of(page, size));
    }

    // New methods for Announcement
    public Announcement addAnnouncement(Announcement announcement) {
        return announcementRepository.save(announcement);
    }

    public List<Announcement> getAllAnnouncements() {
        return announcementRepository.findAllByOrderByDateDesc();
    }

    public List<Announcement> getAnnouncementsByClass(String className) {
        return announcementRepository.findByClassNameOrderByDateDesc(className);
    }

    public List<String> getAllClassNames() {
        return classFeeRepository.findAll().stream()
                .map(ClassFee::getClassName)
                .distinct()
                .sorted()
                .collect(java.util.stream.Collectors.toList());
    }

    public Student getStudentByClassAndRoll(String className, String rollNo) {
        return studentRepository.findByClassNameAndRollNo(className, rollNo).orElse(null);
    }

    public List<Student> getStudentsByClass(String className) {
        return studentRepository.findByClassName(className);
    }
}
