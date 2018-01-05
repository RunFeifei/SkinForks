package com.fork.skin.lib;

import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v7.widget.VectorEnabledTintResources;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewParent;
import android.widget.Button;

import com.fork.skin.lib.widget.SkinCompatButton;
import com.fork.skin.lib.widget.SkinCompatImageView;
import com.fork.skin.lib.widget.SkinCompatLinearLayout;
import com.fork.skin.lib.widget.SkinCompatSupportable;
import com.fork.skin.lib.widget.SkinCompatTextView;
import com.fork.skin.lib.widget.SkinCompatView;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by PengFeifei on 2018/1/5.
 */

public class SkinLayoutFactory implements LayoutInflater.Factory2 {

    private List<WeakReference<SkinCompatSupportable>> mSkinHelpers = new ArrayList<>();

    @Override
    public View onCreateView(String name, Context context, AttributeSet attrs) {
        return null;
    }


    @Override
    public View onCreateView(View parent, String name, Context context, AttributeSet attrs) {
        View view=createViewFromFV(context, name, attrs);
        if (view == null) {
            return null;
        }
        if (view instanceof SkinCompatSupportable) {
            mSkinHelpers.add(new WeakReference<>((SkinCompatSupportable) view));
        }
        return view;
    }


    private View createViewFromFV(Context context, String name, AttributeSet attrs) {
        View view = null;
        if (name.contains(".")) {
            return null;
        }
        switch (name) {
            case "View":
                view = new SkinCompatView(context, attrs);
                break;
            case "LinearLayout":
                view = new SkinCompatLinearLayout(context, attrs);
                break;
            case "TextView":
                view = new SkinCompatTextView(context, attrs);
                break;
            case "ImageView":
                view = new SkinCompatImageView(context, attrs);
                break;
            case "Button":
                view = new SkinCompatButton(context, attrs);
                break;
        }
        return view;
    }

    public void applySkin() {
        if (mSkinHelpers != null && !mSkinHelpers.isEmpty()) {
            for (WeakReference ref : mSkinHelpers) {
                if (ref != null && ref.get() != null) {
                    ((SkinCompatSupportable) ref.get()).applySkin();
                }
            }
        }
    }

}
