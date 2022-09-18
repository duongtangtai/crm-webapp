package cybersoft.java18.api;

import cybersoft.java18.model.ResponseData;
import cybersoft.java18.model.StatusModel;
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

@WebServlet(name = "StatusController", urlPatterns = {
        UrlUtils.STATUS,
        UrlUtils.STATUS_ALL
})
public class StatusController extends HttpServlet {
    private Service service;

    @Override
    public void init() {
        service = ServiceHolder.getService();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            if (req.getPathInfo() == null || req.getPathInfo().equals("/")) {
                processAllStatus(resp);
            } else if (Pattern.matches("\\d+", req.getPathInfo().substring(1))) {
                processSpecificStatus(req, resp);
            } else {
                req.getRequestDispatcher(JspUtils.NOT_FOUND).forward(req, resp);
            }
        } catch (Exception e) {
        ResponseData responseData = service.createResponseData(200, false,
                "Internal Error in doGet Status API : " + e.getMessage(), null);
        service.returnJsonData(resp, responseData);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            String json = req.getReader().lines().collect(Collectors.joining());
            StatusModel statusModel = service.getGson().fromJson(json, StatusModel.class);
            switch (req.getServletPath() + req.getPathInfo()) {
                case UrlUtils.STATUS_ADD -> service.addStatus(statusModel);
                case UrlUtils.STATUS_UPDATE -> service.updateStatus(statusModel);
                case UrlUtils.STATUS_DELETE -> service.deleteStatus(statusModel.getId());
                default -> req.getRequestDispatcher(JspUtils.NOT_FOUND).forward(req, resp);
            }
        } catch (Exception e) {
            ResponseData responseData = service.createResponseData(200,false,
                    "Internal Error in doPost Status API: " + e.getMessage(), null);
            service.returnJsonData(resp, responseData);
        }
    }
    private void processAllStatus(HttpServletResponse resp) throws IOException {
        ResponseData responseData = service.createResponseData(200, true,
                "Get all status successfully!", service.findAllStatus());
        service.returnJsonData(resp, responseData);
    }
    private void processSpecificStatus(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        int id = Integer.parseInt(req.getPathInfo().substring(1));
        StatusModel statusModel = service.findStatusById(id);
        ResponseData responseData;
        if (statusModel == null) {
            responseData = service.createResponseData(200, false,"Invalid statusId!", null);
        } else {
            responseData = service.createResponseData(200, true,
                    "Get specific status successfully!", statusModel);
        }
        service.returnJsonData(resp, responseData);
    }
}
