package cybersoft.java18.repository;

import cybersoft.java18.model.UserModel;
import cybersoft.java18.service.Service;
import cybersoft.java18.service.ServiceHolder;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

class UserRepositoryTest {
    UserRepository userRepository = new UserRepository();
    @Test
    void findUserRoleByIdTest() {
        //id = 1 => role should be "Giám đốc"
        Assertions.assertEquals("Giám đốc", userRepository.findUserRoleById(1));
        //id = 10 => invalid ID should be null
        Assertions.assertNull(userRepository.findUserRoleById(10));
    }
    @Test
    void findAllUsersTest() {
        Assertions.assertNotNull(userRepository.findAllUsers());
    }
    @Test
    void findUserProfileByIdTest() {
        UserModel userModel = new UserModel().id(1).email("admin@gmail.com").fullName("Admin").phoneNum("123456789")
                .avatar(null);
        Assertions.assertEquals(userModel, userRepository.findUserProfileById(1));
        Assertions.assertNull(userRepository.findUserProfileById(10));//wrong id will return null
    }
    @Test
    void findUserByIdTest() {
        UserModel userModel = new UserModel().id(1).email("admin@gmail.com").fullName("Admin").avatar(null);
        Assertions.assertEquals(userModel, userRepository.findUserById(1));
        Assertions.assertNull(userRepository.findUserById(10));//wrong id will return null
    }
    @Test
    void findUserByEmailTest() {
        UserModel userModel = new UserModel().id(1)
                .password("$2a$10$ucZpUlftGBfrXLbcCXf8TOUNVqj5A78BSpF592uzWBLfv./Riu/0a")
                .fullName("Admin")
                .avatar(null)
                .role("Giám đốc");
        Assertions.assertEquals(userModel, userRepository.findUserByEmail("admin@gmail.com"));
        Assertions.assertNull(userRepository.findUserByEmail("someStrangeEmail@gmail.com"));//wrong email will return null
    }
    @Test
    void findUserByJobIdTest() {
        //find a list of users (info including: id, fullName, avatar)
        //for instance, job with ID = 1 will have 2 userModels with id 1 & 4
        UserModel userModel1 = new UserModel().id(1).fullName("Admin").avatar(null);
        UserModel userModel2 = new UserModel().id(4).fullName("Dương Kim Ngân").avatar(null);
        List<UserModel> userList = new ArrayList<>();
        userList.add(userModel1);
        userList.add(userModel2);
        Assertions.assertEquals(userList, userRepository.findUserByJobId(1));
        Assertions.assertEquals(0, userRepository.findUserByJobId(10).size()); //wrong job id will return empty list
    }
    @Test
    void findAndChangeUserPasswordByIdTest() {
        //test findUserPWByID
        Service service = ServiceHolder.getService();
        Assertions.assertEquals(true, service.getPasswordEncoder().matches("admin", userRepository.findUserPWById(1)));
        //test changeUserPWById
        String newPW = "newPassword";
        userRepository.changeUserPWById(1, service.getPasswordEncoder().encode(newPW));
        Assertions.assertEquals(true, service.getPasswordEncoder().matches(newPW, userRepository.findUserPWById(1)));
    }
    @Test
    void logoutAndSaveAndFindRefreshTokenByIdTest() {
        //before having refresh token
        Assertions.assertNull(userRepository.findRefreshTokenById(1));//null because user haven't logged in yet
        //save refresh token
        String refreshToken = "A brand new refreshToken";
        userRepository.saveRefreshTokenByUserId(refreshToken, 1);
        Assertions.assertEquals(refreshToken, userRepository.findRefreshTokenById(1)); // now user have the refresh token
        //when user log out, refresh token will be erased
        userRepository.logout(1);
        Assertions.assertNull(userRepository.findRefreshTokenById(1));
    }
    @Test
    void addUserAndDeleteUserTest() {
        //add a new user
        UserModel userModel = new UserModel().email("test@gmail.com").password("123").fullName("testName")
                .phoneNum("0190491231").role("Nhân viên");
        userRepository.addUser(userModel);
        //check whether it succeeded with findUserByEmail() function
        //set email = null and id = 5 because findUserByEmail doesn't return email value but it returns id
        Assertions.assertEquals(userModel.email(null).phoneNum(null).id(5), userRepository.findUserByEmail("test@gmail.com"));
        //before delete user with id = 5
        Assertions.assertNotNull(userRepository.findUserById(5));
        //delete user with id = 5
        userRepository.deleteUserById(5);
        //after deletion
        Assertions.assertNull(userRepository.findUserById(5));
        Assertions.assertNull(userRepository.findUserByEmail("test@gmail.com"));
    }
    @Test
    void updateUserTest() {
        UserModel userModel = new UserModel().id(3).email("testUpdate@gmail.com").fullName("testUpdateName")
                .phoneNum("1221212").role("Nhân viên");
        userRepository.updateUser(userModel);
        //set phoneNum and role = null cz we don't access them
        Assertions.assertEquals(userModel.phoneNum(null).role(null), userRepository.findUserById(3));
    }
    @Test
    void updateUserProfileTest() {
        UserModel userModel = new UserModel().id(4).email("testUpdateProfile@gmail.com").fullName("testUpdateName")
                .phoneNum("1221212");
        userRepository.updateUserProfile(userModel);
        //set phoneNum = null cz we don't access them
        Assertions.assertEquals(userModel.phoneNum(null), userRepository.findUserById(4));
    }
    @Test
    void saveAvatarTest() {
        userRepository.saveAvatar("linkToAvatar", 2);
        Assertions.assertEquals("linkToAvatar", userRepository.findUserProfileById(2).getAvatar());
    }
}
