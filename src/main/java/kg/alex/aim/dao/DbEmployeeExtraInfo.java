/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package kg.alex.aim.dao;

import kg.alex.aim.domain.EmployeeExtraInfo;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

public class DbEmployeeExtraInfo extends BaseDb {

    public DbEmployeeExtraInfo() throws Exception {
        super();
    }

    public int exec_insert(EmployeeExtraInfo eei) throws SQLException {
        String sql = "INSERT IGNORE INTO hr_employee_extra_info (employee_id,hr_health_status_id,"
                + "hobbies,fobbies,health_notes,info) VALUES(?,?,?,?,?,?)";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setInt(1, eei.getEmployee_id());
        stat.setInt(2, eei.getHealth_status_id());
        if (!eei.getHobbies().isEmpty() && eei.getHobbies() != null) {
            stat.setString(3, eei.getHobbies());
        } else {
            stat.setNull(3, Types.VARCHAR);
        }
        if (!eei.getPhobias().isEmpty() && eei.getPhobias() != null) {
            stat.setString(4, eei.getPhobias());
        } else {
            stat.setNull(4, Types.VARCHAR);
        }
        if (!eei.getHealth_notes().isEmpty() && eei.getHealth_notes() != null) {
            stat.setString(5, eei.getHealth_notes());
        } else {
            stat.setNull(5, Types.VARCHAR);
        }
        if (!eei.getShort_notes().isEmpty() && eei.getShort_notes() != null) {
            stat.setString(6, eei.getShort_notes());
        } else {
            stat.setNull(6, Types.VARCHAR);
        }
        int st = stat.executeUpdate();
        if (st != 0) {
            return getLastInsertedId();
        } else {
            return 0;
        }
    }

    public int exec_update(EmployeeExtraInfo eei) throws SQLException {
        String sql = "update hr_employee_extra_info set "
                + "hr_health_status_id = ?, hobbies = ?, fobbies = ?, health_notes = ?, info = ? WHERE employee_id = ?";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setInt(1, eei.getHealth_status_id());
        if (!eei.getHobbies().isEmpty() && eei.getHobbies() != null) {
            stat.setString(2, eei.getHobbies());
        } else {
            stat.setNull(2, Types.VARCHAR);
        }
        if (!eei.getPhobias().isEmpty() && eei.getPhobias() != null) {
            stat.setString(3, eei.getPhobias());
        } else {
            stat.setNull(3, Types.VARCHAR);
        }
        if (!eei.getHealth_notes().isEmpty() && eei.getHealth_notes() != null) {
            stat.setString(4, eei.getHealth_notes());
        } else {
            stat.setNull(4, Types.VARCHAR);
        }
        if (!eei.getShort_notes().isEmpty() && eei.getShort_notes() != null) {
            stat.setString(5, eei.getShort_notes());
        } else {
            stat.setNull(5, Types.VARCHAR);
        }
        stat.setInt(6, eei.getEmployee_id());
        return stat.executeUpdate();
    }

    public EmployeeExtraInfo execSQL(int employee_id) throws SQLException {
        String sql = "SELECT * FROM hr_employee_extra_info AS eei " +
                "LEFT JOIN hr_health_status AS hs ON hs.id = eei.hr_health_status_id WHERE eei.employee_id = ?";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setInt(1, employee_id);
        ResultSet result = stat.executeQuery();
        EmployeeExtraInfo eei = null;
        if (result.next()) {
            eei = new EmployeeExtraInfo();
            eei.setHobbies(result.getString("eei.hobbies"));
            eei.setPhobias(result.getString("eei.fobbies"));
            eei.setShort_notes(result.getString("eei.info"));
            eei.setHealth_notes(result.getString("eei.health_notes"));
            eei.setHealth_status_id(result.getInt("hs.id"));
        }
        return eei;
    }

    public EmployeeExtraInfo execSQL_for_cv(int employee_id) throws SQLException {
        String sql = "SELECT n.name, g.name, m.name, c.name, sal.name, ei.health_notes, ei.hobbies, " +
                "ei.fobbies, h.name, cont.email, cont.address, cont.birth_place, fam.fullname, fam.health_notes, h2.name, " +
                "(select count(id) from hr_employee_children as ch where ch.employee_id = e.id) as children, " +
                "GROUP_CONCAT(DISTINCT l.name ORDER BY l.name ASC SEPARATOR ', ') as langs, " +
                "GROUP_CONCAT(DISTINCT ph.number ORDER BY ph.id ASC SEPARATOR ', ') as phones, " +
                "(select cmpl.modification_date from hr_employee_completeness as cmpl where cmpl.employee_id = e.id) as modified " +
                "FROM employee AS e " +
                "LEFT JOIN hr_employee_extra_info AS ei ON e.id = ei.employee_id " +
                "LEFT JOIN hr_health_status AS h ON h.id = ei.hr_health_status_id " +
                "LEFT JOIN hr_employee_phone_number AS ph ON ph.employee_id = e.id " +
                "LEFT JOIN hr_employee_contacts AS cont ON cont.employee_id = e.id " +
                "LEFT JOIN nationality AS n ON n.id = e.nationality_id " +
                "LEFT JOIN hr_martial_status AS m ON m.id = e.hr_martial_status_id " +
                "LEFT JOIN hr_country AS c ON c.id = e.hr_country_id " +
                "LEFT JOIN gender AS g ON g.id = e.gender_id " +
                "LEFT JOIN acc_category AS cat ON cat.employee_id = e.id AND cat.activity_status_id = 2 " +
                "LEFT JOIN acc_category AS cat2 ON cat2.id = cat.parent_id " +
                "LEFT JOIN hr_salary_category AS sal ON sal.acc_category_id = cat2.parent_id " +
                "LEFT JOIN hr_employee_language AS el ON el.employee_id = e.id " +
                "LEFT JOIN hr_language AS l ON el.hr_language_id = l.id " +
                "LEFT JOIN hr_employee_spouse AS fam ON fam.employee_id = e.id " +
                "LEFT JOIN hr_health_status AS h2 ON h2.id = fam.hr_health_status_id " +
                "WHERE e.id = ?";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setInt(1, employee_id);
        ResultSet result = stat.executeQuery();
        EmployeeExtraInfo eei = null;
        if (result.next()) {
            eei = new EmployeeExtraInfo();
            eei.setMartialStatus(result.getString("m.name"));
            eei.setNationality(result.getString("n.name"));
            eei.setCitizenship(result.getString("c.name"));
            eei.setSalaryCategory(result.getString("sal.name"));
            eei.setGender(result.getString("g.name"));
            eei.setLanguages(result.getString("langs"));
            eei.setPhobias(result.getString("ei.fobbies"));
            eei.setHobbies(result.getString("ei.hobbies"));
            eei.setModificationDate(result.getDate("modified"));
            if (result.getString("h.name") != null) {
                eei.setHealth_notes(result.getString("h.name") + (result.getString("ei.health_notes") == null ?
                        "" : ", " + result.getString("ei.health_notes")));
            } else {
                eei.setHealth_notes("");
            }
            if (result.getString("fam.fullname") != null) {
                eei.setFamilyInfo(result.getString("fam.fullname") + " (" + result.getString("h2.name")
                        + (result.getString("fam.health_notes") == null ? "" : " - " + result.getString("fam.health_notes")) + ")");
            } else {
                eei.setFamilyInfo("");
            }
            eei.setChildren(result.getInt("children"));
            eei.setPhones(result.getString("phones"));
            eei.setAddress(result.getString("cont.address"));
            eei.setEmail(result.getString("cont.email"));
            eei.setBirth_place(result.getString("cont.birth_place"));
        }
        return eei;
    }
}
