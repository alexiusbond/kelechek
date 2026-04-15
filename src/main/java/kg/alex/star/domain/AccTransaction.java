/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package kg.alex.star.domain;

import java.io.Serializable;
import java.util.Date;

public class AccTransaction implements Serializable {
    private String id;
    private Date date;
    private int category_id;
    private int accTypeId;
    private CashBox cashbox;
    private double currency_rate;
    private double amount;
    private double overLimit;
    private double limit;
    private int employee_id;
    private int school_id;
    private String note;
    private String category;
    private String employee;
    private String from_to_employee;
    private int dp_invoice_id;
    private int acc_invoice_id;
    private int student_payments_id;
    private int from_to_employee_id;
    private int order_number;

    public int getAccTypeId() {
        return accTypeId;
    }

    public void setAccTypeId(int accTypeId) {
        this.accTypeId = accTypeId;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getSchool_id() {
        return school_id;
    }

    public void setSchool_id(int school_id) {
        this.school_id = school_id;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public int getCategory_id() {
        return category_id;
    }

    public void setCategory_id(int category_id) {
        this.category_id = category_id;
    }


    public CashBox getCashbox() {
        return cashbox;
    }

    public void setCashbox(CashBox cashbox) {
        this.cashbox = cashbox;
    }

    public double getCurrency_rate() {
        return currency_rate;
    }

    public void setCurrency_rate(double currency_rate) {
        this.currency_rate = currency_rate;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public int getEmployee_id() {
        return employee_id;
    }

    public void setEmployee_id(int employee_id) {
        this.employee_id = employee_id;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public int getStudent_payments_id() {
        return student_payments_id;
    }

    public void setStudent_payments_id(int student_payments_id) {
        this.student_payments_id = student_payments_id;
    }

    public double getOverLimit() {
        return overLimit;
    }

    public void setOverLimit(double overLimit) {
        this.overLimit = overLimit;
    }

    public double getLimit() {
        return limit;
    }

    public void setLimit(double limit) {
        this.limit = limit;
    }

    public int getFrom_to_employee_id() {
        return from_to_employee_id;
    }

    public void setFrom_to_employee_id(int from_to_employee_id) {
        this.from_to_employee_id = from_to_employee_id;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getEmployee() {
        return employee;
    }

    public void setEmployee(String employee) {
        this.employee = employee;
    }

    public String getFrom_to_employee() {
        return from_to_employee;
    }

    public void setFrom_to_employee(String from_to_employee) {
        this.from_to_employee = from_to_employee;
    }

    public int getOrder_number() {
        return order_number;
    }

    public void setOrder_number(int order_number) {
        this.order_number = order_number;
    }

    public int getDp_invoice_id() {
        return dp_invoice_id;
    }

    public void setDp_invoice_id(int dp_invoice_id) {
        this.dp_invoice_id = dp_invoice_id;
    }

    public int getAcc_invoice_id() {
        return acc_invoice_id;
    }

    public void setAcc_invoice_id(int acc_invoice_id) {
        this.acc_invoice_id = acc_invoice_id;
    }
}
