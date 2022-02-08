package mosis.comiccollector.ui;

public interface ListItemProvider {
    ListItemData getItem(int index);

    int getCount();
}
