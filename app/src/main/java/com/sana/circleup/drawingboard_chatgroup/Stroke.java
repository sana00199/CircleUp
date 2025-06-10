package com.sana.circleup.drawingboard_chatgroup;



import java.util.List;
import java.util.ArrayList; // Important: Use concrete List types for Firebase/Room compatibility

// Room database requires public constructors and getters/setters
// Firebase requires a default public constructor for deserialization



import java.util.ArrayList;
import java.util.List;
import androidx.annotation.Nullable; // Make sure this is imported

public class Stroke {
    private List<Point> points;
    private int color; // Stored color (pen color, or white for simple erase)
    private float strokeWidth; // Stored width
    private String userId;
    private long timestamp;
    private int toolType; // <<< NEW field: Stores TOOL_PEN or TOOL_ERASER

    // Default constructor required for Firebase deserialization
    public Stroke() {
        points = new ArrayList<>(); // Initialize list in default constructor
    }

    // <<< UPDATE Constructor to include toolType >>>
    public Stroke(List<Point> points, int color, float strokeWidth, String userId, long timestamp, int toolType) {
        this.points = points != null ? points : new ArrayList<>(); // Defensive check
        this.color = color;
        this.strokeWidth = strokeWidth;
        this.userId = userId;
        this.timestamp = timestamp;
        this.toolType = toolType; // Initialize new field
    }

    public <E> Stroke(ArrayList<E> es, int currentColor, float currentStrokeWidth, Object o, int i) {
        this.points = points != null ? points : new ArrayList<>(); // Defensive check
        this.color = color;
        this.strokeWidth = strokeWidth;
        this.userId = userId;
        this.timestamp = timestamp;
    }

    // Getters (Required for Firebase serialization and accessing data)
    public List<Point> getPoints() { return points; }
    public int getColor() { return color; }
    public float getStrokeWidth() { return strokeWidth; }
    public String getUserId() { return userId; }
    public long getTimestamp() { return timestamp; }
    public int getToolType() { return toolType; } // <<< NEW getter

    // Setters (Good practice, needed by Firebase if no full constructor or using default)
    public void setPoints(List<Point> points) { this.points = points; }
    public void setColor(int color) { this.color = color; }
    public void setStrokeWidth(float strokeWidth) { this.strokeWidth = strokeWidth; }
    public void setUserId(String userId) { this.userId = userId; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
    public void setToolType(int toolType) { this.toolType = toolType; } // <<< NEW setter

    // Optional: Add point method
    public void addPoint(Point point) {
        if (this.points == null) this.points = new ArrayList<>();
        this.points.add(point);
    }
}