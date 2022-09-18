package cybersoft.java18.repository;

import cybersoft.java18.jdbc.JDBCFunction;
import cybersoft.java18.model.UserModel;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class UserRepository extends JDBCFunction {
    private int addOrUpdateUser(UserModel userModel, String sql) {
        return processObjQuery(sql, preparedStatement -> {
           int id = userModel.getId();
           String email = userModel.getEmail();
           String password = userModel.getPassword();
           String fullName = userModel.getFullName();
           String avatar = userModel.getAvatar();
           String phoneNum = userModel.getPhoneNum();
           String role = userModel.getRole();
           if (sql.startsWith("INSERT")) {
               return fillStatement(preparedStatement, email, password, fullName, avatar, phoneNum, role).executeUpdate();
           }
            return fillStatement(preparedStatement, email, fullName, phoneNum, role, id).executeUpdate();
        });
    }
    public int addUser(UserModel userModel) {
        String sql = """
                INSERT INTO users (email, password, full_name, avatar, phone_num, role_id)
                values (?, ?, ?, ?, ?,
                (SELECT MAX(id)
                FROM roles
                WHERE name = ?));
                """;
        return addOrUpdateUser(userModel, sql);
    }

    public UserModel findUserById(int id) {
        String sql = """ 
                SELECT id, email, full_name, avatar
                FROM users
                WHERE id = ?
                """;
        return processObjQuery(sql, preparedStatement -> {
           ResultSet resultSet = fillStatement(preparedStatement, id).executeQuery();
           if (resultSet.next()) {
               return new UserModel()
                       .id(resultSet.getInt("id"))
                       .email(resultSet.getString("email"))
                       .fullName(resultSet.getString("full_name"))
                       .avatar(resultSet.getString("avatar"));
           }
           return null;
        });
    }
    public UserModel findUserProfileById(int id) {
        String sql = """ 
                SELECT id, email, full_name, phone_num, avatar
                FROM users
                WHERE id = ?
                """;
        return processObjQuery(sql, preparedStatement -> {
            ResultSet resultSet = fillStatement(preparedStatement, id).executeQuery();
            if (resultSet.next()) {
                return new UserModel()
                        .id(resultSet.getInt("id"))
                        .email(resultSet.getString("email"))
                        .fullName(resultSet.getString("full_name"))
                        .phoneNum(resultSet.getString("phone_num"))
                        .avatar(resultSet.getString("avatar"));
            }
            return null;
        });
    }
    public String findRefreshTokenById(int id) {
        String sql = """
                SELECT refresh_token
                FROM users
                where id = ?
                """;
        return processObjQuery(sql, preparedStatement -> {
            ResultSet resultSet = fillStatement(preparedStatement, id).executeQuery();
            if (resultSet.next()) {
                return resultSet.getString("refresh_token");
            }
            return null;
        });
    }
    public UserModel findUserByEmail(String email) {
        String sql = """
                SELECT u.id, u.password, u.full_name, u.avatar, r.name as role_name
                FROM users u
                JOIN roles r
                ON u.role_id = r.id
                WHERE email = ?
                """;
        return processObjQuery(sql, preparedStatement -> {
            ResultSet resultSet = fillStatement(preparedStatement, email).executeQuery();
            if (resultSet.next()) {
                return new UserModel().id(resultSet.getInt("id"))
                        .password(resultSet.getString("password"))
                        .fullName(resultSet.getString("full_name"))
                        .avatar(resultSet.getString("avatar"))
                        .role(resultSet.getString("role_name"));
            }
            return null;
        });
    }

    public List<UserModel> findUserByJobId(int id) { //User Model needs to return id, fullName, avatar
        String sql = """
                SELECT u.id, u.full_name, u.avatar
                FROM tasks t
                JOIN users u on t.user_id = u.id
                WHERE job_id = ?
                GROUP BY user_id
                ORDER BY user_id
                """;
        return processListQuery(sql, preparedStatement -> {
            ResultSet resultSet = fillStatement(preparedStatement, id).executeQuery();
            List<UserModel> output = new ArrayList<>();
            while (resultSet.next()) {
                output.add(
                        new UserModel()
                                .id(resultSet.getInt("id"))
                                .fullName(resultSet.getString("full_name"))
                                .avatar(resultSet.getString("avatar"))
                );
            }
            return output;
        });
    }
    public String findUserPWById(int id) {
        String sql = """
                SELECT password
                FROM users
                WHERE id = ?
                """;
        return processObjQuery(sql, preparedStatement -> {
            ResultSet resultSet = fillStatement(preparedStatement, id).executeQuery();
            if (resultSet.next()) {
                return resultSet.getString("password");
            }
            return null;
        });
    }
    public int changeUserPWById(int id, String newPW) {
        String sql = """
                UPDATE users
                SET password = ?
                WHERE id = ?
                """;
        return processObjQuery(sql, preparedStatement -> fillStatement(preparedStatement, newPW, id).executeUpdate());
    }
    public List<UserModel> findAllUsers() {
        String sql = """
                SELECT u.id, u.email, u.full_name, u.avatar, u.phone_num, r.name role
                FROM users u
                LEFT JOIN roles r
                ON u.role_id = r.id
                """;
        List<UserModel> userModelList = new ArrayList<>();
        return processListQuery(sql, preparedStatement -> {
            ResultSet resultSet = fillStatement(preparedStatement).executeQuery();
            while (resultSet.next()) {
                userModelList.add(
                        new UserModel()
                                .id(resultSet.getInt("id"))
                                .email(resultSet.getString("email"))
                                .fullName(resultSet.getString("full_name"))
                                .avatar(resultSet.getString("avatar"))
                                .phoneNum(resultSet.getString("phone_num"))
                                .role(resultSet.getString("role")));
            }
            return userModelList;
        });
    }
    public int updateUser(UserModel userModel) {
        String sql = """
                UPDATE users
                SET email = ?, full_name = ?, phone_num = ?, role_id =
                (SELECT MAX(id)
                FROM roles
                WHERE name = ?)
                WHERE id = ?
                """;
        return addOrUpdateUser(userModel, sql);
    }

    public int updateUserProfile(UserModel userModel) {
        String sql = """
                UPDATE users
                SET email = ?, full_name = ?, phone_num = ?
                WHERE id = ?
                """;
        return processObjQuery(sql, preparedStatement -> fillStatement(preparedStatement,
                userModel.getEmail(),
                userModel.getFullName(),
                userModel.getPhoneNum(),
                userModel.getId()).executeUpdate());
    }
    public int deleteUserById(int id) {
        String sql = """
                DELETE FROM users WHERE id = ?
                """;
        return processObjQuery(sql, preparedStatement -> fillStatement(preparedStatement, id).executeUpdate());
    }

    public void saveRefreshTokenByUserId(String refreshToken, int id) {
        String sql = """
                UPDATE users
                SET refresh_token = ?
                WHERE id = ?
                """;
        processObjQuery(sql, preparedStatement ->
                fillStatement(preparedStatement, refreshToken, id).executeUpdate());
    }
    public int logout(int userId) {
        String sql = """
                UPDATE users
                SET refresh_token = null
                WHERE id = ?
                """;
        return processObjQuery(sql, preparedStatement -> fillStatement(preparedStatement, userId).executeUpdate());
    }
    public int saveAvatar(String avatar, int id) {
        String sql = """
                UPDATE users
                SET avatar = ?
                WHERE id = ?
                """;
        return processObjQuery(sql ,preparedStatement -> fillStatement(preparedStatement, avatar, id).executeUpdate());
    }
    public String findUserRoleById(int id) {
        String sql = """
                SELECT r.name
                FROM users u
                JOIN roles r
                ON u.role_id = r.id
                WHERE u.id = ?
                """;
        return processObjQuery(sql, preparedStatement -> {
            ResultSet resultSet = fillStatement(preparedStatement, id).executeQuery();
            if (resultSet.next()) {
                return resultSet.getString("name");
            }
            return null;
        });
    }
}
