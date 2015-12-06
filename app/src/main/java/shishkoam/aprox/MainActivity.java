package shishkoam.aprox;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    // variables for image data
    private int zeroYBase = 0;
    private int zeroXBase = 0;
    private int sixXBase;
    private int tenYBase;
    private int oneXInterval;
    private int oneYInterval;
    private int screenWidth;
    private int screenHeight;
    private int viewHeight;
    private int viewWidth;
    private FrameLayout mFrame;

    private static final int MIN_DXDY = 2;

    // Assume no more than 20 simultaneous touches
    final private static int MAX_TOUCHES = 10;

    // Pool of MarkerViews
    final private static LinkedList<MarkerView> mInactiveMarkers = new LinkedList<MarkerView>();
    final private static Map<Integer, MarkerView> mActiveMarkers = new HashMap<Integer, MarkerView>();

    //ratio's from original background image - kY0 = (value for y=0) /( all height), kY10 for y=10 and so. XtoY= width / height
    private double kY0 = 430.0 / 530;
    private double kY10 = 70.0 / 530;
    private double kX0 = 51.0 / 730;
    private double kX6 = 523.0 / 730;
    private double XtoY = 730.0 / 530;

    private String stLagrange = "Lagrange";
    private String stReset = "Reset";
    private String stMnk = "MNK";

    // arrays for y values
    private float[] array = new float[5];
    private int[] arrayAllFunctionValuesForLagrange;
    private int[] arrayAllFunctionValuesForMNK;
    private int valueForNull = -10;

    //value that show how many points user set
    private int arrayCurrentSize = 0;

    AproximFunction aproximFunctions;
    boolean lagrangeButtonIsPopped = false;
    boolean mnkButtonIsPopped = false;

    DrawView drawView;

    String TAG = "shishkoam";

    private int menuHeight;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //get width and height screen values for operating with view size and menu size
        Display display = getWindowManager().getDefaultDisplay();
        screenWidth = display.getWidth();
        screenHeight = display.getHeight();
        initViews();

        //new aproximation function
        aproximFunctions = new AproximFunction();

        //preparing array
        for (int i = 0; i < 5; i++) {
            if (!(array[i] > 0 && array[i] <= 11))
                array[i] = valueForNull;
        }

        mFrame = (FrameLayout) findViewById(R.id.frame);
        drawView = new DrawView(this);
        mFrame.addView(drawView);

        // Create and set on touch listener
        mFrame.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                switch (event.getActionMasked()) {

                    // Show new MarkerView
                    case MotionEvent.ACTION_DOWN:
                    case MotionEvent.ACTION_POINTER_DOWN: {

                        int pointerIndex = event.getActionIndex();
                        int pointerID = event.getPointerId(pointerIndex);

                        // get menu height to operate in future only with view height
                        menuHeight = screenHeight - viewHeight;

                        // get information about one tap gesture
                        float eventX;
                        float eventY;
                        try {
                            eventX = event.getX(pointerID);
                            eventY = event.getY(pointerID);
                        }
                        catch (IllegalArgumentException e){
                            eventX = -1;
                            eventY = -1;
                        }
                        //get nearest x number to touched area
                        int numberNearX = intersects(eventX);

                        for (MarkerView marker : mInactiveMarkers) {
                            if (marker.getXLoc() == (zeroXBase + numberNearX * oneXInterval)) {
                                mFrame.removeView(marker);
                            }
                        }

                        boolean MarkerWithSameXNotActive = true;
                        for (MarkerView marker : mActiveMarkers.values()) {
                            if (marker.getXLoc() == (zeroXBase + numberNearX * oneXInterval)
                                    && marker.getYLoc() != eventY) {
                                MarkerWithSameXNotActive = false;
                            }
                        }

                        //if we have one of five main points
                        if ((numberNearX <= 5) && (numberNearX >= 1) && MarkerWithSameXNotActive) {
                            MarkerView marker = new MarkerView(getApplicationContext(), -1, -1);
                            if (null != marker) {
                                    mFrame.removeView(marker);
                                    mActiveMarkers.put(pointerID, marker);
                                    marker.setXLoc(zeroXBase + numberNearX * oneXInterval);
                                    marker.setYLoc(eventY);
                                    mFrame.addView(marker);
                            }
                        }
                        if (arrayCurrentSize == 5) {
                            //button for "lagrange" functionality (if was set 5 points, and button area pressed or been pressed before)
                            //lagrange button is popped (unpopped)
                            if (doesUserPressedLagrangeButton(eventX, eventY))
                                lagrangeButtonIsPopped = changeBoolean(lagrangeButtonIsPopped);
                            if (lagrangeButtonIsPopped) {
                                calculateFunctionValuesForLAgrange();
                            }
                            //button for "mnk" functionality
                            //MNK button is popped (unpopped)
                            if (doesUserPressedMNKButton(eventX, eventY))
                                mnkButtonIsPopped = changeBoolean(mnkButtonIsPopped);
                            if ((mnkButtonIsPopped)) {
                                calculateFunctionValuesForMNK();
                            }
                            drawView.invalidate();
                        }
                        //button for reset function
                        if (doesUserPressedResetButton(eventX, eventY)) {
                            resetFiveBasePoints();
                        }
                        break;
                    }

                    // Save one MarkerView
                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_POINTER_UP: {

                        int pointerIndex = event.getActionIndex();
                        int pointerID = event.getPointerId(pointerIndex);

                        MarkerView marker = mActiveMarkers.remove(pointerID);

                        if (null != marker) {
                            mInactiveMarkers.add(marker);
                            int numberNearX = intersects(marker.getXLoc());
//                            Log.i(TAG, intersects(event.getX(pointerID)) + " / " + intersects(marker.getXLoc()));

                            if (numberNearX >= 1 && numberNearX <= 5) {
                                // if value of current x was set before - we only change it in our array
                                if (array[numberNearX - 1] != valueForNull) {
                                    array[numberNearX - 1] = (zeroYBase - marker.getYLoc()) / oneYInterval;
                                    Log.i(TAG, "y = " + array[numberNearX - 1] + " x = " + numberNearX);
                                }
                                // if value of current x wasn't set before - we set it and  increase arrayCurrentSize
                                else {
                                    array[numberNearX - 1] = (zeroYBase - marker.getYLoc()) / oneYInterval;
                                    Log.i(TAG, "y = " + array[numberNearX - 1]);
                                    arrayCurrentSize++;
                                }
                            }
                            //recalculating lagrange function if needed
                            if ((arrayCurrentSize == 5) && (lagrangeButtonIsPopped)) {
                                calculateFunctionValuesForLAgrange();
                                drawView.invalidate();
                            }
                            //recalculating mnk function if needed
                            if ((arrayCurrentSize == 5) && (mnkButtonIsPopped)) {
                                calculateFunctionValuesForMNK();
                                drawView.invalidate();
                            }
                        }
                        break;
                    }

                    // Move all currently active MarkerViews
                    case MotionEvent.ACTION_MOVE: {

                        for (int idx = 0; idx < event.getPointerCount(); idx++) {
                            int ID = event.getPointerId(idx);
                            MarkerView marker = mActiveMarkers.get(ID);
                            if (null != marker) {
                                // Redraw only if finger has travel ed a minimum distance
                                if (Math.abs(marker.getYLoc()
                                        - event.getY(idx)) > MIN_DXDY) {
                                    // Set new location only by y
                                    marker.setYLoc(event.getY(idx));
                                    // Request re-draw
                                    marker.invalidate();
                                }
                            }
                        }
                        break;
                    }
                    default:
                        Log.i(TAG, "unhandled action");
                }
                return true;
            }
        });
    }

    private boolean changeBoolean(boolean value){
        if (value == true) return false;
        return true;
    }
    private void resetFiveBasePoints() {
        //set all values to some null value
        for (int i = 0; i < 5; i++) {
            array[i] = valueForNull;
        }

        for (MarkerView marker:mInactiveMarkers){
            marker = new MarkerView(this, -1, -1);
            marker.invalidate();
            mFrame.removeAllViews();
            mFrame.addView(drawView);
        }

        for (MarkerView marker:mActiveMarkers.values()) {
            marker = new MarkerView(this, -1, -1);
        }

        lagrangeButtonIsPopped = false;
        mnkButtonIsPopped = false;
        arrayCurrentSize = 0;
    }

    private boolean doesUserPressedResetButton(float x, float y) {
        //if user touch was in button Reset area we will have true, else - false
        return (x >= sixXBase) && (x <= viewWidth - oneXInterval) &&
                (y >= 2 * tenYBase - 2 * oneYInterval + 5 ) && (y <= 2 * tenYBase - 5);
    }

    private void calculateFunctionValuesForLAgrange() {
        // getting interval in pixels for six x values
        float sizeByXInPx = sixXBase-zeroXBase;
        //creating lagrange function for 5 already set points
        float[] lagrangeFunction = aproximFunctions.lagrangeFunction(array);
        //creating array for all function values
        arrayAllFunctionValuesForLagrange = new int [(int) sizeByXInPx];
        //getting all function values using approximation functions
        for (int i = 0; i < sizeByXInPx; i++) {
            arrayAllFunctionValuesForLagrange[i] = zeroYBase - getApproxFunctionValue(i, lagrangeFunction);
        }
    }

    private void calculateFunctionValuesForMNK() {
        // getting interval in pixels for six x values
        float sizeByXInPx = sixXBase-zeroXBase;
        //creating lagrange function for 5 already set points
        float[] mnkFunction = aproximFunctions.mnkFunctionThirdPower(array);
        //creating array for all function values
        arrayAllFunctionValuesForMNK = new int [(int) sizeByXInPx];
        //getting all function values using approximation functions
        for (int i = 0; i < sizeByXInPx; i++) {
            arrayAllFunctionValuesForMNK[i] = zeroYBase - mnkFunctionThirdPower(i, mnkFunction);
        }
    }

    private boolean doesUserPressedLagrangeButton(float x, float y) {
        //if user touch was in button Lagrange area we will have true, else - false
        return (x >=  sixXBase) && (x <= viewWidth - oneXInterval) &&
                (y >= 2 * tenYBase + 5) && (y <=  2 * tenYBase + 5 + 2 * oneYInterval );
    }

    private boolean doesUserPressedMNKButton(float x, float y) {
        //if user touch was in button Lagrange area we will have true, else - false
        return  (x >=  sixXBase) && (x <= viewWidth - oneXInterval) &&
                (y >= 2 * tenYBase + 10 + 2 * oneYInterval) && (y <=  2 * tenYBase + 5 + 4 * oneYInterval );
    }

    private void initViews() {
        for (int idx = 0; idx < MAX_TOUCHES; idx++) {
            mInactiveMarkers.add(new MarkerView(this, -1, -1));
        }
    }

    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putFloatArray("array", array);
        outState.putInt("arrayCurrentSize", arrayCurrentSize);
        outState.putBoolean("lagrangeButtonIsPopped", lagrangeButtonIsPopped);
    }

    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        array = savedInstanceState.getFloatArray("array");
        arrayCurrentSize = savedInstanceState.getInt("arrayCurrentSize");
        lagrangeButtonIsPopped = savedInstanceState.getBoolean("lagrangeButtonIsPopped");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    //method that return value of lagrangeFunction for 5 point using current x
    private int getApproxFunctionValue(int xPx, float[] lagrangeFunction){
        //get x value in graphic from x in pixels
        double x=((double)xPx)/oneXInterval;
        //calculating value of lagrange function
        double arg = x * x * x * x * lagrangeFunction[0] +
                x * x * x * lagrangeFunction[1] +
                x * x * lagrangeFunction[2] +
                x * lagrangeFunction[3] + lagrangeFunction[4];
        return (int)(arg*oneYInterval);
    }

    //method that return value of mnkFunctionThirdPower for 5 point using current x
    private int mnkFunctionThirdPower(int xPx, float[] mnkFunctionThirdPower){
        //get x value in graphic from x in pixels
        double x=((double)xPx)/oneXInterval;
        //calculating value of lagrange function
        double arg = x * x * x * mnkFunctionThirdPower[3] +
                x * x * mnkFunctionThirdPower[2] +
                x * mnkFunctionThirdPower[1] +
                mnkFunctionThirdPower[0];
        return (int)(arg*oneYInterval);
    }


    //function that return nearest natural value for current x
    private int intersects(double x){
        int nearestIntX=(int)((x - zeroXBase) / oneXInterval);
//        Log.i(TAG,"narestInt " + nearestIntX + "zeroXBASE " + zeroXBase + "oneX " + oneXInterval);
        return nearestIntX;
    }

    class DrawView extends View {
        Paint p;
        public DrawView(Context context) {
            super(context);
            p = new Paint();
        }

        @Override
        protected void onDraw(Canvas canvas) {
            // getting view height and width
            viewHeight = this.getHeight();
            viewWidth = this.getWidth();

/**         comment next to lines to make image full screen (you will fill it by one of ways) */
//               or uncomment for make image with good view
//            if (viewHeight*XtoY>(double)viewWidth) viewHeight=(int)Math.round(viewWidth/XtoY);
//            if (viewHeight*XtoY<(double)viewWidth) viewWidth=(int)Math.round(viewHeight*XtoY);

            //get scaled values for zero value points for x and y, for ten value point for y and for six value point for x
            zeroYBase = (int) Math.round(viewHeight * kY0);
            tenYBase = (int) Math.round(viewHeight * kY10);
            zeroXBase = (int) Math.round(viewWidth * kX0);
            sixXBase = (int) Math.round(viewWidth * kX6);

            //get scaled y and x interval values
            oneXInterval = (sixXBase-zeroXBase) / 6;
            oneYInterval = (zeroYBase-tenYBase) / 10;

            settingCoordinateSystem(canvas);
            settingButtons(canvas);

            // if lagrange button is popped and all 5 points set - draw blue graphic for function
            if (arrayCurrentSize == 5 && lagrangeButtonIsPopped)  {
                drawingLagrangeApproximation(canvas);
            }

            // if lagrange button is popped and all 5 points set - draw blue graphic for function
            if (arrayCurrentSize == 5 && mnkButtonIsPopped)  {
                drawingMNKApproximation(canvas);
            }

        }

        private void drawingLagrangeApproximation(Canvas canvas) {
            p.setColor(Color.BLUE);
            int y ;
//                Log.i(TAG,"lagrange");
            // we will draw graphic using lines from previous (y,x) to current
            int previousX = zeroXBase;
            int previousY = arrayAllFunctionValuesForLagrange[0];
            for (int x = zeroXBase + 1; x < sixXBase; x++) {
                y = arrayAllFunctionValuesForLagrange[x - zeroXBase];
                Log.i(TAG,"lagrange " + y + " / " + x);
                //drawing values only in graphic area
                p.setStrokeWidth(3);
                if (y < viewHeight)
                canvas.drawLine(previousX,previousY,x,y,p);
                previousX = x;
                previousY = y;
            }
        }

        private void drawingMNKApproximation(Canvas canvas) {
            p.setColor(Color.YELLOW);
            int y ;
//                Log.i(TAG,"lagrange");
            // we will draw graphic using lines from previous (y,x) to current
            int previousX = zeroXBase;
            int previousY = arrayAllFunctionValuesForMNK[0];
            for (int x = zeroXBase + 1; x < sixXBase; x++) {
                y = arrayAllFunctionValuesForMNK[x - zeroXBase];
                Log.i(TAG,"lagrange " + y + " / " + x);
                //drawing values only in graphic area
                p.setStrokeWidth(3);
                if (y < viewHeight)
                    canvas.drawLine(previousX,previousY,x,y,p);
                previousX = x;
                previousY = y;
            }
        }

        private void settingButtons(Canvas canvas) {
            // indent between buttons and for their stroke for text
            int indent = 5;
            // drawing red button for reset action
            p.setColor(Color.RED);
            canvas.drawRect(sixXBase, 2 * tenYBase - 2 * oneYInterval - indent, viewWidth - oneXInterval, 2 * tenYBase - indent, p);

            // drawing blue button for lagrange action
            p.setColor(Color.BLUE);
            canvas.drawRect(sixXBase, 2 * tenYBase + indent, viewWidth - oneXInterval, 2 * tenYBase + 2 * oneYInterval, p);

            // drawing red button for reset action
            p.setColor(Color.YELLOW);
            canvas.drawRect(sixXBase, 2 * tenYBase + 2* indent + 2 * oneYInterval, viewWidth - oneXInterval, 2 * tenYBase + indent + 4 * oneYInterval, p);

            //setting text for buttons
            p.setColor(Color.BLACK);
            p.setTextSize(3 * (viewWidth - oneXInterval - sixXBase) / (2 * stLagrange.length()));
            canvas.drawText(stReset, sixXBase + indent, 2 * tenYBase + indent - oneYInterval, p);
            canvas.drawText(stLagrange, sixXBase + indent, 2 * tenYBase + indent + oneYInterval, p);
            canvas.drawText(stMnk, sixXBase + indent, 2 * tenYBase + 2 * indent + 3 * oneYInterval, p);
        }

        // setting background picture in rectangle with - coordinate system
        private void settingCoordinateSystem(Canvas canvas) {
            Bitmap image = BitmapFactory.decodeResource(getResources(), R.drawable.background);
            RectF rectFull = new RectF(0,0,viewWidth,viewHeight);
            canvas.drawBitmap(image, null, rectFull, p);
        }
    }
}
