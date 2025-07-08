/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package kg.alex.lomonosov.dao;

import kg.alex.lomonosov.domain.EmployeeSpouse;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

public class DbEmployeeSpouse extends BaseDb {

    public DbEmployeeSpouse() throws Exception {
        super();
    }

    public int exec_insert(EmployeeSpouse es) throws SQLException {
        String sql = "INSERT IGNORE INTO hr_employee_spouse (employee_id,hr_health_status_id,"
                + "fullname,phone,health_notes) VALUES(?,?,?,?,?)";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setInt(1, es.getEmployee_id());
        stat.setInt(2, es.getHealth_status_id());
        stat.setString(3, es.getFullName());
        if (!es.getPhone().isEmpty() && es.getPhone() != null) {
            stat.setString(4, es.getPhone());
        } else {
            stat.setNull(4, Types.VARCHAR);
        }
        if (!es.getHealth_notes().isEmpty() && es.getHealth_notes() != null) {
            stat.setString(5, es.getHealth_notes());
        } else {
            stat.setNull(5, Types.VARCHAR);
        }
        int st = stat.executeUpdate();
        if (st != 0) {
            return getLastInsertedId();
        } else {
            return 0;
        }
    }

    public int exec_update(EmployeeSpouse es) throws SQLException {
        String sql = "update hr_employee_spouse set "
                + "hr_health_status_id = ?, fullname = ?, phone = ?, health_notes = ? WHERE employee_id = ?";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setInt(1, es.getHealth_status_id());
        stat.setString(2, es.getFullName());
        if (!es.getPhone().isEmpty() && es.getPhone() != null) {
            stat.setString(3, es.getPhone());
        } else {
            stat.setNull(3, Types.VARCHAR);
        }
        if (!es.getHealth_notes().isEmpty() && es.getHealth_notes() != null) {
            stat.setString(4, es.getHealth_notes());
        } else {
            stat.setNull(4, Types.VARCHAR);
        }
        stat.setInt(5, es.getEmployee_id());
        return stat.executeUpdate();
    }

    public EmployeeSpouse execSQL(int employee_id) throws SQLException {
        String sql = "SELECT * FROM hr_employee_spouse AS es LEFT JOIN hr_health_status AS hs ON hs.id = es.hr_health_status_id WHERE es.employee_id = ?";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setInt(1, employee_id);
        ResultSet result = stat.executeQuery();
        EmployeeSpouse ec = null;
        if (result.next()) {
            ec = new EmployeeSpouse();
            ec.setFullName(result.getString("es.fullname"));
            ec.setPhone(result.getString("es.phone"));
            ec.setHealth_notes(result.getString("es.health_notes"));
            ec.setHealth_status_id(result.getInt("hs.id"));
        }
        return ec;
    }
}
