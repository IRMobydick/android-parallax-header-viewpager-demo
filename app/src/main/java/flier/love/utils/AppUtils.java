package flier.love.utils;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.app.ActivityManager.RunningTaskInfo;
import android.content.ComponentName;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Build;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * AppUtils
 */
public class AppUtils {

    private static List<String> safePackagenNameList = new ArrayList<>();

    private AppUtils() {
        throw new AssertionError();
    }

    public static PackageInfo getPackageInfo(Context ctx) {
        PackageInfo info = null;
        try {
            info = ctx.getPackageManager().getPackageInfo(ctx.getPackageName(), 0);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace(System.err);
        }
        if (info == null) info = new PackageInfo();
        return info;
    }

    /**
     * whether this process is named with processName
     *
     * @param context
     * @param processName
     * @return <ul>
     * return whether this process is named with processName
     * <li>if context is null, return false</li>
     * <li>if {@link ActivityManager#getRunningAppProcesses()} is null, return false</li>
     * <li>if one process of {@link ActivityManager#getRunningAppProcesses()} is equal to processName, return
     * true, otherwise return false</li>
     * </ul>
     */
    public static boolean isNamedProcess(Context context, String processName) {
        if (context == null) {
            return false;
        }

        int pid = android.os.Process.myPid();
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<RunningAppProcessInfo> processInfoList = manager.getRunningAppProcesses();
        if (processInfoList == null) {
            return true;
        }

        for (RunningAppProcessInfo processInfo : manager.getRunningAppProcesses()) {
            if (processInfo.pid == pid && processInfo.processName.equals(processName)) {
                return true;
            }
        }
        return false;
    }

    /**
     * whether application is in background
     * <ul>
     * <li>need use permission android.permission.GET_TASKS in Manifest.xml</li>
     * </ul>
     *
     * @param context
     * @return if application is in background return true, otherwise return false
     */
    public static boolean isApplicationInBackground(Context context) {
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<RunningTaskInfo> taskList = am.getRunningTasks(1);
        if (taskList != null && !taskList.isEmpty()) {
            ComponentName topActivity = taskList.get(0).topActivity;
            if (topActivity != null && !topActivity.getPackageName().equals(context.getPackageName())) {
                return true;
            }
        }
        return false;
    }


    /**
     * 修改状态栏为全透明
     *
     * @param window
     */
    public static void transparencyBar(Window window) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            if (MIUISetStatusBarLightMode(window, true)) {
                window.setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            } else if (FlymeSetStatusBarLightMode(window, true)) {
                window.setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS | WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
                window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
                window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
                window.setStatusBarColor(Color.TRANSPARENT);
            }
        }
    }

    public static boolean supportStatusBarLightMode(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            Window window = ((Activity) context).getWindow();
            if (AppUtils.MIUISetStatusBarLightMode(window, true)
                    || AppUtils.FlymeSetStatusBarLightMode(window, true)
                    || Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                return true;
            }
        }
        return false;
    }

    public static boolean supportStatusBarLightMode(Window window) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            if (AppUtils.MIUISetStatusBarLightMode(window, true)
                    || AppUtils.FlymeSetStatusBarLightMode(window, true)
                    || Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                return true;
            }
        }
        return false;
    }

    /**
     * 设置状态栏黑色字体图标，
     * 适配4.4以上版本MIUIV、Flyme和6.0以上版本其他Android
     *
     * @param window
     * @return 1:MIUUI 2:Flyme 3:android6.0
     */
    public static int StatusBarLightMode(Window window) {
        int result = 0;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            if (MIUISetStatusBarLightMode(window, true)) {
                result = 1;
            } else if (FlymeSetStatusBarLightMode(window, true)) {
                result = 2;
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
                result = 3;
            }
        }
        return result;
    }

    public static int StatusBarDarkMode(Window window) {
        int result = 0;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            if (MIUISetStatusBarLightMode(window, false)) {
                result = 1;
            } else if (FlymeSetStatusBarLightMode(window, false)) {
                result = 2;
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
                result = 3;
            }
        }
        return result;
    }

    /**
     * 已知系统类型时，设置状态栏黑色字体图标。
     * 适配4.4以上版本MIUIV、Flyme和6.0以上版本其他Android
     *
     * @param window
     * @param type   1:MIUUI 2:Flyme 3:android6.0
     */
    public static void StatusBarLightMode(Window window, int type) {
        if (type == 1) {
            MIUISetStatusBarLightMode(window, true);
        } else if (type == 2) {
            FlymeSetStatusBarLightMode(window, true);
        } else if (type == 3) {
            window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }

    }

    /**
     * 清除MIUI或flyme或6.0以上版本状态栏黑色字体
     */
    public static void StatusBarDarkMode(Window window, int type) {
        if (type == 1) {
            MIUISetStatusBarLightMode(window, false);
        } else if (type == 2) {
            FlymeSetStatusBarLightMode(window, false);
        } else if (type == 3) {
            window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
        }

    }


    /**
     * 设置状态栏图标为深色和魅族特定的文字风格
     * 可以用来判断是否为Flyme用户
     *
     * @param window 需要设置的窗口
     * @param dark   是否把状态栏字体及图标颜色设置为深色
     * @return boolean 成功执行返回true
     */
    public static boolean FlymeSetStatusBarLightMode(Window window, boolean dark) {
        boolean result = false;
        if (window != null) {
            try {
                WindowManager.LayoutParams lp = window.getAttributes();
                Field darkFlag = WindowManager.LayoutParams.class
                        .getDeclaredField("MEIZU_FLAG_DARK_STATUS_BAR_ICON");
                Field meizuFlags = WindowManager.LayoutParams.class
                        .getDeclaredField("meizuFlags");
                darkFlag.setAccessible(true);
                meizuFlags.setAccessible(true);
                int bit = darkFlag.getInt(null);
                int value = meizuFlags.getInt(lp);
                if (dark) {
                    value |= bit;
                } else {
                    value &= ~bit;
                }
                meizuFlags.setInt(lp, value);
                window.setAttributes(lp);
                result = true;
            } catch (Exception e) {
            }
        }
        return result;
    }

    /**
     * 设置状态栏字体图标为深色，需要MIUIV6以上
     *
     * @param window 需要设置的窗口
     * @param dark   是否把状态栏字体及图标颜色设置为深色
     * @return boolean 成功执行返回true
     */
    public static boolean MIUISetStatusBarLightMode(Window window, boolean dark) {
        boolean result = false;
        if (window != null) {
            Class clazz = window.getClass();
            try {
                int darkModeFlag = 0;
                Class layoutParams = Class.forName("android.view.MiuiWindowManager$LayoutParams");
                Field field = layoutParams.getField("EXTRA_FLAG_STATUS_BAR_DARK_MODE");
                darkModeFlag = field.getInt(layoutParams);
                Method extraFlagField = clazz.getMethod("setExtraFlags", int.class, int.class);
                if (dark) {
                    extraFlagField.invoke(window, darkModeFlag, darkModeFlag);//状态栏透明且黑色字体
                } else {
                    extraFlagField.invoke(window, 0, darkModeFlag);//清除黑色字体
                }
                result = true;
            } catch (Exception e) {
            }
        }
        return result;
    }

    /**
     * 获取手机最顶层activity 类名字
     *
     * @param context
     * @return
     */
    public static String getTopActivity(Context context) {
        ActivityManager manager = (ActivityManager) context.getSystemService(Activity.ACTIVITY_SERVICE);
        List<RunningTaskInfo> runningTaskInfos = manager.getRunningTasks(1);
        if (runningTaskInfos != null)
            return runningTaskInfos.get(0).topActivity.getClassName();
        else
            return null;
    }

    public static int getStatusBarHeight(Context context) {
        int resourceId = context.getResources().getIdentifier("status_bar_height", "dimen", "android");
        return context.getResources().getDimensionPixelSize(resourceId);
    }

    public static int getNavigationBarHeight(Context context) {
        int resourceId = context.getResources().getIdentifier("navigation_bar_height", "dimen", "android");
        return context.getResources().getDimensionPixelSize(resourceId);
    }

    public static boolean checkDeviceHasNavigationBar(Context context) {
        boolean hasNavigationBar = false;
        Resources rs = context.getResources();
        int id = rs.getIdentifier("config_showNavigationBar", "bool", "android");
        if (id > 0) {
            hasNavigationBar = rs.getBoolean(id);
        }
        try {
            Class systemPropertiesClass = Class.forName("android.os.SystemProperties");
            Method m = systemPropertiesClass.getMethod("get", String.class);
            String navBarOverride = (String) m.invoke(systemPropertiesClass, "qemu.hw.mainkeys");
            if ("1".equals(navBarOverride)) {
                hasNavigationBar = false;
            } else if ("0".equals(navBarOverride)) {
                hasNavigationBar = true;
            }
        } catch (Exception e) {

        }
        return hasNavigationBar;
    }

    /**
     * 校验当前Activity是否安全
     *
     * @param context
     * @return
     */
    public static boolean checkActivitySafty(Context context) {
        boolean safe = false;
        if (safePackagenNameList != null && safePackagenNameList.size() > 0){
            // 查询所有已经安装的应用程序
            PackageManager manager = context.getPackageManager();
            List<ApplicationInfo> applicationInfoList = manager.getInstalledApplications(PackageManager.GET_UNINSTALLED_PACKAGES);
            // 对应用名进行排序
            Collections.sort(applicationInfoList, new ApplicationInfo.DisplayNameComparator(manager));
            for (ApplicationInfo app : applicationInfoList) {
                if ((app.flags & ApplicationInfo.FLAG_SYSTEM) != 0) {
                    safePackagenNameList.add(app.packageName);
                }
            }
        }
        // 得到所有的系统程序包名放进白名单里面.
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        // 顶层覆盖的应用包名
        String runningActivityPackageName;
        int sdkVersion;
        try {
            sdkVersion = Integer.valueOf(Build.VERSION.SDK);
        } catch (NumberFormatException e) {
            sdkVersion = 0;
        }
        if (sdkVersion >= 21) {
            runningActivityPackageName = getCurrentPkgName(context);
        } else {
            runningActivityPackageName = activityManager.getRunningTasks(1).get(0).topActivity.getPackageName();
        }
        if (runningActivityPackageName != null) {
            if (runningActivityPackageName.equals(context.getPackageName())) {
                safe = true;
            }
            // 白名单比对
            for (String safePack : safePackagenNameList) {
                if (safePack.equals(runningActivityPackageName)) {
                    safe = true;
                }
            }
        }
        return safe;
    }

    /**
     * 获取当前栈顶activity， 5.x
     *
     * @param context
     * @return
     */
    public static String getCurrentPkgName(Context context) {
        final int START_TASK_TO_FRONT = 2;
        Field field = null;
        try {
            field = RunningAppProcessInfo.class.getDeclaredField("processState");//通过反射获取进程状态字段.
        } catch (Exception e) {
            e.printStackTrace();
        }
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<RunningAppProcessInfo> runningAppProcesses = manager.getRunningAppProcesses();
        RunningAppProcessInfo currentInfo = null;
        for (int i = 0; i < runningAppProcesses.size(); i++) {
            RunningAppProcessInfo process = runningAppProcesses.get(i);
            if (process.importance == RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                // 前台运行进程
                Integer state = null;
                try {
                    state = field.getInt(process);//反射调用字段值的方法,获取该进程的状态.
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (state != null && state == START_TASK_TO_FRONT) {
                    currentInfo = process;
                    break;
                }
            }
        }
        String pkgName = null;
        if (currentInfo != null) {
            pkgName = currentInfo.processName;
        }
        return pkgName;
    }


}
