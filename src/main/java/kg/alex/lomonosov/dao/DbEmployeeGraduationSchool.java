/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package kg.alex.lomonosov.dao;

import kg.alex.lomonosov.Settings;
import kg.alex.lomonosov.domain.EmployeeGraduationSchool;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

public class DbEmployeeGraduationSchool extends BaseDb {

    public DbEmployeeGraduationSchool() throws Exception {
        super();
    }

    public int exec_insert(EmployeeGraduationSchool egs) throws SQLException {
        String sql = "INSERT IGNORE INTO hr_employee_grad_school (employee_id, school_id, start_date, end_date) VALUES(?,?,?,?)";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setInt(1, egs.getEmployee_id());
        if (egs.getSchool_id() != 0) {
            stat.setInt(2, egs.getSchool_id());
        } else {
            stat.setNull(2, Types.INTEGER);
        }
        stat.setString(3, Settings.mysql_only_year.format(egs.getStart()));
        stat.setString(4, Settings.mysql_only_year.format(egs.getEnd()));
        int st = stat.executeUpdate();
        if (st != 0) {
            return getLastInsertedId();
        } else {
            return 0;
        }
    }

    public int exec_update(EmployeeGraduationSchool egs) throws SQLException {
        String sql = "update hr_employee_grad_school set "
                + "school_id = ?, start_date = ?, end_date = ? WHERE employee_id = ?";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        if (egs.getSchool_id() != 0) {
            stat.setInt(1, egs.getSchool_id());
        } else {
            stat.setNull(1, Types.INTEGER);
        }
        stat.setString(2, Settings.mysql_only_year.format(egs.getStart()));
        stat.setString(3, Settings.mysql_only_year.format(egs.getEnd()));
        stat.setInt(4, egs.getEmployee_id());
        return stat.executeUpdate();
    }

    public EmployeeGraduationSchool execSQL(int employee_id) throws SQLException {
        String sql = "SELECT * FROM hr_employee_grad_school AS egs WHERE egs.employee_id = ?";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setInt(1, employee_id);
        ResultSet result = stat.executeQuery();
        EmployeeGraduationSchool egs = null;
        if (result.next()) {
            egs = new EmployeeGraduationSchool();
            egs.setSchool_id(result.getInt("egs.school_id"));
            egs.setStart(result.getDate("egs.start_date"));
            egs.setEnd(result.getDate("egs.end_date"));
        }
        return egs;
    }
}
