package me.littlecheesecake.qrcodescanner.core;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;

import me.littlecheesecake.qrcodescanner.R;

/**
 * Created by yulu on 1/12/15.
 */
public class ViewFinderView extends View implements ViewFinder {
    private static final String TAG = "ViewFinderView";

    private Rect mFramingRect;

    private static final int MIN_FRAME_WIDTH = 240;
    private static final int MIN_FRAME_HEIGHT = 240;

    private static final float LANDSCAPE_WIDTH_RATIO = 5f/8;
    private static final float LANDSCAPE_HEIGHT_RATIO = 5f/8;
    private static final int LANDSCAPE_MAX_FRAME_WIDTH = (int) (1920 * LANDSCAPE_WIDTH_RATIO); // = 5/8 * 1920
    private static final int LANDSCAPE_MAX_FRAME_HEIGHT = (int) (1080 * LANDSCAPE_HEIGHT_RATIO); // = 5/8 * 1080

    private static final float PORTRAIT_WIDTH_RATIO = 6f/8;
    private static final float PORTRAIT_HEIGHT_RATIO = 3f/8;
    private static final int PORTRAIT_MAX_FRAME_WIDTH = (int) (1080 * PORTRAIT_WIDTH_RATIO); // = 7/8 * 1080
    private static final int PORTRAIT_MAX_FRAME_HEIGHT = (int) (1920 * PORTRAIT_HEIGHT_RATIO); // = 3/8 * 1920

    private static final String MASK_COLOR = "#60000000";
    private static final String BORDER_COLOR = "#ffafed44";
    private static final int BORDER_STROKE_WIDTH = 10;
    private static final int BORDER_LINE_LENGTH = 100;

    private static final int SCANNER_RATE = 10;

    private int mDefaultMaskColor;
    private int mDefaultBorderColor;
    private int mDefaultBorderStrokeWidth;
    private int mDefaultBorderLineLength;

    protected Paint mLaserPaint;
    protected Paint mFinderMaskPaint;
    protected Paint mBorderPaint;
    protected int mBorderLineLength;

    protected Bitmap scannerView;
    protected int offset;
    protected int minOffset;

    public ViewFinderView(Context context) {
        super(context);
        init(context, null);
    }

    public ViewFinderView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public ViewFinderView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        obtainAttributes(context, attrs);

        //set up laser paint
        mLaserPaint = new Paint();
        mLaserPaint.setStyle(Paint.Style.FILL);

        //finder mask paint
        mFinderMaskPaint = new Paint();
        mFinderMaskPaint.setColor(mDefaultMaskColor);

        //border paint
        mBorderPaint = new Paint();
        mBorderPaint.setColor(mDefaultBorderColor);
        mBorderPaint.setStyle(Paint.Style.STROKE);
        mBorderPaint.setStrokeWidth(mDefaultBorderStrokeWidth);

        mBorderLineLength = mDefaultBorderLineLength;

        scannerView = BitmapFactory.decodeResource(context.getResources(), R.drawable.scan_bar);
    }

    private void obtainAttributes(Context context, AttributeSet attrs) {
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.ScannerLayout);

        mDefaultMaskColor = ta.getColor(R.styleable.ScannerLayout_sc_mask_color, Color.parseColor(MASK_COLOR));
        mDefaultBorderColor = ta.getColor(R.styleable.ScannerLayout_sc_border_color, Color.parseColor(BORDER_COLOR));
        mDefaultBorderStrokeWidth = ta.getInt(R.styleable.ScannerLayout_sc_border_stroke_width, BORDER_STROKE_WIDTH);
        mDefaultBorderLineLength = ta.getInt(R.styleable.ScannerLayout_sc_border_line_length, BORDER_LINE_LENGTH);
    }

    public void setLaserColor(int laserColor) {
        mLaserPaint.setColor(laserColor);
    }
    public void setMaskColor(int maskColor) {
        mFinderMaskPaint.setColor(maskColor);
    }
    public void setBorderColor(int borderColor) {
        mBorderPaint.setColor(borderColor);
    }
    public void setBorderStrokeWidth(int borderStrokeWidth) {
        mBorderPaint.setStrokeWidth(borderStrokeWidth);
    }
    public void setBorderLineLength(int borderLineLength) {
        mBorderLineLength = borderLineLength;
    }

    public void setupViewFinder() {
        updateFramingRect();
        invalidate();
    }


    public Rect getFramingRect() {
        return mFramingRect;
    }

    @Override
    public void onDraw(Canvas canvas) {
        if(mFramingRect == null) {
            return;
        }

        drawViewFinderMask(canvas);
        drawViewFinderBorder(canvas);
        drawLaser(canvas);
        invalidate();
    }

    public void drawViewFinderMask(Canvas canvas) {
        int width = canvas.getWidth();
        int height = canvas.getHeight();

        canvas.drawRect(0, 0, width, mFramingRect.top, mFinderMaskPaint);
        canvas.drawRect(0, mFramingRect.top, mFramingRect.left, mFramingRect.bottom + 1, mFinderMaskPaint);
        canvas.drawRect(mFramingRect.right + 1, mFramingRect.top, width, mFramingRect.bottom + 1, mFinderMaskPaint);
        canvas.drawRect(0, mFramingRect.bottom + 1, width, height, mFinderMaskPaint);
    }

    public void drawViewFinderBorder(Canvas canvas) {
        int width = mDefaultBorderStrokeWidth;
        int halfWidth = width / 2;
        canvas.drawLine(mFramingRect.left - halfWidth, mFramingRect.top - width,
                mFramingRect.left - halfWidth, mFramingRect.top + mBorderLineLength - width, mBorderPaint);
        canvas.drawLine(mFramingRect.left - width, mFramingRect.top - halfWidth,
                mFramingRect.left - width + mBorderLineLength, mFramingRect.top - halfWidth, mBorderPaint);

        canvas.drawLine(mFramingRect.left - halfWidth, mFramingRect.bottom + width,
                mFramingRect.left - halfWidth, mFramingRect.bottom + width - mBorderLineLength, mBorderPaint);
        canvas.drawLine(mFramingRect.left - width, mFramingRect.bottom + halfWidth,
                mFramingRect.left - width + mBorderLineLength, mFramingRect.bottom + halfWidth, mBorderPaint);

        canvas.drawLine(mFramingRect.right + halfWidth, mFramingRect.top - width,
                mFramingRect.right + halfWidth, mFramingRect.top - width + mBorderLineLength, mBorderPaint);
        canvas.drawLine(mFramingRect.right + width, mFramingRect.top - halfWidth,
                mFramingRect.right + width - mBorderLineLength, mFramingRect.top - halfWidth, mBorderPaint);

        canvas.drawLine(mFramingRect.right + halfWidth, mFramingRect.bottom + width,
                mFramingRect.right + halfWidth, mFramingRect.bottom + width - mBorderLineLength, mBorderPaint);
        canvas.drawLine(mFramingRect.right + width, mFramingRect.bottom + halfWidth,
                mFramingRect.right + width - mBorderLineLength, mFramingRect.bottom + halfWidth, mBorderPaint);
    }

    public void drawLaser(Canvas canvas) {
        offset -= SCANNER_RATE;
        offset = offset <= 0 ? mFramingRect.bottom - mFramingRect.top : offset;

        canvas.drawBitmap(scannerView, null, new Rect(
                mFramingRect.left,
                mFramingRect.top,
                mFramingRect.right,
                mFramingRect.bottom - offset
        ), mLaserPaint);
    }

    @Override
    protected void onSizeChanged(int xNew, int yNew, int xOld, int yOld) {
        updateFramingRect();
    }

    public synchronized void updateFramingRect() {
        Point viewResolution = new Point(getWidth(), getHeight());
        int width;
        int height;
        int orientation = DisplayUtils.getScreenOrientation(getContext());

        if(orientation != Configuration.ORIENTATION_PORTRAIT) {
            width = findDesiredDimensionInRange(LANDSCAPE_WIDTH_RATIO, viewResolution.x, MIN_FRAME_WIDTH, LANDSCAPE_MAX_FRAME_WIDTH);
            height = findDesiredDimensionInRange(LANDSCAPE_HEIGHT_RATIO, viewResolution.y, MIN_FRAME_HEIGHT, LANDSCAPE_MAX_FRAME_HEIGHT);
        } else {
            width = findDesiredDimensionInRange(PORTRAIT_WIDTH_RATIO, viewResolution.x, MIN_FRAME_WIDTH, PORTRAIT_MAX_FRAME_WIDTH);
            height = findDesiredDimensionInRange(PORTRAIT_HEIGHT_RATIO, viewResolution.y, MIN_FRAME_HEIGHT, PORTRAIT_MAX_FRAME_HEIGHT);
        }

        int leftOffset = (viewResolution.x - width) / 2;
        int topOffset = (viewResolution.y - height) / 2;
        mFramingRect = new Rect(leftOffset, topOffset, leftOffset + width, topOffset + height);

        minOffset = topOffset;
        offset= height;
    }

    private static int findDesiredDimensionInRange(float ratio, int resolution, int hardMin, int hardMax) {
        int dim = (int) (ratio * resolution);
        if (dim < hardMin) {
            return hardMin;
        }
        if (dim > hardMax) {
            return hardMax;
        }
        return dim;
    }
}
