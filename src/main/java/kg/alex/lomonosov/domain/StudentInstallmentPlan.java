/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package kg.alex.lomonosov.domain;

import java.io.Serializable;
import java.util.Date;

public class StudentInstallmentPlan implements Serializable {

    private int id;
    private int student_id;
    private Double amount;
    private Date date_of_payment;
    private int year_id;

    public int getYear_id() {
        return year_id;
    }

    public void setYear_id(int year_id) {
        this.year_id = year_id;
    }

    public Date getDate_of_payment() {
        return date_of_payment;
    }

    public void setDate_of_payment(Date date_of_payment) {
        this.date_of_payment = date_of_payment;
    }

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

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

}
