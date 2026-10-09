package com.zhangzc.wifispoof;

import java.net.InetAddress;

import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedBridge;

/**
 * 配置读取。
 * 配置由模块自身 App（SettingsActivity）写入 shared_prefs/config.xml；
 * Hook 进程通过 LSPosed 支持的 XSharedPreferences 跨进程读取。
 * 可配置项：启用开关 + SSID/BSSID/IP/网络ID；信号强度、链路速率、带宽为内置固定值。
 */
public final class Prefs {

    public static final String PREF_FILE = "config";

    private static XSharedPreferences sPrefs;

    private Prefs() {
    }

    public static synchronized void init() {
        if (sPrefs != null) {
            return;
        }
        try {
            sPrefs = new XSharedPreferences(BuildConfig.APPLICATION_ID, PREF_FILE);
            // LSPosed 下 XSharedPreferences 可直接读取模块私有数据；此行仅为兼容旧框架。
            sPrefs.makeWorldReadable();
        } catch (Throwable t) {
            XposedBridge.log("[WifiSpoof] 初始化 XSharedPreferences 失败: " + t);
        }
    }

    private static XSharedPreferences getPrefs() {
        init();
        if (sPrefs != null) {
            // 每次读取前重载，让 App 里的修改尽快生效
            sPrefs.reload();
        }
        return sPrefs;
    }

    private static boolean getBoolean(String key, boolean def) {
        try {
            XSharedPreferences p = getPrefs();
            return p == null ? def : p.getBoolean(key, def);
        } catch (Throwable t) {
            return def;
        }
    }

    private static String getString(String key, String def) {
        try {
            XSharedPreferences p = getPrefs();
            return p == null ? def : p.getString(key, def);
        } catch (Throwable t) {
            return def;
        }
    }

    private static int getInt(String key, int def) {
        try {
            XSharedPreferences p = getPrefs();
            return p == null ? def : p.getInt(key, def);
        } catch (Throwable t) {
            return def;
        }
    }

    // ---------- 开关 ----------

    public static boolean enabled() {
        return getBoolean("enabled", false);
    }

    // ---------- WiFi 身份参数（可配置） ----------

    public static String ssid() {
        String s = getString("ssid", "AndroidAP");
        return s == null || s.isEmpty() ? "AndroidAP" : s;
    }

    public static String bssid() {
        String s = getString("bssid", "02:00:00:00:00:00");
        return s == null || s.isEmpty() ? "02:00:00:00:00:00" : s;
    }

    public static String ip() {
        String s = getString("ip", "192.168.1.100");
        return s == null || s.isEmpty() ? "192.168.1.100" : s;
    }

    public static int networkId() {
        return getInt("network_id", 0);
    }

    /** 将 IP 字符串转为 WifiInfo#getIpAddress() 使用的整型表示（主机字节序）。 */
    public static int ipInt() {
        try {
            byte[] b = InetAddress.getByName(ip()).getAddress();
            return ((b[0] & 0xFF) << 24) | ((b[1] & 0xFF) << 16)
                    | ((b[2] & 0xFF) << 8) | (b[3] & 0xFF);
        } catch (Exception e) {
            return 0;
        }
    }

    // ---------- 固定信号/速率参数（不可配置） ----------

    public static int rssi() {
        return -45;
    }

    public static int linkSpeed() {
        return 100;
    }

    public static int frequency() {
        return 5180;
    }

    public static int downlinkKbps() {
        return 100000;
    }

    public static int uplinkKbps() {
        return 100000;
    }
}
