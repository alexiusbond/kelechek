/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package kg.alex.aim.domain;

import kg.alex.aim.excel.ExcelColumn;

import java.io.Serializable;

public class EmployeeBranchesExcel implements Serializable {

    @ExcelColumn("ID Преподавателя")
    private String login;
    @ExcelColumn("Код урока")
    private String course_code;
    @ExcelColumn("Часы")
    private String hours;
    @ExcelColumn("Доп. Часы")
    private String extra_hours;
    @ExcelColumn("Класс")
    private String class_number;

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getCourse_code() {
        return course_code;
    }

    public String getHours() {
        return hours;
    }

    public void setHours(String hours) {
        this.hours = hours;
    }

    public String getExtra_hours() {
        return extra_hours;
    }

    public void setExtra_hours(String extra_hours) {
        this.extra_hours = extra_hours;
    }

    public String getClass_number() {
        return class_number;
    }

    public void setClass_number(String class_number) {
        this.class_number = class_number;
    }
}
