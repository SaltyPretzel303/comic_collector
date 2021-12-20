package mosis.comiccollector.ui.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;

import mosis.comiccollector.repository.ComicRepository;
import mosis.comiccollector.util.DepProvider;

public class ComicsViewModel extends AndroidViewModel {

    private ComicRepository repository;

    public ComicsViewModel(@NonNull Application application) {
        super(application);

        this.repository = DepProvider.getComicRepository();


    }
}
