/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package kg.alex.abl.dao;

import kg.alex.abl.domain.EmployeeContact;

import java.sql.*;

public class DbEmployeeContact extends BaseDb {

    public DbEmployeeContact() throws Exception {
        super();
    }

    public int exec_insert(EmployeeContact ec) throws SQLException {
        String sql = "INSERT IGNORE INTO hr_employee_contacts " +
                "(employee_id, birth_place, address, email, passport, passport_given, passport_date, inn) " +
                "VALUES(?,?,?,?,?,?,?,?)";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setInt(1, ec.getEmployee_id());
        stat.setString(2, ec.getBirth_place());
        stat.setString(3, ec.getAddress());
        if (ec.getEmail() != null && !ec.getEmail().isEmpty()) {
            stat.setString(4, ec.getEmail());
        } else {
            stat.setNull(4, Types.VARCHAR);
        }
        if (ec.getPassport() != null && !ec.getPassport().isEmpty()) {
            stat.setString(5, ec.getPassport());
        } else {
            stat.setNull(5, Types.VARCHAR);
        }
        if (ec.getPassportGiven() != null && !ec.getPassportGiven().isEmpty()) {
            stat.setString(6, ec.getPassportGiven());
        } else {
            stat.setNull(6, Types.VARCHAR);
        }
        if (ec.getPassportDate() != null) {
            stat.setDate(7, new Date(ec.getPassportDate().getTime()));
        } else {
            stat.setNull(7, Types.DATE);
        }
        if (ec.getInn() != null && !ec.getInn().isEmpty()) {
            stat.setString(8, ec.getInn());
        } else {
            stat.setNull(8, Types.VARCHAR);
        }
        int st = stat.executeUpdate();
        if (st != 0) {
            return getLastInsertedId();
        } else {
            return 0;
        }
    }

    public int exec_update(EmployeeContact ec) throws SQLException {
        String sql = "update hr_employee_contacts set birth_place = ?, address = ?, email = ?, " +
                "passport = ?, passport_given = ?, passport_date = ?, inn = ? WHERE employee_id = ?";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setString(1, ec.getBirth_place());
        stat.setString(2, ec.getAddress());
        if (ec.getEmail() != null && !ec.getEmail().isEmpty()) {
            stat.setString(3, ec.getEmail());
        } else {
            stat.setNull(3, Types.VARCHAR);
        }
        if (ec.getPassport() != null && !ec.getPassport().isEmpty()) {
            stat.setString(4, ec.getPassport());
        } else {
            stat.setNull(4, Types.VARCHAR);
        }
        if (ec.getPassportGiven() != null && !ec.getPassportGiven().isEmpty()) {
            stat.setString(5, ec.getPassportGiven());
        } else {
            stat.setNull(5, Types.VARCHAR);
        }
        if (ec.getPassportDate() != null) {
            stat.setDate(6, new Date(ec.getPassportDate().getTime()));
        } else {
            stat.setNull(6, Types.DATE);
        }
        if (ec.getInn() != null && !ec.getInn().isEmpty()) {
            stat.setString(7, ec.getInn());
        } else {
            stat.setNull(7, Types.VARCHAR);
        }
        stat.setInt(8, ec.getEmployee_id());
        return stat.executeUpdate();
    }

    public EmployeeContact execSQL(int employee_id) throws SQLException {
        String sql = "SELECT ec.*, IFNULL(GROUP_CONCAT(ph.number SEPARATOR ', '), '') AS phones " +
                "FROM hr_employee_contacts AS ec " +
                "left join hr_employee_phone_number as ph on ph.employee_id = ec.employee_id " +
                "WHERE ec.employee_id = ? group by ec.employee_id";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setInt(1, employee_id);
        ResultSet result = stat.executeQuery();
        EmployeeContact ec = null;
        if (result.next()) {
            ec = new EmployeeContact();
            ec.setAddress(result.getString("ec.address"));
            ec.setPassport(result.getString("ec.passport"));
            ec.setInn(result.getString("ec.inn"));
            ec.setPassportGiven(result.getString("ec.passport_given"));
            ec.setPassportDate(result.getDate("ec.passport_date"));
            ec.setBirth_place(result.getString("ec.birth_place"));
            ec.setEmail(result.getString("ec.email"));
            ec.setPhoneNumbers(result.getString("phones"));
        }
        return ec;
    }
}
