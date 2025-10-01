package com.example.viewinspector;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
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
    private androidx.appcompat.widget.Toolbar toolbar;
    
    private List<ViewInfo> allViewInfos = new ArrayList<>();
    private String currentLanguage = "zh"; // 默认中文
    
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
        
        // 加载语言设置
        loadLanguageSetting();
        
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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(refreshReceiver, filter, Context.RECEIVER_EXPORTED);
        } else {
            registerReceiver(refreshReceiver, filter);
        }
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        
        if (id == R.id.action_language) {
            String newLanguage = currentLanguage.equals("zh") ? "en" : "zh";
            currentLanguage = newLanguage;
            setAppLanguage(currentLanguage);
            recreate(); // 重新创建活动以应用新语言
            return true;
        }
        
        return super.onOptionsItemSelected(item);
    }
    
    private void showLanguageDialog() {
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this);
        builder.setTitle(R.string.language_settings);
        
        String[] languages = {getString(R.string.language_chinese), getString(R.string.language_english)};
        int checkedItem = currentLanguage.equals("zh") ? 0 : 1;
        
        builder.setSingleChoiceItems(languages, checkedItem, (dialog, which) -> {
            String selectedLanguage = which == 0 ? "zh" : "en";
            if (!selectedLanguage.equals(currentLanguage)) {
                currentLanguage = selectedLanguage;
                setAppLanguage(currentLanguage);
                recreate(); // 重新创建活动以应用新语言
            }
            dialog.dismiss();
        });
        
        builder.show();
    }
    
    private void setAppLanguage(String languageCode) {
        Resources resources = getResources();
        Configuration config = resources.getConfiguration();
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            config.setLocale(new java.util.Locale(languageCode));
        } else {
            config.locale = new java.util.Locale(languageCode);
        }
        
        resources.updateConfiguration(config, resources.getDisplayMetrics());
        
        // 保存语言设置到SharedPreferences
        android.content.SharedPreferences prefs = getSharedPreferences("AppSettings", MODE_PRIVATE);
        android.content.SharedPreferences.Editor editor = prefs.edit();
        editor.putString("app_language", languageCode);
        editor.apply();
    }
    
    private void loadLanguageSetting() {
        android.content.SharedPreferences prefs = getSharedPreferences("AppSettings", MODE_PRIVATE);
        String savedLanguage = prefs.getString("app_language", "zh"); // 默认中文
        
        if (!savedLanguage.equals(currentLanguage)) {
            currentLanguage = savedLanguage;
            setAppLanguage(currentLanguage);
        }
    }
    
    private void initViews() {
        scrollView = findViewById(R.id.scroll_view);
        infoTextView = findViewById(R.id.info_text_view);
        emptyView = findViewById(R.id.empty_view);
        textFilterEditText = findViewById(R.id.text_filter);
        clickableFilterCheckBox = findViewById(R.id.clickable_filter);
        refreshButton = findViewById(R.id.refresh_button);
        startFloatingButton = findViewById(R.id.start_floating_button);
        toolbar = findViewById(R.id.toolbar);
        
        setSupportActionBar(toolbar);
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
            Toast.makeText(this, R.string.accessibility_service_required, Toast.LENGTH_LONG).show();
            Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
            accessibilitySettingsLauncher.launch(intent);
        } else {
            // 提示用户现在需要手动刷新
            Toast.makeText(this, R.string.please_click_refresh, Toast.LENGTH_SHORT).show();
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
            Toast.makeText(this, R.string.accessibility_service_not_running, Toast.LENGTH_SHORT).show();
            scrollView.setVisibility(View.GONE);
            emptyView.setVisibility(View.VISIBLE);
            emptyView.setText(R.string.empty_message);
            return;
        }
        
        ViewInspectorAccessibilityService service = ViewInspectorAccessibilityService.getInstance();
        if (service != null) {
            allViewInfos = service.getCurrentWindowViewInfos();
            applyFilters();
            
            if (allViewInfos.isEmpty()) {
                scrollView.setVisibility(View.GONE);
                emptyView.setVisibility(View.VISIBLE);
                emptyView.setText(R.string.no_control_info_try_again);
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
                        emptyView.setText(R.string.no_control_info_try_again);
                    } else {
                        emptyView.setVisibility(View.GONE);
                        scrollView.setVisibility(View.VISIBLE);
                    }
                } else {
                    Toast.makeText(this, R.string.accessibility_service_connecting, Toast.LENGTH_SHORT).show();
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
            emptyView.setText(R.string.no_control_info_try_again);
            return;
        }
        
        scrollView.setVisibility(View.VISIBLE);
        emptyView.setVisibility(View.GONE);
        
        StringBuilder sb = new StringBuilder();
        sb.append("<b>").append(getString(R.string.total_controls, viewInfos.size())).append("</b><br><br>");
        
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
            
            // 属性状态（绿色/红色）
            sb.append(" <font color='").append(info.isClickable ? "#4CAF50" : "#F44336").append("'>")
              .append(getString(R.string.clickable)).append(":").append(info.isClickable ? getString(R.string.yes) : getString(R.string.no))
              .append("</font>");
            sb.append(" <font color='").append(info.isEnabled ? "#4CAF50" : "#F44336").append("'>")
              .append(getString(R.string.enabled)).append(":").append(info.isEnabled ? getString(R.string.yes) : getString(R.string.no))
              .append("</font>");
            sb.append(" <font color='").append(info.isFocusable ? "#4CAF50" : "#F44336").append("'>")
              .append(getString(R.string.focusable)).append(":").append(info.isFocusable ? getString(R.string.yes) : getString(R.string.no))
              .append("</font>");
            
            // 位置（青色）
            sb.append(" <font color='#0097A7'>").append(info.bounds).append("</font>");
            
            sb.append("<br>");
        }
        
        infoTextView.setText(android.text.Html.fromHtml(sb.toString()));
    }
    
    private void startFloatingService() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && 
            !Settings.canDrawOverlays(this)) {
            Toast.makeText(this, R.string.please_grant_overlay_permission, Toast.LENGTH_SHORT).show();
            return;
        }
        
        if (!ViewInspectorAccessibilityService.isServiceEnabled(this)) {
            Toast.makeText(this, R.string.please_enable_accessibility_first, Toast.LENGTH_SHORT).show();
            return;
        }
        
        Intent serviceIntent = new Intent(this, ViewInspectorFloatingService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(serviceIntent);
        } else {
            startService(serviceIntent);
        }
        
        Toast.makeText(this, R.string.floating_window_service_started, Toast.LENGTH_SHORT).show();
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == OVERLAY_PERMISSION_REQUEST_CODE) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && 
                Settings.canDrawOverlays(this)) {
                Toast.makeText(this, R.string.overlay_permission_granted, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, R.string.overlay_permission_needed, Toast.LENGTH_SHORT).show();
            }
        }
    }
}