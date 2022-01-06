package mosis.comiccollector.ui.map;

import org.osmdroid.api.IGeoPoint;
import org.osmdroid.views.overlay.OverlayItem;

public class OverlayItemWithId extends OverlayItem {

    private String itemId;

    public OverlayItemWithId(String itemId,
                             String aTitle,
                             String aSnippet,
                             IGeoPoint aGeoPoint) {
        super(aTitle, aSnippet, aGeoPoint);

        this.itemId = itemId;
    }

    public String getItemId() {
        return itemId;
    }

}
