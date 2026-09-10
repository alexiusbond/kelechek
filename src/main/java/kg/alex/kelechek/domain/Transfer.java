/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package kg.alex.kelechek.domain;

import java.io.Serializable;

public class Transfer implements Serializable {

    private int id;
    private double amount;
    private double rate;
    private int currency_id;
    private int acc_category_id;
    private int invoice_id;
    private int acc_balance_settings_id;
    private String note;

    public int getAcc_balance_settings_id() {
        return acc_balance_settings_id;
    }

    public void setAcc_balance_settings_id(int acc_balance_settings_id) {
        this.acc_balance_settings_id = acc_balance_settings_id;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public double getRate() {
        return rate;
    }

    public void setRate(double rate) {
        this.rate = rate;
    }

    public int getCurrency_id() {
        return currency_id;
    }

    public void setCurrency_id(int currency_id) {
        this.currency_id = currency_id;
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

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }
}
