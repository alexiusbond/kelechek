/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package kg.alex.ispa.domain;

import java.io.Serializable;
import java.util.Date;

public class Student implements Serializable {

    private int id;
    private String login;
    private String password;
    private String name;
    private String surname;
    private String address;
    private String middle_name;
    private int gender_id;
    private Date birth_date;
    private String class_name;
    private int class_name_id;
    private int edu_status_id;
    private String photo;
    private String school;
    private int school_id;
    private int entering_year_id;
    private int employee_id;

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSurname() {
        return surname;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    public String getMiddle_name() {
        return middle_name;
    }

    public void setMiddle_name(String middle_name) {
        this.middle_name = middle_name;
    }

    public int getGender_id() {
        return gender_id;
    }

    public void setGender_id(int gender_id) {
        this.gender_id = gender_id;
    }

    public Date getBirth_date() {
        return birth_date;
    }

    public void setBirth_date(Date birth_date) {
        this.birth_date = birth_date;
    }

    public String getClass_name() {
        return class_name;
    }

    public void setClass_name(String class_name) {
        this.class_name = class_name;
    }

    public int getClass_name_id() {
        return class_name_id;
    }

    public void setClass_name_id(int class_name_id) {
        this.class_name_id = class_name_id;
    }

    public int getEdu_status_id() {
        return edu_status_id;
    }

    public void setEdu_status_id(int edu_status_id) {
        this.edu_status_id = edu_status_id;
    }

    public String getPhoto() {
        return photo;
    }

    public void setPhoto(String photo) {
        this.photo = photo;
    }

    public String getSchool() {
        return school;
    }

    public void setSchool(String school) {
        this.school = school;
    }

    public int getSchool_id() {
        return school_id;
    }

    public void setSchool_id(int school_id) {
        this.school_id = school_id;
    }

    public int getEntering_year_id() {
        return entering_year_id;
    }

    public void setEntering_year_id(int entering_year_id) {
        this.entering_year_id = entering_year_id;
    }

    public int getEmployee_id() {
        return employee_id;
    }

    public void setEmployee_id(int employee_id) {
        this.employee_id = employee_id;
    }

}
