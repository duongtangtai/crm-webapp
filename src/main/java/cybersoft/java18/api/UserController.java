package cybersoft.java18.api;

import cybersoft.java18.model.ResponseData;
import cybersoft.java18.model.UserModel;
import cybersoft.java18.service.Service;
import cybersoft.java18.service.ServiceHolder;
import cybersoft.java18.utils.JspUtils;
import cybersoft.java18.utils.UrlUtils;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@WebServlet(name = "UserController", urlPatterns = {
        UrlUtils.USER,
        UrlUtils.USER_ALL
})
public class UserController extends HttpServlet {
    private Service service;

    @Override
    public void init() {
        service = ServiceHolder.getService();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            if (req.getPathInfo() == null || req.getPathInfo().equals("/")) { //get all users
                processAllUsers(resp);
            } else if (Pattern.matches("\\d+", req.getPathInfo().substring(1))) { //get specific user
                processSpecificUser(req, resp);
            } else { //other get methods
                req.getRequestDispatcher(JspUtils.NOT_FOUND).forward(req, resp);
            }
        } catch (Exception e) {
            ResponseData responseData = service.createResponseData(200, false,
                    "Internal Error in doGet User API: " + e.getMessage(), null);
            service.returnJsonData(resp, responseData);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            switch (req.getServletPath() + req.getPathInfo()) {
                case UrlUtils.USER_ADD -> processAddUser(req, resp);
                case UrlUtils.USER_UPDATE -> processUpdateUser(req, resp);
                case UrlUtils.USER_DELETE -> processDeleteUser(req, resp);
                default -> req.getRequestDispatcher(JspUtils.NOT_FOUND).forward(req, resp);
            }
        } catch (Exception e) {
            ResponseData responseData = service.createResponseData(200,false,
                    "Internal Error in doPost User API: " + e.getMessage(), null);
            service.returnJsonData(resp, responseData);
        }
    }
    private void processAllUsers(HttpServletResponse resp) throws IOException {
        ResponseData responseData = service.createResponseData(200, true,
                "Get all users successfully", service.findAllUsers());
        service.returnJsonData(resp, responseData);
    }
    private void processSpecificUser(HttpServletRequest req, HttpServletResponse resp) throws IOException, NumberFormatException {
        int id = Integer.parseInt(req.getPathInfo().substring(1));
        UserModel userModel = service.findUserById(id);
        ResponseData responseData;
        if (userModel == null) {
            responseData = service.createResponseData(200, false,"Invalid userId!", null);
        } else {
            responseData = service.createResponseData(200, true,
                    "Get specific user successfully", userModel);
        }
        service.returnJsonData(resp, responseData);
    }
    private void processAddUser(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String json = req.getReader().lines().collect(Collectors.joining());
        UserModel userModel = service.getGson().fromJson(json, UserModel.class);
        int result = service.addUser(userModel);
        ResponseData responseData;
        if (result == 0) {
            responseData = service.createResponseData(200,false, "Failed to add user!", null);
        } else {
            responseData = service.createResponseData(200,true, "Added user successfully!", null);
        }
        service.returnJsonData(resp, responseData);
    }
    private void processUpdateUser(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String json = req.getReader().lines().collect(Collectors.joining());
        UserModel userModel = service.getGson().fromJson(json, UserModel.class);
        int result = service.updateUser(userModel);
        ResponseData responseData;
        if (result == 0) {
            responseData = service.createResponseData(200,false, "Failed to update user!", null);
        } else {
            responseData = service.createResponseData(200,true, "Updated user successfully!", null);
        }
        service.returnJsonData(resp, responseData);
    }
    private void processDeleteUser(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        int result = service.deleteUserById(Integer.parseInt(req.getParameter("id")));
        ResponseData responseData;
        if (result == 0) {
            responseData = service.createResponseData(200,false, "Failed to delete user!", null);
        } else {
            responseData = service.createResponseData(200,true, "Deleted user successfully!", null);
        }
        service.returnJsonData(resp, responseData);
    }
}
