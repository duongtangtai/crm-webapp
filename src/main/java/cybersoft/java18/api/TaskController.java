package cybersoft.java18.api;

import cybersoft.java18.model.ResponseData;
import cybersoft.java18.model.TaskModel;
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

@WebServlet(name = "TaskController", urlPatterns = {
        UrlUtils.TASK,
        UrlUtils.TASK_ALL
})
public class TaskController extends HttpServlet {
    private Service service;
    @Override
    public void init() {
        service = ServiceHolder.getService();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            if (req.getPathInfo() == null || req.getPathInfo().equals("/")) { //get all users
                processAllTasks(resp);
            } else if (Pattern.matches("\\d+", req.getPathInfo().substring(1))) { //get specific user
                processSpecificTask(req, resp);
            } else { //other get methods
                req.getRequestDispatcher(JspUtils.NOT_FOUND).forward(req, resp);
            }
        } catch (Exception e) {
            ResponseData responseData = service.createResponseData(200,false,
                    "Internal Error in doGet Task API: " + e.getMessage(), null);
            service.returnJsonData(resp, responseData);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            switch (req.getServletPath() + req.getPathInfo()) {
                case UrlUtils.TASK_ADD -> processAddTask(req, resp);
                case UrlUtils.TASK_UPDATE -> processUpdateTask(req, resp);
                case UrlUtils.TASK_DELETE -> processDelete(req, resp);
                default -> req.getRequestDispatcher(JspUtils.NOT_FOUND).forward(req, resp);
            }
        } catch (Exception e) {
            ResponseData responseData = service.createResponseData(200,false,
                    "Internal Error in doPost Task API: " + e.getMessage(), null);
            service.returnJsonData(resp, responseData);
        }
    }
    private void processAllTasks(HttpServletResponse resp) throws IOException {
        ResponseData responseData = service.createResponseData(200, true,
                "Get all tasks successfully", service.findAllTasks());
        service.returnJsonData(resp, responseData);
    }
    private void processSpecificTask(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        int taskId = Integer.parseInt(req.getPathInfo().substring(1));
        TaskModel taskModel = service.findTaskById(taskId);
        ResponseData responseData;
        if (taskModel == null) {
            responseData = service.createResponseData(200, false,"Invalid taskId", null);
        } else {
            responseData = service.createResponseData(200, true,
                    "Get specific task successfully", taskModel);
        }
        service.returnJsonData(resp, responseData);
    }
    private void processAddTask(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String json = req.getReader().lines().collect(Collectors.joining());
        TaskModel taskModel = service.getGson().fromJson(json, TaskModel.class);
        int result = service.addTask(taskModel);
        ResponseData responseData;
        if (result == 0) {
            responseData = service.createResponseData(200,false, "Failed to add task!", null);
        } else {
            responseData = service.createResponseData(200,true, "Added task successfully!", null);
        }
        service.returnJsonData(resp, responseData);
    }
    private void processUpdateTask(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String json = req.getReader().lines().collect(Collectors.joining());
        TaskModel taskModel = service.getGson().fromJson(json, TaskModel.class);
        int result = service.updateTask(taskModel);
        ResponseData responseData;
        if (result == 0) {
            responseData = service.createResponseData(200,false, "Failed to update task!", null);
        } else {
            responseData = service.createResponseData(200,true, "Updated task successfully!", null);
        }
        service.returnJsonData(resp, responseData);
    }
    private void processDelete(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        int result = service.deleteTaskById(Integer.parseInt(req.getParameter("id")));
        ResponseData responseData;
        if (result == 0) {
            responseData = service.createResponseData(200,false, "Failed to delete task!", null);
        } else {
            responseData = service.createResponseData(200,true, "Deleted task successfully!", null);
        }
        service.returnJsonData(resp, responseData);
    }
}

