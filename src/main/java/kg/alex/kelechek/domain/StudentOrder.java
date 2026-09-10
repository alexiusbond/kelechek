/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package kg.alex.kelechek.domain;

import java.io.Serializable;
import java.util.Date;

public class StudentOrder implements Serializable {

    private int id;
    private int student_id;
    private int order_id;
    private String year;
    private int year_id;
    private int from_class_id;
    private int to_class_id;
    private int from_education_status_id;
    private int to_education_status_id;
    private int employee_id;
    private Date modification_date;
    private String reasons;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getStudent_id() {
        return student_id;
    }

    public void setStudent_id(int student_id) {
        this.student_id = student_id;
    }

    public int getOrder_id() {
        return order_id;
    }

    public void setOrder_id(int order_id) {
        this.order_id = order_id;
    }

    public String getYear() {
        return year;
    }

    public void setYear(String year) {
        this.year = year;
    }

    public int getYear_id() {
        return year_id;
    }

    public void setYear_id(int year_id) {
        this.year_id = year_id;
    }

    public int getFrom_class_id() {
        return from_class_id;
    }

    public void setFrom_class_id(int from_class_id) {
        this.from_class_id = from_class_id;
    }

    public int getTo_class_id() {
        return to_class_id;
    }

    public void setTo_class_id(int to_class_id) {
        this.to_class_id = to_class_id;
    }

    public int getFrom_education_status_id() {
        return from_education_status_id;
    }

    public void setFrom_education_status_id(int from_education_status_id) {
        this.from_education_status_id = from_education_status_id;
    }

    public int getTo_education_status_id() {
        return to_education_status_id;
    }

    public void setTo_education_status_id(int to_education_status_id) {
        this.to_education_status_id = to_education_status_id;
    }

    public int getEmployee_id() {
        return employee_id;
    }

    public void setEmployee_id(int employee_id) {
        this.employee_id = employee_id;
    }

    public Date getModification_date() {
        return modification_date;
    }

    public void setModification_date(Date modification_date) {
        this.modification_date = modification_date;
    }

    public String getReasons() {
        return reasons;
    }

    public void setReasons(String reasons) {
        this.reasons = reasons;
    }
}
