/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package kg.alex.lomonosov.domain;

import java.io.Serializable;

public class EmployeeMessage implements Serializable {

    private int id;
    private int employee_id;
    private String employee;
    private int order_message_id;
    private int message_status_id;

    public String getEmployee() {
        return employee;
    }

    public void setEmployee(String employee) {
        this.employee = employee;
    }

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

    public int getOrder_message_id() {
        return order_message_id;
    }

    public void setOrder_message_id(int order_message_id) {
        this.order_message_id = order_message_id;
    }

    public int getMessage_status_id() {
        return message_status_id;
    }

    public void setMessage_status_id(int message_status_id) {
        this.message_status_id = message_status_id;
    }
}
