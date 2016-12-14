package com.mxn.soul.flowingdrawer_core;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Path;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;

/**
 * Created by mxn on 2016/12/13.
 */

public class FlowingMenuLayout extends FrameLayout {

    private Path mClipPath;
    private float mClipOffsetPixels = 0;

    public final static int TYPE_NONE = 0 ;
    public final static int TYPE_UP_MANUAL = 1 ;
    public final static int TYPE_UP_AUTO = 2 ;
    public final static int TYPE_UP1 = 3 ;
    public final static int TYPE_UP2 = 4 ;
    public final static int TYPE_UP3 = 5 ;
    public final static int TYPE_UP4 = 6 ;

    private int currentType = TYPE_NONE ;

    public FlowingMenuLayout(Context context) {
        this(context, null);
    }

    public FlowingMenuLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public FlowingMenuLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mClipPath = new Path();
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR2
                && Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        }
    }

    public float getClipOffsetPixels() {
        return mClipOffsetPixels;
    }

    public void setClipOffsetPixels(float clipOffsetPixels, float eventY, int type) {
        mClipOffsetPixels = clipOffsetPixels;
        currentType = type ;
        invalidate();
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {

        double ratio1 = 1;
        double ratio2 = 1;
        int currentPointY = getHeight() / 2;
        int currentPointX = (int) (getWidth() - mClipOffsetPixels);
        int bottomY = (int) (currentPointY + 0.7 * getHeight() / (ratio1 + 1) + currentPointX * 6 / (ratio2 + 1));
        int topY = (int) (currentPointY - 0.7 * getHeight() / (1 + 1 / ratio1) - currentPointX * 6 / (1 / ratio2 +
                                                                                                              1));
        int topControlY = -bottomY / 4 + 5 * currentPointY / 4;
        int bottomControlY = bottomY / 4 + 3 * currentPointY / 4;

        mClipPath.reset();
        mClipPath.moveTo(getWidth() - mClipOffsetPixels, topY);
        mClipPath.cubicTo(getWidth() - mClipOffsetPixels, topControlY, getWidth(),
                topControlY, getWidth(), currentPointY);
        mClipPath.cubicTo(getWidth(), bottomControlY, getWidth() - mClipOffsetPixels,
                bottomControlY, getWidth() - mClipOffsetPixels, bottomY);
        mClipPath.lineTo(getWidth() - mClipOffsetPixels, topY);
        canvas.save();
        canvas.clipPath(mClipPath);

        super.dispatchDraw(canvas);
        canvas.restore();

        switch ( currentType) {
            case TYPE_NONE :
                // x == 0 或全部
                break ;
            case TYPE_UP_MANUAL :

                break ;
            case TYPE_UP_AUTO :

                break ;
        }
    }


}