package mosis.comiccollector.ui.viewmodel.mapper;

import org.jetbrains.annotations.NotNull;

import mosis.comiccollector.model.comic.Comic;
import mosis.comiccollector.repository.DataMapper;
import mosis.comiccollector.ui.comic.ViewComic;

public class ComicModelMapper implements DataMapper<Comic, ViewComic> {
    @Override
    public ViewComic mapThis(@NotNull Comic input) {
        return new ViewComic(
                input.getId(),
                input.getAuthorId(),
                input.getComicTitle(),
                input.getDescription(),
                input.getLocation(),
                input.getPagesCount(),
                input.getRating());
    }

}
