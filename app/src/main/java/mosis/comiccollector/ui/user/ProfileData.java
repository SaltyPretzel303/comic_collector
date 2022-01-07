package mosis.comiccollector.ui.user;

import java.io.Serializable;

public class ProfileData implements Serializable {
    public String userId;

    public String email;
    public String name;

    public int rating;

    public String picUri;

    public ProfileData() {

    }

    public ProfileData(String id,
                       String email,
                       String name,
                       int rating,
                       String picUri) {

        this.userId = id;
        this.email = email;
        this.name = name;
        this.rating = rating;
        this.picUri = picUri;
    }

}
