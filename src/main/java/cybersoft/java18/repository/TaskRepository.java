package cybersoft.java18.repository;

import cybersoft.java18.jdbc.JDBCFunction;
import cybersoft.java18.model.TaskModel;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TaskRepository extends JDBCFunction {
    private TaskModel resultSetToTaskModel(ResultSet resultSet) throws SQLException {
        return new TaskModel()
                .id(resultSet.getInt("id"))
                .name(resultSet.getString("name"))
                .startDate(resultSet.getDate("start_date").toString())
                .endDate(resultSet.getDate("end_date").toString())
                .userName(resultSet.getString("user_name"))
                .jobName(resultSet.getString("job_name"))
                .statusName(resultSet.getString("status_name"))
                .note(resultSet.getString("note"));
    }
    private int addOrUpdateTask(TaskModel taskModel, String sql) {
        return processObjQuery(sql, preparedStatement -> {
            int id = taskModel.getId();
            String name = taskModel.getName();
            LocalDate startDate = LocalDate.parse(taskModel.getStartDate(), DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            LocalDate endDate = LocalDate.parse(taskModel.getEndDate(), DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            String userName = taskModel.getUserName();
            String jobName = taskModel.getJobName();
            String statusName = taskModel.getStatusName();
            String note = taskModel.getNote();
            if (sql.startsWith("INSERT")) {
                return fillStatement(preparedStatement, name, startDate, endDate,
                        userName, jobName, statusName).executeUpdate();
            }
            return fillStatement(preparedStatement, name, startDate, endDate, note,
                    userName, jobName, statusName, id).executeUpdate();
        });
    }
    public int addTask(TaskModel taskModel) {
        String sql = """
                INSERT INTO tasks (name, start_date, end_date, user_id, job_id, status_id)
                VALUES ( ?, ?, ?,
                (SELECT max(id) FROM users WHERE full_name = ?),
                (SELECT max(id) FROM jobs WHERE name = ?),
                (SELECT max(id) FROM status WHERE name = ?))
                """;
        return addOrUpdateTask(taskModel, sql);
    }
    public TaskModel findTaskById(int id) {
        String sql = """
                SELECT t.id, t.name, t.start_date, t.end_date, u.full_name user_name, j.name job_name,
                s.name status_name, t.note
                FROM tasks t
                LEFT JOIN users u ON t.user_id = u.id
                LEFT JOIN jobs j ON t.job_id = j.id
                LEFT JOIN status s ON t.status_id = s.id
                WHERE t.id = ?
                """;
        return processObjQuery(sql, preparedStatement -> {
            ResultSet resultSet = fillStatement(preparedStatement, id).executeQuery();
            if (resultSet.next()) {
                return resultSetToTaskModel(resultSet);
            }
            return null;
        });
    }
    public List<TaskModel> findAllTasks() {
        String sql = """
                SELECT t.id, t.name, t.start_date, t.end_date, u.full_name user_name,
                j.name job_name, s.name status_name, t.note
                FROM tasks t
                LEFT JOIN users u ON t.user_id = u.id
                LEFT JOIN jobs j ON t.job_id = j.id
                LEFT JOIN status s ON t.status_id = s.id
                """;
        return processListQuery(sql, preparedStatement -> {
           ResultSet resultSet = preparedStatement.executeQuery();
           List<TaskModel> taskModelList = new ArrayList<>();
           while (resultSet.next()) {
               taskModelList.add(resultSetToTaskModel(resultSet));
           }
           return taskModelList;
        });
    }
    public Map<String, Integer> findAllTaskStatistics() {
        String sql = """
                SELECT st.name status_name, count(t.status_id) num_of_status
                FROM status st
                LEFT JOIN tasks t
                ON st.id = t.status_id
                GROUP BY st.name;
                """;
        return processObjQuery(sql, preparedStatement -> {
            ResultSet resultSet = preparedStatement.executeQuery();
            Map<String, Integer> taskStatistics = new HashMap<>();
            while (resultSet.next()) {
                taskStatistics.put(resultSet.getString("status_name"), resultSet.getInt("num_of_status"));
            }
            return taskStatistics;
        });
    }
    public int updateTask(TaskModel taskModel) {
        String sql = """
                UPDATE tasks
                SET name = ?, start_date = ?, end_date = ?, note = ?,
                user_id = (SELECT max(id) FROM users WHERE full_name = ?),
                job_id = (SELECT max(id) FROM jobs WHERE name = ?),
                status_id = (SELECT max(id) FROM status WHERE name = ?)
                WHERE id = ?
                """;
        return addOrUpdateTask(taskModel, sql);
    }
    public int deleteTaskById(int id) {
        String sql = "DELETE FROM tasks WHERE id = ?";
        return processObjQuery(sql, preparedStatement -> fillStatement(preparedStatement, id).executeUpdate());
    }
    public Map<String, Integer> findStatisticByUserId(int id) {
        String sql = """
                SELECT st.name as status_name, count(t.user_id) as num_of_tasks
                FROM tasks t
                JOIN status st
                ON t.status_id = st.id
                WHERE t.user_id = ?
                GROUP BY st.name;
                """;
        return processObjQuery(sql, preparedStatement -> {
            ResultSet resultSet = fillStatement(preparedStatement, id).executeQuery();
            Map<String, Integer> output = new HashMap<>();
            while (resultSet.next()) {
                output.put(resultSet.getString("status_name"), resultSet.getInt("num_of_tasks"));
            }
            return output;
        });
    }
    public List<TaskModel> findTaskByUserId(int id) {
        String sql = """
                SELECT t.id, t.name, t.start_date, t.end_date, st.name as status_name, j.name as job_name
                FROM tasks t JOIN status st
                ON t.status_id = st.id
                JOIN jobs j
                ON t.job_id = j.id
                WHERE t.user_id = ?
                """;
        List<TaskModel> taskModelList = new ArrayList<>();
        return processListQuery(sql, preparedStatement -> {
            ResultSet resultSet = fillStatement(preparedStatement, id).executeQuery();
            while (resultSet.next()) {
                taskModelList.add(
                        new TaskModel()
                                .id(resultSet.getInt("id"))
                                .name(resultSet.getString("name"))
                                .startDate(resultSet.getDate("start_date").toString())
                                .endDate(resultSet.getDate("end_date").toString())
                                .statusName(resultSet.getString("status_name"))
                                .jobName(resultSet.getString("job_name"))
                );
            }
            return taskModelList;
        });
    }
    public List<TaskModel> findTaskByUserIdAndJobId(int userId, int jobId) {
        String sql = """
                SELECT t.id, t.name, t.start_date, t.end_date, st.name status_name
                FROM tasks t
                JOIN status st
                ON t.status_id = st.id
                WHERE user_id = ? and job_id = ?
                """;
        return processObjQuery(sql, preparedStatement -> {
            ResultSet resultSet = fillStatement(preparedStatement, userId, jobId).executeQuery();
            List<TaskModel> taskModelList = new ArrayList<>();
            while (resultSet.next()) {
                taskModelList.add(
                        new TaskModel()
                            .id(resultSet.getInt("id"))
                            .name(resultSet.getString("name"))
                            .startDate(resultSet.getDate("start_date").toString())
                            .endDate(resultSet.getDate("end_date").toString())
                            .statusName(resultSet.getString("status_name"))
                );
            }
            return taskModelList;
        });
    }
    public TaskModel findTaskByUserIdAndTaskId(int userId, int taskId) {
        String sql = """
                SELECT t.id, t.name, t.start_date, t.end_date, u.full_name user_name, j.name job_name,
                s.name status_name, t.note
                FROM tasks t
                LEFT JOIN users u ON t.user_id = u.id
                LEFT JOIN jobs j ON t.job_id = j.id
                LEFT JOIN status s ON t.status_id = s.id
                WHERE t.user_id = ? AND t.id = ?
                """;
        return processObjQuery(sql, preparedStatement -> {
            ResultSet resultSet = fillStatement(preparedStatement, userId, taskId).executeQuery();
            if (resultSet.next()) {
                return resultSetToTaskModel(resultSet);
            }
            return null;
        });
    }
    public int updateProfileTask(TaskModel taskModel) {
        String sql = """
                UPDATE tasks
                SET note = ?,
                status_id = (SELECT max(id) FROM status WHERE name = ?)
                WHERE id = ?
                """;
        return processObjQuery(sql, preparedStatement -> fillStatement(preparedStatement,
                taskModel.getNote(), taskModel.getStatusName(), taskModel.getId()).executeUpdate());
    }
}
