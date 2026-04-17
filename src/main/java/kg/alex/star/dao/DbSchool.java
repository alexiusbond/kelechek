/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package kg.alex.star.dao;

import com.vaadin.data.Item;
import com.vaadin.data.util.IndexedContainer;
import kg.alex.star.MyVaadinUI;
import kg.alex.star.domain.School;
import kg.alex.star.i18n.Messages;
import kg.alex.star.utils.Settings;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

public class DbSchool extends BaseDb {

    public DbSchool() throws Exception {
        super();
    }

    public IndexedContainer execSQL(MyVaadinUI myUi) throws SQLException {

        String sql = "SELECT s.id, s.code, s.name_kg, s.name_ru, s.name_en, t.name, "
                     + "s.activity_status_id, ac.name, s.city, s.address, "
                     + "s.inn, s.bank, s.bank_account, s.phone, s.photo, s.school_type_id FROM school as s "
                     + "left join activity_status as ac on ac.id = s.activity_status_id "
                     + "left join school_type as t on t.id = s.school_type_id "
                     + "order by CAST(s.code AS UNSIGNED)";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        ResultSet result = stat.executeQuery();
        IndexedContainer container = new IndexedContainer();
        container.addContainerProperty(myUi.getMessage(Messages.Code), String.class, null);
        container.addContainerProperty(myUi.getMessage(Messages.TitleKg), String.class, null);
        container.addContainerProperty(myUi.getMessage(Messages.TitleRu), String.class, null);
        container.addContainerProperty(myUi.getMessage(Messages.TitleEn), String.class, null);
        container.addContainerProperty(myUi.getMessage(Messages.SchoolType), String.class, null);
        container.addContainerProperty(myUi.getMessage(Messages.Status), String.class, null);
        container.addContainerProperty(Settings.status_id, Integer.class, 0);
        container.addContainerProperty(Settings.school_type_id, Integer.class, 0);
        container.addContainerProperty(myUi.getMessage(Messages.City), String.class, null);
        container.addContainerProperty(myUi.getMessage(Messages.Address), String.class, null);
        container.addContainerProperty(myUi.getMessage(Messages.INN), String.class, null);
        container.addContainerProperty(myUi.getMessage(Messages.Bank), String.class, null);
        container.addContainerProperty(myUi.getMessage(Messages.BankAccount), String.class, null);
        container.addContainerProperty(myUi.getMessage(Messages.Phone), String.class, null);
        container.addContainerProperty(myUi.getMessage(Messages.Logo), String.class, null);
        container.addContainerProperty(Settings.id, Integer.class, 0);

        while (result.next()) {
            Item item = container.addItem(result.getInt("s.id"));
            item.getItemProperty(myUi.getMessage(Messages.Code)).setValue(
                    result.getString("s.code"));
            item.getItemProperty(myUi.getMessage(Messages.TitleKg)).setValue(
                    result.getString("s.name_kg"));
            item.getItemProperty(myUi.getMessage(Messages.TitleRu)).setValue(
                    result.getString("s.name_ru"));
            item.getItemProperty(myUi.getMessage(Messages.TitleEn)).setValue(
                    result.getString("s.name_en"));
            item.getItemProperty(myUi.getMessage(Messages.Status)).setValue(
                    result.getString("ac.name"));
            item.getItemProperty(myUi.getMessage(Messages.SchoolType)).setValue(
                    result.getString("t.name"));
            item.getItemProperty(Settings.status_id).setValue(
                    result.getInt("s.activity_status_id"));
            item.getItemProperty(Settings.school_type_id).setValue(
                    result.getInt("s.school_type_id"));
            item.getItemProperty(myUi.getMessage(Messages.City)).setValue(
                    result.getString("s.city"));
            item.getItemProperty(myUi.getMessage(Messages.Address)).setValue(
                    result.getString("s.address"));
            item.getItemProperty(myUi.getMessage(Messages.INN)).setValue(
                    result.getString("s.inn"));
            item.getItemProperty(myUi.getMessage(Messages.Bank)).setValue(
                    result.getString("s.bank"));
            item.getItemProperty(myUi.getMessage(Messages.BankAccount)).setValue(
                    result.getString("s.bank_account"));
            item.getItemProperty(myUi.getMessage(Messages.Phone)).setValue(
                    result.getString("s.phone"));
            item.getItemProperty(myUi.getMessage(Messages.Logo)).setValue(
                    result.getString("s.photo"));
            item.getItemProperty(Settings.id).setValue(result.getInt("s.id"));
        }
        return container;
    }

    public int exec_update(School scl) throws SQLException {
        String sql = "UPDATE school SET code = ?, name_ru = ?, name_kg = ?, name_en = ?, activity_status_id = ?, " +
                     "address = ?, inn = ?, bank = ?, bank_account = ?, phone = ?, photo = ?, city = ?, school_type_id = ? " +
                     "WHERE id = ?";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setString(1, scl.getCode());
        stat.setString(2, scl.getName_ru());
        if (scl.getName_kg() != null && !scl.getName_kg().isEmpty()) {
            stat.setString(3, scl.getName_kg());
        } else {
            stat.setNull(3, Types.VARCHAR);
        }
        if (scl.getName_en() != null && !scl.getName_en().isEmpty()) {
            stat.setString(4, scl.getName_en());
        } else {
            stat.setNull(4, Types.VARCHAR);
        }
        stat.setInt(5, scl.getStatus_id());
        if (scl.getAddress() != null && !scl.getAddress().isEmpty()) {
            stat.setString(6, scl.getAddress());
        } else {
            stat.setNull(6, Types.VARCHAR);
        }
        if (scl.getInn() != null && !scl.getInn().isEmpty()) {
            stat.setString(7, scl.getInn());
        } else {
            stat.setNull(7, Types.VARCHAR);
        }
        if (scl.getBank() != null && !scl.getBank().isEmpty()) {
            stat.setString(8, scl.getBank());
        } else {
            stat.setNull(8, Types.VARCHAR);
        }
        if (scl.getBank_account() != null && !scl.getBank_account().isEmpty()) {
            stat.setString(9, scl.getBank_account());
        } else {
            stat.setNull(9, Types.VARCHAR);
        }
        if (scl.getPhone() != null && !scl.getPhone().isEmpty()) {
            stat.setString(10, scl.getPhone());
        } else {
            stat.setNull(10, Types.VARCHAR);
        }
        if (scl.getPhoto() != null && !scl.getPhoto().isEmpty()) {
            stat.setString(11, scl.getPhoto());
        } else {
            stat.setNull(11, Types.VARCHAR);
        }
        if (scl.getCity() != null && !scl.getCity().isEmpty()) {
            stat.setString(12, scl.getCity());
        } else {
            stat.setNull(12, Types.VARCHAR);
        }
        stat.setInt(13, scl.getSchool_type_id());
        stat.setInt(14, scl.getId());

        return stat.executeUpdate();
    }

    public int exec_insert(School scl) throws SQLException {
        String sql = "INSERT IGNORE INTO school (code, name_ru, name_kg, name_en, "
                     + "activity_status_id, city, address, inn, bank, "
                     + "bank_account, phone, photo, school_type_id) "
                     + "VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?)";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setString(1, scl.getCode());
        stat.setString(2, scl.getName_ru());
        if (scl.getName_kg() != null && !scl.getName_kg().isEmpty()) {
            stat.setString(3, scl.getName_kg());
        } else {
            stat.setNull(3, Types.VARCHAR);
        }
        if (scl.getName_en() != null && !scl.getName_en().isEmpty()) {
            stat.setString(4, scl.getName_en());
        } else {
            stat.setNull(4, Types.VARCHAR);
        }
        stat.setInt(5, scl.getStatus_id());
        if (scl.getCity() != null && !scl.getCity().isEmpty()) {
            stat.setString(6, scl.getCity());
        } else {
            stat.setNull(6, Types.VARCHAR);
        }
        if (scl.getAddress() != null && !scl.getAddress().isEmpty()) {
            stat.setString(7, scl.getAddress());
        } else {
            stat.setNull(7, Types.VARCHAR);
        }
        if (scl.getInn() != null && !scl.getInn().isEmpty()) {
            stat.setString(8, scl.getInn());
        } else {
            stat.setNull(8, Types.VARCHAR);
        }
        if (scl.getBank() != null && !scl.getBank().isEmpty()) {
            stat.setString(9, scl.getBank());
        } else {
            stat.setNull(9, Types.VARCHAR);
        }
        if (scl.getBank_account() != null && !scl.getBank_account().isEmpty()) {
            stat.setString(10, scl.getBank_account());
        } else {
            stat.setNull(10, Types.VARCHAR);
        }
        if (scl.getPhone() != null && !scl.getPhone().isEmpty()) {
            stat.setString(11, scl.getPhone());
        } else {
            stat.setNull(11, Types.VARCHAR);
        }
        if (scl.getPhoto() != null && scl.getPhoto().isEmpty()) {
            stat.setString(12, scl.getPhoto());
        } else {
            stat.setNull(12, Types.VARCHAR);
        }
        stat.setInt(13, scl.getSchool_type_id());

        int st = stat.executeUpdate();
        if (st != 0) {
            return getLastInsertedId();
        } else {
            return 0;
        }
    }

    public School execSchool(int scl_id) throws SQLException {
        School scl = new School();
        String sql = "SELECT s.id, s.code, s.name_kg, s.name_ru, s.name_en, s.activity_status_id, s.city, s.address, "
                     + "s.inn, s.bank, s.bank_account, s.phone, s.photo, s.school_type_id, s.okpo, s.bik, s.swift " +
                     "FROM school as s where s.id = ?";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setInt(1, scl_id);
        ResultSet result = stat.executeQuery();
        while (result.next()) {
            scl.setAddress(result.getString("s.address"));
            scl.setBank(result.getString("s.bank"));
            scl.setBank_account(result.getString("s.bank_account"));
            scl.setCode(result.getString("s.code"));
            scl.setCity(result.getString("s.city"));
            scl.setInn(result.getString("s.inn"));
            scl.setOkpo(result.getString("s.okpo"));
            scl.setBik(result.getString("s.bik"));
            scl.setSwift(result.getString("s.swift"));
            scl.setName_kg(result.getString("s.name_kg"));
            scl.setName_en(result.getString("s.name_en"));
            scl.setName_ru(result.getString("s.name_ru"));
            scl.setPhone(result.getString("s.phone"));
            scl.setStatus_id(result.getInt("s.activity_status_id"));
            scl.setSchool_type_id(result.getInt("s.school_type_id"));
            scl.setPhoto(result.getString("s.photo"));
        }
        return scl;
    }

    public IndexedContainer execSchoolSel(MyVaadinUI myUI, int school_id, int employee_id) throws SQLException {
        String sql = "SELECT s.id, s.school_type_id, " +
                     "concat(s.code, ' - ', s.name_ru) as name, s.name_kg, " +
                     "s.photo, s.code, s.primary_code, s.secondary_code from school as s where s.is_visible = 1 ";
        if (school_id != 0) {
            sql += "and s.id = ? ";
        } else {
            sql += "and s.id not in (select school_id from employee_hide_school where employee_id = ?) ";
        }
        sql += " order by CAST(s.code AS UNSIGNED)";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setInt(1, school_id != 0 ? school_id : employee_id);
        ResultSet result = stat.executeQuery();
        IndexedContainer container = new IndexedContainer();

        container.addContainerProperty(myUI.getMessage(Messages.Title), String.class, null);
        container.addContainerProperty(myUI.getMessage(Messages.TitleKg), String.class, null);
        container.addContainerProperty(myUI.getMessage(Messages.Code), String.class, null);
        container.addContainerProperty(myUI.getMessage(Messages.PrimaryCode), String.class, null);
        container.addContainerProperty(myUI.getMessage(Messages.SecondaryCode), String.class, null);
        container.addContainerProperty(myUI.getMessage(Messages.Logo), String.class, null);
        container.addContainerProperty(Settings.dbSchoolType, Integer.class, 0);

        while (result.next()) {
            Item item = container.addItem(result.getInt("s.id"));
            item.getItemProperty(myUI.getMessage(Messages.Title)).setValue(result.getString("name"));
            item.getItemProperty(myUI.getMessage(Messages.TitleKg)).setValue(result.getString("s.name_kg"));
            item.getItemProperty(myUI.getMessage(Messages.Code)).setValue(result.getString("s.code"));
            item.getItemProperty(myUI.getMessage(Messages.PrimaryCode)).setValue(result.getString("s.primary_code"));
            item.getItemProperty(myUI.getMessage(Messages.SecondaryCode)).setValue(result.getString("s.secondary_code"));
            item.getItemProperty(myUI.getMessage(Messages.Logo)).setValue(result.getString("s.photo"));
            item.getItemProperty(Settings.dbSchoolType).setValue(result.getInt("s.school_type_id"));
        }
        return container;
    }

    public IndexedContainer execSchoolSel(MyVaadinUI myUI, int except_id) throws SQLException {
        String sql = "SELECT s.id, concat(s.code, ' - ', s.name_ru) as name, s.name_kg, " +
                     "s.photo, s.code, s.primary_code, s.secondary_code from school as s " +
                     "where s.id != ? order by CAST(s.code AS UNSIGNED)";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setInt(1, except_id);
        ResultSet result = stat.executeQuery();
        IndexedContainer container = new IndexedContainer();

        container.addContainerProperty(myUI.getMessage(Messages.Title), String.class, null);
        container.addContainerProperty(myUI.getMessage(Messages.TitleKg), String.class, null);
        container.addContainerProperty(myUI.getMessage(Messages.Code), String.class, null);
        container.addContainerProperty(myUI.getMessage(Messages.PrimaryCode), String.class, null);
        container.addContainerProperty(myUI.getMessage(Messages.SecondaryCode), String.class, null);
        container.addContainerProperty(myUI.getMessage(Messages.Logo), String.class, null);

        while (result.next()) {
            Item item = container.addItem(result.getInt("s.id"));
            item.getItemProperty(myUI.getMessage(Messages.Title)).setValue(result.getString("name"));
            item.getItemProperty(myUI.getMessage(Messages.TitleKg)).setValue(result.getString("s.name_kg"));
            item.getItemProperty(myUI.getMessage(Messages.Code)).setValue(result.getString("s.code"));
            item.getItemProperty(myUI.getMessage(Messages.PrimaryCode)).setValue(result.getString("s.primary_code"));
            item.getItemProperty(myUI.getMessage(Messages.SecondaryCode)).setValue(result.getString("s.secondary_code"));
            item.getItemProperty(myUI.getMessage(Messages.Logo)).setValue(result.getString("s.photo"));
        }
        return container;
    }

    public IndexedContainer execSchoolSel(MyVaadinUI myUI, String school_type_ids) throws SQLException {
        String sql = "SELECT s.id, concat(s.code, ' - ', s.name_ru) as name, s.code from school as s " +
                     "where s.school_type_id in (" + school_type_ids + ") order by CAST(s.code AS UNSIGNED)";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        ResultSet result = stat.executeQuery();
        IndexedContainer container = new IndexedContainer();

        container.addContainerProperty(myUI.getMessage(Messages.Title), String.class, null);
        container.addContainerProperty(myUI.getMessage(Messages.Code), String.class, null);

        while (result.next()) {
            Item item = container.addItem(result.getInt("s.id"));
            item.getItemProperty(myUI.getMessage(Messages.Title)).setValue(result.getString("name"));
            item.getItemProperty(myUI.getMessage(Messages.Code)).setValue(result.getString("s.code"));
        }
        return container;
    }

    public String execGet_logo(String st_login) throws SQLException {
        String sql = "SELECT sc.photo FROM school as sc "
                     + "left join student as st on st.school_id = sc.id "
                     + "where st.login = ?";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setString(1, st_login);
        ResultSet result = stat.executeQuery();
        String logo = null;
        while (result.next()) {
            logo = result.getString("sc.photo");
        }
        return logo;
    }
}
