package mosis.comiccollector.ui.viewmodel.mapper;

import mosis.comiccollector.model.user.User;
import mosis.comiccollector.repository.DataMapper;
import mosis.comiccollector.ui.user.ViewUser;

public class UserModelMapper implements DataMapper<User, ViewUser> {
    @Override
    public ViewUser mapToViewModel(User input) {
        ViewUser retUser = new ViewUser(
                input.getUserId(),
                input.getEmail(),
                input.getName(),
                input.getRating(),
                "");

        return retUser;
    }
}
