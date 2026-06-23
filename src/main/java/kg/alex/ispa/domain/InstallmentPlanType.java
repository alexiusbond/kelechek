package kg.alex.ispa.domain;

import java.io.Serializable;

public class InstallmentPlanType implements Serializable {

    private int id;
    private String code;
    private String name;

    private Integer payment_count;
    private Integer payment_interval_months;
    private Integer start_month;
    private Integer due_day;

    private boolean divide_equal;
    private boolean is_custom;

    private String description;

    public InstallmentPlanType() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }


    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }


    public Integer getPayment_count() {
        return payment_count;
    }

    public void setPayment_count(Integer payment_count) {
        this.payment_count = payment_count;
    }


    public Integer getPayment_interval_months() {
        return payment_interval_months;
    }

    public void setPayment_interval_months(Integer payment_interval_months) {
        this.payment_interval_months = payment_interval_months;
    }


    public Integer getStart_month() {
        return start_month;
    }

    public void setStart_month(Integer start_month) {
        this.start_month = start_month;
    }


    public Integer getDue_day() {
        return due_day;
    }

    public void setDue_day(Integer due_day) {
        this.due_day = due_day;
    }


    public boolean isDivide_equal() {
        return divide_equal;
    }

    public void setDivide_equal(boolean divide_equal) {
        this.divide_equal = divide_equal;
    }


    public boolean isIs_custom() {
        return is_custom;
    }

    public void setIs_custom(boolean is_custom) {
        this.is_custom = is_custom;
    }


    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}