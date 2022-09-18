package cybersoft.java18.model;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class JobModel {
    private int id;
    private String name;
    private String startDate;
    private String endDate;
    private Map<String, Integer> statistics;
    private List<UserModel> userModelList;
    public JobModel id(int id) {
        this.id = id;
        return this;
    }
    public JobModel name(String name) {
        this.name = name;
        return this;
    }
   public JobModel startDate(String startDate) {
        this.startDate = startDate;
        return this;
   }
   public JobModel endDate(String endDate) {
        this.endDate = endDate;
        return this;
   }
    public JobModel statistics(Map<String, Integer> statistics){
        this.statistics = statistics;
        return this;
    }
    public JobModel userModelList(List<UserModel> userModelList) {
        this.userModelList = userModelList;
        return this;
    }
}
