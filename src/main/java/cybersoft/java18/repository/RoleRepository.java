package cybersoft.java18.repository;

import cybersoft.java18.jdbc.JDBCFunction;
import cybersoft.java18.model.RoleModel;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class RoleRepository extends JDBCFunction {
    private RoleModel resultSetToRoleModel(ResultSet resultSet) throws SQLException {
        return new RoleModel()
                        .id(resultSet.getInt("id"))
                        .name(resultSet.getString("name"))
                        .description(resultSet.getString("description"));
    }
    private int addOrUpdateRole(RoleModel roleModel, String sql) {
        return processObjQuery(sql, preparedStatement -> {
            int id = roleModel.getId();
            String name = roleModel.getName();
            String description = roleModel.getDescription();
           if (sql.startsWith("INSERT")) {
               return fillStatement(preparedStatement, name, description).executeUpdate();
           } else {
               return fillStatement(preparedStatement, name, description, id).executeUpdate();
           }
        });
    }
    public int addRole(RoleModel roleModel) {
        String sql = "INSERT INTO roles (name, description) VALUES (?, ?)";
        return addOrUpdateRole(roleModel, sql);
    }
    public RoleModel findRoleById(int id) {
        String sql = "SELECT id, name, description FROM roles WHERE id=?";
        return processObjQuery(sql, preparedStatement -> {
           ResultSet resultSet = fillStatement(preparedStatement, id).executeQuery();
           if (resultSet.next()) {
               return resultSetToRoleModel(resultSet);
           }
           return null;
        });
    }
    public List<RoleModel> findAllRoles() {
        String sql = "SELECT id, name, description FROM roles";
        List<RoleModel> roleModelList = new ArrayList<>();
        return processListQuery(sql, preparedStatement -> {
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                roleModelList.add(resultSetToRoleModel(resultSet));
            }
            return roleModelList;
        });
    }
    public int updateRole(RoleModel roleModel) {
        String sql = "UPDATE roles SET name = ?, description = ? WHERE id = ?";
        return addOrUpdateRole(roleModel, sql);
    }
    public int deleteRoleById(int id) {
        String sql = "DELETE FROM roles where id = ?";
        return processObjQuery(sql, preparedStatement -> fillStatement(preparedStatement, id).executeUpdate());
    }
}
