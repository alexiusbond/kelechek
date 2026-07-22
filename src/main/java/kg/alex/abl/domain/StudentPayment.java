/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package kg.alex.abl.domain;

import java.io.Serializable;
import java.util.Date;

public class StudentPayment implements Serializable {

    private int id;
    private int student_id;
    private int year_id;
    private double amount;
    private double init_pay;
    private double ttl_pay;
    private double rate;
    private int payment_type_id;
    private int currency_id;
    private int cashBox_id;
    private int payment_cat_type_id;
    private int employee_id;
    private int school_id;
    private String who_paid;
    private String note;
    private String noteForCashBox;
    private Date modification_date;

    public int getCashBox_id() {
        return cashBox_id;
    }

    public void setCashBox_id(int cashBox_id) {
        this.cashBox_id = cashBox_id;
    }

    public int getCurrency_id() {
        return currency_id;
    }

    public void setCurrency_id(int currency_id) {
        this.currency_id = currency_id;
    }

    public String getNoteForCashBox() {
        return noteForCashBox;
    }

    public void setNoteForCashBox(String noteForCashBox) {
        this.noteForCashBox = noteForCashBox;
    }

    public int getSchool_id() {
        return school_id;
    }

    public void setSchool_id(int school_id) {
        this.school_id = school_id;
    }

    public double getRate() {
        return rate;
    }

    public void setRate(double rate) {
        this.rate = rate;
    }

    public double getInit_pay() {
        return init_pay;
    }

    public void setInit_pay(double init_pay) {
        this.init_pay = init_pay;
    }

    public double getTtl_pay() {
        return ttl_pay;
    }

    public void setTtl_pay(double ttl_pay) {
        this.ttl_pay = ttl_pay;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public String getWho_paid() {
        return who_paid;
    }

    public void setWho_paid(String who_paid) {
        this.who_paid = who_paid;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getPayment_cat_type_id() {
        return payment_cat_type_id;
    }

    public void setPayment_cat_type_id(int payment_cat_type_id) {
        this.payment_cat_type_id = payment_cat_type_id;
    }

    public int getStudent_id() {
        return student_id;
    }

    public void setStudent_id(int student_id) {
        this.student_id = student_id;
    }

    public int getYear_id() {
        return year_id;
    }

    public void setYear_id(int year_id) {
        this.year_id = year_id;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public int getPayment_type_id() {
        return payment_type_id;
    }

    public void setPayment_type_id(int payment_type_id) {
        this.payment_type_id = payment_type_id;
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

}
