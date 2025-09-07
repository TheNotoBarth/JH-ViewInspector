package com.example.viewinspector;

public class ViewInfo {
    public int depth;
    public String className;
    public String text;
    public String contentDescription;
    public String viewId;
    public boolean isClickable;
    public boolean isEnabled;
    public boolean isFocusable;
    public boolean isFocused;
    public String bounds;
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < depth; i++) {
            sb.append("  ");
        }
        
        sb.append(className);
        if (text != null) {
            sb.append(" - text: \"").append(text).append("\"");
        }
        if (contentDescription != null) {
            sb.append(" - desc: \"").append(contentDescription).append("\"");
        }
        if (viewId != null) {
            sb.append(" - id: ").append(viewId);
        }
        sb.append(" - clickable: ").append(isClickable);
        sb.append(" - enabled: ").append(isEnabled);
        sb.append(" - focusable: ").append(isFocusable);
        sb.append(" - focused: ").append(isFocused);
        sb.append(" - bounds: ").append(bounds);
        
        return sb.toString();
    }
}