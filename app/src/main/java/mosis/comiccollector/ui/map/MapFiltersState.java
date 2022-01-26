package mosis.comiccollector.ui.map;

import android.content.Context;

import java.io.Serializable;

public class MapFiltersState implements Serializable {

    private static final String FILTERS_PREFS = "map_filters";

    private static final String SHOW_FRIENDS_PREF = "show_friends_filter";
    private static final String SHOW_UNKNOWN_PEOPLE_PREF = "show_unknown_people";

    private static final String SHOW_CREATED_COMICS_PREF = "show_created_comics";
    private static final String SHOW_COLLECTED_COMICS_PREF = "show_collected_comics";
    private static final String SHOW_UNKNOWN_COMICS_PREF = "show_unknown_comics";

    public boolean showFriends;
    public boolean showUnknownPeople;

    public boolean showCreatedComics;
    public boolean showCollectedComics;
    public boolean showUnknownComics;

    public MapFiltersState() {

    }

    public MapFiltersState(boolean showFriends,
                           boolean showUnknownPeople,
                           boolean showCreatedComics,
                           boolean showCollectedComics,
                           boolean showUnknownComics) {

        this.showFriends = showFriends;
        this.showUnknownPeople = showUnknownPeople;
        this.showCreatedComics = showCreatedComics;
        this.showCollectedComics = showCollectedComics;
        this.showUnknownComics = showUnknownComics;
    }

    public static MapFiltersState read(Context context) {

        MapFiltersState state = new MapFiltersState();
        var prefs = context.getSharedPreferences(FILTERS_PREFS, Context.MODE_PRIVATE);

        state.showFriends = prefs.getBoolean(SHOW_FRIENDS_PREF, false);
        state.showUnknownPeople = prefs.getBoolean(SHOW_UNKNOWN_PEOPLE_PREF, false);

        state.showCreatedComics = prefs.getBoolean(SHOW_CREATED_COMICS_PREF, false);
        state.showCollectedComics = prefs.getBoolean(SHOW_COLLECTED_COMICS_PREF, false);
        state.showUnknownComics = prefs.getBoolean(SHOW_UNKNOWN_COMICS_PREF, false);

        return state;
    }

    public static void write(Context context, MapFiltersState state) {
        var prefs = context
                .getSharedPreferences(FILTERS_PREFS, Context.MODE_PRIVATE)
                .edit();

        prefs.putBoolean(SHOW_FRIENDS_PREF, state.showFriends);
        prefs.putBoolean(SHOW_UNKNOWN_PEOPLE_PREF, state.showUnknownPeople);

        prefs.putBoolean(SHOW_CREATED_COMICS_PREF, state.showCreatedComics);
        prefs.putBoolean(SHOW_COLLECTED_COMICS_PREF, state.showCollectedComics);
        prefs.putBoolean(SHOW_UNKNOWN_COMICS_PREF, state.showUnknownComics);

        prefs.apply();
    }

}
