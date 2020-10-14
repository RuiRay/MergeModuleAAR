package com.ruiray.mergea;

import android.content.Context;
import android.support.annotation.Keep;

@Keep
public class ModuleA {

    public static String getAppId() {
        return BuildConfig.APPLICATION_ID;
    }

    public static String getModuleName(Context context) {
        return context.getResources().getString(R.string.app_name_modelA);
    }
}
