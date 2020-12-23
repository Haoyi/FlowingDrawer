package com.mxn.soul.flowingdrawer_core;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Region;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;

/**
 * Created by mxn on 2016/12/13.
 * menu layout
 */

@SuppressWarnings("FieldCanBeLocal")
public class FlowingMenuLayout extends FrameLayout {

    private static final String TAG = "FlowingMenuLayout";
    private Path mClipPath;
    private float mClipOffsetPixels = 0;

    public final static int TYPE_NONE = 0;
    public final static int TYPE_UP_MANUAL = 1;
    public final static int TYPE_UP_AUTO = 2;
    public final static int TYPE_UP_DOWN = 3;
    public final static int TYPE_DOWN_AUTO = 4;
    public final static int TYPE_DOWN_MANUAL = 5;
    public final static int TYPE_DOWN_SMOOTH = 6;

    private int currentType = TYPE_NONE;
    private float eventXoY = 0;
    private int topControlX;
    private int topControlY;
    private int bottomControlX;
    private int bottomControlY;
    private int topX;
    private int topY;
    private int bottomX;
    private int bottomY;
    private int width;
    private int height;
    private double verticalOffsetRatio;
    private double horizontalOffsetRatio;
    private double ratio1;
    private double ratio2;
    private float fraction;
    private float fractionUpDown;
    private float fractionEdge;
    private float fractionCenter;
    private float fractionCenterDown;

    private int centerXOffset;
    private int centerYOffset;
    private int edgeXOffset;
    private int edgeYOffset;

    private Paint mPaint;
    private int position;

    public FlowingMenuLayout(Context context) {
        this(context, null);
    }

    public FlowingMenuLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public FlowingMenuLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mClipPath = new Path();
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setStyle(Paint.Style.FILL);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR2
                && Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            setLayerType(View.LAYER_TYPE_SOFTWARE, mPaint);
        }
    }

    public void setPaintColor(int color) {
        mPaint.setColor(color);
    }

    public void setMenuPosition(int position) {
        this.position = position;
    }

    public void setClipOffsetPixels(float clipOffsetPixels, float eventXoY, int type) {
        mClipOffsetPixels = clipOffsetPixels;
        currentType = type;
        this.eventXoY = eventXoY;
        invalidate();
    }

    public void setUpDownFraction(float fraction) {
        fractionUpDown = fraction;
        currentType = TYPE_UP_DOWN;
        invalidate();
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {

        width = getWidth();
        height = getHeight();
        mClipPath.reset();
        if (position == ElasticDrawer.Position.LEFT) {
            drawLeftMenu();
        } else if (position == ElasticDrawer.Position.RIGHT){
            drawRightMenu();
        } else if (position == ElasticDrawer.Position.TOP){
            drawTopMenu();
        } else if (position == ElasticDrawer.Position.BOTTOM){
            drawBottomMenu();
        }
        canvas.save();
        canvas.drawPath(mClipPath, mPaint);
        canvas.clipPath(mClipPath, Region.Op.INTERSECT);
        super.dispatchDraw(canvas);
        canvas.restore();
    }

    private void drawLeftMenu() {
        switch (currentType) {
            case TYPE_NONE:
                Log.d(TAG,"drawLeftMenu"+"TYPE_NONE");
                /**
                 * 空状态
                 * mClipOffsetPixels =0 or mClipOffsetPixels = width
                 */
                mClipPath.moveTo(0, 0);
                mClipPath.lineTo(width, 0);
                mClipPath.lineTo(width, height);
                mClipPath.lineTo(0, height);
                mClipPath.lineTo(0, 0);
                break;
            case TYPE_UP_MANUAL:
                Log.d(TAG,"drawLeftMenu"+"TYPE_UP_MANUAL");
                /**
                 * 手动打开状态
                 verticalOffsetRatio = 0 when currentPointY = 0.5 * height ;
                 verticalOffsetRatio = 1 when currentPointY = height or currentPointY = 0;
                 bottomY,topY由两部分组成
                 第一部分是初始位置，由ratio1和currentPointY决定
                 第二部分由currentPointX移动位置决定
                 两部分系数分别是ratio1，ratio2
                 ratio1，ratio2表示 (currentPointY - topY)/ (bottomY - currentPointY)
                 第一部分bottomY - topY的初始值为height的0.7 倍
                 第二部分bottomY - topY变化的总长为currentPointX变化总长的6倍
                 */
                verticalOffsetRatio = Math.abs((double) (2 * eventXoY - height) / height);
                ratio1 = verticalOffsetRatio * 3 + 1;
                ratio2 = verticalOffsetRatio * 5 + 1;
                if (eventXoY - height / 2 >= 0) {
                    bottomY = (int) (eventXoY + 0.7 * height / (ratio1 + 1) + mClipOffsetPixels * 6 / (ratio2 + 1));
                    topY = (int) (eventXoY - 0.7 * height / (1 + 1 / ratio1) - mClipOffsetPixels * 6 / (1 / ratio2 + 1));
                    topControlY = (int) (-bottomY / 4 + 5 * eventXoY / 4);
                    bottomControlY = (int) (bottomY / 4 + 3 * eventXoY / 4);
                } else {
                    bottomY =
                            (int) (eventXoY + 0.7 * height / (1 / ratio1 + 1) + mClipOffsetPixels * 6 / (1 / ratio2 + 1));
                    topY = (int) (eventXoY - 0.7 * height / (1 + ratio1) - mClipOffsetPixels * 6 / (ratio2 + 1));
                    topControlY = (int) (topY / 4 + 3 * eventXoY / 4);
                    bottomControlY = (int) (-topY / 4 + 5 * eventXoY / 4);
                }
                mClipPath.moveTo(width - mClipOffsetPixels, topY);
                mClipPath.cubicTo(width - mClipOffsetPixels, topControlY, width,
                        topControlY, width, eventXoY);
                mClipPath.cubicTo(width, bottomControlY, width - mClipOffsetPixels,
                        bottomControlY, width - mClipOffsetPixels, bottomY);
                mClipPath.lineTo(width - mClipOffsetPixels, topY);
                break;
            case TYPE_UP_AUTO:
                Log.d(TAG,"drawLeftMenu"+"TYPE_UP_AUTO");
                /**
                 * 自动打开状态
                 fraction变化范围是0-1
                 0-0.5时fractionCenter变化慢（根号函数）,fractionEdge变化快（指数函数）
                 0.5-1时fractionCenter变化快（指数函数）,fractionEdge变化慢（根号函数）
                 centerXOffset初始值width / 2, 变化到width + 150
                 edgeXOffset初始值width * 0.75 ,变化到width + 100
                 */
                fraction = (mClipOffsetPixels - width / 2) / (width / 2);
                if (fraction <= 0.5) {
                    fractionCenter = (float) (2 * Math.pow(fraction, 2));
                    fractionEdge = (float) ((1 / Math.sqrt(2)) * Math.sqrt(fraction));
                } else {
                    fractionCenter =
                            (float) (1 / (2 - Math.sqrt(2)) * Math.sqrt(fraction) + 1 - 1 / (2 - Math.sqrt(2)));
                    fractionEdge = (float) (2 * Math.pow(fraction, 2) / 3 + (float) 1 / 3);
                }
                centerXOffset = (int) (width / 2 + fractionCenter * (width / 2 + 150));
                edgeXOffset = (int) (width * 0.75 + fractionEdge * (width / 4 + 100));
                mClipPath.moveTo(width - mClipOffsetPixels, 0);
                mClipPath.lineTo(edgeXOffset, 0);
                mClipPath.quadTo(centerXOffset, eventXoY, edgeXOffset, height);
                mClipPath.lineTo(width - mClipOffsetPixels, height);
                mClipPath.lineTo(width - mClipOffsetPixels, 0);
                break;
            case TYPE_UP_DOWN:
                Log.d(TAG,"drawLeftMenu"+"TYPE_UP_DOWN");
                /**
                 * 打开后回弹状态
                 centerXOffset初始值width + 150,变化到width
                 edgeXOffset初始值width + 100 ,变化到width
                 */
                centerXOffset = (int) (width + 150 - 150 * fractionUpDown);
                edgeXOffset = (int) (width + 100 - 100 * fractionUpDown);
                mClipPath.moveTo(width - mClipOffsetPixels, 0);
                mClipPath.lineTo(edgeXOffset, 0);
                mClipPath.quadTo(centerXOffset, eventXoY, edgeXOffset, height);
                mClipPath.lineTo(width - mClipOffsetPixels, height);
                mClipPath.lineTo(width - mClipOffsetPixels, 0);
                break;
            case TYPE_DOWN_AUTO:
                Log.d(TAG,"drawLeftMenu"+"TYPE_DOWN_AUTO");
                /**
                 * 自动关闭状态
                 edgeXOffset值width
                 centerXOffset 比edgeXOffset多移动0.5 * width
                 */
                fractionCenterDown = 1 - mClipOffsetPixels / width;
                centerXOffset = (int) (width - 0.5 * width * fractionCenterDown);
                mClipPath.moveTo(width - mClipOffsetPixels, 0);
                mClipPath.lineTo(width, 0);
                mClipPath.quadTo(centerXOffset, eventXoY, width, height);
                mClipPath.lineTo(width - mClipOffsetPixels, height);
                mClipPath.lineTo(width - mClipOffsetPixels, 0);
                break;
            case TYPE_DOWN_MANUAL:
                Log.d(TAG,"drawLeftMenu"+"TYPE_DOWN_MANUAL");
                /**
                 * 手动关闭状态
                 edgeXOffset值width
                 centerXOffset 比edgeXOffset多移动0.5 * width
                 */
                fractionCenterDown = 1 - mClipOffsetPixels / width;
                centerXOffset = (int) (width - 0.5 * width * fractionCenterDown);
                mClipPath.moveTo(width - mClipOffsetPixels, 0);
                mClipPath.lineTo(width, 0);
                mClipPath.quadTo(centerXOffset, eventXoY, width, height);
                mClipPath.lineTo(width - mClipOffsetPixels, height);
                mClipPath.lineTo(width - mClipOffsetPixels, 0);
                break;
            case TYPE_DOWN_SMOOTH:
                Log.d(TAG,"drawLeftMenu"+"TYPE_DOWN_SMOOTH");
                /**
                 * 手动打开不到一半,松手后恢复到初始状态
                 每次绘制两边纵坐标增加10
                 */
                bottomY = bottomY + 10;
                topY = topY - 10;
                if (eventXoY - height / 2 >= 0) {
                    topControlY = (int) (-bottomY / 4 + 5 * eventXoY / 4);
                    bottomControlY = (int) (bottomY / 4 + 3 * eventXoY / 4);
                } else {
                    topControlY = (int) (topY / 4 + 3 * eventXoY / 4);
                    bottomControlY = (int) (-topY / 4 + 5 * eventXoY / 4);
                }
                mClipPath.moveTo(width - mClipOffsetPixels, topY);
                mClipPath.cubicTo(width - mClipOffsetPixels, topControlY, width,
                        topControlY, width, eventXoY);
                mClipPath.cubicTo(width, bottomControlY, width - mClipOffsetPixels,
                        bottomControlY, width - mClipOffsetPixels, bottomY);
                mClipPath.lineTo(width - mClipOffsetPixels, topY);
                break;
            default:
                break;
        }
    }

    private void drawRightMenu() {
        switch (currentType) {
            case TYPE_NONE:
                /**
                 * 空状态
                 * mClipOffsetPixels =0 or mClipOffsetPixels = width
                 */
                mClipPath.moveTo(width, 0);
                mClipPath.lineTo(0, 0);
                mClipPath.lineTo(0, height);
                mClipPath.lineTo(width, height);
                mClipPath.lineTo(width, 0);
                break;
            case TYPE_UP_MANUAL:
                /**
                 * 手动打开状态
                 verticalOffsetRatio = 0 when currentPointY = 0.5 * height ;
                 verticalOffsetRatio = 1 when currentPointY = height or currentPointY = 0;
                 bottomY,topY由两部分组成
                 第一部分是初始位置，由ratio1和currentPointY决定
                 第二部分由currentPointX移动位置决定
                 两部分系数分别是ratio1，ratio2
                 ratio1，ratio2表示 (currentPointY - topY)/ (bottomY - currentPointY)
                 第一部分bottomY - topY的初始值为height的0.7 倍
                 第二部分bottomY - topY变化的总长为currentPointX变化总长的6倍
                 */
                verticalOffsetRatio = Math.abs((double) (2 * eventXoY - height) / height);
                ratio1 = verticalOffsetRatio * 3 + 1;
                ratio2 = verticalOffsetRatio * 5 + 1;
                if (eventXoY - height / 2 >= 0) {
                    bottomY = (int) (eventXoY + 0.7 * height / (ratio1 + 1) - mClipOffsetPixels * 6 / (ratio2 + 1));
                    topY = (int) (eventXoY - 0.7 * height / (1 + 1 / ratio1) + mClipOffsetPixels * 6 / (1 / ratio2 + 1));
                    topControlY = (int) (-bottomY / 4 + 5 * eventXoY / 4);
                    bottomControlY = (int) (bottomY / 4 + 3 * eventXoY / 4);
                } else {
                    bottomY =
                            (int) (eventXoY + 0.7 * height / (1 / ratio1 + 1) - mClipOffsetPixels * 6 / (1 / ratio2 +
                                                                                                               1));
                    topY = (int) (eventXoY - 0.7 * height / (1 + ratio1) + mClipOffsetPixels * 6 / (ratio2 + 1));
                    topControlY = (int) (topY / 4 + 3 * eventXoY / 4);
                    bottomControlY = (int) (-topY / 4 + 5 * eventXoY / 4);
                }
                mClipPath.moveTo(-mClipOffsetPixels, topY);
                mClipPath.cubicTo(-mClipOffsetPixels, topControlY, 0,
                        topControlY, 0, eventXoY);
                mClipPath.cubicTo(0, bottomControlY, -mClipOffsetPixels,
                        bottomControlY, -mClipOffsetPixels, bottomY);
                mClipPath.lineTo(-mClipOffsetPixels, topY);
                break;
            case TYPE_UP_AUTO:
                /**
                 * 自动打开状态
                 fraction变化范围是0-1
                 0-0.5时fractionCenter变化慢（根号函数）,fractionEdge变化快（指数函数）
                 0.5-1时fractionCenter变化快（指数函数）,fractionEdge变化慢（根号函数）
                 centerXOffset初始值width / 2, 变化到width + 150
                 edgeXOffset初始值width * 0.75 ,变化到width + 100
                 */
                fraction = (-mClipOffsetPixels - width / 2) / (width / 2);
                if (fraction <= 0.5) {
                    fractionCenter = (float) (2 * Math.pow(fraction, 2));
                    fractionEdge = (float) ((1 / Math.sqrt(2)) * Math.sqrt(fraction));
                } else {
                    fractionCenter =
                            (float) (1 / (2 - Math.sqrt(2)) * Math.sqrt(fraction) + 1 - 1 / (2 - Math.sqrt(2)));
                    fractionEdge = (float) (2 * Math.pow(fraction, 2) / 3 + (float) 1 / 3);
                }
                centerXOffset = (int) (width / 2 + fractionCenter * (width / 2 + 150));
                edgeXOffset = (int) (width * 0.75 + fractionEdge * (width / 4 + 100));
                mClipPath.moveTo(-mClipOffsetPixels, 0);
                mClipPath.lineTo(width - edgeXOffset, 0);
                mClipPath.quadTo(width - centerXOffset, eventXoY, width - edgeXOffset, height);
                mClipPath.lineTo(-mClipOffsetPixels, height);
                mClipPath.lineTo(-mClipOffsetPixels, 0);
                break;
            case TYPE_UP_DOWN:
                /**
                 * 打开后回弹状态
                 centerXOffset初始值width + 150,变化到width
                 edgeXOffset初始值width + 100 ,变化到width
                 */
                centerXOffset = (int) (width + 150 - 150 * fractionUpDown);
                edgeXOffset = (int) (width + 100 - 100 * fractionUpDown);
                mClipPath.moveTo(-mClipOffsetPixels, 0);
                mClipPath.lineTo(width - edgeXOffset, 0);
                mClipPath.quadTo(width - centerXOffset, eventXoY, width - edgeXOffset, height);
                mClipPath.lineTo(-mClipOffsetPixels, height);
                mClipPath.lineTo(-mClipOffsetPixels, 0);
                break;
            case TYPE_DOWN_AUTO:
                /**
                 * 自动关闭状态
                 edgeXOffset值width
                 centerXOffset 比edgeXOffset多移动0.5 * width
                 */
                fractionCenterDown = 1 + mClipOffsetPixels / width;
                centerXOffset = (int) (width - 0.5 * width * fractionCenterDown);
                mClipPath.moveTo(-mClipOffsetPixels, 0);
                mClipPath.lineTo(0, 0);
                mClipPath.quadTo(width - centerXOffset, eventXoY, 0, height);
                mClipPath.lineTo(-mClipOffsetPixels, height);
                mClipPath.lineTo(-mClipOffsetPixels, 0);
                break;
            case TYPE_DOWN_MANUAL:
                /**
                 * 手动关闭状态
                 edgeXOffset值width
                 centerXOffset 比edgeXOffset多移动0.5 * width
                 */
                fractionCenterDown = 1 + mClipOffsetPixels / width;
                centerXOffset = (int) (width - 0.5 * width * fractionCenterDown);
                mClipPath.moveTo(-mClipOffsetPixels, 0);
                mClipPath.lineTo(0, 0);
                mClipPath.quadTo(width - centerXOffset, eventXoY, 0, height);
                mClipPath.lineTo(-mClipOffsetPixels, height);
                mClipPath.lineTo(-mClipOffsetPixels, 0);
                break;
            case TYPE_DOWN_SMOOTH:
                /**
                 * 手动打开不到一半,松手后恢复到初始状态
                 每次绘制两边纵坐标增加10
                 */
                bottomY = bottomY + 10;
                topY = topY - 10;
                if (eventXoY - height / 2 >= 0) {
                    topControlY = (int) (-bottomY / 4 + 5 * eventXoY / 4);
                    bottomControlY = (int) (bottomY / 4 + 3 * eventXoY / 4);
                } else {
                    topControlY = (int) (topY / 4 + 3 * eventXoY / 4);
                    bottomControlY = (int) (-topY / 4 + 5 * eventXoY / 4);
                }
                mClipPath.moveTo(-mClipOffsetPixels, topY);
                mClipPath.cubicTo(-mClipOffsetPixels, topControlY, 0,
                        topControlY, 0, eventXoY);
                mClipPath.cubicTo(0, bottomControlY, -mClipOffsetPixels,
                        bottomControlY, -mClipOffsetPixels, bottomY);
                mClipPath.lineTo(-mClipOffsetPixels, topY);
                break;
            default:
                break;
        }
    }
    private void drawTopMenu() {
        switch (currentType) {
            case TYPE_NONE:
                /**
                 * 空状态
                 * mClipOffsetPixels =0 or mClipOffsetPixels = width
                 */
                mClipPath.moveTo(0, 0);
                mClipPath.lineTo(width, 0);
                mClipPath.lineTo(width, height);
                mClipPath.lineTo(0, height);
                mClipPath.lineTo(0, 0);
                Log.d(TAG,"drawTopMenu"+"TYPE_NONE");
                break;
            case TYPE_UP_MANUAL:
                Log.d(TAG,"drawTopMenu"+"TYPE_UP_MANUAL");
                /**
                 * 手动打开状态
                 verticalOffsetRatio = 0 when currentPointY = 0.5 * width ;
                 verticalOffsetRatio = 1 when currentPointY = width or currentPointY = 0;
                 bottomX,topY由两部分组成
                 第一部分是初始位置，由ratio1和currentPointY决定
                 第二部分由currentPointX移动位置决定
                 两部分系数分别是ratio1，ratio2
                 ratio1，ratio2表示 (currentPointY - topY)/ (bottomX - currentPointY)
                 第一部分bottomX - topY的初始值为width的0.7 倍
                 第二部分bottomX - topY变化的总长为currentPointX变化总长的6倍
                 */
                horizontalOffsetRatio = Math.abs((double) (2 * eventXoY - width) / width);
                ratio1 = horizontalOffsetRatio * 3 + 1;
                ratio2 = horizontalOffsetRatio * 5 + 1;
                if (eventXoY - width / 2 >= 0) {
                    bottomX = (int) (eventXoY + 0.7 * width / (ratio1 + 1) + mClipOffsetPixels * 6 / (ratio2 + 1));
                    topX = (int) (eventXoY - 0.7 * width / (1 + 1 / ratio1) - mClipOffsetPixels * 6 / (1 / ratio2 + 1));
                    topControlX = (int) (-bottomX / 4 + 5 * eventXoY / 4);
                    bottomControlX = (int) (bottomX / 4 + 3 * eventXoY / 4);
                } else {
                    bottomX =
                            (int) (eventXoY + 0.7 * width / (1 / ratio1 + 1) + mClipOffsetPixels * 6 / (1 / ratio2 + 1));
                    topX = (int) (eventXoY - 0.7 * width / (1 + ratio1) - mClipOffsetPixels * 6 / (ratio2 + 1));
                    topControlX = (int) (topX / 4 + 3 * eventXoY / 4);
                    bottomControlX = (int) (-topX / 4 + 5 * eventXoY / 4);
                }
                mClipPath.moveTo(topX, height - mClipOffsetPixels);
                mClipPath.cubicTo(topControlX, height - mClipOffsetPixels,topControlX, height,
                        eventXoY, height);
                mClipPath.cubicTo(bottomControlX, height, bottomControlX, height - mClipOffsetPixels,
                        bottomX, height - mClipOffsetPixels);
                mClipPath.lineTo(topX,height - mClipOffsetPixels);
                break;
            case TYPE_UP_AUTO:
                Log.d(TAG,"drawTopMenu"+"TYPE_UP_AUTO");
                /**
                 * 自动打开状态
                 fraction变化范围是0-1
                 0-0.5时fractionCenter变化慢（根号函数）,fractionEdge变化快（指数函数）
                 0.5-1时fractionCenter变化快（指数函数）,fractionEdge变化慢（根号函数）
                 centerXOffset初始值width / 2, 变化到width + 150
                 edgeXOffset初始值width * 0.75 ,变化到width + 100
                 */
                fraction = (mClipOffsetPixels - height / 2) / (height / 2);
                if (fraction <= 0.5) {
                    fractionCenter = (float) (2 * Math.pow(fraction, 2));
                    fractionEdge = (float) ((1 / Math.sqrt(2)) * Math.sqrt(fraction));
                } else {
                    fractionCenter =
                            (float) (1 / (2 - Math.sqrt(2)) * Math.sqrt(fraction) + 1 - 1 / (2 - Math.sqrt(2)));
                    fractionEdge = (float) (2 * Math.pow(fraction, 2) / 3 + (float) 1 / 3);
                }
                centerYOffset = (int) (height / 2 + fractionCenter * (height / 2 + 150));
                edgeYOffset = (int) (height * 0.75 + fractionEdge * (height / 4 + 100));
                mClipPath.moveTo(0, height - mClipOffsetPixels);
                mClipPath.lineTo(0, edgeYOffset);
                mClipPath.quadTo(eventXoY, centerYOffset, width, edgeYOffset);
                mClipPath.lineTo(width, height-mClipOffsetPixels);
                mClipPath.lineTo(0, height-mClipOffsetPixels);
                break;
            case TYPE_UP_DOWN:
                Log.d(TAG,"drawTopMenu"+"TYPE_UP_DOWN");
                /**
                 * 打开后回弹状态
                 centerXOffset初始值width + 150,变化到width
                 edgeXOffset初始值width + 100 ,变化到width
                 */
                centerYOffset = (int) (height + 150 - 150 * fractionUpDown);
                edgeYOffset = (int) (height + 100 - 100 * fractionUpDown);
                mClipPath.moveTo(0, height - mClipOffsetPixels);
                mClipPath.lineTo(0, edgeYOffset);
                mClipPath.quadTo(eventXoY, centerYOffset, width, edgeYOffset);
                mClipPath.lineTo(width, height-mClipOffsetPixels);
                mClipPath.lineTo(0, height-mClipOffsetPixels);
                break;
            case TYPE_DOWN_AUTO:
                Log.d(TAG,"drawTopMenu"+"TYPE_DOWN_AUTO");
                /**
                 * 自动关闭状态
                 edgeXOffset值width
                 centerXOffset 比edgeXOffset多移动0.5 * width
                 */
                fractionCenterDown = 1 - mClipOffsetPixels / height;
                centerYOffset = (int) (height - 0.5 * height * fractionCenterDown);
                mClipPath.moveTo(0, height - mClipOffsetPixels);
                mClipPath.lineTo(0, height);
                mClipPath.quadTo(eventXoY, centerYOffset, width, height);
                mClipPath.lineTo(width, height - mClipOffsetPixels);
                mClipPath.lineTo(0, height - mClipOffsetPixels);
                break;
            case TYPE_DOWN_MANUAL:
                Log.d(TAG,"drawTopMenu"+"TYPE_DOWN_MANUAL");
                /**
                 * 手动关闭状态
                 edgeXOffset值width
                 centerXOffset 比edgeXOffset多移动0.5 * width
                 */
                fractionCenterDown = 1 - mClipOffsetPixels / height;
                centerYOffset = (int) (height - 0.5 * height * fractionCenterDown);
                mClipPath.moveTo(0, height - mClipOffsetPixels);
                mClipPath.lineTo(0, height);
                mClipPath.quadTo(eventXoY, centerYOffset, width, height);
                mClipPath.lineTo(width, height - mClipOffsetPixels);
                mClipPath.lineTo(0, height - mClipOffsetPixels);
                break;
            case TYPE_DOWN_SMOOTH:
                Log.d(TAG,"drawTopMenu"+"TYPE_DOWN_SMOOTH");
                /**
                 * 手动打开不到一半,松手后恢复到初始状态
                 每次绘制两边纵坐标增加10
                 */
                bottomX = bottomX + 10;
                topX = topX - 10;
                if (eventXoY - width / 2 >= 0) {
                    topControlX = (int) (-bottomX / 4 + 5 * eventXoY / 4);
                    bottomControlX = (int) (bottomX / 4 + 3 * eventXoY / 4);
                } else {
                    topControlX = (int) (topX / 4 + 3 * eventXoY / 4);
                    bottomControlX = (int) (-topX / 4 + 5 * eventXoY / 4);
                }
                mClipPath.moveTo(topX, height - mClipOffsetPixels);
                mClipPath.cubicTo(topControlX, height - mClipOffsetPixels, topControlX, height,
                        eventXoY, height);
                mClipPath.cubicTo(bottomControlX, height, bottomControlX, height - mClipOffsetPixels,
                        bottomX, height - mClipOffsetPixels);
                mClipPath.lineTo(topX,height - mClipOffsetPixels);
                break;
            default:
                break;
        }
    }

    private void drawBottomMenu() {
        switch (currentType) {
            case TYPE_NONE:
                /**
                 * 空状态
                 * mClipOffsetPixels =0 or mClipOffsetPixels = width
                 */
                mClipPath.moveTo(width, 0);
                mClipPath.lineTo(0, 0);
                mClipPath.lineTo(0, height);
                mClipPath.lineTo(width, height);
                mClipPath.lineTo(width, 0);
                break;
            case TYPE_UP_MANUAL:
                /**
                 * 手动打开状态
                 verticalOffsetRatio = 0 when currentPointY = 0.5 * height ;
                 verticalOffsetRatio = 1 when currentPointY = height or currentPointY = 0;
                 bottomY,topY由两部分组成
                 第一部分是初始位置，由ratio1和currentPointY决定
                 第二部分由currentPointX移动位置决定
                 两部分系数分别是ratio1，ratio2
                 ratio1，ratio2表示 (currentPointY - topY)/ (bottomY - currentPointY)
                 第一部分bottomY - topY的初始值为width的0.7 倍
                 第二部分bottomY - topY变化的总长为currentPointX变化总长的6倍
                 */
                horizontalOffsetRatio = Math.abs((double) (2 * eventXoY - width) / width);
                ratio1 = horizontalOffsetRatio * 3 + 1;
                ratio2 = horizontalOffsetRatio * 5 + 1;
                if (eventXoY - width / 2 >= 0) {
                    bottomX = (int) (eventXoY + 0.7 * width / (ratio1 + 1) - mClipOffsetPixels * 6 / (ratio2 + 1));
                    topX = (int) (eventXoY - 0.7 * width / (1 + 1 / ratio1) + mClipOffsetPixels * 6 / (1 / ratio2 + 1));
                    topControlX = (int) (-bottomX / 4 + 5 * eventXoY / 4);
                    bottomControlX = (int) (bottomX / 4 + 3 * eventXoY / 4);
                } else {
                    bottomX =
                            (int) (eventXoY + 0.7 * width / (1 / ratio1 + 1) - mClipOffsetPixels * 6 / (1 / ratio2 +
                                    1));
                    topX = (int) (eventXoY - 0.7 * width / (1 + ratio1) + mClipOffsetPixels * 6 / (ratio2 + 1));
                    topControlX = (int) (topX / 4 + 3 * eventXoY / 4);
                    bottomControlX = (int) (-topX / 4 + 5 * eventXoY / 4);
                }
                mClipPath.moveTo(topX, -mClipOffsetPixels);
                mClipPath.cubicTo(topControlX, -mClipOffsetPixels, topControlX, 0,
                        eventXoY,0);
                mClipPath.cubicTo(bottomControlX, 0, bottomControlX, -mClipOffsetPixels,
                        bottomX, -mClipOffsetPixels);
                mClipPath.lineTo(topX,-mClipOffsetPixels);
                break;
            case TYPE_UP_AUTO:
                /**
                 * 自动打开状态
                 fraction变化范围是0-1
                 0-0.5时fractionCenter变化慢（根号函数）,fractionEdge变化快（指数函数）
                 0.5-1时fractionCenter变化快（指数函数）,fractionEdge变化慢（根号函数）
                 centerXOffset初始值width / 2, 变化到width + 150
                 edgeXOffset初始值width * 0.75 ,变化到width + 100
                 */
                fraction = (-mClipOffsetPixels - height / 2) / (height / 2);
                if (fraction <= 0.5) {
                    fractionCenter = (float) (2 * Math.pow(fraction, 2));
                    fractionEdge = (float) ((1 / Math.sqrt(2)) * Math.sqrt(fraction));
                } else {
                    fractionCenter =
                            (float) (1 / (2 - Math.sqrt(2)) * Math.sqrt(fraction) + 1 - 1 / (2 - Math.sqrt(2)));
                    fractionEdge = (float) (2 * Math.pow(fraction, 2) / 3 + (float) 1 / 3);
                }
                centerYOffset = (int) (height / 2 + fractionCenter * (height / 2 + 150));
                edgeYOffset = (int) (height * 0.75 + fractionEdge * (height / 4 + 100));
                mClipPath.moveTo(0, -mClipOffsetPixels);
                mClipPath.lineTo(0, height - edgeYOffset);
                mClipPath.quadTo(eventXoY, height - centerYOffset, width, height - edgeYOffset);
                mClipPath.lineTo(width, -mClipOffsetPixels);
                mClipPath.lineTo(0, -mClipOffsetPixels);
                break;
            case TYPE_UP_DOWN:
                /**
                 * 打开后回弹状态
                 centerXOffset初始值width + 150,变化到width
                 edgeXOffset初始值width + 100 ,变化到width
                 */
                centerYOffset = (int) (height + 150 - 150 * fractionUpDown);
                edgeYOffset = (int) (height + 100 - 100 * fractionUpDown);
                mClipPath.moveTo(0, -mClipOffsetPixels);
                mClipPath.lineTo(0,height - edgeYOffset);
                mClipPath.quadTo(eventXoY, height - centerYOffset, width, height - edgeYOffset);
                mClipPath.lineTo( width, -mClipOffsetPixels);
                mClipPath.lineTo(0, -mClipOffsetPixels);
                break;
            case TYPE_DOWN_AUTO:
                /**
                 * 自动关闭状态
                 edgeXOffset值width
                 centerXOffset 比edgeXOffset多移动0.5 * width
                 */
                fractionCenterDown = 1 + mClipOffsetPixels / height;
                centerYOffset = (int) (height - 0.5 * height * fractionCenterDown);
                mClipPath.moveTo(0, -mClipOffsetPixels);
                mClipPath.lineTo(0, 0);
                mClipPath.quadTo(eventXoY, height - centerYOffset, width,0);
                mClipPath.lineTo(width, -mClipOffsetPixels);
                mClipPath.lineTo(0, -mClipOffsetPixels);
                break;
            case TYPE_DOWN_MANUAL:
                /**
                 * 手动关闭状态
                 edgeXOffset值width
                 centerXOffset 比edgeXOffset多移动0.5 * width
                 */
                fractionCenterDown = 1 + mClipOffsetPixels / height;
                centerYOffset = (int) (height - 0.5 * height * fractionCenterDown);
                mClipPath.moveTo(0, -mClipOffsetPixels);
                mClipPath.lineTo(0, 0);
                mClipPath.quadTo(eventXoY, height - centerYOffset, width, 0);
                mClipPath.lineTo(width, -mClipOffsetPixels);
                mClipPath.lineTo(0, -mClipOffsetPixels);
                break;
            case TYPE_DOWN_SMOOTH:
                /**
                 * 手动打开不到一半,松手后恢复到初始状态
                 每次绘制两边纵坐标增加10
                 */
                bottomX = bottomX + 10;
                topX = topX - 10;
                if (eventXoY - width / 2 >= 0) {
                    topControlX = (int) (-bottomX / 4 + 5 * eventXoY / 4);
                    bottomControlX = (int) (bottomX / 4 + 3 * eventXoY / 4);
                } else {
                    topControlX = (int) (topX / 4 + 3 * eventXoY / 4);
                    bottomControlX = (int) (-topX / 4 + 5 * eventXoY / 4);
                }
                mClipPath.moveTo(topX, -mClipOffsetPixels);
                mClipPath.cubicTo(topControlX, -mClipOffsetPixels, topControlX, 0,
                        eventXoY, 0);
                mClipPath.cubicTo(bottomControlX, 0, bottomControlX, -mClipOffsetPixels,
                        bottomX, -mClipOffsetPixels);
                mClipPath.lineTo(topX, -mClipOffsetPixels);
                break;
            default:
                break;
        }
    }
}
