package mosis.comiccollector.util;

import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;

import mosis.comiccollector.model.user.UserAuthResponseType;

public class FirebaseTypesMapper {

    public static UserAuthResponseType getUserResponse(Exception exc) {
        if (exc instanceof FirebaseAuthInvalidUserException) {
            return UserAuthResponseType.NoSuchUser;
        } else if (exc instanceof FirebaseAuthInvalidCredentialsException) {
            return UserAuthResponseType.InvalidCredential;
        } else if (exc instanceof FirebaseAuthWeakPasswordException) {
            return UserAuthResponseType.InvalidPassword;
        }else if (exc instanceof FirebaseAuthUserCollisionException) {
            return UserAuthResponseType.EmailAlreadyInUse;
        }

        return UserAuthResponseType.UnknownError;
    }

}
