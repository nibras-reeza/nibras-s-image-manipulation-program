/**
 * ***********************************************************************************
 *
 * Author: Nibras Ahamed Reeza (CB004641) Email: nibras.me@facebook.com
 *
 * Created: 21st of December, 2013
 *
 * This file part of the image processing artifact created for Imaging and
 * Special Effects module.
 *
 * This is the core of the program. All manipulations of the image happen here.
 *
 * References:
 *
 * Complete list is given at the end of the file.
 *
 * This file also contains excerpts from Skeletal code distributed in class.
 *
 ***********************************************************************************
 */
package lk.apiit.nibras.ise;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class PixelImage {

    private BufferedImage image;
    // Arrays to read and store pixel values for manipulation.
    // Image is loaded into rawImageArray. Where image cannot
    // be manipulated in same array, targetImageArray is used
    // to store the result.
    // Both gray-scale and color images are treated as color
    // images. Alpha layer is ignored. Each image is considered
    // as a 2D array with three/four layer with each layer
    // representing either a color(Red, Green or Blue) or alpha
    // layer. This results in a 3D array.
    private short[][][] rawImageArray;
    private short[][][] targetImageArray;
    private int[] rgbArray;
    private int h, l, w;
    private short[][][] snapshot;
    private boolean undoable;
    // Temporary counter variables are defined once here instead
    // of redefining them as required to improve performance.
    private int srcRow, srcCol;
    private int layer, row, col;
    private final int colorDepth = 256;

    private void updateImage() {
        NIMP.getInstance().getStatusBar().setStatus("Rendering Image...");
        NIMP.getInstance().getStatusBar().setProgress(0);
        // (stackoverflow 2012)
        updateDimensions(); // In case images sizes have changed, check it.

        // Make sure the buffer is large enough to hold the entire image.
        if (rgbArray.length != h * w) {
            rgbArray = new int[h * w];
        }

        int colOffset;
        for (row = 0; row < h; row++) {
            colOffset = row * w;
            for (col = 0; col < w; col++) {
                // Alpha layer is always 100%/255.
                // Use the 0xAARRGGBB style for int.
                rgbArray[col + colOffset] = 0xff000000
                        | rawImageArray[0][row][col] << 16
                        | rawImageArray[1][row][col] << 8
                        | rawImageArray[2][row][col];
            }
        }

        // Make sure the BufferedImage used is the right size. If not,
        // make a new one.
        if (h != image.getHeight() || w != image.getWidth()) {
            image = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        }

        // Put the RGB int values into the BufferedImage.
        image.setRGB(0, 0, w, h, rgbArray, 0, w);

        NIMP.getInstance().getStatusBar().setProgress(100);
        NIMP.getInstance().getStatusBar().setStatus("");
    }

    public BufferedImage getImage() {
        return image;
    }

    // Likely to be the first method called.
    public void setImage(BufferedImage image) {
        // Store a reference to the original BufferedImage. BufferedImage acts
        // as the universal currency in this program. All images are loaded as
        // BufferedImage and passed here. The BufferedImage is sliced into 3
        // layers and stored in Arrays and manipulated. When image is to be
        // displayed, it's converted from the Arrays back into BufferedImage.
        this.image = image;
        w = image.getWidth();
        h = image.getHeight();

        // Color layer is the first array. Using [row][column][layer]
        // would result in row*column number of short[] arrays but, this
        // setup would result in only layer*row number of short[] arrays
        // which is significantly lower.

        // Initialize the required Arrays.
        rawImageArray = new short[3][h][w];
        rgbArray = new int[h * w]; // The buffer between 3D Array and
        // BufferedImage.

        image.getRGB(0, 0, w, h, rgbArray, 0, w);

        int colOffset;
        for (row = 0; row < h; row++) {
            colOffset = row * w;
            for (col = 0; col < w; col++) {
                // Convert from the combined ARGB colored model to individual
                // color layers.
                // (stackoverflow 2012)
                int temp = rgbArray[col + colOffset];
                rawImageArray[0][row][col] = (short) (temp >> 16 & 0xff);
                rawImageArray[1][row][col] = (short) (temp >> 8 & 0xff);
                rawImageArray[2][row][col] = (short) (temp & 0xff);
                // Alpha layers is discarded. It's not loaded.
            }
        }
        // (Burke, 2011)
        // (Campbell, 2007)

        updateDimensions();

    }

    private void updateDimensions() {
        l = rawImageArray.length;
        h = rawImageArray[0].length;
        w = rawImageArray[0][0].length;

    }

    // (Amarasinghe n.d.; Durovik n.d.)
    public void adjustBrightness(short amount) {
        for (int layer = 0; layer < l; ++layer) {
            for (int row = 0; row < h; ++row) {
                for (int col = 0; col < w; col++) {
                    rawImageArray[layer][row][col] += amount;

                    if (rawImageArray[layer][row][col] > 255) {
                        rawImageArray[layer][row][col] = (short) 255;
                    } else if (rawImageArray[layer][row][col] < 0) {
                        rawImageArray[layer][row][col] = (short) 0;
                    }

                }
            }
        }

        updateImage();
    }

    // (Amarasinghe n.d.; Durovic n.d.)
    public void quantization(short step_size) {
        for (layer = 0; layer < l; ++layer) {
            for (row = 0; row < h; ++row) {
                for (col = 0; col < w; col++) {
                    rawImageArray[layer][row][col] = (short) (rawImageArray[layer][row][col]
                            / step_size * step_size);
                }
            }
        }

        updateImage();
    }

    // (Amarasinghe n.d.;Durovic n.d.)
    public void rotate(double angle) {

        targetImageArray = getTargetImageArray(h, w);
        angle = Math.toRadians(angle);
        int x0 = w / 2;
        int y0 = h / 2;
        for (row = 0; row < h; ++row) {

            for (col = 0; col < w; col++) {
                row = y0 - row;
                col = x0 - col;

                // Following code uses Cartesian coordinates and rotates the
                // points
                // about the origin. However, by default, top right is the
                // origin.
                // Instead, we are temporarily changing the origin to the center
                // of the image perform the rotation and convert back to regular
                // coordinates.
                srcRow = (int) Math.round((col) * Math.sin(angle) + (row)
                        * Math.cos(angle));
                srcCol = (int) Math.round((col) * Math.cos(angle) - (row)
                        * Math.sin(angle));
                srcRow -= y0;
                srcCol -= x0;

                srcRow *= -1;
                srcCol *= -1;

                row -= y0;
                col -= x0;
                row *= -1;
                col *= -1;
                if (srcRow >= 0 && srcRow < h && srcCol >= 0 && srcCol < w) {
                    targetImageArray[0][row][col] = rawImageArray[0][srcRow][srcCol];
                    targetImageArray[1][row][col] = rawImageArray[1][srcRow][srcCol];
                    targetImageArray[2][row][col] = rawImageArray[2][srcRow][srcCol];
                } else {
                    targetImageArray[0][row][col] = targetImageArray[1][row][col] = targetImageArray[2][row][col] = 255;
                }
            }
        }

        rawImageArray = targetImageArray;

        updateImage();
    }

    // (Amarasinghe n.d.; Durovic n.d.)
    public void rotateIngterpolate(double angle) {
        angle = Math.toRadians(angle);

        double x, y;
        short row1WeightedAvg, row2WeigtedAvg;

        int xcenter = w / 2;
        int ycenter = h / 2;

        targetImageArray = getTargetImageArray(h, w);

        double tempSrcRow, tempSrcCol;

        for (row = 0; row < h; ++row) {
            NIMP.getInstance().getStatusBar().setProgress(row / (2 * l));

            for (col = 0; col < w; col++) {
                row = ycenter - row;
                col = xcenter - col;
                tempSrcRow = Math.round(col * Math.sin(angle) + row
                        * Math.cos(angle));
                tempSrcCol = Math.round(col * Math.cos(angle) - row
                        * Math.sin(angle));

                tempSrcRow -= ycenter;
                tempSrcCol -= xcenter;

                tempSrcRow *= -1;
                tempSrcCol *= -1;

                row -= ycenter;
                col -= xcenter;
                row *= -1;
                col *= -1;

                srcRow = (int) tempSrcRow;
                srcCol = (int) tempSrcCol;

                x = tempSrcCol - srcCol;
                y = tempSrcRow - srcRow;

                for (layer = 0; layer < l; ++layer) {
                    if (srcRow >= 0 && srcRow < h - 1 && srcCol >= 0
                            && srcCol < w - 1) {

                        row1WeightedAvg = getLinearInterpolate(x,
                                rawImageArray[layer][srcRow][srcCol],
                                rawImageArray[layer][srcRow][srcCol + 1]);
                        row2WeigtedAvg = getLinearInterpolate(x,
                                rawImageArray[layer][srcRow + 1][srcCol],
                                rawImageArray[layer][srcRow + 1][srcCol + 1]);

                        targetImageArray[layer][row][col] = getLinearInterpolate(
                                y, row1WeightedAvg, row2WeigtedAvg);

                    } else {
                        targetImageArray[layer][row][col] = 255;
                    }
                }
            }
        }

        rawImageArray = targetImageArray;

        updateImage();
    }

    // (Amarasinghe n.d.; Durovic n.d.)
    public void pixellate(int rStepSize, int cStepSize) {
        targetImageArray = getTargetImageArray(h, w);
        for (layer = 0; layer < l; ++layer) {
            for (row = 0; row < h; ++row) {
                srcRow = row / rStepSize * rStepSize;
                for (col = 0; col < w; col++) {
                    srcCol = col / cStepSize * cStepSize;
                    targetImageArray[layer][row][col] = rawImageArray[layer][srcRow][srcCol];
                }
            }
        }

        rawImageArray = targetImageArray;

        updateImage();
    }

    // (Amarasinghe n.d.; Durovic n.d.)
    public void enlarge(int horizontalPercentage, int verticalPercentage) {

        final int tw = w * horizontalPercentage;
        final int th = h * verticalPercentage;

        targetImageArray = getTargetImageArray(th, tw);

        int srcRow;
        int srcCol;

        for (row = 0; row < th; ++row) {
            NIMP.getInstance().getStatusBar().setProgress(row / (th));
            srcRow = row / verticalPercentage;
            for (col = 0; col < tw; ++col) {

                srcCol = col / horizontalPercentage;
                if (srcRow >= 0 && srcRow < h && srcCol >= 0 && srcCol < w) {
                    targetImageArray[0][row][col] = rawImageArray[0][srcRow][srcCol];
                    targetImageArray[1][row][col] = rawImageArray[1][srcRow][srcCol];
                    targetImageArray[2][row][col] = rawImageArray[2][srcRow][srcCol];
                } else {
                    targetImageArray[0][row][col] = targetImageArray[1][row][col] = targetImageArray[2][row][col] = 255;
                }
            }
        }

        rawImageArray = targetImageArray;

        updateDimensions();
        updateImage();
    }

    // (Amarasinghe n.d.; Durovic n.d.)
    public short getLinearInterpolate(double value, int pixelValue1,
            int pixelValue2) {

        return (short) (value * (pixelValue2 - pixelValue1) + pixelValue1);

    }

    // (Amarasinghe n.d.; Durovic n.d.)
    public void enlargeByLinearInterpolate(double horizontalPercentage,
            double verticalPercentage) {

        final int targetWidth = (int) (w * horizontalPercentage);
        final int targetHeight = (int) (h * verticalPercentage);

        double x, y; // Interpolation constants.
        short rVal1;
        short rVal2;
        targetImageArray = getTargetImageArray(targetHeight, targetWidth);

        int srcRow;
        int srcCol;

        h -= 1;
        w -= 1;
        for (layer = 0; layer < l; layer++) {
            for (row = 0; row < targetHeight; ++row) {
                NIMP.getInstance().getStatusBar().setProgress(row / (2 * l));
                srcRow = (int) (row / verticalPercentage);
                y = row / verticalPercentage - srcRow;
                for (col = 0; col < targetWidth; ++col) {

                    srcCol = (int) (col / horizontalPercentage);
                    x = col / horizontalPercentage - srcCol;
                    if (srcRow >= 0 && srcRow < h && srcCol >= 0 && srcCol < w) {
                        rVal1 = getLinearInterpolate(x,
                                rawImageArray[layer][srcRow][srcCol],
                                rawImageArray[layer][srcRow][srcCol + 1]);
                        rVal2 = getLinearInterpolate(x,
                                rawImageArray[layer][srcRow + 1][srcCol],
                                rawImageArray[layer][srcRow + 1][srcCol + 1]);

                        targetImageArray[layer][row][col] = getLinearInterpolate(
                                y, rVal1, rVal2);

                    } else {
                        targetImageArray[layer][row][col] = 255;
                    }
                }
            }
        }

        rawImageArray = targetImageArray;

        updateDimensions();
        updateImage();
    }

    private int blobCounter(int row, int col) {

        if (row > h || col > w) {
            return 0;
        }

        if (rawImageArray[0][row][col] == 255) {
            return 0;
        }

        rawImageArray[0][row][col] = 255;

        return 1 + blobCounter(row + 1, col + 1) + blobCounter(row - 1, col - 1)
                + blobCounter(row + 1, col)
                + blobCounter(row - 1, col)
                + blobCounter(row, col + 1)
                + blobCounter(row, col - 1)
                + blobCounter(row + 1, col - 1)
                + blobCounter(row - 1, col + 1);

    }

    public static enum FILTERS {

        AVERAGE_BOX, GAUSSIAN_BOX_1, GAUSSIAN_BOX_2, LAPLACEAN_LIGHT, LAPLACEAN_DARK, MEDIAN, MEDIAN_LOW, MEDIAN_HIGH, MODE
    }
    int j;
    private int temp;

    public void applyModeFilter() {
        int mode;
        int mcount;
        int count;
        final short[] temp = new short[9];

        targetImageArray = getTargetImageArray(h, w);

        final int temph = h - 2, tempw = w - 2;

        // (Amarasinghe n.d.; Durovic n.d.)
        for (layer = 0; layer < l; layer++) {
            for (row = 1; row < temph; row++) {
                for (col = 1; col < tempw; col++) {
                    count = 0;
                    for (i = -1; i < 2; i++) {
                        for (j = -1; j < 2; j++) {
                            temp[count++] = rawImageArray[layer][row + i][col
                                    + j];
                        }
                    }

                    Arrays.sort(temp);

                    mode = rawImageArray[layer][row][col];
                    count = 1;
                    mcount = 1;
                    for (i = 1; i < 9; i++) {
                        if (temp[i] == temp[i - 1]) {
                            mcount++;
                        } else {
                            mcount = 0;
                        }
                        if (mcount > count) {
                            count = mcount;
                            mode = temp[i];
                        }

                    }

                    targetImageArray[layer][row][col] = (short) mode;

                }
            }
        }
        rawImageArray = targetImageArray;
        updateImage();
    }

    // (Amarasinghe n.d.; Durovic n.d.)
    public static class MASKS {

        public static short[][] AVERAGE_BOX = new short[][]{{1, 1, 1},
            {1, 1, 1},
            {1, 1, 1}};
        public static short[][] GAUSSIAN_BOX_1 = new short[][]{{1, 2, 1},
            {2, 4, 2},
            {1, 2, 1}};
        public static short[][] GAUSSIAN_BOX_2 = new short[][]{{0, 1, 0},
            {1, 2, 1},
            {0, 1, 0}};
        public static short[][] LAPLACEAN_DARK = new short[][]{{0, 1, 0},
            {1, -4, 1},
            {0, 1, 0}};
        public static short[][] LAPLACEAN_LIGHT = new short[][]{{0, -1, 0},
            {-1, 4, -1},
            {0, -1, 0}};
        public static short[][] SOBEL_X_MASK = new short[][]{{1, 0, -1},
            {2, 0, -2},
            {1, 0, -1}};
        public static short[][] SOBEL_Y_MASK = new short[][]{{1, 2, 1},
            {0, 0, 0},
            {-1, -2, -1}};
    }

    public void applyUnweightedMaskAndShowRaw(short[][] mask) {
        rawImageArray = applyMask(rawImageArray, mask);

        for (layer = 0; layer < l; layer++) {
            for (row = 0; row < h; row++) {
                NIMP.getInstance().getStatusBar()
                        .setProgress(layer * row / (l * h));
                for (col = 0; col < w; col++) {

                    if (rawImageArray[layer][row][col] > 255) {
                        rawImageArray[layer][row][col] = 255;
                    } else if (rawImageArray[layer][row][col] < 0) {
                        rawImageArray[layer][row][col] = 0;
                    }

                }
            }
        }
        updateImage();
    }

    // spie.org/samples/TT92.pdf
    public void enhanceContrastNaively(int scale) {
        temp = 255 / scale;
        for (layer = 0; layer < l; layer++) {
            for (row = 0; row < h; row++) {
                for (col = 0; col < w; col++) {
                    if (rawImageArray[layer][row][col] < temp) {
                        rawImageArray[layer][row][col] *= scale;
                    } else {
                        rawImageArray[layer][row][col] = 255;
                    }
                }
            }
        }

        updateImage();
    }

    // (Amarasinghe n.d.; Durovic n.d.)
    public void enhanceContrastUsingHistogramEqualization() {
        final int[][] histogram = generateRGBHistogram(rawImageArray);
        final int N = w * h;

        // (Amarasinghe n.d.; Durovic n.d.)
        // Convert histogram to show cumulative values.
        // Gtg/N -1

        int temp;
        for (layer = 0; layer < l; layer++) {
            temp = 0;
            for (int i = 0; i < colorDepth; i++) {

                temp += histogram[layer][i];
                histogram[layer][i] = temp;

            }
        }
        for (layer = 0; layer < l; layer++) {
            for (row = 0; row < h; row++) {
                for (col = 0; col < w; col++) {

                    rawImageArray[layer][row][col] = (short) (colorDepth
                            * histogram[layer][rawImageArray[layer][row][col]]
                            / N - 1);

                    if (rawImageArray[layer][row][col] < 0) {
                        rawImageArray[layer][row][col] = 0;
                    }

                }
            }
        }
        updateImage();
    }

    // (Amarasinghe n.d.; Durovic n.d.)
    public void enhanceContrastByStretch(int newMin, int newMax) {

        int curMax = 0, curMin = 0, factor;

        // spie.org/samples/TT92.pdf

        for (layer = 0; layer < l; layer++) {
            curMax = curMin = rawImageArray[layer][0][0];
            for (row = 0; row < h && curMin >= 0 && curMax <= 255; row++) {
                for (col = 0; col < w; col++) {
                    if (rawImageArray[layer][row][col] > curMax) {
                        curMax = rawImageArray[layer][row][col];
                    }

                    if (rawImageArray[layer][row][col] < curMin) {
                        curMin = rawImageArray[layer][row][col];
                    }
                }
            }

            factor = (newMax - newMin) / (curMax - curMin);
            for (row = 0; row < h; row++) {
                for (col = 0; col < w; col++) {
                    rawImageArray[layer][row][col] = (short) (factor
                            * (rawImageArray[layer][row][col] - curMin) + newMin);
                }
            }
        }

        updateImage();
    }

    public void enhanceContrastByStretch() {
        enhanceContrastByStretch(0, 255);
    }

    public int[][] generateRGBHistogram(short[][][] image) {
        // (Amarasinghe n.d.; Durovic n.d.)
        final int[][] histogram = new int[3][];

        final int l = image.length;

        for (layer = 0; layer < l; layer++) {
            histogram[layer] = generateHistogram(image[layer]);
        }

        return histogram;

    }

    public int[] generateHistogram(short[][] image) {
        // (Amarasinghe n.d.; Durovic n.d.)
        final int[] histogram = new int[colorDepth];
        final int h = image.length;
        final int w = image[0].length;

        for (row = 0; row < h; row++) {
            for (col = 0; col < w; col++) {
                histogram[image[row][col]]++;
            }
        }

        return histogram;

    }

    public void applyFilter(PixelImage.FILTERS filter) {
        switch (filter) {
            case AVERAGE_BOX:
                targetImageArray = applyWeightedMask(rawImageArray,
                        MASKS.AVERAGE_BOX);
                break;
            case GAUSSIAN_BOX_1:
                targetImageArray = applyWeightedMask(rawImageArray,
                        MASKS.GAUSSIAN_BOX_1);
                break;
            case GAUSSIAN_BOX_2:
                targetImageArray = applyWeightedMask(rawImageArray,
                        MASKS.GAUSSIAN_BOX_2);
                break;
            case LAPLACEAN_DARK:
                targetImageArray = applyMask(rawImageArray,
                        MASKS.LAPLACEAN_DARK);
                // (Amarasinghe n.d.; Durovic n.d.)
                for (layer = 0; layer < l; layer++) {
                    for (row = 0; row < h; row++) {
                        NIMP.getInstance().getStatusBar()
                                .setProgress(layer * row / (l * h));
                        for (col = 0; col < w; col++) {

                            targetImageArray[layer][row][col] = (short) (rawImageArray[layer][row][col] - targetImageArray[layer][row][col]);
                            if (targetImageArray[layer][row][col] > 255) {
                                targetImageArray[layer][row][col] = 255;
                            } else if (targetImageArray[layer][row][col] < 0) {
                                targetImageArray[layer][row][col] = 0;
                            }

                        }
                    }
                }

                break;
            case LAPLACEAN_LIGHT:

                targetImageArray = applyMask(rawImageArray,
                        MASKS.LAPLACEAN_LIGHT);
                // (Amarasinghe n.d.; Durovic n.d.)
                for (layer = 0; layer < l; layer++) {
                    for (row = 1; row < h; row++) {
                        NIMP.getInstance().getStatusBar()
                                .setProgress(layer * row / (l * h));
                        for (col = 1; col < w; col++) {

                            targetImageArray[layer][row][col] = (short) (rawImageArray[layer][row][col] - targetImageArray[layer][row][col]);
                            if (targetImageArray[layer][row][col] > 255) {
                                targetImageArray[layer][row][col] = 255;
                            } else if (targetImageArray[layer][row][col] < 0) {
                                targetImageArray[layer][row][col] = 0;
                            }

                        }
                    }
                }
                break;
            case MEDIAN:
            case MEDIAN_HIGH:
            case MEDIAN_LOW:
                applyMedianFilter(filter);
                break;
            case MODE:
                applyModeFilter();
                break;

        }

        rawImageArray = targetImageArray;

        updateImage();
    }

    public void applyMedianFilter(PixelImage.FILTERS filter) {
        int count;
        final int l = rawImageArray.length;
        final int h = rawImageArray[0].length - 2;
        final int w = rawImageArray[0][0].length - 2;
        final short[] temp = new short[9];

        int position = 0;

        switch (filter) {
            case MEDIAN:
                position = 4;
                break;
            case MEDIAN_HIGH:
                position = 8;
                break;
            case MEDIAN_LOW:
                position = 0;
                break;
            default:
                assert false; // We shouldn't come here.
        }

        targetImageArray = getTargetImageArray(h + 2, w + 2);

        // (Amarasinghe n.d.; Durovic n.d.)
        for (int layer = 0; layer < l; layer++) {
            for (int row = 1; row < h; row++) {
                NIMP.getInstance().getStatusBar()
                        .setProgress(layer * row / (l * h));
                for (int col = 1; col < w; col++) {
                    count = 0;

                    for (int i = -1; i < 2; i++) {
                        for (int j = -1; j < 2; j++, count++) {
                            temp[count] = rawImageArray[layer][row + i][col + j];
                        }
                    }

                    Arrays.sort(temp);
                    targetImageArray[layer][row][col] = temp[position];

                }
            }
        }

        rawImageArray = targetImageArray;
        updateImage();

    }

    public short[][][] applyMask(short[][][] sourceImage, short[][] mask) {
        int total;

        final int h = sourceImage[0].length - 2;
        final int w = sourceImage[0][0].length - 2;
        final short[][][] result = new short[l][h + 2][w + 2];

        // (Amarasinghe n.d.; Durovic n.d.)
        for (layer = 0; layer < l; layer++) {
            for (row = 1; row < h; row++) {
                NIMP.getInstance().getStatusBar()
                        .setProgress(layer * row / (l * h));
                for (col = 1; col < w; col++) {
                    total = 0;

                    for (i = -1; i < 2; i++) {
                        for (j = -1; j < 2; j++) {
                            total += mask[i + 1][j + 1]
                                    * sourceImage[layer][row + i][col + j];
                        }
                    }

                    result[layer][row][col] = (short) total;

                }
            }
        }

        return result;
    }

    public void applyKValueFilter(int k) {
        // (Amarasinghe n.d.; Durovic n.d.)
        int total;

        final int h = rawImageArray[0].length - 2 * k;
        final int w = rawImageArray[0][0].length - 2 * k;

        targetImageArray = getTargetImageArray(h + 2 * k, w + 2 * k);

        final int avg = (int) Math.pow(2 * k + 1, 2);

        for (layer = 0; layer < l; layer++) {
            for (row = k; row < h; row++) {
                NIMP.getInstance().getStatusBar()
                        .setProgress(layer * row / (l * h));
                for (col = k; col < w; col++) {
                    total = 0;

                    for (i = -k; i <= k; i++) {
                        for (j = -k; j <= k; j++) {
                            total += rawImageArray[layer][row + i][col + j];
                        }
                    }

                    total /= avg;

                    targetImageArray[layer][row][col] = (short) total;

                }
            }
        }

        rawImageArray = targetImageArray;
        updateImage();
    }

    public short[][][] applyWeightedMask(short[][][] sourceImage, short[][] mask) {
        int total;

        final int h = sourceImage[0].length - 2;
        final int w = sourceImage[0][0].length - 2;

        int avg = 0;

        for (short[] aMask : mask) {
            for (int j = 0; j < mask[0].length; j++) {
                avg += aMask[j];
            }
        }

        final short[][][] result = new short[l][h + 2][w + 2];
        // (Amarasinghe n.d.; Durovic n.d.)
        for (layer = 0; layer < l; layer++) {
            for (row = 1; row < h; row++) {
                NIMP.getInstance().getStatusBar()
                        .setProgress(layer * row / (l * h));
                for (col = 1; col < w; col++) {
                    total = 0;

                    for (i = -1; i < 2; i++) {
                        for (j = -1; j < 2; j++) {
                            total += mask[i + 1][j + 1]
                                    * sourceImage[layer][row + i][col + j];
                        }
                    }

                    total /= avg;

                    if (total > 255) {
                        total = 255;
                    } else if (total < 0) {
                        total = 0;
                    }

                    result[layer][row][col] = (short) total;

                }
            }
        }

        return result;
    }
    int i;

    // (Amarasinghe n.d.; Durovic n.d.)
    public void negate() {

        List<Integer> counts = new ArrayList<Integer>();
        for (row = 0; row < h; ++row) {
            for (col = 0; col < w; col++) {
                if (rawImageArray[0][row][col] == 0) {
                    counts.add(blobCounter(row, col));
                    
                }
            }
        }

        
        counts.add(50);
        counts.add(60);
        counts.add(10);
        counts.add(20);
        Collections.sort(counts);
        
        
        int count =counts.size();
        
        

        for(int i=0; i<counts.size();i++)
            if(counts.get(counts.size()-1)/5>counts.get(i))
                count--;

        System.out.println(count);
    }

    public void applyUnsharpMasking() {

        final short[][][] backup = cloneArray(rawImageArray);
        applyFilter(FILTERS.MEDIAN);

        // (Amarasinghe n.d.; Durovic n.d.)
        for (layer = 0; layer < l; ++layer) {
            for (row = 0; row < h; ++row) {
                for (col = 0; col < w; col++) {
                    backup[layer][row][col] += (short) (backup[layer][row][col] - rawImageArray[layer][row][col]);
                    backup[layer][row][col] = backup[layer][row][col] > 255 ? 255
                            : backup[layer][row][col] < 0 ? 0
                            : backup[layer][row][col];

                }
            }
        }

        rawImageArray = backup;

        updateImage();
    }

    private short[][][] cloneArray(short[][][] original) {
        final int l = original.length;
        final int h = original[0].length;
        final int w = original[0][0].length;

        final short[][][] clone = new short[l][h][w];

        for (layer = 0; layer < l; layer++) {
            for (row = 0; row < h; row++) {
                for (col = 0; col < w; col++) {
                    clone[layer][row][col] = original[layer][row][col];
                }
            }
        }

        return clone;
    }

    // (Amarasinghe n.d.; Durovic n.d.)
    public void applySobelOperator() {

        final short[][][] xImage = applyMask(rawImageArray, MASKS.SOBEL_X_MASK);
        final short[][][] yImage = applyMask(rawImageArray, MASKS.SOBEL_Y_MASK);

        for (layer = 0; layer < l; ++layer) {
            for (row = 0; row < h; ++row) {
                for (col = 0; col < w; col++) {
                    rawImageArray[layer][row][col] += (short) Math.sqrt(Math
                            .pow(xImage[layer][row][col], 2)
                            + Math.pow(yImage[layer][row][col], 2));

                    rawImageArray[layer][row][col] = rawImageArray[layer][row][col] > 255 ? 255
                            : rawImageArray[layer][row][col] < 0 ? 0
                            : rawImageArray[layer][row][col];

                }
            }
        }

        updateImage();

    }

    public void prepareUndo() {
        undoable = true;
        snapshot = cloneArray(rawImageArray);

    }

    public void undo() {
        if (!undoable) {
            throw new UnsupportedOperationException("Cannot undo");
        }
        rawImageArray = cloneArray(snapshot);
        updateImage();

    }

    // (Amarasinghe n.d.; Durovic n.d.)
    public void applySobelOperatorOnly() {
        final short[][][] xImage = applyMask(rawImageArray, MASKS.SOBEL_X_MASK);

        final short[][][] yImage = applyMask(rawImageArray, MASKS.SOBEL_Y_MASK);

        for (layer = 0; layer < l; ++layer) {
            for (row = 0; row < h; ++row) {
                for (col = 0; col < w; col++) {
                    rawImageArray[layer][row][col] = (short) Math.sqrt(Math
                            .pow(xImage[layer][row][col], 2)
                            + Math.pow(yImage[layer][row][col], 2));

                    rawImageArray[layer][row][col] = rawImageArray[layer][row][col] > 255 ? 255
                            : rawImageArray[layer][row][col] < 0 ? 0
                            : rawImageArray[layer][row][col];

                }
            }
        }

        updateImage();

    }

    // (Amarasinghe n.d.; Durovic n.d.)
    public void convertToPencilSketchUsingSobel(short lightestShade) {
        convertToGrayScaleUsingAveraging();
        applyFilter(FILTERS.MEDIAN);

        final short[][][] xImage = applyMask(rawImageArray, MASKS.SOBEL_X_MASK);
        final short[][][] yImage = applyMask(rawImageArray, MASKS.SOBEL_Y_MASK);

        for (layer = 0; layer < l; ++layer) {

            for (row = 0; row < h; ++row) {
                for (col = 0; col < w; col++) {
                    rawImageArray[layer][row][col] = (short) Math.sqrt(Math
                            .pow(xImage[layer][row][col], 2)
                            + Math.pow(yImage[layer][row][col], 2));

                    if (rawImageArray[layer][row][col] < 4) {
                        rawImageArray[layer][row][col] = 0;
                    }
                    rawImageArray[layer][row][col] = pencilEffectHelperClamper(
                            lightestShade, rawImageArray[layer][row][col]);

                }
            }
        }

        applyFilter(FILTERS.MEDIAN);

        updateImage();

    }

    // (Amarasinghe n.d.; Durovic n.d.)
    public void convertToPencilSketchUsingJinZhou(short lightestShade) {

        // This algorithm is adapted from
        // (Zhou and Li, 2005, pp. 1026--1029)
        convertToGrayScaleUsingAveraging();
        applyFilter(FILTERS.MEDIAN);

        targetImageArray = getTargetImageArray(h, w);

        for (layer = 0; layer < l; ++layer) {
            for (row = 0; row < h; ++row) {
                for (col = 0; col < w; col++) {

                    // Find the maxima to the left

                    int maxg = 0;
                    int maxrow = 0;

                    for (i = row - 1; i >= 0; i--) {
                        int g = (rawImageArray[layer][row][col] - rawImageArray[layer][i][col])
                                / (row - i);

                        if (Math.abs(g) > Math.abs(maxg)) {
                            maxg = g;
                        } else {
                            break;
                        }

                    }

                    maxrow += maxg;

                    // To the right
                    maxg = 0;

                    for (i = row + 1; i < h; i++) {
                        int g = (rawImageArray[layer][row][col] - rawImageArray[layer][i][col])
                                / (row - i);

                        if (Math.abs(g) > Math.abs(maxg)) {
                            maxg = g;
                        } else {
                            break;
                        }
                    }

                    maxrow += maxg;

                    int maxcol = 0;

                    maxg = 0;
                    for (i = col - 1; i >= 0; i--) {
                        int g = (rawImageArray[layer][row][col] - rawImageArray[layer][row][i])
                                / (col - i);

                        if (Math.abs(g) > Math.abs(maxg)) {
                            maxg = g;
                        } else {
                            break;
                        }
                    }
                    maxcol += maxg;

                    maxg = 0;
                    for (i = col + 1; i < w; i++) {
                        int g = (rawImageArray[layer][row][col] - rawImageArray[layer][row][i])
                                / (col - i);

                        if (Math.abs(g) > Math.abs(maxg)) {
                            maxg = g;
                        } else {
                            break;
                        }
                    }
                    maxcol += maxg;

                    targetImageArray[layer][row][col] = (short) ((Math
                            .abs(maxrow) >= Math.abs(maxcol)) ? maxrow : maxcol);

                    if (targetImageArray[layer][row][col] < 0) {
                        targetImageArray[layer][row][col] = (short) Math
                                .abs(targetImageArray[layer][row][col]);
                    }

                    if (targetImageArray[layer][row][col] > 255) {
                        targetImageArray[layer][row][col] = 255;
                    }

                    if (targetImageArray[layer][row][col] < 4) {
                        targetImageArray[layer][row][col] = 0;
                    }
                    targetImageArray[layer][row][col] = pencilEffectHelperClamper(
                            lightestShade, targetImageArray[layer][row][col]);

                }
            }
        }

        rawImageArray = targetImageArray;

        applyFilter(FILTERS.MEDIAN);

        updateImage();

    }

    // (Amarasinghe n.d.; Durovic n.d.)
    public short getOtsuThreshold(short[][] image) {
        // (Greensted 2010)
        //

        short threshold;
        int[] histogram = generateHistogram(image);

        // Total number of pixels
        int totalPixels = image.length * image[0].length;

        float weight = 0;
        for (int t = 0; t < colorDepth; t++) {
            weight += t * histogram[t];
        }

        float sumB = 0;
        int weightBackground = 0;
        int weightForeground = 0;

        float varMax = 0;
        threshold = 0;

        for (int t = 0; t < colorDepth; t++) {
            weightBackground += histogram[t]; // Weight Background
            if (weightBackground == 0) {
                continue;
            }

            weightForeground = totalPixels - weightBackground; // Weight
            // Foreground
            if (weightForeground == 0) {
                break;
            }

            sumB += t * histogram[t];

            float mB = sumB / weightBackground; // Mean Background
            float mF = (weight - sumB) / weightForeground; // Mean Foreground

            // Calculate Between Class Variance
            float varBetween = (float) weightBackground
                    * (float) weightForeground * (mB - mF) * (mB - mF);

            // Check if new maximum found
            if (varBetween > varMax) {
                varMax = varBetween;
                threshold = (short) t;
            }
        }
        return threshold;
    }

    // (Amarasinghe n.d.; Durovic n.d.)
    public void convertToPencilSketchUsingLaplaceanDark(short lightestShade) {
        convertToGrayScaleUsingAveraging();
        applyFilter(FILTERS.MEDIAN);

        rawImageArray = applyMask(rawImageArray, MASKS.LAPLACEAN_DARK);

        for (layer = 0; layer < l; ++layer) {
            for (row = 0; row < h; ++row) {
                for (col = 0; col < w; col++) {

                    if (rawImageArray[layer][row][col] > 0) {
                        rawImageArray[layer][row][col] = 0;
                    } else {
                        rawImageArray[layer][row][col] *= -1;
                    }
                    rawImageArray[layer][row][col] = pencilEffectHelperClamper(
                            lightestShade, rawImageArray[layer][row][col]);

                }
            }
        }

        applyFilter(FILTERS.MEDIAN);

        updateImage();

    }

    // (Amarasinghe n.d.; Durovic n.d.)
    public void convertToPencilSketchUsingLaplaceanLight(short lightestShade) {
        convertToGrayScaleUsingAveraging();
        applyFilter(FILTERS.MEDIAN);

        rawImageArray = applyMask(rawImageArray, MASKS.LAPLACEAN_DARK);

        for (layer = 0; layer < l; ++layer) {
            for (row = 0; row < h; ++row) {
                for (col = 0; col < w; col++) {

                    if (rawImageArray[layer][row][col] > 0) {
                        rawImageArray[layer][row][col] = 0;
                    } else {
                        rawImageArray[layer][row][col] *= -1;
                    }
                    rawImageArray[layer][row][col] = pencilEffectHelperClamper(
                            (short) 120, rawImageArray[layer][row][col]);

                }
            }
        }

        applyFilter(FILTERS.MEDIAN);

        updateImage();
    }

    private short pencilEffectHelperClamper(short lightestShade, short pixel) {
        if (pixel > 0) {
            pixel = (short) (lightestShade - pixel);
        } else {
            pixel = 255;
        }

        return pixel > 255 ? 255 : pixel < 0 ? 0 : pixel;

    }

    public void convertToGrayScaleUsingAveraging() {
        for (int row = 0; row < h; ++row) {
            for (int col = 0; col < w; col++) {
                final short avg = (short) ((rawImageArray[0][row][col]
                        + rawImageArray[1][row][col] + rawImageArray[2][row][col]) / 3);
                rawImageArray[0][row][col] = rawImageArray[1][row][col] = rawImageArray[2][row][col] = avg;
            }
        }

        updateImage();
    }

    public void convertToGrayScaleUsingLuminescence() {
        // (Stokes and Anderson et al., 1996)
        // (Cook 2009)
        for (row = 0; row < h; ++row) {
            for (col = 0; col < w; col++) {

                final short avg = (short) (rawImageArray[0][row][col] * 0.21
                        + rawImageArray[1][row][col] * 0.71 + rawImageArray[2][row][col] * 0.07);
                rawImageArray[0][row][col] = rawImageArray[1][row][col] = rawImageArray[2][row][col] = avg;
            }
        }

        updateImage();
    }

    public void fade(double balance, PixelImage image) {
        short[][][] secondImage = image.rawImageArray;

        targetImageArray = getTargetImageArray(h, w);

        int h = secondImage[0].length;
        int w = secondImage[0][0].length;

        int tvoffset = (this.h - h) / 2;
        int thoffset = (this.w - w) / 2;

        targetImageArray = cloneArray(rawImageArray);

        for (layer = 0; layer < l; layer++) {
            for (row = 0; row < h; ++row) {
                for (col = 0; col < w; col++) {

                    // (Amarasinghe n.d.; Durovic n.d.)
                    targetImageArray[layer][row + tvoffset][col + thoffset] = getLinearInterpolate(
                            balance, rawImageArray[layer][row + tvoffset][col
                            + thoffset], secondImage[layer][row][col]);
                }
            }
        }

        rawImageArray = targetImageArray;

        updateImage();

    }

    // (Amarasinghe n.d.; Durovic n.d.)
    public void convertToBWusing128() {
        convertToGrayScaleUsingAveraging();
        threshold((short) 128);
        updateImage();
    }

    public void thresholdUsing128() {

        for (layer = 0; layer < l; layer++) {
            thresholdLayer(layer, (short) 128);
        }
        updateImage();
    }

    public void thresholdUsingOtsu() {
        for (layer = 0; layer < l; layer++) {
            thresholdLayer(layer, getOtsuThreshold(rawImageArray[layer]));
        }
        updateImage();
    }

    public void convertToBWusingOtsu() {
        convertToGrayScaleUsingAveraging();

        threshold(getOtsuThreshold(rawImageArray[0]));

        updateImage();
    }

    private void threshold(short threshold) {
        for (layer = 0; layer < l; layer++) {
            for (row = 0; row < h; ++row) {
                for (col = 0; col < w; col++) {
                    rawImageArray[layer][row][col] = (short) (rawImageArray[layer][row][col] > threshold ? 255
                            : 0);

                }
            }
        }
    }

    private void thresholdLayer(int layer, short threshold) {

        for (row = 0; row < h; ++row) {
            for (col = 0; col < w; col++) {// (Amarasinghe n.d.; Durovic n.d.)
                rawImageArray[layer][row][col] = (short) (rawImageArray[layer][row][col] > threshold ? 255
                        : 0);

            }
        }
    }

    // (Amarasinghe n.d.; Durovic n.d.)
    public void translate(int vertical, int horizontal) {
        targetImageArray = getTargetImageArray(h, w);
        for (layer = 0; layer < l; layer++) {
            for (row = 0; row < h; row++) {
                srcRow = row - vertical;
                for (col = 0; col < w; col++) {
                    srcCol = col - horizontal;

                    if (srcCol >= 0 && srcCol < w && srcRow >= 0 && srcRow < h) {
                        targetImageArray[layer][row][col] = rawImageArray[layer][srcRow][srcCol];
                    } else {
                        targetImageArray[layer][row][col] = 255;
                    }
                }
            }
        }

        rawImageArray = targetImageArray;

        updateImage();

    }

    public short[][][] getTargetImageArray(int h, int w) {
        if (targetImageArray == null || targetImageArray[0].length != h
                || targetImageArray[0][0].length != w) {
            targetImageArray = new short[3][h][w];
        }

        return targetImageArray;

    }

    // (Amarasinghe n.d.; Durovic n.d.)
    public void crop(int x, int y, int height, int width) {

        // Reinitialize targetImageArray since it is liked to be larger.
        targetImageArray = getTargetImageArray(height, width);
        for (layer = 0; layer < l; layer++) {
            for (row = y, i = 0; i < height; row++, i++) {
                for (col = x, j = 0; j < width; col++, j++) {
                    targetImageArray[layer][i][j] = rawImageArray[layer][row][col];
                }
            }
        }

        rawImageArray = targetImageArray;
        updateDimensions();
        updateImage();
    }

    public void applyFishEyeWarp(double factor) {
        targetImageArray = getTargetImageArray(h, w);

        // (Gribbon and Johnston et al., 2003, pp. 408--413)
        // (stackoverflow 2012)
        // (Bourke 2002)
        // (Vass and Perlaki, 2003, pp. 9--16)
        // (Hughes and Glavin et al., 2008)

        double x0 = w / 2;
        double y0 = h / 2;
        double x, y, src, angle;
        int maxW = w;
        int maxH = h;
        int minW = 0;
        int minH = 0;
        double cvalue, rvalue;

        if (factor < 0.7 && factor > 0.45) {

            maxW = (int) (w - (0.7 - factor) * w);
            maxH = (int) (h - (0.70 - factor) * h);
            minH = (int) ((0.70 - factor) * h);
            minW = (int) ((0.70 - factor) * w);
        } else if (factor < 0.46 && factor > 0.38) {

            maxW = (int) (w - (0.65 - factor) * w);
            maxH = (int) (h - (0.65 - factor) * h);
            minH = (int) ((0.65 - factor) * h);
            minW = (int) ((0.65 - factor) * w);
        } else if (factor < 0.39 && factor > 0.36) {

            maxW = (int) (w - (0.6 - factor) * w);
            maxH = (int) (h - (0.6 - factor) * h);
            minH = (int) ((0.6 - factor) * h);
            minW = (int) ((0.6 - factor) * w);
        } else if (factor < 0.37 && factor > 0.32) {

            maxW = (int) (w - (0.63 - factor) * w);
            maxH = (int) (h - (0.63 - factor) * h);
            minH = (int) ((0.63 - factor) * h);
            minW = (int) ((0.63 - factor) * w);
        } else if (factor < 0.33 && factor > 0.29) {

            maxW = (int) (w - (0.61 - factor) * w);
            maxH = (int) (h - (0.61 - factor) * h);
            minH = (int) ((0.61 - factor) * h);
            minW = (int) ((0.61 - factor) * w);
        }
        System.out.println(factor);

        for (row = 0; row < h; row++) {
            y = -(row - y0) / (h / 2);
            for (col = 0; col < w; col++) {
                if (row >= maxH || col >= maxW || row <= minH || col <= minW) {
                    targetImageArray[0][row][col] = targetImageArray[1][row][col] = targetImageArray[2][row][col] = 255;
                    continue;
                }

                x = -(col - x0) / (w / 2);

                angle = Math.atan2(y, x);

                src = factor * Math.tan(Math.sqrt(x * x + y * y) / factor);

                rvalue = (src * -Math.sin(angle) * (h / 2) + y0);
                cvalue = (src * -Math.cos(angle) * (w / 2) + x0);

                srcCol = (int) cvalue;
                srcRow = (int) rvalue;

                rvalue -= srcRow;
                cvalue -= srcCol;
                int r1temp;
                int r2temp;
                if (srcCol >= 0 && srcCol < w - 1 && srcRow >= 0
                        && srcRow < h - 1) {
                    r1temp = getLinearInterpolate(cvalue,
                            rawImageArray[0][srcRow][srcCol],
                            rawImageArray[0][srcRow][srcCol + 1]);
                    r2temp = getLinearInterpolate(cvalue,
                            rawImageArray[0][srcRow + 1][srcCol],
                            rawImageArray[0][srcRow + 1][srcCol + 1]);
                    targetImageArray[0][row][col] = getLinearInterpolate(
                            rvalue, r1temp, r2temp);

                    r1temp = getLinearInterpolate(cvalue,
                            rawImageArray[1][srcRow][srcCol],
                            rawImageArray[1][srcRow][srcCol + 1]);
                    r2temp = getLinearInterpolate(cvalue,
                            rawImageArray[1][srcRow + 1][srcCol],
                            rawImageArray[1][srcRow + 1][srcCol + 1]);
                    targetImageArray[1][row][col] = getLinearInterpolate(
                            rvalue, r1temp, r2temp);

                    r1temp = getLinearInterpolate(cvalue,
                            rawImageArray[2][srcRow][srcCol],
                            rawImageArray[2][srcRow][srcCol + 1]);
                    r2temp = getLinearInterpolate(cvalue,
                            rawImageArray[2][srcRow + 1][srcCol],
                            rawImageArray[2][srcRow + 1][srcCol + 1]);
                    targetImageArray[2][row][col] = getLinearInterpolate(
                            rvalue, r1temp, r2temp);

                } else if (srcRow == h - 1 && srcCol == w - 1) {
                    targetImageArray[0][row][col] = rawImageArray[0][srcRow][srcCol];
                    targetImageArray[1][row][col] = rawImageArray[1][srcRow][srcCol];
                    targetImageArray[2][row][col] = rawImageArray[2][srcRow][srcCol];
                } else {
                    targetImageArray[0][row][col] = targetImageArray[1][row][col] = targetImageArray[2][row][col] = 255;
                }
            }
        }
        rawImageArray = targetImageArray;
        updateImage();

    }

    public void applyTwirlWarp(double factor) {
        // (The Supercomputing Blog, n.d.)
        double angle = Math.toRadians(1440);
        double x, y, tempangle;
        short rVal1, rVal2;

        int x0 = w / 2;
        int y0 = h / 2;

        targetImageArray = getTargetImageArray(h, w);

        double tempSrcRow, tempSrcCol;

        for (int row = 0; row < h; ++row) {
            NIMP.getInstance().getStatusBar().setProgress(row / (2 * l));

            for (int col = 0; col < w; col++) {

                row = y0 - row;
                col = x0 - col;

                double length = Math.sqrt(row * row / ((double) h * h) + col
                        * col / ((double) w * w));

                tempangle = angle * length * factor;

                tempSrcRow = Math.round(col * Math.sin(tempangle) + row
                        * Math.cos(tempangle));
                tempSrcCol = Math.round(col * Math.cos(tempangle) - row
                        * Math.sin(tempangle));

                tempSrcRow -= y0;
                tempSrcCol -= x0;

                tempSrcRow *= -1;
                tempSrcCol *= -1;

                row -= y0;
                col -= x0;
                row *= -1;
                col *= -1;

                srcRow = (int) tempSrcRow;
                srcCol = (int) tempSrcCol;

                x = tempSrcCol - srcCol;
                y = tempSrcRow - srcRow;

                for (layer = 0; layer < l; ++layer) {
                    if (srcRow >= 0 && srcRow < h - 1 && srcCol >= 0
                            && srcCol < w - 1) {

                        rVal1 = getLinearInterpolate(x,
                                rawImageArray[layer][srcRow][srcCol],
                                rawImageArray[layer][srcRow][srcCol + 1]);
                        rVal2 = getLinearInterpolate(x,
                                rawImageArray[layer][srcRow + 1][srcCol],
                                rawImageArray[layer][srcRow + 1][srcCol + 1]);

                        targetImageArray[layer][row][col] = getLinearInterpolate(
                                y, rVal1, rVal2);

                    } else {
                        targetImageArray[layer][row][col] = rawImageArray[layer][row][col];
                    }
                }
            }
        }

        rawImageArray = targetImageArray;

        updateImage();

    }

    public void applyBulgeWarp(double factor) {
        targetImageArray = getTargetImageArray(h, w);

        // (Gribbon and Johnston et al., 2003, pp. 408--413)
        // (stackoverflow 2012)
        // (Bourke 2002)
        // (Vass and Perlaki, 2003, pp. 9--16)
        // (Hughes and Glavin et al., 2008)
        // (StackExchange 2013)
        // (stackoverflow 2012)

        double x0 = w / 2;
        double y0 = h / 2;
        double x, y, src, angle;

        double cvalue, rvalue;

        for (row = 0; row < h; row++) {
            y = -(row - y0) / (h / 2);
            for (col = 0; col < w; col++) {

                x = -(col - x0) / (w / 2);

                angle = Math.atan2(y, x);

                src = Math.sqrt(x * x + y * y);

                src = Math.pow(src, factor);// / 0.25;

                rvalue = (src * -Math.sin(angle) * (h / 2) + y0);
                cvalue = (src * -Math.cos(angle) * (w / 2) + x0);

                srcCol = (int) cvalue;
                srcRow = (int) rvalue;

                rvalue -= srcRow;
                cvalue -= srcCol;
                int r1temp;
                int r2temp;
                if (srcCol >= 0 && srcCol < w - 1 && srcRow >= 0
                        && srcRow < h - 1) {
                    r1temp = getLinearInterpolate(cvalue,
                            rawImageArray[0][srcRow][srcCol],
                            rawImageArray[0][srcRow][srcCol + 1]);
                    r2temp = getLinearInterpolate(cvalue,
                            rawImageArray[0][srcRow + 1][srcCol],
                            rawImageArray[0][srcRow + 1][srcCol + 1]);
                    targetImageArray[0][row][col] = getLinearInterpolate(
                            rvalue, r1temp, r2temp);

                    r1temp = getLinearInterpolate(cvalue,
                            rawImageArray[1][srcRow][srcCol],
                            rawImageArray[1][srcRow][srcCol + 1]);
                    r2temp = getLinearInterpolate(cvalue,
                            rawImageArray[1][srcRow + 1][srcCol],
                            rawImageArray[1][srcRow + 1][srcCol + 1]);
                    targetImageArray[1][row][col] = getLinearInterpolate(
                            rvalue, r1temp, r2temp);

                    r1temp = getLinearInterpolate(cvalue,
                            rawImageArray[2][srcRow][srcCol],
                            rawImageArray[2][srcRow][srcCol + 1]);
                    r2temp = getLinearInterpolate(cvalue,
                            rawImageArray[2][srcRow + 1][srcCol],
                            rawImageArray[2][srcRow + 1][srcCol + 1]);
                    targetImageArray[2][row][col] = getLinearInterpolate(
                            rvalue, r1temp, r2temp);

                } else if (srcRow == h - 1 && srcCol == w - 1) {
                    targetImageArray[0][row][col] = rawImageArray[0][srcRow][srcCol];
                    targetImageArray[1][row][col] = rawImageArray[1][srcRow][srcCol];
                    targetImageArray[2][row][col] = rawImageArray[2][srcRow][srcCol];
                } else {
                    targetImageArray[0][row][col] = rawImageArray[0][row][col];
                    targetImageArray[1][row][col] = rawImageArray[1][row][col];
                    targetImageArray[2][row][col] = rawImageArray[2][row][col];
                }
            }
        }
        rawImageArray = targetImageArray;
        updateImage();

    }
}
/**
 * ***********************************************************************************
 *
 * References:
 *
 * Algorithm, E. 2013. linear algebra - Explanation of this image warping (bulge
 * filter) algorithm - Mathematics Stack Exchange. [online] Available at:
 * http://
 * math.stackexchange.com/questions/266250/explanation-of-this-image-warping
 * -bulge-filter-algorithm [Accessed: 7 Jan 2014].
 *
 * Amarasinghe, U. (n.d). Introduction. [PowerPoint slides]. Colombo: Asia
 * Pacific Institute of Information Technology. Available at: Learning
 * Management System APIIT City Campus. Imaging and Special Effects.
 * <http://lms.apiit.lk/course/view.php?id=1815>(accessed 6th January 2014)
 *
 * Amarasinghe, U. (n.d). Basic Effects. [PowerPoint slides]. Colombo: Asia
 * Pacific Institute of Information Technology. Available at: Learning
 * Management System APIIT City Campus. Imaging and Special Effects.
 * <http://lms.apiit.lk/course/view.php?id=1815>(accessed 6th January 2014)
 *
 * Amarasinghe, U. (n.d). Contrast Enhancement. [PowerPoint slides]. Colombo:
 * Asia Pacific Institute of Information Technology. Available at: Learning
 * Management System APIIT City Campus. Imaging and Special Effects.
 * <http://lms.apiit.lk/course/view.php?id=1815>(accessed 6th January 2014)
 * Amarasinghe, U. (n.d). Segmentation 1. [PowerPoint slides]. Colombo: Asia
 * Pacific Institute of Information Technology. Available at: Learning
 * Management System APIIT City Campus. Imaging and Special Effects.
 * <http://lms.apiit.lk/course/view.php?id=1815>(accessed 6th January 2014)
 *
 * Amarasinghe, U. (n.d). Segmentation 2. [PowerPoint slides]. Colombo: Asia
 * Pacific Institute of Information Technology. Available at: Learning
 * Management System APIIT City Campus. Imaging and Special Effects.
 * <http://lms.apiit.lk/course/view.php?id=1815>(accessed 6th January 2014)
 *
 * Amarasinghe, U. (n.d). Smoothing Techniques. [PowerPoint slides]. Colombo:
 * Asia Pacific Institute of Information Technology. Available at: Learning
 * Management System APIIT City Campus. Imaging and Special Effects.
 * <http://lms.apiit.lk/course/view.php?id=1815>(accessed 6th January 2014)
 *
 * Amarasinghe, U. (n.d). Highpass Filters. [PowerPoint slides]. Colombo: Asia
 * Pacific Institute of Information Technology. Available at: Learning
 * Management System APIIT City Campus. Imaging and Special Effects.
 * <http://lms.apiit.lk/course/view.php?id=1815>(accessed 6th January 2014)
 *
 * Amarasinghe, U. (n.d). Filtering Techniques. [PowerPoint slides]. Colombo:
 * Asia Pacific Institute of Information Technology. Available at: Learning
 * Management System APIIT City Campus. Imaging and Special Effects.
 * <http://lms.apiit.lk/course/view.php?id=1815>(accessed 6th January 2014)
 *
 * Amarasinghe, U. (n.d). Interpolation. [PowerPoint slides]. Colombo: Asia
 * Pacific Institute of Information Technology. Available at: Learning
 * Management System APIIT City Campus. Imaging and Special Effects.
 * <http://lms.apiit.lk/course/view.php?id=1815>(accessed 6th January 2014)
 *
 * Amarasinghe, U. (n.d). Interpolation Techniques and Applications. [PowerPoint
 * slides]. Colombo: Asia Pacific Institute of Information Technology. Available
 * at: Learning Management System APIIT City Campus. Imaging and Special
 * Effects. <http://lms.apiit.lk/course/view.php?id=1815>(accessed 6th January
 * 2014)
 *
 * Amarasinghe, U. (n.d). Colour Storage. [PowerPoint slides]. Colombo: Asia
 * Pacific Institute of Information Technology. Available at: Learning
 * Management System APIIT City Campus. Imaging and Special Effects.
 * <http://lms.apiit.lk/course/view.php?id=1815>(accessed 6th January 2014)
 *
 * Amarasinghe, U. (n.d). Colour Processing. [PowerPoint slides]. Colombo: Asia
 * Pacific Institute of Information Technology. Available at: Learning
 * Management System APIIT City Campus. Imaging and Special Effects.
 * <http://lms.apiit.lk/course/view.php?id=1815>(accessed 6th January 2014)
 *
 * Amarasinghe, U. (n.d). Warping. [PowerPoint slides]. Colombo: Asia Pacific
 * Institute of Information Technology. Available at: Learning Management System
 * APIIT City Campus. Imaging and Special Effects.
 * <http://lms.apiit.lk/course/view.php?id=1815>(accessed 6th January 2014)
 *
 * Amarasinghe, U. (n.d). Morphing. [PowerPoint slides]. Colombo: Asia Pacific
 * Institute of Information Technology. Available at: Learning Management System
 * APIIT City Campus. Imaging and Special Effects.
 * <http://lms.apiit.lk/course/view.php?id=1815>(accessed 6th January 2014)
 *
 * Amarasinghe, U. (n.d). Colour Theory. [PowerPoint slides]. Colombo: Asia
 * Pacific Institute of Information Technology. Available at: Learning
 * Management System APIIT City Campus. Imaging and Special Effects.
 * <http://lms.apiit.lk/course/view.php?id=1815>(accessed 6th January 2014)
 *
 * Amarasinghe, U. (n.d). More Colour. [PowerPoint slides]. Colombo: Asia
 * Pacific Institute of Information Technology. Available at: Learning
 * Management System APIIT City Campus. Imaging and Special Effects.
 * <http://lms.apiit.lk/course/view.php?id=1815>(accessed 6th January 2014)
 *
 * Anonymous. n.d. [online] Available at:
 * http://www.apl.jhu.edu/~hall/java/Java2D-Tutorial.html [Accessed: 6 Jan
 * 2014].
 *
 * Bourke, P. 2002. Lens Correction / Distortion. [online] Available at:
 * http://paulbourke.net/miscellaneous/lenscorrection/ [Accessed: 7 Jan 2014].
 *
 * Burke, D. 2011. image thumbnail question (Swing / AWT / SWT forum at
 * JavaRanch). [online] Available at:
 * http://www.coderanch.com/t/559661/GUI/java/image-thumbnail [Accessed: 6 Jan
 * 2014].
 *
 * Campbell, C. 2007. The Perils of Image.getScaledInstance() | Java.net.
 * [online] Available at:
 * https://today.java.net/pub/a/today/2007/04/03/perils-of
 * -image-getscaledinstance.html [Accessed: 6 Jan 2014].
 *
 * Cook, J. 2009. Three algorithms for converting color to grayscale | The
 * Endeavour. [online] Available at:
 * http://www.johndcook.com/blog/2009/08/24/algorithms-convert-color-grayscale/
 * [Accessed: 7 Jan 2014].
 *
 * �urovic I. (n.d). Digital image processing. [PowerPoint slides].
 * <www.etf.ucg.ac.me/Digital%20image%20processing.pdf?> (accessed 6th January
 * 2014)
 *
 * �urovic I. (n.d). Automatic Thresholding. [PowerPoint slides].
 * <http://www.math.tau.ac.il/~turkel/notes/otsu.pdf> (accessed 6th January
 * 2014)
 *
 * Greensted, A. 2010. Otsu Thresholding - The Lab Book Pages. [online]
 * Available at:
 * http://www.labbookpages.co.uk/software/imgProc/otsuThreshold.html [Accessed:
 * 7 Jan 2014].
 *
 * Gribbon, K., Johnston, C. and Bailey, D. 2003. A real-time FPGA
 * implementation of a barrel distortion correction algorithm with bilinear
 * interpolation. pp. 408--413.
 *
 * Hughes, C., Glavin, M., Jones, E. and Denny, P. 2008. Review of geometric
 * distortion compensation in fish-eye cameras. IET.
 *
 * Image, C. 2012. java - Convert short[] into a grayscale image - Stack
 * Overflow. [online] Available at:
 * http://stackoverflow.com/questions/8765004/convert
 * -short-into-a-grayscale-image [Accessed: 7 Jan 2014].
 *
 * Java, C. 2013. string - Check file extension in Java - Stack Overflow.
 * [online] Available at:
 * http://stackoverflow.com/questions/10928387/check-file-extension-in-java
 * [Accessed: 7 Jan 2014].
 *
 * Java2s. 2009. Using JOptionPane with a JSlider : JOptionPane Dialog � Swing �
 * Java Tutorial. [online] Available at: http://www.java2s.com/Tutorial/Java/
 * 0240__Swing/UsingJOptionPanewithaJSlider.htm [Accessed: 7 Jan 2014].
 *
 * javadocs. n.d. JOptionPane (Java Platform SE 7 ). [online] Available at:
 * http://docs.oracle.com/javase/7/docs/api/javax/swing/JOptionPane.html#
 * showInputDialog%28java.awt.Component,%20java.lang.Object%29 [Accessed: 7 Jan
 * 2014].
 *
 * JavaDocs. n.d. FileNameExtensionFilter (Java Platform SE 6). [online]
 * Available at:
 * http://docs.oracle.com/javase/6/docs/api/javax/swing/filechooser
 * /FileNameExtensionFilter.html [Accessed: 7 Jan 2014].
 *
 * JavaDocs. n.d. JInternalFrame (Java Platform SE 7 ). [online] Available at:
 * http://docs.oracle.com/javase/7/docs/api/javax/swing/JInternalFrame.html
 * [Accessed: 7 Jan 2014].
 *
 * JavaDocs. n.d. Component (Java Platform SE 7 ). [online] Available at:
 * http://
 * docs.oracle.com/javase/7/docs/api/java/awt/Component.html#dispatchEvent
 * %28java.awt.AWTEvent%29 [Accessed: 7 Jan 2014].
 *
 * JavaDocs. n.d. AWTEvent (Java Platform SE 7 ). [online] Available at:
 * http://docs.oracle.com/javase/7/docs/api/java/awt/AWTEvent.html [Accessed: 7
 * Jan 2014].
 *
 * JavaDocs. n.d. InternalFrameEvent (Java Platform SE 7 ). [online] Available
 * at: http://docs.oracle.com/javase/7/docs/api/javax/swing/event/
 * InternalFrameEvent.html [Accessed: 7 Jan 2014].
 *
 * JavaDocs. n.d. BufferedImage (Java Platform SE 7 ). [online] Available at:
 * http://docs.oracle.com/javase/7/docs/api/java/awt/image/BufferedImage.html
 * [Accessed: 7 Jan 2014].
 *
 * Kaving, J. 2012. swing - Maximizing JInternalFrame in Java - Stack Overflow.
 * [online] Available at:
 * http://stackoverflow.com/questions/9438035/maximizing-jinternalframe-in-java
 * [Accessed: 6 Jan 2014].
 *
 * Manipulation, B. 2012. java - Buffered image pixel manipulation - Stack
 * Overflow. [online] Available at:
 * http://stackoverflow.com/questions/7742444/buffered-image-pixel-manipulation
 * [Accessed: 7 Jan 2014].
 *
 * stackoverflow. 2014. java - Add a JScrollPane to a JLabel - Stack Overflow.
 * [online] Available at:
 * http://stackoverflow.com/questions/9335138/add-a-jscrollpane-to-a-jlabel
 * [Accessed: 7 Jan 2014].
 *
 * stackoverflow. 2012. swing - Java ScrollPane on Buffered Image - Stack
 * Overflow. [online] Available at:
 * http://stackoverflow.com/questions/7678840/java-scrollpane-on-buffered-image
 * [Accessed: 7 Jan 2014].
 *
 * stackoverflow. 2010. java - How to programmatically close a JFrame - Stack
 * Overflow. [online] Available at:
 * http://stackoverflow.com/questions/1234912/how
 * -to-programmatically-close-a-jframe [Accessed: 7 Jan 2014].
 *
 * stackoverflow. 2012. Image Warping - Bulge Effect Algorithm - Stack Overflow.
 * [online] Available at:
 * http://stackoverflow.com/questions/5055625/image-warping
 * -bulge-effect-algorithm [Accessed: 7 Jan 2014].
 *
 * stackoverflow. 2012. image processing - FishEye Picture Effect (Barrel
 * Distortion) Algorithm (with Java)? - Stack Overflow. [online] Available at:
 * http://stackoverflow.com/questions/4978039/fisheye-picture-effect-barrel-
 * distortion-algorithm-with-java?rq=1 [Accessed: 7 Jan 2014].
 *
 * Stokes, M., Anderson, M., Motta, R. and Chandrasekar, S. 1996. A Standard
 * Default Color Space for the Internet - sRGB. [online] Available at:
 * http://www.w3.org/Graphics/Color/sRGB [Accessed: 7 Jan 2014].
 *
 * The Java Tutorials. n.d. Reading/Loading an Image (The Java� Tutorials > 2D
 * Graphics > Working with Images). [online] Available at:
 * http://docs.oracle.com/javase/tutorial/2d/images/loadimage.html [Accessed: 7
 * Jan 2014].
 *
 * The Supercomputing Blog. n.d. Image twist and swirl algorithm | The
 * Supercomputing Blog. [online] Available at:
 * http://supercomputingblog.com/openmp/image-twist-and-swirl-algorithm/
 * [Accessed: 7 Jan 2014].
 *
 * Vass, G. and Perlaki, T. 2003. Applying and removing lens distortion in post
 * production. pp. 9--16.
 *
 * Zhou, J. and Li, B. 2005. Automatic generation of pencil-sketch like drawings
 * from personal photos. pp. 1026--1029.
 ***********************************************************************************
 */
