package com.example.jianpan.mydoubletriangle;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Region;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by sbb on 2016/9/26.
 * 双三角布局
 */
public class DoubleTriangleLayout extends ViewGroup {

    public interface TriangleOnClickListener{
        void leftTriangleOnClick(View view);
        void rightTriangleOnClick(View view);
    }
    public TriangleOnClickListener mTriangleOnClickListener;

    private View mTriangleL;//左边的view
    private View mTriangleR;//右边的view
    private SlashView mSlashView;//线

    private int mLastPointX, mLastPointY;
    private Path mPathL, mPathR;
    private  RectF mRectFL, mRectFR;
    private Region mRegionL;
    private Region mRegionR;

    public DoubleTriangleLayout(Context context) {
        this(context, null);
    }

    public DoubleTriangleLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DoubleTriangleLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        mPathL = new Path();
        mPathR = new Path();
        mRectFL = new RectF();
        mRectFR = new RectF();
        mRegionL = new Region();
        mRegionR = new Region();
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int parentDesireWidth = 0;
        int parentDesireHeight = 0;
        for (int i = 0; i < getChildCount(); i++){ // -1 不计算线的宽
            View childView = getChildAt(i);
            CustomLayoutParams margin = (CustomLayoutParams) childView.getLayoutParams();
            measureChildWithMargins(childView, widthMeasureSpec, 0, heightMeasureSpec, 0);

            int width = childView.getMeasuredWidth() + margin.leftMargin + margin.rightMargin;
            int height = childView.getMeasuredHeight() + margin.topMargin + margin.bottomMargin;
            if (parentDesireWidth < width){
                parentDesireWidth = width;
            }
            if (parentDesireHeight < height){
                parentDesireHeight = height;
            }
        }

        parentDesireWidth += getPaddingLeft() + getPaddingRight();
        parentDesireHeight += getPaddingTop() + getPaddingLeft();

        setMeasuredDimension(resolveSize(parentDesireWidth, widthMeasureSpec), resolveSize(parentDesireHeight, heightMeasureSpec));

        setTrianglePath();
    }

    /**
     * 左边三角的绘制路径
     * <ul>
     *         <li>  起点： --------------->  </li>
     *         <li>        ↖             /   </li>
     *         <li>         \           /    </li>
     *         <li>          \        /      </li>
     *         <li>          \      /        </li>
     *     <li>                |             </li>
     * </ul>
     *
     * 右边三角的绘制路径
     * <ul>
     *         <li>           |            </li>
     *         <li>         /   ↖         </li>
     *         <li>       /       \       </li>
     *         <li>     /          \      </li>
     *         <li>   ↙            \     </li>
     *     <li>起点： --------------->    </li>
     * </ul>
     */
    private void setTrianglePath() {
        mPathL.moveTo(getPaddingLeft(), getPaddingTop());
        mPathL.lineTo(mTriangleL.getMeasuredWidth() + getPaddingLeft(), getPaddingTop());
        mPathL.lineTo(getPaddingLeft(), mTriangleL.getMeasuredHeight() + getPaddingTop());
        mPathL.close();

        mPathR.moveTo(getPaddingLeft(), mTriangleR.getMeasuredHeight() + getPaddingTop());
        mPathR.lineTo(mTriangleR.getMeasuredWidth() + getPaddingLeft(), mTriangleR.getMeasuredHeight() + getPaddingTop());
        mPathR.lineTo(mTriangleR.getMeasuredWidth() + getPaddingLeft(), getPaddingTop());
        mPathR.close();

        mPathL.computeBounds(mRectFL, true);
        mPathR.computeBounds(mRectFR, true);
        mRegionL.setPath(mPathL, new Region((int) mRectFL.left, (int) mRectFL.top, (int) mRectFL.right, (int) mRectFL.bottom));
        mRegionR.setPath(mPathR, new Region((int) mRectFR.left, (int) mRectFR.top, (int) mRectFR.right, (int) mRectFR.bottom));
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        for (int i = 0; i < getChildCount(); i++){
            View childView = getChildAt(i);
            CustomLayoutParams margin = (CustomLayoutParams) childView.getLayoutParams();
            int left = getPaddingLeft() + margin.leftMargin;
            int top  = getPaddingTop() + margin.topMargin;
            childView.layout(left, top, childView.getMeasuredWidth() + left, childView.getMeasuredHeight() + top);
        }
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        //画左边的三角
        canvas.save();
        canvas.clipPath(mPathL);
        drawChild(canvas, mTriangleL, getDrawingTime());
        canvas.restore();
        //画右边的三角
        canvas.save();
        canvas.clipPath(mPathR);
        drawChild(canvas, mTriangleR, getDrawingTime());
        canvas.restore();
        //画线
        drawChild(canvas, mSlashView, getDrawingTime());
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mTriangleL = getChildAt(0);
        mTriangleR = getChildAt(1);
        mSlashView = new SlashView(getContext());
        mSlashView.setClickable(false);
        mSlashView.setLayoutParams(mTriangleL.getLayoutParams());//设置线的params为左边的三角或右边的三角
        addView(mSlashView);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()){
            case MotionEvent.ACTION_DOWN:
                mLastPointX = (int) event.getX();
                mLastPointY = (int) event.getY();
                break;

            case MotionEvent.ACTION_MOVE:
                break;

            case MotionEvent.ACTION_UP:
                //判断为一个点击事件
                if (Math.abs(mLastPointX - event.getX()) < 25 && Math.abs(mLastPointY - event.getY()) < 25){
                    defineRegion((int) event.getX(), (int) event.getY());
                }
                break;
        }
        return true;
    }
    /** 点击的范围 */
    private void defineRegion(int x, int y) {
        if (mTriangleOnClickListener != null) {
            if (mRegionL.contains(x, y)) {
                mTriangleOnClickListener.leftTriangleOnClick(mTriangleL);
            } else if (mRegionR.contains(x, y)) {
                mTriangleOnClickListener.rightTriangleOnClick(mTriangleR);
            }
        }
    }

    public void setTriangleOnClickListener(TriangleOnClickListener onClickListener){
        this.mTriangleOnClickListener = onClickListener;
    }

    @Override
    protected CustomLayoutParams generateDefaultLayoutParams() {
        return new CustomLayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
    }

    @Override
    protected android.view.ViewGroup.LayoutParams generateLayoutParams(android.view.ViewGroup.LayoutParams p) {
        return new CustomLayoutParams(p);
    }

    @Override
    public android.view.ViewGroup.LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new CustomLayoutParams(getContext(), attrs);
    }

    @Override
    protected boolean checkLayoutParams(android.view.ViewGroup.LayoutParams p) {
        return p instanceof CustomLayoutParams;
    }

    public static class CustomLayoutParams extends MarginLayoutParams {

        public CustomLayoutParams(MarginLayoutParams source) {
            super(source);
        }

        public CustomLayoutParams(android.view.ViewGroup.LayoutParams source) {
            super(source);
        }

        public CustomLayoutParams(Context c, AttributeSet attrs) {
            super(c, attrs);
        }

        public CustomLayoutParams(int width, int height) {
            super(width, height);
        }
    }

    /**
     * Created by 键盘 on 2016/9/26.
     * 斜线
     */
    public class SlashView extends View{

        private final int SLASH_WIDTH = 4;
        private final int SLASH_COLOR = 0xffcccccc;

        private Paint mPaint;

        public SlashView(Context context) {
            this(context, null);
        }

        public SlashView(Context context, AttributeSet attrs) {
            this(context, attrs, 0);
        }

        public SlashView(Context context, AttributeSet attrs, int defStyleAttr) {
            super(context, attrs, defStyleAttr);

            mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            mPaint.setStrokeWidth(SLASH_WIDTH);
            mPaint.setColor(SLASH_COLOR);
        }

        @Override
        protected void onDraw(Canvas canvas) {
            canvas.drawLine(0, getMeasuredHeight(), getMeasuredWidth(), 0, mPaint);
            super.onDraw(canvas);
        }

        public int getSlashWidth(){
            return SLASH_WIDTH;
        }
    }
}
