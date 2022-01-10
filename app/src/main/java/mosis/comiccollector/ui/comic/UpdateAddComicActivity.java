package mosis.comiccollector.ui.comic;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
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
import android.widget.ProgressBar;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import mosis.comiccollector.R;
import mosis.comiccollector.model.Location;
import mosis.comiccollector.model.comic.Comic;
import mosis.comiccollector.ui.PermissionRequester;
import mosis.comiccollector.ui.PickAPlaceDialog;
import mosis.comiccollector.ui.ProgressDialog;
import mosis.comiccollector.ui.viewmodel.UpdateComicViewModel;
import mosis.comiccollector.util.Toaster;

public class UpdateAddComicActivity extends AppCompatActivity {

    public static final String COMIC_INFO_EXTRA = "comic_info";
    public static final String COMIC_PAGES_EXTRA = "comic_pages";

    private static final String[] POSSIBLE_PERMISSIONS = new String[]{
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_BACKGROUND_LOCATION
    };

    private ActivityResultLauncher<String[]> permissionsRequesterLauncher;
    private PermissionRequester permissionRequester;

    private ProgressBar progressBar;

    private PreviewListAdapter previewAdapter;
    private RecyclerView recView;

    private List<String> newPages = new ArrayList<>();

    private UpdateComicViewModel viewModel;

    private ActivityResultLauncher<String> loadPicActivityLauncher;

    private Location comicLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_update_comic);

        initLoadPictureActivityLauncher();
        initPermissionRequester();

        this.viewModel = new ViewModelProvider(this).get(UpdateComicViewModel.class);

        this.previewAdapter = new PreviewListAdapter(
                this,
                R.layout.comic_preview_list_item,
                this::previewClick);
        this.recView = findViewById(R.id.new_pages_rv);
        this.recView.setAdapter(this.previewAdapter);

        Intent intent = getIntent();
        if (intent.hasExtra(COMIC_INFO_EXTRA)) {

            // TODO this is gonna be ViewComic and not just Comic
            // still not implemented, got by clicking on preview from ProfileActivity
            this.fillComicInfo((Comic) intent.getSerializableExtra(COMIC_INFO_EXTRA));

            if (intent.hasExtra(COMIC_PAGES_EXTRA)) {
                this.fillExistingPages((List<Bitmap>) intent.getSerializableExtra(COMIC_PAGES_EXTRA));
            }

            findViewById(R.id.add_comic_finish_button).setOnClickListener(this::updateFinishClick);
            findViewById(R.id.pick_a_place_button).setVisibility(View.GONE);
        } else {
            findViewById(R.id.add_comic_finish_button).setOnClickListener(this::createFinishClick);
            findViewById(R.id.pick_a_place_button).setVisibility(View.VISIBLE);
            findViewById(R.id.pick_a_place_button).setOnClickListener(this::pickAPlaceClick);
        }

        findViewById(R.id.add_page_button).setOnClickListener(this::addPageClick);
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

    private void fillComicInfo(Comic info) {
        ((EditText) findViewById(R.id.new_comic_name_et)).setText(info.getComicTitle());
        ((EditText) findViewById(R.id.new_comic_description_et)).setText(info.getDescription());

        // TODO maybe add progress ...
    }

    private void fillExistingPages(List<Bitmap> pages) {
        int index = 0;
        for (Bitmap page : pages) {
            previewAdapter.addItem("" + index++, page);
        }
    }

    private void previewClick(String itemId) {
        Toaster.makeToast(this, "Not implemented yet ... ");
    }

    private void initLoadPictureActivityLauncher() {
        this.loadPicActivityLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                (Uri uri) -> {
                    Log.e("Pic uri", "We got this pic uri: " + uri.toString());
                    addNewPage(uri.toString());
                }
        );
    }

    private void addPageClick(View v) {
        this.loadPicActivityLauncher.launch("image/*");
    }

    private void addNewPage(String newUri) {
        int cnt = previewAdapter.getItemCount();
        this.newPages.add(newUri);
        previewAdapter.addItem("" + (cnt + 1), newUri);
    }

    private void pickAPlaceClick(View v) {
        this.requestPermissions((boolean granted) -> {
            if (granted) {
                Dialog dialog = new PickAPlaceDialog(this, (Location place) -> {
                    this.comicLocation = place;
                });
                dialog.show();
            } else {
                Toaster.makeToast(this, "You have to grant all permission ... ");
            }
        });
    }

    private void updateFinishClick(View v) {
        Toaster.makeToast(this, "Not implemented ... ");
    }

    private void createFinishClick(View v) {
        EditText titleText = findViewById(R.id.new_comic_name_et);
        EditText descText = findViewById(R.id.new_comic_description_et);

        if (validateInputs(titleText, descText)) {

            ProgressDialog progressDialog = new ProgressDialog(this, newPages.size());
            progressDialog.show();

            Handler mainHandler = new Handler(Looper.getMainLooper());

            viewModel.createComic(titleText.getText().toString(),
                    descText.getText().toString(),
                    comicLocation,
                    this.newPages)
                    .observe(this, (Long aLong) -> {
                        mainHandler.post(() -> {
                            progressDialog.addProgress(1);
                            if (progressDialog.isDone()) {
                                progressDialog.hide();
                                progressDialog.dismiss();
                            }
                        });
                    });

        } else {
            Toaster.makeToast(this, "Fill all fields to finish ... ");
        }

    }

    private boolean validateInputs(EditText title, EditText description) {
        return (title.getText() != null && !title.getText().toString().isEmpty())
                && (description.getText() != null && !description.getText().toString().isEmpty())
                && (newPages.size() > 0)
                && comicLocation != null;

    }

}
