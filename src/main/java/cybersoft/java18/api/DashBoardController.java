package cybersoft.java18.api;

import cybersoft.java18.model.ResponseData;
import cybersoft.java18.service.Service;
import cybersoft.java18.service.ServiceHolder;
import cybersoft.java18.utils.UrlUtils;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet(name = "dashBoardController", urlPatterns = {
        UrlUtils.DASH_BOARD
})
public class DashBoardController extends HttpServlet {
    private Service service;

    @Override
    public void init() {
        service = ServiceHolder.getService();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        ResponseData responseData = service.createResponseData(200, true,
                "Get dashboard statistics successfully!", service.findDashBoardStatistics());
        service.returnJsonData(resp, responseData);
    }
}
