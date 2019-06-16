package mosis.comiccollector.login;

public class FakeLoginManager implements LoginManager {


    @Override
    public void login(String username, String password, OnResponseAction response_callback) {

        response_callback.execute(true);

    }

    @Override
    public void register(String username, String password, OnResponseAction response_callback) {

        response_callback.execute(true);

    }
}
