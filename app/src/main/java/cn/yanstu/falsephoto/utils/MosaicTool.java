package cn.yanstu.falsephoto.utils;

import android.graphics.Bitmap;
import android.graphics.Color;

public class MosaicTool {


    public static Bitmap BitmapMosaic(Bitmap bitmap, int BLOCK_SIZE) {

        if (bitmap == null || bitmap.getWidth() == 0 || bitmap.getHeight() == 0
                || bitmap.isRecycled()) {
            return null;
        }
        int mBitmapWidth = bitmap.getWidth();
        int mBitmapHeight = bitmap.getHeight();
        Bitmap mBitmap = Bitmap.createBitmap(mBitmapWidth, mBitmapHeight,
                Bitmap.Config.ARGB_8888);//创建画布
        int row = mBitmapWidth / BLOCK_SIZE;// 获得列的切线
        int col = mBitmapHeight / BLOCK_SIZE;// 获得行的切线
        int[] block = new int[BLOCK_SIZE * BLOCK_SIZE];
        for (int i = 0; i <=row; i++)
        {
            for (int j =0; j <= col; j++)
            {
                int length = block.length;
                int flag = 0;// 是否到边界标志
                if (i == row && j != col) {
                    length = (mBitmapWidth - i * BLOCK_SIZE) * BLOCK_SIZE;
                    if (length == 0) {
                        break;// 边界外已经没有像素
                    }
                    bitmap.getPixels(block, 0, BLOCK_SIZE, i * BLOCK_SIZE, j
                                    * BLOCK_SIZE, mBitmapWidth - i * BLOCK_SIZE,
                            BLOCK_SIZE);

                    flag = 1;
                } else if (i != row && j == col) {
                    length = (mBitmapHeight - j * BLOCK_SIZE) * BLOCK_SIZE;
                    if (length == 0) {
                        break;// 边界外已经没有像素
                    }
                    bitmap.getPixels(block, 0, BLOCK_SIZE, i * BLOCK_SIZE, j
                            * BLOCK_SIZE, BLOCK_SIZE, mBitmapHeight - j
                            * BLOCK_SIZE);
                    flag = 2;
                } else if (i == row && j == col) {
                    length = (mBitmapWidth - i * BLOCK_SIZE)
                            * (mBitmapHeight - j * BLOCK_SIZE);
                    if (length == 0) {
                        break;// 边界外已经没有像素
                    }
                    bitmap.getPixels(block, 0, BLOCK_SIZE, i * BLOCK_SIZE, j
                                    * BLOCK_SIZE, mBitmapWidth - i * BLOCK_SIZE,
                            mBitmapHeight - j * BLOCK_SIZE);

                    flag = 3;
                } else
                {
                    bitmap.getPixels(block, 0, BLOCK_SIZE, i * BLOCK_SIZE, j
                            * BLOCK_SIZE, BLOCK_SIZE, BLOCK_SIZE);//取出像素数组
                }

                int r = 0, g = 0, b = 0, a = 0;
                for (int k = 0; k < length; k++) {
                    r += Color.red(block[k]);
                    g += Color.green(block[k]);
                    b += Color.blue(block[k]);
                    a += Color.alpha(block[k]);
                }
                int color = Color.argb(a / length, r / length, g / length, b
                        / length);//求块内所有颜色的平均值
                for (int k = 0; k < length; k++) {
                    block[k] = color;
                }
                if (flag == 1) {
                    mBitmap.setPixels(block, 0, mBitmapWidth - i * BLOCK_SIZE,
                            i * BLOCK_SIZE, j
                                    * BLOCK_SIZE, mBitmapWidth - i * BLOCK_SIZE,
                            BLOCK_SIZE);
                } else if (flag == 2) {
                    mBitmap.setPixels(block, 0, BLOCK_SIZE, i * BLOCK_SIZE, j
                            * BLOCK_SIZE, BLOCK_SIZE, mBitmapHeight - j
                            * BLOCK_SIZE);
                } else if (flag == 3) {
                    mBitmap.setPixels(block, 0, BLOCK_SIZE, i * BLOCK_SIZE, j
                                    * BLOCK_SIZE, mBitmapWidth - i * BLOCK_SIZE,
                            mBitmapHeight - j * BLOCK_SIZE);
                } else {
                    mBitmap.setPixels(block, 0, BLOCK_SIZE, i * BLOCK_SIZE, j
                            * BLOCK_SIZE, BLOCK_SIZE, BLOCK_SIZE);
                }

            }
        }
        //并没有回收传进来的bitmap  原因是JAVA传值默认是引用,如果回收了之后,其他地方用到bitmap的位置可能报NULL指针异常,请根据实际情况决定是否回收.
        return mBitmap;
    }

}
