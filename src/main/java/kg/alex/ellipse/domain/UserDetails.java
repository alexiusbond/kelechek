/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package kg.alex.ellipse.domain;

import java.io.Serializable;
import java.util.Date;

/**
 * @author alex
 */
public class UserDetails implements Serializable {

    private int id;
    private String login;
    private int working_status_id;
    private int position_id;
    private String fullName;
    private int branch_id;
    private Year current_year;
    private Date transactions_start_date;
    private boolean isUnreadMessages;
    private School school;

    public School getSchool() {
        return school;
    }

    public void setSchool(School school) {
        this.school = school;
    }

    public int getBranch_id() {
        return branch_id;
    }

    public void setBranch_id(int branch_id) {
        this.branch_id = branch_id;
    }

    public Date getTransactions_start_date() {
        return transactions_start_date;
    }

    public void setTransactions_start_date(Date transactions_start_date) {
        this.transactions_start_date = transactions_start_date;
    }

    public boolean isUnreadMessages() {
        return isUnreadMessages;
    }

    public void setUnreadMessages(boolean unreadMessages) {
        isUnreadMessages = unreadMessages;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public int getWorking_status_id() {
        return working_status_id;
    }

    public void setWorking_status_id(int working_status_id) {
        this.working_status_id = working_status_id;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public Year getCurrent_year() {
        return current_year;
    }

    public void setCurrent_year(Year current_year) {
        this.current_year = current_year;
    }

    public int getPosition_id() {
        return position_id;
    }

    public void setPosition_id(int position_id) {
        this.position_id = position_id;
    }
}
