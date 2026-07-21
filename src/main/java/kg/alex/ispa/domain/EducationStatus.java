/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package kg.alex.ispa.domain;

import java.io.Serializable;

public class EducationStatus implements Serializable {
    private int total;
    private int pre_registered;
    private int active;
    private int not_confirmed;
    private int outOf;
    private int graduated;

    public int getTotal() {

        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    public int getPre_registered() {
        return pre_registered;
    }

    public void setPre_registered(int pre_registered) {
        this.pre_registered = pre_registered;
    }

    public int getActive() {
        return active;
    }

    public void setActive(int active) {
        this.active = active;
    }

    public int getNot_confirmed() {
        return not_confirmed;
    }

    public void setNot_confirmed(int not_confirmed) {
        this.not_confirmed = not_confirmed;
    }

    public int getOutOf() {
        return outOf;
    }

    public void setOutOf(int outOf) {
        this.outOf = outOf;
    }

    public int getGraduated() {
        return graduated;
    }

    public void setGraduated(int graduated) {
        this.graduated = graduated;
    }

}
