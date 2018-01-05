package com.fork.skin.lib;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.content.res.XmlResourceParser;
import android.graphics.drawable.Drawable;
import android.support.annotation.AnyRes;
import android.support.v7.content.res.AppCompatResources;
import android.text.TextUtils;
import android.util.TypedValue;


public class SkinCompatResources {

    private static volatile SkinCompatResources sInstance;
    private final Context mAppContext;
    private Resources mResources;
    private String mSkinPkgName;
    private String mSkinName;
    private SkinAssetsLoader mStrategy;
    private boolean isDefaultSkin;

    private SkinCompatResources(Context context) {
        mAppContext = context.getApplicationContext();
        reset();
    }

    public static void init(Context context) {
        if (sInstance == null) {
            synchronized (SkinCompatResources.class) {
                if (sInstance == null) {
                    sInstance = new SkinCompatResources(context);
                }
            }
        }
    }

    public static SkinCompatResources getInstance() {
        return sInstance;
    }

    public void reset() {
        mResources = mAppContext.getResources();
        mSkinPkgName = mAppContext.getPackageName();
        mSkinName = "";
        mStrategy = null;
        isDefaultSkin = true;
    }


    public void setupSkin(Resources resources, String pkgName, String skinName, SkinAssetsLoader strategy) {
        mResources = resources;
        mSkinPkgName = pkgName;
        mSkinName = skinName;
        mStrategy = strategy;
        isDefaultSkin = TextUtils.isEmpty(skinName);
    }

    public int getColor(int resId) {
        if (!isDefaultSkin) {
            int targetResId = getTargetResId(resId);
            if (targetResId != 0) {
                return mResources.getColor(targetResId);
            }
        }
        return mAppContext.getResources().getColor(resId);
    }

    public float getDimension(int id) {
        float dimension = mAppContext.getResources().getDimension(id);
        if (isDefaultSkin) {
            return dimension;
        }

        String resName = mAppContext.getResources().getResourceEntryName(id);

        int targetResId = mResources.getIdentifier(resName, "dimen", mSkinPkgName);

        return targetResId == 0 ? dimension : mResources.getDimension(targetResId);
    }

    public Drawable getSrcCompatDrawable(Context context, int resId) {
        if (!isDefaultSkin) {
            int targetResId = getTargetResId(resId);
            if (targetResId != 0) {
                try {
                    return mResources.getDrawable(targetResId);
                } catch (Exception e) {
                }
            }
        }
        return AppCompatResources.getDrawable(context, resId);
    }

    public Drawable getDrawable(int resId) {
        if (!isDefaultSkin) {
            int targetResId = getTargetResId(resId);
            if (targetResId != 0) {
                return mResources.getDrawable(targetResId);
            }
        }
        return mAppContext.getResources().getDrawable(resId);
    }

    public Drawable getDrawable(Context context, int resId) {
        Drawable originDrawable = AppCompatResources.getDrawable(context, resId);
        if (isDefaultSkin) {
            return originDrawable;
        }

        String resName = context.getResources().getResourceEntryName(resId);

        int targetResId = mResources.getIdentifier(resName, "drawable", mSkinPkgName);

        return targetResId == 0 ? originDrawable : mResources.getDrawable(targetResId);

    }

    public ColorStateList getColorStateList(int resId) {
        if (!isDefaultSkin) {
            int targetResId = getTargetResId(resId);
            if (targetResId != 0) {
                return mResources.getColorStateList(targetResId);
            }
        }
        return mAppContext.getResources().getColorStateList(resId);
    }

    public XmlResourceParser getXml(int resId) {
        if (!isDefaultSkin) {
            int targetResId = getTargetResId(resId);
            if (targetResId != 0) {
                return mResources.getXml(targetResId);
            }
        }
        return mAppContext.getResources().getXml(resId);
    }

    public void getValue(@AnyRes int resId, TypedValue outValue, boolean resolveRefs) {
        if (!isDefaultSkin) {
            int targetResId = getTargetResId(resId);
            if (targetResId != 0) {
                mResources.getValue(targetResId, outValue, resolveRefs);
                return;
            }
        }
        mAppContext.getResources().getValue(resId, outValue, resolveRefs);
    }

    private int getTargetResId(int resId) {
        try {
            String resName = mAppContext.getResources().getResourceEntryName(resId);
            String type = mAppContext.getResources().getResourceTypeName(resId);
            return mResources.getIdentifier(resName, type, mSkinPkgName);
        } catch (Exception e) {
            // 换肤失败不至于应用崩溃.
            return 0;
        }
    }
}
