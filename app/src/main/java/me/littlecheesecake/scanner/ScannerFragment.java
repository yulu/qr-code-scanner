package me.littlecheesecake.scanner;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.zxing.Result;

import me.littlecheesecake.qrcodescanner.view.QRCodeScannerView;

/**
 * Created by visenze on 1/12/15.
 */
public class ScannerFragment extends Fragment implements QRCodeScannerView.ResultHandler {
    private QRCodeScannerView qrCodeScannerView;

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
}
