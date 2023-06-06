/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package kg.alex.indigo.domain;

import java.io.Serializable;

public class StudentInfoPdf implements Serializable {

    private String period;
    private String period_kg;
    private String year;
    private Employee director;
    private Employee accountant;
    private StudentRelative relative;
    private School school;
    private Student student;
    private ContractInfo contractInfo;

    public String getYear() {
        return year;
    }

    public void setYear(String year) {
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

    public String getPeriod_kg() {
        return period_kg;
    }

    public void setPeriod_kg(String period_kg) {
        this.period_kg = period_kg;
    }

    public String getPeriod() {
        return period;
    }

    public void setPeriod(String period) {
        this.period = period;
    }

    public School getSchool() {
        return school;
    }

    public void setSchool(School school) {
        this.school = school;
    }
}
