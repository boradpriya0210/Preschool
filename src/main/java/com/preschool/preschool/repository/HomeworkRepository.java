package com.preschool.preschool.repository;

import com.preschool.preschool.entity.Homework;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface HomeworkRepository extends JpaRepository<Homework, Long> {
    List<Homework> findByClassName(String className);
}
