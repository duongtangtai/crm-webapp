package cybersoft.java18.utils;

public class JspUtils {
    private JspUtils () {
        throw new IllegalStateException("Utility Class");
    }
    public static final String NOT_FOUND = "/WEB-INF/views/error/not-found.jsp";
    public static final String INTERNAL_ERROR = "/WEB-INF/views/error/internal-error.jsp";
}
