package mosis.comiccollector.ui.map;

import android.content.Context;

import org.osmdroid.views.overlay.ItemizedIconOverlay;
import org.osmdroid.views.overlay.OverlayItem;

import java.util.List;

public class NamedItemsOverlay<ItemType extends OverlayItem>
        extends ItemizedIconOverlay<ItemType> {

    public enum Names {
        MyLocationOverlay,
        FriendsOverlay,
        UnknownPeopleOverlay,
        CreatedComicsOverlay,
        CollectedComicsOverlay,
        UnknownComicsOverlay
    }

    public Names name;

    public NamedItemsOverlay(
            Names name,
            Context pContext,
            List<ItemType> pList,
            OnItemGestureListener<ItemType> pOnItemGestureListener) {

        super(pContext, pList, pOnItemGestureListener);

        this.name = name;
    }
}
