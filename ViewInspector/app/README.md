# View Inspector

一个用于Android应用的控件信息查看工具，通过无障碍服务获取当前页面的控件层级结构，支持悬浮窗实时查看和筛选功能。

## ✨ 功能特性

- 🔍 **控件信息查看**: 获取当前页面所有控件的详细信息
- 🏗️ **层级结构**: 以树形结构展示控件层级关系
- 🔎 **智能筛选**: 支持文本筛选和可点击控件筛选
- 📱 **悬浮窗模式**: 可在其他应用上层显示控件信息
- 🎯 **实时刷新**: 动态获取最新控件信息
- 🌈 **可视化标识**: 不同属性用不同颜色标识，便于识别

## 🚀 快速开始

### 环境要求

- Android 7.0 (API 24) 及以上
- 启用无障碍服务权限
- 悬浮窗权限（用于悬浮窗功能）

### 安装

1. 下载最新版本的APK文件
2. 允许安装来自未知来源的应用
3. 安装APK文件

### 使用方法

1. **启用无障碍服务**:
   - 打开系统设置 → 无障碍 → View Inspector
   - 启用服务权限

2. **基本使用**:
   - 打开应用，点击"刷新控件信息"获取当前页面控件
   - 使用文本输入框筛选特定控件
   - 勾选"仅显示可点击控件"进行筛选

3. **悬浮窗模式**:
   - 点击"启动悬浮窗"按钮
   - 授权悬浮窗权限
   - 在其他应用上层查看控件信息

## 📋 控件信息说明

每个控件显示以下信息：

- **类名**: 控件的Java类名
- **文本**: 控件显示的文本内容
- **描述**: 控件的内容描述
- **ID**: 控件的资源ID
- **属性状态**:
  - 是否可点击（绿色/红色）
  - 是否可用（绿色/红色）
  - 是否可聚焦（绿色/红色）
- **位置**: 控件在屏幕上的坐标范围

## 🛠️ 开发

### 技术栈

- **语言**: Java
- **最低API**: 24 (Android 7.0)
- **目标API**: 34 (Android 14)
- **构建工具**: Gradle
- **UI框架**: Material Design Components

### 本地开发

1. 克隆项目
```bash
git clone [项目地址]
cd viewinspector
```

2. 使用Android Studio打开项目
3. 连接设备或启动模拟器
4. 点击运行按钮

### 构建发布版本

```bash
./gradlew assembleRelease
```

构建后的APK文件位于: `app/release/ViewInspector.apk`

## 📁 项目结构

```
app/
├── src/main/java/com/example/viewinspector/
│   ├── MainActivity.java                 # 主界面
│   ├── ViewInspectorAccessibilityService.java  # 无障碍服务
│   └── ViewInspectorFloatingService.java     # 悬浮窗服务
├── src/main/res/
│   ├── layout/                          # 布局文件
│   ├── drawable/                        # 图标资源
│   └── values/                          # 字符串和样式
└── build.gradle.kts                    # 构建配置
```

## 🔧 权限说明

应用需要以下权限：

- **SYSTEM_ALERT_WINDOW**: 显示悬浮窗
- **FOREGROUND_SERVICE**: 前台服务支持
- **BIND_ACCESSIBILITY_SERVICE**: 无障碍服务绑定

## 🤝 贡献

欢迎提交Issue和Pull Request！

### 开发计划

- [ ] 支持导出控件信息到文件
- [ ] 添加控件点击测试功能
- [ ] 支持保存常用筛选条件
- [ ] 添加暗黑模式支持
- [ ] 优化悬浮窗交互体验

## 📄 许可证

本项目采用 [MIT许可证](LICENSE) 开源。

## 📞 联系

如有问题或建议，请通过以下方式联系：

- 提交 [Issue]([项目Issues地址])
- 发送邮件: [你的邮箱]

## 🙏 致谢

感谢所有为这个项目做出贡献的开发者们。

---

<div align="center">
  <p><b>View Inspector</b> - 让Android控件调试更简单</p>
</div>