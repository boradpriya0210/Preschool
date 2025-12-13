package com.preschool.preschool.repository;

import com.preschool.preschool.entity.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface StudentRepository extends JpaRepository<Student, Long> {
    Optional<Student> findByUser_Username(String username);

    Optional<Student> findByRollNo(String rollNo);

    Optional<Student> findByClassNameAndRollNo(String className, String rollNo);

    java.util.List<Student> findByClassName(String className);
}
