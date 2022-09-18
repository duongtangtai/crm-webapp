package cybersoft.java18.jdbc;

import java.sql.PreparedStatement;
import java.sql.SQLException;

//this interface will store lambda, we need a wrapper lambda to handle exception due to database connection
@FunctionalInterface
public interface JDBCExecute <T> {
    T executeStatement(PreparedStatement preparedStatement) throws SQLException;
}
