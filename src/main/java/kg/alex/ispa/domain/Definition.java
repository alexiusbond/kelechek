/*
 * Semester.java
 * Created on December 18, 2007, 1:32 PM
 */
package kg.alex.ispa.domain;

import java.io.Serializable;

/**
 * @author Alex
 */
public class Definition implements Serializable {

    private int id;
    private String name;
    private int activity_status_id;

    public Definition() {
    }

    public Definition(int id, String name) {
        this.id = id;
        this.name = name;
    }

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

    public int getActivity_status_id() {
        return activity_status_id;
    }

    public void setActivity_status_id(int activity_status_id) {
        this.activity_status_id = activity_status_id;
    }
}
