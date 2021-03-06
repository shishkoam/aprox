package shishkoam.aprox;

import android.util.Log;

/**
 * Created by ав on 20.11.2015.
 */
public class AproximFunction {
    String TAG="shishkoam";

    public float[] lagrangeFunction(float[] dArray ) {

        int numberOfL = dArray.length;
        float[][] lForLagrang;
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

    public float[] mnkFunctionThirdPower(float[] dArray ){
        int numberOfValues = dArray.length;
        float[] mnkFunction = new float[2];
        float a = 0;
        float[][] matrixForThirdPowerMNK = new float[4][4];
        int summX[] = new int[7];
        float summY[] = new float[4];
        for (int i = 0; i < numberOfValues; i++) {
            summX[0] += 1;
            summX[1] += (i + 1);
            summX[2] += (i + 1)*(i + 1);
            summX[3] += (i + 1)*(i + 1)*(i + 1);
            summX[4] += (i + 1)*(i + 1)*(i + 1)*(i + 1);
            summX[5] += (i + 1)*(i + 1)*(i + 1)*(i + 1)*(i + 1);
            summX[6] += (i + 1)*(i + 1)*(i + 1)*(i + 1)*(i + 1)*(i + 1);
            summY[0] += dArray[i];
            summY[1] += dArray[i]*(i + 1);
            summY[2] += dArray[i]*(i + 1)*(i + 1);
            summY[3] += dArray[i]*(i + 1)*(i + 1)*(i + 1);
        }

        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                matrixForThirdPowerMNK[i][j] = summX[i+j];
            }
        }
        mnkFunction = methodGausa(matrixForThirdPowerMNK, summY);
        return mnkFunction;
    }

    private float[] methodGausa(float[][] matrix, float[] vector){
        float[] vectorAnswer = vector;
        for (int k = 0; k < 4; k++) {

            float firstDivider = matrix[k][k];
            vectorAnswer[k] = vectorAnswer[k] / firstDivider;
            for (int i = k; i < 4; i++) {
                matrix[k][i] = matrix[k][i] / firstDivider;
            }
            for (int i = 0; i < 4; i++) {
                if (i != k) {
                    float divider = matrix[i][k] / matrix[k][k];
                    vectorAnswer[i] = vectorAnswer[i] - vectorAnswer[k] * divider;
                    for (int j = 0; j < 4; j++) {
                        matrix[i][j] = matrix[i][j] - matrix[k][j] * divider;
                    }
                }
            }
        }
        return vectorAnswer;
    }
}
