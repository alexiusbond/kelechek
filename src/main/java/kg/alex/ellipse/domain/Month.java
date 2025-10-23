/*
 * Semester.java
 * Created on December 18, 2007, 1:32 PM
 */
package kg.alex.ellipse.domain;

import java.io.Serializable;

/**
 * @author Alex
 */
public class Month implements Serializable {

    private int id;
    private String name;
    private double total;

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

    public double getTotal() {
        return total;
    }

    public void setTotal(double total) {
        this.total = total;
    }
}
