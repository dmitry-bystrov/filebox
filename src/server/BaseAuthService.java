package server;

import java.sql.*;

public class BaseAuthService implements AuthService {

    private static Connection conn;
    private static Statement st;
    private static PreparedStatement ps;

    public BaseAuthService() {
    }

    @Override
    public void start() {
        try {
            Class.forName("org.sqlite.JDBC");
            conn = DriverManager.getConnection("jdbc:sqlite:sqlite.db");
            st = conn.createStatement();
            createTable();
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }
    }

    public static void createTable() throws SQLException {
        st.executeUpdate("CREATE TABLE IF NOT EXISTS accounts (" +
                "    id     INTEGER PRIMARY KEY AUTOINCREMENT," +
                "    nickname STRING NOT NULL UNIQUE ON CONFLICT ROLLBACK," +
                "    login  STRING NOT NULL UNIQUE ON CONFLICT ROLLBACK," +
                "    pass   STRING NOT NULL" +
                ");");
    }

    @Override
    public String getNicknameByLoginPass(String login, String pass) {
        try {
            ps = conn.prepareStatement("SELECT nickname FROM accounts WHERE login = ? AND pass = ?;");
            ps.setString(1, login);
            ps.setString(2, pass);
            ResultSet rs = ps.executeQuery();
            if (! rs.isBeforeFirst()) return null;
            rs.next();
            return rs.getString("nickname");
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public boolean setNicknameByLoginPass(String nickname, String login, String pass) {
        try {
            ps = conn.prepareStatement("INSERT INTO accounts (nickname, login, pass) VALUES (?, ?, ?);");
            ps.setString(1, nickname);
            ps.setString(2, login);
            ps.setString(3, pass);
            return ps.executeUpdate() == 1;
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    @Override
    public void stop() {
        try {
            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
