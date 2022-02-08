package mosis.comiccollector.ui.comic;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import mosis.comiccollector.R;
import mosis.comiccollector.model.Location;
import mosis.comiccollector.ui.PermissionRequester;
import mosis.comiccollector.ui.map.PickAPlaceDialog;
import mosis.comiccollector.ui.PreviewItemData;
import mosis.comiccollector.ui.PreviewItemProvider;
import mosis.comiccollector.ui.ProgressDialog;
import mosis.comiccollector.ui.user.ShortProfileDialog;
import mosis.comiccollector.ui.viewmodel.ComicPreviewViewModel;
import mosis.comiccollector.util.Toaster;

public class ComicPreviewAndEditActivity extends AppCompatActivity {

    public enum PreviewReason {
        JustPreview,
        PreviewAndEdit,
        Create
    }

    public enum UpdateResult {
        Updated,
        Created
    }

    public static final String UPDATE_RESULT_PATH = "update_result";
    public static final String NEW_PAGES_COUNT_RESULT = "new_pages_count";
    public static final String COMIC_ID_RESULT = "new_comic_id";

    public static final String PREVIEW_REASON = "reason";
    public static final String COMIC_INFO_EXTRA = "comic_info";


    private static final String[] POSSIBLE_PERMISSIONS = new String[]{
        Manifest.permission.WRITE_EXTERNAL_STORAGE,
        Manifest.permission.ACCESS_COARSE_LOCATION,
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_BACKGROUND_LOCATION
    };

    private static final int PREVIEW_PAGE_WIDTH = 150;
    private static final int PREVIEW_PAGE_HEIGHT = 200;

    private PreviewReason reason;

    private ActivityResultLauncher<String[]> permissionsRequesterLauncher;
    private PermissionRequester permissionRequester;

    private RecyclerView pagesListRv;
    private PreviewListAdapter pagesAdapter;

    private ComicPreviewViewModel viewModel;
    private Location pickedLocation;

    private ActivityResultLauncher<String> loadPicActivityLauncher;
    private ActivityResultLauncher<Intent> readActivityLauncher;

    private Dialog currentDialog;

    private boolean updated = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comic_preview_edit);

        initLoadPictureActivityLauncher();
        initReadActivityLauncher();
        initPermissionRequester();

        this.viewModel = new ViewModelProvider(this).get(ComicPreviewViewModel.class);

        ViewComic comic = null;
        if (getIntent().hasExtra(COMIC_INFO_EXTRA)) {
            comic = (ViewComic) getIntent().getSerializableExtra(COMIC_INFO_EXTRA);
            viewModel.setComic(comic);
        }

        reason = this.getReason(getIntent());

        if (reason == PreviewReason.Create) {
            setupForCreate();
            setupPagesAdapter(this::previewWithRemoveClick);
        } else if (reason == PreviewReason.JustPreview) {
            setupForPreview();
            setupPagesAdapter(this::pagePreviewCLick);
        } else {
            // PreviewReason.PreviewAndEdit
            setupForEdit();
            setupPagesAdapter(this::previewWithRemoveClick);
        }

    }

    private void setupForPreview() {
        findViewById(R.id.new_comic_name_et).setVisibility(View.GONE);
        findViewById(R.id.new_comic_description_et).setVisibility(View.GONE);

        findViewById(R.id.comic_name_tv).setVisibility(View.VISIBLE);
        findViewById(R.id.comic_description_tv).setVisibility(View.VISIBLE);

        findViewById(R.id.create_comic_finish_buttons).setVisibility(View.GONE);
        findViewById(R.id.add_page_button).setVisibility(View.GONE);

        ((TextView) findViewById(R.id.comic_name_tv))
            .setText(viewModel.getComic().title);
        ((TextView) findViewById(R.id.comic_description_tv))
            .setText(viewModel.getComic().description);
        ((RatingBar) findViewById(R.id.preview_edit_comic_rating_rb))
            .setRating(viewModel.getComic().rating);

        findViewById(R.id.preview_comic_author_button).setOnClickListener(this::showAuthorClick);
    }

    private void setupForEdit() {
        findViewById(R.id.new_comic_name_et).setVisibility(View.GONE);
        findViewById(R.id.new_comic_description_et).setVisibility(View.GONE);

        findViewById(R.id.comic_name_tv).setVisibility(View.VISIBLE);
        findViewById(R.id.comic_description_tv).setVisibility(View.VISIBLE);

        findViewById(R.id.pick_a_place_button).setVisibility(View.GONE);
        findViewById(R.id.preview_comic_author_button).setVisibility(View.GONE);

        findViewById(R.id.add_page_button).setOnClickListener(this::addPageClick);
        findViewById(R.id.add_comic_finish_button).setOnClickListener(this::updateFinishClick);

        ((TextView) findViewById(R.id.comic_name_tv))
            .setText(viewModel.getComic().title);
        ((TextView) findViewById(R.id.comic_description_tv))
            .setText(viewModel.getComic().description);
        ((RatingBar) findViewById(R.id.preview_edit_comic_rating_rb))
            .setRating(viewModel.getComic().rating);
    }

    private void setupForCreate() {

        findViewById(R.id.new_comic_name_et).setVisibility(View.VISIBLE);
        findViewById(R.id.new_comic_description_et).setVisibility(View.VISIBLE);

        findViewById(R.id.comic_name_tv).setVisibility(View.GONE);
        findViewById(R.id.comic_description_tv).setVisibility(View.GONE);

        findViewById(R.id.preview_comic_author_button_holder).setVisibility(View.GONE);

        findViewById(R.id.preview_edit_comic_rating_rb).setVisibility(View.GONE);

        findViewById(R.id.add_page_button).setOnClickListener(this::addPageClick);
        findViewById(R.id.pick_a_place_button).setOnClickListener(this::pickAPlaceClick);
        findViewById(R.id.add_comic_finish_button).setOnClickListener(this::createFinishClick);

    }

    private void setupPagesAdapter(PreviewClickHandler clickHandler) {

        pagesListRv = findViewById(R.id.new_pages_rv);
        pagesAdapter = new PreviewListAdapter(
            this,
            R.layout.comic_preview_list_item,
            PREVIEW_PAGE_WIDTH,
            PREVIEW_PAGE_HEIGHT,
            new PreviewItemProvider() {
                @Override
                public PreviewItemData getItem(int index) {
                    return new PreviewItemData(
                        "" + index,
                        "" + index,
                        " ",
                        viewModel.getPage(
                            index,
                            PREVIEW_PAGE_WIDTH,
                            PREVIEW_PAGE_HEIGHT)
                            .livePage);
                }

                @Override
                public int getItemsCount() {
                    return viewModel.getPagesCount();
                }
            },
            clickHandler);

        pagesListRv.setAdapter(this.pagesAdapter);
    }

    private void pagePreviewCLick(String itemId) {
        Bitmap image = viewModel.getPage(
            Integer.parseInt(itemId),
            ImagePreviewWithActionsDialog.PAGE_WIDTH,
            ImagePreviewWithActionsDialog.PAGE_HEIGHT)
            .livePage
            .getValue(); // this bitmap should already be loaded
        // TODO dimensions for dialog are gonna be larger than for previewList
        // pass LiveData to dialog and setup ViewModel to reload bitmap if diff size is requested

        Intent readIntent = new Intent(this, ReadActivity.class);
        readIntent.putExtra(ReadActivity.COMIC_EXTRA, viewModel.getComic());
        readIntent.putExtra(ReadActivity.COMIC_PAGE_EXTRA, Integer.parseInt(itemId));
//        startActivity(readIntent);
        readActivityLauncher.launch(readIntent);

    }

    private void previewWithRemoveClick(String itemId) {
        int index = Integer.parseInt(itemId);

        if (viewModel.isNewPage(index)) {
            // clicked on some of the new comics
            Bitmap image = viewModel.getPage(
                Integer.parseInt(itemId),
                ImagePreviewWithActionsDialog.PAGE_WIDTH,
                ImagePreviewWithActionsDialog.PAGE_HEIGHT)
                .livePage
                .getValue(); // this bitmap should already be loaded

            ImagePreviewWithActionsDialog.Action action = new ImagePreviewWithActionsDialog.Action() {
                @Override
                public String getText() {
                    return "Remove";
                }

                @Override
                public void handlePress(View v) {
                    currentDialog.dismiss();
                    currentDialog = null;

                    removePage(index);
                }
            };


            this.currentDialog = new ImagePreviewWithActionsDialog(this, image, Arrays.asList(action));
            this.currentDialog.show();

        } else {
            pagePreviewCLick(itemId);
        }
    }

    private PreviewReason getReason(Intent intent) {
        if (intent.hasExtra(PREVIEW_REASON)) {
            return (PreviewReason) intent.getSerializableExtra(PREVIEW_REASON);
        }

        return PreviewReason.JustPreview;
    }

    private void initPermissionRequester() {
        this.permissionsRequesterLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestMultiplePermissions(),
            (Map<String, Boolean> result) -> {

                for (String key : result.keySet()) {

                    if (result.get(key) == false) {
                        Log.e("Permission denied", "User denied permission: " + key);
                        if (permissionRequester != null) {
                            permissionRequester.handlePermissionResult(false);
                            permissionRequester = null;
                        }
                        return;
                    }
                }

                Log.e("permissions", "All permissions granted ... ");

                if (permissionRequester != null) {
                    permissionRequester.handlePermissionResult(true);
                    permissionRequester = null;
                }

                return;
            });
    }

    private List<String> getRequiredPermissions() {
        ArrayList<String> retList = new ArrayList<>();

        for (String permission : POSSIBLE_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                // Permission is not already granted

                retList.add(permission);
            }
        }

        return retList;
    }

    private void requestPermissions(PermissionRequester requester) {

        this.permissionRequester = requester;

        List<String> notGrantedPermissions = this.getRequiredPermissions();
        if (notGrantedPermissions.size() > 0) {
            this.permissionsRequesterLauncher.launch(notGrantedPermissions.toArray(new String[0]));
            // toArray(new String[0]) is how you convert List<String> to String[]
        } else {
            if (this.permissionRequester != null) {
                this.permissionRequester.handlePermissionResult(true);
                this.permissionRequester = null;
            }
        }

    }

    private void initLoadPictureActivityLauncher() {
        this.loadPicActivityLauncher = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            (Uri uri) -> {
                if (uri == null) {
                    Log.e("newPageAct", "Load new page act. returned null as uri ... ");
                    return;
                }
                addNewPage(uri.toString());
            }
        );
    }

    private void initReadActivityLauncher() {
        readActivityLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            (actResult) -> {
                if (actResult.getResultCode() == Activity.RESULT_OK
                    && actResult.getData() != null
                    && actResult.getData().hasExtra(ReadActivity.RATING_DONE_EXTRA)
                    && actResult.getData().getBooleanExtra(ReadActivity.RATING_DONE_EXTRA, false)) {

                    viewModel
                        .getRating()
                        .observe(this, (rating) -> {
                            ((RatingBar) findViewById(R.id.preview_edit_comic_rating_rb))
                                .setRating(rating);
                        });
                }
            });
    }


    private void addPageClick(View v) {
        this.loadPicActivityLauncher.launch("image/*");
    }

    private void addNewPage(String newUri) {

        int index = viewModel.addPage(newUri);
        pagesAdapter.notifyItemInserted(index);

        pagesListRv.scrollToPosition(index);
    }

    private void removePage(int index) {
        this.viewModel.removePage(index);
        this.pagesAdapter.notifyItemRemoved(index);
//        this.pagesAdapter.notifyItemRangeChanged(index - 1, viewModel.getNewCount());
        this.pagesAdapter.notifyDataSetChanged();

    }

    private void viewAuthorClick(View v) {
        // TODO implement
    }

    private void pickAPlaceClick(View v) {
        this.requestPermissions((boolean granted) -> {
            if (granted) {
                Dialog dialog = new PickAPlaceDialog(this, (Location place) -> {
                    this.pickedLocation = place;
                });
                dialog.show();
            } else {
                Toaster.makeToast(this, "You have to grant all permissions ... ");
            }
        });
    }

    private void createFinishClick(View v) {
        EditText titleText = findViewById(R.id.new_comic_name_et);
        EditText descText = findViewById(R.id.new_comic_description_et);

        if (validateInputs(titleText, descText)) {

            ProgressDialog progressDialog = new ProgressDialog(this, viewModel.getNewCount());
            progressDialog.show();

            String title = titleText.getText().toString();
            String description = descText.getText().toString();

            Handler mainHandler = new Handler(Looper.getMainLooper());

            viewModel.createComic(
                titleText.getText().toString(),
                descText.getText().toString(),
                pickedLocation)
                .observe(this, (ComicPreviewViewModel.UploadProgress upProgress) -> {

                    mainHandler.post(() -> {
                        if (upProgress.progress < 0) {
                            Log.e("progressBar", "Cant update progress bar with neg. value ... ");
                            return;
                        }

                        progressDialog.addProgress(1);
                        if (progressDialog.isDone()) {
                            progressDialog.hide();
                            progressDialog.dismiss();

                            setupForEdit();

                            updated = true;
                        }
                    });
                });

        } else {
            Toaster.makeToast(this, "Fill all fields to finish ... ");
        }

    }

    private void showAuthorClick(View v) {
        Dialog authorDialog = new ShortProfileDialog(
            this,
            viewModel.getAuthor());
        authorDialog.show();
    }

    private void updateFinishClick(View v) {
        if (viewModel.getNewCount() == 0) {
            // I guess nothing ...
            Toaster.makeToast(this, "Nothing to update ... ");
            return;
        }

        Log.e("updateFinishCLick", "Trying to update with: " + viewModel.getNewCount() + " new pages ... ");
        ProgressDialog progressDialog = new ProgressDialog(this, viewModel.getNewCount());
        progressDialog.show();

        Handler mainHandler = new Handler(Looper.getMainLooper());

        viewModel.updateComic().observe((LifecycleOwner) this, (Long aLong) -> {
            mainHandler.post(() -> {
                progressDialog.addProgress(1);
                Log.e("updateDialog", "Got progress ... ");
                if (progressDialog.isDone()) {
                    progressDialog.hide();
                    progressDialog.dismiss();

                    updated = true;
                }
            });
        });


    }

    private boolean validateInputs(EditText title, EditText description) {
        return (title.getText() != null && !title.getText().toString().isEmpty())
            && (description.getText() != null && !description.getText().toString().isEmpty())
            && (viewModel.getNewCount() > 0)
            && (this.pickedLocation != null);

    }

    @Override
    public void onBackPressed() {
        Intent retIntent = new Intent();
        ViewComic comic = viewModel.getComic();

        if (reason == PreviewReason.Create && comic != null && updated) { // updated or created
            retIntent.putExtra(UPDATE_RESULT_PATH, UpdateResult.Created);
            retIntent.putExtra(COMIC_ID_RESULT, comic.comicId);
        } else if (reason == PreviewReason.PreviewAndEdit && comic != null && updated) {
            Log.e("backPress", "Returning with update ... ");
            retIntent.putExtra(UPDATE_RESULT_PATH, UpdateResult.Updated);
            retIntent.putExtra(COMIC_ID_RESULT, comic.comicId);
            retIntent.putExtra(NEW_PAGES_COUNT_RESULT, comic.pagesCount);
        }

        setResult(Activity.RESULT_OK, retIntent);

        finish();
    }

}
