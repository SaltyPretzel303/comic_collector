package mosis.comiccollector.model.user;

public class UserAuthResponse {

    public User user;
    public UserAuthResponseType responseType;

    public UserAuthResponse() {

    }

    public UserAuthResponse(User user, UserAuthResponseType responseType) {
        this.user = user;
        this.responseType = responseType;
    }

}
