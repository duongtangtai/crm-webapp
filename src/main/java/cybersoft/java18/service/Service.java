package cybersoft.java18.service;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.google.gson.Gson;
import cybersoft.java18.model.*;
import cybersoft.java18.repository.*;
import cybersoft.java18.utils.FileUtils;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class Service {
    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final JobRepository jobRepository;
    private final TaskRepository taskRepository;
    private final StatusRepository statusRepository;
    private Gson gson;
    private Algorithm algorithm;
    private BCryptPasswordEncoder passwordEncoder;
    Service() {
        roleRepository = new RoleRepository();
        userRepository = new UserRepository();
        jobRepository = new JobRepository();
        taskRepository = new TaskRepository();
        statusRepository = new StatusRepository();
    }
    public Gson getGson() {
        if (gson == null) {
            gson = new Gson();
        }
        return gson;
    }
    public ResponseData createResponseData(int statusCode, boolean successful, String message, Object content) {
        return new ResponseData()
                .statusCode(statusCode)
                .successful(successful)
                .message(message)
                .content(content);
    }

    public Algorithm getAlgorithm() {
        if (algorithm == null) {
            algorithm = Algorithm.HMAC256("myLovelySecret".getBytes());
        }
        return algorithm;
    }

    public BCryptPasswordEncoder getPasswordEncoder() {
        if (passwordEncoder == null) {
            passwordEncoder = new BCryptPasswordEncoder();
        }
        return passwordEncoder;
    }
    public String createToken(String userId, String issuer, String role, Date expiredDate) {
        return JWT.create()
                .withSubject(userId)
                .withIssuer(issuer)
                .withClaim("role", role)
                .withExpiresAt(expiredDate)
                .sign(getAlgorithm());
    }
    public DecodedJWT decodingToken(HttpServletRequest req) {
        String token = req.getHeader("Authorization").substring("Bearer ".length());
        JWTVerifier verifier = JWT.require(getAlgorithm()).build();
        return verifier.verify(token);
    }
    public void returnJsonData(HttpServletResponse resp, Object data) throws IOException {
        try (PrintWriter writer = resp.getWriter()) {
            String json = getGson().toJson(data);
            writer.println(json);
            writer.flush();
        } catch (IOException e) {
            throw new IOException("An error occurred while returning json data");
        }
    }
    //------------ROLE----------------
    public int addRole(RoleModel roleModel) {
        return roleRepository.addRole(roleModel);
    }

    public RoleModel findRoleById(int id) {
        return roleRepository.findRoleById(id);
    }

    public List<RoleModel> findAllRoles() {
        return roleRepository.findAllRoles();
    }

    public int updateRole(RoleModel roleModel) {
        return roleRepository.updateRole(roleModel);
    }

    public int deleteRoleById(int id) {
        return roleRepository.deleteRoleById(id);
    }

    //------------USER----------------

    public int addUser(UserModel userModel) {
        return userRepository.addUser(userModel.password(getPasswordEncoder().encode(userModel.getPassword())));
    }

    public UserModel findUserById(int id) {
        UserModel userModel = userRepository.findUserById(id);
        if (userModel == null) {
            return null;
        }
        return userModel
                .statistics(taskRepository.findStatisticByUserId(id))
                .taskList(taskRepository.findTaskByUserId(id))
                .avatar(getAvatarURL(userModel.getAvatar()));
    }
    public UserModel findUserProfileById(int id) {
        UserModel userModel = userRepository.findUserProfileById(id);
        return userModel.avatar(getAvatarURL(userModel.getAvatar()));
    }
    public String findUserRoleById(int id) {
        return userRepository.findUserRoleById(id);
    }

    public List<UserModel> findAllUsers() {
        return userRepository.findAllUsers();
    }

    public int updateUser(UserModel userModel) {
        return userRepository.updateUser(userModel);
    }

    public int updateUserProfile(UserModel userModel) {
        return userRepository.updateUserProfile(userModel);
    }

    public int deleteUserById(int id) {
        return userRepository.deleteUserById(id);
    }

    public void saveUserRefreshToken(String refreshToken, int id) {
        userRepository.saveRefreshTokenByUserId(refreshToken, id);
    }

    public boolean isRefreshTokenValid(String inputRefreshToken, int id) {
        return inputRefreshToken.equals(userRepository.findRefreshTokenById(id));
    }
    public int saveUserAvatar(String avatar, int id) {
        return userRepository.saveAvatar(avatar, id);
    }
    private String getAvatarURL(String avatar) {
        if (avatar != null) {
            avatar = FileUtils.API_FILE + avatar;
        }
        return avatar;
    }
    //------------JOB----------------

    public int addJob(JobModel jobModel) {
        return jobRepository.addJob(jobModel);
    }

    public JobModel findJobById(int id) { //find all users, each user has his own task list
        List<UserModel> userModelList = userRepository.findUserByJobId(id);
        if (userModelList.size() == 0) {
            return null;
        }
        userModelList.forEach(e -> {
                    e.taskList(taskRepository.findTaskByUserIdAndJobId(e.getId(), id));
                    e.setAvatar(e.getAvatar() == null ? (null) : (FileUtils.API_FILE + e.getAvatar()));
                }
        );
        return new JobModel()
                    .id(id)
                    .statistics(jobRepository.findStatisticById(id))
                    .userModelList(userModelList);
    }

    public List<JobModel> findAllJobs() {
        return jobRepository.findAllJobs();
    }

    public int updateJob(JobModel jobModel) {
        return jobRepository.updateJob(jobModel);
    }

    public int deleteJobById(int id){
        return jobRepository.deleteJobById(id);
    }

    //------------TASK----------------

    public int addTask(TaskModel taskModel) {
        return taskRepository.addTask(taskModel);
    }

    public TaskModel findTaskById(int id) {
        return taskRepository.findTaskById(id);
    }

    public List<TaskModel> findAllTasks() {
        return taskRepository.findAllTasks();
    }

    public int updateTask(TaskModel taskModel) {
        return taskRepository.updateTask(taskModel);
    }

    public int deleteTaskById(int id) {
        return taskRepository.deleteTaskById(id);
    }

    public TaskModel getTaskByUserIdAndTaskId(int userId, int taskId) {
        return taskRepository.findTaskByUserIdAndTaskId(userId, taskId);
    }

    public int updateProfileTask(TaskModel taskModel) { //update status and note
        return taskRepository.updateProfileTask(taskModel);
    }
    //------------STATUS----------------

    public void addStatus(StatusModel statusModel) {
        statusRepository.addStatus(statusModel);
    }

    public StatusModel findStatusById(int id) {
        return statusRepository.findStatusById(id);
    }

    public List<StatusModel> findAllStatus() {
        return statusRepository.findAllStatus();
    }

    public void updateStatus(StatusModel statusModel) {
        statusRepository.updateStatus(statusModel);
    }

    public void deleteStatus(int id) {
        statusRepository.deleteStatus(id);
    }
    //------------AUTH-----------------
    /**
     *  This function will check if the input email & password are valid.
     *  It returns NULL if they are invalid. Otherwise it returns UserModel including
     *  id, fullName and avatar url for the user
     */
    public UserModel login(String inputEmail, String inputPassword) {
        UserModel userModel = userRepository.findUserByEmail(inputEmail);
        if (userModel == null) { //invalid email & password
            return null;
        }
        return getPasswordEncoder().matches(inputPassword, userModel.getPassword())
                ? userModel.password(null) //set password null so it won't be transferred back to the page
                            .avatar(getAvatarURL(userModel.getAvatar()))
                : null;
    }
    /**
     * This function will remove the user's refresh token. In case their token has been stolen, that token will no longer
     * valid. The access token can't be refreshed with an invalid refresh token
     */
    public int logout(int userId) {
        return userRepository.logout(userId);
    }

    /**
     * This function will check whether the old password is correct. If it's correct, the function will replace the
     * old password with the new one. Return 1 if successful, otherwise return 0
     */
    public int changeUserPW(int userId, String oldPW, String newPW) {
        //check whether the old PW is correct
        if (getPasswordEncoder().matches(oldPW, userRepository.findUserPWById(userId))) { // if it matches
            return userRepository.changeUserPWById(userId, getPasswordEncoder().encode(newPW)); //replace oldPW with newPW
        } else {
            return 0; //it doesn't matches
        }
    }
    //-------------------------DASHBOARD--------------------------
    public Map<String, Integer> findDashBoardStatistics() {
        return taskRepository.findAllTaskStatistics();
    }
}
