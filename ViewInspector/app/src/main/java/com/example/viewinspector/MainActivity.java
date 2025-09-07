package com.example.viewinspector;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.example.viewinspector.ViewInfo;
import com.example.viewinspector.ViewInspectorAccessibilityService;
import com.example.viewinspector.R;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    
    private static final int OVERLAY_PERMISSION_REQUEST_CODE = 1001;
    
    private ScrollView scrollView;
    private TextView infoTextView;
    private TextView emptyView;
    private EditText textFilterEditText;
    private CheckBox clickableFilterCheckBox;
    private Button refreshButton;
    private Button startFloatingButton;
    
    private List<ViewInfo> allViewInfos = new ArrayList<>();
    
    private final ActivityResultLauncher<Intent> accessibilitySettingsLauncher = 
        registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            checkAccessibilityService();
        });

    private static MainActivity instance;
    private BroadcastReceiver refreshReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        instance = this;
        setContentView(R.layout.activity_main);
        
        initViews();
        setupListeners();
        checkPermissions();
        
        // 注册全局广播接收器
        refreshReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if ("com.example.viewinspector.REFRESH_VIEW_INFO".equals(intent.getAction())) {
                    if (instance != null) {
                        instance.runOnUiThread(() -> {
                            refreshViewInfo();
                        });
                    }
                }
            }
        };
        
        IntentFilter filter = new IntentFilter("com.example.viewinspector.REFRESH_VIEW_INFO");
        registerReceiver(refreshReceiver, filter);
    }
    
    private void initViews() {
        scrollView = findViewById(R.id.scroll_view);
        infoTextView = findViewById(R.id.info_text_view);
        emptyView = findViewById(R.id.empty_view);
        textFilterEditText = findViewById(R.id.text_filter);
        clickableFilterCheckBox = findViewById(R.id.clickable_filter);
        refreshButton = findViewById(R.id.refresh_button);
        startFloatingButton = findViewById(R.id.start_floating_button);
    }
    
    private void setupListeners() {
        refreshButton.setOnClickListener(v -> refreshViewInfo());
        
        startFloatingButton.setOnClickListener(v -> startFloatingService());
        
        textFilterEditText.addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                applyFilters();
            }
            
            @Override
            public void afterTextChanged(android.text.Editable s) {}
        });
        
        clickableFilterCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            applyFilters();
        });
    }
    
    private void checkPermissions() {
        checkAccessibilityService();
        checkOverlayPermission();
    }
    
    private void checkAccessibilityService() {
        if (!ViewInspectorAccessibilityService.isServiceEnabled(this)) {
            Toast.makeText(this, "请启用无障碍服务", Toast.LENGTH_LONG).show();
            Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
            accessibilitySettingsLauncher.launch(intent);
        } else {
            // 提示用户现在需要手动刷新
            Toast.makeText(this, "请点击刷新按钮或悬浮窗的刷新按钮获取控件信息", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (refreshReceiver != null) {
            unregisterReceiver(refreshReceiver);
        }
        instance = null;
    }
    
    private void checkOverlayPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && 
            !Settings.canDrawOverlays(this)) {
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:" + getPackageName()));
            startActivityForResult(intent, OVERLAY_PERMISSION_REQUEST_CODE);
        }
    }
    
    private void refreshViewInfo() {
        if (!ViewInspectorAccessibilityService.isServiceEnabled(this)) {
            Toast.makeText(this, "无障碍服务未运行，请先在系统设置中启用", Toast.LENGTH_SHORT).show();
            scrollView.setVisibility(View.GONE);
            emptyView.setVisibility(View.VISIBLE);
            emptyView.setText("暂无控件信息\n\n请先启用无障碍服务并点击刷新按钮");
            return;
        }
        
        ViewInspectorAccessibilityService service = ViewInspectorAccessibilityService.getInstance();
        if (service != null) {
            allViewInfos = service.getCurrentWindowViewInfos();
            applyFilters();
            
            if (allViewInfos.isEmpty()) {
                scrollView.setVisibility(View.GONE);
                emptyView.setVisibility(View.VISIBLE);
                emptyView.setText("暂无控件信息\n\n请尝试点击刷新按钮或切换到其他应用");
            } else {
                emptyView.setVisibility(View.GONE);
                scrollView.setVisibility(View.VISIBLE);
            }
        } else {
            // 服务可能刚启动，稍等一下再试
            new android.os.Handler().postDelayed(() -> {
                ViewInspectorAccessibilityService delayedService = ViewInspectorAccessibilityService.getInstance();
                if (delayedService != null) {
                    allViewInfos = delayedService.getCurrentWindowViewInfos();
                    applyFilters();
                    
                    if (allViewInfos.isEmpty()) {
                        scrollView.setVisibility(View.GONE);
                        emptyView.setVisibility(View.VISIBLE);
                        emptyView.setText("暂无控件信息\n\n请尝试点击刷新按钮或切换到其他应用");
                    } else {
                        emptyView.setVisibility(View.GONE);
                        scrollView.setVisibility(View.VISIBLE);
                    }
                } else {
                    Toast.makeText(this, "无障碍服务连接中，请稍后重试", Toast.LENGTH_SHORT).show();
                }
            }, 1000);
        }
    }
    
    private void applyFilters() {
        String textFilter = textFilterEditText.getText().toString().toLowerCase();
        boolean clickableOnly = clickableFilterCheckBox.isChecked();
        
        List<ViewInfo> filteredList = new ArrayList<>();
        
        for (ViewInfo info : allViewInfos) {
            boolean matchesText = textFilter.isEmpty() || 
                                (info.text != null && info.text.toLowerCase().contains(textFilter)) ||
                                (info.contentDescription != null && info.contentDescription.toLowerCase().contains(textFilter));
            
            boolean matchesClickable = !clickableOnly || info.isClickable;
            
            if (matchesText && matchesClickable) {
                filteredList.add(info);
            }
        }
        
        displayViewInfos(filteredList);
    }
    
    private void displayViewInfos(List<ViewInfo> viewInfos) {
        if (viewInfos.isEmpty()) {
            scrollView.setVisibility(View.GONE);
            emptyView.setVisibility(View.VISIBLE);
            emptyView.setText("暂无控件信息\n\n请尝试点击刷新按钮或切换到其他应用");
            return;
        }
        
        scrollView.setVisibility(View.VISIBLE);
        emptyView.setVisibility(View.GONE);
        
        StringBuilder sb = new StringBuilder();
        sb.append("<b>控件总数: ").append(viewInfos.size()).append("</b><br><br>");
        
        for (ViewInfo info : viewInfos) {
            // 构建树形结构缩进，使用HTML的非断空格
            String indent = "";
            for (int i = 0; i < info.depth; i++) {
                indent += "&nbsp;&nbsp;&nbsp;&nbsp;"; // 每个层级4个非断空格
            }
            
            // 添加树形连接线
            if (info.depth > 0) {
                sb.append(indent).append("└─ ");
            } else {
                sb.append(indent);
            }
            
            // 类名（黑色）
            sb.append("<font color='#000000'><b>").append(info.className).append("</b></font>");
            
            // 文本（蓝色）
            if (info.text != null && !info.text.isEmpty()) {
                sb.append(" <font color='#1976D2'>\"").append(info.text).append("\"</font>");
            }
            
            // 描述（紫色）
            if (info.contentDescription != null && !info.contentDescription.isEmpty()) {
                sb.append(" <font color='#7B1FA2'>[").append(info.contentDescription).append("]</font>");
            }
            
            // ID（橙色）
            if (info.viewId != null) {
                sb.append(" <font color='#FF6F00'>#").append(info.viewId).append("</font>");
            }
            
            // 属性状态（绿色/红色）- 使用中文显示
            sb.append(" <font color='").append(info.isClickable ? "#4CAF50" : "#F44336").append("'>点击：").append(info.isClickable ? "是" : "否").append("</font>");
            sb.append(" <font color='").append(info.isEnabled ? "#4CAF50" : "#F44336").append("'>可用：").append(info.isEnabled ? "是" : "否").append("</font>");
            sb.append(" <font color='").append(info.isFocusable ? "#4CAF50" : "#F44336").append("'>聚焦：").append(info.isFocusable ? "是" : "否").append("</font>");
            
            // 位置（青色）
            sb.append(" <font color='#0097A7'>").append(info.bounds).append("</font>");
            
            sb.append("<br>");
        }
        
        infoTextView.setText(android.text.Html.fromHtml(sb.toString()));
    }
    
    private void startFloatingService() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && 
            !Settings.canDrawOverlays(this)) {
            Toast.makeText(this, "请先授予悬浮窗权限", Toast.LENGTH_SHORT).show();
            return;
        }
        
        if (!ViewInspectorAccessibilityService.isServiceEnabled(this)) {
            Toast.makeText(this, "请先启用无障碍服务", Toast.LENGTH_SHORT).show();
            return;
        }
        
        Intent serviceIntent = new Intent(this, ViewInspectorFloatingService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(serviceIntent);
        } else {
            startService(serviceIntent);
        }
        
        Toast.makeText(this, "悬浮窗服务已启动", Toast.LENGTH_SHORT).show();
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == OVERLAY_PERMISSION_REQUEST_CODE) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && 
                Settings.canDrawOverlays(this)) {
                Toast.makeText(this, "悬浮窗权限已授予", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "需要悬浮窗权限才能使用此功能", Toast.LENGTH_SHORT).show();
            }
        }
    }
}