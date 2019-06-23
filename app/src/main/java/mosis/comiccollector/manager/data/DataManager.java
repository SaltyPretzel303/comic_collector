package mosis.comiccollector.manager.data;

import android.graphics.Bitmap;
import android.net.Uri;

import java.util.List;

import mosis.comiccollector.comic.Comic;
import mosis.comiccollector.manager.data.handler.DataRetrievedHandler;
import mosis.comiccollector.manager.handler.JobDoneHandler;
import mosis.comiccollector.manager.user.handler.PictureReadyHandler;

public interface DataManager {

    // comics specific

    List<Comic> getCollectedComcis();

    List<Comic> getDiscoverComics();

    List<Comic> getQueuedComics();

    List<Comic> getMyComics();

    boolean fetchCollectedComics(int index, DataRetrievedHandler handler);

    boolean fetchDiscoverComics(int index, DataRetrievedHandler handler);

    Comic getComicAt(int index);

    Comic getComic(String name, String author);

    void downloadCollectedPaged(int index);

    // profile picture specific

    void saveUserProfilePic(Bitmap pic, JobDoneHandler onJobDone);

    void loadLocalProfilePic(String pic_path, PictureReadyHandler onPicReady);

    void uploadProfilePic(Uri pic_uri, JobDoneHandler onJobDone);

    void downloadProfilePic(String pic_path, PictureReadyHandler onPicReady);

}
