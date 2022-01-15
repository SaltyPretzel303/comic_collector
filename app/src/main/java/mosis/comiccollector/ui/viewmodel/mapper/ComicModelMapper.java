package mosis.comiccollector.ui.viewmodel.mapper;

import mosis.comiccollector.model.comic.Comic;
import mosis.comiccollector.repository.DataMapper;
import mosis.comiccollector.ui.comic.ViewComic;

public class ComicModelMapper implements DataMapper<Comic, ViewComic> {
    @Override
    public ViewComic mapToViewModel(Comic input) {
        return new ViewComic(
                input.getId(),
                input.getComicTitle(),
                input.getDescription(),
                input.getAuthorId(),
                input.getLocation(),
                input.getPagesCount());
    }
}
