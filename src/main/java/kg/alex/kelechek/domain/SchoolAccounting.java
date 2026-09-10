/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package kg.alex.kelechek.domain;

import java.io.Serializable;

public class SchoolAccounting implements Serializable {

    private Double total_income;
    private Double total_outcome;
    private Double previous_balance;

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

}
