package com.example.farhan.iotdemoapp;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.media.ImageReader;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Calendar;
import java.util.Map;

/**
 * Doorbell activity that capture a picture from an Android Things
 * camera on a button press and post it to Firebase and Google Cloud
 * Vision API.
 */
public class MainActivity extends Activity{
    private static final String TAG = MainActivity.class.getSimpleName();

    private DoorbellCamera mCamera;

    /**
     * Driver for the doorbell button;
     */

    /**
     * A {@link Handler} for running Camera tasks in the background.
     */
    private Handler mCameraHandler;

    /**
     * An additional thread for running Camera tasks that shouldn't block the UI.
     */
    private HandlerThread mCameraThread;

    /**
     * A {@link Handler} for running Cloud tasks in the background.
     */
    private Handler mCloudHandler;

    /**
     * An additional thread for running Cloud tasks that shouldn't block the UI.
     */
    private HandlerThread mCloudThread;
    ImageView imageView;

    FirebaseStorage mStorage;
    FirebaseDatabase mDatabase;
    StorageReference storageRef;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        //GoogleApiAvailability.makeGooglePlayServicesAvailable();

        String refreshedToken = FirebaseInstanceId.getInstance().getToken();
        Log.d(TAG, "Refreshed token: " + refreshedToken);

        mStorage = FirebaseStorage.getInstance("gs://photocompapp-app.appspot.com");
        mDatabase = FirebaseDatabase.getInstance("https://photocompapp-app.firebaseio.com/");
        //storage = FirebaseStorage.getInstance();
        // Create a storage reference from our app
        // imagesRef now points to root of our storage


        imageView=findViewById(R.id.imageView);
        Log.d(TAG, "Doorbell Activity created.");

        // We need permission to access the camera
        if (checkSelfPermission(Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            // A problem occurred auto-granting the permission
            Log.e(TAG, "No permission");
            return;
        }

        //mDatabase = FirebaseDatabase.getInstance();
        //mStorage = FirebaseStorage.getInstance();

        // Creates new handlers and associated threads for camera and networking operations.
        mCameraThread = new HandlerThread("CameraBackground");
        mCameraThread.start();
        mCameraHandler = new Handler(mCameraThread.getLooper());

        mCloudThread = new HandlerThread("CloudThread");
        mCloudThread.start();
        mCloudHandler = new Handler(mCloudThread.getLooper());


        // Camera code is complicated, so we've shoved it all in this closet class for you.
        mCamera = DoorbellCamera.getInstance();
        mCamera.initializeCamera(this, mCameraHandler, mOnImageAvailableListener);


        if(getIntent().getBooleanExtra("take_picture",false))
        {
            mCamera.takePicture();
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        mCamera.shutDown();

        mCameraThread.quitSafely();
        mCloudThread.quitSafely();
    }


    /**
     * Listener for new camera images.
     */
    private ImageReader.OnImageAvailableListener mOnImageAvailableListener =
            new ImageReader.OnImageAvailableListener() {
                @Override
                public void onImageAvailable(ImageReader reader) {
                    Image image = reader.acquireLatestImage();
                    // get image bytes
                    ByteBuffer imageBuf = image.getPlanes()[0].getBuffer();
                    final byte[] imageBytes = new byte[imageBuf.remaining()];
                    imageBuf.get(imageBytes);
                    Bitmap bitmapImage = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length, null);
                    Log.d(TAG,"Picture Taken");
                    onPictureTaken(bitmapImage, imageBytes);
                    image.close();

                }
            };

    public void onPictureTaken(final Bitmap bitmap, final byte[] imageBytes)
    {

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                imageView.setImageBitmap(bitmap);
            }
        });

        uploadImage(bitmap,imageBytes);
    }
    public void uploadImage(final Bitmap bitmap, final byte[] imageBytes)
    {
        if (imageBytes != null) {
            final DatabaseReference log = mDatabase.getReference("logs").push();
            final StorageReference imageRef = mStorage.getReference().child(log.getKey());

            // upload image to storage
            UploadTask task = imageRef.putBytes(imageBytes);
            task.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    Uri downloadUrl = taskSnapshot.getDownloadUrl();
                    // mark image in the database
                    Log.i(TAG, "Image upload successful");
                    log.child("timestamp").setValue(ServerValue.TIMESTAMP);
                    log.child("image").setValue(downloadUrl.toString());
                    // process image annotations
                    annotateImage(log, imageBytes);
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    // clean up this entry
                    Log.w(TAG, "Unable to upload image to Firebase");
                    log.removeValue();
                }
            });
        }
    }

    /**
     * Process image contents with Cloud Vision.
     */
    private void annotateImage(final DatabaseReference ref, final byte[] imageBytes) {
        mCloudHandler.post(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "sending image to cloud vision");
                // annotate image by uploading to Cloud Vision API
                try {
                    Map<String, Float> annotations = CloudVisionUtils.annotateImage(imageBytes);
                    Log.d(TAG, "cloud vision annotations:" + annotations);
                    if (annotations != null) {
                        ref.child("annotations").setValue(annotations);
                    }
                } catch (IOException e) {
                    Log.e(TAG, "Cloud Vison API error: ", e);
                }
            }
        });
    }


    public void takePicture(View view) {
        mCamera.takePicture();
    }
}
