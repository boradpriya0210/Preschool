package com.preschool.preschool.repository;

import com.preschool.preschool.entity.FeePayment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface FeePaymentRepository extends JpaRepository<FeePayment, Long> {
    List<FeePayment> findByStudent_Id(Long studentId);

    @Query("SELECT SUM(f.amount) FROM FeePayment f WHERE f.student.id = :studentId")
    Double getTotalFeesPaidByStudent(@Param("studentId") Long studentId);

    List<FeePayment> findTop20ByOrderByPaymentDateDescIdDesc();
}
