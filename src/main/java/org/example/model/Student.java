package org.example.model;
import java.time.LocalDate;

public class Student {
    private boolean deleted;
    private int id;
    private String fullname;
    private LocalDate birthDate;
    private double cost;
    private boolean active;
    public static final int NAME_LENGTH = 30;
    public static final int RECORD_SIZE = 82;

    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getFullname() {
        return fullname;
    }

    public void setFullname(String fullname) {
        this.fullname = fullname;
    }

    public LocalDate getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(LocalDate birthDate) {
        this.birthDate = birthDate;
    }

    public double getCost() {
        return cost;
    }

    public void setCost(double cost) {
        this.cost = cost;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    @Override
    public String toString() {
        return "Student{" +
                "deleted=" + deleted +
                ", id=" + id +
                ", fullname='" + fullname + '\'' +
                ", birthDate=" + birthDate +
                ", cost=" + cost +
                ", active=" + active +
                '}';
    }

    public Student(boolean deleted, int id, String fullname, LocalDate birthDate, double cost, boolean active) {
        this.deleted = false;
        this.id = id;
        this.fullname = fullname;
        this.birthDate = birthDate;
        this.cost = cost;
        this.active = active;
    }
}
