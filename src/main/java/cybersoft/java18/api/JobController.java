package cybersoft.java18.api;

import cybersoft.java18.model.JobModel;
import cybersoft.java18.model.ResponseData;
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

@WebServlet(name = "JobController", urlPatterns = {
        UrlUtils.JOB,
        UrlUtils.JOB_ALL
})
public class JobController extends HttpServlet {
    private Service service;

    @Override
    public void init() {
        service = ServiceHolder.getService();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            if (req.getPathInfo() == null || req.getPathInfo().equals("/")) { //get all jobs
                processAllJobs(resp);
            } else if (Pattern.matches("\\d+", req.getPathInfo().substring(1))) { //get specific job
                processSpecificJob(req, resp);
            } else { //other get methods
                req.getRequestDispatcher(JspUtils.NOT_FOUND).forward(req, resp);
            }
        } catch (Exception e) {
            ResponseData responseData = service.createResponseData(200,false,
                    "Internal Error in doGet Job API:" + e.getMessage(), null);
            service.returnJsonData(resp, responseData);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            switch (req.getServletPath() + req.getPathInfo()) {
                case UrlUtils.JOB_ADD -> processAddJob(req, resp);
                case UrlUtils.JOB_UPDATE -> processUpdateJob(req, resp);
                case UrlUtils.JOB_DELETE -> processDeleteJob(req, resp);
                default -> req.getRequestDispatcher(JspUtils.NOT_FOUND).forward(req, resp);
            }
        } catch (Exception e) {
            ResponseData responseData = service
                    .createResponseData(200,false, "Internal Error at doPost Job API: " + e.getMessage(), null);
            service.returnJsonData(resp, responseData);
        }
    }
    private void processAllJobs(HttpServletResponse resp) throws IOException {
        ResponseData responseData = service.createResponseData(200, true,
                "Get all jobs successfully!", service.findAllJobs());
        service.returnJsonData(resp, responseData);
    }
    private void processSpecificJob(HttpServletRequest req, HttpServletResponse resp) throws IOException, NumberFormatException {
        int jobId = Integer.parseInt(req.getPathInfo().substring(1));
        JobModel jobModel = service.findJobById(jobId);
        ResponseData responseData;
        if (jobModel == null) {
            responseData = service.createResponseData(200, false,
                    "Invalid jobId!", null);
        } else {
            responseData = service.createResponseData(200, true,
                    "Get specific job successfully!", jobModel);
        }
        service.returnJsonData(resp, responseData);
    }
    private void processAddJob(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String json = req.getReader().lines().collect(Collectors.joining());
        JobModel jobModel = service.getGson().fromJson(json, JobModel.class);
        int result = service.addJob(jobModel);
        ResponseData responseData;
        if (result == 0) {
            responseData = service.createResponseData(200,false, "Failed to add job!", null);
        } else {
            responseData = service.createResponseData(200,true, "Added job successfully!", null);
        }
        service.returnJsonData(resp, responseData);
    }
    private void processUpdateJob(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String json = req.getReader().lines().collect(Collectors.joining());
        JobModel jobModel = service.getGson().fromJson(json, JobModel.class);
        int result = service.updateJob(jobModel);
        ResponseData responseData;
        if (result == 0) {
            responseData = service.createResponseData(200,false, "Failed to update job!", null);
        } else {
            responseData = service.createResponseData(200,true, "Updated job successfully!", null);
        }
        service.returnJsonData(resp, responseData);
    }
    private void processDeleteJob(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        int result = service.deleteJobById(Integer.parseInt(req.getParameter("id")));
        ResponseData responseData;
        if (result == 0) {
            responseData = service.createResponseData(200,false, "Failed to delete job!", null);
        } else {
            responseData = service.createResponseData(200,true, "Deleted job successfully!", null);
        }
        service.returnJsonData(resp, responseData);
    }
}
