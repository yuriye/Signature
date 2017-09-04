package com.yelisoft.signature;

/**
 * Created by eliseev on 04.09.2017.
 */
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.os.Environment;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class SigningView extends View{
    // setup initial color
    private final int paintColor = R.color.colorInk;
    // defines paint and canvas
    private Paint drawPaint;
    // stores next circle
    private Path path = new Path();
    private boolean clearing = false;

    public SigningView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setFocusable(true);
        setFocusableInTouchMode(true);
        setupPaint();
    }

    private void setupPaint() {
        // Setup paint with color and stroke styles
        drawPaint = new Paint();
        drawPaint.setColor(getResources().getColor(R.color.colorInk));
        drawPaint.setAntiAlias(true);
        drawPaint.setStrokeWidth(3);
        drawPaint.setStyle(Paint.Style.STROKE);
        drawPaint.setStrokeJoin(Paint.Join.ROUND);
        drawPaint.setStrokeCap(Paint.Cap.ROUND);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (clearing) {
            canvas.drawColor(Color.TRANSPARENT);
            clearing = false;

            return;
        }
        canvas.drawPath(path, drawPaint);
    }

    public void clear() {
        clearing = true;
        path = new Path();
        postInvalidate();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float pointX = event.getX();
        float pointY = event.getY();
        // Checks for the event that occurs
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                path.moveTo(pointX, pointY);
                return true;
            case MotionEvent.ACTION_MOVE:
                path.lineTo(pointX, pointY);
                break;
            default:
                return false;
        }
        // Force a view to draw again
        postInvalidate();
        return true;
    }

    public Bitmap trimAllSides(Bitmap bitmap) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        int minx = Integer.MAX_VALUE;
        int maxx = Integer.MIN_VALUE;
        int miny = Integer.MAX_VALUE;
        int maxy = Integer.MIN_VALUE;
        /*for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                if (bitmap.getPixel(i, j) == 0) continue;
                if (i < minx) minx = i;
                if (i > maxx) maxx = i;
                if (j < miny) miny = j;
                if (j > maxy) maxy = j;
            }
        }*/
        int i, j;
        br1:
        for (i = 0; i < width; i++) {
            for (j = 0; j < height; j++) {
                if (bitmap.getPixel(i, j) == 0) continue;
                break br1;
            }
        }
        minx = i;

        br2:
        for (i = width - 1; i > 0; i--) {
            for (j = 0; j < height; j++) {
                if (bitmap.getPixel(i, j) == 0) continue;
                break br2;
            }
        }
        maxx = i;

        br3:
        for (j = 0; j < height; j++) {
            for (i = minx; i <= maxx; i++) {
                if (bitmap.getPixel(i, j) == 0) continue;
                break br3;
            }
        }
        miny = j;

        br4:
        for (j = height - 1; j >= 0; j--) {
            for (i = minx; i <= maxx; i++) {
                if (bitmap.getPixel(i, j) == 0) continue;
                break br4;
            }
        }
        maxy = j;

        if (minx > maxx || miny > maxy)
            return bitmap;

        return bitmap.createBitmap(bitmap, minx, miny, maxx - minx, maxy - miny);
    }

    public void saveToPng(Context context) {
        FileOutputStream os;
        File directory = context.getExternalFilesDir(Environment.DIRECTORY_DCIM);
        System.out.println(directory);
        this.setDrawingCacheEnabled(true);
        Bitmap bitmap = this.getDrawingCache();

        try {
            Bitmap bitmap1 = trimAllSides(bitmap);
            File file = new File(directory, "sign.png");
            file.createNewFile();
            os = new FileOutputStream(file);
            bitmap1.compress(Bitmap.CompressFormat.PNG, 60, os);
            os.flush();
            os.close();

        } catch (IOException e) {

            System.out.println(directory);
            e.printStackTrace();
        }
        clear();
        this.setDrawingCacheEnabled(false);
    }

}
