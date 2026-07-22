/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package kg.alex.abl.dao;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DbEmployeeCompleteness extends BaseDb {

    public DbEmployeeCompleteness() throws Exception {
        super();
    }

    public int exec_insert(int employee_id) throws SQLException {
        String sql = "INSERT IGNORE INTO hr_employee_completeness (employee_id) VALUES(?)";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setInt(1, employee_id);

        int st = stat.executeUpdate();
        if (st != 0) {
            return getLastInsertedId();
        } else {
            return 0;
        }
    }

    public int exec_update(int employee_id, String columnName, boolean isFilled) throws SQLException {
        String sql = "update hr_employee_completeness set " + columnName + " = ? WHERE employee_id = ?";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setBoolean(1, isFilled);
        stat.setInt(2, employee_id);
        return stat.executeUpdate();
    }

    public int exec_update_modification_date(int employee_id) throws SQLException {
        String sql = "update hr_employee_completeness set modification_date = NOW() WHERE employee_id = ?";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setInt(1, employee_id);
        return stat.executeUpdate();
    }

    public Boolean execSQL(int employee_id, String columnName) throws SQLException {
        String sql = "SELECT " + columnName + " FROM hr_employee_completeness as cmpl "
                + "where cmpl.employee_id = ?";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setInt(1, employee_id);
        ResultSet result = stat.executeQuery();
        while (result.next()) {
            if (result.getString(columnName) != null) {
                return result.getBoolean(columnName);
            }
        }
        return null;
    }
}
