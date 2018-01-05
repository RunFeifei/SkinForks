package com.fork.skin.lib;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.fork.skin.lib.observe.SkinObservable;
import com.fork.skin.lib.utils.SkinPreference;

import java.lang.reflect.Method;

/**
 * Created by PengFeifei on 2018/1/5.
 */

public class SkinManager extends SkinObservable {

    private final Object mLock = new Object();
    private boolean mLoading = false;
    private final Context mAppContext;
    private static volatile SkinManager sInstance;

    private SkinManager(Context context) {
        mAppContext = context.getApplicationContext();
    }

    public static SkinManager init(Context context) {
        if (sInstance == null) {
            synchronized (SkinManager.class) {
                if (sInstance == null) {
                    sInstance = new SkinManager(context);
                }
            }
        }
        SkinPreference.init(context);
        SkinCompatResources.init(context);
        return sInstance;
    }

    public static SkinManager getInstance() {
        return sInstance;
    }

    public void reset() {
        loadSkin("", null);
    }

    public String getSkinPackageName(String skinPkgPath) {
        PackageManager mPm = mAppContext.getPackageManager();
        PackageInfo info = mPm.getPackageArchiveInfo(skinPkgPath, PackageManager.GET_ACTIVITIES);
        return info.packageName;
    }

    @Nullable
    public Resources getSkinResources(String skinPkgPath) {
        try {
            AssetManager assetManager = AssetManager.class.newInstance();
            Method addAssetPath = assetManager.getClass().getMethod("addAssetPath", String.class);
            addAssetPath.invoke(assetManager, skinPkgPath);

            Resources superRes = mAppContext.getResources();
            return new Resources(assetManager, superRes.getDisplayMetrics(), superRes.getConfiguration());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public interface SkinLoaderListener {
        void onStart();

        void onSuccess();

        void onFailed(String errMsg);
    }

    public AsyncTask loadSkin(String skinName, SkinLoaderListener listener) {
        return new SkinLoadTask(listener).execute(skinName);
    }

    private class SkinLoadTask extends AsyncTask<String, Void, String> {
        private final SkinLoaderListener mListener;
        private final SkinAssetsLoader mStrategy;


        SkinLoadTask(@Nullable SkinLoaderListener listener) {
            mListener = listener;
            mStrategy = new SkinAssetsLoader();
        }

        protected void onPreExecute() {
            if (mListener != null) {
                mListener.onStart();
            }
        }

        @Override
        protected String doInBackground(String... params) {
            synchronized (mLock) {
                while (mLoading) {
                    try {
                        mLock.wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                mLoading = true;
            }
            try {
                if (params.length == 1) {
                    if (TextUtils.isEmpty(params[0])) {
                        SkinCompatResources.getInstance().reset();
                        return params[0];
                    }
                    if (!TextUtils.isEmpty(
                            mStrategy.loadSkinInBackground(mAppContext, params[0]))) {
                        return params[0];
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            SkinCompatResources.getInstance().reset();
            return null;
        }

        protected void onPostExecute(String skinName) {
            synchronized (mLock) {
                // skinName 为""时，恢复默认皮肤
                if (skinName != null) {
                    SkinPreference.getInstance().setSkinName(skinName).setSkinStrategy(0).commitEditor();
                    notifyUpdateSkin();
                    if (mListener != null) mListener.onSuccess();
                } else {
                    SkinPreference.getInstance().setSkinName("").setSkinStrategy(-1).commitEditor();
                    if (mListener != null) mListener.onFailed("皮肤资源获取失败");
                }
                mLoading = false;
                mLock.notifyAll();
            }
        }
    }


}
