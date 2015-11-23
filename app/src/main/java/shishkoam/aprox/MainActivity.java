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

public class MainActivity extends AppCompatActivity {

    // variables for image data
    private int zeroYBase=0;
    private int zeroXBase=0;
    private int sixXBase;
    private int tenYBase;
    private int oneXInterval;
    private int oneYInterval;
    private int screenWidth;
    private int screenHeight;
    private int viewHeight;
    private int viewWidth ;

    //ratio's from original background image - kY0 = (value for y=0) /( all height), kY10 for y=10 and so. XtoY= width / height
    private double kY0 = 430.0/530;
    private double kY10 = 70.0/530;
    private double kX0 = 51.0/730;
    private double kX6 = 523.0/730;
    private double XtoY = 730.0/530;

    private String stLagrange = "Lagrange";
    private String stReset = "Reset";

    // arrays for y values
    private float[] array = new float[5];
    private int[] arrayAllFunctionValues;
    private int valueForNull = -10;

    //value that show how many points user set
    private int arrayCurrentSize = 0;

    AproximFunction aproximFunctions;
    boolean lagrangeButtonIsPopped = false;

    private GestureDetector mGestureDetector;
    DrawView drawView;

    String TAG = "shishkoam";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //get width and height screen values for operating with view size and menu size
        Display display = getWindowManager().getDefaultDisplay();
        screenWidth = display.getWidth();
        screenHeight = display.getHeight();

        //new aproxim function
        aproximFunctions = new AproximFunction();

        //preparing array
        for (int i = 0; i < 5; i++) {
            if (!(array[i] > 0 && array[i] <= 11))
            array[i] = valueForNull;
        }

        drawView = new DrawView(this);
        setContentView(drawView);

    }

    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putFloatArray("array", array);
        outState.putInt("arrayCurrentSize", arrayCurrentSize);
        outState.putIntArray("arrayAllFunctionValues", arrayAllFunctionValues);
        outState.putBoolean("lagrangeButtonIsPopped", lagrangeButtonIsPopped);
    }

    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        array = savedInstanceState.getFloatArray("array");
        arrayCurrentSize = savedInstanceState.getInt("arrayCurrentSize");
        //arrayAllFunctionValues = savedInstanceState.getIntArray("arrayAllFunctionValues");
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

    // setup Gestures on single tap - buttons and point values
    private void setupGestureDetector(){

        mGestureDetector = new GestureDetector(this,
                new GestureDetector.SimpleOnGestureListener() {

                    @Override
                    public boolean onSingleTapConfirmed(MotionEvent event) {

                        // get menu height to operate in future only with view height
                        int menuHeight = screenHeight - viewHeight;

                        // get information about one tap gesture
                        int pointerIndex = event.getActionIndex();
                        int pointerID = event.getPointerId(pointerIndex);
                        float x = event.getX(pointerID);
                        float y = event.getY(pointerID);

                        //get nearest x number to touched area
                        int numberNearX = intersects(x);

                        // boolean variable to understand if value in current x was set before
                        boolean valueAlreadySet = false;

                        //if we have of one of five main points
                        if ((numberNearX <= 5)&& (numberNearX >= 1)) {

                            // if value of curent x was set before - we only change it in our array
                            if (array[numberNearX - 1] != valueForNull) {
                                array[numberNearX - 1] = (zeroYBase - y + menuHeight) / oneYInterval;
                                valueAlreadySet = true;
                            }

                            // if value of curent x wasn't set before - we set it and  increase arrayCurrentSize
                            if (!valueAlreadySet) {
                                array[numberNearX - 1] = (zeroYBase - y + menuHeight) / oneYInterval;
                                arrayCurrentSize++;
                            }

                            //set new points on draw
                            setContentView(drawView);
                        }

                        //if user touch was in button Lagrange area we will have true, else - false
                        boolean doesUserPressedLagrangeButton=(x >=  sixXBase) && (x <= viewWidth - oneXInterval) &&
                                (y >= 2 * tenYBase + 5 + menuHeight) && (y <=  2 * tenYBase + 5 + 2 * oneYInterval + menuHeight);

                        //button for "lagrange" functionality (if was set 5 points, and button area pressed or been pressed before)
                        if ((arrayCurrentSize == 5) && (doesUserPressedLagrangeButton || lagrangeButtonIsPopped)) {


                            //lagrange button is popped
                            lagrangeButtonIsPopped=true;

                              /** use if there is no rotates */
//                            // getting interval in pixels for six x values
//                            float sizeByXInPx=sixXBase-zeroXBase;
//                            //creating lagrange function for 5 already set points
//                            float[] lagrangeFunction = aproximFunctions.lagrangeFunction(array);
//
//                            //creating array for all function values
//                            arrayAllFunctionValues = new int [(int) sizeByXInPx];
//
//                            //getting all function values using approximation functions
//                            for (int i = 0; i < sizeByXInPx; i++) {
//                                arrayAllFunctionValues[i]=zeroYBase - getApproxFunctionValue(i, lagrangeFunction);
//                            }

                            //set graphics values of view
                            setContentView(drawView);
                        }

                        //if user touch was in button Reset area we will have true, else - false
                        boolean doesUserPressedResetButton=(x >=  sixXBase) && (x <= viewWidth - oneXInterval) &&
                                (y >= 2 * tenYBase - 2 * oneYInterval + 5 + menuHeight) && (y <=  2 * tenYBase - 5 + menuHeight);

                        //button for reset function
                        if (doesUserPressedResetButton) {

                            //set all values to some null value
                            for (int i = 0; i < 5; i++) {
                                array[i] = valueForNull;
                            }
                            lagrangeButtonIsPopped = false;
                            arrayCurrentSize = 0;
                            //set graphics values of view
                            setContentView(drawView);
                        }

                        return true;
                    }
                });
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

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {

        // delegate the touch to the gestureDetector
        Log.i(TAG, "Dispatched touch");
        return mGestureDetector.onTouchEvent(event);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        // delegate the touch to the gestureDetector
        Log.i(TAG, "Detected on Touch");
        return mGestureDetector.onTouchEvent(event);
    }

    //function that return nearest natural value for current x
    private int intersects(double x){

        int nearestIntX=(int)((x - zeroXBase) / oneXInterval);
        Log.i(TAG,"narestInt " + nearestIntX + "zeroXBASE " + zeroXBase + "oneX " + oneXInterval);
        return nearestIntX;
    }

    class DrawView extends View {

        Paint p;

        public DrawView(Context context) {
            super(context);
            p = new Paint();
            //setting up gesture detector
            setupGestureDetector();
        }


        @Override
        protected void onDraw(Canvas canvas) {
//            this.setOnTouchListener(new OnTouchListener() {
//
//                @Override
//                public boolean onTouch(View v, MotionEvent event) {
//                    mGestureDetector.onTouchEvent(event);
//                    return false;
//                }
//            });

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

            // setting background picture in rectangle with - coordinate system
            Bitmap image = BitmapFactory.decodeResource(getResources(), R.drawable.background);
            RectF rectFull = new RectF(0,0,viewWidth,viewHeight);
            canvas.drawBitmap(image, null, rectFull, p);

            // indent between buttons and for their stroke for text
            int indent = 5;
            // drawing red button for reset action
            p.setColor(Color.RED);
            canvas.drawRect(sixXBase, 2 * tenYBase - 2 * oneYInterval - indent, viewWidth - oneXInterval, 2 * tenYBase - indent, p);

            // drawing blue button for lagrange action
            p.setColor(Color.BLUE);
            canvas.drawRect(sixXBase, 2 * tenYBase + indent, viewWidth - oneXInterval, 2 * tenYBase + indent + 2 * oneYInterval, p);

            //setting text for buttons
            p.setColor(Color.BLACK);
            p.setTextSize(3 * (viewWidth - oneXInterval - sixXBase) / (2 * stLagrange.length()));
            canvas.drawText(stLagrange, sixXBase + indent, 2 * tenYBase + indent + oneYInterval, p);
            canvas.drawText(stReset, sixXBase + indent, 2 * tenYBase - indent - oneYInterval, p);

            // if lagrange button is popped and all 5 points set - draw blue graphic for function
            p.setColor(Color.BLUE);
            int y ;
            if (arrayCurrentSize == 5 && lagrangeButtonIsPopped)  {

                /** comment from here to the end of to do if you don't use rotation
                */
                // getting interval in pixels for six x values
                float sizeByXInPx = sixXBase - zeroXBase;
                //creating lagrange function for 5 already set points
                float[] lagrangeFunction = aproximFunctions.lagrangeFunction(array);
                //creating array for all function values
                arrayAllFunctionValues = new int[(int) sizeByXInPx];

                //getting all function values using approximation functions
                for (int i = 0; i < sizeByXInPx; i++) {
                    arrayAllFunctionValues[i] = zeroYBase - getApproxFunctionValue(i, lagrangeFunction);
                }
                /** end for TO DO */

                // we will draw graphic using lines from previous (y,x) to current
                int previousX = zeroXBase;
                int previousY = arrayAllFunctionValues[0];
                for (int x = zeroXBase+1; x < sixXBase; x++) {
                    y = arrayAllFunctionValues[x - zeroXBase];
                    //drawing values only in graphic area

                    // setting stroke width
                    p.setStrokeWidth(3);

                    if (y<viewHeight)
                    canvas.drawLine(previousX,previousY,x,y,p);
                    previousX = x;
                    previousY = y;
                }
            }

            //drawing red points, that we set using rectangles pointSize*pointSize for each one
            int pointSize=24;
            p.setColor(Color.RED);

            for (int i = 0; i < 5; i++) {
                //drawing values only in graphic area
                if (array[i] != valueForNull)
                    canvas.drawRect((i+1)*oneXInterval + zeroXBase - pointSize/2,
                            zeroYBase - array[i]*oneYInterval - pointSize/2,
                            (i+1)*oneXInterval + zeroXBase + pointSize/2,
                            zeroYBase - array[i]*oneYInterval + pointSize/2, p);
            }


        }

    }
}
