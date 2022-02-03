package mosis.comiccollector.ui.viewmodel.mapper;

import org.jetbrains.annotations.NotNull;

import mosis.comiccollector.model.comic.Comic;
import mosis.comiccollector.repository.DataMapper;
import mosis.comiccollector.ui.comic.ViewComic;

public class ViewComicMapper implements DataMapper<ViewComic, Comic> {
    @Override
    public Comic mapThis(@NotNull ViewComic input) {
        return new Comic(
                input.authorId,
                input.title,
                input.description,
                input.rating,
                input.pagesCount,
                input.location);
    }
}
