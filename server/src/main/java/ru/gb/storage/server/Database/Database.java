package ru.gb.storage.server.Database;

import java.sql.*;

public class Database {

    private final static String DRIVER = "org.sqlite.JDBC";
    private final static String CONNECTION = "jdbc:sqlite:./Network.db";
    private final static String CREATE_TABLE = "CREATE TABLE IF NOT EXITS Clients (id INTEGER PRIMARY KEY AUTOINCREMENT,login TEXT UNIQUE NOT NULL, password TEXT NOT NULL);";
    private final static String ADD_ADMIN = "INSERT INTO Clients  (login, password) VALUES ('Admin', '123')";
    private static PreparedStatement prepStat;
    private static Connection con;
    private static Statement stat;

    public static void main(String[] args) throws Exception {
        connect();
    }

    public static void connect() throws Exception{
        System.out.println("Connect to DB");
        Class.forName(DRIVER);
        con = DriverManager.getConnection(CONNECTION);
        stat = con.createStatement();
        stat.execute(CREATE_TABLE);
        stat.executeUpdate(ADD_ADMIN);
    }

    public static boolean isConnected() throws SQLException {
        return !con.isClosed();
    }

    public static boolean login(String login, String pass) throws SQLException {
        prepStat = con.prepareStatement("SELECT * FROM Clients WHERE login = ? AND password = ?");
        prepStat.setString(1, login);
        prepStat.setString(2,pass);
        ResultSet resultSet = prepStat.executeQuery();
        return resultSet.next();
    }

    public static void disconnect() {
        if(stat != null) {
            try {
                stat.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        if (con !=null){
            try {
                con.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}
