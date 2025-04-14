/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package kg.alex.lomonosov.domain;

public class StockInvoice extends Invoice {

    private int stock_id;
    private String invoiceNumberStr;
    private int from_employee_id;
    private int to_employee_id;
    private String from_employee;
    private String to_employee;
    private String stock;
    private String acc_category;
    private int service_type_id;
    private int acc_category_id;

    public int getStock_id() {
        return stock_id;
    }

    public void setStock_id(int stock_id) {
        this.stock_id = stock_id;
    }

    public String getInvoiceNumberStr() {
        return invoiceNumberStr;
    }

    public void setInvoiceNumberStr(String invoiceNumberStr) {
        this.invoiceNumberStr = invoiceNumberStr;
    }

    public int getFrom_employee_id() {
        return from_employee_id;
    }

    public void setFrom_employee_id(int from_employee_id) {
        this.from_employee_id = from_employee_id;
    }

    public int getTo_employee_id() {
        return to_employee_id;
    }

    public void setTo_employee_id(int to_employee_id) {
        this.to_employee_id = to_employee_id;
    }

    public String getFrom_employee() {
        return from_employee;
    }

    public void setFrom_employee(String from_employee) {
        this.from_employee = from_employee;
    }

    public String getTo_employee() {
        return to_employee;
    }

    public void setTo_employee(String to_employee) {
        this.to_employee = to_employee;
    }

    public String getStock() {
        return stock;
    }

    public void setStock(String stock) {
        this.stock = stock;
    }

    public String getAcc_category() {
        return acc_category;
    }

    public void setAcc_category(String acc_category) {
        this.acc_category = acc_category;
    }

    public int getService_type_id() {
        return service_type_id;
    }

    public void setService_type_id(int service_type_id) {
        this.service_type_id = service_type_id;
    }

    public int getAcc_category_id() {
        return acc_category_id;
    }

    public void setAcc_category_id(int acc_category_id) {
        this.acc_category_id = acc_category_id;
    }
}
