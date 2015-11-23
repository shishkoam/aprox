package shishkoam.aprox;

import android.util.Log;

/**
 * Created by ав on 20.11.2015.
 */
public class AproximFunction {
    private  float[][] lForLagrang;
    String TAG="shishkoam";

    public float[] lagrangeFunction(float[] dArray ) {

        int numberOfL = dArray.length;
        lForLagrang = new float[5][5];
        for (int i = 0; i < numberOfL; i++) {
            float[] koef = new float[numberOfL];
            int divider = 1;
            koef[0] = 1;
            koef[4] = 1;
            koef[1] = 0;
            koef[2] = 0;
            koef[3] = 0;
            for (int j = 0; j < numberOfL; j++) {
                if (j != i) {
                    divider = divider * (i - j);
                    koef[4] *= j + 1;
                    koef[1] += -(j + 1);

                    int helperForKoefForFistPower = 1;
                    for (int k = 0; k < numberOfL; k++) {
                        if (j != k && i != k) {
                            helperForKoefForFistPower *= -(k + 1);
                        }
                    }
                    koef[3] += helperForKoefForFistPower;

                    int helperForKoefForSecondPower = 0;
                    for (int n = 0; n < 5; n++) {
                        if (n != i) {
                            for (int m = n + 1; m < 5; m++) {
                                if (m!=i)
                                helperForKoefForSecondPower += (n + 1) * (m + 1);
                            }
                        }
                    }
                    koef[2] = helperForKoefForSecondPower;
                }
            }
            for (int j = 0; j <5 ; j++) {
                koef[j]=koef[j]/divider;
            }
            lForLagrang[i]=koef;
        }
        float[] lagrangF= new float[5];
        for (int i = 0; i < 5; i++) {
            float koefLagr = 0;
            for (int j = 0; j <5 ; j++) {

                koefLagr += dArray[j]*lForLagrang[j][i];
            }
            lagrangF[i]=koefLagr;
        }
	return lagrangF;
    }
}
