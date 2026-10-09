package com.zhangzc.wifispoof.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

/**
 * 桌面入口（可被"隐藏桌面图标"开关禁用，禁用后仅影响入口，不影响设置界面）。
 * 仅负责跳转到设置界面，自身不承载配置 UI。
 */
public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        startActivity(new Intent(this, SettingsActivity.class));
        finish();
    }
}
