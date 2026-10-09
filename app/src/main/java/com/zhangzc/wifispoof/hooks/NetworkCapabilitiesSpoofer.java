package com.zhangzc.wifispoof.hooks;

import android.net.NetworkCapabilities;

import com.zhangzc.wifispoof.Prefs;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;

/**
 * Hook NetworkCapabilities：
 * 新版 API 中应用通过 hasTransport(TRANSPORT_WIFI) 判断当前网络类型，
 * 这里把 WIFI 传输识别为真、CELLULAR 识别为假，并伪造链路带宽。
 */
public final class NetworkCapabilitiesSpoofer {

    private NetworkCapabilitiesSpoofer() {
    }

    public static void hook(ClassLoader cl) {
        XposedHelpers.findAndHookMethod(NetworkCapabilities.class, "hasTransport", int.class,
                new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) {
                        if (!Prefs.enabled()) {
                            return;
                        }
                        int transport = (Integer) param.args[0];
                        if (transport == NetworkCapabilities.TRANSPORT_WIFI) {
                            param.setResult(true);
                        } else if (transport == NetworkCapabilities.TRANSPORT_CELLULAR) {
                            param.setResult(false);
                        }
                    }
                });

        XposedHelpers.findAndHookMethod(NetworkCapabilities.class, "getTransportTypes",
                new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) {
                        if (Prefs.enabled()) {
                            param.setResult(new int[]{NetworkCapabilities.TRANSPORT_WIFI});
                        }
                    }
                });

        // 非计量 / 非漫游能力
        XposedHelpers.findAndHookMethod(NetworkCapabilities.class, "hasCapability", int.class,
                new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) {
                        if (!Prefs.enabled()) {
                            return;
                        }
                        int cap = (Integer) param.args[0];
                        if (cap == NetworkCapabilities.NET_CAPABILITY_NOT_METERED
                                || cap == NetworkCapabilities.NET_CAPABILITY_NOT_ROAMING) {
                            param.setResult(true);
                        }
                    }
                });

        XposedHelpers.findAndHookMethod(NetworkCapabilities.class, "getLinkDownstreamBandwidthKbps",
                new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) {
                        if (Prefs.enabled()) {
                            param.setResult(Prefs.downlinkKbps());
                        }
                    }
                });

        XposedHelpers.findAndHookMethod(NetworkCapabilities.class, "getLinkUpstreamBandwidthKbps",
                new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) {
                        if (Prefs.enabled()) {
                            param.setResult(Prefs.uplinkKbps());
                        }
                    }
                });
    }
}
