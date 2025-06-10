package com.sana.circleup.drawingboard_chatgroup;



import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import androidx.annotation.Nullable;


import java.util.ArrayList;
import java.util.List;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff; // For blending modes if needed for advanced erase
import android.graphics.PorterDuffXfermode; // For blending modes
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;
// No need for Collections.sort here, sorting is done in Activity when receiving

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

//




import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import androidx.annotation.Nullable;

import android.view.ScaleGestureDetector; // Import for ScaleGestureDetector
import android.view.GestureDetector; // Import for GestureDetector
import android.view.GestureDetector.OnGestureListener; // Import interface
import android.view.ScaleGestureDetector.OnScaleGestureListener; // Import interface

import java.util.ArrayList;
import java.util.List;
import java.lang.Math; // For Math.max/min


// This view handles drawing, pan/zoom state, and reporting strokes
// It receives pan/zoom updates from the Activity via setters
public class DrawingView extends View { // <<< Removed implements OnGestureListener, OnScaleGestureListener

    private static final String TAG = "DrawingView";

    // --- Drawing Data ---
    private List<Stroke> completedStrokes;
    private Stroke currentStroke;

    // --- Drawing State & Tools ---
    private Paint drawingPaint;
    private Path drawingPath;

    private Paint erasePaint;
    private Path erasePath;

    private int currentColor = Color.BLACK;
    private float currentStrokeWidth = 12f;

    private int currentTool = TOOL_PEN;
    public static final int TOOL_PEN = 0;
    public static final int TOOL_ERASER = 1;

    // Listener for stroke completion
    private OnStrokeCompleteListener strokeCompleteListener;

    // --- Scale and Translation Factors (Managed by Activity, Applied by DrawingView) ---
    // These track the current zoom level and pan position relative to the original content (0,0 at top-left, scale 1.0f)
    private float mScaleFactor = 1.0f; // Current zoom level (1.0f = no zoom)
    private float mPosX = 0.0f;      // Current pan position X offset
    private float mPosY = 0.0f;      // Current pan position Y offset

    // Bounds for scale factor (optional, prevents zooming too far in/out) - Made public static final
    public static final float MIN_SCALE_FACTOR = 0.2f; // Minimum zoom out (20%)
    public static final float MAX_SCALE_FACTOR = 5.0f; // Maximum zoom in (500%)


    // --- Constructors ---
    public DrawingView(Context context) {
        super(context);
        init(context); // Pass context to init
    }

    public DrawingView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context); // Pass context to init
    }

    public DrawingView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context); // Pass context to init
    }

    // --- init() method ---
    private void init(Context context) {
        Log.d(TAG, "DrawingView initialized.");
        completedStrokes = new ArrayList<>();
        drawingPath = new Path();
        erasePath = new Path();

        drawingPaint = new Paint();
        drawingPaint.setAntiAlias(true); drawingPaint.setDither(true); drawingPaint.setStyle(Paint.Style.STROKE);
        drawingPaint.setStrokeJoin(Paint.Join.ROUND); drawingPaint.setStrokeCap(Paint.Cap.ROUND);
        drawingPaint.setColor(currentColor); drawingPaint.setStrokeWidth(currentStrokeWidth); drawingPaint.setXfermode(null);

        erasePaint = new Paint();
        erasePaint.setAntiAlias(true); erasePaint.setDither(true); erasePaint.setStyle(Paint.Style.STROKE);
        erasePaint.setStrokeJoin(Paint.Join.ROUND); erasePaint.setStrokeCap(Paint.Cap.ROUND);
        erasePaint.setColor(Color.WHITE); // <<< IMPORTANT: MATCH YOUR BACKGROUND
        erasePaint.setStrokeWidth(currentStrokeWidth * 1.5f);
        erasePaint.setXfermode(null);

        // --- Removed Gesture Detector Initialization ---
        // mScaleDetector = new ScaleGestureDetector(context, this); // Removed
        // mGestureDetector = new GestureDetector(context, this); // Removed

        Log.d(TAG, "DrawingView initialized.");
    }

    // --- Methods called by the Activity ---
    public void setOnStrokeCompleteListener(OnStrokeCompleteListener listener) { this.strokeCompleteListener = listener; Log.d(TAG, "OnStrokeCompleteListener set."); }
    public void setCurrentColor(int color) { this.currentColor = color; setTool(TOOL_PEN); Log.d(TAG, "Current color set to: " + String.format("#%06X", (0xFFFFFF & color))); }
    public void setCurrentStrokeWidth(float width) { this.currentStrokeWidth = width; drawingPaint.setStrokeWidth(this.currentStrokeWidth); erasePaint.setStrokeWidth(this.currentStrokeWidth * 1.5f); Log.d(TAG, "Current stroke width set to: " + width); }
    public void setTool(int toolType) { this.currentTool = toolType; String toolName = (toolType == TOOL_PEN) ? "PEN" : (toolType == TOOL_ERASER) ? "ERASER" : "Unknown"; Log.d(TAG, "Current tool set to: " + toolName); }
    public int getCurrentTool() { return currentTool; }

    public void addCompletedStroke(Stroke stroke) { if (stroke != null && stroke.getPoints() != null && !stroke.getPoints().isEmpty()) { completedStrokes.add(stroke); Log.d(TAG, "Added received stroke. Total strokes: " + completedStrokes.size()); invalidate(); } else { Log.w(TAG, "Attempted to add null or empty completed stroke."); } }
    public void addCompletedStrokes(List<Stroke> strokes) { if (strokes != null && !strokes.isEmpty()) { completedStrokes.addAll(strokes); Log.d(TAG, "Added initial strokes (" + strokes.size() + "). Total strokes: " + completedStrokes.size()); invalidate(); } else { Log.d(TAG, "addCompletedStrokes called with null or empty list."); } }
    public void clearDrawing() { completedStrokes.clear(); drawingPath.reset(); erasePath.reset(); Log.d(TAG, "Drawing cleared locally."); invalidate(); }
    public void resetView() { currentStroke = null; drawingPath.reset(); erasePath.reset(); invalidate(); Log.d(TAG, "DrawingView reset."); }


    // --- NEW: Setters and Getters for Scale and Pan (Called by Activity) ---
    public float getScaleFactor() { return mScaleFactor; }
    public float getPosX() { return mPosX; }
    public float getPosY() { return mPosY; }

    // Activity calls this to update zoom level
    public void setScaleFactor(float scaleFactor) {
        this.mScaleFactor = scaleFactor;
        // Optional: Constrain scale factor here or rely on Activity
        this.mScaleFactor = Math.max(MIN_SCALE_FACTOR, Math.min(this.mScaleFactor, MAX_SCALE_FACTOR));
        Log.d(TAG, "ScaleFactor set to: " + this.mScaleFactor);
        invalidate(); // Request redraw when state changes
    }

    // Activity calls this to update pan position
    public void setPan(float posX, float posY) {
        this.mPosX = posX;
        this.mPosY = posY;
        // Optional: Constrain pan position here if needed
        Log.d(TAG, "Pan position set to: (" + this.mPosX + ", " + this.mPosY + ")");
        invalidate(); // Request redraw when state changes
    }


    // --- onDraw(Canvas canvas) method ---
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // --- Apply Scale and Translation to the Canvas ---
        // This transforms the entire drawing space based on pan and zoom factors.
        // All subsequent drawing commands will be affected by these transformations.
        canvas.save(); // Save the current canvas state (before transformations)
        canvas.translate(mPosX, mPosY); // Apply pan (translation) offset
        canvas.scale(mScaleFactor, mScaleFactor); // Apply zoom (scaling) factor

        // --- 1. Draw all COMPLETED strokes ---
        if (completedStrokes != null) {
            for (Stroke stroke : completedStrokes) {
                Path strokePath = new Path();
                List<Point> points = stroke.getPoints();

                if (points != null && !points.isEmpty()) {
                    strokePath.moveTo(points.get(0).getX(), points.get(0).getY());
                    for (int i = 1; i < points.size(); i++) {
                        strokePath.lineTo(points.get(i).getX(), points.get(i).getY());
                    }

                    // --- Choose the right paint and settings based on stroke's stored tool type ---
                    // Stroke width needs to be inverse-scaled so it looks the same regardless of zoom level
                    float drawnStrokeWidth = stroke.getStrokeWidth() / mScaleFactor;

                    if (stroke.getToolType() == TOOL_PEN) {
                        drawingPaint.setColor(stroke.getColor());
                        drawingPaint.setStrokeWidth(drawnStrokeWidth);
                        drawingPaint.setXfermode(null);
                        canvas.drawPath(strokePath, drawingPaint);
                    } else if (stroke.getToolType() == TOOL_ERASER) {
                        erasePaint.setStrokeWidth(drawnStrokeWidth);
                        erasePaint.setColor(Color.WHITE); // Ensure white for simple erase
                        erasePaint.setXfermode(null);
                        canvas.drawPath(strokePath, erasePaint);
                    } else { // Unknown tool
                        drawingPaint.setColor(Color.BLACK); drawingPaint.setStrokeWidth(drawnStrokeWidth); drawingPaint.setXfermode(null); canvas.drawPath(strokePath, drawingPaint);
                    }
                }
            }
        }

        // --- 2. Draw the CURRENT stroke ---
        // Note: The current stroke's path needs to be built using TRANSFORMED coordinates
        // from onTouchEvent, and drawn with paint widths adjusted by mScaleFactor.
        if (currentStroke != null && !currentStroke.getPoints().isEmpty()) {
            if (currentTool == TOOL_PEN) {
                drawingPaint.setColor(currentColor); // Use current selected color
                drawingPaint.setStrokeWidth(currentStrokeWidth / mScaleFactor); // <<< Adjust current stroke width by scale
                drawingPaint.setXfermode(null);
                canvas.drawPath(drawingPath, drawingPaint); // Use path built with transformed coords
            } else if (currentTool == TOOL_ERASER) {
                erasePaint.setStrokeWidth(currentStrokeWidth * 1.5f / mScaleFactor); // <<< Adjust current stroke width by scale
                erasePaint.setColor(Color.WHITE); // Ensure erase paint is WHITE
                erasePaint.setXfermode(null);
                canvas.drawPath(erasePath, erasePaint); // Use path built with transformed coords
            }
        }


        canvas.restore(); // Restore the canvas state (remove pan/zoom transformations)

        // Note: Any UI elements drawn AFTER canvas.restore() will NOT be affected by pan/zoom
        // (e.g., if you drew cursors here in view coordinates).
    }


    // --- onTouchEvent method --- Handle Drawing & Coordinate Transformation ---
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // --- IMPORTANT: Check if input is enabled ---
        if (!isEnabled()) { return false; }

        // --- NEW: Get raw touch coordinates (view pixels) ---
        float rawTouchX = event.getX();
        float rawTouchY = event.getY();

        // --- NEW: Transform raw touch coordinates to drawing coordinates ---
        // Formula: drawing_coord = (view_coord - pan_offset) / scale_factor
        // This is the location *within* the drawing where the user is touching
        float transformedX = (rawTouchX - mPosX) / mScaleFactor;
        float transformedY = (rawTouchY - mPosY) / mScaleFactor;


        // --- Handle Drawing Strokes (Only if 1 pointer and drawing tool) ---
        boolean handledByDrawing = false;
        boolean isDrawingTool = (currentTool == TOOL_PEN || currentTool == TOOL_ERASER);

        // If 1 finger is down and tool is PEN/ERASER, process as drawing.
        // If multiple fingers or other action, process differently or ignore.
        if (event.getPointerCount() == 1 && isDrawingTool) {
            switch (event.getActionMasked()) { // Use getActionMasked for multi-touch compatibility
                case MotionEvent.ACTION_DOWN: // First finger down
                    // Request parent to not intercept for drawing
                    if (getParent() != null) getParent().requestDisallowInterceptTouchEvent(true);

                    // Start a new stroke - Use TRANSFORMED coordinates
                    currentStroke = new Stroke(new ArrayList<>(), currentColor, currentStrokeWidth, null, 0, currentTool); // Capture tool
                    currentStroke.addPoint(new Point(transformedX, transformedY)); // Use transformed coords

                    // Reset path using TRANSFORMED coordinates
                    if (currentTool == TOOL_PEN) { drawingPath.reset(); drawingPath.moveTo(transformedX, transformedY); }
                    else if (currentTool == TOOL_ERASER) { erasePath.reset(); erasePath.moveTo(transformedX, transformedY); }
                    handledByDrawing = true;
                    break;

                case MotionEvent.ACTION_MOVE: // Finger moved
                    if (currentStroke != null) {
                        currentStroke.addPoint(new Point(transformedX, transformedY)); // Use transformed coords
                        if (currentTool == TOOL_PEN) drawingPath.lineTo(transformedX, transformedY); // Use transformed coords
                        else if (currentTool == TOOL_ERASER) erasePath.lineTo(transformedX, transformedY); // Use transformed coords
                        handledByDrawing = true;
                    }
                    break;

                case MotionEvent.ACTION_UP: // Last finger up (and only one down)
                    if (getParent() != null) getParent().requestDisallowInterceptTouchEvent(false);
                    if (currentStroke != null && !currentStroke.getPoints().isEmpty()) {
                        currentStroke.addPoint(new Point(transformedX, transformedY)); // Use transformed coords
                        if (currentTool == TOOL_PEN) drawingPath.lineTo(transformedX, transformedY); // Use transformed coords
                        else if (currentTool == TOOL_ERASER) erasePath.lineTo(transformedX, transformedY); // Use transformed coords

                        if (strokeCompleteListener != null) { strokeCompleteListener.onStrokeComplete(currentStroke); }
                        else { Log.w(TAG, "ACTION_UP (Draw): Listener is null."); }

                        if (currentTool == TOOL_PEN) drawingPath.reset();
                        else if (currentTool == TOOL_ERASER) erasePath.reset();
                        currentStroke = null;
                        handledByDrawing = true;
                    }
                    break;

                case MotionEvent.ACTION_CANCEL:
                    if (getParent() != null) getParent().requestDisallowInterceptTouchEvent(false);
                    if (currentTool == TOOL_PEN) drawingPath.reset();
                    else if (currentTool == TOOL_ERASER) erasePath.reset();
                    currentStroke = null;
                    handledByDrawing = true;
                    break;
                // Ignore other masked actions like POINTER_DOWN/UP for simple 1-touch drawing
            }
        } else {
            // If it's NOT a drawing tool or MULTIPLE pointers, don't process as drawing stroke.
            // Ensure requestDisallowInterceptTouchEvent is released if a multi-touch gesture or cancellation happens unexpectedly.
            int action = event.getActionMasked();
            if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_CANCEL || action == MotionEvent.ACTION_POINTER_UP) {
                if (getParent() != null) getParent().requestDisallowInterceptTouchEvent(false);
            }
            // Note: Since Pan/Zoom are button driven, other multi-touch gestures are currently ignored by this view's onTouchEvent.
            // If you added gesture detectors for pan/zoom *on this view*, they would handle these events here.
        }


        // Request redraw if drawing was handled
        if (handledByDrawing) {
            invalidate();
            return true; // Consume the event if handled as drawing
        }

        // If not handled as drawing, let the superclass handle it.
        // For button-based pan/zoom, touch events on the drawing area are ONLY for drawing.
        // Pan/Zoom is triggered by BUTTON CLICKS. So, if it wasn't drawing, we can return false.
        return false; // Event was not handled by drawing logic
    }


    // --- Implement OnGestureListener Methods --- (Return false as pan/zoom are button driven)
    // These methods are only relevant if GestureDetectors are attached and handling events.
    // Since pan/zoom are button-driven now, these should return false.
     public boolean onDown(MotionEvent e) { return false; }
     public void onShowPress(MotionEvent e) { }
    public boolean onSingleTapUp(MotionEvent e) { return false; }
     public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) { return false; }
    public void onLongPress(MotionEvent e) { }
     public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) { return false; }

    // --- Implement OnScaleGestureListener Methods --- (Return false as pan/zoom are button driven)
     public boolean onScaleBegin(ScaleGestureDetector detector) { return false; }
    public boolean onScale(ScaleGestureDetector detector) { return false; }
     public void onScaleEnd(ScaleGestureDetector detector) { }


    // Interface to communicate back to the Activity when a stroke is completed
    public interface OnStrokeCompleteListener {
        void onStrokeComplete(Stroke completedStroke);
    }

    // --- onSizeChanged method --- Handle view size changes
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        // Called when the size of the view changes (e.g., on initial layout, orientation change)

        // Optional: Initialize pan/zoom position on initial layout, if it's at default 0,0
        // This will make the top-left of the drawing align with the top-left of the view initially.
        // No code needed here if mPosX and mPosY default to 0.
    }

}