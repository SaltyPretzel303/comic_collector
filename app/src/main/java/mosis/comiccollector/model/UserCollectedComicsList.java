package mosis.comiccollector.model;

import java.io.Serializable;
import java.util.List;

public class UserCollectedComicsList implements Serializable {

//    public static final String USER_ID_FIELD = "userId";

    public String userId;
    public List<String> comicsIds;

    public UserCollectedComicsList() {

    }


}
