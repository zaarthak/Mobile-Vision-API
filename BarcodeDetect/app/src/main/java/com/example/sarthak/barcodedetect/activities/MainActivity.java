package com.example.sarthak.barcodedetect.activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.SparseArray;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.sarthak.barcodedetect.R;
import com.example.sarthak.barcodedetect.utils.AppConstants;
import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;

import java.io.IOException;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    boolean isFABOpen = false;

    private ImageView barCodeImageView;
    FloatingActionButton fabAdd, fabCamera, fabGallery;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // initialise view components
        setUpView();

        //------------------------------------------------------------------
        // onClick listeners for Floating Buttons
        //------------------------------------------------------------------
        fabAdd.setOnClickListener(this);
        fabCamera.setOnClickListener(this);
        fabGallery.setOnClickListener(this);
    }

    @Override
    public void onBackPressed() {
        if(!isFABOpen){
            super.onBackPressed();
        }

        // call closeFABMenu() to hide floating action buttons, if visible.
        else{
            closeFABMenu();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // close fab menu
        closeFABMenu();

        if (resultCode == RESULT_OK) {

            if (requestCode == AppConstants.CAMERA_REQUEST) {

                Bitmap photo = (Bitmap) data.getExtras().get("data");
                detectBarCode(photo);

            } else if (requestCode == AppConstants.PICK_IMAGE && null != data) {

                try {

                    Bitmap photo = MediaStore.Images.Media.getBitmap(getContentResolver(), data.getData());
                    detectBarCode(photo);

                } catch (IOException e) {

                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public void onClick(View view) {

        switch (view.getId()) {

            case R.id.fabAdd :
                if (!isFABOpen) {
                    showFABMenu();
                } else {
                    closeFABMenu();
                }
                break;

            case R.id.fabCamera :
                // start camera intent
                Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(cameraIntent, AppConstants.CAMERA_REQUEST);
                break;

            case R.id.fabGallery :
                // start gallery intent
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Select Picture"), AppConstants.PICK_IMAGE);
                break;
        }
    }

    /**
     * Initialise view components
     */
    private void setUpView() {

        fabAdd = (FloatingActionButton) findViewById(R.id.fabAdd);
        fabCamera = (FloatingActionButton) findViewById(R.id.fabCamera);
        fabGallery = (FloatingActionButton) findViewById(R.id.fabGallery);

        barCodeImageView = (ImageView) findViewById(R.id.barcodeImageView);
    }

    //-------------------------------------------------------------------------
    // floating action button animations
    //-------------------------------------------------------------------------
    private void showFABMenu(){
        isFABOpen=true;
        fabCamera.animate().translationY(-getResources().getDimension(R.dimen.standard_55));
        fabGallery.animate().translationY(-getResources().getDimension(R.dimen.standard_105));
    }

    private void closeFABMenu(){
        isFABOpen=false;
        fabCamera.animate().translationY(0);
        fabGallery.animate().translationY(0);
    }

    /**
     * Returns the raw value of a bar code image.
     *
     * @param bitmap is the image loaded from camera or gallery
     */
    private void detectBarCode(Bitmap bitmap) {

        // initialise bar code detector
        BarcodeDetector detector = new BarcodeDetector.Builder(getApplicationContext())
                .setBarcodeFormats(Barcode.ALL_FORMATS)
                .build();

        // check if the required native library is currently available
        if(!detector.isOperational()){

            Toast.makeText(MainActivity.this, R.string.barcode_error_message, Toast.LENGTH_LONG).show();
        }

        // create Frame instance from the bitmap to supply to the detector
        // returns a sparseArray of barcodes
        Frame frame = new Frame.Builder().setBitmap(bitmap).build();
        // call detector synchronously with a frame to detect barcode
        SparseArray<Barcode> barcodes = detector.detect(frame);

        // check if the image is valid bar code
        // if image is not a valid bar code, size of sparse array is 0
        if (barcodes.size() == 0) {
            Toast.makeText(MainActivity.this, R.string.incorrect_image_message, Toast.LENGTH_LONG).show();
        } else {
            Barcode thisCode = barcodes.valueAt(0);
            Toast.makeText(MainActivity.this, thisCode.rawValue, Toast.LENGTH_LONG).show();
        }

        barCodeImageView.setImageBitmap(bitmap);
    }
}
