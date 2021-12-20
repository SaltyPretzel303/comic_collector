package mosis.comiccollector.model.user;

public class UserResponse {
    public User user;
    public UserResponseType responseType;

    public UserResponse() {

    }

    public UserResponse(User user, UserResponseType responseType) {
        this.user = user;
        this.responseType = responseType;
    }

}
