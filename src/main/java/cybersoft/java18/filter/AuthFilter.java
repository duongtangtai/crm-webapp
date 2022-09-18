package cybersoft.java18.filter;

import com.auth0.jwt.interfaces.DecodedJWT;
import cybersoft.java18.model.ResponseData;
import cybersoft.java18.service.Service;
import cybersoft.java18.service.ServiceHolder;
import cybersoft.java18.utils.JspUtils;
import cybersoft.java18.utils.RoleUtils;
import cybersoft.java18.utils.UrlUtils;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebFilter(filterName = "UrlFilter", urlPatterns = UrlUtils.ALL)
public class AuthFilter implements Filter {
    private Service service;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        service = ServiceHolder.getService();
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) servletRequest;
        HttpServletResponse resp = (HttpServletResponse) servletResponse;
        //The Access-Control-Allow-Origin header is included in the response from one website
        // to a request originating from another website, and identifies the permitted origin of the request
        req.setCharacterEncoding("UTF-8");
        resp.setCharacterEncoding("UTF-8");
        resp.setContentType("application/json");
        resp.addHeader("Access-Control-Allow-Origin", "*");
        resp.addHeader("Access-Control-Allow-Headers", "*");
        resp.addHeader("Access-Control-Expose-Headers", "*");
        if (isValidUrl(req.getServletPath())) { //if url is valid, check if it's public url
            if (!isPublicUrl(req.getMethod(), req.getServletPath() + (req.getPathInfo() == null ? "" : req.getPathInfo()))) {
                processCheckToken(req, resp, filterChain);
            } else {
                filterChain.doFilter(req,resp);
            }
        } else {
            req.getRequestDispatcher(JspUtils.NOT_FOUND).forward(req, resp);
        }
    }
    private void processCheckToken(HttpServletRequest req, HttpServletResponse resp, FilterChain filterChain) throws IOException {
        String authorizationHeader = req.getHeader("Authorization");
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            try {
                DecodedJWT decodedJWT = service.decodingToken(req);
                String roleClaim = decodedJWT.getClaim("role").toString();
                String role = roleClaim.substring(1, roleClaim.length() - 1);
                //check role -> can this role access the resource?
                if (isRoleValid(role, req.getServletPath() + (req.getPathInfo() == null ? "" : req.getPathInfo()))) {
                    filterChain.doFilter(req, resp);
                } else {
                    ResponseData responseData = service.createResponseData(403, false,
                            role + "Authorization is invalid", null);
                    service.returnJsonData(resp, responseData);
                }
            } catch (Exception e) { //in case token is invalid
                sendInvalidTokenResponse(resp);
            }
        } else {
            sendInvalidTokenResponse(resp);
        }
    }
    private boolean isPublicUrl(String method, String url) {
        //get method can get through
        if (url != null ) {
            return url.equals(UrlUtils.AUTH_LOGIN) ||
                    url.equals(UrlUtils.AUTH_REFRESH_TOKEN) ||
                    url.startsWith(UrlUtils.FILE) && method.equals("GET");
        }
        return false;
    }
    private boolean isValidUrl(String url) {
        String slash = "/";
        return url.equals(UrlUtils.ROLE) || url.startsWith(UrlUtils.ROLE + slash) ||
                url.equals(UrlUtils.STATUS) || url.startsWith(UrlUtils.STATUS + slash) ||
                url.equals(UrlUtils.USER) || url.startsWith(UrlUtils.USER + slash) ||
                url.equals(UrlUtils.JOB) || url.startsWith(UrlUtils.JOB + slash) ||
                url.equals(UrlUtils.TASK) || url.startsWith(UrlUtils.TASK + slash) ||
                url.equals(UrlUtils.AUTH) || url.startsWith(UrlUtils.AUTH + slash) ||
                url.equals(UrlUtils.PROFILE) || url.equals(UrlUtils.PROFILE_STATISTIC) ||
                url.equals(UrlUtils.PROFILE_TASK) || url.startsWith(UrlUtils.PROFILE_TASK + slash) ||
                url.equals(UrlUtils.FILE) || url.startsWith(UrlUtils.FILE + slash) ||
                url.equals(UrlUtils.DASH_BOARD) || url.equals(UrlUtils.AUTH_CHECK_TOKEN);
    }
    private boolean isRoleValid(String role, String url) { //Authorization
        switch (role) {
            case RoleUtils.LEADER -> {
                return !url.startsWith(UrlUtils.ROLE) && !url.equals(UrlUtils.USER_ADD) &&
                        !url.equals(UrlUtils.USER_UPDATE) && !url.equals(UrlUtils.USER_DELETE);
            }
            case RoleUtils.MEMBER -> {
                return !url.startsWith(UrlUtils.ROLE) && !url.startsWith(UrlUtils.USER) &&
                        !url.startsWith(UrlUtils.JOB) && !url.startsWith(UrlUtils.TASK);
            }
            default -> { //ADMIN
                return true;
            }
        }
    }
    private void sendInvalidTokenResponse(HttpServletResponse resp) throws IOException {
        ResponseData responseData = service.createResponseData(401, false, "Invalid token", null);
        service.returnJsonData(resp, responseData);
    }
}
