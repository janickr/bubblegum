package be.janickreynders.bubblegum.jdbc;

import javax.sql.DataSource;
import java.sql.*;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import static be.janickreynders.bubblegum.jdbc.ConnectionProvider.closeQuietly;
import static be.janickreynders.bubblegum.jdbc.ConnectionProvider.connection;
import static be.janickreynders.bubblegum.jdbc.ResultSetHandlers.*;

public class JdbcHelper  {
    private static Logger LOG = Logger.getLogger(JdbcHelper.class.getName());

    private DataSource ds;

    public JdbcHelper(DataSource ds) {
        this.ds = ds;
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
        Connection connection = connection(ds);
        PreparedStatement statement = null;
        ResultSet result = null;
        try {
            statement = prepare(sql, connection, params);
            result = statement.executeQuery();
            return handler.handle(result);
        } catch (SQLException e1) {
            throw new JdbcException(e1);
        } finally {
            close(result);
            close(statement);
            closeQuietly(connection);
        }
    }

    public void update(final String sql, final Object... params) {
        Connection connection = connection(ds);
        PreparedStatement statement = null;
        try {
            statement = prepare(sql, connection, params);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new JdbcException(e);
        } finally {
            close(statement);
            closeQuietly(connection);
        }
    }

    private PreparedStatement prepare(String sql, Connection connection, Object... params) throws SQLException {
        PreparedStatement statement = connection.prepareStatement(sql);
        for (int i = 0; i < params.length; i++) {
            statement.setObject(i + 1, params[i]);
        }
        return statement;
    }

    private void close(Statement statement) {
        try {
            if (statement != null) statement.close();
        } catch (SQLException ignore) {
            LOG.log(Level.WARNING, "Exception while closing Statement", ignore);
        }
    }

    private void close(ResultSet result) {
        try {
            if (result != null) result.close();
        } catch (SQLException ignore) {
            LOG.log(Level.WARNING, "Exception while closing ResultSet", ignore);
        }
    }
}
