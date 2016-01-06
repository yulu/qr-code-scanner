package me.littlecheesecake.qrcodescanner.core;

import android.graphics.Rect;

/**
 * Created by visenze on 1/12/15.
 */
public interface ViewFinder {
    /**
     * Method that executes when Camera preview is starting
     * It is recommended to update framing rect here and invalidate view after that.
     * For example see; {@link ViewFinderView#setupViewFinder()}
     */
    void setupViewFinder();

    /**
     * Provides {@link Rect} that identifies area where barcode scanner can detect visual codes
     * <p>Note: this rect is an area representation in absolute pixel values.
     * For example:
     * If view's size is 1024 x 800 so framing rect might be 500x400</p>
     *
     * @return {@link Rect} that identifies barcode scanner area
     */
    Rect getFramingRect();

    /**
     * Width of a {@link android.view.View} that implements this interface
     * <p>Note: this is already implemented in {@link android.view.View},
     * so you don't need to override method and provide your implementation</p>
     *
     * @return width of a view
     */
    int getWidth();

    /**
     * Height of a {@link android.view.View} that implements this interface
     * <p>Note: this is already implemented in {@link android.view.View},
     * so you don't need to override method and provide your implementation</p>
     *
     * @return height of a view
     */
    int getHeight();
}
