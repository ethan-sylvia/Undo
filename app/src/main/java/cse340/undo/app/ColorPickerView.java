package cse340.undo.app;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Rect;
import android.support.annotation.ColorInt;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.graphics.Paint;
import android.view.View;


import static android.graphics.Color.HSVToColor;
import static android.graphics.Color.RED;
import static android.graphics.Color.WHITE;

/**
 * This is a subclass of AbstractColorPickerView, that is, this View implements a ColorPicker.
 *
 * There are several class fields, enums, callback classes, and helper functions which have
 * been implemented for you.
 *
 * PLEASE READ AbstractColorPickerView.java to learn about these.
 */
public class ColorPickerView extends AbstractColorPickerView {
/* ********************************************************************************************** *
 * All of your applications state (the model) and methods that directly manipulate it are here    *
 * This does not include mState which is the literal state of your PPS, which is inherited
 * ********************************************************************************************** */

    /**
     * The current color selected in the ColorPicker. Not necessarily the last
     * color that was sent to the listeners.
     */
    @ColorInt
    protected int mCurrentColor;

    @Override
    public void setColor(@ColorInt int newColor) {
        mCurrentColor = newColor;
        invalidate();
    }

    private void updateModel(float x, float y) {
        setColor(getColorFromAngle(getTouchAngle(x, y)));
        // hint: we give you a very helpful function to call
    }

/* ********************************************************************************************** *
 *                               <End of model declarations />
 * ********************************************************************************************** */

/* ********************************************************************************************** *
 * You may create any constants you wish here.                                                     *
 * You may also create any fields you want, that are not necessary for the state but allow       *
 * for better optimized or cleaner code                                                           *
 * ********************************************************************************************** */



/* ********************************************************************************************** *
 *                               <End of other fields and constants declarations />
 * ********************************************************************************************** */

    public ColorPickerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mState = State.START;
        this.mCurrentColor = DEFAULT_COLOR;
        setVisibility(View.GONE);
        // TODO: Initialize variables as necessary (such as state)
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        Paint paintThumb = new Paint();
        paintThumb.setStyle(Paint.Style.FILL);
        paintThumb.setColor(WHITE);
        if (mState == State.INSIDE) {
            paintThumb.setAlpha(128);
        }

        float theta = getAngleFromColor(mCurrentColor);
        //float y = (float) (mRadius * Math.sin(theta));
        //float x = (float) (mRadius * Math.cos(theta));
        double thumbRatio = (mRadius - (mRadius * RADIUS_TO_THUMB_RATIO));
        canvas.drawCircle(
                (float) (mCenterX + (thumbRatio * Math.cos(theta))),
                (float) (mCenterY + (thumbRatio * Math.sin(theta))),
                (mRadius * RADIUS_TO_THUMB_RATIO), paintThumb);

        Paint paintCircle = new Paint();
        paintCircle.setColor(mCurrentColor);
    //    canvas.drawCircle(mCenterX, mCenterY,
    //            mRadius - (2 * RADIUS_TO_THUMB_RATIO * mRadius), paintCircle);



    }

    @Override
    public void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        mRadius = Math.min(getHeight() / 2, getWidth() / 2);
        mCenterX = getWidth() / 2;
        mCenterY = getHeight() / 2;

        // TODO: calculate mRadius, mCenterX, and mCenterY based View dimensions


    }

    /**
     * Calculate the essential geometry given an event.
     *
     * @param event Motion event to compute geometry for, most likely a touch.
     * @return EssentialGeometry value.
     */
    @Override
    protected EssentialGeometry essentialGeometry(MotionEvent event) {
        // TODO: compute the geometry for the given event
        float touchX = event.getX();
        float touchY = event.getY();
        double distance = Math.sqrt(Math.pow(mCenterX - touchX, 2.0) + Math.pow(mCenterY - touchY, 2.0));


        if (distance <= mRadius && mRadius - (2 * RADIUS_TO_THUMB_RATIO * mRadius) <= distance) {
            return EssentialGeometry.WHEEL;
        }
        return EssentialGeometry.OFFWHEEL;

    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        EssentialGeometry geometry = essentialGeometry(event);
        switch(mState) {
            case START:
                if (event.getAction() == MotionEvent.ACTION_DOWN && geometry == EssentialGeometry.WHEEL) {
                    mState = State.INSIDE;

                    updateModel(event.getX(), event.getY());
                    invalidate();
                    return true;
                }
                break;
            case INSIDE:
                if (event.getAction() == MotionEvent.ACTION_MOVE && geometry == EssentialGeometry.WHEEL) {
                    updateModel(event.getX(), event.getY());
                    invalidate();
                    return true;
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    mState = State.START;
                    invokeColorChangeListeners(mCurrentColor);
                    invalidate();
                    return true;
                }
                break;
            default:
                break;
        }
        return false;

    }

    /**
     * Converts from a color to angle on the wheel.
     *
     * @param color hue color as integer.
     * @return Position of this color on the wheel in radians.
     * @see AbstractColorPickerView#getTouchAngle(float, float)
     */
    public static float getAngleFromColor(int color) {
        float[] hsv = new float[3];
        Color.colorToHSV(color, hsv);
        return ((float) Math.toRadians(hsv[0]-90));
    }
}
