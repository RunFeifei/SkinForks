package com.fork.skin.skinforks;

import android.app.Application;

import com.fork.skin.lib.SkinActivityLifecycle;
import com.fork.skin.lib.SkinManager;

/**
 * Created by PengFeifei on 2018/1/5.
 */

public class SkinApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        SkinManager.init(this);
        SkinActivityLifecycle.init(this);
    }


}
