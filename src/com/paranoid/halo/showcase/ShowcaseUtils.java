package com.paranoid.halo.showcase;

import android.content.Context;

public class ShowcaseUtils {
    public static final int SHOWCASE_PIN_APPLICATION = 0;
    public static final int SHOWCASE_START_SERVICE = 1;

    private static final String PREFERENCES_SHOWCASE = "showcase";
    private static final String HAS_SHOWCASED = "has_showcased_";

    public static boolean needsShowcase(Context context, int showcaseId) {
        return !context.getSharedPreferences(PREFERENCES_SHOWCASE, Context.MODE_PRIVATE)
                .getBoolean(HAS_SHOWCASED + showcaseId, false);
    }

    public static boolean setShowcased(Context context, int showcaseId) {
        return context.getSharedPreferences(PREFERENCES_SHOWCASE, Context.MODE_PRIVATE)
                .edit().putBoolean(HAS_SHOWCASED + showcaseId, true).commit();
    }
}
