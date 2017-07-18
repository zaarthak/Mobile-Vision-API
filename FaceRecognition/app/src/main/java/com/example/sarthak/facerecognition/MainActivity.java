package com.example.sarthak.facerecognition;

import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.SparseArray;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.sarthak.facerecognition.utils.AppConstants;
import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.text.TextBlock;
import com.google.android.gms.vision.text.TextRecognizer;

import java.io.IOException;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    boolean isFABOpen = false;

    private ImageView textImageView;
    private TextView decodedTextView;

    private FloatingActionButton fabAdd, fabCamera, fabGallery;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // initialise view components
        setUpView();

        //------------------------------------------------------------------
        // onClick listeners for floating buttons
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
                detectTextFromImage(photo);

            } else if (requestCode == AppConstants.PICK_IMAGE && null != data) {

                try {

                    Bitmap photo = MediaStore.Images.Media.getBitmap(getContentResolver(), data.getData());
                    detectTextFromImage(photo);

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

        textImageView = (ImageView) findViewById(R.id.image_view);
        decodedTextView = (TextView) findViewById(R.id.text_value);
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
     * Detects and image for any text and displays in textView.
     *
     * @param bitmap is the image loaded from camera or gallery
     */
    private void detectTextFromImage(Bitmap bitmap) {

        // create the TextRecognizer
        TextRecognizer textRecognizer = new TextRecognizer.Builder(MainActivity.this).build();

        // check if the TextRecognizer is operational.
        if (!textRecognizer.isOperational()) {
            Toast.makeText(MainActivity.this, R.string.text_recogniser_error_message, Toast.LENGTH_LONG).show();

            // Check for low storage.  If there is low storage, the native library will not be
            // downloaded, so detection will not become operational.
            IntentFilter lowStorageFilter = new IntentFilter(Intent.ACTION_DEVICE_STORAGE_LOW);
            boolean hasLowStorage = registerReceiver(null, lowStorageFilter) != null;

            if (hasLowStorage) {
                Toast.makeText(this, R.string.low_storage_error, Toast.LENGTH_LONG).show();
            }
        }

        // create Frame instance from the bitmap to supply to the detector
        // returns a sparseArray of text data
        Frame frame = new Frame.Builder().setBitmap(bitmap).build();
        // call detector synchronously with a frame to detect text data
        SparseArray<TextBlock> items = textRecognizer.detect(frame);

        // create an empty string builder
        StringBuilder stringBuilder = new StringBuilder();

        // append detected data to string builder
        for (int i = 0 ; i < items.size() ; ++i) {

            TextBlock item = items.valueAt(i);
            stringBuilder.append(item.getValue());
            stringBuilder.append("\n");
        }

        textImageView.setImageBitmap(bitmap);
        decodedTextView.setText(stringBuilder.toString());
    }
}
