/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package kg.alex.indigo.domain;

import java.io.Serializable;

public class AccBalanceSettings implements Serializable {

    private int id;
    private int accCategoryId;
    private int row;
    private int column;
    private String accCategory;
    private String prefix;
    private String postfix;
    private boolean withTextField;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getAccCategoryId() {
        return accCategoryId;
    }

    public void setAccCategoryId(int accCategoryId) {
        this.accCategoryId = accCategoryId;
    }

    public int getRow() {
        return row;
    }

    public void setRow(int row) {
        this.row = row;
    }

    public int getColumn() {
        return column;
    }

    public void setColumn(int column) {
        this.column = column;
    }

    public String getAccCategory() {
        return accCategory;
    }

    public void setAccCategory(String accCategory) {
        this.accCategory = accCategory;
    }

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public String getPostfix() {
        return postfix;
    }

    public void setPostfix(String postfix) {
        this.postfix = postfix;
    }

    public boolean isWithTextField() {
        return withTextField;
    }

    public void setWithTextField(boolean withTextField) {
        this.withTextField = withTextField;
    }
}
