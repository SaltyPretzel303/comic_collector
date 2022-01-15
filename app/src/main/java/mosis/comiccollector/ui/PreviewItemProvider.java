package mosis.comiccollector.ui;

public interface PreviewItemProvider {
    PreviewItemData getItem(int index);

    int getItemsCount();
}
