package com.carrental.dto;

import java.time.LocalDate;

public class ReturnCarRequest {

    private LocalDate returnDate;
    private Integer currentMileage;
    private String inspectionNotes;
    private boolean hasDamage = false;

    public ReturnCarRequest() {
    }

    public LocalDate getReturnDate() {
        return returnDate;
    }

    public void setReturnDate(LocalDate returnDate) {
        this.returnDate = returnDate;
    }

    public Integer getCurrentMileage() {
        return currentMileage;
    }

    public void setCurrentMileage(Integer currentMileage) {
        this.currentMileage = currentMileage;
    }

    public String getInspectionNotes() {
        return inspectionNotes;
    }

    public void setInspectionNotes(String inspectionNotes) {
        this.inspectionNotes = inspectionNotes;
    }

    public boolean isHasDamage() {
        return hasDamage;
    }

    public void setHasDamage(boolean hasDamage) {
        this.hasDamage = hasDamage;
    }
}
