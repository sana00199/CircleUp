package com.sana.circleup.drawingboard_chatgroup;



// Room database requires public constructors and getters/setters
// Firebase requires a default public constructor for deserialization

public class Point {
    private float x;
    private float y;
    // Optional: Add timestamp if you need very precise sync ordering, but Firebase push key is often enough
    // private long timestamp;

    // Default constructor required for Firebase deserialization and Room (if used directly)
    public Point() { }

    public Point(float x, float y) {
        this.x = x;
        this.y = y;
        // this.timestamp = System.currentTimeMillis(); // Or pass timestamp
    }

    // Getters (Required for Firebase serialization and Room)
    public float getX() { return x; }
    public float getY() { return y; }
    // public long getTimestamp() { return timestamp; } // Getter for timestamp

    // Setters (Optional for Room if using full constructor, but good for Firebase)
    public void setX(float x) { this.x = x; }
    public void setY(float y) { this.y = y; }
    // public void setTimestamp(long timestamp) { this.timestamp = timestamp; } // Setter for timestamp

    // Optional: Add toString for logging
    @Override
    public String toString() {
        return "Point{" + "x=" + x + ", y=" + y + '}';
    }
}