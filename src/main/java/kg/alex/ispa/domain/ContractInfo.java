package kg.alex.ispa.domain;

import java.io.Serializable;
import java.util.Date;

public class ContractInfo implements Serializable {

    private Double contract;
    private String contractTitle;
    private Double debt;
    private Double installmentPlanDebt;
    private Double discount;
    private String currency;
    private String discountStr;
    private Double correction;
    private String correctionStr;
    private Double paid;
    private String totalPayments;
    private String totalInstallments;
    private Double left;
    private Double initialPayment;
    private Double net;
    private int students;
    private int contractNumber;
    private Date creationDate;

    public Double getInitialPayment() {
        return initialPayment;
    }

    public void setInitialPayment(Double initialPayment) {
        this.initialPayment = initialPayment;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getTotalPayments() {
        return totalPayments;
    }

    public void setTotalPayments(String totalPayments) {
        this.totalPayments = totalPayments;
    }

    public String getTotalInstallments() {
        return totalInstallments;
    }

    public void setTotalInstallments(String totalInstallments) {
        this.totalInstallments = totalInstallments;
    }

    public Double getInstallmentPlanDebt() {
        return installmentPlanDebt;
    }

    public void setInstallmentPlanDebt(Double installmentPlanDebt) {
        this.installmentPlanDebt = installmentPlanDebt;
    }

    public Double getDiscount() {
        return discount;
    }

    public void setDiscount(Double discount) {
        this.discount = discount;
    }


    public String getCorrectionStr() {
        return correctionStr;
    }

    public void setCorrectionStr(String correctionStr) {
        this.correctionStr = correctionStr;
    }

    public String getDiscountStr() {
        return discountStr;
    }

    public void setDiscountStr(String discountStr) {
        this.discountStr = discountStr;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    public int getContractNumber() {
        return contractNumber;
    }

    public void setContractNumber(int contractNumber) {
        this.contractNumber = contractNumber;
    }

    public Double getCorrection() {
        return correction;
    }

    public void setCorrection(Double correction) {
        this.correction = correction;
    }

    public Double getNet() {
        return net;
    }

    public void setNet(Double net) {
        this.net = net;
    }

    public int getStudents() {
        return students;
    }

    public void setStudents(int students) {
        this.students = students;
    }

    public Double getContract() {
        return contract;
    }

    public void setContract(Double contract) {
        this.contract = contract;
    }

    public Double getDebt() {
        return debt;
    }

    public void setDebt(Double debt) {
        this.debt = debt;
    }

    public Double getPaid() {
        return paid;
    }

    public void setPaid(Double paid) {
        this.paid = paid;
    }

    public Double getLeft() {
        return left;
    }

    public void setLeft(Double left) {
        this.left = left;
    }

    public String getContractTitle() {
        return contractTitle;
    }

    public void setContractTitle(String contractTitle) {
        this.contractTitle = contractTitle;
    }
}
