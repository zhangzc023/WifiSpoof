package com.zhangzc.wifispoof;

import com.zhangzc.wifispoof.hooks.ConnectivitySpoofer;
import com.zhangzc.wifispoof.hooks.NetworkCapabilitiesSpoofer;
import com.zhangzc.wifispoof.hooks.WifiInfoSpoofer;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

/**
 * LSPosed 模块入口（通用工具）。
 * 注入范围完全由 LSPosed「作用域」决定，不做任何包名过滤。
 */
public class XposedInit implements IXposedHookLoadPackage {

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
        Prefs.init();

        XposedBridge.log("[WifiSpoof] 注入进程: " + lpparam.packageName
                + ", processName=" + lpparam.processName + ", enabled=" + Prefs.enabled());
        if (!Prefs.enabled()) {
            XposedBridge.log("[WifiSpoof] 提示：当前未启用。请打开「WiFi伪装」App 开启开关后强制停止并重启目标应用。");
        }

        ClassLoader cl = lpparam.classLoader;
        ConnectivitySpoofer.hook(cl);
        NetworkCapabilitiesSpoofer.hook(cl);
        WifiInfoSpoofer.hook(cl);
    }
}
