/*
 * Semester.java
 * Created on December 18, 2007, 1:32 PM
 */
package kg.alex.aim.domain;

import java.io.Serializable;

/**
 * @author Alex
 */
public class Month implements Serializable {

    private int id;
    private String name;
    private double totalPayments;
    private double totalInstallments;

    public Month() {
    }

    public Month(int id, String name) {
        this.id = id;
        this.name = name;
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

    public double getTotalPayments() {
        return totalPayments;
    }

    public void setTotalPayments(double totalPayments) {
        this.totalPayments = totalPayments;
    }

    public double getTotalInstallments() {
        return totalInstallments;
    }

    public void setTotalInstallments(double totalInstallments) {
        this.totalInstallments = totalInstallments;
    }
}
