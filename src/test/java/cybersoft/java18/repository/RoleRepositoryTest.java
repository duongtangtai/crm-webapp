package cybersoft.java18.repository;

import cybersoft.java18.model.RoleModel;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class RoleRepositoryTest {
    RoleRepository roleRepository = new RoleRepository();
    @Test
    void findRoleTest() { //ID=2 is valid, ID=10 is invalid
        Assertions.assertNotNull(roleRepository.findRoleById(2));
        Assertions.assertNull(roleRepository.findRoleById(10));
    }
    @Test
    void addRoleAndDeleteRoleTest() { //add two new roleModels, id would be 4 and 5 so we can compare them easily
        //before add
        Assertions.assertNull(roleRepository.findRoleById(4));
        Assertions.assertNull(roleRepository.findRoleById(5));
        //add roles
        RoleModel roleModel1 = new RoleModel().id(4).name("Test1").description("Người quản lý cao nhất");
        RoleModel roleModel2 = new RoleModel().id(5).name("Test2").description("");
        roleRepository.addRole(roleModel1);
        roleRepository.addRole(roleModel2);
        //check whether it succeeded
        Assertions.assertEquals(roleModel1, roleRepository.findRoleById(4));
        Assertions.assertEquals(roleModel2, roleRepository.findRoleById(5));
        //delete roles with id = 4 & 5
        roleRepository.deleteRoleById(4);
        roleRepository.deleteRoleById(5);
        //check if deletion succeeded
        Assertions.assertNull(roleRepository.findRoleById(4));
        Assertions.assertNull(roleRepository.findRoleById(5));
    }
    @Test
    void updateRoleTest() { //update a role then check if the update succeeded
        RoleModel roleModel = roleRepository.findRoleById(3).name("Test Role").description("Test Description");
        roleRepository.updateRole(roleModel);
        RoleModel newRoleModel = roleRepository.findRoleById(3);
        Assertions.assertEquals(roleModel, newRoleModel);
    }
}
