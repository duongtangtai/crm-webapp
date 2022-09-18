package cybersoft.java18.repository;

import cybersoft.java18.jdbc.JDBCFunction;
import cybersoft.java18.model.JobModel;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JobRepository extends JDBCFunction {
    private JobModel resultSetToJobModel(ResultSet resultSet) throws SQLException {
        return new JobModel()
                .id(resultSet.getInt("id"))
                .name(resultSet.getString("name"))
                .startDate(resultSet.getDate("start_date").toString())
                .endDate(resultSet.getDate("end_date").toString());
    }

    private int addOrUpdateJob(JobModel jobModel, String sql) {
        return processObjQuery(sql, preparedStatement -> {
            int id = jobModel.getId();
            String name = jobModel.getName();
            LocalDate startDate = LocalDate.parse(jobModel.getStartDate(), DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            LocalDate endDate = LocalDate.parse(jobModel.getEndDate(), DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            if (sql.startsWith("INSERT")) {
                return fillStatement(preparedStatement, name, startDate, endDate).executeUpdate();
            }
            return fillStatement(preparedStatement, name, startDate, endDate, id).executeUpdate();
        });
    }

    public int addJob(JobModel jobModel) {
        String sql = "INSERT INTO jobs (name, start_date, end_date) VALUES ( ?, ?, ?)";
        return addOrUpdateJob(jobModel, sql);
    }

    public List<JobModel> findAllJobs() {
        String sql = """
                SELECT id, name, start_date, end_date
                FROM jobs
                """;
        return processListQuery(sql, preparedStatement -> {
            ResultSet resultSet = preparedStatement.executeQuery();
            List<JobModel> jobModelList = new ArrayList<>();
            while (resultSet.next()) {
                jobModelList.add(resultSetToJobModel(resultSet));
            }
            return jobModelList;
        });
    }

    public int updateJob(JobModel jobModel) {
        String sql = """
                UPDATE jobs
                SET name = ?, start_date = ?, end_date = ?
                WHERE id = ?
                """;
        return addOrUpdateJob(jobModel, sql);
    }

    public int deleteJobById(int id) {
        String sql = "DELETE FROM jobs WHERE id = ?";
        return processObjQuery(sql, preparedStatement -> fillStatement(preparedStatement, id).executeUpdate());
    }

    public Map<String, Integer> findStatisticById(int id) {
        String sql = """
                SELECT st.name, count(j.id) num_of_tasks
                FROM jobs j
                JOIN tasks t ON j.id = t.job_id
                JOIN status st ON t.status_id = st.id
                WHERE j.id = ?
                GROUP BY st.name;
                """;
        return processObjQuery(sql, preparedStatement -> {
            ResultSet resultSet = fillStatement(preparedStatement, id).executeQuery();
            Map<String, Integer> output = new HashMap<>();
            while (resultSet.next()) {
                output.put(resultSet.getString("name"),resultSet.getInt("num_of_tasks"));
            }
            return output;
        });
    }
}
