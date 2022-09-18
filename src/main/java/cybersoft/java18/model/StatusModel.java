package cybersoft.java18.model;

import lombok.Data;

@Data
public class StatusModel {
    private int id;
    private String name;
    public StatusModel id(int id) {
        this.id = id;
        return this;
    }
    public StatusModel name(String name) {
        this.name = name;
        return this;
    }
}
