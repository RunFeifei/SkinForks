package com.fork.skin.lib;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.view.LayoutInflaterCompat;
import android.view.LayoutInflater;

import com.fork.skin.lib.observe.SkinObservable;
import com.fork.skin.lib.observe.SkinObserver;
import com.fork.skin.lib.widget.SkinCompatHelper;
import com.fork.skin.lib.widget.SkinCompatSupportable;
import com.fork.skin.lib.widget.SkinCompatThemeUtils;

import java.lang.reflect.Field;
import java.util.WeakHashMap;

/**
 * Created by PengFeifei on 2018/1/5.
 */

public class SkinActivityLifecycle implements Application.ActivityLifecycleCallbacks {

    private static volatile SkinActivityLifecycle instance = null;
    private WeakHashMap<Context, SkinObserver> mSkinObserverMap;
    private WeakHashMap<Context, SkinLayoutFactory> mSkinDelegateMap;


    private SkinActivityLifecycle(Application application) {
        application.registerActivityLifecycleCallbacks(this);
        installLayoutFactory(application);
        SkinManager.getInstance().addObserver(getObserver(application));
    }

    public static SkinActivityLifecycle init(Application application) {
        if (instance != null) {
            return instance;
        }
        synchronized (SkinActivityLifecycle.class) {
            if (instance == null) {
                instance = new SkinActivityLifecycle(application);
            }
        }
        return instance;
    }

    private void installLayoutFactory(Context context) {
        LayoutInflater layoutInflater = LayoutInflater.from(context);
        try {
            Field field = LayoutInflater.class.getDeclaredField("mFactorySet");
            field.setAccessible(true);
            field.setBoolean(layoutInflater, false);
            LayoutInflaterCompat.setFactory2(layoutInflater, getSkinDelegate(context));
        } catch (NoSuchFieldException | IllegalArgumentException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    private SkinObserver getObserver(final Context context) {
        if (mSkinObserverMap == null) {
            mSkinObserverMap = new WeakHashMap<>();
        }
        SkinObserver observer = mSkinObserverMap.get(context);
        if (observer == null) {
            observer = new SkinObserver() {
                @Override
                public void updateSkin(SkinObservable observable, Object o) {
                    if (context instanceof Activity && isContextSkinEnable(context)) {

                        updateStatusBarColor((Activity) context);
                        updateWindowBackground((Activity) context);
                    }
                    getSkinDelegate(context).applySkin();
                    if (context instanceof SkinCompatSupportable) {
                        ((SkinCompatSupportable) context).applySkin();
                    }
                }
            };
        }
        mSkinObserverMap.put(context, observer);
        return observer;
    }


    private SkinLayoutFactory getSkinDelegate(Context context) {
        if (mSkinDelegateMap == null) {
            mSkinDelegateMap = new WeakHashMap<>();
        }

        SkinLayoutFactory mSkinDelegate = mSkinDelegateMap.get(context);
        if (mSkinDelegate == null) {
            mSkinDelegate = new SkinLayoutFactory();
        }
        mSkinDelegateMap.put(context, mSkinDelegate);
        return mSkinDelegate;
    }


    private void updateStatusBarColor(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            int statusBarColorResId = SkinCompatThemeUtils.getStatusBarColorResId(activity);
            int colorPrimaryDarkResId = SkinCompatThemeUtils.getColorPrimaryDarkResId(activity);
            if (SkinCompatHelper.checkResourceId(statusBarColorResId) != SkinCompatHelper.INVALID_ID) {
                activity.getWindow().setStatusBarColor(SkinCompatResources.getInstance().getColor(statusBarColorResId));
            } else if (SkinCompatHelper.checkResourceId(colorPrimaryDarkResId) != SkinCompatHelper.INVALID_ID) {
                activity.getWindow().setStatusBarColor(SkinCompatResources.getInstance().getColor(colorPrimaryDarkResId));
            }
        }
    }

    private void updateWindowBackground(Activity activity) {
        int windowBackgroundResId = SkinCompatThemeUtils.getWindowBackgroundResId(activity);
        if (SkinCompatHelper.checkResourceId(windowBackgroundResId) != SkinCompatHelper.INVALID_ID) {
            Drawable drawable = SkinCompatResources.getInstance().getDrawable(activity, windowBackgroundResId);
            if (drawable != null) {
                activity.getWindow().setBackgroundDrawable(drawable);
            }
        }
    }


    @Override
    public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
        if (isContextSkinEnable(activity)) {
            installLayoutFactory(activity);
            updateStatusBarColor(activity);
            updateWindowBackground(activity);
            if (activity instanceof SkinCompatSupportable) {
                ((SkinCompatSupportable) activity).applySkin();
            }
        }
    }

    @Override
    public void onActivityStarted(Activity activity) {

    }

    @Override
    public void onActivityResumed(Activity activity) {
        if (isContextSkinEnable(activity)) {
            SkinManager.getInstance().addObserver(getObserver(activity));
        }
    }

    @Override
    public void onActivityPaused(Activity activity) {

    }

    @Override
    public void onActivityStopped(Activity activity) {

    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle outState) {

    }

    @Override
    public void onActivityDestroyed(Activity activity) {
        if (isContextSkinEnable(activity)) {
            SkinManager.getInstance().deleteObserver(getObserver(activity));
            mSkinObserverMap.remove(activity);
            mSkinDelegateMap.remove(activity);
        }
    }

    private boolean isContextSkinEnable(Context context) {
        return true;
    }

}
