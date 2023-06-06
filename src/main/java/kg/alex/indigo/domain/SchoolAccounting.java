/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package kg.alex.indigo.domain;

import java.io.Serializable;

public class SchoolAccounting implements Serializable {

    private Double total_income;
    private Double total_outcome;
    private Double previous_balance;
    private String last_income_date;
    private String last_outcome_date;

    public Double getTotal_income() {
        return total_income;
    }

    public void setTotal_income(Double total_income) {
        this.total_income = total_income;
    }

    public Double getTotal_outcome() {
        return total_outcome;
    }

    public void setTotal_outcome(Double total_outcome) {
        this.total_outcome = total_outcome;
    }

    public Double getPrevious_balance() {
        return previous_balance;
    }

    public void setPrevious_balance(Double previous_balance) {
        this.previous_balance = previous_balance;
    }

    public String getLast_income_date() {
        return last_income_date;
    }

    public void setLast_income_date(String last_income_date) {
        this.last_income_date = last_income_date;
    }

    public String getLast_outcome_date() {
        return last_outcome_date;
    }

    public void setLast_outcome_date(String last_outcome_date) {
        this.last_outcome_date = last_outcome_date;
    }
}
