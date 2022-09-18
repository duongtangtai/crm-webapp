package cybersoft.java18.repository;

import cybersoft.java18.model.TaskModel;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class TaskRepositoryTest {
    TaskRepository taskRepository = new TaskRepository();
    @Test
    void findTaskByIdTest() {
        TaskModel taskModel = new TaskModel().id(1).name("Công việc 1").startDate("2020-12-10").endDate("2020-12-20")
                .userName("Admin").jobName("Dự án A").statusName("Chưa bắt đầu").note("Những việc cần làm? Các bước thực hiện?");
        Assertions.assertEquals(taskModel, taskRepository.findTaskById(1));
        Assertions.assertNull(taskRepository.findTaskById(15)); //wrong taskId should return null
    }
    @Test
    void findTaskByUserId() {
        String sql = """
                SELECT t.id, t.name, t.start_date, t.end_date, st.name as status_name, j.name as job_name
                FROM tasks t JOIN status st
                ON t.status_id = st.id
                JOIN jobs j
                ON t.job_id = j.id
                WHERE t.user_id = ?
                """;
        TaskModel taskModel1 = new TaskModel().id(2).name("Công việc 2").startDate("2020-12-21").endDate("2020-12-30")
                .statusName("Đang thực hiện").jobName("Dự án B");
        TaskModel taskModel2 = new TaskModel().id(5).name("Công việc 6").startDate("2020-12-21").endDate("2020-12-30")
                .statusName("Chưa bắt đầu").jobName("Dự án B");
        TaskModel taskModel3 = new TaskModel().id(9).name("Công việc 10").startDate("2021-01-10").endDate("2021-01-20")
                .statusName("Đang thực hiện").jobName("Dự án C");
        List<TaskModel> taskList = new ArrayList<>();
        taskList.add(taskModel1);
        taskList.add(taskModel2);
        taskList.add(taskModel3);
        Assertions.assertEquals(taskList, taskRepository.findTaskByUserId(2));
        Assertions.assertEquals(0, taskRepository.findTaskByUserId(5).size()); //if userID is invalid
    }
    @Test
    void findTaskByUserIdAndTaskId() {
        TaskModel taskModel = new TaskModel().id(1).name("Công việc 1").startDate("2020-12-10").endDate("2020-12-20")
                .userName("Admin").jobName("Dự án A").statusName("Chưa bắt đầu").note("Những việc cần làm? Các bước thực hiện?");
        Assertions.assertEquals(taskModel, taskRepository.findTaskByUserIdAndTaskId(1, 1));
        Assertions.assertNull(taskRepository.findTaskByUserIdAndTaskId(1, 2));
    }
    @Test
    void findTaskByUserIdAndJobId() {
        TaskModel taskModel1 = new TaskModel().id(1).name("Công việc 1").startDate("2020-12-10").endDate("2020-12-20")
                .statusName("Chưa bắt đầu");
        TaskModel taskModel2 = new TaskModel().id(4).name("Công việc 5").startDate("2020-12-10").endDate("2020-12-20")
                .statusName("Chưa bắt đầu");
        List<TaskModel> taskList = new ArrayList<>();
        taskList.add(taskModel1);
        taskList.add(taskModel2);
        Assertions.assertEquals(taskList, taskRepository.findTaskByUserIdAndJobId(1, 1));
        Assertions.assertEquals(0, taskRepository.findTaskByUserIdAndJobId(1, 2).size());//empty if none matches
    }
    @Test
    void findAllTasksTest() {
        List<TaskModel> taskList = taskRepository.findAllTasks();
        Assertions.assertEquals(12, taskList.size());
        //check some tasks
        TaskModel taskModel1 = new TaskModel().id(3).name("Công việc 4").startDate("2021-01-10").endDate("2021-01-20")
                .userName("Trần Mỹ Nhi").jobName("Dự án C").statusName("Đã hoàn thành")
                .note("Những việc cần làm? Các bước thực hiện?");
        TaskModel taskModel2 = new TaskModel().id(7).name("Công việc 8").startDate("2021-01-10").endDate("2021-01-20")
                .userName("Admin").jobName("Dự án C").statusName("Đang thực hiện")
                .note("Những việc cần làm? Các bước thực hiện?");
        Assertions.assertEquals(taskModel1, taskList.get(2));
        Assertions.assertEquals(taskModel2, taskList.get(6));
    }
    @Test
    void findStatisticByIdTest() {
        Map<String, Integer> map = new HashMap<>();
        map.put("Chưa bắt đầu", 2);
        map.put("Đang thực hiện", 1);
        map.put("Đã hoàn thành", 1);
        Assertions.assertEquals(map, taskRepository.findStatisticByUserId(1));
        map.clear();
        map.put("Đang thực hiện", 2);
        map.put("Chưa bắt đầu", 1);
        Assertions.assertEquals(map, taskRepository.findStatisticByUserId(2));
        Assertions.assertEquals(0, taskRepository.findStatisticByUserId(5).size()); //empty if userId is invalid
    }
    @Test
    void addTaskAndDeleteTaskTest() {
        //add task
        TaskModel taskModel = new TaskModel().name("TestTask").startDate("1111-11-11").endDate("1212-12-12")
                .userName("Admin").jobName("Dự án B").statusName("Chưa bắt đầu");
        taskRepository.addTask(taskModel);
        //check whether it succeeded
        Assertions.assertEquals(taskModel.id(13) , taskRepository.findTaskById(13)); //id=13 cz we already have 12 tasks
        //delete task with id = 13
        taskRepository.deleteTaskById(13);
        //check whether it succeeded
        Assertions.assertNull(taskRepository.findTaskById(13));
    }
    @Test
    void updateTaskTest() {
        TaskModel taskModel = taskRepository.findTaskById(12); //take a task out and update it
        taskModel.name("newName").startDate("1010-02-12").endDate("1010-12-01").statusName("Chưa bắt đầu");
        //update task
        taskRepository.updateTask(taskModel);
        //check whether it succeeded
        Assertions.assertEquals(taskModel, taskRepository.findTaskById(12));
    }
    @Test
    void updateProfileTaskTest() {
        TaskModel taskModel = taskRepository.findTaskById(11); //take a task out and update it
        //update note and status
        taskModel.note("new note").statusName("Đã hoàn thành");
        //update profile task
        taskRepository.updateProfileTask(taskModel);
        //check whether it succeeded
        Assertions.assertEquals(taskModel, taskRepository.findTaskById(11));
    }
}
