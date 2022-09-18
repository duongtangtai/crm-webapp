package cybersoft.java18.model;

import lombok.Data;
@Data
public class TaskModel {
    private int id;
    private String name;
    private String jobName;
    private String userName;
    private String startDate;
    private String endDate;
    private String statusName;
    private String note;
    public TaskModel id(int id) {
        this.id = id;
        return this;
    }
    public TaskModel name(String name) {
        this.name = name;
        return this;
    }
    public TaskModel startDate(String startDate) {
        this.startDate = startDate;
        return this;
    }
    public TaskModel endDate(String endDate) {
        this.endDate = endDate;
        return this;
    }
    public TaskModel userName(String userName) {
        this.userName = userName;
        return this;
    }
    public TaskModel jobName(String jobName) {
        this.jobName = jobName;
        return this;
    }
    public TaskModel statusName(String statusName) {
        this.statusName = statusName;
        return this;
    }
    public TaskModel note(String note) {
        this.note = note;
        return this;
    }
}
