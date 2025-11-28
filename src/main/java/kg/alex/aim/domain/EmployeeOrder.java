/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package kg.alex.aim.domain;

import java.io.Serializable;
import java.util.Date;

public class EmployeeOrder implements Serializable {

    private int id;
    private String idStr;
    private int employee_id;
    private int order_id;
    private int school_id;
    private int from_to_school_id;
    private int position_id;
    private int class_name_id;
    private int m_employee_id;
    private Date from_date;
    private Date to_date;
    private String note;
    private int effected_by_id;
    private int can_not_delete;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getIdStr() {
        return idStr;
    }

    public void setIdStr(String idStr) {
        this.idStr = idStr;
    }

    public int getEmployee_id() {
        return employee_id;
    }

    public void setEmployee_id(int employee_id) {
        this.employee_id = employee_id;
    }

    public int getOrder_id() {
        return order_id;
    }

    public void setOrder_id(int order_id) {
        this.order_id = order_id;
    }

    public int getSchool_id() {
        return school_id;
    }

    public void setSchool_id(int school_id) {
        this.school_id = school_id;
    }

    public int getFrom_to_school_id() {
        return from_to_school_id;
    }

    public void setFrom_to_school_id(int from_to_school_id) {
        this.from_to_school_id = from_to_school_id;
    }

    public int getPosition_id() {
        return position_id;
    }

    public void setPosition_id(int position_id) {
        this.position_id = position_id;
    }

    public int getClass_name_id() {
        return class_name_id;
    }

    public void setClass_name_id(int class_name_id) {
        this.class_name_id = class_name_id;
    }

    public int getM_employee_id() {
        return m_employee_id;
    }

    public void setM_employee_id(int m_employee_id) {
        this.m_employee_id = m_employee_id;
    }

    public Date getFrom_date() {
        return from_date;
    }

    public void setFrom_date(Date from_date) {
        this.from_date = from_date;
    }

    public Date getTo_date() {
        return to_date;
    }

    public void setTo_date(Date to_date) {
        this.to_date = to_date;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public int getEffected_by_id() {
        return effected_by_id;
    }

    public void setEffected_by_id(int effected_by_id) {
        this.effected_by_id = effected_by_id;
    }

    public int getCan_not_delete() {
        return can_not_delete;
    }

    public void setCan_not_delete(int can_not_delete) {
        this.can_not_delete = can_not_delete;
    }
}
