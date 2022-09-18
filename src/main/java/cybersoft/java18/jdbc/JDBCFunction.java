package cybersoft.java18.jdbc;

import cybersoft.java18.exception.DatabaseNotFoundException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

//this class contains all the methods for JDBC and handle lambda exception while connecting to database
public abstract class JDBCFunction {
    protected <T> T processObjQuery(String sql, JDBCExecute<T> processor) {
        try (Connection connection = MySQLConnection.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)){
            return processor.executeStatement(preparedStatement);
        } catch (SQLException e) {
            throw new DatabaseNotFoundException(e.getMessage());
        }
    }
    protected <T> List<T> processListQuery(String sql, JDBCExecute<List<T>> processQuery) {
        try (Connection connection = MySQLConnection.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)){
            return processQuery.executeStatement(preparedStatement);
        } catch (SQLException e) {
            throw new DatabaseNotFoundException(e.getMessage());
        }
    }
    @SafeVarargs
    protected final<T> PreparedStatement fillStatement(PreparedStatement preparedStatement, T... varList) throws SQLException {
        int start = 1;
        for (T e : varList) {
            preparedStatement.setObject(start++, e);
        }
        return preparedStatement;
    }
}
