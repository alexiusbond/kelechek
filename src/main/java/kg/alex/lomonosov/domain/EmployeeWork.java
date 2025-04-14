/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package kg.alex.lomonosov.domain;

import java.io.Serializable;
import java.util.Date;
import java.util.Set;

public class EmployeeWork implements Serializable {

    boolean isSapat;
    private int id;
    private int employee_id;
    private int own_id;
    private int working_status_id;
    private int work_place_id;
    private int main_position_id;
    private Set<?> extra_position_ids;
    private Date start;
    private Date end;

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

    public int getOwn_id() {
        return own_id;
    }

    public void setOwn_id(int own_id) {
        this.own_id = own_id;
    }

    public int getWorking_status_id() {
        return working_status_id;
    }

    public void setWorking_status_id(int working_status_id) {
        this.working_status_id = working_status_id;
    }

    public int getWork_place_id() {
        return work_place_id;
    }

    public void setWork_place_id(int work_place_id) {
        this.work_place_id = work_place_id;
    }

    public int getMain_position_id() {
        return main_position_id;
    }

    public void setMain_position_id(int main_position_id) {
        this.main_position_id = main_position_id;
    }

    public Set<?> getExtra_position_ids() {
        return extra_position_ids;
    }

    public void setExtra_position_ids(Set<?> extra_position_ids) {
        this.extra_position_ids = extra_position_ids;
    }

    public boolean isSapat() {
        return isSapat;
    }

    public void setSapat(boolean sapat) {
        isSapat = sapat;
    }

    public Date getStart() {
        return start;
    }

    public void setStart(Date start) {
        this.start = start;
    }

    public Date getEnd() {
        return end;
    }

    public void setEnd(Date end) {
        this.end = end;
    }
}
