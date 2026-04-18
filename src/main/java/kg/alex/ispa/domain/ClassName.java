/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package kg.alex.ispa.domain;

import java.io.Serializable;

/**
 * @author alex
 */
public class ClassName implements Serializable {
    private int id;
    private String name;
    private int class_type_id;
    private int class_number_id;
    private int status_id;
    private int school_id;

    public int getClass_type_id() {
        return class_type_id;
    }

    public void setClass_type_id(int class_type_id) {
        this.class_type_id = class_type_id;
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

    public int getStatus_id() {
        return status_id;
    }

    public void setStatus_id(int status_id) {
        this.status_id = status_id;
    }

    public int getClass_number_id() {
        return class_number_id;
    }

    public void setClass_number_id(int class_number_id) {
        this.class_number_id = class_number_id;
    }

    public int getSchool_id() {
        return school_id;
    }

    public void setSchool_id(int school_id) {
        this.school_id = school_id;
    }

}
