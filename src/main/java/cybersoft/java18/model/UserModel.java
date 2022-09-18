package cybersoft.java18.model;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class UserModel {
    private int id;
    private String email;
    private String password;
    private String fullName;
    private String avatar;
    private String phoneNum;
    private String role;
    private Map<String, Integer> statistics;
    private List<TaskModel> taskList;
    public UserModel id(int id) {
        this.id = id;
        return this;
    }
    public UserModel email(String email) {
        this.email = email;
        return this;
    }
    public UserModel fullName(String fullName) {
        this.fullName = fullName;
        return this;
    }
    public UserModel password(String password) {
        this.password = password;
        return this;
    }
    public UserModel avatar(String avatar) {
        this.avatar = avatar;
        return this;
    }
    public UserModel phoneNum(String phoneNum) {
        this.phoneNum = phoneNum;
        return this;
    }
    public UserModel role(String role) {
        this.role = role;
        return this;
    }
    public UserModel statistics(Map<String, Integer> statistics) {
        this.statistics = statistics;
        return this;
    }
    public UserModel taskList(List<TaskModel> taskList) {
        this.taskList = taskList;
        return this;
    }
}
