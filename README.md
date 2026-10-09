# WiFi 伪装（WifiSpoof）

基于 **LSPosed** 的通用 Xposed 模块：让 LSPosed 作用域内的目标应用把当前蜂窝数据识别为 **WiFi**。**不限定应用，作用域勾选谁就对谁生效**（例如飞书 `com.ss.android.lark`）。模块包名：`com.zhangzc.wifispoof`。

> ⚠️ 使用须知：本模块仅建议用于**个人设备上的自主场景**（如流量包有限时下载大文件）。绕过公司 WiFi 限制可能违反企业安全策略或服务条款，由此产生的风险自行承担。本项目仅供学习 Android Hook 技术。

---

## 原理

模块注入目标应用进程后，Hook 以下标准 Android API（注入范围完全由 **LSPosed 作用域**控制，未被勾选的应用不受影响）：

| Hook 目标 | 说明 |
| --- | --- |
| `ConnectivityManager.getActiveNetworkInfo()` 等返回的 `NetworkInfo` | `getType()` → `TYPE_WIFI`、`getState()` → `CONNECTED`、`isConnected()` → `true` |
| `ConnectivityManager.isActiveNetworkMetered()` | 返回 `false`（非计量网络） |
| `NetworkCapabilities.hasTransport()` / `getTransportTypes()` | `TRANSPORT_WIFI` 为真、`TRANSPORT_CELLULAR` 为假 |
| `NetworkCapabilities.getLinkDownstream/UpstreamBandwidthKbps()` | 返回固定带宽值 |
| `WifiManager.getConnectionInfo()` 返回的 `WifiInfo` | `getSSID()`、`getBSSID()`、`getRssi()`、`getLinkSpeed()`、`getIpAddress()` 等返回固定 WiFi 详情 |
| `WifiManager.isWifiEnabled()` / `getWifiState()` | 返回已启用 |

配置通过 `XSharedPreferences` 跨进程读取（LSPosed 原生支持）。可配置项：**启用开关 + SSID/BSSID/IP/网络ID**；信号强度、链路速率、带宽为内置固定值。注入范围完全由 LSPosed 作用域决定，模块不做任何包名过滤。

---

## 环境要求

- 已 Root（Magisk）且安装 **LSPosed**（Zygisk 版或 Riru 版均可），Android 8.0+（LSPosed 支持范围）
- 可选替代：LSPatch（免 Root 免模块管理器，直接把模块打进目标应用 APK），但不保证所有机型可用
- 开发机：Android Studio（较新版本）+ JDK 17（本仓库已配置 AGP 8.5.2 / compileSdk 34）

---

## 构建

本仓库已在 Windows 本机完成环境安装与编译验证（Android SDK 位于 `C:\Android\Sdk`，Gradle 8.10.2 位于 `C:\Gradle`，JDK 17）。用 Android Studio 打开工程即可：

1. Android Studio → `File → Open` 选择 `FeishuWifiSpoof` 目录
2. 等待 Gradle Sync 完成（Xposed API 依赖为仓库内本地 jar `app/libs/api-82.jar`，无需联网拉取）
3. `Build → Build APK(s)`（或命令行 `gradle assembleDebug`）
4. 产物：`app/build/outputs/apk/debug/app-debug.apk`（当前已编译生成）

> 注：`de.robv.android.xposed:api:82` 原托管于已关闭的 JCenter，故改用本地 jar（源自阿里云 JCenter 镜像，已校验含 `XposedHelpers`/`XSharedPreferences` 等类）；后续在任何平台编译均使用该本地 jar 即可。

---

## 安装与启用

> ⚠️ 重要：模块「总开关」默认关闭，且配置需由模块 App 写入后 Hook 进程才能读到。**安装后必须先打开「WiFi伪装」App 开启开关（开关改动自动保存）**，否则即使勾了作用域也不会生效。

1. 安装 APK（包名 `com.zhangzc.wifispoof`）到已装 LSPosed 的手机
2. **先打开「WiFi伪装」App → 开启"启用模拟"开关**（可选设置 SSID/BSSID/IP/网络ID 后点"保存配置"）
3. 打开 **LSPosed 管理器** → 模块 → 勾选「WiFi伪装」
4. 在 **作用域** 中勾选需要生效的应用（如飞书 `com.ss.android.lark`；多进程应用全部勾上）。⚠️ 不要勾选「系统框架」或无关系统应用，以免影响系统稳定性
5. 重启手机（或强停目标应用后重开）

> 参考：同类型开源实现（Guise Reborn 等）的网络 Hook 点与本模块一致（`NetworkInfo.getType/getTypeName/getSubtype`、`NetworkCapabilities.hasTransport`、`WifiInfo.getSSID`、`WifiManager.isWifiEnabled` 等），同样要求"先写入配置，再重启目标应用"。

---

## 隐藏桌面图标

设置页内可开启「隐藏桌面图标」：仅禁用桌面入口（`MainActivity`），**配置界面（`SettingsActivity`）声明了 `de.robv.android.xposed.category.MODULE_SETTINGS`**（Xposed/LSPosed 官方的模块设置 category，参考 CustoMIUIzer），因此隐藏图标后**仍可在 LSPosed 管理器 → 模块 → 打开模块设置**进入配置页，互不影响。恢复图标：设置 → 应用 → WiFi伪装 → 启用，或 `adb shell pm enable com.zhangzc.wifispoof/.ui.MainActivity`。

---

## 验证与排障

- **确认模块已注入**：`adb logcat -s LSPosed-Bridge` 搜索 `WifiSpoof`，应看到 `注入进程 ... enabled=true`
  - 若 `enabled=false`：配置未跨进程读到——确认已打开模块 App 并开启开关，再强停目标应用；**若使用的是旧包名/旧版 APK，请升级到当前版本**（当前 Manifest 已声明 `xposedminversion=93` + `xposedsharedprefs=true`，这是 Android 7+ 上 XSharedPreferences 生效的前提）
  - 若**没有任何 `WifiSpoof` 日志**：模块未被 LSPosed 加载——确认模块已勾选、作用域已勾选目标应用，并已重启系统或强停目标应用
- **确认 Hook 生效**：在目标应用内观察网络状态或下载提示（例如飞书上传大文件时是否出现 WiFi 提示）
- **修改不生效**：Hook 每次查询都会重读配置；若目标应用缓存了网络状态，请「强制停止」后重开
- **目标应用更新后失效**：应用若改用私有网络检测类（而非标准 API），需要在 `hooks/` 下新增对应 Hook。可通过 `adb logcat` 观察应用调用的网络类定位（标准 API 已覆盖绝大多数场景）

---

## 已知限制

- 只伪造**应用层看到**的网络类型；真实的蜂窝流量计费、网速、IP 归属不受影响
- 不伪造 `TelephonyManager`（蜂窝数据开关状态）与 VPN 检测
- 部分依赖 `NetworkCallback` 回调的应用可能缓存旧状态，需重启应用

## 目录结构

```
FeishuWifiSpoof/
├── app/
│   ├── libs/api-82.jar              # Xposed API 本地依赖（JCenter 关闭替代方案）
│   └── src/main/
│       ├── assets/xposed_init       # Xposed 入口声明（com.zhangzc.wifispoof.XposedInit）
│       ├── java/com/zhangzc/wifispoof/
│       │   ├── XposedInit.java      # 模块入口（通用，按 LSPosed 作用域，无包名过滤）
│       │   ├── Prefs.java           # 跨进程配置读取
│       │   ├── hooks/
│       │   │   ├── ConnectivitySpoofer.java         # NetworkInfo / ConnectivityManager
│       │   │   ├── NetworkCapabilitiesSpoofer.java  # 新版 API 传输类型
│       │   │   └── WifiInfoSpoofer.java             # WifiInfo / WifiManager
│       │   └── ui/
│       │       ├── MainActivity.java    # 桌面入口（可被隐藏，跳转设置页）
│       │       └── SettingsActivity.java # 配置界面（无桌面图标，可 adb 直达）
│       └── res/                     # 布局 / 字符串 / 图标
├── local.properties                 # sdk.dir=C:\Android\Sdk（本机已生成）
└── README.md
```
