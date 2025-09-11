package com.example.viewinspector;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.content.SharedPreferences;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.IBinder;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.example.viewinspector.R;

import java.util.List;

public class ViewInspectorFloatingService extends Service {
    
    private static final String TAG = "ViewInspectorFloating";
    private static final String CHANNEL_ID = "ViewInspectorChannel";
    
    private WindowManager windowManager;
    private View floatingView;
    private WindowManager.LayoutParams params;
    private TextView infoTextView;
    private Button refreshButton;
    private Button closeButton;
    
    private int initialX, initialY;
    private float initialTouchX, initialTouchY;

    @Override
    public void onCreate() {
        super.onCreate();
        
        // 设置服务语言
        setServiceLanguage();
        
        startForegroundService();
        initFloatingWindow();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (floatingView != null && windowManager != null) {
            windowManager.removeView(floatingView);
        }
    }

    private void setServiceLanguage() {
        // 读取应用的语言设置
        SharedPreferences prefs = getSharedPreferences("AppSettings", MODE_PRIVATE);
        String languageCode = prefs.getString("app_language", "zh"); // 默认中文
        
        // 设置服务语言
        Resources resources = getResources();
        Configuration config = resources.getConfiguration();
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            config.setLocale(new java.util.Locale(languageCode));
        } else {
            config.locale = new java.util.Locale(languageCode);
        }
        
        resources.updateConfiguration(config, resources.getDisplayMetrics());
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void initFloatingWindow() {
        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        
        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        floatingView = inflater.inflate(R.layout.layout_floating_window, null);

        params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.O ?
                        WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY :
                        WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT);

        params.gravity = Gravity.TOP | Gravity.START;
        params.x = 100;
        params.y = 300;

        windowManager.addView(floatingView, params);

        infoTextView = floatingView.findViewById(R.id.info_text);
        refreshButton = floatingView.findViewById(R.id.refresh_button);
        closeButton = floatingView.findViewById(R.id.close_button);

        refreshButton.setOnClickListener(v -> refreshViewInfo());
        closeButton.setOnClickListener(v -> stopSelf());

        floatingView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        initialX = params.x;
                        initialY = params.y;
                        initialTouchX = event.getRawX();
                        initialTouchY = event.getRawY();
                        return true;
                    case MotionEvent.ACTION_MOVE:
                        params.x = initialX + (int) (event.getRawX() - initialTouchX);
                        params.y = initialY + (int) (event.getRawY() - initialTouchY);
                        windowManager.updateViewLayout(floatingView, params);
                        return true;
                }
                return false;
            }
        });

        refreshViewInfo();
    }

    private void refreshViewInfo() {
        ViewInspectorAccessibilityService service = ViewInspectorAccessibilityService.getInstance();
        if (service != null) {
            List<ViewInfo> viewInfos = service.getCurrentWindowViewInfos();
            
            // 统计可点击控件数量
            int clickableCount = 0;
            for (ViewInfo info : viewInfos) {
                if (info.isClickable) {
                    clickableCount++;
                }
            }
            
            StringBuilder sb = new StringBuilder();
            sb.append(getString(R.string.total_controls_count, viewInfos.size()));
            sb.append(" ");
            sb.append(getString(R.string.clickable_controls_count, clickableCount));
            sb.append("\n");
            sb.append(getString(R.string.return_to_app_for_details));
            
            infoTextView.setText(sb.toString());
            
            // 发送广播通知主页面刷新
            Intent refreshIntent = new Intent("com.example.viewinspector.REFRESH_VIEW_INFO");
            sendBroadcast(refreshIntent);
            
        } else {
            infoTextView.setText(R.string.accessibility_service_not_running_floating);
            Toast.makeText(this, R.string.accessibility_service_required, Toast.LENGTH_SHORT).show();
        }
    }

    private void startForegroundService() {
        createNotificationChannel();
        
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, 
                notificationIntent, PendingIntent.FLAG_IMMUTABLE);

        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle(getString(R.string.notification_title))
                .setContentText(getString(R.string.notification_text))
                .setSmallIcon(android.R.drawable.ic_menu_info_details)
                .setContentIntent(pendingIntent)
                .build();

        startForeground(1, notification);
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    CHANNEL_ID,
                    getString(R.string.notification_title),
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(serviceChannel);
        }
    }
}