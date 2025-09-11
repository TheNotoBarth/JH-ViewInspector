# JH-ViewInspector

<p align="center">
  <a href="README.md">中文</a> | <strong>English</strong>
</p>

A simple Android app for retrieving control IDs from any Android application. Uses accessibility services + floating windows to capture the control hierarchy structure and detailed information of any page, with filtering capabilities.

## ✨ Features

- 🔍 **Control Information Viewing**: Get detailed information of all controls on the current page through floating windows
- 🏗️ **Hierarchy Structure**: Display control hierarchy relationships in a tree structure
- 🔎 **Smart Filtering**: Find the controls you need through text filtering + property filtering
- 🌈 **Visual Identification**: Different properties are marked with different colors for easy identification

## 🚀 Quick Start

### Requirements

- Android 7.0 (API 24) and above
- Enable accessibility service permissions
- Floating window permissions (for floating window functionality)

### Installation

1. Download the latest APK file
2. Install the APK file

### Usage

1. **Enable Accessibility Service**:
   - Open system settings → Accessibility → View Inspector
   - Enable service permissions

2. **Enable Floating Window Service**:
   - Click the "Start Floating Window" button
   - Grant floating window permissions

3. **Start Using**:
   - Switch to another app interface, click the "Refresh" button on the floating window
   - Return to the app to view control information

## 📋 Control Information Description

Each control displays the following information:

- **Class Name**: The Java class name of the control
- **Text**: The text content displayed by the control
- **Description**: The content description of the control
- **ID**: The resource ID of the control
- **Property Status**:
  - Whether clickable
  - Whether available
  - Whether focusable
- **Position**: The coordinate range of the control on the screen

## 🛠️ Development

### Tech Stack

- **Language**: Java
- **Minimum API**: 24 (Android 7.0)
- **Target API**: 34 (Android 14)
- **Build Tool**: Gradle
- **UI Framework**: Material Design Components

### Local Development

1. Clone the project
```bash
git clone https://github.com/TheNotoBarth/JH-ViewInspector.git
cd JH-ViewInspector
```

2. Open the project with Android Studio
3. Connect a device or start an emulator
4. Click the run button

## 🤝 Contributing

Issues and Pull Requests are welcome!

## 📄 License

This project is open source under the [MIT License](LICENSE).

## 📞 Contact

For questions or suggestions, please contact:

- Submit an [Issue](https://github.com/TheNotoBarth/JH-ViewInspector/issues)
- Send email: 1591943735@qq.com

---

<div align="center">
  <p><b>JH-ViewInspector</b> - Making Android control debugging easier</p>
</div>