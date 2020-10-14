package com.ruiray.merge;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import com.ruiray.mergea.ModuleA;
import com.ruiray.ModuleB;
import com.ruiray.ModuleC;
import com.ruiray.ModuleD;

/**
 * Created by ruiray on 2020-10-14.
 */
public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final TextView tvMsg = (TextView) findViewById(R.id.tv_msg);

        tvMsg.append(ModuleA.getAppId() + ",   " + ModuleA.getModuleName(this) + "\n");
        tvMsg.append(ModuleB.getAppId() + ",   " + ModuleB.getModuleName(this) + "\n");
        tvMsg.append(ModuleC.getAppId() + ",   " + ModuleC.getModuleName(this) + "\n");
        tvMsg.append(ModuleD.getAppId() + ",   " + ModuleD.getModuleName(this) + "\n");
    }
}
