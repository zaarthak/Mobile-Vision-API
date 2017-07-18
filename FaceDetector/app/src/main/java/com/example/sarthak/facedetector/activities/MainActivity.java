package com.example.sarthak.facedetector.activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.util.SparseArray;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.sarthak.facedetector.R;
import com.example.sarthak.facedetector.utils.AppConstants;
import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.face.Face;
import com.google.android.gms.vision.face.FaceDetector;

import java.io.IOException;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    boolean isFABOpen = false;

    private ImageView imageView;
    FloatingActionButton fabAdd, fabCamera, fabGallery;

    FaceDetector detector;

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

        if (resultCode == RESULT_OK) {

            if (requestCode == AppConstants.CAMERA_REQUEST) {

                Bitmap photo = (Bitmap) data.getExtras().get("data");
                detectFaces(photo);

            } else if (requestCode == AppConstants.PICK_IMAGE && null != data) {

                try {

                    Bitmap photo = MediaStore.Images.Media.getBitmap(getContentResolver(), data.getData());
                    detectFaces(photo);

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
                Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(cameraIntent, AppConstants.CAMERA_REQUEST);
                break;

            case R.id.fabGallery :
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

        imageView = (ImageView) findViewById(R.id.detectFacesImageView);
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
     *
     *
     * @param bitmap is the image loaded from camera or gallery
     */
    private void detectFaces(Bitmap bitmap) {

        // define 'Paint' frame for face
        Paint rectPaint = new Paint();
        rectPaint.setStrokeWidth(5);
        rectPaint.setColor(Color.RED);
        rectPaint.setStyle(Paint.Style.STROKE);

        // create a bitmap of the frame
        Bitmap frameBitmap = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.RGB_565);
        // create a canvas of the frame from bitmap
        Canvas canvas = new Canvas(frameBitmap);
        canvas.drawBitmap(bitmap, 0, 0, null);

        // initialise face detector
        detector = new FaceDetector.Builder(getApplicationContext())
                .setTrackingEnabled(false)
                .setLandmarkType(FaceDetector.ALL_LANDMARKS)
                .build();

        // check if the required native library is currently available
        if (!detector.isOperational()) {

            Toast.makeText(MainActivity.this, R.string.face_detection_error_message, Toast.LENGTH_LONG).show();
        }

        // create Frame instance from the bitmap to supply to the detector
        Frame frame = new Frame.Builder().setBitmap(bitmap).build();
        // call detector synchronously with a frame to detect faces
        // result returned includes a collection of Face instances
        SparseArray<Face> faces = detector.detect(frame);

        // iterate the Face instances over the collection of faces
        // draw the canvas frame for each face
        for (int i = 0 ; i < faces.size() ; i++) {

            Face face = faces.valueAt(i);
            // get start and end points for each frame
            float x1 = face.getPosition().x;
            float y1 = face.getPosition().y;
            float x2 = x1 + face.getWidth();
            float y2 = y1 + face.getHeight();

            // create a rectangle canvas for the four float points
            RectF rectF = new RectF(x1, y1, x2, y2);
            canvas.drawRoundRect(rectF, 2, 2, rectPaint);
        }

        // convert frameBitmap to drawable and set in imageView
        imageView.setImageDrawable(new BitmapDrawable(getResources(), frameBitmap));
        // release face detector
        detector.release();
    }
}
