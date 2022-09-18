package cybersoft.java18.api;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.interfaces.DecodedJWT;
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
import java.util.Date;

@WebServlet(name = "authController", urlPatterns = {
        UrlUtils.AUTH_ALL
})
public class AuthController extends HttpServlet {
    private Service service;
    @Override
    public void init() {
        service = ServiceHolder.getService();
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            switch (req.getServletPath() + req.getPathInfo()) {
                case UrlUtils.AUTH_LOGIN -> processLogin(req, resp);
                case UrlUtils.AUTH_LOGOUT -> processLogout(req, resp);
                case UrlUtils.AUTH_CHECK_TOKEN -> processCheckToken(resp);
                case UrlUtils.AUTH_REFRESH_TOKEN -> processRefreshToken(req, resp);
                case UrlUtils.AUTH_CHANGE_PASSWORD -> processChangePassword(req, resp);
                default -> req.getRequestDispatcher(JspUtils.NOT_FOUND).forward(req, resp);
            }
        } catch (Exception e) {
            ResponseData responseData = service
                    .createResponseData(200,false, "Internal Error in doPost Auth API: " + e.getMessage(), null);
            service.returnJsonData(resp, responseData);
        }
    }

    /**
     * This function will use input email & password to authenticate the user. If they're valid, it returns
     * UserModel including id, fullName and avatar url. Otherwise it returns a Response Data to show email & password
     * are invalid
     */
    private void processLogin(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String email = req.getParameter("email");
        String password = req.getParameter("password");
        UserModel userModel = service.login(email, password);
        ResponseData responseData;
        if (userModel == null) { //email & password are invalid
            responseData = service.createResponseData(200, false,
                    "Email & password are invalid!", null);
        } else { //if valid -> create tokens (assign them on headers) and return user's name and avatar url
            String accessToken = service.createToken(String.valueOf(userModel.getId()),
                    req.getRequestURL().toString(),
                    userModel.getRole(),
                    new Date(System.currentTimeMillis() + 5 * 60 * 1000)); // 5 minutes
            String refreshToken = service.createToken(String.valueOf(userModel.getId()),
                    req.getRequestURL().toString(),
                    userModel.getRole(),
                    new Date(System.currentTimeMillis() + 30L * 24 * 3600 * 1000)); // one month
            service.saveUserRefreshToken(refreshToken, userModel.getId()); // save refreshToken in DB for future checks
            resp.addHeader("access_token", accessToken);
            resp.addHeader("refresh_token", refreshToken);
            responseData = service.createResponseData(200, true,
                    "Login successfully!", userModel);
        }
        service.returnJsonData(resp, responseData);
    }

    /**
     *  This function will decode the user's token to take the user's ID, then it will call service to handle
     *  the logout processing
     */
    private void processLogout(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        DecodedJWT decodedJWT = service.decodingToken(req);
        int userId = Integer.parseInt(decodedJWT.getSubject());
        int result = service.logout(userId);
        ResponseData responseData;
        if (result == 0) {
            responseData = service.createResponseData(200,false, "Failed to logout!", null);
        } else {
            responseData = service.createResponseData(200,true, "Logout successfully!",null );
        }
        service.returnJsonData(resp, responseData);
    }

    /**
     * If the request can get to this function, it already passed the filter which means it's valid.
     * Otherwise, it was already handled by the filter
     */
    private void processCheckToken(HttpServletResponse resp) throws IOException {
        ResponseData responseData = service.createResponseData(200,true, "Token is valid", null);
        service.returnJsonData(resp, responseData);
    }
    /**
     * If the client's refresh token can go through filter, it means it's still valid. We need to check with refresh
     * token at DB to make sure they're matched. Then we create two brand new tokens (access & refresh) to give to the
     * user, the old refresh_token will no longer valid.
     */
    private void processRefreshToken(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String inputRefreshToken = req.getHeader("Authorization").substring("Bearer ".length());
        JWTVerifier verifier = JWT.require(service.getAlgorithm()).build();
        DecodedJWT decodedJWT = verifier.verify(inputRefreshToken);
        String userId = decodedJWT.getSubject();
        String role = service.findUserRoleById(Integer.parseInt(userId)); //update new role
        if (service.isRefreshTokenValid(inputRefreshToken, Integer.parseInt(userId))) { //if refresh_token is valid
            String accessToken = service.createToken(userId,
                    req.getRequestURL().toString(),
                    role,
                    new Date(System.currentTimeMillis() + 5 * 60 * 1000)); //5 minutes
            String newRefreshToken = service.createToken(userId,
                    req.getRequestURL().toString(),
                    role,
                    new Date(System.currentTimeMillis() + 30L * 24 * 3600 * 1000)); // cast long or it's negative
            service.saveUserRefreshToken(newRefreshToken, Integer.parseInt(userId));
            resp.addHeader("access_token", accessToken);
            resp.addHeader("refresh_token", newRefreshToken);
            ResponseData responseData = service.createResponseData(200, true,
                    "Tokens refreshed successfully!", null);
            service.returnJsonData(resp, responseData);
        } else {
            ResponseData responseData = service.createResponseData(200, false,
                    "Input refresh token is invalid!", null);
            service.returnJsonData(resp, responseData);
        }
    }

    /**
     * This function will receive old password, the new password and the repeated new password. If the repeated
     * new password and the new password are not equal. The function returns a fail response. If they're identical,
     * the function proceeds to call service to process.
     */
    private void processChangePassword(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String oldPW = req.getParameter("oldPW");
        String newPW = req.getParameter("newPW");
        String repeatPW = req.getParameter("repeatNewPW");
        DecodedJWT decodedJWT = service.decodingToken(req);
        int userId = Integer.parseInt(decodedJWT.getSubject());
        ResponseData responseData;
        if (!newPW.equals(repeatPW)) {
            responseData = service.createResponseData(200, false,
                    "Mật khẩu mới nhập lại không chính xác!", null);
        } else { //change old pw with new pw
            int result = service.changeUserPW(userId, oldPW, newPW);
            if (result == 1) { //successful
                responseData = service.createResponseData(200, true,
                        "Mật khẩu thay đổi thành công!", null);
            } else {
                responseData = service.createResponseData(200, false,
                        "Mật khẩu cũ không chính xác!", null);
            }
        }
        service.returnJsonData(resp, responseData);
    }
}
