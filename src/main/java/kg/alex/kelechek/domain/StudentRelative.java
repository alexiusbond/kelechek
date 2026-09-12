
package kg.alex.kelechek.domain;

import java.io.Serializable;
import java.util.Date;

public class StudentRelative implements Serializable {

    private String id;
    private int student_id;
    private String fullName;
    private String phone;
    private String address;
    private String work_place;
    private String passport;
    private String passport_issue_place;
    private Date passport_issue_date;
    private int is_main;
    private int relative_id;
    private String relativeDeclarative;
    private String relativeTitle;
    private int gender_id;


    public int getGender_id() {
        return gender_id;
    }

    public void setGender_id(int gender_id) {
        this.gender_id = gender_id;
    }

    public String getRelativeTitle() {
        return relativeTitle;
    }

    public void setRelativeTitle(String relativeTitle) {
        this.relativeTitle = relativeTitle;
    }

    public String getRelativeDeclarative() {
        return relativeDeclarative;
    }

    public void setRelativeDeclarative(String relativeDeclarative) {
        this.relativeDeclarative = relativeDeclarative;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getStudent_id() {
        return student_id;
    }

    public void setStudent_id(int student_id) {
        this.student_id = student_id;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public int getIs_main() {
        return is_main;
    }

    public void setIs_main(int is_main) {
        this.is_main = is_main;
    }

    public int getRelative_id() {
        return relative_id;
    }

    public void setRelative_id(int relative_id) {
        this.relative_id = relative_id;
    }

    public String getWork_place() {
        return work_place;
    }

    public void setWork_place(String work_place) {
        this.work_place = work_place;
    }

    public String getPassport() {
        return passport;
    }

    public void setPassport(String passport) {
        this.passport = passport;
    }

    public String getPassport_issue_place() {
        return passport_issue_place;
    }

    public void setPassport_issue_place(String passport_issue_place) {
        this.passport_issue_place = passport_issue_place;
    }

    public Date getPassport_issue_date() {
        return passport_issue_date;
    }

    public void setPassport_issue_date(Date passport_issue_date) {
        this.passport_issue_date = passport_issue_date;
    }
}