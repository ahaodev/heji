package com.hao.heji.widget;

import android.content.Context;
import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.view.View;

import com.haibin.calendarview.Calendar;
import com.haibin.calendarview.MonthView;
import com.hao.heji.R;

public class MyMonthView extends MonthView {
    private final Paint mRectPaint = new Paint(Paint.ANTI_ALIAS_FLAG);//抗锯齿设置
    /**
     * 收入画笔
     */
    private final Paint incomePaint = new Paint();

    public MyMonthView(Context context) {
        super(context);

        mRectPaint.setStyle(Paint.Style.STROKE);
        mRectPaint.setStrokeWidth(dipToPx(context, 0.5f));
        mRectPaint.setColor(0x88efefef);

        incomePaint.setAntiAlias(true);
        incomePaint.setStyle(Paint.Style.FILL);
        incomePaint.setTextAlign(Paint.Align.CENTER);
        incomePaint.setFakeBoldText(true);
        incomePaint.setTextSize(dipToPx(getContext(), 9));
        incomePaint.setColor(context.getColor(R.color.income));

        //兼容硬件加速无效的代码
        setLayerType(View.LAYER_TYPE_SOFTWARE, incomePaint);
        //4.0以上硬件加速会导致无效
        mSelectedPaint.setMaskFilter(new BlurMaskFilter(50, BlurMaskFilter.Blur.SOLID));

    }

    /**
     * 绘制选中的日子
     *
     * @param canvas    canvas
     * @param calendar  日历日历calendar
     * @param x         日历Card x起点坐标
     * @param y         日历Card y起点坐标
     * @param hasScheme hasScheme 非标记的日期
     * @return true 则绘制onDrawScheme，因为这里背景色不是是互斥的
     */
    @Override
    protected boolean onDrawSelected(Canvas canvas, Calendar calendar, int x, int y, boolean hasScheme) {
        RectF oval = new RectF(x, y, x + mItemWidth, y + mItemHeight);// 设置个新的长方形
        canvas.drawRoundRect(oval, 20, 20, mSelectedPaint);//第二个参数是x半径，第三个参数是y半径

        //canvas.drawRect(x, y , x + mItemWidth, y + mItemHeight, mSelectedPaint);//方形背景
        return true;
    }

    /**
     * 绘制标记的事件日子
     *
     * @param canvas   canvas
     * @param calendar 日历calendar
     * @param x        日历Card x起点坐标
     * @param y        日历Card y起点坐标
     */
    @SuppressWarnings("IntegerDivisionInFloatingPointContext")
    @Override
    protected void onDrawScheme(Canvas canvas, Calendar calendar, int x, int y) {
        //mSchemeBasicPaint.setColor(calendar.getSchemeColor());
//        List<Calendar.Scheme> schemes = calendar.getSchemes();
//        if (schemes == null || schemes.size() == 0) {
//            return;
//        }
//        int space = dipToPx(getContext(), 2);
//        int indexY = y + mItemHeight - 2 * space;
//        int sw = dipToPx(getContext(), mItemWidth / 10);
//        int sh = dipToPx(getContext(), 4);
//        for (Calendar.Scheme scheme : schemes) {
//            mSchemePaint.setColor(scheme.getShcemeColor());
//            canvas.drawRect(x + mItemWidth - sw - 2 * space, indexY - sh, x + mItemWidth - 2 * space, indexY, mSchemePaint);
//            indexY = indexY - space - sh;
//        }
        //drawScheme(canvas,calendar,x,y,false);
    }

    /**
     * 绘制文本
     *
     * @param canvas     canvas
     * @param calendar   日历calendar
     * @param x          日历Card x起点坐标
     * @param y          日历Card y起点坐标
     * @param hasScheme  是否是标记的日期
     * @param isSelected 是否选中
     */
    @SuppressWarnings("IntegerDivisionInFloatingPointContext")
    @Override
    protected void onDrawText(Canvas canvas, Calendar calendar, int x, int y, boolean hasScheme, boolean isSelected) {
        canvas.drawRect(x, y, x + mItemWidth, y + mItemHeight, mRectPaint);
        int cx = x + mItemWidth / 2;
        int top = y - mItemHeight / 6;

        boolean isInRange = isInRange(calendar);

        mCurDayTextPaint.setColor(getContext().getColor(R.color.colorPrimary));
        //日期
        canvas.drawText(String.valueOf(calendar.getDay()), cx, mTextBaseLine + top, calendar.isCurrentDay() ? mCurDayTextPaint :
                calendar.isCurrentMonth() && isInRange ? mCurMonthTextPaint : mOtherMonthTextPaint);

        if (isSelected) {
            //选中时的字体
            //canvas.drawText(String.valueOf(calendar.getDay()), cx, mTextBaseLine + top, mSelectTextPaint);
            if (hasScheme) {//收支
                drawScheme(canvas, calendar, y, cx, isSelected);
            } else {//农历
                canvas.drawText(calendar.getLunar(), cx, mTextBaseLine + y + mItemHeight / 10, mSelectedLunarTextPaint);
            }
        } else if (hasScheme) { //有收支的日子
            //日期
            //canvas.drawText(String.valueOf(calendar.getDay()), cx, mTextBaseLine + top, calendar.isCurrentMonth() && isInRange ? mSchemeTextPaint : mOtherMonthTextPaint);
            //收入
            drawScheme(canvas, calendar, y, cx, isSelected);
        } else {
            //农历
            canvas.drawText(calendar.getLunar(), cx, mTextBaseLine + y + mItemHeight / 10,
                    calendar.isCurrentDay() && isInRange ? mCurDayLunarTextPaint :
                            calendar.isCurrentMonth() ? mCurMonthLunarTextPaint : mOtherMonthLunarTextPaint);
        }

    }

    private void drawScheme(Canvas canvas, Calendar calendar, int y, int cx, boolean isSelected) {
        int space = dipToPx(getContext(), 2);//间距
        int indexY = (int) (mTextBaseLine + y + mItemHeight / 10);
        //特定日期
        for (int i = 0; i < calendar.getSchemes().size(); i++) {
            Calendar.Scheme scheme = calendar.getSchemes().get(i);
            if (null == scheme.getObj()) return;
            if (isSelected) {
                incomePaint.setColor(mSelectTextPaint.getColor());
            } else {
                incomePaint.setColor(scheme.getShcemeColor());
            }
            canvas.drawText(String.valueOf(scheme.getObj()), cx, indexY, incomePaint);
            indexY = (int) (indexY + incomePaint.getTextSize());
        }
    }


    /**
     * dp转px
     *
     * @param context context
     * @param dpValue dp
     * @return px
     */
    private static int dipToPx(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }
}
