/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package kg.alex.indigo.domain;

import java.io.Serializable;

public class Discount implements Serializable {

    private int id;
    private String name;
    private int disc_type_id;
    private double amount;
    private int year_id;
    private int status_id;
    private int currency_id;
    private int discount_unit_id;

    public int getCurrency_id() {
        return currency_id;
    }

    public void setCurrency_id(int currency_id) {
        this.currency_id = currency_id;
    }

    public int getDiscount_unit_id() {
        return discount_unit_id;
    }

    public void setDiscount_unit_id(int discount_unit_id) {
        this.discount_unit_id = discount_unit_id;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getDisc_type_id() {
        return disc_type_id;
    }

    public void setDisc_type_id(int disc_type_id) {
        this.disc_type_id = disc_type_id;
    }

    public int getYear_id() {
        return year_id;
    }

    public void setYear_id(int year_id) {
        this.year_id = year_id;
    }

    public int getStatus_id() {
        return status_id;
    }

    public void setStatus_id(int status_id) {
        this.status_id = status_id;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double value) {
        this.amount = value;
    }

}
