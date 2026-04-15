package kg.alex.star.domain;

import java.io.Serializable;

public class StudentCorrection implements Serializable {
    private String id;
    private double amount;
    private String note;
    private int student_id;
    private int year_id;
    private int correction_type_id;
    private int employee_id;

    public int getYear_id() {
        return year_id;
    }

    public void setYear_id(int year_id) {
        this.year_id = year_id;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public int getStudent_id() {
        return student_id;
    }

    public void setStudent_id(int student_id) {
        this.student_id = student_id;
    }

    public int getCorrection_type_id() {
        return correction_type_id;
    }

    public void setCorrection_type_id(int correction_type_id) {
        this.correction_type_id = correction_type_id;
    }

    public int getEmployee_id() {
        return employee_id;
    }

    public void setEmployee_id(int employee_id) {
        this.employee_id = employee_id;
    }

}
