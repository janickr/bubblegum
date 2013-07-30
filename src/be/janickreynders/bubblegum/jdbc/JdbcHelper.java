package be.janickreynders.bubblegum.jdbc;

import be.janickreynders.bubblegum.Chain;
import be.janickreynders.bubblegum.Filter;
import be.janickreynders.bubblegum.Request;
import be.janickreynders.bubblegum.Response;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import static be.janickreynders.bubblegum.jdbc.ResultSetHandlers.*;

public class JdbcHelper implements Filter {
    private static Logger LOG = Logger.getLogger(JdbcHelper.class.getName());

    private static ThreadLocal<Connection> transactionalConnection = new ThreadLocal<Connection>();
    private DataSource ds;

    public JdbcHelper(DataSource ds) {
        this.ds = ds;
    }

    @Override
    public void handle(Request req, Response resp, Chain chain) throws Exception {
        try {
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
        Connection connection = transactionalConnection.get();
        if (connection != null) connection.commit();
    }

    private static void rollback() throws SQLException {
        Connection connection = transactionalConnection.get();
        if (connection != null) connection.rollback();
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
            Connection connection = transactionalConnection.get();
            if (connection != null && !connection.isClosed()) connection.close();
        } catch (Exception ignore) {
            LOG.log(Level.WARNING, "Exception while closing connection", ignore);
        } finally {
            transactionalConnection.remove();
        }
    }

    public Connection connection() {
        Connection connection = transactionalConnection.get();
        if (connection != null) return connection;

        try {
            Connection newConnection = ds.getConnection();
            newConnection.setAutoCommit(false);
            transactionalConnection.set(newConnection);
            return newConnection;
        } catch (SQLException e) {
            throw new JdbcException(e);
        }
    }

    public Integer getInt(String sql, Object...params) {
        return queryFor(single(Integer.class), sql, params);
    }

    public Long getLong(String sql, Object...params) {
        return queryFor(single(Long.class), sql, params);
    }

    public String getString(String sql, Object...params) {
        return queryFor(single(String.class), sql, params);
    }

    public Double getDouble(String sql, Object...params) {
        return queryFor(single(Double.class), sql, params);
    }

    public Float getFloat(String sql, Object...params) {
        return queryFor(single(Float.class), sql, params);
    }

    public Map<String, Object> map(String sql, Object...params) {
        return queryFor(MAP, sql, params);
    }

    public List<Map<String, Object>> list(String sql, Object...params) {
        return queryFor(LIST, sql, params);
    }

    public <T> List<T> list(String sql, Class<T> clazz, Object...params) {
        return queryFor(listOf(clazz), sql, params);
    }

    public <T> T queryFor(ResultSetHandler<T> handler, String sql, Object... params) {
        ResultSet rs = query(sql, params);

        try {
            return handler.handle(rs);
        } catch (SQLException e) {
            throw new JdbcException(e);
        } finally {
            try {
                rs.close();
            } catch (SQLException ignore) {
                LOG.log(Level.WARNING, "Exception while closing ResultSet", ignore);
            }
        }
    }

    public void update(String sql, Object... params) {

        PreparedStatement statement = createStatement(sql);
        try {
            for (int i = 0; i < params.length; i++) {
                statement.setObject(i+1, params[i]);
            }
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new JdbcException(e);
        } finally {
            try {
                statement.close();
            } catch (SQLException ignore) {
                LOG.log(Level.WARNING, "Exception while closing Statement", ignore);
            }
        }
    }

    private PreparedStatement createStatement(String sql) {
        try {
            return connection().prepareStatement(sql);
        } catch (SQLException e) {
            throw new JdbcException(e);
        }
    }

    public ResultSet query(String sql, Object...params) {
        try {
            PreparedStatement statement = createStatement(sql);
            for (int i = 0; i < params.length; i++) {
                statement.setObject(i+1, params[i]);
            }
            return statement.executeQuery();
        } catch (SQLException e) {
            throw new JdbcException(e);
        }
    }

    public static Runnable withDbConnection(final Runnable r) {
        return new Runnable() {
            @Override
            public void run() {
                try {
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


}
