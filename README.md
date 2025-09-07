# JH-ViewInspector

一个用于获取任意Android应用控件id的简易APP，通过无障碍服务+悬浮窗获取任意页面的控件层级结构和详细信息，支持筛选功能。

## ✨ 功能特性

- 🔍 **控件信息查看**: 通过悬浮窗获取当前页面所有控件的详细信息
- 🏗️ **层级结构**: 以树形结构展示控件层级关系
- 🔎 **智能筛选**: 通过文本筛选+属性筛选来找到你需要的控件
- 🌈 **可视化标识**: 不同属性用不同颜色标识，便于识别

## 🚀 快速开始

### 环境要求

- Android 7.0 (API 24) 及以上
- 启用无障碍服务权限
- 悬浮窗权限（用于悬浮窗功能）

### 安装

1. 下载最新版本的APK文件
2. 安装APK文件

### 使用方法

1. **启用无障碍服务**:
   - 打开系统设置 → 无障碍 → View Inspector
   - 启用服务权限

2. **启用悬浮窗服务**:
   - 点击"启动悬浮窗"按钮
   - 授权悬浮窗权限

3. **开始使用**:
   - 切换至其他应用界面，点击悬浮窗“刷新”按钮
   - 回到APP查看控件信息

## 📋 控件信息说明

每个控件显示以下信息：

- **类名**: 控件的Java类名
- **文本**: 控件显示的文本内容
- **描述**: 控件的内容描述
- **ID**: 控件的资源ID
- **属性状态**:
  - 是否可点击
  - 是否可用
  - 是否可聚焦
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
git clone https://github.com/TheNotoBarth/JH-ViewInspector.git
cd JH-ViewInspector
```

2. 使用Android Studio打开项目
3. 连接设备或启动模拟器
4. 点击运行按钮

## 🤝 贡献

欢迎提交Issue和Pull Request！

## 📄 许可证

本项目采用 [MIT许可证](LICENSE) 开源。

## 📞 联系

如有问题或建议，请通过以下方式联系：

- 提交 [Issue](https://github.com/TheNotoBarth/JH-ViewInspector/issues)
- 发送邮件: 1591943735@qq.com

---

<div align="center">
  <p><b>JH-ViewInspector</b> - 让Android控件调试更简单</p>
</div>