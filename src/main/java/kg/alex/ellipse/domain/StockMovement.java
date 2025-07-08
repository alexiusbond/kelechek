/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package kg.alex.ellipse.domain;

import java.io.Serializable;

public class StockMovement implements Serializable {

    private int id;
    private double quantity;
    private double price;
    private double rate;
    private double remain;
    private int order_number;
    private int measurement_id;
    private int acc_category_id;
    private int invoice_id;
    private int stock_movement_id;
    private String note;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public double getQuantity() {
        return quantity;
    }

    public void setQuantity(double quantity) {
        this.quantity = quantity;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public double getRate() {
        return rate;
    }

    public void setRate(double rate) {
        this.rate = rate;
    }

    public double getRemain() {
        return remain;
    }

    public void setRemain(double remain) {
        this.remain = remain;
    }

    public int getOrder_number() {
        return order_number;
    }

    public void setOrder_number(int order_number) {
        this.order_number = order_number;
    }

    public int getMeasurement_id() {
        return measurement_id;
    }

    public void setMeasurement_id(int measurement_id) {
        this.measurement_id = measurement_id;
    }

    public int getAcc_category_id() {
        return acc_category_id;
    }

    public void setAcc_category_id(int acc_category_id) {
        this.acc_category_id = acc_category_id;
    }

    public int getInvoice_id() {
        return invoice_id;
    }

    public void setInvoice_id(int invoice_id) {
        this.invoice_id = invoice_id;
    }

    public int getStock_movement_id() {
        return stock_movement_id;
    }

    public void setStock_movement_id(int stock_movement_id) {
        this.stock_movement_id = stock_movement_id;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }
}
