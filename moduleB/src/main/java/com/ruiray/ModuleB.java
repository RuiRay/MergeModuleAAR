package com.ruiray;

import android.content.Context;
import android.support.annotation.Keep;

import com.ruiray.mergeb.BuildConfig;
import com.ruiray.mergeb.R;

@Keep
public class ModuleB {

    public static String getAppId() {
        return BuildConfig.APPLICATION_ID;
    }

    public static String getModuleName(Context context) {
        return context.getResources().getString(R.string.app_name_modelB);
    }
}
