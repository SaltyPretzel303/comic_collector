package mosis.comiccollector.manager.user.handler;

import mosis.comiccollector.login.LoginResponseType;

public interface LoginResponseHandler {

    void execute(LoginResponseType response);

}
