package mosis.comiccollector.login;

public interface LoginManager {

    void login(String username, String password, OnResponseAction response_callback);

    void register(String username, String password, OnResponseAction response_callback);

}
