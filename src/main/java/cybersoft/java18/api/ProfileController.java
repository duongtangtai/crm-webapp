package cybersoft.java18.api;

import com.auth0.jwt.interfaces.DecodedJWT;
import cybersoft.java18.model.ResponseData;
import cybersoft.java18.model.TaskModel;
import cybersoft.java18.model.UserModel;
import cybersoft.java18.service.Service;
import cybersoft.java18.service.ServiceHolder;
import cybersoft.java18.utils.JspUtils;
import cybersoft.java18.utils.UrlUtils;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.stream.Collectors;

@WebServlet(name = "profileController", urlPatterns = {
        UrlUtils.PROFILE,
        UrlUtils.PROFILE_TASK_ALL,
        UrlUtils.PROFILE_STATISTIC
})
public class ProfileController extends HttpServlet {
    private Service service;
    @Override
    public void init() throws ServletException {
        service = ServiceHolder.getService();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            switch (req.getServletPath()) {
                case UrlUtils.PROFILE -> processUserProfile(req, resp);
                case UrlUtils.PROFILE_STATISTIC -> processUserStatistic(req, resp);
                case UrlUtils.PROFILE_TASK -> processUserProfileTask(req, resp);
                default ->  req.getRequestDispatcher(JspUtils.NOT_FOUND).forward(req, resp);
            }
        } catch (Exception e) {
            ResponseData responseData = service.createResponseData(200, false,
                            "Internal Error in doGet Profile API: " + e.getMessage(), null);
            service.returnJsonData(resp, responseData);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            switch (req.getServletPath()) {
                case UrlUtils.PROFILE -> processUpdateProfile(req, resp);
                case UrlUtils.PROFILE_TASK -> processUpdateProfileTask(req, resp);
                default -> req.getRequestDispatcher(JspUtils.NOT_FOUND).forward(req, resp);
            }
        } catch (Exception e) {
            ResponseData responseData = service.createResponseData(200,false,
                    "Internal Error in doPost Profile API: " + e.getMessage(), null);
            service.returnJsonData(resp, responseData);
        }
    }
    private void processUserProfile(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        DecodedJWT decodedJWT = service.decodingToken(req);
        int userId = Integer.parseInt(decodedJWT.getSubject());
        UserModel userModel = service.findUserProfileById(userId);
        ResponseData responseData;
        if (userModel == null) {
            responseData = service.createResponseData(200,false,
                    "Failed to get user's profile!", null);
        } else {
            responseData = service.createResponseData(200, true,
                    "Get user's profile successfully!", userModel);
        }
        service.returnJsonData(resp, responseData);
    }
    private void processUserStatistic(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        DecodedJWT decodedJWT = service.decodingToken(req);
        String userId = decodedJWT.getSubject();
        UserModel userModel = service.findUserById(Integer.parseInt(userId));
        ResponseData responseData;
        if (userModel == null) {
            responseData = service.createResponseData(200,false,
                    "Failed to get user's statistics!", null);
        } else {
            responseData = service.createResponseData(200, true,
                    "Get user's statistics successfully!", userModel);
        }
        service.returnJsonData(resp, responseData);
    }
    private void processUserProfileTask(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        DecodedJWT decodedJWT = service.decodingToken(req);
        int userId = Integer.parseInt(decodedJWT.getSubject());
        int taskId = Integer.parseInt(req.getPathInfo().substring(1)); //check if this taskId belongs to this userId
        TaskModel taskModel = service.getTaskByUserIdAndTaskId(userId, taskId);
        ResponseData responseData;
        if (taskModel == null) {
            responseData = service.createResponseData(200,false,
                    "Failed to get user's profile task!", null);
        } else {
            responseData = service.createResponseData(200, true,
                    "Get user's profile task successfully!", taskModel);
        }
        service.returnJsonData(resp, responseData);
    }
    private void processUpdateProfile(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String json = req.getReader().lines().collect(Collectors.joining());
        UserModel userModel = service.getGson().fromJson(json, UserModel.class);
        DecodedJWT decodedJWT = service.decodingToken(req);
        int userId = Integer.parseInt(decodedJWT.getSubject());
        int result = service.updateUserProfile(userModel.id(userId));
        ResponseData responseData;
        if (result == 0) {
            responseData = service.createResponseData(200,false,
                    "Failed to update user's profile!", null);
        } else {
            responseData = service.createResponseData(200,true,
                    "Updated user's profile successfully!", null);
        }
        service.returnJsonData(resp, responseData);
    }
    private void processUpdateProfileTask(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String json = req.getReader().lines().collect(Collectors.joining());
        TaskModel taskModel = service.getGson().fromJson(json, TaskModel.class);
        int result = service.updateProfileTask(taskModel);
        ResponseData responseData;
        if (result == 0) {
            responseData = service.createResponseData(200,false,
                    "Failed to update profile task!", null);
        } else {
            responseData = service.createResponseData(200,true,
                    "Updated profile task successfully!", null);
        }
        service.returnJsonData(resp, responseData);
    }
}
