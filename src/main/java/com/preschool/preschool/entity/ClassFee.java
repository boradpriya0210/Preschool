package com.preschool.preschool.entity;

import jakarta.persistence.*;

@Entity
public class ClassFee {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String className;

    private Double yearlyFee;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public Double getYearlyFee() {
        return yearlyFee;
    }

    public void setYearlyFee(Double yearlyFee) {
        this.yearlyFee = yearlyFee;
    }
}
