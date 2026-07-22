/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package kg.alex.abl.dao;

import kg.alex.abl.domain.CurrencyRate;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DbCurrencyRate extends BaseDb {

    public DbCurrencyRate() throws Exception {
        super();
    }

    public double execSQL_last_rate(int school_id) throws SQLException {
        String sql = "SELECT value, is_mannual FROM currency_rates " +
                "WHERE school_id = ? or school_id is null order by modification_date DESC limit 1";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setInt(1, school_id);
        ResultSet result = stat.executeQuery();
        while (result.next()) {
            if (result.getInt("is_mannual") == 1) {
                return result.getDouble("value");
            }
        }
        return 0.0;
    }

    public int exec_insert(CurrencyRate r) throws SQLException {
        String sql = "INSERT INTO currency_rates (value, school_id, modification_date, employee_id, is_mannual) "
                + "VALUES(?,?,NOW(),?,?)";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setDouble(1, r.getValue());
        stat.setInt(2, r.getSchool_id());
        stat.setInt(3, r.getEmployee_id());
        stat.setInt(4, r.getManual());
        stat.executeUpdate();
        return getLastInsertedId();
    }
}
