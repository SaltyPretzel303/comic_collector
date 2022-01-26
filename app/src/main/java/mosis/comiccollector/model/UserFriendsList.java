package mosis.comiccollector.model;

import java.util.List;

public class UserFriendsList {

    public static final String FRIENDS_IDS_FIELD = "friendsIds";

    public String userId;
    public List<String> friendsIds;

    public UserFriendsList() {

    }

    public UserFriendsList(String id, List<String> friends) {
        this.userId = id;
        this.friendsIds = friends;
    }

}
