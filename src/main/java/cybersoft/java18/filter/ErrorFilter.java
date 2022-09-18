package cybersoft.java18.filter;

import cybersoft.java18.utils.JspUtils;
import cybersoft.java18.utils.UrlUtils;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
@WebFilter(filterName = "ErrorFilter", urlPatterns = {UrlUtils.ALL})
public class ErrorFilter implements Filter {
    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) servletRequest;
        HttpServletResponse resp = (HttpServletResponse) servletResponse;
        if (resp.getStatus() == 500) {
            req.getRequestDispatcher(JspUtils.INTERNAL_ERROR).forward(req, resp);
        } else {
            filterChain.doFilter(servletRequest, servletResponse);
        }
    }
}
