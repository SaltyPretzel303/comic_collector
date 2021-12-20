package mosis.comiccollector.util;

import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.auth.FirebaseUser;

import mosis.comiccollector.model.user.User;
import mosis.comiccollector.model.user.UserResponseType;

public class FirebaseTypesMapper {

    public static UserResponseType getUserResponse(Exception exc) {
        if (exc instanceof FirebaseAuthInvalidUserException) {
            return UserResponseType.NoSuchUser;
        } else if (exc instanceof FirebaseAuthInvalidCredentialsException) {
            return UserResponseType.InvalidCredential;
        } else if (exc instanceof FirebaseAuthWeakPasswordException) {
            return UserResponseType.InvalidPassword;
        }else if (exc instanceof FirebaseAuthUserCollisionException) {
            return UserResponseType.EmailAlreadyInUse;
        }

        return UserResponseType.UnknownError;
    }

    public static User getUser(FirebaseUser user) {

        return new User(user.getUid(),
                user.getEmail(),
                user.getDisplayName());

    }

}
