package com.example.viewinspector;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.util.Log;
import android.view.accessibility.AccessibilityNodeInfo;

import java.util.ArrayList;
import java.util.List;

public class ViewInspectorAccessibilityService extends AccessibilityService {
    
    private static final String TAG = "ViewInspectorService";
    private static ViewInspectorAccessibilityService instance;
    
    public static ViewInspectorAccessibilityService getInstance() {
        return instance;
    }
    
    public static boolean isServiceEnabled(android.content.Context context) {
        android.content.pm.PackageManager packageManager = context.getPackageManager();
        android.content.ComponentName componentName = new android.content.ComponentName(context, ViewInspectorAccessibilityService.class);
        int state = packageManager.getComponentEnabledSetting(componentName);
        
        // 检查服务是否实际运行
        android.app.ActivityManager manager = (android.app.ActivityManager) context.getSystemService(android.content.Context.ACTIVITY_SERVICE);
        for (android.app.ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (ViewInspectorAccessibilityService.class.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        
        // 备用方法：检查无障碍服务状态
        android.content.ContentResolver contentResolver = context.getContentResolver();
        String enabledServices = android.provider.Settings.Secure.getString(contentResolver, 
            android.provider.Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES);
        
        return enabledServices != null && enabledServices.contains(context.getPackageName() + "/" + ViewInspectorAccessibilityService.class.getName());
    }
    
    @Override
    public void onServiceConnected() {
        super.onServiceConnected();
        instance = this;
        
        AccessibilityServiceInfo info = new AccessibilityServiceInfo();
        info.eventTypes = android.view.accessibility.AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED | 
                         android.view.accessibility.AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED;
        info.feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC;
        info.flags = AccessibilityServiceInfo.FLAG_REPORT_VIEW_IDS |
                    AccessibilityServiceInfo.FLAG_RETRIEVE_INTERACTIVE_WINDOWS;
        
        setServiceInfo(info);
        Log.d(TAG, "无障碍服务已连接");
    }
    
    @Override
    public void onAccessibilityEvent(android.view.accessibility.AccessibilityEvent event) {
        // 处理窗口变化事件
    }
    
    @Override
    public void onInterrupt() {
        Log.d(TAG, "无障碍服务被中断");
    }
    
    @Override
    public void onDestroy() {
        super.onDestroy();
        instance = null;
        Log.d(TAG, "无障碍服务已销毁");
    }
    
    public List<ViewInfo> getCurrentWindowViewInfos() {
        List<ViewInfo> viewInfos = new ArrayList<>();
        AccessibilityNodeInfo rootNode = getRootInActiveWindow();
        
        if (rootNode != null) {
            traverseNode(rootNode, 0, viewInfos);
            rootNode.recycle();
        }
        
        return viewInfos;
    }
    
    private void traverseNode(AccessibilityNodeInfo node, int depth, List<ViewInfo> viewInfos) {
        if (node == null) return;
        
        ViewInfo info = new ViewInfo();
        info.depth = depth;
        info.className = node.getClassName() != null ? node.getClassName().toString() : "Unknown";
        info.text = node.getText() != null ? node.getText().toString() : null;
        info.contentDescription = node.getContentDescription() != null ? 
                                  node.getContentDescription().toString() : null;
        info.viewId = node.getViewIdResourceName();
        info.isClickable = node.isClickable();
        info.isEnabled = node.isEnabled();
        info.isFocusable = node.isFocusable();
        info.isFocused = node.isFocused();
        info.bounds = getNodeBounds(node);
        
        viewInfos.add(info);
        
        // 遍历子节点
        for (int i = 0; i < node.getChildCount(); i++) {
            AccessibilityNodeInfo child = node.getChild(i);
            if (child != null) {
                traverseNode(child, depth + 1, viewInfos);
                child.recycle();
            }
        }
    }
    
    private String getNodeBounds(AccessibilityNodeInfo node) {
        android.graphics.Rect bounds = new android.graphics.Rect();
        node.getBoundsInScreen(bounds);
        return String.format("[%d,%d][%d,%d]", 
                           bounds.left, bounds.top, bounds.right, bounds.bottom);
    }
    
    public void printCurrentWindowViews() {
        List<ViewInfo> viewInfos = getCurrentWindowViewInfos();
        Log.d(TAG, "=== 当前窗口控件信息 ===");
        Log.d(TAG, "控件总数: " + viewInfos.size());
        
        for (ViewInfo info : viewInfos) {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < info.depth; i++) {
                sb.append("  ");
            }
            
            sb.append(info.className);
            if (info.text != null) {
                sb.append(" - text: \"").append(info.text).append("\"");
            }
            if (info.contentDescription != null) {
                sb.append(" - desc: \"").append(info.contentDescription).append("\"");
            }
            if (info.viewId != null) {
                sb.append(" - id: ").append(info.viewId);
            }
            sb.append(" - clickable: ").append(info.isClickable);
            sb.append(" - bounds: ").append(info.bounds);
            
            Log.d(TAG, sb.toString());
        }
    }
}