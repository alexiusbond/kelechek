/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package kg.alex.abl.domain;

import java.io.Serializable;
import java.util.Date;

public class EmployeeContract implements Serializable {

    private int id;
    private int employee_id;
    private int contract_type_id;
    private String salary;
    private Date fromDate;
    private Date tillDate;
    private Date creationDate;
    private int salaryDay;
    private int yearId;
    private String year;
    private int probationaryPeriod;
    private int workingDays;
    private int workingHours;
    private String patent;
    private String equipment;
    private Date patentDate;

    public Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    public String getYear() {
        return year;
    }

    public void setYear(String year) {
        this.year = year;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getEmployee_id() {
        return employee_id;
    }

    public void setEmployee_id(int employee_id) {
        this.employee_id = employee_id;
    }

    public int getContract_type_id() {
        return contract_type_id;
    }

    public void setContract_type_id(int contract_type_id) {
        this.contract_type_id = contract_type_id;
    }

    public String getSalary() {
        return salary;
    }

    public void setSalary(String salary) {
        this.salary = salary;
    }

    public Date getFromDate() {
        return fromDate;
    }

    public void setFromDate(Date fromDate) {
        this.fromDate = fromDate;
    }

    public Date getTillDate() {
        return tillDate;
    }

    public void setTillDate(Date tillDate) {
        this.tillDate = tillDate;
    }

    public int getSalaryDay() {
        return salaryDay;
    }

    public void setSalaryDay(int salaryDay) {
        this.salaryDay = salaryDay;
    }

    public int getYearId() {
        return yearId;
    }

    public void setYearId(int yearId) {
        this.yearId = yearId;
    }

    public int getProbationaryPeriod() {
        return probationaryPeriod;
    }

    public void setProbationaryPeriod(int probationaryPeriod) {
        this.probationaryPeriod = probationaryPeriod;
    }

    public int getWorkingDays() {
        return workingDays;
    }

    public void setWorkingDays(int workingDays) {
        this.workingDays = workingDays;
    }

    public int getWorkingHours() {
        return workingHours;
    }

    public void setWorkingHours(int workingHours) {
        this.workingHours = workingHours;
    }

    public String getPatent() {
        return patent;
    }

    public void setPatent(String patent) {
        this.patent = patent;
    }

    public String getEquipment() {
        return equipment;
    }

    public void setEquipment(String equipment) {
        this.equipment = equipment;
    }

    public Date getPatentDate() {
        return patentDate;
    }

    public void setPatentDate(Date patentDate) {
        this.patentDate = patentDate;
    }
}
