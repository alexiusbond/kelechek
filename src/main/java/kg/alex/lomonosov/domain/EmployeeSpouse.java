/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package kg.alex.lomonosov.domain;

import java.io.Serializable;

public class EmployeeSpouse implements Serializable {

    private int employee_id;
    private int health_status_id;
    private String fullName;
    private String phone;
    private String health_notes;

    public int getEmployee_id() {
        return employee_id;
    }

    public void setEmployee_id(int employee_id) {
        this.employee_id = employee_id;
    }

    public int getHealth_status_id() {
        return health_status_id;
    }

    public void setHealth_status_id(int health_status_id) {
        this.health_status_id = health_status_id;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getHealth_notes() {
        return health_notes;
    }

    public void setHealth_notes(String health_notes) {
        this.health_notes = health_notes;
    }
}
