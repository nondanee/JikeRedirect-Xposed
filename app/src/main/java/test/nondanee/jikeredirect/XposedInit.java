package test.nondanee.jikeredirect;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import java.net.URLDecoder;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class XposedInit implements IXposedHookLoadPackage {

    final static String TAG = "nondanee.jike.redirect";

    private String getViewName (View view){
        int resourceId = view.getId();
        if (resourceId == View.NO_ID) return "";
        else return view.getContext().getResources().getResourceEntryName(resourceId);
    }

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam loadPackageParam) throws Throwable {
        if (!loadPackageParam.packageName.equals("com.ruguoapp.jike")) return;

        XposedBridge.log("Jike Redirect Start");

        XposedHelpers.findAndHookMethod(
            "com.ruguoapp.jike.network.d",
            loadPackageParam.classLoader,
            "a",
            String.class,
            new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    String host = (String) param.args[0];
                    param.args[0] = host.replace("app.jike.ruguoapp.com", "api.jellow.club");
                    super.beforeHookedMethod(param);
                }
            }
        );

        XposedHelpers.findAndHookMethod(
            "com.ruguoapp.jike.network.d",
            loadPackageParam.classLoader,
            "c",
            String.class,
            new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    String url = (String) param.args[0];
                    param.args[0] = url.replace("jike.ruguoapp.com", "jellow.club");
                    super.beforeHookedMethod(param);
                }
            }
        );

        XposedHelpers.findAndHookMethod(
            "com.ruguoapp.jike.global.h",
            loadPackageParam.classLoader,
            "b",
            String.class,
            new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    String uri = (String) param.args[0];
                    if (!uri.startsWith("jike://page.jk/web?url=")) return;
                    uri = uri.substring(23);
                    uri = URLDecoder.decode(uri, "utf-8");
                    if (!uri.startsWith("https://redirect.jike.ruguoapp.com/?redirect=")) return;
                    param.args[0] = "jike://page.jk/web?url=" + uri.substring(45);
                    super.beforeHookedMethod(param);
                }
            }
        );

        XposedHelpers.findAndHookMethod(
            "com.ruguoapp.jike.global.h",
            loadPackageParam.classLoader,
            "a",
            Context.class,
            Intent.class,
            new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    Intent intent = (Intent) param.args[1];
                    if (intent != null && intent.getComponent().getClassName().equals("com.ruguoapp.jike.business.web.ui.WebActivity")) {
                        if (intent.getExtras().getString("url").equals("http://localhost:48030/files/guide/index.html?displayHeader=false")) {
                            intent.setClassName("com.ruguoapp.jike", "com.ruguoapp.jike.business.main.ui.MainActivity");
                        }
                    }
                    super.beforeHookedMethod(param);
                }
            }
        );

        XposedHelpers.findAndHookMethod(
            View.class,
            "setVisibility",
            int.class,
            new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    View view = (View) param.thisObject;
                    if (getViewName(view).isEmpty() && view instanceof ViewGroup) {
                        for (int index = 0; index < ((ViewGroup)view).getChildCount(); index ++) {
                            View childView = ((ViewGroup)view).getChildAt(index);
                            if (getViewName(childView).equals("layNotificationEntryContainer")) {
                                param.args[0] = View.VISIBLE;
                                break;
                            }
                        }
                    }
                    super.beforeHookedMethod(param);
                }
            }
        );
    }
}
