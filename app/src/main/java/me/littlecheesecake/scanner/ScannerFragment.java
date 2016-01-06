package me.littlecheesecake.scanner;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.zxing.BinaryBitmap;
import com.google.zxing.PlanarYUVLuminanceSource;
import com.google.zxing.RGBLuminanceSource;
import com.google.zxing.ReaderException;
import com.google.zxing.Result;
import com.google.zxing.common.HybridBinarizer;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import me.littlecheesecake.qrcodescanner.core.DisplayUtils;
import me.littlecheesecake.qrcodescanner.view.QRCodeScannerView;

/**
 * Created by visenze on 1/12/15.
 */
public class ScannerFragment extends Fragment implements QRCodeScannerView.ResultHandler {
    private static final int RESULT_LOAD_IMAGE_FROM_GALLERY = 0x00;
    private QRCodeScannerView qrCodeScannerView;
    private LinearLayout albumButton;

    /**
     * Constructor
     *
     * @return new instance of CameraFragment
     */
    public static ScannerFragment newInstance() {
        //can set some properties for the fragment
        return new ScannerFragment();
    }

    @Override
    public View onCreateView(LayoutInflater layoutinflater, ViewGroup viewGroup, Bundle savedInstanceState) {
        View view = layoutinflater.inflate(R.layout.scanner_fragment_layout, viewGroup, false);
        qrCodeScannerView = (QRCodeScannerView) view.findViewById(R.id.scanner_view);
        albumButton = (LinearLayout) view.findViewById(R.id.scan_album);
        albumButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent openAlbumIntent = new Intent(Intent.ACTION_GET_CONTENT);
                openAlbumIntent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
                startActivityForResult(openAlbumIntent, RESULT_LOAD_IMAGE_FROM_GALLERY);
            }
        });

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        qrCodeScannerView.setResultHandler(this);
        qrCodeScannerView.startCamera();
    }

    @Override
    public void onPause() {
        super.onPause();
        qrCodeScannerView.stopCamera();
    }

    @Override
    public void handleResult(Result rawResult) {
        Toast.makeText(getActivity(), rawResult.getText(), Toast.LENGTH_SHORT).show();
        qrCodeScannerView.startCamera();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RESULT_LOAD_IMAGE_FROM_GALLERY && resultCode == Activity.RESULT_OK && null != data) {
            Uri uri = data.getData();

            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            Bitmap bitmap = null;
            try {
                bitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), uri);
                int width = bitmap.getWidth(), height = bitmap.getHeight();
                int[] pixels = new int[width * height];
                bitmap.getPixels(pixels, 0, width, 0, 0, width, height);
                bitmap.recycle();
                bitmap = null;

                Result result = readBitmapFromAlbum(pixels, width, height);
                if (result != null)
                    Toast.makeText(getActivity(), result.getText(), Toast.LENGTH_SHORT).show();
                else
                    Toast.makeText(getActivity(), "error", Toast.LENGTH_SHORT).show();

            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(getActivity(), "error", Toast.LENGTH_SHORT).show();
            }
        }

        qrCodeScannerView.startCamera();
    }


    public Result readBitmapFromAlbum(int[] data, int width, int height) {
        Result rawResult = null;
        // Go ahead and assume it's YUV rather than die.
        RGBLuminanceSource source = null;

        try {
            source = new RGBLuminanceSource(width, height, data);
        } catch(Exception e) {
        }

        if (source != null) {
            BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));
            try {
                rawResult = qrCodeScannerView.getMultiFormatReader().decodeWithState(bitmap);
            } catch (ReaderException re) {
                // continue
            } catch (NullPointerException npe) {
                // This is terrible
            } catch (ArrayIndexOutOfBoundsException aoe) {

            } finally {
                qrCodeScannerView.getMultiFormatReader().reset();
            }
        }

        return rawResult;
    }
}
