package com.ruiray;

import android.content.Context;
import android.support.annotation.Keep;

import com.ruiray.merged.BuildConfig;
import com.ruiray.merged.R;

@Keep
public class ModuleD {

    public static String getAppId() {
        return BuildConfig.APPLICATION_ID;
    }

    public static String getModuleName(Context context) {
        return context.getResources().getString(R.string.app_name_modelD);
    }
}
