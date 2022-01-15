package mosis.comiccollector.ui;

import android.graphics.drawable.Drawable;

import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.overlay.OverlayItem;

import java.util.ArrayList;
import java.util.List;

import mosis.comiccollector.ui.comic.ViewComic;

// TODO remove, looks like this one is not used
public class MapComicMarker extends OverlayItem {

    private List<ViewComic> comics;

    private Drawable unknownMarker;
    private Drawable knownMarker;

    public MapComicMarker(ViewComic comic,
                          Drawable knownMarker,
                          Drawable unknownMarker) {
        super("title", "description",
//                new GeoPoint(comic.position.x, comic.position.y));
                new GeoPoint(0, 1));

        this.comics = new ArrayList<ViewComic>();

        this.comics.add(comic);

    }

    public void addComic(ViewComic newComic) {
        this.comics.add(newComic);

    }

    private void showAsUnknown() {
        super.setMarker(this.unknownMarker);
    }

    private void showAsKnown() {
        super.setMarker(this.knownMarker);

    }

}
