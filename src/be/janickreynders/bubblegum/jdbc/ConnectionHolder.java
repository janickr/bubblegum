package be.janickreynders.bubblegum.jdbc;

import java.sql.Connection;

public class ConnectionHolder {
    private Connection connection;

    public Connection getConnection() {
        return connection;
    }

    public void setConnection(Connection connection) {
        this.connection = connection;
    }
}
