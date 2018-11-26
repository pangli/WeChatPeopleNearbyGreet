package com.tencent.test;

import android.accessibilityservice.AccessibilityService;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.os.Build;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.Toast;

import java.util.List;
import java.util.Locale;

/**
 * 附近人打招呼
 * 1.使用此服务需要获取手机特殊权限：部分手机点击本demo页面中“打开辅助服务”按钮进入辅助功能页即可找到名称为“微信附近人”的服务，然后打开即可，
 * 其他手机需要在辅助功能中找到“无障碍”项，然后在“无障碍”中找到“微信附近人”打开即可
 * note:APP获取到辅助功能权限后，一旦APP进程被强杀就会清除该权限，再次进入APP又需要重新申请，正常退出则不会
 * Created by zorro
 * WeChatPeopleNearbyGreet
 */
public class AutoService extends AccessibilityService {
    public static final String TAG = "Zorro";
    /**
     * 微信的包名
     */
    static final String WECHAT_PACKAGENAME = "com.tencent.mm";

    /**
     * 向附近的人自动打招呼的内容
     */
    public static String hello = "您好，打扰了";

    public static boolean enableFunc;          //是否开启自动添加附近的人为好友的功能;


    private int i = 0;//记录已打招呼的人数
    private int page = 1;//记录附近的人列表页码,初始页码为1
    private int prepos = -1;//记录页面跳转来源，0--从附近的人页面跳转到详细资料页，1--从打招呼页面跳转到详细资料页

    private TextToSpeech mTts;


    private String currentActivity = "";

    @Override
    public void onAccessibilityEvent(final AccessibilityEvent event) {
        if (event != null) {
            int eventType = event.getEventType();
            currentActivity = String.valueOf(event.getClassName());
            if (eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED && MainActivity.canShowWindow(this)) {
                TasksWindow.show(this, event.getPackageName() + "\n" + currentActivity);
            }
            String str_eventType;
            switch (eventType) {
                case AccessibilityEvent.TYPE_VIEW_CLICKED:
                    Log.e(TAG, "==============Start====================");
                    str_eventType = "TYPE_VIEW_CLICKED";
                    Log.e(TAG, "=============END=====================");
                    break;
                case AccessibilityEvent.TYPE_VIEW_FOCUSED:
                    str_eventType = "TYPE_VIEW_FOCUSED";
                    break;
                case AccessibilityEvent.TYPE_VIEW_LONG_CLICKED:
                    str_eventType = "TYPE_VIEW_LONG_CLICKED";
                    break;
                case AccessibilityEvent.TYPE_VIEW_SELECTED:
                    str_eventType = "TYPE_VIEW_SELECTED";
                    break;
                case AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED:
                    str_eventType = "TYPE_VIEW_TEXT_CHANGED";
                    break;
                case AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED:
                    str_eventType = "TYPE_WINDOW_STATE_CHANGED";
                    break;
                case AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED:
                    str_eventType = "TYPE_NOTIFICATION_STATE_CHANGED";
                    break;
                case AccessibilityEvent.TYPE_TOUCH_EXPLORATION_GESTURE_END:
                    str_eventType = "TYPE_TOUCH_EXPLORATION_GESTURE_END";
                    break;
                case AccessibilityEvent.TYPE_ANNOUNCEMENT:
                    str_eventType = "TYPE_ANNOUNCEMENT";
                    break;
                case AccessibilityEvent.TYPE_TOUCH_EXPLORATION_GESTURE_START:
                    str_eventType = "TYPE_TOUCH_EXPLORATION_GESTURE_START";
                    break;
                case AccessibilityEvent.TYPE_VIEW_HOVER_ENTER:
                    str_eventType = "TYPE_VIEW_HOVER_ENTER";
                    break;
                case AccessibilityEvent.TYPE_VIEW_HOVER_EXIT:
                    str_eventType = "TYPE_VIEW_HOVER_EXIT";
                    break;
                case AccessibilityEvent.TYPE_VIEW_SCROLLED:
                    str_eventType = "TYPE_VIEW_SCROLLED";
                    break;
                case AccessibilityEvent.TYPE_VIEW_TEXT_SELECTION_CHANGED:
                    str_eventType = "TYPE_VIEW_TEXT_SELECTION_CHANGED";
                    break;
                case AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED:
                    str_eventType = "TYPE_WINDOW_CONTENT_CHANGED";
                    break;
                default:
                    str_eventType = String.valueOf(eventType);
            }
            Log.e(TAG, "eventType---" + str_eventType + "----currentActivity---" + currentActivity);
            //自动加人
            if (enableFunc) {
                if (eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED && "com.tencent.mm.ui.LauncherUI"
                        .equals(currentActivity)) {//微信主页
                    //记录打招呼人数置零
                    i = 0;
                    //当前在微信聊天页就点开发现
                    openNext("发现");
                    //然后跳转到附近的人
                    openDelay(1000, "附近的人");
                } else if ("com.tencent.mm.plugin.nearby.ui.NearbyFriendsUI".equals(currentActivity) && eventType ==
                        AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
                    prepos = 0;
                    //当前在附近的人界面就点选人打招呼
                    final AccessibilityNodeInfo nodeInfo = getRootInActiveWindow();
                    if (nodeInfo != null) {
                        final List<AccessibilityNodeInfo> list = nodeInfo.findAccessibilityNodeInfosByText("以内");
                        if (list != null && !list.isEmpty()) {
                            Log.e(TAG, "附近的人列表人数: " + list.size());
                            if (i < (list.size() * page)) {
                                list.get(i % list.size()).performAction(AccessibilityNodeInfo.ACTION_CLICK);
                                list.get(i % list.size()).getParent().performAction(AccessibilityNodeInfo.ACTION_CLICK);
                            } else if (i == list.size() * page) {
                                //本页已全部打招呼，所以下滑列表加载下一页，每次下滑的距离是一屏
                                for (int i = 0; i < nodeInfo.getChild(0).getChildCount(); i++) {
                                    if (nodeInfo.getChild(0).getChild(i).getClassName().equals("android.widget" +
                                            ".ListView")) {
                                        AccessibilityNodeInfo node_lsv = nodeInfo.getChild(0).getChild(i);
                                        node_lsv.performAction(AccessibilityNodeInfo.ACTION_SCROLL_FORWARD);
                                        page++;
                                        new Thread(new Runnable() {
                                            @Override
                                            public void run() {
                                                try {
                                                    Thread.sleep(1000);
                                                } catch (InterruptedException mE) {
                                                    mE.printStackTrace();
                                                }
                                                AccessibilityNodeInfo nodeInfo_ = getRootInActiveWindow();
                                                if (nodeInfo_!=null) {
                                                    List<AccessibilityNodeInfo> list_ = nodeInfo_.findAccessibilityNodeInfosByText("以内");
                                                    if (list_ != null && list_.size() >= 2) {
                                                        Log.e(TAG, "列表人数: " + list_.size());
                                                        //滑动之后，上一页的最后一个item为当前的第一个item，所以从第二个开始打招呼
                                                        list_.get(1).performAction(AccessibilityNodeInfo.ACTION_CLICK);
                                                        list_.get(1).getParent().performAction(AccessibilityNodeInfo.ACTION_CLICK);
                                                    }
                                                }


                                            }
                                        }).start();
                                    }
                                }
                            }
                        }
                    }

                } else if ("com.tencent.mm.plugin.profile.ui.ContactInfoUI".equals(currentActivity) && eventType ==
                        AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
                    if (prepos == 1) {
                        //从打招呼界面跳转来的，则点击返回到附近的人页面
                        performGlobalAction(AccessibilityService.GLOBAL_ACTION_BACK);
                        i++;
                    } else if (prepos == 0) {
                        //从附近的人跳转来的，则点击打招呼按钮
                        AccessibilityNodeInfo nodeInfo = getRootInActiveWindow();
                        if (nodeInfo == null) {
                            Log.d(TAG, "rootWindow为空");
                            return;
                        }
                        List<AccessibilityNodeInfo> list = nodeInfo.findAccessibilityNodeInfosByText("打招呼");
                        if (list.size() > 0) {
                            list.get(list.size() - 1).performAction(AccessibilityNodeInfo.ACTION_CLICK);
                            list.get(list.size() - 1).getParent().performAction(AccessibilityNodeInfo.ACTION_CLICK);
                        } else {
                            //如果遇到已加为好友的则界面的“打招呼”变为“发消息"，所以直接返回上一个界面并记录打招呼人数+1
                            performGlobalAction(AccessibilityService.GLOBAL_ACTION_BACK);
                            i++;
                        }
                    }
                } else if ("com.tencent.mm.ui.contact.SayHiEditUI".equals(currentActivity) && eventType ==
                        AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
                    //当前在打招呼页面
                    prepos = 1;
                    //输入打招呼的内容并发送
                    inputHello(hello);
                    openNext("发送");
                }
            }

            //自动从桌面打开微信，利用微信多开助手可实现多个微信账号之间的切换
//        if(topActivity.equals("com.huawei.android.launcher.Launcher")){
//            openNext(event,"微信");
//            AccessibilityNodeInfo nodeInfo = getRootInActiveWindow();
//            nodeInfo.getChildCount();
//            for (int i=0;i<nodeInfo.getChildCount();i++){
//                String name=nodeInfo.getChild(i).getViewIdResourceName();
//            }
//        }
        }
    }

    /**
     * 点击匹配的nodeInfo
     *
     * @param str text关键字
     */
    private void openNext(String str) {
        AccessibilityNodeInfo nodeInfo = getRootInActiveWindow();
        if (nodeInfo == null) {
            Toast.makeText(this, "rootWindow为空", Toast.LENGTH_SHORT).show();
            return;
        }
        List<AccessibilityNodeInfo> list = nodeInfo.findAccessibilityNodeInfosByText(str);
        if (list != null && list.size() > 0) {
            list.get(list.size() - 1).performAction(AccessibilityNodeInfo.ACTION_CLICK);
            list.get(list.size() - 1).getParent().performAction(AccessibilityNodeInfo.ACTION_CLICK);
        } else {
            Toast.makeText(this, "找不到有效的节点", Toast.LENGTH_SHORT).show();
        }
    }

    //延迟打开界面
    private void openDelay(final int delaytime, final String text) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(delaytime);
                } catch (InterruptedException mE) {
                    mE.printStackTrace();
                }
                openNext(text);
            }
        }).start();
    }

    //自动输入打招呼内容
    private void inputHello(String hello) {
        AccessibilityNodeInfo nodeInfo = getRootInActiveWindow();
        //找到当前获取焦点的view
        AccessibilityNodeInfo target = nodeInfo.findFocus(AccessibilityNodeInfo.FOCUS_INPUT);
        if (target == null) {
            Log.d(TAG, "inputHello: null");
            return;
        }
        ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("label", hello);
        clipboard.setPrimaryClip(clip);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            target.performAction(AccessibilityNodeInfo.ACTION_PASTE);
        }
    }

    @Override
    public void onInterrupt() {
        Toast.makeText(this, "服务已中断", Toast.LENGTH_SHORT).show();
        mTts.shutdown();
    }

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        Toast.makeText(this, "服务已开启", Toast.LENGTH_SHORT).show();
        mTts = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status == TextToSpeech.SUCCESS) {
                    mTts.setLanguage(Locale.CHINESE);
                }
            }
        });
    }

    @Override
    public boolean onUnbind(Intent intent) {
        TasksWindow.dismiss();
        return super.onUnbind(intent);
    }

}
