package mosis.comiccollector.ui;

public interface ProgressDisplay {

    int getMaxValue();

    int getProgress();

    int addProgress(int progress);

    boolean isDone();

    void show();

    void hide();

}
