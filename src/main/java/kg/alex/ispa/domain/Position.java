/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package kg.alex.ispa.domain;

import java.io.Serializable;

public class Position implements Serializable {

    private int id;
    private String name;
    private int position_category_id;
    private int activity_status_id;
    private String permissions;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getPosition_category_id() {
        return position_category_id;
    }

    public void setPosition_category_id(int position_category_id) {
        this.position_category_id = position_category_id;
    }

    public int getActivity_status_id() {
        return activity_status_id;
    }

    public void setActivity_status_id(int activity_status_id) {
        this.activity_status_id = activity_status_id;
    }

    public String getPermissions() {
        return permissions;
    }

    public void setPermissions(String permissions) {
        this.permissions = permissions;
    }
}
