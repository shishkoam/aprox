package shishkoam.aprox;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.view.View;

import java.util.Random;

/**
 * Created by ав on 05.12.2015.
 */
class MarkerView extends View {
    private float mY;
    private int mX;
    final static private int SIZE = 10;
    private int mTouches = 0;
    final private Paint mPaint = new Paint();

    public MarkerView(Context context, int x, float y) {
        super(context);
        mX = x;
        mY = y;
        mPaint.setStyle(Paint.Style.FILL);

        mPaint.setARGB(255, 255, 0, 0);
    }

    int getXLoc() {
        return mX;
    }

    void setXLoc(int x) {
        mX = x;
    }

    float getYLoc() {
        return mY;
    }

    void setYLoc(float y) {
        mY = y;
    }

    void setTouches(int touches) {
        mTouches = touches;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawCircle(mX, mY, SIZE, mPaint);
    }
}