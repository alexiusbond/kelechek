/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package kg.alex.abl.domain;

import java.io.Serializable;

public class EmployeePhoneNumber implements Serializable {

    private int id;
    private int employee_id;
    private int phone_type_id;
    private String number;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getEmployee_id() {
        return employee_id;
    }

    public void setEmployee_id(int employee_id) {
        this.employee_id = employee_id;
    }

    public int getPhone_type_id() {
        return phone_type_id;
    }

    public void setPhone_type_id(int phone_type_id) {
        this.phone_type_id = phone_type_id;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }
}
