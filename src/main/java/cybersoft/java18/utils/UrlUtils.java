package cybersoft.java18.utils;

public class UrlUtils {
    private UrlUtils () {
        throw new IllegalStateException("Utility Class");
    }
    public static final String ALL = "/*";
    public static final String AUTH = "/api/auth";
    public static final String AUTH_ALL = "/api/auth/*";
    public static final String AUTH_LOGIN = "/api/auth/login";
    public static final String AUTH_LOGOUT = "/api/auth/logout";
    public static final String AUTH_CHECK_TOKEN = "/api/auth/check-token";
    public static final String AUTH_REFRESH_TOKEN = "/api/auth/refresh-token";
    public static final String AUTH_CHANGE_PASSWORD = "/api/auth/change-password";
    public static final String ROLE = "/api/role";
    public static final String ROLE_ALL = "/api/role/*";
    public static final String ROLE_ADD = "/api/role/add";
    public static final String ROLE_UPDATE = "/api/role/update";
    public static final String ROLE_DELETE = "/api/role/delete";
    public static final String STATUS = "/api/status";
    public static final String STATUS_ALL = "/api/status/*";
    public static final String STATUS_ADD = "/api/status/add";
    public static final String STATUS_UPDATE = "/api/status/update";
    public static final String STATUS_DELETE = "/api/status/delete";
    public static final String USER = "/api/user";
    public static final String USER_ALL = "/api/user/*";
    public static final String USER_ADD = "/api/user/add";
    public static final String USER_UPDATE = "/api/user/update";
    public static final String USER_DELETE = "/api/user/delete";
    public static final String JOB = "/api/job";
    public static final String JOB_ALL = "/api/job/*";
    public static final String JOB_ADD = "/api/job/add";
    public static final String JOB_UPDATE = "/api/job/update";
    public static final String JOB_DELETE = "/api/job/delete";
    public static final String TASK = "/api/task";
    public static final String TASK_ALL = "/api/task/*";
    public static final String TASK_ADD = "/api/task/add";
    public static final String TASK_UPDATE = "/api/task/update";
    public static final String TASK_DELETE = "/api/task/delete";
    public static final String PROFILE = "/api/profile";
    public static final String PROFILE_TASK = "/api/profile-task";
    public static final String PROFILE_STATISTIC = "/api/profile-statistic";
    public static final String PROFILE_TASK_ALL = "/api/profile-task/*";
    public static final String FILE = "/api/file";
    public static final String FILE_ALL = "/api/file/*";
    public static final String DASH_BOARD = "/api/dashboard";
}
