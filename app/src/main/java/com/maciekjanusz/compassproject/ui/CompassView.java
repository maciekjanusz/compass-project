package com.maciekjanusz.compassproject.ui;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;

import com.maciekjanusz.compassproject.util.ValueFormatter;

import static java.lang.Math.cos;
import static java.lang.Math.min;
import static java.lang.Math.sin;
import static java.lang.Math.toRadians;

public class CompassView extends View {

    private static final int DEFAULT_SCALE_LINES = 144;
    private static final int SPIKES = 8;
    private static final float STEP_ANGLE = 360f / SPIKES;
    private static final float HALF_STEP_ANGLE = STEP_ANGLE / 2;

    private int strokeWidth = 2; // default
    private int scaleLines;

    private final Paint facePaint = new Paint();
    private final Paint linePaint = new Paint();
    private final Paint northPaint = new Paint();
    private final Paint southPaint = new Paint();
    private final Paint backgroundPaint = new Paint();
    private final Paint navigationPaint = new Paint();
    private final Paint textPaint = new Paint();
    private final Path path = new Path();

    // view center
    private float centerX;
    private float centerY;

    // angle step for drawing scales
    private float scaleAngleStep;

    // radii, largest to smallest
    private float fullRadius;
    private float cardinalsRadius;
    private float faceRadius;
    private float shortSpikeRadius;
    private float spikeCornerRadius;

    // parameter flags
    private boolean compassEnabled;
    private boolean navigationEnabled;
    private boolean cardinalsEnabled;
    private boolean hasBackground;

    // bearing
    private float compassBearing;
    private float navigationBearing;

    // value formatter for drawing cardinals
    private ValueFormatter valueFormatter;

    public CompassView(Context context) {
        super(context);
        init(context);
    }

    public CompassView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public CompassView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public CompassView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context);
    }

    private void init(Context context) {
        initPaint();
        setScaleLines(DEFAULT_SCALE_LINES);
        path.setFillType(Path.FillType.EVEN_ODD);
        valueFormatter = new ValueFormatter(context);
    }

    private void initPaint() {
        linePaint.setColor(Color.BLACK);
        linePaint.setStrokeWidth(strokeWidth);
        linePaint.setStyle(Paint.Style.STROKE);
        linePaint.setAntiAlias(true);

        facePaint.setColor(Color.BLACK);
        facePaint.setStrokeWidth(strokeWidth);
        facePaint.setStyle(Paint.Style.FILL_AND_STROKE);
        facePaint.setAntiAlias(true);

        northPaint.setColor(Color.RED);
        northPaint.setStrokeWidth(strokeWidth);
        northPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        northPaint.setAntiAlias(true);

        southPaint.setColor(Color.BLUE);
        southPaint.setStrokeWidth(strokeWidth);
        southPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        southPaint.setAntiAlias(true);

        backgroundPaint.setColor(Color.WHITE);
        backgroundPaint.setStrokeWidth(0);
        backgroundPaint.setStyle(Paint.Style.FILL);
        backgroundPaint.setAntiAlias(true);

        navigationPaint.setColor(Color.GREEN);
        navigationPaint.setStrokeWidth(0);
        navigationPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        navigationPaint.setAntiAlias(true);

        textPaint.setColor(Color.BLACK);
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setLinearText(true);
        textPaint.setAntiAlias(true);
        textPaint.setStyle(Paint.Style.STROKE);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        float diameter = min(w, h);
        this.centerX = w / 2f;
        this.centerY = h / 2f;

        // fractions are completely arbitrary -> might rewrite for customization
        fullRadius = diameter / 2f;
        cardinalsRadius = fullRadius * 0.9f;
        shortSpikeRadius = cardinalsRadius * 0.7f;
        spikeCornerRadius = cardinalsRadius * 0.2f;
        faceRadius = cardinalsRadius * 0.9f;

        // adjust text size to view size
        textPaint.setTextSize(fullRadius / 12f);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (cardinalsEnabled) {
            drawCardinals(canvas);
        }
        if (navigationEnabled) {
            drawNavigation(canvas);
        }
        if (hasBackground) {
            canvas.drawCircle(centerX, centerY, faceRadius, backgroundPaint);
        }
        if (compassEnabled) {
            drawScaleLines(canvas);
            drawWindrose(canvas);
        }
    }

    private void drawNavigation(Canvas canvas) {
        double degRad = toRadians(compassBearing + navigationBearing - 90);
        double ang1 = degRad - toRadians(HALF_STEP_ANGLE / 3);
        double ang2 = degRad + toRadians(HALF_STEP_ANGLE / 3);

        float endX = (float) (centerX + fullRadius * cos(degRad));
        float endY = (float) (centerY + fullRadius * sin(degRad));

        float vertex1X = (float) (centerX + faceRadius * cos(ang1));
        float vertex1Y = (float) (centerY + faceRadius * sin(ang1));

        float vertex2X = (float) (centerX + faceRadius * cos(ang2));
        float vertex2Y = (float) (centerY + faceRadius * sin(ang2));

        path.moveTo(endX, endY);
        path.lineTo(vertex1X, vertex1Y);
        path.lineTo(vertex2X, vertex2Y);
        path.close();

        canvas.drawPath(path, navigationPaint);
        canvas.drawLine(endX, endY, vertex1X, vertex1Y, facePaint);
        canvas.drawLine(endX, endY, vertex2X, vertex2Y, facePaint);
        path.reset();
    }

    private void drawCardinals(Canvas canvas) {
        float y = centerY - cardinalsRadius;
        canvas.save();
        canvas.rotate(compassBearing, centerX, centerY);

        for (int i = 0; i < 8; i++) {
            float currentDegrees = i * 45;
            String cardinal = valueFormatter.getCardinalDirection(currentDegrees);
            canvas.drawText(cardinal, centerX, y, textPaint);
            canvas.rotate(45, centerX, centerY);
        }

        canvas.restore();
    }

    private void drawScaleLines(Canvas canvas) {
        // maybe it's more efficient to do it with canvas rotating and less trigonometry
        canvas.drawCircle(centerX, centerY, faceRadius, linePaint);
        for (int i = 0; i < scaleLines; i++) {
            float fraction = i % (scaleLines / 12) == 0 ? 0.8f : 0.9f;
            float fromRadius = faceRadius * fraction;

            float degrees = compassBearing + i * scaleAngleStep;
            double degRad = toRadians(degrees);

            float fromX = (float) (centerX + fromRadius * cos(degRad));
            float fromY = (float) (centerY + fromRadius * sin(degRad));
            float toX = (float) (centerX + faceRadius * cos(degRad));
            float toY = (float) (centerY + faceRadius * sin(degRad));

            canvas.drawLine(fromX, fromY, toX, toY, facePaint);
        }
    }

    private void drawWindrose(Canvas canvas) {
        // maybe it's more efficient to do it with canvas rotating and less trigonometry
        for (int i = 0; i < SPIKES; i++) {
            float radius = i % 2 == 0 ? faceRadius : shortSpikeRadius;
            float degrees = compassBearing + i * STEP_ANGLE;
            float plusDegrees = degrees + HALF_STEP_ANGLE;
            float minusDegrees = degrees - HALF_STEP_ANGLE;
            double degRad = toRadians(degrees);
            double plusDegRad = toRadians(plusDegrees);
            double minusDegRad = toRadians(minusDegrees);

            float endX = (float) (centerX + radius * cos(degRad));
            float vertexX = (float) (centerX + spikeCornerRadius * cos(plusDegRad));
            float backVertexX = (float) (centerX + spikeCornerRadius * cos(minusDegRad));
            float endY = (float) (centerY + radius * sin(degRad));
            float vertexY = (float) (centerY + spikeCornerRadius * sin(plusDegRad));
            float backVertexY = (float) (centerY + spikeCornerRadius * sin(minusDegRad));

            path.moveTo(centerX, centerY);
            path.lineTo(endX, endY);
            path.lineTo(vertexX, vertexY);
            path.close();
            canvas.drawPath(path, facePaint);
            path.reset();

            // 6 is north, 2 is south - ugly but whatever
            if (i == 6 || i == 2) {
                path.moveTo(centerX, centerY);
                path.lineTo(endX, endY);
                path.lineTo(backVertexX, backVertexY);
                path.close();
                canvas.drawPath(path, i == 6 ? northPaint : southPaint);
                path.reset();
                // correct line from center to smallradius
                canvas.drawLine(centerX, centerY, backVertexX, backVertexY, facePaint);
            }

            canvas.drawLine(endX, endY, backVertexX, backVertexY, facePaint);
        }
    }

    public boolean isCompassEnabled() {
        return compassEnabled;
    }

    public void setCompassEnabled(boolean compassEnabled) {
        this.compassEnabled = compassEnabled;
    }

    public boolean isNavigationEnabled() {
        return navigationEnabled;
    }

    public void setNavigationEnabled(boolean navigationEnabled) {
        this.navigationEnabled = navigationEnabled;
    }

    public float getCompassBearing() {
        return compassBearing;
    }

    public void setCompassBearing(float compassBearing) {
        this.compassBearing = compassBearing;
        invalidate();
    }

    public float getNavigationBearing() {
        return navigationBearing;
    }

    public void setNavigationBearing(float navigationBearing) {
        this.navigationBearing = navigationBearing;
        invalidate();
    }

    public void setHasBackground(boolean hasBackground) {
        this.hasBackground = hasBackground;
    }

    public void setStrokeWidth(int strokeWidth) {
        this.strokeWidth = strokeWidth;
    }

    public boolean isCardinalsEnabled() {
        return cardinalsEnabled;
    }

    public void setCardinalsEnabled(boolean cardinalsEnabled) {
        this.cardinalsEnabled = cardinalsEnabled;
    }

    public int getScaleLines() {
        return scaleLines;
    }

    public void setScaleLines(int scaleLines) {
        this.scaleLines = scaleLines;
        this.scaleAngleStep = (360f / scaleLines);
    }
}
