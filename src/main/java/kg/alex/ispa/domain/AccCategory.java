/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package kg.alex.ispa.domain;

import java.io.Serializable;

public class AccCategory implements Serializable {

    private int id;
    private String name;
    private String code;
    private String note;
    private int parent_id;
    private String parent_code;
    private int type_id;
    private int status_id;
    private int school_id;
    private int employee_id;
    private int modified_employee_id;

    public int getModified_employee_id() {
        return modified_employee_id;
    }

    public void setModified_employee_id(int modified_employee_id) {
        this.modified_employee_id = modified_employee_id;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public int getType_id() {
        return type_id;
    }

    public void setType_id(int type_id) {
        this.type_id = type_id;
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

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public int getParent_id() {
        return parent_id;
    }

    public void setParent_id(int parent_id) {
        this.parent_id = parent_id;
    }

    public int getStatus_id() {
        return status_id;
    }

    public void setStatus_id(int status_id) {
        this.status_id = status_id;
    }

    /**
     * @return the parent_code
     */
    public String getParent_code() {
        return parent_code;
    }

    /**
     * @param parent_code the parent_code to set
     */
    public void setParent_code(String parent_code) {
        this.parent_code = parent_code;
    }

    /**
     * @return the school_id
     */
    public int getSchool_id() {
        return school_id;
    }

    /**
     * @param school_id the school_id to set
     */
    public void setSchool_id(int school_id) {
        this.school_id = school_id;
    }

    /**
     * @return the employee_id
     */
    public int getEmployee_id() {
        return employee_id;
    }

    /**
     * @param employee_id the employee_id to set
     */
    public void setEmployee_id(int employee_id) {
        this.employee_id = employee_id;
    }

}
