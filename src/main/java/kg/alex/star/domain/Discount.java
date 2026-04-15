package kg.alex.star.domain;

import java.io.Serializable;

public class Discount implements Serializable {

    private int id;
    private String name;
    private int disc_type_id;
    private double amount;
    private int year_id;
    private int status_id;

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

    public int getDisc_type_id() {
        return disc_type_id;
    }

    public void setDisc_type_id(int disc_type_id) {
        this.disc_type_id = disc_type_id;
    }

    public int getYear_id() {
        return year_id;
    }

    public void setYear_id(int year_id) {
        this.year_id = year_id;
    }

    public int getStatus_id() {
        return status_id;
    }

    public void setStatus_id(int status_id) {
        this.status_id = status_id;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double value) {
        this.amount = value;
    }

}
