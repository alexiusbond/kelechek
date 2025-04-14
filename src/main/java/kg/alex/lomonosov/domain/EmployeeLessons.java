/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package kg.alex.lomonosov.domain;

import java.io.Serializable;

public class EmployeeLessons implements Serializable {

    private int id;
    private int employee_id;
    private int branch_id;
    private int year_id;
    private int school_id;
    private int class_number_id;
    private int hours;
    private int extra_hours;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getEmployee_id() {
        return employee_id;
    }

    public void setEmployee_id(int employee_id) {
        this.employee_id = employee_id;
    }

    public int getBranch_id() {
        return branch_id;
    }

    public void setBranch_id(int branch_id) {
        this.branch_id = branch_id;
    }

    public int getYear_id() {
        return year_id;
    }

    public void setYear_id(int year_id) {
        this.year_id = year_id;
    }

    public int getSchool_id() {
        return school_id;
    }

    public void setSchool_id(int school_id) {
        this.school_id = school_id;
    }

    public int getClass_number_id() {
        return class_number_id;
    }

    public void setClass_number_id(int class_number_id) {
        this.class_number_id = class_number_id;
    }

    public int getHours() {
        return hours;
    }

    public void setHours(int hours) {
        this.hours = hours;
    }

    public int getExtra_hours() {
        return extra_hours;
    }

    public void setExtra_hours(int extra_hours) {
        this.extra_hours = extra_hours;
    }
}
