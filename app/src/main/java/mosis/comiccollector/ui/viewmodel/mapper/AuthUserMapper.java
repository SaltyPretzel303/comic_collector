package mosis.comiccollector.ui.viewmodel.mapper;

import mosis.comiccollector.model.user.UserAuthResponse;
import mosis.comiccollector.model.user.UserAuthResponseType;
import mosis.comiccollector.repository.DataMapper;
import mosis.comiccollector.ui.user.ViewUser;

public class AuthUserMapper implements DataMapper<UserAuthResponse, ViewUser> {

    @Override
    public ViewUser mapToViewModel(UserAuthResponse input) {

        String errorMessage = null;
        if (input.responseType != UserAuthResponseType.Success) {
            errorMessage = input.responseType.toString();
        }

        if (input.user != null) {

            return new ViewUser(
                    input.user.getUserId(),
                    input.user.getEmail(),
                    input.user.getName(),
                    input.user.getRating(),
                    errorMessage);
        } else {
            return null;
        }
    }

}
