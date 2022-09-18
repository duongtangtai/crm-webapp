package cybersoft.java18.api;

import com.auth0.jwt.interfaces.DecodedJWT;
import cybersoft.java18.model.ResponseData;
import cybersoft.java18.service.Service;
import cybersoft.java18.service.ServiceHolder;
import cybersoft.java18.utils.FileUtils;
import cybersoft.java18.utils.JspUtils;
import cybersoft.java18.utils.UrlUtils;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

@WebServlet(name = "fileController", urlPatterns = {
        UrlUtils.FILE,
        UrlUtils.FILE_ALL
})
public class FileController extends HttpServlet {
    private Service service;

    /**
     *  Create a directory for images, if the directory already exists, do nothing.
     */
    @Override
    public void init() throws ServletException {
        service = ServiceHolder.getService();
        try {
            Files.createDirectories(Path.of(FileUtils.IMAGE_PATH));
        } catch (IOException e) {
            System.out.println("Error occurred while creating directory!");
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            if (req.getPathInfo() != null && !req.getPathInfo().equals("/")) {
                processGetImage(req, resp);
            } else {
                req.getRequestDispatcher(JspUtils.NOT_FOUND).forward(req, resp);
            }
        } catch (Exception e) {
            ResponseData responseData = service.createResponseData(200, false,
                    "Internal Error in doGet File API: " + e.getMessage(), null);
            service.returnJsonData(resp, responseData);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            if (req.getPathInfo() == null && req.getServletPath().equals(UrlUtils.FILE)) {
                processSaveImage(req, resp);
            } else {
                req.getRequestDispatcher(JspUtils.NOT_FOUND).forward(req, resp);
            }
        } catch (Exception e) {
            ResponseData responseData = service.createResponseData(200, false,
                    "Internal Error in doPost File API: " + e.getMessage(), null);
            service.returnJsonData(resp, responseData);
        }
    }

    /**
     *  This function checks if the avatar ID is valid. If the ID is valid, the function returns an image from
     *  the image directory. Otherwise returns a JSON Response Data
     */
    private void processGetImage(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("image/png");
        String avatar = req.getPathInfo().substring(1);
        File file = new File(FileUtils.IMAGE_PATH + avatar + FileUtils.IMAGE_SUFFIX);
        if (file.exists()) {
            try ( // use buffered streams to increase performance & reduce consumed memory
                    ServletOutputStream outputStream = resp.getOutputStream();
                    FileInputStream fileInputStream = new FileInputStream(file);
                    BufferedInputStream bufferedInputStream = new BufferedInputStream(fileInputStream);
                    BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(outputStream)
            ){
                int ch;
                while((ch=bufferedInputStream.read())!=-1) //as long as there are any remaining characters
                {
                    bufferedOutputStream.write(ch);
                }
            } catch (Exception e) { //if an error occurs
                resp.setContentType("application/json");
                ResponseData responseData = service.createResponseData(200, false,
                        "Error occurred in processGetImage:" + e.getMessage(), null);
                service.returnJsonData(resp, responseData);
            }
        } else { //if the file doesn't exists
            resp.setContentType("application/json");
            ResponseData responseData = service.createResponseData(200, false,
                    "Invalid avatarId", null);
            service.returnJsonData(resp, responseData);
        }
    }

    /**
     *  This function saves the requested image in the image directory. Then save the image ID in DB.
     */
    private void processSaveImage(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        DecodedJWT decodedJWT = service.decodingToken(req);
        String avatar = decodedJWT.getSubject(); //avatar can be unique like userId. This case we simply use user ID
        ResponseData responseData;
        try {
            ServletFileUpload servletFileUpload = new ServletFileUpload(new DiskFileItemFactory());
            List<FileItem> multiFiles = servletFileUpload.parseRequest(req);
            for (FileItem item : multiFiles) {
                item.write(new File(FileUtils.IMAGE_PATH + avatar + FileUtils.IMAGE_SUFFIX));
            }
            int result = service.saveUserAvatar(avatar, Integer.parseInt(avatar)); // save avatar in DB
            if (result == 0) {
                responseData = service.createResponseData(200, false,
                        "Failed to save avatar!", null);
            } else {
                resp.setHeader("avatar", FileUtils.API_FILE + avatar); //return newAvatarId
                responseData = service.createResponseData(200, true,
                        "Saved avatar successfully!", null);
            }
        } catch (Exception e) { //if an error occurs
            responseData = service.createResponseData(200, false,
                    "Error occurred while processSaveImage:" + e.getMessage(), null);
        }
        service.returnJsonData(resp, responseData);
    }
}
