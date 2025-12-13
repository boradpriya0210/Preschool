package com.preschool.preschool.dto;

import com.preschool.preschool.entity.Student;
import java.time.LocalDate;

public class StudentFeeDTO {
    private Student student;
    private Double yearlyFee;
    private Double totalPaid;
    private Double pendingFee;
    private LocalDate lastPaymentDate;

    public StudentFeeDTO(Student student, Double yearlyFee, Double totalPaid, Double pendingFee,
            LocalDate lastPaymentDate) {
        this.student = student;
        this.yearlyFee = yearlyFee;
        this.totalPaid = totalPaid;
        this.pendingFee = pendingFee;
        this.lastPaymentDate = lastPaymentDate;
    }

    public Student getStudent() {
        return student;
    }

    public void setStudent(Student student) {
        this.student = student;
    }

    public Double getYearlyFee() {
        return yearlyFee;
    }

    public void setYearlyFee(Double yearlyFee) {
        this.yearlyFee = yearlyFee;
    }

    public Double getTotalPaid() {
        return totalPaid;
    }

    public void setTotalPaid(Double totalPaid) {
        this.totalPaid = totalPaid;
    }

    public Double getPendingFee() {
        return pendingFee;
    }

    public void setPendingFee(Double pendingFee) {
        this.pendingFee = pendingFee;
    }

    public LocalDate getLastPaymentDate() {
        return lastPaymentDate;
    }

    public void setLastPaymentDate(LocalDate lastPaymentDate) {
        this.lastPaymentDate = lastPaymentDate;
    }
}
