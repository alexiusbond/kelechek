/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package kg.alex.ellipse.domain;

import java.io.Serializable;

public class StudentDiscount implements Serializable {

    private String id;
    private int year_id;
    private int discount_id;
    private int student_id;
    private int employee_id;
    private double free_entry_amount;
    private double discount_value;
    private String note;

    public double getDiscount_value() {
        return discount_value;
    }

    public void setDiscount_value(double discount_value) {
        this.discount_value = discount_value;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public double getFree_entry_amount() {
        return free_entry_amount;
    }

    public void setFree_entry_amount(double free_entry_amount) {
        this.free_entry_amount = free_entry_amount;
    }

    public int getYear_id() {
        return year_id;
    }

    public void setYear_id(int year_id) {
        this.year_id = year_id;
    }

    public int getDiscount_id() {
        return discount_id;
    }

    public void setDiscount_id(int discount_id) {
        this.discount_id = discount_id;
    }

    public int getStudent_id() {
        return student_id;
    }

    public void setStudent_id(int student_id) {
        this.student_id = student_id;
    }

    public int getEmployee_id() {
        return employee_id;
    }

    public void setEmployee_id(int employee_id) {
        this.employee_id = employee_id;
    }

    @Override
    public String toString() {
        return ">>>> StudentDiscount{" +
                "id='" + id + '\'' +
                ", year_id=" + year_id +
                ", discount_id=" + discount_id +
                ", student_id=" + student_id +
                ", employee_id=" + employee_id +
                ", free_entry_amount=" + free_entry_amount +
                ", discount_value=" + discount_value +
                ", note='" + note + '\'' +
                '}';
    }
}
