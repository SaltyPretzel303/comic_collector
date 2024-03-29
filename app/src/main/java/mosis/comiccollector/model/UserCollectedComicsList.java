package mosis.comiccollector.model;

import java.io.Serializable;
import java.util.List;

public class UserCollectedComicsList implements Serializable {

    public static final String USER_ID_FIELD = "userId";
    public static final String COLLECTED_LIST_FIELD = "comicsIds";

    public String userId;
    public List<String> comicsIds;

    public UserCollectedComicsList() {

    }

    public UserCollectedComicsList(String userId, List<String> comics) {
        this.userId = userId;
        this.comicsIds = comics;
    }

}
