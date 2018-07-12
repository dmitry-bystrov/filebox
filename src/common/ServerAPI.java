package common;

public interface ServerAPI {
    String CLOSE_CONNECTION = "/end";
    String AUTH_REQUEST = "/auth";
    String AUTH_REGISTER = "/register";
    String AUTH_SUCCESSFUL = "/authok";
    String SERVICE_MESSAGE = "/service";
    String UNAUTHORIZED = "/unauthorized";
}
