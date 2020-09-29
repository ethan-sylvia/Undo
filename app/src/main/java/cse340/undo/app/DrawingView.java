package cse340.undo.app;


import android.content.Context;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.FrameLayout;

import java.util.HashSet;
import java.util.Set;

import cse340.undo.actions.AbstractAction;
import cse340.undo.actions.AbstractReversibleViewAction;
import cse340.undo.actions.StrokeAction;

/***
 * The canvas on which the drawing takes place. Drawings are made up of
 * strokes, which are handled by the StrokeAction class.
 * This canvas doesn't know anything about undo, and you won't need
 * to modify it to add features, or to add support for undo.
 */
public class DrawingView extends FrameLayout {
    public static final String LOG_TAG = "DrawingView";

    /** State machine enum and field. */
    private enum DrawingModel {
        START, DRAWING
    }

    /** What state the PPS is in. */
    private DrawingModel state;

    /** Drawing fields. */
    private Path currentPath;
    private Paint currentPaint;

    /** Stroke drawing buffer. Used to render the line while it's being drawn. */
    protected AbstractReversibleViewAction buffer;

    /** Stroke event listeners. */
    public interface OnStrokeCompletedListener {
        void onStrokeCompleted(AbstractAction action);
    }

    /** Collection of current stroke listeners. */
    private final Set<OnStrokeCompletedListener> listeners;

    /** Min distance the user should move before you add to the path. */
    public static int MIN_MOVE_DIST = 5;

    /** Used to track last touch point for path drawing. */
    private final PointF lastPoint;

    /**
     * Creates a new, empty DrawingView with default paint properties.
     */
    public DrawingView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);

        listeners = new HashSet<>();

        lastPoint = new PointF();

        currentPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        currentPaint.setDither(true);
        currentPaint.setStyle(Paint.Style.STROKE);
        currentPaint.setStrokeJoin(Paint.Join.ROUND);
        currentPaint.setStrokeCap(Paint.Cap.ROUND);

        currentPath = new Path();

        state = DrawingModel.START;
    }

    /**
     * Handles touch events for the purposes of drawing on the canvas. On touch down,
     * begins drawing a path using the current paint. On touch move, continues drawing.
     * On touch up, notifies listeners of the completed stroke.
     *
     * @param event Event to use for drawing.
     * @return True if the event was handled, false otherwise.
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();
        Log.i(LOG_TAG, "Touch at (" + x + ", " + y + ")");

        // Handle input events.
        switch (state) {
            case START:
                return handleStartState(event, x, y);
            case DRAWING:
                return handleDrawingState(event, x, y);
            default:
                break;
        }
        return false;
    }

    /**
     * Private helper method to handle the Start state in the PPS
     * @param event The MotionEvent that triggered onTouchEvent
     * @param x The x coordinate of the touch event
     * @param y The y coordinate of the touch event
     * @return true if the event was consumed, false otherwise
     */
    private boolean handleStartState(MotionEvent event, float x, float y) {
        Log.i(LOG_TAG, "onDrawStart");
        onDrawStart(x, y);
        state = DrawingModel.DRAWING;
        return true;
    }

    /**
     * Private helper method to handle the Drawing state in the PPS
     * @param event The MotionEvent that triggered onTouchEvent
     * @param x The x coordinate of the touch event
     * @param y The y coordinate of the touch event
     * @return true if the event was consumed, false otherwise
     */
    private boolean handleDrawingState(MotionEvent event, float x, float y) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_MOVE:
                Log.i(LOG_TAG, "onDrawMove");
                onDrawMove(x, y);
                return true;
            case MotionEvent.ACTION_UP:
                Log.i(LOG_TAG, "onDrawEnd");
                onDrawEnd();
                state = DrawingModel.START;
                return true;
            case MotionEvent.ACTION_CANCEL:
                Log.i(LOG_TAG, "onDrawCancel");
                onDrawCancel();
                state = DrawingModel.START;
                return true;
            default:
                break;
        }
        return false;
    }

    /**
     * Triggered when drawing starts.
     *
     * @param x Horizontal coordinate of touch.
     * @param y Vertical coordinate of touch.
     */
    protected void onDrawStart(float x, float y) {
        // Start a new drawing path.
        currentPath.moveTo(x, y);
        lastPoint.x = x;
        lastPoint.y = y;
        Log.i(LOG_TAG, "onDrawStart: starting new stroke @ " + lastPoint);

        buffer = new StrokeAction(currentPath, currentPaint);
        buffer.doAction(this);
    }

    /**
     * Triggered when drawing moves. If we've moved enough, add a new point to the path.
     *
     * @param x Horizontal coordinate of touch.
     * @param y Vertical coordinate of touch.
     */
    protected void onDrawMove(float x, float y) {
        // Only add a bezier when the distance is larger than a threshold (MIN_MOVE_DIST).
        // If the distance is smaller, wait until a ACTION_MOVE event that creates a large enough distance.
        if (Math.sqrt(Math.pow(x - lastPoint.x, 2) + Math.pow(y - lastPoint.y, 2)) >= MIN_MOVE_DIST) {
            // For each ACTION_MOVE event, add a quadratic bezier from the last point (in the drawing path) to current point.
            // Each bezier is a smooth arc to be added in the drawing path.
            currentPath.quadTo(lastPoint.x, lastPoint.y,
                    (x + lastPoint.x) / 2, (y + lastPoint.y) / 2);
            lastPoint.x = x;
            lastPoint.y = y;

            // The stroke buffer has access to currentPath, invalidate to trigger redraw.
            buffer.invalidate();
        }
    }

    /**
     * Triggered when drawing ends. Commits the current buffer as a done action by triggering
     * callbacks.
     */
    protected void onDrawEnd() {
        buffer.undoAction(this);

        Log.i(LOG_TAG, "Stroke completed, triggering " + listeners.size() + " listener" + (listeners.size() == 1 ? "" : "s"));
        for (OnStrokeCompletedListener l : listeners) {
            l.onStrokeCompleted(buffer);
        }

        // Very important; buffer has a reference to currentPath. If we don't reinitialize, every
        // buffer will share the same path.
        currentPath = new Path();
        buffer = null;
    }

    /**
     * Triggered when drawing is cancelled. Trashes the current buffer and ignores callbacks.
     */
    protected void onDrawCancel() {
        if (buffer != null) {
            buffer.undoAction(this);
        }
        buffer = null;

        currentPath.reset();
    }

    //region Getters & Setters
    /**
     * Adds a new listener for stroke completion.
     *
     * @param listener  Listener to add.
     * @return true if the listener was added, false on duplicate.
     */
    public boolean addListener(OnStrokeCompletedListener listener) {
        return listeners.add(listener);
    }

    /**
     * Removes a listener.
     *
     * @param listener  Listener to remove.
     * @return true if the listener removed, false if not present.
     */
    public boolean removeListener(OnStrokeCompletedListener listener) {
        return listeners.remove(listener);
    }

    public Paint getCurrentPaint() {
        return currentPaint;
    }

    public void setCurrentPaint(Paint paint) {
        currentPaint = paint;
    }
    //endregion
}