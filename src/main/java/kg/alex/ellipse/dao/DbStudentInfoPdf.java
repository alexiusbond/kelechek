/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package kg.alex.ellipse.dao;

import kg.alex.ellipse.domain.*;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DbStudentInfoPdf extends BaseDb {

    public DbStudentInfoPdf() throws Exception {
        super();
    }

    public StudentInfoPdf execSQL(int year_id, int student_id) throws SQLException {
        StudentInfoPdf sti = new StudentInfoPdf();
        String sql = "SELECT s.id, s.login, s.photo, s.surname, s.name, s.middle_name, s.gender_id, "
                     + "s.date_of_birth, s.address, sr.fullname, "
                     + "sr.phone, sr.passport, sr.given_by, sr.address, r.name_ru, r.name_ru_dec, r.gender_id, "
                     + "y.period, y.end_date, y.name, sc.contract_number, sc.creation_date, "
                     + "vcs.class_name "
                     + "FROM student as s "
                     + "LEFT JOIN view_student_class_status as vcs on vcs.student_id = s.id and vcs.year_id = ? "
                     + "left join student_relatives as sr on sr.student_id = s.id "
                     + "left join relatives as r on r.id = sr.relatives_id "
                     + "left join year as y on y.id = ? "
                     + "left join student_contract as sc on sc.student_id = s.id and sc.year_id = y.id "
                     + "where s.id = ? and sr.is_main = 1";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setInt(1, year_id);
        stat.setInt(2, year_id);
        stat.setInt(3, student_id);
        ResultSet result = stat.executeQuery();
        while (result.next()) {
            sti.setStudent(new Student());
            sti.setContractInfo(new ContractInfo());
            sti.setRelative(new StudentRelative());
            sti.getStudent().setId(result.getInt("s.id"));
            sti.getStudent().setLogin(result.getString("s.login"));
            sti.getStudent().setPhoto(result.getString("s.photo"));
            sti.getStudent().setName(result.getString("s.name"));
            sti.getStudent().setSurname(result.getString("s.surname"));
            sti.getStudent().setAddress(result.getString("s.address"));
            sti.getStudent().setBirth_date(result.getDate("s.date_of_birth"));
            if (result.getString("s.middle_name") == null
                || result.getString("s.middle_name").isEmpty()) {
                sti.getStudent().setMiddle_name("");
            } else {
                sti.getStudent().setMiddle_name(result.getString("s.middle_name"));
            }
            sti.getStudent().setGender_id(result.getInt("s.gender_id"));
            sti.getStudent().setClass_name(result.getString("vcs.class_name"));
            sti.getRelative().setFullName(result.getString("sr.fullname"));
            sti.getRelative().setPhone(result.getString("sr.phone"));
            sti.getRelative().setAddress(result.getString("sr.address"));
            sti.getRelative().setPassport(result.getString("sr.passport"));
            sti.getRelative().setGivenBy(result.getString("sr.given_by"));
            sti.getRelative().setRelativeTitle(result.getString("r.name_ru"));
            sti.getRelative().setGender_id(result.getInt("r.gender_id"));
            sti.getRelative().setRelativeDeclarative(result.getString("r.name_ru_dec"));
            sti.setYear(new Year(result.getString("y.period"),
                    result.getString("y.name"),
                    result.getDate("y.end_date")));
            sti.getContractInfo().setContractNumber(result.getInt("sc.contract_number"));
            sti.getContractInfo().setCreationDate(result.getDate("sc.creation_date"));
        }
        return sti;
    }
}
