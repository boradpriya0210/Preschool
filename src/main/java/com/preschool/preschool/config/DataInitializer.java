package com.preschool.preschool.config;

import com.preschool.preschool.entity.ClassFee;
import com.preschool.preschool.entity.User;
import com.preschool.preschool.entity.Teacher;
import com.preschool.preschool.entity.Student;
import com.preschool.preschool.repository.ClassFeeRepository;
import com.preschool.preschool.repository.UserRepository;
import com.preschool.preschool.repository.TeacherRepository;
import com.preschool.preschool.repository.StudentRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DataInitializer {

    @Bean
    public CommandLineRunner initData(ClassFeeRepository classFeeRepository,
            UserRepository userRepository,
            TeacherRepository teacherRepository,
            StudentRepository studentRepository,
            PasswordEncoder passwordEncoder) {
        return args -> {
            try {
                System.out.println("\n============================================");
                System.out.println("INITIALIZING DEFAULT DATA...");
                System.out.println("============================================\n");

                // Initialize Class Fees
                createClassFeeIfNotFound(classFeeRepository, "Preschool", 5500.0);
                createClassFeeIfNotFound(classFeeRepository, "Playgroup", 6000.0);
                createClassFeeIfNotFound(classFeeRepository, "Nursery", 6500.0);
                createClassFeeIfNotFound(classFeeRepository, "LKG", 7000.0);
                createClassFeeIfNotFound(classFeeRepository, "UKG", 7500.0);

                // Initialize Default Users
                createDefaultTeachers(userRepository, teacherRepository, passwordEncoder);
                createDefaultStudents(userRepository, studentRepository, passwordEncoder);

                System.out.println("\n✓ Data initialization completed successfully!");
                System.out.println("============================================\n");
            } catch (Exception e) {
                System.err.println("Error during data initialization: " + e.getMessage());
                e.printStackTrace();
            }
        };
    }

    private void createClassFeeIfNotFound(ClassFeeRepository repository, String className, Double fee) {
        try {
            if (repository.findByClassName(className) == null) {
                ClassFee classFee = new ClassFee();
                classFee.setClassName(className);
                classFee.setYearlyFee(fee);
                repository.save(classFee);
                System.out.println("  ✓ Created ClassFee: " + className + " (Rs. " + fee + ")");
            }
        } catch (Exception e) {
            System.err.println("  ✗ Error creating ClassFee " + className + ": " + e.getMessage());
        }
    }

    private void createDefaultTeachers(UserRepository userRepository, TeacherRepository teacherRepository,
            PasswordEncoder passwordEncoder) {
        try {
            System.out.println("\n  Creating Teachers...");
        } catch (Exception e) {
            System.err.println("  ✗ Error creating Teachers: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void createDefaultStudents(UserRepository userRepository, StudentRepository studentRepository,
            PasswordEncoder passwordEncoder) {
        try {
            System.out.println("\n  Creating Students...");
        } catch (Exception e) {
            System.err.println("  ✗ Error creating Students: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
