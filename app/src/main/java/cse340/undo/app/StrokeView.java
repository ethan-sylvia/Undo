package cse340.undo.app;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.view.View;

/**
 * Simple little view which takes a path and paint object and uses and renders them.
 */
@SuppressLint("ViewConstructor")
public class StrokeView extends View {
    protected final Path path;
    protected final Paint paint;

    public StrokeView(Context context, Path path, Paint paint) {
        super(context);
        this.path = path;
        this.paint = paint;
    }

    /**
     * Renders the stroke by drawing the path on the view Canvas.
     */
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        canvas.drawPath(path, paint);
    }
}
