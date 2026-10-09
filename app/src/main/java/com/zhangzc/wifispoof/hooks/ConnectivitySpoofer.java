package com.zhangzc.wifispoof.hooks;

import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.zhangzc.wifispoof.Prefs;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;

/**
 * Hook ConnectivityManager / NetworkInfo：
 * 让飞书在询问网络类型时拿到「WIFI / 已连接 / 非计量」的结果。
 * 模块只注入飞书进程，因此这些 hook 只在飞书内生效，不影响其他应用。
 */
public final class ConnectivitySpoofer {

    private ConnectivitySpoofer() {
    }

    public static void hook(ClassLoader cl) {
        // ---- NetworkInfo 实例方法（进程内所有 NetworkInfo 对象统一生效）----
        XposedHelpers.findAndHookMethod(NetworkInfo.class, "getType", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) {
                if (Prefs.enabled()) {
                    param.setResult(ConnectivityManager.TYPE_WIFI);
                }
            }
        });

        XposedHelpers.findAndHookMethod(NetworkInfo.class, "getTypeName", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) {
                if (Prefs.enabled()) {
                    param.setResult("WIFI");
                }
            }
        });

        XposedHelpers.findAndHookMethod(NetworkInfo.class, "getSubtype", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) {
                if (Prefs.enabled()) {
                    param.setResult(0);
                }
            }
        });

        XposedHelpers.findAndHookMethod(NetworkInfo.class, "getSubtypeName", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) {
                if (Prefs.enabled()) {
                    param.setResult("");
                }
            }
        });

        XposedHelpers.findAndHookMethod(NetworkInfo.class, "getState", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) {
                if (Prefs.enabled()) {
                    param.setResult(NetworkInfo.State.CONNECTED);
                }
            }
        });

        XposedHelpers.findAndHookMethod(NetworkInfo.class, "getDetailedState", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) {
                if (Prefs.enabled()) {
                    param.setResult(NetworkInfo.DetailedState.CONNECTED);
                }
            }
        });

        XposedHelpers.findAndHookMethod(NetworkInfo.class, "isConnected", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) {
                if (Prefs.enabled()) {
                    param.setResult(true);
                }
            }
        });

        XposedHelpers.findAndHookMethod(NetworkInfo.class, "isConnectedOrConnecting", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) {
                if (Prefs.enabled()) {
                    param.setResult(true);
                }
            }
        });

        XposedHelpers.findAndHookMethod(NetworkInfo.class, "isAvailable", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) {
                if (Prefs.enabled()) {
                    param.setResult(true);
                }
            }
        });

        XposedHelpers.findAndHookMethod(NetworkInfo.class, "getExtraInfo", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) {
                if (Prefs.enabled()) {
                    param.setResult(Prefs.ssid());
                }
            }
        });

        // ---- ConnectivityManager 级判断 ----
        // 非计量网络：WiFi 场景一般 unmetered，飞书可能据此决定是否提示流量消耗
        XposedHelpers.findAndHookMethod(ConnectivityManager.class, "isActiveNetworkMetered",
                new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) {
                        if (Prefs.enabled()) {
                            param.setResult(false);
                        }
                    }
                });

        // 后台流量不受限制
        XposedHelpers.findAndHookMethod(ConnectivityManager.class, "getRestrictBackgroundStatus",
                new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) {
                        if (Prefs.enabled()) {
                            param.setResult(ConnectivityManager.RESTRICT_BACKGROUND_STATUS_DISABLED);
                        }
                    }
                });
    }
}
