/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package kg.alex.ellipse.domain;

import java.io.Serializable;
import java.util.Date;

public class EmployeeChildren implements Serializable {

    private int id;
    private int employee_id;
    private int education_status_id;
    private int health_status_id;
    private String fullName;
    private String institution;
    private Date date_of_birth;

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

    public int getEducation_status_id() {
        return education_status_id;
    }

    public void setEducation_status_id(int education_status_id) {
        this.education_status_id = education_status_id;
    }

    public int getHealth_status_id() {
        return health_status_id;
    }

    public void setHealth_status_id(int health_status_id) {
        this.health_status_id = health_status_id;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getInstitution() {
        return institution;
    }

    public void setInstitution(String institution) {
        this.institution = institution;
    }

    public Date getDate_of_birth() {
        return date_of_birth;
    }

    public void setDate_of_birth(Date date_of_birth) {
        this.date_of_birth = date_of_birth;
    }
}
