package mosis.comiccollector.ui;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import mosis.comiccollector.R;
import mosis.comiccollector.location.LocationConsumer;
import mosis.comiccollector.model.user.UserLocation;
import mosis.comiccollector.ui.comic.ComicPreviewAndEditActivity;
import mosis.comiccollector.ui.comic.ViewComic;
import mosis.comiccollector.ui.user.ViewUser;
import mosis.comiccollector.util.Distance;

public class ShortComicDialog extends Dialog implements LocationConsumer {

    interface CollectHandler {
        void collect(String comicId);
    }

    private static final String UNKNOWN_COMIC_BUTTON_TEXT = "COLLECT";
    private static final String KNOWN_COMIC_BUTTON_TEXT = "PREVIEW";

    private static final double MAX_COLLECT_DISTANCE_KM = 0.02; // 20m ... ?

    public static final int TITLE_PAGE_WIDTH = 200;
    public static final int TITLE_PAGE_HEIGHT = 200;

    private Context context;

    private MutableLiveData<ViewComic> comic;
    private ComicOrigin origin;

    private Button actionButton;

    private CollectHandler howToCollect;

    public ShortComicDialog(
            @NonNull Context context,
            MutableLiveData<ViewComic> comic,
            ComicOrigin origin) {
        super(context);

        this.context = context;

        this.comic = comic;
        this.origin = origin;
    }

    public ShortComicDialog(
            @NonNull Context context,
            MutableLiveData<ViewComic> comic,
            ComicOrigin origin,
            CollectHandler howToCollect) {
        this(context, comic, origin);

        this.howToCollect = howToCollect;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.comic_from_map_dialog);

        comic.observe((LifecycleOwner) context, this::initViews);
    }

    private void initViews(ViewComic vComic) {

        ((TextView) findViewById(R.id.comic_from_map_title_tv))
                .setText(vComic.title);

        ((TextView) findViewById(R.id.comic_from_map_description_tv))
                .setText(vComic.description);

        ((ProgressBar) findViewById(R.id.comic_from_map_pb))
                .setProgress(vComic.rating);
        ((TextView) findViewById(R.id.comic_from_map_rating_tv))
                .setText(vComic.rating + "/100");

        vComic.liveAuthor.observe((LifecycleOwner) context, (ViewUser viewUser) -> {
            TextView button = findViewById(R.id.comic_from_map_author_tv);
            button.setText(viewUser.name);
            button.setOnClickListener(v -> {
                Dialog authorDialog = new ShortProfileDialog(context, vComic.liveAuthor);
                authorDialog.show();
            });
        });

        vComic.liveTitlePage.observe((LifecycleOwner) context, (Bitmap page) -> {
            if (page == null) {
                Log.e("ShortComicDialog", "got null as title page");
                return;
            }

            ((ImageView) findViewById(R.id.comic_from_map_title_page_iv))
                    .setImageBitmap(page);

        });

        actionButton = findViewById(R.id.comic_from_map_action_button);
        if (origin == ComicOrigin.Created) {
            actionButton.setText(KNOWN_COMIC_BUTTON_TEXT);
            actionButton.setOnClickListener(v -> {
                Intent updateIntent = new Intent(context, ComicPreviewAndEditActivity.class);
                updateIntent.putExtra(
                        ComicPreviewAndEditActivity.COMIC_INFO_EXTRA,
                        vComic);
                updateIntent.putExtra(
                        ComicPreviewAndEditActivity.PREVIEW_REASON,
                        ComicPreviewAndEditActivity.PreviewReason.PreviewAndEdit);

                context.startActivity(updateIntent);
            });
        } else if (origin == ComicOrigin.Collected) {
            actionButton.setText(KNOWN_COMIC_BUTTON_TEXT);
            actionButton.setOnClickListener(v -> {
                Intent updateIntent = new Intent(context, ComicPreviewAndEditActivity.class);
                updateIntent.putExtra(
                        ComicPreviewAndEditActivity.COMIC_INFO_EXTRA,
                        vComic);
                updateIntent.putExtra(
                        ComicPreviewAndEditActivity.PREVIEW_REASON,
                        ComicPreviewAndEditActivity.PreviewReason.JustPreview);

                context.startActivity(updateIntent);
            });
        } else {
            // unknown comic

            actionButton.setText(UNKNOWN_COMIC_BUTTON_TEXT);
            actionButton.setOnClickListener(v -> {
                Log.e("shortComic", "COLLECTING this comic ... ");
                if (howToCollect != null) {
                    howToCollect.collect(vComic.comicId);
                }
            });

            // will be enabled once updateLocation method receives location
            // that is within the MAX_COLLECT_DISTANCE
            actionButton.setEnabled(false);
        }


    }


    @Override
    public void updateLocation(UserLocation newLocation) {
        if (origin == ComicOrigin.Unknown && actionButton != null) {
            double myDistance = myDistance(newLocation.getLatitude(), newLocation.getLongitude());
            Log.e("shortComic", "Calculated (km) distance is : " + myDistance);
            actionButton.setEnabled(myDistance < MAX_COLLECT_DISTANCE_KM);
        }
    }

    private double myDistance(double myLat, double myLong) {
        if (comic.getValue() != null) {
            return Distance.calculateInKm(
                    myLat,
                    myLong,
                    comic.getValue().location.latitude,
                    comic.getValue().location.longitude);
        }

        // just some value that wont trigger collect button activation
        return 2 * MAX_COLLECT_DISTANCE_KM;
    }

}
