package be.janickreynders.bubblegum.jdbc;

import be.janickreynders.bubblegum.Chain;
import be.janickreynders.bubblegum.Filter;
import be.janickreynders.bubblegum.Request;
import be.janickreynders.bubblegum.Response;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ConnectionProvider implements Filter {
    private static Logger LOG = Logger.getLogger(ConnectionProvider.class.getName());

    private static ThreadLocal<ConnectionHolder> transactionalConnection = new ThreadLocal<ConnectionHolder>();

    @Override
    public void handle(Request req, Response resp, Chain chain) throws Exception {
        try {
            transactionalConnection.set(new ConnectionHolder());
            chain.handle(req, resp);
            commit();
        } catch (Exception e) {
            rollbackQuietly();
            throw e;
        } finally {
            closeQuietly();
        }
    }

    private static void commit() throws SQLException {
        ConnectionHolder holder = transactionalConnection.get();
        if (hasConnection(holder)) holder.getConnection().commit();
    }

    private static void rollback() throws SQLException {
        ConnectionHolder holder = transactionalConnection.get();
        if (hasConnection(holder)) holder.getConnection().rollback();
    }

    private static boolean hasConnection(ConnectionHolder holder) {
        return holder != null && holder.getConnection() != null;
    }

    private static void rollbackQuietly() {
        try {
            rollback();
        } catch (Exception ignore) {
            LOG.log(Level.WARNING, "Exception during rollback", ignore);
        }
    }

    private static void closeQuietly() {
        try {
            ConnectionHolder holder = transactionalConnection.get();
            if (hasConnection(holder) && !holder.getConnection().isClosed()) {
                holder.getConnection().setAutoCommit(true);
                holder.getConnection().close();
            }
        } catch (Exception ignore) {
            LOG.log(Level.WARNING, "Exception while closing connection", ignore);
        } finally {
            transactionalConnection.remove();
        }
    }

    public static Connection connection(DataSource ds) {
        ConnectionHolder holder = transactionalConnection.get();
        try {
            if (holder != null) {
                if (holder.getConnection() != null) return holder.getConnection();
                Connection newConnection = ds.getConnection();
                newConnection.setAutoCommit(false);
                holder.setConnection(newConnection);
                return newConnection;
            } else {
                Connection connection = ds.getConnection();
                connection.setAutoCommit(true);
                return connection;
            }
        } catch (SQLException e) {
            throw new JdbcException(e);
        }
    }

    public static Runnable withDbConnection(final Runnable r) {
        return new Runnable() {
            @Override
            public void run() {
                try {
                    transactionalConnection.set(new ConnectionHolder());
                    r.run();
                    commit();
                } catch (RuntimeException e) {
                    rollbackQuietly();
                    throw e;
                } catch (SQLException e) {
                    throw new JdbcException(e);
                } finally {
                    closeQuietly();
                }
            }
        };
    }

    public static void closeQuietly(Connection connection) {
        try {
            if (!isConnectionManaged(connection) && !connection.isClosed()) connection.close();
        } catch (Exception ignore) {
            LOG.log(Level.WARNING, "Exception while closing connection", ignore);
        }

    }

    private static boolean isConnectionManaged(Connection connection) {
        ConnectionHolder holder = transactionalConnection.get();
        return holder != null && holder.getConnection() == connection && connection != null;
    }

}
