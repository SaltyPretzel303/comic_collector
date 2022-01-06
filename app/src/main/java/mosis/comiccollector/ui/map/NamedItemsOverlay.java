package mosis.comiccollector.ui.map;

import android.content.Context;

import org.osmdroid.views.overlay.ItemizedIconOverlay;
import org.osmdroid.views.overlay.OverlayItem;

import java.util.List;

public class NamedItemsOverlay<ItemType extends OverlayItem>
        extends ItemizedIconOverlay<ItemType> {

    public enum Names {
        MyLocationOverlay, FriendsOverlay, PeopleOverlay, ComicsOverlay
    }

    public Names name;

    public NamedItemsOverlay(
            Names name,
            Context pContext,
            List pList,
            OnItemGestureListener pOnItemGestureListener) {

        super(pContext, pList, pOnItemGestureListener);

        this.name = name;


    }
}
