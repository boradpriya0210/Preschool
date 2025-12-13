package com.preschool.preschool.service;

import com.preschool.preschool.entity.Student;
import com.preschool.preschool.entity.User;
import com.preschool.preschool.repository.StudentRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class SchoolServiceTest {

    @Mock
    private StudentRepository studentRepository;

    @Mock
    private UserService userService;

    @InjectMocks
    private SchoolService schoolService;

    @Test
    public void testAddStudent_NewStudent_Success() {
        // Arrange
        Student student = new Student();
        student.setRollNo("S101");
        student.setName("Test Student");
        student.setClassName("Class 1");

        User savedUser = new User();
        savedUser.setId(1L);
        savedUser.setUsername("S101");

        when(studentRepository.findByRollNo("S101")).thenReturn(Optional.empty());
        when(userService.findByUsername("S101")).thenReturn(null);
        when(userService.createUser(any(User.class))).thenReturn(savedUser);
        when(studentRepository.save(any(Student.class))).thenReturn(student);

        // Act
        Student result = schoolService.addStudent(student, null, "password");

        // Assert
        assertNotNull(result);
        verify(userService, times(1)).createUser(any(User.class));
        verify(studentRepository, times(1)).save(student);
    }
}
