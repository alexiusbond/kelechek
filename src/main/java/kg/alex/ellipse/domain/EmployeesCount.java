/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package kg.alex.ellipse.domain;

import java.io.Serializable;

public class EmployeesCount implements Serializable {

    private int others_count;
    private String director;
    private String others;
    private String accountant;

    public int getOthers_count() {
        return others_count;
    }

    public void setOthers_count(int others_count) {
        this.others_count = others_count;
    }

    public String getDirector() {
        return director;
    }

    public void setDirector(String director) {
        this.director = director;
    }

    public String getOthers() {
        return others;
    }

    public void setOthers(String others) {
        this.others = others;
    }

    public String getAccountant() {
        return accountant;
    }

    public void setAccountant(String accountant) {
        this.accountant = accountant;
    }

}
