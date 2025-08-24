/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package kg.alex.ellipse.dao;

import kg.alex.ellipse.domain.School;
import kg.alex.ellipse.domain.UserDetails;
import kg.alex.ellipse.domain.Year;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * @author alex
 */
public class DbUserDetails extends BaseDb {

    public DbUserDetails() throws Exception {
        super();
    }

    public UserDetails execSQLUserInfo(String login) throws SQLException {
        String sql = "select e.id, ord.working_status_id, eb.hr_branch_id, e.login, concat(e.surname, ' ', e.name) as fullname, "
                + "eo.school_id, sch.name_ru, sch.school_type_id, sch.photo, sch.code, pos.id, "
                + "y.id, y.name, y.is_last, y.installment_date_limit "
                + "from employee as e "
                + "left join hr_employee_branch as eb on eb.employee_id = e.id and eb.hr_importance_id = 1 "
                + "left join hr_employee_order as eo on eo.employee_id = e.id and eo.to_date IS NULL "
                + "LEFT JOIN hr_position AS p ON p.id = eo.hr_position_id "
                + "LEFT JOIN position AS pos ON p.id = pos.hr_position_id "
                + "left join hr_orders as ord on ord.id = eo.hr_orders_id "
                + "left join school as sch on eo.school_id = sch.id "
                + "left join year as y on e.year_id = y.id "
                + "where e.login = ? and ord.working_status_id IS NOT NULL";

        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setString(1, login);
        ResultSet result = stat.executeQuery();
        UserDetails user = new UserDetails();
        while (result.next()) {
            user.setId(result.getInt("e.id"));
            user.setLogin(result.getString("login"));
            user.setFullName(result.getString("fullname"));
            user.setWorking_status_id(result.getInt("ord.working_status_id"));
            user.setSchool(new School());
            user.getSchool().setId(result.getInt("eo.school_id"));
            user.getSchool().setName_ru(result.getString("sch.name_ru"));
            user.getSchool().setPhoto(result.getString("sch.photo"));
            user.getSchool().setSchool_type_id(result.getInt("sch.school_type_id"));
            user.setBranch_id(result.getInt("eb.hr_branch_id"));
            user.setPosition_id(result.getInt("pos.id"));
            user.setCurrent_year(new Year(result.getInt("y.id"), result.getString("y.name"),
                    result.getLong("y.installment_date_limit"), result.getBoolean("y.is_last")));
        }
        return user;
    }

    public String execSQL_pass(String u_id) throws SQLException {
        String sql = "select  u.password from employee as u where u.login = ?";

        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setString(1, u_id);
        ResultSet result = stat.executeQuery();
        while (result.next()) {
            return result.getString("u.password");
        }
        return null;
    }

    public int editPass(String u_name, String pass) throws SQLException {
        String sql = "update employee set password = ? where login = ?";

        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setString(1, pass);
        stat.setString(2, u_name);
        return stat.executeUpdate();
    }
}
