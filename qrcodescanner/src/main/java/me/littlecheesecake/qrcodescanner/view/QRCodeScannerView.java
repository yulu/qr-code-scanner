package me.littlecheesecake.qrcodescanner.view;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Rect;
import android.hardware.Camera;
import android.util.AttributeSet;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.DecodeHintType;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.PlanarYUVLuminanceSource;
import com.google.zxing.ReaderException;
import com.google.zxing.Result;
import com.google.zxing.common.HybridBinarizer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import me.littlecheesecake.qrcodescanner.core.AbstractScannerView;
import me.littlecheesecake.qrcodescanner.core.DisplayUtils;

/**
 * Created by yulu on 1/12/15.
 */
public class QRCodeScannerView extends AbstractScannerView {
    public interface ResultHandler {
        void handleResult(Result rawResult);
    }
    public void setResultHandler(ResultHandler resultHandler) {
        this.resultHandler = resultHandler;
    }

    private MultiFormatReader multiFormatReader;
    public static final List<BarcodeFormat> ALL_FORMATS = new ArrayList<>();
    private List<BarcodeFormat> formats;
    private ResultHandler resultHandler;

    static {
        ALL_FORMATS.add(BarcodeFormat.QR_CODE);
    }

    public QRCodeScannerView(Context context) {
        super(context);
        initMultiFormatReader();
    }

    public QRCodeScannerView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        initMultiFormatReader();
    }

    public QRCodeScannerView(Context context, AttributeSet attributeSet, int defStyleAttr) {
        super(context, attributeSet);
        initMultiFormatReader();
    }

    private void initMultiFormatReader() {
        Map<DecodeHintType, Object> hints = new EnumMap<>(DecodeHintType.class);
        hints.put(DecodeHintType.POSSIBLE_FORMATS, getFormats());
        multiFormatReader = new MultiFormatReader();
        multiFormatReader.setHints(hints);
    }

    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {
        Camera.Parameters parameters = camera.getParameters();
        Camera.Size size = parameters.getPreviewSize();
        int width = size.width;
        int height = size.height;

        if(DisplayUtils.getScreenOrientation(getContext()) == Configuration.ORIENTATION_PORTRAIT) {
            byte[] rotatedData = new byte[data.length];
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++)
                    rotatedData[x * height + height - y - 1] = data[x + y * width];
            }
            int tmp = width;
            width = height;
            height = tmp;
            data = rotatedData;
        }

        Result rawResult = null;
        PlanarYUVLuminanceSource source = buildLuminanceSource(data, width, height);

        if(source != null) {
            BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));
            try {
                rawResult = multiFormatReader.decodeWithState(bitmap);
            } catch (ReaderException re) {
                // continue
            } catch (NullPointerException npe) {
                // This is terrible
            } catch (ArrayIndexOutOfBoundsException aoe) {

            } finally {
                multiFormatReader.reset();
            }
        }

        if (rawResult != null) {
            stopCamera();
            if(resultHandler != null) {
                resultHandler.handleResult(rawResult);
            }
        } else {
            camera.setOneShotPreviewCallback(this);
        }
    }

    private Collection<BarcodeFormat> getFormats() {
        if(formats == null) {
            return ALL_FORMATS;
        }
        return formats;
    }

    private PlanarYUVLuminanceSource buildLuminanceSource(byte[] data, int width, int height) {
        Rect rect = getFramingRectInPreview(width, height);
        if (rect == null) {
            return null;
        }
        // Go ahead and assume it's YUV rather than die.
        PlanarYUVLuminanceSource source = null;

        try {
            source = new PlanarYUVLuminanceSource(data, width, height, rect.left, rect.top,
                    rect.width(), rect.height(), false);
        } catch(Exception e) {
        }

        return source;
    }
}
