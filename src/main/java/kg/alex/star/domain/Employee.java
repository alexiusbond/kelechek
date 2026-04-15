/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package kg.alex.star.domain;

import java.io.Serializable;
import java.util.Date;

public class Employee implements Serializable {

    private int id;
    private String login;
    private String password;
    private String name;
    private String surname;
    private String middle_name;
    private Date birth_date;
    private int gender_id;
    private int nationality_id;
    private int martial_status_id;
    private String photo;
    private int modified_by_id;
    private int citizenship_id;

    public int getCitizenship_id() {
        return citizenship_id;
    }

    public void setCitizenship_id(int citizenship_id) {
        this.citizenship_id = citizenship_id;
    }

    /**
     * @return the id
     */
    public int getId() {
        return id;
    }

    /**
     * @param id the id to set
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * @return the login
     */
    public String getLogin() {
        return login;
    }

    /**
     * @param login the login to set
     */
    public void setLogin(String login) {
        this.login = login;
    }

    /**
     * @return the password
     */
    public String getPassword() {
        return password;
    }

    /**
     * @param password the password to set
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return the surname
     */
    public String getSurname() {
        return surname;
    }

    /**
     * @param surname the surname to set
     */
    public void setSurname(String surname) {
        this.surname = surname;
    }

    /**
     * @return the middle_name
     */
    public String getMiddle_name() {
        return middle_name;
    }

    /**
     * @param middle_name the middle_name to set
     */
    public void setMiddle_name(String middle_name) {
        this.middle_name = middle_name;
    }

    /**
     * @return the birth_date
     */
    public Date getBirth_date() {
        return birth_date;
    }

    /**
     * @param birth_date the birth_date to set
     */
    public void setBirth_date(Date birth_date) {
        this.birth_date = birth_date;
    }

    /**
     * @return the gender_id
     */
    public int getGender_id() {
        return gender_id;
    }

    /**
     * @param gender_id the gender_id to set
     */
    public void setGender_id(int gender_id) {
        this.gender_id = gender_id;
    }

    /**
     * @return the nationality_id
     */
    public int getNationality_id() {
        return nationality_id;
    }

    /**
     * @param nationality_id the nationality_id to set
     */
    public void setNationality_id(int nationality_id) {
        this.nationality_id = nationality_id;
    }

    /**
     * @return the martial_status_id
     */
    public int getMartial_status_id() {
        return martial_status_id;
    }

    /**
     * @param martial_status_id the martial_status_id to set
     */
    public void setMartial_status_id(int martial_status_id) {
        this.martial_status_id = martial_status_id;
    }

    /**
     * @return the photo
     */
    public String getPhoto() {
        return photo;
    }

    /**
     * @param photo the photo to set
     */
    public void setPhoto(String photo) {
        this.photo = photo;
    }

    /**
     * @return the modified_by_id
     */
    public int getModified_by_id() {
        return modified_by_id;
    }

    /**
     * @param modified_by_id the modified_by_id to set
     */
    public void setModified_by_id(int modified_by_id) {
        this.modified_by_id = modified_by_id;
    }

}
