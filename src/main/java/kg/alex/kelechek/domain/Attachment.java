/*
 * Semester.java
 * Created on December 18, 2007, 1:32 PM
 */
package kg.alex.kelechek.domain;

import java.io.Serializable;

/**
 * @author Alex
 */
public class Attachment implements Serializable {
    private int id;
    private String name;
    private String extension;
    private String unique_name;

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

    public String getExtension() {
        return extension;
    }

    public void setExtension(String extension) {
        this.extension = extension;
    }

    public String getUnique_name() {
        return unique_name;
    }

    public void setUnique_name(String unique_name) {
        this.unique_name = unique_name;
    }

    @Override
    public String toString() {
        return "Attachment{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", extension='" + extension + '\'' +
                ", unique_name='" + unique_name + '\'' +
                '}';
    }
}
