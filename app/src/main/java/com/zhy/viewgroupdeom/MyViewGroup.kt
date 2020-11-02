package com.zhy.viewgroupdeom

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.VelocityTracker
import android.view.ViewConfiguration
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.Scroller
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.marginBottom
import androidx.core.view.marginLeft
import androidx.core.view.marginRight
import androidx.core.view.marginTop


/**
 * ViewGroup
 * 流式布局
 * 简易版
 */
class MyViewGroup(context: Context, attributeSet: AttributeSet) : ViewGroup(context, attributeSet) {
    var baseAdapter: MyAdapter? = null
        set(value) {
            field = value
        }
    private lateinit var mScroller: Scroller
    private var mStart = 0f
    private var mEnd = 0f
    private var mLastY = 0f
    private var mScreenHeight = 0
    private var velocityTracker: VelocityTracker? = null
    private var mViewContentHeight = 0
    private var mViewHeight = 0

    /**
     * 判定拖动的最小像素值
     */
    private var mTouchSlop = 0f
    private var mOverscrollDistance = 0

    // 系统给的最大触摸滑动速度
    private var mMaximumVelocity = 0

    // 系统给的最小触摸滑动速度
    private var mMinimumVelocity = 0

    init {
        mScroller = Scroller(context)

        val viewConfiguration = ViewConfiguration.get(context)
        mTouchSlop = viewConfiguration.scaledTouchSlop.toFloat()
        mOverscrollDistance = viewConfiguration.scaledOverscrollDistance
        mMaximumVelocity = viewConfiguration.scaledMaximumFlingVelocity
        mMinimumVelocity = viewConfiguration.scaledMinimumFlingVelocity
        isClickable = true

    }

    private fun add() {
        removeAllViews()
        baseAdapter?.let {
            for (i in 0 until it.count) {
                addView(it.getView(i, null, this))
            }
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        mViewHeight = h
    }

    override fun onInterceptTouchEvent(ev: MotionEvent?): Boolean {
        when (ev?.action) {
            MotionEvent.ACTION_DOWN -> {
                if (velocityTracker == null) {
                    velocityTracker = VelocityTracker.obtain()
                }
                if (!mScroller.isFinished) {
                    mScroller.abortAnimation()
                }
                mLastY = ev.y
            }
            MotionEvent.ACTION_MOVE -> {
                val moveSlop = Math.abs(ev.y - mLastY)
                if (moveSlop > 0) {
                    //直接被viewgroup消费掉 不在向下传递
                    return true
                }
            }
        }

        return super.onInterceptTouchEvent(ev)
    }


    override fun onTouchEvent(event: MotionEvent?): Boolean {
        when (event?.action) {
            MotionEvent.ACTION_MOVE -> {
                velocityTracker?.addMovement(event)
                val moveY = event.y
                val moveDis = (mLastY - moveY).toInt()
                overScrollBy(0, moveDis, 0, scrollY, 0, getRangY(), 0, 0, true)
                mLastY = event.y
                return true
            }
            MotionEvent.ACTION_UP -> {
                velocityTracker?.computeCurrentVelocity(1000, mMaximumVelocity.toFloat())
                val velocity = velocityTracker?.yVelocity!!
                if (Math.abs(velocity) > mMinimumVelocity) {
                    mScroller.fling(0, scrollY, 0, (-velocity).toInt(), 0, 0, 0, getRangY());
                }
                velocityTracker?.clear();
            }
        }
        return super.onTouchEvent(event)
    }


    override fun onOverScrolled(scrollX: Int, scrollY: Int, clampedX: Boolean, clampedY: Boolean) {
        if (mScroller.isFinished) {
            super.scrollTo(scrollX, scrollY)
        }
    }

    override fun computeScroll() {
        if (mScroller.computeScrollOffset()) {
            scrollTo(mScroller.currX, mScroller.currY)
            postInvalidate()
        }
    }

    fun getRangY(): Int = mViewContentHeight - mViewHeight

    override fun generateLayoutParams(p: LayoutParams?): LayoutParams {
        return MarginLayoutParams(p)
    }

    override fun generateLayoutParams(attrs: AttributeSet?): LayoutParams {
        return MarginLayoutParams(context, attrs)
    }

    override fun generateDefaultLayoutParams(): LayoutParams {
        return MarginLayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
    }

    override fun onAttachedToWindow() {
        if (baseAdapter != null) {
            add()
        }
        super.onAttachedToWindow()
    }

    fun getMaxViewWidth(): Int {
        var maxWidth = 0
        for (i in 0 until childCount) {
            val currentWidth = getChildAt(i).measuredWidth + getChildAt(i).marginLeft + getChildAt(i).marginRight
            if (currentWidth > maxWidth) {
                maxWidth = currentWidth
            }
        }
        return maxWidth
    }

    fun getTotalHeight(): Int {
        var totalHeight = 0
        for (i in 0 until childCount) {
            totalHeight += getChildAt(i).measuredHeight + marginTop + marginBottom
        }
        return totalHeight
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        measureChildren(widthMeasureSpec, heightMeasureSpec)
        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        val widthSize = MeasureSpec.getSize(widthMeasureSpec) - paddingLeft - paddingRight
        val heightMode = MeasureSpec.getMode(heightMeasureSpec)
        val heightSize = MeasureSpec.getSize(heightMeasureSpec) - paddingTop - paddingBottom
        if (widthMode == MeasureSpec.AT_MOST && heightMode == MeasureSpec.AT_MOST) {
            // width : wrap_content height : wrap_content
            val maxViewGroupWidth = getMaxViewWidth()
            val maxViewGroupHeight = getTotalHeight()
            setMeasuredDimension(maxViewGroupWidth, maxViewGroupHeight)
        } else if (widthMode == MeasureSpec.AT_MOST && heightMode == MeasureSpec.EXACTLY) {
            //width:wrap_content height: match_parengt/精确数值
            setMeasuredDimension(getMaxViewWidth(), heightSize)
        } else if (widthMode == MeasureSpec.EXACTLY && heightMode == MeasureSpec.AT_MOST) {
            // width : match_parengt/精确数值 height : wrap_content
            setMeasuredDimension(widthSize, getTotalHeight())
        }


    }

    override fun onLayout(p0: Boolean, l: Int, t: Int, r: Int, b: Int) {
        var indexY = 0 //y轴
        var indexX = 0 //x轴
        var totalSpace = 0 //一行的总和
        var cWidth = 0
        var cHeight = 0
        var viewGroupWidth = width - paddingRight - paddingLeft
        for (i in 0 until childCount) {
            val cView = getChildAt(i).apply {
                cWidth = measuredWidth + marginLeft + marginRight
                cHeight = measuredHeight + marginTop + marginBottom
                setOnClickListener {
                    Toast.makeText(context, baseAdapter?.strings!![i], Toast.LENGTH_SHORT).show()
                }
            }

            if (totalSpace + cWidth > viewGroupWidth) {
                indexY += 1
                indexX = 0
                totalSpace = 0
            }

            var left = totalSpace + cView.marginLeft
            var top = indexY * cHeight + cView.marginTop
            var right = left + cWidth
            var bottom = top + cHeight
            if(indexX == 0) {
                left += paddingLeft
                totalSpace += paddingLeft
            }
            cView.layout(left, top, right, bottom)
            indexX += 1
            totalSpace += cWidth

            mViewContentHeight = cView.top + cHeight
        }
    }

}