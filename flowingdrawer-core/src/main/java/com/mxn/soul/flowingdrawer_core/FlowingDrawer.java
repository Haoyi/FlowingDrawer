
package com.mxn.soul.flowingdrawer_core;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;

/**
 * Created by mxn on 2016/10/17.
 * FlowingDrawer
 */
public class FlowingDrawer extends ElasticDrawer {

    private static final String TAG = "FlowingDrawer";

    public FlowingDrawer(Context context) {
        super(context);
    }

    public FlowingDrawer(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public FlowingDrawer(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @SuppressLint("NewApi")
    @Override
    protected void initDrawer(Context context, AttributeSet attrs, int defStyle) {
        super.initDrawer(context, attrs, defStyle);
    }

    @Override
    public void openMenu(boolean animate) {
        switch (getPosition()) {
            case Position.LEFT:
            case Position.RIGHT:
                openMenu(animate, getHeight() / 2);
                break;
            case Position.TOP:
            case Position.BOTTOM:
                openMenu(animate, getWidth() / 2);
                break;
        }
    }

    @Override
    public void openMenu(boolean animate, float posXoY) {
        int animateTo = 0;
        switch (getPosition()) {
            case Position.LEFT:
            case Position.TOP:
                animateTo = mMenuSize;
                break;
            case Position.RIGHT:
            case Position.BOTTOM:
                animateTo = -mMenuSize;
                break;
        }
        animateOffsetTo(animateTo, 0, animate, posXoY);
    }

    @Override
    public void closeMenu(boolean animate) {
        switch (getPosition()) {
            case Position.LEFT:
            case Position.RIGHT:
                closeMenu(animate, getHeight() / 2);
                break;
            case Position.TOP:
            case Position.BOTTOM:
                closeMenu(animate, getWidth() / 2);
                break;
        }
    }

    @Override
    public void closeMenu(boolean animate, float posXoY) {
        animateOffsetTo(0, 0, animate, posXoY);
    }

    @SuppressLint("NewApi")
    @Override
    protected void onOffsetPixelsChanged(int offsetPixels) {
        switch (getPosition()) {
            case Position.LEFT:
                mMenuContainer.setTranslationX(offsetPixels - mMenuSize);
                break;
            case Position.RIGHT:
                mMenuContainer.setTranslationX(offsetPixels + mMenuSize);
                break;
            case Position.TOP:
                mMenuContainer.setTranslationY(offsetPixels - mMenuSize);
                break;
            case Position.BOTTOM:
                mMenuContainer.setTranslationY(offsetPixels + mMenuSize);
                break;
        }
        invalidate();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        onOffsetPixelsChanged((int) mOffsetPixels);
    }

    @SuppressLint("NewApi")
    @Override
    protected void startLayerTranslation() {
        if (mHardwareLayersEnabled && !mLayerTypeHardware) {
            mLayerTypeHardware = true;
            mMenuContainer.setLayerType(View.LAYER_TYPE_HARDWARE, null);
        }
    }

    @SuppressLint("NewApi")
    @Override
    protected void stopLayerTranslation() {
        if (mLayerTypeHardware) {
            mLayerTypeHardware = false;
            mMenuContainer.setLayerType(View.LAYER_TYPE_NONE, null);
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        final int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        final int heightMode = MeasureSpec.getMode(heightMeasureSpec);

        if (widthMode == MeasureSpec.UNSPECIFIED || heightMode == MeasureSpec.UNSPECIFIED) {
            throw new IllegalStateException("Must measure with an exact size");
        }

        final int width = MeasureSpec.getSize(widthMeasureSpec);
        final int height = MeasureSpec.getSize(heightMeasureSpec);

        if (mOffsetPixels == -1) {
            openMenu(false);
        }

        int menuWidthMeasureSpec = 0;
        int menuHeightMeasureSpec = 0;
        if((getPosition() == Position.LEFT)||(getPosition() == Position.RIGHT)) {
            menuWidthMeasureSpec = getChildMeasureSpec(widthMeasureSpec, 0, mMenuSize);
            menuHeightMeasureSpec = getChildMeasureSpec(widthMeasureSpec, 0, height);
        }else if((getPosition() == Position.TOP)||(getPosition() == Position.BOTTOM)){
            menuWidthMeasureSpec = getChildMeasureSpec(widthMeasureSpec, 0, width);
            menuHeightMeasureSpec = getChildMeasureSpec(widthMeasureSpec, 0, mMenuSize);
        }
        mMenuContainer.measure(menuWidthMeasureSpec, menuHeightMeasureSpec);

        final int contentWidthMeasureSpec = getChildMeasureSpec(widthMeasureSpec, 0, width);
        final int contentHeightMeasureSpec = getChildMeasureSpec(widthMeasureSpec, 0, height);
        mContentContainer.measure(contentWidthMeasureSpec, contentHeightMeasureSpec);

        setMeasuredDimension(width, height);

        updateTouchAreaSize();
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        final int width = r - l;
        final int height = b - t;

        mContentContainer.layout(0, 0, width, height);
        switch (getPosition()) {
            case Position.LEFT:
                mMenuContainer.layout(0, 0, mMenuSize, height);
                break;

            case Position.RIGHT:
                mMenuContainer.layout(width - mMenuSize, 0, width, height);
                break;
            case Position.TOP:
                mMenuContainer.layout(0, 0, width, height);
                break;

            case Position.BOTTOM:
                mMenuContainer.layout(0, height-mMenuSize, width,height);
                break;
        }
    }

    private boolean isContentTouch(int x,int y) {
        boolean contentTouch = false;

        switch (getPosition()) {
            case Position.LEFT:
                contentTouch = ViewHelper.getRight(mMenuContainer) < x;
                break;
            case Position.RIGHT:
                contentTouch = ViewHelper.getLeft(mMenuContainer) > x;
                break;
            case Position.TOP:
                contentTouch = ViewHelper.getBottom(mMenuContainer) < y;
                break;
            case Position.BOTTOM:
                contentTouch = ViewHelper.getTop(mMenuContainer) > y;
                break;
        }
        return contentTouch;
    }

    private boolean willCloseEnough() {
        boolean closeEnough = false;

        switch (getPosition()) {
            case Position.LEFT:
            case Position.TOP:
                closeEnough = mOffsetPixels <= mMenuSize/ 2;
                break;
            case Position.RIGHT:
            case Position.BOTTOM:
                closeEnough = -mOffsetPixels <= mMenuSize / 2;
                break;
        }
        return closeEnough;
    }

    protected boolean onDownAllowDrag() {
        switch (getPosition()) {
            case Position.LEFT:
                return (!mMenuVisible && mInitialMotionX <= mTouchSize)
                        || (mMenuVisible && mInitialMotionX <= mOffsetPixels);

            case Position.RIGHT:
                final int width = getWidth();
                final int initialMotionX = (int) mInitialMotionX;

                return (!mMenuVisible && initialMotionX >= width - mTouchSize)
                        || (mMenuVisible && initialMotionX >= width + mOffsetPixels);
            case Position.TOP:
                return (!mMenuVisible && mInitialMotionY <= mTouchSize)
                        || (mMenuVisible && mInitialMotionY <= mOffsetPixels);

            case Position.BOTTOM:
                final int height = getHeight();
                final int initialMotionY = (int) mInitialMotionY;

                return (!mMenuVisible && initialMotionY >= height - mTouchSize)
                        || (mMenuVisible && initialMotionY >= height + mOffsetPixels);
        }

        return false;
    }

    protected boolean onMoveAllowDrag(int x, int y, float dis) {
        if (mMenuVisible && mTouchMode == TOUCH_MODE_FULLSCREEN) {
            return true;
        }

        switch (getPosition()) {
            case Position.LEFT:
                return (!mMenuVisible && mInitialMotionX <= mTouchSize && (dis > 0)) // Drawer closed
                        || (mMenuVisible && x <= mOffsetPixels);// Drawer open
            case Position.RIGHT:
                final int width = getWidth();
                return (!mMenuVisible && mInitialMotionX >= width - mTouchSize && (dis < 0))
                        || (mMenuVisible && x >= width + mOffsetPixels);
            case Position.TOP:
                return (!mMenuVisible && mInitialMotionY <= mTouchSize && (dis > 0)) // Drawer closed
                        || (mMenuVisible && y <= mOffsetPixels);// Drawer open
            case Position.BOTTOM:
                final int height = getHeight();
                return (!mMenuVisible && mInitialMotionY >= height - mTouchSize && (dis < 0))
                        || (mMenuVisible && y >= height + mOffsetPixels);

        }

        return false;
    }

    protected void onMoveEvent(float dis, float x, float y, int type) {
        switch (getPosition()) {
            case Position.LEFT:
                setOffsetPixels(Math.min(Math.max(mOffsetPixels + dis, 0), mMenuSize), y, type);
                break;
            case Position.RIGHT:
                setOffsetPixels(Math.max(Math.min(mOffsetPixels + dis, 0), -mMenuSize), y, type);
                break;
            case Position.TOP:
                setOffsetPixels(Math.min(Math.max(mOffsetPixels + dis, 0), mMenuSize), x, type);
                break;
            case Position.BOTTOM:
                setOffsetPixels(Math.max(Math.min(mOffsetPixels + dis, 0), -mMenuSize), x, type);
                break;
        }
    }

    protected void onUpEvent(int x, int y) {
        switch (getPosition()) {
            case Position.LEFT: {
                if (mIsDragging) {
                    if (mDrawerState == STATE_DRAGGING_CLOSE) {
                        closeMenu(true, y);
                        return;
                    }
                    if (mDrawerState == STATE_DRAGGING_OPEN && willCloseEnough()) {
                        smoothClose(y);
                        return;
                    }
                    mVelocityTracker.computeCurrentVelocity(1000, mMaxVelocity);
                    final int initialVelocity = (int) getXVelocity(mVelocityTracker);
                    mLastMotionX = x;
                    animateOffsetTo(initialVelocity > 0 ? mMenuSize : 0, initialVelocity, true, y);
                } else if (isFirstPointUp) {
                    isFirstPointUp = false;
                    return;
                }
                // Close the menu when content is clicked while the menu is visible.
                else if (mMenuVisible) {
                    closeMenu(true, y);
                }
                break;
            }
            case Position.RIGHT: {
                if (mIsDragging) {
                    if (mDrawerState == STATE_DRAGGING_CLOSE) {
                        closeMenu(true, y);
                        return;
                    }
                    if (mDrawerState == STATE_DRAGGING_OPEN && willCloseEnough()) {
                        smoothClose(y);
                        return;
                    }
                    mVelocityTracker.computeCurrentVelocity(1000, mMaxVelocity);
                    final int initialVelocity = (int) getXVelocity(mVelocityTracker);
                    mLastMotionX = x;
                    animateOffsetTo(initialVelocity > 0 ? 0 : -mMenuSize, initialVelocity, true, y);
                } else if (isFirstPointUp) {
                    isFirstPointUp = false;
                    return;
                }
                // Close the menu when content is clicked while the menu is visible.
                else if (mMenuVisible) {
                    closeMenu(true, y);
                }
                break;
            }
            case Position.TOP: {
                if (mIsDragging) {
                    if (mDrawerState == STATE_DRAGGING_CLOSE) {
                        closeMenu(true, x);
                        return;
                    }
                    if (mDrawerState == STATE_DRAGGING_OPEN && willCloseEnough()) {
                        smoothClose(x);
                        return;
                    }
                    mVelocityTracker.computeCurrentVelocity(1000, mMaxVelocity);
                    final int initialVelocity = (int) getYVelocity(mVelocityTracker);
                    mLastMotionY = y;
                    animateOffsetTo(initialVelocity > 0 ? mMenuSize : 0, initialVelocity, true, x);
                } else if (isFirstPointUp) {
                    isFirstPointUp = false;
                    return;
                }
                // Close the menu when content is clicked while the menu is visible.
                else if (mMenuVisible) {
                    closeMenu(true, x);
                }
                break;
            }
            case Position.BOTTOM: {
                if (mIsDragging) {
                    if (mDrawerState == STATE_DRAGGING_CLOSE) {
                        closeMenu(true, x);
                        return;
                    }
                    if (mDrawerState == STATE_DRAGGING_OPEN && willCloseEnough()) {
                        smoothClose(x);
                        return;
                    }
                    mVelocityTracker.computeCurrentVelocity(1000, mMaxVelocity);
                    final int initialVelocity = (int) getYVelocity(mVelocityTracker);
                    mLastMotionX = y;
                    animateOffsetTo(initialVelocity > 0 ? 0 : -mMenuSize, initialVelocity, true, x);
                } else if (isFirstPointUp) {
                    isFirstPointUp = false;
                    return;
                }
                // Close the menu when content is clicked while the menu is visible.
                else if (mMenuVisible) {
                    closeMenu(true, x);
                }
                break;
            }
        }
    }

    protected boolean checkTouchSlop(float dx, float dy) {
        if((getPosition() == Position.LEFT ||getPosition() == Position.RIGHT)){
            return Math.abs(dx) > mTouchSlop && Math.abs(dx) > Math.abs(dy);
        }else if ((getPosition() == Position.TOP) || (getPosition() == Position.BOTTOM)){
            return Math.abs(dy) > mTouchSlop && Math.abs(dy) > Math.abs(dx);
        }else{
            return false;
        }
    }

    @Override
    protected void stopAnimation() {
        super.stopAnimation();
    }

    @Override
    public void requestDisallowInterceptTouchEvent(boolean disallowIntercept) {
        super.requestDisallowInterceptTouchEvent(disallowIntercept);
    }

    private void onPointerUp(MotionEvent ev) {
        final int pointerIndex = ev.getActionIndex();
        final int pointerId = ev.getPointerId(pointerIndex);
        if (pointerId == mActivePointerId) {
            final int newPointerIndex = pointerIndex == 0 ? 1 : 0;
            mLastMotionX = ev.getX(newPointerIndex);
            mActivePointerId = ev.getPointerId(newPointerIndex);
            if (mVelocityTracker != null) {
                mVelocityTracker.clear();
            }
        }
    }

    public boolean onInterceptTouchEvent(MotionEvent ev) {
        final int action = ev.getAction() & MotionEvent.ACTION_MASK;

        if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_CANCEL) {

            mActivePointerId = INVALID_POINTER;
            mIsDragging = false;

            if (mVelocityTracker != null) {
                mVelocityTracker.recycle();
                mVelocityTracker = null;
            }
            if((getPosition()==Position.LEFT)||getPosition()==Position.RIGHT){
                if (Math.abs(mOffsetPixels) > mMenuSize / 2) {
                    openMenu(true, ev.getY());
                } else {
                    closeMenu(true, ev.getY());
                }
            }else if ((getPosition()==Position.TOP)||getPosition()==Position.BOTTOM){
                if (Math.abs(mOffsetPixels) > mMenuSize / 2) {
                    openMenu(true, ev.getX());
                } else {
                    closeMenu(true, ev.getX());
                }
            }
            return false;
        }

        if (action == MotionEvent.ACTION_DOWN && mMenuVisible && isCloseEnough()) {
            setOffsetPixels(0, 0, FlowingMenuLayout.TYPE_NONE);
            stopAnimation();
            setDrawerState(STATE_CLOSED);
            mIsDragging = false;
        }
        // Always intercept events over the content while menu is visible.
        if (mMenuVisible) {
            int index = 0;
            if (mActivePointerId != INVALID_POINTER) {
                index = ev.findPointerIndex(mActivePointerId);
                index = index == -1 ? 0 : index;
            }
            final int x = (int) ev.getX(index);
            final int y = (int) ev.getY(index);
            if (isContentTouch(x,y)) {
                return true;
            }
        }

        if (!mMenuVisible && !mIsDragging && mTouchMode == TOUCH_MODE_NONE) {
            return false;
        }

        if (action != MotionEvent.ACTION_DOWN && mIsDragging) {
            return true;
        }

        switch (action) {
            case MotionEvent.ACTION_DOWN: {
                mLastMotionX = mInitialMotionX = ev.getX();
                mLastMotionY = mInitialMotionY = ev.getY();
                final boolean allowDrag = onDownAllowDrag();
                mActivePointerId = ev.getPointerId(0);

                if (allowDrag) {
                    setDrawerState(mMenuVisible ? STATE_OPEN : STATE_CLOSED);
                    stopAnimation();
                    mIsDragging = false;
                }
                break;
            }

            case MotionEvent.ACTION_MOVE: {
                final int activePointerId = mActivePointerId;
                if (activePointerId == INVALID_POINTER) {
                    // If we don't have a valid id, the touch down wasn't on content.
                    break;
                }

                final int pointerIndex = ev.findPointerIndex(activePointerId);
                if (pointerIndex == -1) {
                    mIsDragging = false;
                    mActivePointerId = INVALID_POINTER;
                    endDrag();
                    closeMenu(true, ev.getY());
                    return false;
                }

                final float x = ev.getX(pointerIndex);
                final float dx = x - mLastMotionX;
                final float y = ev.getY(pointerIndex);
                final float dy = y - mLastMotionY;

                float dis = 0;
                if((getPosition()==Position.LEFT)||getPosition()==Position.RIGHT){
                    dis = dx;
                }else if ((getPosition()==Position.TOP)||getPosition()==Position.BOTTOM){
                    dis = dy;
                }

                if (checkTouchSlop(dx, dy)) {
                    if (mOnInterceptMoveEventListener != null && (mTouchMode == TOUCH_MODE_FULLSCREEN || mMenuVisible)
                            && canChildrenScroll((int) dis, (int) x, (int) y)) {
                        endDrag();
                        // Release the velocity tracker
                        requestDisallowInterceptTouchEvent(true);
                        return false;
                    }
                    final boolean allowDrag = onMoveAllowDrag((int) x, (int) y, dis);
                    if (allowDrag) {
                        stopAnimation();
                        if (mDrawerState == STATE_OPEN || mDrawerState == STATE_OPENING) {
                            setDrawerState(STATE_DRAGGING_CLOSE);
                        } else {
                            setDrawerState(STATE_DRAGGING_OPEN);
                        }
                        mIsDragging = true;
                        mLastMotionX = x;
                        mLastMotionY = y;
                    }
                }
                break;
            }
            case MotionEvent.ACTION_POINTER_UP:
                onPointerUp(ev);
                mLastMotionX = ev.getX(ev.findPointerIndex(mActivePointerId));
                mLastMotionY = ev.getY(ev.findPointerIndex(mActivePointerId));
                break;
        }
        if (mVelocityTracker == null) {
            mVelocityTracker = VelocityTracker.obtain();
        }
        mVelocityTracker.addMovement(ev);
        return mIsDragging;
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if (!mMenuVisible && !mIsDragging && mTouchMode == TOUCH_MODE_NONE) {
            return false;
        }
        final int action = ev.getAction() & MotionEvent.ACTION_MASK;
        if (mVelocityTracker == null) {
            mVelocityTracker = VelocityTracker.obtain();
        }
        mVelocityTracker.addMovement(ev);
        switch (action) {
            case MotionEvent.ACTION_DOWN: {
                mLastMotionX = mInitialMotionX = ev.getX();
                mLastMotionY = mInitialMotionY = ev.getY();
                final boolean allowDrag = onDownAllowDrag();
                mActivePointerId = ev.getPointerId(0);
                if (allowDrag) {
                    stopAnimation();
                    startLayerTranslation();
                }
                break;
            }
            case MotionEvent.ACTION_MOVE: {
                final int pointerIndex = ev.findPointerIndex(mActivePointerId);
                if (pointerIndex == -1) {
                    mIsDragging = false;
                    mActivePointerId = INVALID_POINTER;
                    endDrag();
                    if ((getPosition() == Position.LEFT)||(getPosition() == Position.RIGHT)){
                        closeMenu(true, ev.getY());
                    } else if ((getPosition() == Position.TOP)||(getPosition() == Position.BOTTOM)){
                        closeMenu(true, ev.getX());
                    }

                    return false;
                }
                if (mIsDragging) {
                    startLayerTranslation();
                    final float x = ev.getX(pointerIndex);
                    final float y = ev.getY(pointerIndex);
                    float dis = 0;

                    if ((getPosition() == Position.LEFT)||(getPosition() == Position.RIGHT)){
                        dis = x - mLastMotionX;
                    } else if ((getPosition() == Position.TOP)||(getPosition() == Position.BOTTOM)){
                        dis = y - mLastMotionY;
                    }

                    mLastMotionX = x;
                    mLastMotionY = y;

                    if (mDrawerState == STATE_DRAGGING_OPEN) {
                        if (getPosition() == Position.LEFT) {
                            if (mOffsetPixels + dis < mMenuSize / 2) {
                                onMoveEvent(dis, x,y, FlowingMenuLayout.TYPE_UP_MANUAL);
                            } else {
                                mVelocityTracker.computeCurrentVelocity(1000, mMaxVelocity);
                                final int initialVelocity = (int) getXVelocity(mVelocityTracker);
                                mLastMotionX = x;
                                mLastMotionX = y;
                                animateOffsetTo(mMenuSize, initialVelocity, true, y);
                                isFirstPointUp = true;
                                endDrag();
                            }
                        } else if(getPosition() == Position.RIGHT) {
                            if (mOffsetPixels + dis > - mMenuSize / 2) {
                                onMoveEvent(dis, x, y, FlowingMenuLayout.TYPE_UP_MANUAL);
                            } else {
                                mVelocityTracker.computeCurrentVelocity(1000, mMaxVelocity);
                                final int initialVelocity = (int) getXVelocity(mVelocityTracker);
                                mLastMotionX = x;
                                mLastMotionX = y;
                                animateOffsetTo(-mMenuSize, initialVelocity, true, y);
                                isFirstPointUp = true;
                                endDrag();
                            }
                        } else if(getPosition() == Position.TOP) {
                            if (mOffsetPixels + dis < mMenuSize / 2) {
                                onMoveEvent(dis, x,y, FlowingMenuLayout.TYPE_UP_MANUAL);
                            } else {
                                mVelocityTracker.computeCurrentVelocity(1000, mMaxVelocity);
                                final int initialVelocity = (int) getYVelocity(mVelocityTracker);
                                mLastMotionX = x;
                                mLastMotionX = y;
                                animateOffsetTo(mMenuSize, initialVelocity, true, x);
                                isFirstPointUp = true;
                                endDrag();
                            }
                        } else if(getPosition() == Position.BOTTOM) {
                            if (mOffsetPixels + dis > - mMenuSize / 2) {
                                onMoveEvent(dis, x, y, FlowingMenuLayout.TYPE_UP_MANUAL);
                            } else {
                                mVelocityTracker.computeCurrentVelocity(1000, mMaxVelocity);
                                final int initialVelocity = (int) getYVelocity(mVelocityTracker);
                                mLastMotionX = x;
                                mLastMotionX = y;
                                animateOffsetTo(-mMenuSize, initialVelocity, true, x);
                                isFirstPointUp = true;
                                endDrag();
                            }
                        }
                    } else if (mDrawerState == STATE_DRAGGING_CLOSE) {
                        onMoveEvent(dis, x, y, FlowingMenuLayout.TYPE_DOWN_MANUAL);
                    }
                }
                break;
            }
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP: {
                int index = ev.findPointerIndex(mActivePointerId);
                index = index == -1 ? 0 : index;
                final int x = (int) ev.getX(index);
                final int y = (int) ev.getY(index);
                onUpEvent(x, y);
                mActivePointerId = INVALID_POINTER;
                mIsDragging = false;
                break;
            }
            case MotionEvent.ACTION_POINTER_DOWN:
                final int index = (ev.getAction() & MotionEvent.ACTION_POINTER_INDEX_MASK)
                        >> MotionEvent.ACTION_POINTER_INDEX_SHIFT;
                mLastMotionX = ev.getX(index);
                mLastMotionY = ev.getY(index);
                mActivePointerId = ev.getPointerId(index);
                break;
            case MotionEvent.ACTION_POINTER_UP:
                onPointerUp(ev);
                mLastMotionX = ev.getX(ev.findPointerIndex(mActivePointerId));
                mLastMotionY = ev.getY(ev.findPointerIndex(mActivePointerId));
                break;
        }
        return true;
    }
}
