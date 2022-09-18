package cybersoft.java18.repository;

import cybersoft.java18.jdbc.JDBCFunction;
import cybersoft.java18.model.StatusModel;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class StatusRepository extends JDBCFunction {
    private StatusModel resultSetToStatusModel(ResultSet resultSet) throws SQLException {
        return new StatusModel()
                .id(resultSet.getInt("id"))
                .name(resultSet.getString("name"));
    }

    private void addOrUpdateStatus(StatusModel statusModel, String sql) {
        processObjQuery(sql, preparedStatement -> {
           if (sql.startsWith("insert")) {
               return fillStatement(preparedStatement, statusModel.getId(), statusModel.getName()).executeUpdate();
           }
           return fillStatement(preparedStatement, statusModel.getName(), statusModel.getId()).executeUpdate();
        });
    }
    public void addStatus(StatusModel statusModel) {
        String sql = "insert into status (id, name) values (?, ?)";
        addOrUpdateStatus(statusModel, sql);
    }

    public StatusModel findStatusById(int id) {
        String sql = "select id, name from status where id = ?";
        return processObjQuery(sql, preparedStatement -> {
           ResultSet resultSet = fillStatement(preparedStatement, id).executeQuery();
           if (resultSet.next()) {
               return resultSetToStatusModel(resultSet);
           }
           return null;
        });
    }

    public List<StatusModel> findAllStatus() {
        String sql = "select id, name from status";
        List<StatusModel> statusModelList = new ArrayList<>();
        return processListQuery(sql, preparedStatement -> {
           ResultSet resultSet = preparedStatement.executeQuery();
           while (resultSet.next()) {
               statusModelList.add(resultSetToStatusModel(resultSet));
           }
           return statusModelList;
        });
    }

    public void updateStatus(StatusModel statusModel) {
        String sql = "update status set name = ? where id = ?";
        addOrUpdateStatus(statusModel, sql);
    }

    public void deleteStatus(int id) {
        String sql = "delete from status where id = ?";
        processObjQuery(sql, preparedStatement -> fillStatement(preparedStatement, id).executeUpdate());
    }
}
