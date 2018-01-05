package com.fork.skin.skinforks;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.fork.skin.lib.SkinManager;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.skin).setOnClickListener(this);
        findViewById(R.id.textDefault).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.skin) {
            SkinManager.getInstance().loadSkin("skin-debug.apk", null);
            return;
        }
        SkinManager.getInstance().reset();
    }
}
