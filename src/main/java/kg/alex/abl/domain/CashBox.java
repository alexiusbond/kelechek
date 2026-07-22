/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package kg.alex.abl.domain;

import kg.alex.abl.utils.Settings;

import java.io.Serializable;

public class CashBox implements Serializable {
    private int id;
    private int currency_id;
    private String currency;

    public CashBox(int id, int currency_id) {
        this.id = id;
        this.currency_id = currency_id;
        this.currency = this.currency_id == 1 ? Settings.KGS : Settings.USD;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getCurrency_id() {
        return currency_id;
    }

    public void setCurrency_id(int currency_id) {
        this.currency_id = currency_id;
    }
}
