package com.preschool.preschool.dto;

import java.time.LocalDate;

public class RecentPaymentDTO {
    private LocalDate paymentDate;
    private String rollNo;
    private String studentName;
    private String className;
    private Double paidAmount;
    private Double pendingAmount;
    private String remarks;

    public RecentPaymentDTO(LocalDate paymentDate, String rollNo, String studentName, String className,
            Double paidAmount, Double pendingAmount, String remarks) {
        this.paymentDate = paymentDate;
        this.rollNo = rollNo;
        this.studentName = studentName;
        this.className = className;
        this.paidAmount = paidAmount;
        this.pendingAmount = pendingAmount;
        this.remarks = remarks;
    }

    // Getters
    public LocalDate getPaymentDate() {
        return paymentDate;
    }

    public String getRollNo() {
        return rollNo;
    }

    public String getStudentName() {
        return studentName;
    }

    public String getClassName() {
        return className;
    }

    public Double getPaidAmount() {
        return paidAmount;
    }

    public Double getPendingAmount() {
        return pendingAmount;
    }

    public String getRemarks() {
        return remarks;
    }
}
