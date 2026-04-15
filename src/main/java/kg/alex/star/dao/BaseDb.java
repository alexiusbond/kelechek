package kg.alex.star.dao;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class BaseDb {

    protected Connection dbCon;
    protected DataSource pool;

    /* Creates a new instance of BaseDb */
    public BaseDb() throws Exception {
        Context env;
        try {
            env = (Context) new InitialContext().lookup("java:comp/env");
            pool = (DataSource) env.lookup("jdbc/star");
            if (pool == null) {
                throw new Exception("star can not found");
            }
        } catch (NamingException ne) {
            throw new Exception("...BaseDB... " + ne.getMessage());
        }
    }

    public boolean connect() throws ClassNotFoundException, SQLException, IOException {
        Class.forName("com.mysql.cj.jdbc.Driver");
        dbCon = pool.getConnection();
        return true;
    }

    public Connection getConnection() throws ClassNotFoundException, SQLException, IOException {
        return dbCon;
    }

    public void close() throws SQLException {
        dbCon.close();
    }

    public int getLastInsertedId() throws SQLException {
        String sql = "select last_insert_id() as id";
        int id = 0;

        PreparedStatement stat = dbCon.prepareStatement(sql);
        ResultSet result = stat.executeQuery();
        while (result.next()) {
            id = result.getInt("id");
        }
        return id;
    }

    public int getLastInsertedId(Connection conn) throws SQLException {

        String sql = "select last_insert_id() as id";
        int id = 0;

        PreparedStatement stat = conn.prepareStatement(sql);
        ResultSet result = stat.executeQuery();
        while (result.next()) {
            id = result.getInt("id");
        }
        return id;
    }
}
