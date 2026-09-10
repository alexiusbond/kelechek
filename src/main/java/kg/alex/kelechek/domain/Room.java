/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package kg.alex.kelechek.domain;

import java.io.Serializable;

/**
 * @author alex
 */
public class Room implements Serializable {

    private int id;
    private String name;
    private String description;
    private int block_id;
    private int floor_id;
    private int activity_status_id;

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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getBlock_id() {
        return block_id;
    }

    public void setBlock_id(int block_id) {
        this.block_id = block_id;
    }

    public int getFloor_id() {
        return floor_id;
    }

    public void setFloor_id(int floor_id) {
        this.floor_id = floor_id;
    }

    public int getActivity_status_id() {
        return activity_status_id;
    }

    public void setActivity_status_id(int activity_status_id) {
        this.activity_status_id = activity_status_id;
    }
}
