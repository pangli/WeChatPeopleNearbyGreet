package com.tencent.test;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Toast;

import static com.tencent.test.AutoService.TAG;

public class MainActivity extends AppCompatActivity implements CompoundButton.OnCheckedChangeListener {
    private CheckBox cb_assist;
    private CheckBox cb_window;
    private CheckBox cb_people_nearby;
    private EditText edit_content;
    private Button btn_start_weChat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        cb_assist = (CheckBox) findViewById(R.id.cb_assist_permission);
        if (cb_assist != null) {
            cb_assist.setOnCheckedChangeListener(this);
        }
        cb_window = (CheckBox) findViewById(R.id.cb_show_window);
        if (cb_window != null) {
            cb_window.setOnCheckedChangeListener(this);
        }
        cb_people_nearby = (CheckBox) findViewById(R.id.cb_people_nearby);
        if (cb_people_nearby != null) {
            cb_people_nearby.setOnCheckedChangeListener(this);
        }
        edit_content = (EditText) findViewById(R.id.edit_content);
        btn_start_weChat = (Button) findViewById(R.id.btn_start_weChat);
        if (btn_start_weChat != null) {
            btn_start_weChat.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    openApp();
                }
            });
        }

    }

    /**
     * 跳转微信主界面
     */
    public void openApp() {
        try {
            PackageManager packageManager = getPackageManager();
            Intent intent = new Intent();
            intent = packageManager.getLaunchIntentForPackage("com.tencent.mm");
            startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
            Intent viewIntent = new Intent("android.intent.action.VIEW", Uri.parse("http://weixin.qq.com/"));
            startActivity(viewIntent);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateCheckBox(cb_assist, isAccessibilitySettingsOn());
        updateCheckBox(cb_window, canShowWindow(this));
        if (canShowWindow(this)) {
            requestFloatWindowPermissionIfNeeded();
        }
    }

    /**
     * 申请辅助功能权限
     */
    private void requestAssistPermission() {
        try {
            //打开系统设置中辅助功能
            Intent intent = new Intent(android.provider.Settings.ACTION_ACCESSIBILITY_SETTINGS);
            startActivity(intent);
            Toast.makeText(MainActivity.this, "找到微信附近人，然后开启服务即可", Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 申请悬浮窗权限
     */
    private void requestFloatWindowPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {
            new AlertDialog.Builder(this)
                    .setMessage(R.string.dialog_enable_overlay_window_msg)
                    .setPositiveButton(R.string.dialog_enable_overlay_window_positive_btn
                            , new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
                                    intent.setData(Uri.parse("package:" + getPackageName()));
                                    startActivity(intent);
                                    dialog.dismiss();
                                }
                            })
                    .setNegativeButton(android.R.string.cancel
                            , new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    setShowWindow(MainActivity.this, false);
                                    updateCheckBox(cb_window, false);
                                }
                            })
                    .setOnCancelListener(new DialogInterface.OnCancelListener() {
                        @Override
                        public void onCancel(DialogInterface dialog) {
                            setShowWindow(MainActivity.this, false);
                            updateCheckBox(cb_window, false);
                        }
                    })
                    .create()
                    .show();

        }
    }

/*    private MoveTextView floatBtn1;
    private MoveTextView floatBtn2;
    private WindowManager wm;

    //创建悬浮按钮
    private void createFloatView() {
        WindowManager.LayoutParams pl = new WindowManager.LayoutParams();
        wm = (WindowManager) getSystemService(getApplication().WINDOW_SERVICE);
        pl.type = WindowManager.LayoutParams.TYPE_TOAST;//修改为此TYPE_TOAST，可以不用申请悬浮窗权限就能创建悬浮窗,但在部分手机上会崩溃
        pl.format = PixelFormat.RGBA_8888;
        pl.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        pl.gravity = Gravity.END | Gravity.BOTTOM;
        pl.x = 0;
        pl.y = 0;

        pl.width = WindowManager.LayoutParams.WRAP_CONTENT;
        pl.height = WindowManager.LayoutParams.WRAP_CONTENT;

        LayoutInflater inflater = LayoutInflater.from(this);
        floatBtn1 = (MoveTextView) inflater.inflate(R.layout.floatbtn, null);
        floatBtn1.setText("打招呼");
        floatBtn2 = (MoveTextView) inflater.inflate(R.layout.floatbtn, null);
        floatBtn2.setText("抢红包");
        wm.addView(floatBtn1, pl);
        pl.gravity = Gravity.BOTTOM | Gravity.START;
        wm.addView(floatBtn2, pl);

        floatBtn1.setOnClickListener(this);
        floatBtn2.setOnClickListener(this);
        floatBtn1.setWm(wm, pl);
        floatBtn2.setWm(wm, pl);
    }*/

    /**
     * 检测辅助功能是否开启
     */
    private boolean isAccessibilitySettingsOn() {
        int accessibilityEnabled = 0;
        String service = getPackageName() + "/" + AutoService.class.getCanonicalName();
        try {
            accessibilityEnabled = Settings.Secure.getInt(getApplicationContext().getContentResolver(),
                    android.provider.Settings.Secure.ACCESSIBILITY_ENABLED);
        } catch (Settings.SettingNotFoundException e) {
            Log.d(TAG, "Error finding setting, default accessibility to not found: " + e.getMessage());
        }
        TextUtils.SimpleStringSplitter mStringColonSplitter = new TextUtils.SimpleStringSplitter(':');

        if (accessibilityEnabled == 1) {
            String settingValue = Settings.Secure.getString(getApplicationContext().getContentResolver(),
                    Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES);
            if (settingValue != null) {
                mStringColonSplitter.setString(settingValue);
                while (mStringColonSplitter.hasNext()) {
                    String accessibilityService = mStringColonSplitter.next();

                    Log.d(TAG, "-------------- > accessibilityService :: " + accessibilityService + " " + service);
                    if (accessibilityService.equalsIgnoreCase(service)) {
                        Log.d(TAG, "We've found the correct setting - accessibility is switched on!");
                        return true;
                    }
                }
            }
        } else {
            Log.d(TAG, "***ACCESSIBILITY IS DISABLED***");
        }
        return false;
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        switch (buttonView.getId()) {
            case R.id.cb_assist_permission:
                if (isChecked && !isAccessibilitySettingsOn()) {
                    requestAssistPermission();
                }
                break;
            case R.id.cb_show_window:
                setShowWindow(this, isChecked);
                if (isChecked) {
                    requestFloatWindowPermissionIfNeeded();
                }
                if (!isChecked) {
                    TasksWindow.dismiss();
                } else {
                    TasksWindow.show(this, getPackageName() + "\n" + getClass().getName());
                }
                break;
            case R.id.cb_people_nearby:
                String content = edit_content.getText().toString();
                if (!TextUtils.isEmpty(content)) {
                    AutoService.hello = content;
                    if (isChecked) {
                        if (isAccessibilitySettingsOn()) {
                            AutoService.enableFunc = true;
                        } else {
                            Toast.makeText(MainActivity.this, "辅助功能未开启", Toast.LENGTH_SHORT).show();
                            buttonView.setChecked(false);
                        }
                    } else {
                        AutoService.enableFunc = false;
                    }
                } else {
                    Toast.makeText(MainActivity.this, "请先输入打招呼内容", Toast.LENGTH_SHORT).show();
                    buttonView.setChecked(false);
                }

                break;
        }
    }

    private void updateCheckBox(CheckBox box, boolean isChecked) {
        if (box != null) {
            box.setChecked(isChecked);
        }
    }

    public static boolean canShowWindow(Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getBoolean("show_window", true);
    }

    public static void setShowWindow(Context context, boolean isShow) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        sp.edit().putBoolean("show_window", isShow).apply();
    }
}
