/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package kg.alex.abl.domain;

import java.io.Serializable;
import java.util.Date;

public class EmployeeExtraInfo implements Serializable {

    private int employee_id;
    private int health_status_id;
    private String hobbies;
    private String phobias;
    private String short_notes;
    private String health_notes;
    private String mainBranch;
    private String extraBranches;
    private String mainPosition;
    private String extraPositions;
    private String workExperience;
    private String workExperienceSapat;
    private int hours;
    private int extraHours;
    private String workingStatus;
    private String canBeAdvisor;
    private String salaryCategory;
    private String nationality;
    private String citizenship;
    private String gender;
    private String martialStatus;
    private String school;
    private String languages;
    private String phones;
    private String address;
    private String email;
    private String birth_place;
    private String familyInfo;
    private int children;
    private Date modificationDate;

    public String getWorkExperienceSapat() {
        return workExperienceSapat;
    }

    public void setWorkExperienceSapat(String workExperienceSapat) {
        this.workExperienceSapat = workExperienceSapat;
    }

    public String getWorkExperience() {
        return workExperience;
    }

    public void setWorkExperience(String workExperience) {
        this.workExperience = workExperience;
    }

    public Date getModificationDate() {
        return modificationDate;
    }

    public void setModificationDate(Date modificationDate) {
        this.modificationDate = modificationDate;
    }

    public int getChildren() {
        return children;
    }

    public void setChildren(int children) {
        this.children = children;
    }

    public String getFamilyInfo() {
        return familyInfo;
    }

    public void setFamilyInfo(String familyInfo) {
        this.familyInfo = familyInfo;
    }

    public String getPhones() {
        if (phones != null) {
            return phones;
        } else {
            return "";
        }
    }

    public void setPhones(String phones) {
        this.phones = phones;
    }

    public String getAddress() {
        if (address != null) {
            return address;
        } else {
            return "";
        }
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getEmail() {
        if (email != null) {
            return email;
        } else {
            return "";
        }
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getBirth_place() {
        return birth_place;
    }

    public void setBirth_place(String birth_place) {
        this.birth_place = birth_place;
    }

    public String getLanguages() {
        if (languages != null) {
            return languages;
        } else {
            return "";
        }
    }

    public void setLanguages(String languages) {
        this.languages = languages;
    }

    public String getSchool() {
        return school;
    }

    public void setSchool(String school) {
        this.school = school;
    }

    public String getSalaryCategory() {
        return salaryCategory;
    }

    public void setSalaryCategory(String salaryCategory) {
        this.salaryCategory = salaryCategory;
    }

    public String getNationality() {
        return nationality;
    }

    public void setNationality(String nationality) {
        this.nationality = nationality;
    }

    public String getCitizenship() {
        return citizenship;
    }

    public void setCitizenship(String citizenship) {
        this.citizenship = citizenship;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getMartialStatus() {
        return martialStatus;
    }

    public void setMartialStatus(String martialStatus) {
        this.martialStatus = martialStatus;
    }

    public int getEmployee_id() {
        return employee_id;
    }

    public void setEmployee_id(int employee_id) {
        this.employee_id = employee_id;
    }

    public int getHealth_status_id() {
        return health_status_id;
    }

    public void setHealth_status_id(int health_status_id) {
        this.health_status_id = health_status_id;
    }

    public String getHobbies() {
        return hobbies;
    }

    public void setHobbies(String hobbies) {
        this.hobbies = hobbies;
    }

    public String getPhobias() {
        return phobias;
    }

    public void setPhobias(String phobias) {
        this.phobias = phobias;
    }

    public String getShort_notes() {
        return short_notes;
    }

    public void setShort_notes(String short_notes) {
        this.short_notes = short_notes;
    }

    public String getHealth_notes() {
        return health_notes;
    }

    public void setHealth_notes(String health_notes) {
        this.health_notes = health_notes;
    }

    public String getMainBranch() {
        if (mainBranch != null) {
            return mainBranch;
        } else {
            return "";
        }
    }

    public void setMainBranch(String mainBranch) {
        this.mainBranch = mainBranch;
    }

    public String getExtraBranches() {
        if (extraBranches != null) {
            return extraBranches;
        } else {
            return "";
        }
    }

    public void setExtraBranches(String extraBranches) {
        this.extraBranches = extraBranches;
    }

    public String getMainPosition() {
        if (mainPosition != null) {
            return mainPosition;
        } else {
            return "";
        }
    }

    public void setMainPosition(String mainPosition) {
        this.mainPosition = mainPosition;
    }

    public String getExtraPositions() {
        if (extraPositions != null) {
            return extraPositions;
        } else {
            return "";
        }
    }

    public void setExtraPositions(String extraPositions) {
        this.extraPositions = extraPositions;
    }

    public int getHours() {
        return hours;
    }

    public void setHours(int hours) {
        this.hours = hours;
    }

    public int getExtraHours() {
        return extraHours;
    }

    public void setExtraHours(int extraHours) {
        this.extraHours = extraHours;
    }

    public String getWorkingStatus() {
        if (workingStatus != null) {
            return workingStatus;
        } else {
            return "";
        }
    }

    public void setWorkingStatus(String workingStatus) {
        this.workingStatus = workingStatus;
    }

    public String getCanBeAdvisor() {
        return canBeAdvisor;
    }

    public void setCanBeAdvisor(String canBeAdvisor) {
        this.canBeAdvisor = canBeAdvisor;
    }
}
