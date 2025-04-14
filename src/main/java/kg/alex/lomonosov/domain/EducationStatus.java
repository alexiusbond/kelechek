/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package kg.alex.lomonosov.domain;

import java.io.Serializable;

public class EducationStatus implements Serializable {

    private String total;
    private String pre_registered;
    private String active;
    private String not_confirmed;
    private String outOf;
    private String graduated;

    public String getTotal() {
        return total;
    }

    public void setTotal(String total) {
        this.total = total;
    }

    public String getPre_registered() {
        return pre_registered;
    }

    public void setPre_registered(String pre_registered) {
        this.pre_registered = pre_registered;
    }

    public String getActive() {
        return active;
    }

    public void setActive(String active) {
        this.active = active;
    }

    public String getNot_confirmed() {
        return not_confirmed;
    }

    public void setNot_confirmed(String not_confirmed) {
        this.not_confirmed = not_confirmed;
    }

    public String getOutOf() {
        return outOf;
    }

    public void setOutOf(String outOf) {
        this.outOf = outOf;
    }

    public String getGraduated() {
        return graduated;
    }

    public void setGraduated(String graduated) {
        this.graduated = graduated;
    }

}
