package cybersoft.java18.api;

import cybersoft.java18.model.ResponseData;
import cybersoft.java18.model.RoleModel;
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

@WebServlet(name = "RoleController", urlPatterns = {
        UrlUtils.ROLE,
        UrlUtils.ROLE_ALL
})
public class RoleController extends HttpServlet {
    private Service service;
    @Override
    public void init() {
        service = ServiceHolder.getService();
    }
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            if (req.getPathInfo() == null || req.getPathInfo().equals("/")) {
                processAllRoles(resp);
            } else if (Pattern.matches("\\d+", req.getPathInfo().substring(1))) {
                processSpecificRole(req, resp);
            } else {
                req.getRequestDispatcher(JspUtils.NOT_FOUND).forward(req, resp);
            }
        } catch (Exception e) {
            ResponseData responseData = service.createResponseData(200,false,
                    "Internal Error in doGet Role API: " + e.getMessage(), null);
            service.returnJsonData(resp, responseData);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            switch (req.getServletPath() + req.getPathInfo()) {
                case UrlUtils.ROLE_ADD -> processAdd(req, resp);
                case UrlUtils.ROLE_UPDATE -> processUpdate(req, resp);
                case UrlUtils.ROLE_DELETE -> processDelete(req, resp);
                default -> req.getRequestDispatcher(JspUtils.NOT_FOUND).forward(req, resp);
            }
        } catch (Exception e) {
            ResponseData responseData = service.createResponseData(200,false,
                    "Internal Error in doPost Role API: " + e.getMessage(), null);
            service.returnJsonData(resp, responseData);
        }
    }
    private void processAllRoles(HttpServletResponse resp) throws IOException {
        ResponseData responseData = service.createResponseData(200, true,
                "Get all roles successfully!", service.findAllRoles());
        service.returnJsonData(resp, responseData);
    }
    private void processSpecificRole(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        int roleId = Integer.parseInt(req.getPathInfo().substring(1));
        RoleModel roleModel = service.findRoleById(roleId);
        ResponseData responseData;
        if (roleModel == null) {
            responseData = service.createResponseData(200, false,"Invalid roleId", null);
        } else {
            responseData = service.createResponseData(200, true,
                    "Get specific role successfully!", roleModel);
        }
        service.returnJsonData(resp, responseData);
    }
    private void processAdd(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String json = req.getReader().lines().collect(Collectors.joining());
        RoleModel roleModel = service.getGson().fromJson(json, RoleModel.class);
        int result = service.addRole(roleModel);
        ResponseData responseData;
        if (result == 0) {
            responseData = service.createResponseData(200,false, "Failed to add role!", null);
        } else {
            responseData = service.createResponseData(200,true, "Added role successfully!", null);
        }
        service.returnJsonData(resp, responseData);
    }
    private void processUpdate(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String json = req.getReader().lines().collect(Collectors.joining());
        RoleModel roleModel = service.getGson().fromJson(json, RoleModel.class);
        int result = service.updateRole(roleModel);
        ResponseData responseData;
        if (result == 0) {
            responseData = service.createResponseData(200,false, "Failed to update role!", null);
        } else {
            responseData = service.createResponseData(200,true, "Updated role successfully!", null);
        }
        service.returnJsonData(resp, responseData);
    }
    private void processDelete(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        int result = service.deleteRoleById(Integer.parseInt(req.getParameter("id")));
        ResponseData responseData;
        if (result == 0) {
            responseData = service.createResponseData(200,false, "Failed to delete role!", null);
        } else {
            responseData = service.createResponseData(200,true, "Deleted role successfully!", null);
        }
        service.returnJsonData(resp, responseData);
    }
}
