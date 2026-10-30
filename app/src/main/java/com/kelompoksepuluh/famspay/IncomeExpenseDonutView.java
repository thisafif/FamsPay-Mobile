package com.kelompoksepuluh.famspay;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

public class IncomeExpenseDonutView extends View {
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF arcBounds = new RectF();
    private long income;
    private long expense;

    public IncomeExpenseDonutView(Context context) {
        super(context);
        init();
    }

    public IncomeExpenseDonutView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public IncomeExpenseDonutView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeCap(Paint.Cap.BUTT);
    }

    public void setData(long income, long expense) {
        this.income = Math.max(0, income);
        this.expense = Math.max(0, expense);
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int width = getWidth();
        int height = getHeight();
        float size = Math.min(width, height);
        float strokeWidth = size * 0.18f;
        float inset = strokeWidth / 2f;
        float left = (width - size) / 2f + inset;
        float top = (height - size) / 2f + inset;
        arcBounds.set(left, top, left + size - strokeWidth, top + size - strokeWidth);

        paint.setStrokeWidth(strokeWidth);
        paint.setColor(ContextCompat.getColor(getContext(), R.color.light_gray));
        canvas.drawArc(arcBounds, -90f, 360f, false, paint);

        long total = income + expense;
        if (total <= 0) {
            return;
        }

        float incomeSweep = (income * 360f) / total;
        float expenseSweep = 360f - incomeSweep;
        float start = -90f;

        if (income > 0) {
            paint.setColor(ContextCompat.getColor(getContext(), R.color.green));
            canvas.drawArc(arcBounds, start, incomeSweep, false, paint);
            start += incomeSweep;
        }

        if (expense > 0) {
            paint.setColor(ContextCompat.getColor(getContext(), R.color.trans_red));
            canvas.drawArc(arcBounds, start, expenseSweep, false, paint);
        }
    }
}
