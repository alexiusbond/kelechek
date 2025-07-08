/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package kg.alex.ellipse.domain;

import java.io.Serializable;
import java.util.Date;

public class Year implements Serializable {

    private int id;
    private String name;
    private String period;
    private String period_kg;
    private Date start_date;
    private Date end_date;
    private Long installment_date_limit;
    private boolean last;

    public Year() {
    }

    public Year(int id, String name, long installment_date_limit, boolean last) {
        this.id = id;
        this.name = name;
        this.last = last;
        this.installment_date_limit = installment_date_limit;
    }

    public Year(String period, String name, Date end_date) {
        this.period = period;
        this.name = name;
        this.end_date = end_date;
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

    public String getPeriod() {
        return period;
    }

    public void setPeriod(String period) {
        this.period = period;
    }

    public String getPeriod_kg() {
        return period_kg;
    }

    public void setPeriod_kg(String period_kg) {
        this.period_kg = period_kg;
    }

    public Date getStart_date() {
        return start_date;
    }

    public void setStart_date(Date start_date) {
        this.start_date = start_date;
    }

    public Date getEnd_date() {
        return end_date;
    }

    public void setEnd_date(Date end_date) {
        this.end_date = end_date;
    }

    public Long getInstallment_date_limit() {
        return installment_date_limit;
    }

    public void setInstallment_date_limit(Long installment_date_limit) {
        this.installment_date_limit = installment_date_limit;
    }

    public boolean isLast() {
        return last;
    }

    public void setLast(boolean last) {
        this.last = last;
    }
}
