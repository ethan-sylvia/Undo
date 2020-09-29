package cse340.undo.actions;

import android.graphics.Color;
import android.graphics.Paint;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;

import cse340.undo.app.DrawingView;

/**
 * Reversible action which changes the color of the DrawingView's paint.
 */
public class ChangeShapeAction extends AbstractReversibleAction {
    /** The color that this action changes the current paint to. */
    @ColorInt
    protected final int color;

    /** The color that this action changes the current paint from. */
    @ColorInt
    protected int prev;

    /**
     * Creates an action that changes the paint color.
     *
     * @param color New color for DrawingView paint.
     */
    public ChangeShapeAction(@ColorInt int color) {
        this.color = color;
    }

    /** @inheritDoc */
    @Override
    public void doAction(DrawingView view) {
        super.doAction(view);
        Paint cur = view.getCurrentPaint();
        prev = cur.getColor();
        cur.setColor(color);
    }

    /** @inheritDoc */
    @Override
    public void undoAction(DrawingView view) {
        super.undoAction(view);
        view.getCurrentPaint().setColor(prev);
    }

    @NonNull
    @Override
    public String toString() {
        return "Change color to RGBA = (" +
                Color.red(color) +
                ", " +
                Color.blue(color) +
                ", " +
                Color.green(color) +
                ", " +
                Color.alpha(color) +
                ")";
    }
}
