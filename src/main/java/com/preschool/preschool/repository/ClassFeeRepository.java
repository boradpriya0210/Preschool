package com.preschool.preschool.repository;

import com.preschool.preschool.entity.ClassFee;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClassFeeRepository extends JpaRepository<ClassFee, Long> {
    ClassFee findByClassName(String className);
}
