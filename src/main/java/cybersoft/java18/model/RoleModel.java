package cybersoft.java18.model;

import lombok.Data;

@Data
public class RoleModel {
    private int id;
    private String name;
    private String description;
    public RoleModel id (int id) {
        this.id = id;
        return this;
    }
    public RoleModel name (String name) {
        this.name = name;
        return this;
    }
    public RoleModel description (String description) {
        this.description = description;
        return this;
    }
}
