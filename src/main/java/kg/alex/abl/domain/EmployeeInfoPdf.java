/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package kg.alex.abl.domain;

import java.io.Serializable;

public class EmployeeInfoPdf implements Serializable {
    private String employeeName;
    private String employeeSurname;
    private String employeeMiddleName;
    private String employeeBranch;
    private String employeePosition;
    private EmployeeContact contact;
    private boolean isEmployeeFemininity;
    private School school;
    private Employee director;
    private EmployeeContract contract;

    public String getEmployeePosition() {
        return employeePosition;
    }

    public void setEmployeePosition(String employeePosition) {
        this.employeePosition = employeePosition;
    }

    public EmployeeContact getContact() {
        return contact;
    }

    public void setContact(EmployeeContact contact) {
        this.contact = contact;
    }

    public String getEmployeeBranch() {
        return employeeBranch;
    }

    public void setEmployeeBranch(String employeeBranch) {
        this.employeeBranch = employeeBranch;
    }

    public Employee getDirector() {
        return director;
    }

    public void setDirector(Employee director) {
        this.director = director;
    }

    public String getEmployeeName() {
        return employeeName;
    }

    public void setEmployeeName(String employeeName) {
        this.employeeName = employeeName;
    }

    public String getEmployeeSurname() {
        return employeeSurname;
    }

    public void setEmployeeSurname(String employeeSurname) {
        this.employeeSurname = employeeSurname;
    }

    public String getEmployeeMiddleName() {
        return employeeMiddleName;
    }

    public void setEmployeeMiddleName(String employeeMiddleName) {
        this.employeeMiddleName = employeeMiddleName;
    }

    public School getSchool() {
        return school;
    }

    public void setSchool(School school) {
        this.school = school;
    }

    public boolean isEmployeeFemininity() {
        return isEmployeeFemininity;
    }

    public void setEmployeeFemininity(boolean employeeFemininity) {
        isEmployeeFemininity = employeeFemininity;
    }

    public EmployeeContract getContract() {
        return contract;
    }

    public void setContract(EmployeeContract contract) {
        this.contract = contract;
    }
}
