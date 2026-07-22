/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package kg.alex.abl.domain;

import java.io.Serializable;
import java.util.Date;

public class StudentContract implements Serializable {

    private int student_id;
    private int year_id;
    private int contract_id;
    private int installmentPlanTypeId;
    private Double debt;
    private int employee_id;
    private int status_id;
    private Double amount;
    private double plan_debt;
    private double correction;
    private double contr_with_disc;
    private String correctionDetails;
    private Date creationDate;

    public int getInstallmentPlanTypeId() {
        return installmentPlanTypeId;
    }

    public void setInstallmentPlanTypeId(int installmentPlanTypeId) {
        this.installmentPlanTypeId = installmentPlanTypeId;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    public double getCorrection() {
        return correction;
    }

    public void setCorrection(double correction) {
        this.correction = correction;
    }

    public String getCorrectionDetails() {
        return correctionDetails;
    }

    public void setCorrectionDetails(String correctionDetails) {
        this.correctionDetails = correctionDetails;
    }

    public double getContr_with_disc() {
        return contr_with_disc;
    }

    public void setContr_with_disc(double contr_with_disc) {
        this.contr_with_disc = contr_with_disc;
    }

    public double getPlan_debt() {
        return plan_debt;
    }

    public void setPlan_debt(double plan_debt) {
        this.plan_debt = plan_debt;
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double value) {
        this.amount = value;
    }

    public int getStudent_id() {
        return student_id;
    }

    public void setStudent_id(int student_id) {
        this.student_id = student_id;
    }

    public int getYear_id() {
        return year_id;
    }

    public void setYear_id(int year_id) {
        this.year_id = year_id;
    }

    public int getContract_id() {
        return contract_id;
    }

    public void setContract_id(int contract_id) {
        this.contract_id = contract_id;
    }

    public Double getDebt() {
        return debt;
    }

    public void setDebt(Double debt) {
        this.debt = debt;
    }

    public int getEmployee_id() {
        return employee_id;
    }

    public void setEmployee_id(int employee_id) {
        this.employee_id = employee_id;
    }

    public int getStatus_id() {
        return status_id;
    }

    public void setStatus_id(int status_id) {
        this.status_id = status_id;
    }

}
