/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package kg.alex.ispa.domain;


public class InventoryInvoice extends Invoice {

    private int id;
    private int room_id;
    private String block;
    private String floor;
    private String room;
    private String employee;
    private String invoiceNumberStr;
    private int employee_id;
    private int school_id;
    private int activity_status_id;

    public String getEmployee() {
        return employee;
    }

    public void setEmployee(String employee) {
        this.employee = employee;
    }

    public String getBlock() {
        return block;
    }

    public void setBlock(String block) {
        this.block = block;
    }

    public String getFloor() {
        return floor;
    }

    public void setFloor(String floor) {
        this.floor = floor;
    }

    public String getRoom() {
        return room;
    }

    public void setRoom(String room) {
        this.room = room;
    }

    @Override
    public int getSchool_id() {
        return school_id;
    }

    @Override
    public void setSchool_id(int school_id) {
        this.school_id = school_id;
    }

    public int getRoom_id() {
        return room_id;
    }

    public void setRoom_id(int room_id) {
        this.room_id = room_id;
    }

    @Override
    public int getId() {
        return id;
    }

    @Override
    public void setId(int id) {
        this.id = id;
    }

    public String getInvoiceNumberStr() {
        return invoiceNumberStr;
    }

    public void setInvoiceNumberStr(String invoiceNumberStr) {
        this.invoiceNumberStr = invoiceNumberStr;
    }

    @Override
    public int getEmployee_id() {
        return employee_id;
    }

    @Override
    public void setEmployee_id(int employee_id) {
        this.employee_id = employee_id;
    }

    public int getActivity_status_id() {
        return activity_status_id;
    }

    public void setActivity_status_id(int activity_status_id) {
        this.activity_status_id = activity_status_id;
    }
}
