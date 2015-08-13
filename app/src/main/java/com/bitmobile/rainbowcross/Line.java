package com.bitmobile.rainbowcross;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.view.View;

/**
 * Created by User1 on 04.03.2015.
 */
public class Line extends View {
    private final static int GAP=1;
    Paint paint = new Paint();
    int length;
    boolean hv;
    Integer x;
    Integer y;
    int xOffset;
    int yOffset;

    public Line(Context context,int color,int length,boolean hv,int x,int y,int xOffset,int yOffset) {
        super(context);
        paint.setColor(color);
        paint.setStyle(Paint.Style.FILL);
        paint.setStrokeWidth(Math.round(length/5));
        this.length=length;
        this.hv=hv;
        this.x=x;
        this.y=y;
        this.xOffset=xOffset;
        this.yOffset=yOffset;
    }


    @Override
    public void onDraw(Canvas canvas) {
        if (hv) {
            Path path = new Path();
            path.moveTo(x*length+xOffset+GAP,y*length+yOffset);
            path.lineTo(Math.round((x+0.10)*length+xOffset)+GAP, Math.round((y-0.10)*length+yOffset));
            path.lineTo(Math.round((x+0.9)*length+xOffset)-GAP, Math.round((y-0.10)*length+yOffset));
            path.lineTo((x+1)*length+xOffset-GAP,y*length+yOffset);
            path.lineTo(Math.round((x+0.9)*length+xOffset)-GAP,  Math.round((y+0.10)*length+yOffset));
            path.lineTo(Math.round((x+0.10)*length+xOffset)+GAP,  Math.round((y+0.10)*length+yOffset));
            path.close();
            canvas.drawPath(path, paint);
        }else{
            Path path = new Path();
            path.moveTo(x*length+xOffset,y*length+yOffset+GAP);
            path.lineTo(Math.round((x+0.10)*length+xOffset), Math.round((y+0.10)*length+yOffset)+GAP);
            path.lineTo(Math.round((x+0.1)*length+xOffset), Math.round((y+0.90)*length+yOffset)-GAP);
            path.lineTo(x*length+xOffset,(y+1)*length+yOffset-GAP);
            path.lineTo(Math.round((x-0.1)*length+xOffset),  Math.round((y+0.90)*length+yOffset)-GAP);
            path.lineTo(Math.round((x-0.10)*length+xOffset),  Math.round((y+0.10)*length+yOffset)+GAP);
            path.close();
            canvas.drawPath(path, paint);
        }


    }
}
