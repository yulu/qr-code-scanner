Add the dependency

```
dependencies {
    implementation 'me.littlecheesecake:qrcodescanner:1.0.4'
}
```

Style Config

```
    <me.littlecheesecake.qrcodescanner.view.QRCodeScannerView
        xmlns:sc="http://schemas.android.com/apk/res-auto"
        android:id="@+id/scanner_view"
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        sc:sc_mask_color="#ffffff"
        sc:sc_border_color="#cccccc"
        sc:sc_border_stroke_width="5dp"
        sc:sc_border_line_length="50dp"/>
```