/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package kg.alex.star.domain;

import java.io.Serializable;
import java.util.Date;

public class StudentRelative implements Serializable {

    private String id;
    private int student_id;
    private String fullName;
    private String givenBy;
    private Date issueDate;
    private String phone;
    private String address;
    private String passport;
    private String inn;
    private int is_main;
    private int relative_id;
    private String relativeDeclarative;
    private String relativeTitle;
    private int gender_id;
    private int attachment_id;
    private String attachmentUniqueName;

    public String getInn() {
        return inn;
    }

    public void setInn(String inn) {
        this.inn = inn;
    }

    public int getAttachment_id() {
        return attachment_id;
    }

    public void setAttachment_id(int attachment_id) {
        this.attachment_id = attachment_id;
    }

    public String getAttachmentUniqueName() {
        return attachmentUniqueName;
    }

    public void setAttachmentUniqueName(String attachmentUniqueName) {
        this.attachmentUniqueName = attachmentUniqueName;
    }

    public Date getIssueDate() {
        return issueDate;
    }

    public void setIssueDate(Date issueDate) {
        this.issueDate = issueDate;
    }

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

    public String getPassport() {
        return passport;
    }

    public void setPassport(String passport) {
        this.passport = passport;
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

    public String getGivenBy() {
        return givenBy;
    }

    public void setGivenBy(String givenBy) {
        this.givenBy = givenBy;
    }
}
