/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package kg.alex.ispa.domain;

import java.io.Serializable;
import java.util.List;

public class StudentInfoPdf implements Serializable {

    private Year year;
    private Employee director;
    private Employee accountant;
    private StudentRelative mainRelative;
    private List<StudentRelative> relatives;
    private School school;
    private Student student;
    private ContractInfo contractInfo;

    public List<StudentRelative> getRelatives() {
        return relatives;
    }

    public void setRelatives(List<StudentRelative> relatives) {
        this.relatives = relatives;
    }

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

    public StudentRelative getMainRelative() {
        return mainRelative;
    }

    public void setMainRelative(StudentRelative mainRelative) {
        this.mainRelative = mainRelative;
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
