/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package kg.alex.indigo.domain;

import kg.alex.indigo.excel.ExcelColumn;

import java.io.Serializable;

public class StudentExcel implements Serializable {

    private int id;
    @ExcelColumn("Id")
    private String login;
    private String password;
    @ExcelColumn("Name")
    private String name;
    @ExcelColumn("Surname")
    private String sur_name;
    @ExcelColumn("Middle Name")
    private String middle_name;
    @ExcelColumn("Gender")
    private String gender;
    private int gender_id;
    @ExcelColumn("Date of Birth")
    private String birth_date;
    @ExcelColumn("Class")
    private String class_name;
    private int class_name_id;
    private int edu_status_id;
    private int school_id;
    private int entering_year_id;
    private int employee_id;

    private int relative_type_id;
    @ExcelColumn("Relative")
    private String relative_type;
    @ExcelColumn("Fullname (Relative)")
    private String relative_fullname;
    @ExcelColumn("Passport (Relative)")
    private String relative_passport;
    @ExcelColumn("Work Place (Relative)")
    private String relative_work_place;
    @ExcelColumn("Phone (Relative)")
    private String relative_phone;
    @ExcelColumn("Address (Relative)")
    private String relative_address;

    /**
     * @return the id
     */
    public int getId() {
        return id;
    }

    /**
     * @param id the id to set
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * @return the login
     */
    public String getLogin() {
        return login;
    }

    /**
     * @param login the login to set
     */
    public void setLogin(String login) {
        this.login = login;
    }

    /**
     * @return the password
     */
    public String getPassword() {
        return password;
    }

    /**
     * @param password the password to set
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return the sur_name
     */
    public String getSur_name() {
        return sur_name;
    }

    /**
     * @param sur_name the sur_name to set
     */
    public void setSur_name(String sur_name) {
        this.sur_name = sur_name;
    }

    /**
     * @return the middle_name
     */
    public String getMiddle_name() {
        return middle_name;
    }

    /**
     * @param middle_name the middle_name to set
     */
    public void setMiddle_name(String middle_name) {
        this.middle_name = middle_name;
    }

    /**
     * @return the gender
     */
    public String getGender() {
        return gender;
    }

    /**
     * @param gender the gender to set
     */
    public void setGender(String gender) {
        this.gender = gender;
    }

    /**
     * @return the gender_id
     */
    public int getGender_id() {
        return gender_id;
    }

    /**
     * @param gender_id the gender_id to set
     */
    public void setGender_id(int gender_id) {
        this.gender_id = gender_id;
    }

    /**
     * @return the class_name
     */
    public String getClass_name() {
        return class_name;
    }

    /**
     * @param class_name the class_name to set
     */
    public void setClass_name(String class_name) {
        this.class_name = class_name;
    }

    /**
     * @return the class_name_id
     */
    public int getClass_name_id() {
        return class_name_id;
    }

    /**
     * @param class_name_id the class_name_id to set
     */
    public void setClass_name_id(int class_name_id) {
        this.class_name_id = class_name_id;
    }

    /**
     * @return the edu_status_id
     */
    public int getEdu_status_id() {
        return edu_status_id;
    }

    /**
     * @param edu_status_id the edu_status_id to set
     */
    public void setEdu_status_id(int edu_status_id) {
        this.edu_status_id = edu_status_id;
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
     * @return the entering_year_id
     */
    public int getEntering_year_id() {
        return entering_year_id;
    }

    /**
     * @param entering_year_id the entering_year_id to set
     */
    public void setEntering_year_id(int entering_year_id) {
        this.entering_year_id = entering_year_id;
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

    /**
     * @return the relative_type
     */
    public String getRelative_type() {
        return relative_type;
    }

    /**
     * @param relative_type the relative_type to set
     */
    public void setRelative_type(String relative_type) {
        this.relative_type = relative_type;
    }

    /**
     * @return the relative_fullname
     */
    public String getRelative_fullname() {
        return relative_fullname;
    }

    /**
     * @param relative_fullname the relative_fullname to set
     */
    public void setRelative_fullname(String relative_fullname) {
        this.relative_fullname = relative_fullname;
    }

    /**
     * @return the relative_passport
     */
    public String getRelative_passport() {
        return relative_passport;
    }

    /**
     * @param relative_passport the relative_passport to set
     */
    public void setRelative_passport(String relative_passport) {
        this.relative_passport = relative_passport;
    }

    /**
     * @return the relative_work_place
     */
    public String getRelative_work_place() {
        return relative_work_place;
    }

    /**
     * @param relative_work_place the relative_work_place to set
     */
    public void setRelative_work_place(String relative_work_place) {
        this.relative_work_place = relative_work_place;
    }

    /**
     * @return the relative_phone
     */
    public String getRelative_phone() {
        return relative_phone;
    }

    /**
     * @param relative_phone the relative_phone to set
     */
    public void setRelative_phone(String relative_phone) {
        this.relative_phone = relative_phone;
    }

    /**
     * @return the relative_address
     */
    public String getRelative_address() {
        return relative_address;
    }

    /**
     * @param relative_address the relative_address to set
     */
    public void setRelative_address(String relative_address) {
        this.relative_address = relative_address;
    }

    /**
     * @return the birth_date
     */
    public String getBirth_date() {
        return birth_date;
    }

    /**
     * @param birth_date the birth_date to set
     */
    public void setBirth_date(String birth_date) {
        this.birth_date = birth_date;
    }

    /**
     * @return the relative_type_id
     */
    public int getRelative_type_id() {
        return relative_type_id;
    }

    /**
     * @param relative_type_id the relative_type_id to set
     */
    public void setRelative_type_id(int relative_type_id) {
        this.relative_type_id = relative_type_id;
    }

}
