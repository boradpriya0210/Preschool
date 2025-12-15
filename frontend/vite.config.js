import { defineConfig } from 'vite';
import { resolve } from 'path';

export default defineConfig({
  root: '.', // Frontend folder is the root
  server: {
    port: 8080, // Run dev server on port 8080
    proxy: {
      '/api': {
        target: 'http://localhost:9097', // Proxy API calls to Spring Boot
        changeOrigin: true,
        secure: false,
      }
    }
  },
  build: {
    // Output directly to Spring Boot's static folder
    outDir: '../src/main/resources/static',
    emptyOutDir: true, // Clean the folder before building
    rollupOptions: {
      input: {
        // Main Pages
        main: resolve(__dirname, 'index.html'),
        login: resolve(__dirname, 'login.html'),
        
        // Teacher Module
        teacher_dashboard: resolve(__dirname, 'teacher/dashboard.html'),
        teacher_activity: resolve(__dirname, 'teacher/activity.html'),
        teacher_add_student: resolve(__dirname, 'teacher/add_student.html'),
        teacher_announcement: resolve(__dirname, 'teacher/announcement.html'),
        teacher_attendance: resolve(__dirname, 'teacher/attendance.html'),
        teacher_edit_exam_result: resolve(__dirname, 'teacher/edit_exam_result.html'),
        teacher_edit_student: resolve(__dirname, 'teacher/edit_student.html'),
        teacher_edit_fee_payment: resolve(__dirname, 'teacher/edit_fee_payment.html'),
        teacher_fees: resolve(__dirname, 'teacher/fees.html'),
        teacher_homework: resolve(__dirname, 'teacher/homework.html'),
        teacher_marks: resolve(__dirname, 'teacher/marks.html'),
        teacher_profile: resolve(__dirname, 'teacher/profile.html'),

        // Student Module
        student_dashboard: resolve(__dirname, 'student/dashboard.html'),
        student_activity: resolve(__dirname, 'student/activity.html'),
        student_announcements: resolve(__dirname, 'student/announcements.html'),
        student_attendance: resolve(__dirname, 'student/attendance.html'),
        student_fees: resolve(__dirname, 'student/fees.html'),
        student_homework: resolve(__dirname, 'student/homework.html'),
        student_profile: resolve(__dirname, 'student/profile.html'),
      }
    }
  }
});
