/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package kg.alex.lomonosov.domain;

import java.io.Serializable;

public class StudentInfoPdf implements Serializable {

    private Year year;
    private Employee director;
    private Employee accountant;
    private StudentRelative relative;
    private School school;
    private Student student;
    private ContractInfo contractInfo;

    public Year getYear() {
        return year;
    }

    public void setYear(Year year) {
        this.year = year;
    }

    public ContractInfo getContractInfo() {
        return contractInfo;
    }

    public void setContractInfo(ContractInfo contractInfo) {
        this.contractInfo = contractInfo;
    }

    public Student getStudent() {
        return student;
    }

    public void setStudent(Student student) {
        this.student = student;
    }

    public StudentRelative getRelative() {
        return relative;
    }

    public void setRelative(StudentRelative relative) {
        this.relative = relative;
    }

    public Employee getAccountant() {
        return accountant;
    }

    public void setAccountant(Employee accountant) {
        this.accountant = accountant;
    }

    public Employee getDirector() {
        return director;
    }

    public void setDirector(Employee director) {
        this.director = director;
    }

    public School getSchool() {
        return school;
    }

    public void setSchool(School school) {
        this.school = school;
    }
}
