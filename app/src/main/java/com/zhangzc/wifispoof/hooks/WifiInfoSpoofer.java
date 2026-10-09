package com.zhangzc.wifispoof.hooks;

import android.net.wifi.SupplicantState;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;

import com.zhangzc.wifispoof.Prefs;

import java.lang.reflect.Constructor;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;

/**
 * Hook WifiManager / WifiInfo：
 * 伪造 SSID、BSSID、信号强度、链路速率、IP 等 WiFi 详情。
 */
public final class WifiInfoSpoofer {

    private WifiInfoSpoofer() {
    }

    public static void hook(ClassLoader cl) {
        // ---- WifiInfo 实例方法 ----
        XposedHelpers.findAndHookMethod(WifiInfo.class, "getSSID", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) {
                if (Prefs.enabled()) {
                    // AOSP 约定 getSSID 带引号返回，多数应用会自行去除
                    param.setResult("\"" + Prefs.ssid() + "\"");
                }
            }
        });

        XposedHelpers.findAndHookMethod(WifiInfo.class, "getBSSID", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) {
                if (Prefs.enabled()) {
                    param.setResult(Prefs.bssid());
                }
            }
        });

        XposedHelpers.findAndHookMethod(WifiInfo.class, "getRssi", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) {
                if (Prefs.enabled()) {
                    param.setResult(Prefs.rssi());
                }
            }
        });

        XposedHelpers.findAndHookMethod(WifiInfo.class, "getLinkSpeed", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) {
                if (Prefs.enabled()) {
                    param.setResult(Prefs.linkSpeed());
                }
            }
        });

        XposedHelpers.findAndHookMethod(WifiInfo.class, "getFrequency", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) {
                if (Prefs.enabled()) {
                    param.setResult(Prefs.frequency());
                }
            }
        });

        XposedHelpers.findAndHookMethod(WifiInfo.class, "getIpAddress", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) {
                if (Prefs.enabled()) {
                    param.setResult(Prefs.ipInt());
                }
            }
        });

        XposedHelpers.findAndHookMethod(WifiInfo.class, "getNetworkId", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) {
                if (Prefs.enabled()) {
                    param.setResult(Prefs.networkId());
                }
            }
        });

        XposedHelpers.findAndHookMethod(WifiInfo.class, "getHiddenSSID", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) {
                if (Prefs.enabled()) {
                    param.setResult(false);
                }
            }
        });

        XposedHelpers.findAndHookMethod(WifiInfo.class, "getMacAddress", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) {
                if (Prefs.enabled()) {
                    param.setResult("02:00:00:00:00:00");
                }
            }
        });

        XposedHelpers.findAndHookMethod(WifiInfo.class, "getSupplicantState", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) {
                if (Prefs.enabled()) {
                    param.setResult(SupplicantState.COMPLETED);
                }
            }
        });

        // 以下方法在 Android 11+ 才存在；旧系统找不到方法时捕获异常跳过，避免模块崩溃
        hookIfExists(cl, "is24GHz", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) {
                if (Prefs.enabled()) {
                    param.setResult(false);
                }
            }
        });
        hookIfExists(cl, "is5GHz", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) {
                if (Prefs.enabled()) {
                    param.setResult(true);
                }
            }
        });
        hookIfExists(cl, "is6GHz", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) {
                if (Prefs.enabled()) {
                    param.setResult(false);
                }
            }
        });

        // ---- WifiManager 级 ----
        // getConnectionInfo() 在系统认为未连 WiFi 时可能返回 null，此时构造一个空对象
        XposedHelpers.findAndHookMethod(WifiManager.class, "getConnectionInfo", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) {
                if (!Prefs.enabled()) {
                    return;
                }
                if (param.getResult() == null) {
                    try {
                        // WifiInfo() 构造器非 public，用反射创建空对象
                        Constructor<WifiInfo> ctor = WifiInfo.class.getDeclaredConstructor();
                        ctor.setAccessible(true);
                        param.setResult(ctor.newInstance());
                    } catch (Throwable t) {
                        XposedBridge.log("[WifiSpoof] 创建 WifiInfo 失败: " + t);
                    }
                }
            }
        });

        XposedHelpers.findAndHookMethod(WifiManager.class, "isWifiEnabled", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) {
                if (Prefs.enabled()) {
                    param.setResult(true);
                }
            }
        });

        XposedHelpers.findAndHookMethod(WifiManager.class, "getWifiState", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) {
                if (Prefs.enabled()) {
                    param.setResult(WifiManager.WIFI_STATE_ENABLED);
                }
            }
        });
    }

    /** 方法不存在时（旧 Android 版本）静默跳过，等价于 findAndHookMethodIfExists。 */
    private static void hookIfExists(ClassLoader cl, String method, XC_MethodHook callback) {
        try {
            XposedHelpers.findAndHookMethod("android.net.wifi.WifiInfo", cl, method, callback);
        } catch (Throwable ignored) {
            // 旧系统无此方法，忽略
        }
    }
}
