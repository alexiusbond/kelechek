/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package kg.alex.abl.domain;

import java.io.Serializable;
import java.util.Date;

public class EmployeeCertificate implements Serializable {

    private int id;
    private int employee_id;
    private int certificate_id;
    private String note;
    private String given_by;
    private Date date_of_issue;
    private String idStr;
    private int attachment_id;
    private String attachmentUniqueName;

    public String getAttachmentUniqueName() {
        return attachmentUniqueName;
    }

    public void setAttachmentUniqueName(String attachmentUniqueName) {
        this.attachmentUniqueName = attachmentUniqueName;
    }

    public String getIdStr() {
        return idStr;
    }

    public void setIdStr(String idStr) {
        this.idStr = idStr;
    }

    public int getAttachment_id() {
        return attachment_id;
    }

    public void setAttachment_id(int attachment_id) {
        this.attachment_id = attachment_id;
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

    public int getCertificate_id() {
        return certificate_id;
    }

    public void setCertificate_id(int certificate_id) {
        this.certificate_id = certificate_id;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public String getGiven_by() {
        return given_by;
    }

    public void setGiven_by(String given_by) {
        this.given_by = given_by;
    }

    public Date getDate_of_issue() {
        return date_of_issue;
    }

    public void setDate_of_issue(Date date_of_issue) {
        this.date_of_issue = date_of_issue;
    }
}
