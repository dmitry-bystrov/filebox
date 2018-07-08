package common;

public interface ServerAPI {
    String CLOSE_CONNECTION = "/end";
    String AUTH_REQUEST = "/auth";
    String AUTH_REGISTER = "/register";
    String AUTH_SUCCESSFUL = "/authok";
    String SERVICE_MESSAGE = "/service";
    String PERSONAL_MESSAGE = "/personal";
    String UNAUTHORIZED = "/unauthorized";
    String FROM_USER = "/from_user";
    String TO_USER = "/w";
    String USERLIST = "/userlist";
}
