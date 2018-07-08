package server;

public interface AuthService {
    void start();
    String getNicknameByLoginPass(String login, String pass);
    boolean setNicknameByLoginPass(String nickname, String login, String pass);
    void stop();
}
