package com.zhangzc.wifispoof.ui;

import android.app.Activity;
import android.content.ComponentName;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.Toast;

import com.zhangzc.wifispoof.Prefs;
import com.zhangzc.wifispoof.R;

/**
 * 配置界面（无桌面图标，仅由 MainActivity 跳转或 adb 启动）：
 * 启用开关 + SSID/BSSID/IP/网络ID + 隐藏桌面图标开关。
 * 配置写入 shared_prefs/config.xml，Hook 侧通过 XSharedPreferences 读取。
 */
@SuppressWarnings("deprecation")
public class SettingsActivity extends Activity {

    private SharedPreferences prefs;
    private Switch swEnable, swHideIcon;
    private EditText etSsid, etBssid, etIp, etNetworkId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        prefs = getSharedPreferences(Prefs.PREF_FILE, MODE_WORLD_READABLE);
        setContentView(R.layout.activity_settings);

        swEnable = findViewById(R.id.sw_enable);
        swEnable.setChecked(prefs.getBoolean("enabled", false));
        // 开关变化立即写入，Hook 侧每次查询都会重读
        swEnable.setOnCheckedChangeListener((buttonView, isChecked) ->
                prefs.edit().putBoolean("enabled", isChecked).apply());

        etSsid = findViewById(R.id.et_ssid);
        etBssid = findViewById(R.id.et_bssid);
        etIp = findViewById(R.id.et_ip);
        etNetworkId = findViewById(R.id.et_network_id);
        etSsid.setText(prefs.getString("ssid", "AndroidAP"));
        etBssid.setText(prefs.getString("bssid", "02:00:00:00:00:00"));
        etIp.setText(prefs.getString("ip", "192.168.1.100"));
        etNetworkId.setText(String.valueOf(prefs.getInt("network_id", 0)));

        Button btnSave = findViewById(R.id.btn_save);
        btnSave.setOnClickListener(v -> save());

        swHideIcon = findViewById(R.id.sw_hide_icon);
        swHideIcon.setChecked(isLauncherHidden());
        swHideIcon.setOnCheckedChangeListener((buttonView, isChecked) -> {
            setLauncherHidden(isChecked);
            Toast.makeText(this,
                    isChecked
                            ? "图标已隐藏；仍可在 LSPosed 管理器中打开设置，恢复图标：设置→应用→WiFi伪装→启用"
                            : "图标已恢复显示",
                    Toast.LENGTH_LONG).show();
        });
    }

    private void save() {
        SharedPreferences.Editor ed = prefs.edit();
        ed.putBoolean("enabled", swEnable.isChecked());
        ed.putString("ssid", etSsid.getText().toString().trim());
        ed.putString("bssid", etBssid.getText().toString().trim());
        ed.putString("ip", etIp.getText().toString().trim());
        try {
            ed.putInt("network_id", Integer.parseInt(etNetworkId.getText().toString().trim()));
        } catch (NumberFormatException ignored) {
            // 输入非法时保留原值
        }
        ed.apply();

        Toast.makeText(this, "已保存；请强制停止目标应用使其重新检测网络", Toast.LENGTH_LONG).show();
    }

    /** 桌面入口 MainActivity 是否被禁用（桌面无图标）。 */
    private boolean isLauncherHidden() {
        try {
            return getPackageManager().getComponentEnabledSetting(
                    new ComponentName(this, MainActivity.class))
                    == PackageManager.COMPONENT_ENABLED_STATE_DISABLED;
        } catch (Throwable t) {
            return false;
        }
    }

    /** 禁用/启用桌面入口 MainActivity，实现桌面图标隐藏/恢复（仅影响入口，不影响本设置界面）。 */
    private void setLauncherHidden(boolean hide) {
        int state = hide
                ? PackageManager.COMPONENT_ENABLED_STATE_DISABLED
                : PackageManager.COMPONENT_ENABLED_STATE_ENABLED;
        getPackageManager().setComponentEnabledSetting(
                new ComponentName(this, MainActivity.class), state, PackageManager.DONT_KILL_APP);
    }
}
