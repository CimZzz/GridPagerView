package com.virtualightning.gridpagerview;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.os.Build;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;

/**
 * Created by CimZzz on 13/6/17.<br>
 * Project Name : Virtual-Lightning GridPagerView<br>
 * Since : GridPagerView_0.0.1<br>
 * Description:<br>
 * Description
 */
public class GridPagerView extends ViewGroup {
    private static final int DEFAULT_ROW = 2;
    private static final int DEFAULT_COL = 4;
    static final int PAGE_NUM = -191923;
    static final int WRAPPER_KEY = -120232;

    private static float OVER_PAGE_SPEED = 350;

    public static final float INTERCEPT_LENGTH = 100;
    public static final int GAP_NONE = 0;
    public static final int GAP_AVG = -1;
    public static final int GAP_CUSTOM = -2;

    public static final int SCROLL_DISTANCE = 0;
    public static final int SCROLL_SPEED = -1;

    public static final int SIZE_UNDEFINED = -1;

    private int paddingLeft;
    private int paddingRight;
    private int paddingTop;
    private int paddingBottom;

    private int rowCount;
    private int colCount;
    private int pageCap;

    private int horGap;
    private int horGapFlag;
    private int verGap;
    private int verGapFlag;

    private int childWidth;
    private int childHeight;

    private int requireWidth;
    private int requireHeight;

    private int curPage;
    private int endPage;

    private float prevX;
    private int prevScrollX;
    private long prevTime;

    private int scrollFlag;

    private float interceptX;

    private Scroller scroller;

    private GridPagerAdapter adapter;
    private ViewPool viewPool;
    private OnPageChangeListener pageChangeListener;

    public GridPagerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context,attrs);
    }

    public GridPagerView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context,attrs);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public GridPagerView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context,attrs);
    }

    private void init(Context context,AttributeSet attrs) {
        viewPool = new ViewPool();
        scroller = new Scroller();

        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.GridPagerView);

        rowCount = typedArray.getInt(R.styleable.GridPagerView_rowCount,DEFAULT_ROW);
        colCount = typedArray.getInt(R.styleable.GridPagerView_colCount,DEFAULT_COL);
        pageCap = rowCount * colCount;
        requireWidth = (int) typedArray.getDimension(R.styleable.GridPagerView_requiredWidth,SIZE_UNDEFINED);
        requireHeight = (int) typedArray.getDimension(R.styleable.GridPagerView_requiredHeight,SIZE_UNDEFINED);
        verGap = (int) typedArray.getDimension(R.styleable.GridPagerView_verGap,0);
        horGap = (int) typedArray.getDimension(R.styleable.GridPagerView_horGap,0);
        verGapFlag = horGapFlag = typedArray.getInt(R.styleable.GridPagerView_gapStyle,GAP_AVG);

        curPage = 0;

        paddingLeft = getPaddingLeft();
        paddingTop = getPaddingTop();
        paddingRight = getPaddingRight();
        paddingBottom = getPaddingBottom();

        setPadding(0,0,0,0);

        typedArray.recycle();
    }

    public void update() {
        int size = getChildCount();
        while (size-- > 0) {
            View view = getChildAt(0);
            removeViewAt(0);
            ViewWrapper wrapper = (ViewWrapper) view.getTag(WRAPPER_KEY);
            viewPool.cacheView(wrapper);
        }

        this.endPage = (this.adapter.getItemCount() - 1) / pageCap;
        this.scroller.abortAnimation();
        if(curPage > endPage) {
            curPage = endPage;
            smoothTo(scroller.getCurrentX(), 0, curPage * getWidth(), 0);
        }
        onPageChange(curPage,endPage);

        addViewFromAdapter(adapter,curPage - 1);
        addViewFromAdapter(adapter,curPage);
        addViewFromAdapter(adapter,curPage + 1);

        requestLayout();
    }

    public void setAdapter(GridPagerAdapter adapter) {
        this.adapter = adapter;
        this.adapter.pagerView = this;
        this.removeAllViews();
        this.viewPool.clear();
        curPage = 0;

        this.endPage = (this.adapter.getItemCount() - 1) / pageCap;
        onPageChange(curPage,endPage);
        addViewFromAdapter(adapter,curPage - 1);
        addViewFromAdapter(adapter,curPage);
        addViewFromAdapter(adapter,curPage + 1);

        requestLayout();
    }

    public void setPageChangeListener(OnPageChangeListener pageChangeListener) {
        this.pageChangeListener = pageChangeListener;
    }

    /**
     * 重写函数
     */
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int width = getRawSize(widthMeasureSpec);
        int height = getRawSize(heightMeasureSpec);

        int horGapCount = colCount - 1;
        int verGapCount = rowCount - 1;

        int realWidth = width - paddingLeft - paddingRight;
        int realHeight = height - paddingTop - paddingBottom;

        switch (verGapFlag) {
            case GAP_CUSTOM:
                int totalVerGap = verGap * verGapCount;

                if(totalVerGap < realHeight)
                    childHeight = (realHeight - totalVerGap) / rowCount;
                else  {
                    verGap = realHeight / verGapCount;
                    childHeight = 0;
                }

                break;
            case GAP_AVG:
                if(requireHeight != SIZE_UNDEFINED) {
                    int totalHeight = requireHeight * rowCount;

                    if(totalHeight < realHeight) {
                        verGap = (realHeight - totalHeight) / verGapCount;
                        break;
                    }
                }
            default:
                childHeight = realHeight / rowCount;
                verGap = 0;
                break;
        }


        switch (horGapFlag) {
            case GAP_CUSTOM:
                int totalHorGap = horGap * horGapCount;

                if(totalHorGap < realWidth)
                    childWidth = (realWidth - totalHorGap) / colCount;
                else  {
                    horGap = realWidth / horGapCount;
                    childWidth = 0;
                }

                break;
            case GAP_AVG:
                if(requireWidth != SIZE_UNDEFINED) {
                    int totalWidth = requireWidth * colCount;

                    if(totalWidth < realWidth) {
                        horGap = (realWidth - totalWidth) / horGapCount;
                        break;
                    }
                }
            default:
                childWidth = realWidth / colCount;
                horGap = 0;
                break;
        }

        int childMeasuredWidth = getMeasureSize(childWidth);
        int childMeasuredHeight = getMeasureSize(childHeight);

        measureChildren(childMeasuredWidth,childMeasuredHeight);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        if(getChildCount() == 0)
            return;

        if(curPage == 0) {
            setLayout(0);
            setLayout(1);
        } else if (curPage == endPage) {
            setLayout(curPage);
            setLayout(curPage - 1);
        } else {
            setLayout(curPage - 1);
            setLayout(curPage);
            setLayout(curPage + 1);
        }
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                prevX = ev.getX();
                prevScrollX = getScrollX();
                prevTime = System.currentTimeMillis();
                break;
            case MotionEvent.ACTION_MOVE:
                float distanceX = Math.abs(prevX - ev.getX());
                if(distanceX > INTERCEPT_LENGTH)
                    return true;
                break;
        }
        return super.onInterceptTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int endScrollX = endPage * getWidth();
        scroller.abortAnimation();

        switch (event.getAction()) {
            case MotionEvent.ACTION_MOVE:
                float distanceX = prevX - event.getX();
                int nextScrollX = (int) (prevScrollX + distanceX);
                if(nextScrollX <= 0)
                    nextScrollX /= 3;
                else if (nextScrollX >= endScrollX)
                    nextScrollX = endScrollX + (nextScrollX - endScrollX) / 3;

                scrollTo(nextScrollX,0);
                break;
            case MotionEvent.ACTION_UP:
                float displacement = prevX - event.getX();
                float displacementAbs = Math.abs(displacement);
                float changeDistance = getWidth() / 3;
                int startX = getScrollX();
                int startY = getScrollY();
                int endX;
                int endY = startY;

                long betweenTime = System.currentTimeMillis() - prevTime;
                float speed = displacementAbs / betweenTime * 1000;

                if(speed >= OVER_PAGE_SPEED)
                    displacement = displacement > 0 ? changeDistance + 1 : -changeDistance - 1;

                if(displacement < -changeDistance && curPage != 0) {
                    endX = (curPage - 1) * getWidth();
                    pageChange(-1);
                    onPageChange(curPage,endPage);
                }
                else if(displacement > changeDistance && curPage != endPage) {
                    endX = (curPage + 1) * getWidth();
                    pageChange(1);
                    onPageChange(curPage,endPage);
                }
                else endX = curPage * getWidth();

                if(endX <= 0)
                    endX = 0;
                else if (endX >= endScrollX)
                    endX = endScrollX;

                smoothTo(startX,startY,endX,endY);

                break;
        }

        return true;
    }

    @Override
    public void computeScroll() {
        super.computeScroll();
        if (!scroller.isFinished() && scroller.computeScrollOffset()) {
            int oldX = getScrollX();
            int oldY = getScrollY();
            int x = scroller.getCurrentX();
            int y = scroller.getCurrentY();

            if (oldX != x || oldY != y) {
                super.scrollTo(x, y);
            }

            ViewCompat.postInvalidateOnAnimation(this);
        }
    }

    @Override
    protected ViewGroup.LayoutParams generateLayoutParams(ViewGroup.LayoutParams p) {
        return new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
    }

    @Override
    protected ViewGroup.LayoutParams generateDefaultLayoutParams() {
        return new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
    }

    @Override
    public ViewGroup.LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
    }

    @Override
    public void addView(View child) {
    }

    @Override
    public void addView(View child, int index) {
    }

    @Override
    public void addView(View child, int index, ViewGroup.LayoutParams params) {
    }

    @Override
    public void addView(View child, ViewGroup.LayoutParams params) {
    }

    @Override
    public void addView(View child, int width, int height) {
    }



    /**
     * 内部方法
     */
    private int getRawSize(int measureSpec){
        return MeasureSpec.getSize(measureSpec);
    }

    private int getMeasureSize(int size){
        return MeasureSpec.makeMeasureSpec(size,MeasureSpec.EXACTLY);
    }

    private boolean addViewFromAdapter(GridPagerAdapter adapter,int pageNum) {
        if(pageNum < 0 || pageNum > endPage)
            return false;
        int pageStart = pageNum * pageCap;
        int pageEnd = pageNum * pageCap + pageCap;
        int itemCount = adapter.getItemCount();
        int i = pageStart;
        for( ; i < pageEnd && i < itemCount; i ++) {
            View itemView = adapter.getChildView(getContext(),this,viewPool,i).rootView;
            itemView.setTag(PAGE_NUM,pageNum);
            super.addView(itemView,-1,null);
        }

        return i == pageEnd;
    }

    private PageRegion getPageRegion(int pageNum) {
        int rightBounded = getChildCount();

        PageRegion pageRegion = null;

        boolean isFounded = false;
        for(int i = 0 ; i < rightBounded ; i ++) {
            View child = getChildAt(i);
            int childPageNum = (int)child.getTag(PAGE_NUM);

            if(childPageNum== pageNum) {
                if(pageRegion == null) {
                    pageRegion = new PageRegion();
                    pageRegion.pageNum = pageNum;
                    pageRegion.startIndex = i;
                    pageRegion.overEndIndex = i + 1;
                    isFounded = true;
                } else
                    pageRegion.overEndIndex = i + 1;
            }
            else if(isFounded)
                break;
        }

        return pageRegion;
    }

    private void setLayout(int pageNum) {
        PageRegion region = getPageRegion(pageNum);

        if(region == null)
            return;

        int startTop = paddingTop;
        int startLeft = region.pageNum * getWidth() + paddingLeft;

        int topWhole = startTop;
        boolean isRowFirst = true;
        for(int i = 0; i < rowCount ; i ++) {
            if(isRowFirst)
                isRowFirst = false;
            else topWhole += verGap;

            int leftWhole = startLeft;
            boolean isColFirst = true;

            for(int j = 0 ; j < colCount ; j ++) {
                int childIndex = region.nextIndex();

                if (childIndex == -1)
                    return;

                if(isColFirst)
                    isColFirst = false;
                else leftWhole += horGap;

                int right = leftWhole + childWidth;
                int bottom = topWhole + childHeight;

                View child = getChildAt(childIndex);
                child.layout(leftWhole, topWhole,right,bottom);

                leftWhole += childWidth;
            }

            topWhole += childHeight;
        }
    }

    private void pageChange(int pageFlag) {
        if(pageFlag == -1) {
            if(curPage == 0)
                return;

            if(curPage != endPage) {
                PageRegion pageRegion = getPageRegion(curPage + 1);
                if(pageRegion != null) {
                    pageRegion.removeAll();
                }
            }

            curPage = curPage - 1;
            addViewFromAdapter(adapter,curPage - 1);
        } else if (pageFlag == 1) {
            if(curPage == endPage)
                return;

            if(curPage != 0) {
                PageRegion pageRegion = getPageRegion(curPage - 1);
                if(pageRegion != null) {
                    pageRegion.removeAll();
                }
            }

            curPage = curPage + 1;
            addViewFromAdapter(adapter,curPage + 1);
        }
    }

    private void smoothTo(int startX,int startY,int endX,int endY)
    {
        if(!scroller.isFinished())
            scroller.abortAnimation();


        int distanceX = endX - startX;
        int distanceY = endY - startY;

        int duration = (int) (((float)Math.abs(distanceX) / getWidth() + 1)) * 100;

        scroller.startScroll(startX,startY,endX,endY,duration);

        ViewCompat.postInvalidateOnAnimation(this);
    }

    private void onPageChange(int curPage,int totalPage) {
        if(pageChangeListener != null)
            pageChangeListener.onPageChange(curPage, totalPage);
    }
    /**
     * 内部类
     */
    public static class LayoutParams extends ViewGroup.LayoutParams {

        public LayoutParams(int width, int height) {
            super(width, height);
        }
    }

    private class PageRegion {
        private int startIndex;
        private int overEndIndex;
        private int pageNum;

        private int nextIndex() {
            int index = startIndex ++;
            return index >= overEndIndex ? -1 : index;
        }

        private void removeAll() {
            for(int i = startIndex;i < overEndIndex ; i ++) {
                View view = getChildAt(startIndex);
                removeViewAt(startIndex);
                ViewWrapper wrapper = (ViewWrapper) view.getTag(WRAPPER_KEY);
                viewPool.cacheView(wrapper);
            }
        }
    }

    private static class Scroller {
        private int startX;
        private int startY;
        private int finishX;
        private int finishY;
        private int deltaX;
        private int deltaY;
        private int duration;
        private long startTime;

        private int currentX;
        private int currentY;

        private float durationReciprocal;
        private float currentReciprocal;

        private boolean isFinish;

        private void startScroll(int startX,int startY,int finishX,int finishY,int duration) {
            isFinish = false;
            this.startTime = AnimationUtils.currentAnimationTimeMillis();
            this.duration = duration;
            this.startX = startX;
            this.startY = startY;
            this.finishX = finishX;
            this.finishY = finishY;
            this.deltaX = finishX - startX;
            this.deltaY = finishY - startY;
            this.startX = startX;
            this.startX = startX;
            this.durationReciprocal = 1.0f / duration;
        }

        boolean computeScrollOffset()
        {
            if (isFinish) {
                return false;
            }

            int timePassed = (int)(AnimationUtils.currentAnimationTimeMillis() - startTime);

            if (timePassed < duration) {
                currentReciprocal = timePassed * durationReciprocal;
                currentX = startX + Math.round(currentReciprocal * deltaX);
                currentY = startY + Math.round(currentReciprocal * deltaY);
            }
            else {
                currentX = finishX;
                currentY = finishY;
                isFinish = true;
            }
            return true;
        }

        void abortAnimation() {
            isFinish = true;
        }

        int getCurrentX() {
            return currentX;
        }

        int getCurrentY() {
            return currentY;
        }

        public boolean isFinished() {
            return isFinish;
        }
    }

    @Override
    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        super.onScrollChanged(l, t, oldl, oldt);
    }
}
