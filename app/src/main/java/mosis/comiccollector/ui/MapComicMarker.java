package mosis.comiccollector.ui;

import android.graphics.drawable.Drawable;

import org.osmdroid.api.IGeoPoint;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.overlay.OverlayItem;

import java.util.ArrayList;
import java.util.List;

import mosis.comiccollector.ui.comic.Comic;

public class MapComicMarker extends OverlayItem {

    private List<Comic> comics;

    private Drawable unknownMarker;
    private Drawable knownMarker;

    public MapComicMarker(Comic comic,
                          Drawable knownMarker,
                          Drawable unknownMarker) {
        super("title", "description",
                new GeoPoint(comic.position.x, comic.position.y));

        this.comics = new ArrayList<Comic>();

        this.comics.add(comic);

        if (comic.discovered) {
            this.showAsKnown();
        } else {
            this.showAsUnknown();
        }

    }

    public void addComic(Comic newComic) {
        this.comics.add(newComic);

        if (newComic.discovered) {
            this.showAsKnown();
        }

    }

    private void showAsUnknown() {
        super.setMarker(this.unknownMarker);
    }

    private void showAsKnown() {
        super.setMarker(this.knownMarker);

    }

}
