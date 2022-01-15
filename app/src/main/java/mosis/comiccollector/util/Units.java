package mosis.comiccollector.util;

import android.content.Context;

public class Units {

    public static int dpToPx(Context context, int dps) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dps * scale + 0.5f);
    }

}
