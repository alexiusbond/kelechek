/*
 * Semester.java
 * Created on December 18, 2007, 1:32 PM
 */
package kg.alex.abl.domain;

import java.io.Serializable;
import java.util.Date;

/**
 * @author Alex
 */
public class OrderMessage implements Serializable {

    private int id;
    private String order_number;
    private String title;
    private String content;
    private String message;
    private String student;
    private Date date;
    private int student_id;
    private int discount_unit_id;
    private int year_id;
    private int employee_id;
    private int discount;
    private double currencyRate;

    public double getCurrencyRate() {
        return currencyRate;
    }

    public void setCurrencyRate(double currencyRate) {
        this.currencyRate = currencyRate;
    }

    public int getDiscount_unit_id() {
        return discount_unit_id;
    }

    public void setDiscount_unit_id(int discount_unit_id) {
        this.discount_unit_id = discount_unit_id;
    }

    public int getYear_id() {
        return year_id;
    }

    public void setYear_id(int year_id) {
        this.year_id = year_id;
    }

    public String getStudent() {
        return student;
    }

    public void setStudent(String student) {
        this.student = student;
    }

    public int getDiscount() {
        return discount;
    }

    public void setDiscount(int discount) {
        this.discount = discount;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getOrder_number() {
        return order_number;
    }

    public void setOrder_number(String order_number) {
        this.order_number = order_number;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
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
}
