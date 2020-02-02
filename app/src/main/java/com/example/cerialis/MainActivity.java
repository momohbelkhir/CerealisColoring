package com.example.cerialis;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import android.media.CamcorderProfile;
import android.net.Uri;

import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.PixelCopy;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.ar.core.Anchor;
import com.google.ar.core.AugmentedImage;
import com.google.ar.core.AugmentedImageDatabase;
import com.google.ar.core.Config;
import com.google.ar.core.Frame;
import com.google.ar.core.Session;
import com.google.ar.core.TrackingState;
import com.google.ar.sceneform.AnchorNode;
import com.google.ar.sceneform.ArSceneView;
import com.google.ar.sceneform.FrameTime;
import com.google.ar.sceneform.Scene;
import com.google.ar.sceneform.rendering.ModelRenderable;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;

public class MainActivity extends AppCompatActivity implements Scene.OnUpdateListener{

    private CustomArFragment arFragment;
    Button capture;
    Button record;
    VideoRecorder videoRecorder =null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);



        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_DENIED){
            ActivityCompat.requestPermissions(this,
                    new String[]{ Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        }

        arFragment = (CustomArFragment) getSupportFragmentManager().findFragmentById(R.id.fragment);
        arFragment.getArSceneView().getScene().addOnUpdateListener(this);

        capture = (Button) findViewById(R.id.capture);
        record = (Button) findViewById(R.id.record);

        capture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                 takePhoto();

            }
        });

        record.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // takePhoto();
                if(videoRecorder == null){
                    videoRecorder = new VideoRecorder();
                    videoRecorder.setSceneView(arFragment.getArSceneView());
                    int orientation = getResources().getConfiguration().orientation;
                    videoRecorder.setVideoQuality(CamcorderProfile.QUALITY_HIGH,orientation);

                }
                boolean isRecording = videoRecorder.onToggleRecord();
                if(isRecording){
                    toastShow("Start");
                }else{
                    toastShow("Finished");
                }
            }
        });

    }

    private void toastShow(String state){

        if(state.equals("Start")){
             Toast.makeText(this,"Recording is started !", Toast.LENGTH_LONG).show();
             record.setText("Stop");
        } else if (state.equals("Finished")) {
            Toast.makeText(this,"Recording is finished ! the video is saved on Pictures folder", Toast.LENGTH_LONG).show();
            record.setText("Record");
        }
    }

    private String generateFilename() {
        String date =
                new SimpleDateFormat("yyyyMMddHHmmss", java.util.Locale.getDefault()).format(new Date());
        return Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES) + File.separator  + date + "_screenshot.jpg";
    }

    private void saveBitmapToDisk(Bitmap bitmap, String filename) throws IOException {


        try  {
            FileOutputStream outputStream = new FileOutputStream(filename);
            ByteArrayOutputStream outputData = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputData);
            outputData.writeTo(outputStream);
            outputStream.flush();
            outputStream.close();

        } catch (IOException ex) {
            Toast.makeText(this, "Save filed : "+ ex, Toast.LENGTH_SHORT).show();
        }
    }

    private void takePhoto() {
        final String filename = generateFilename();
        ArSceneView view = arFragment.getArSceneView();

        // Create a bitmap the size of the scene view.
        final Bitmap bitmap = Bitmap.createBitmap(view.getWidth(), view.getHeight(),
                Bitmap.Config.ARGB_8888);

        // Create a handler thread to offload the processing of the image.
        final HandlerThread handlerThread = new HandlerThread("PixelCopier");
        handlerThread.start();
        // Make the request to copy.
        PixelCopy.request(view, bitmap, (copyResult) -> {
            if (copyResult == PixelCopy.SUCCESS) {
                try {
                    saveBitmapToDisk(bitmap, filename);
                } catch (IOException e) {
                    Toast toast = Toast.makeText(MainActivity.this, e.toString(),
                            Toast.LENGTH_LONG);
                    toast.show();
                    return;
                }
                Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content),
                        "Photo saved in pictures", Snackbar.LENGTH_LONG);
                snackbar.setAction("Open to share", v -> {
                    File photoFile = new File(filename);

                    Uri photoURI = FileProvider.getUriForFile(MainActivity.this,
                            MainActivity.this.getPackageName() + ".ar.codelab.name.provider",
                            photoFile);
                    Intent intent = new Intent(Intent.ACTION_VIEW, photoURI);
                    intent.setDataAndType(photoURI, "image/*");
                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    startActivity(intent);

                });
                snackbar.show();
            } else {
                Toast toast = Toast.makeText(MainActivity.this,
                        "Failed to copyPixels: " + copyResult, Toast.LENGTH_LONG);
                toast.show();
            }
            handlerThread.quitSafely();
        }, new Handler(handlerThread.getLooper()));
    }



    public void stupDatabase (Config config, Session session){

        Bitmap popoBitmap = BitmapFactory.decodeResource(getResources(),R.drawable.popotamus);
        AugmentedImageDatabase aid = new AugmentedImageDatabase(session);
        aid.addImage("popotamus",popoBitmap);
        config.setAugmentedImageDatabase(aid);

        Bitmap popoBitmap2 = BitmapFactory.decodeResource(getResources(),R.drawable.monkey);
        aid.addImage("monkey",popoBitmap2);
        config.setAugmentedImageDatabase(aid);

        Bitmap popoBitmap3 = BitmapFactory.decodeResource(getResources(),R.drawable.snake);
        aid.addImage("snake",popoBitmap3);
        config.setAugmentedImageDatabase(aid);
    }




    @Override
    public void onUpdate(FrameTime frameTime) {

        Frame frame = arFragment.getArSceneView().getArFrame();
        Collection<AugmentedImage>  images = frame.getUpdatedTrackables(AugmentedImage.class);

        for(AugmentedImage image : images){
            if(image.getTrackingState() == TrackingState.TRACKING){
                if(image.getName().equals("popotamus")){


                    Anchor anchor = image.createAnchor(image.getCenterPose());
                    createModel(anchor , "Mesh_Rhinoceros.sfb");

                }else if(image.getName().equals("monkey")){

                    Anchor anchor = image.createAnchor(image.getCenterPose());
                    createModel(anchor , "Monkey.sfb");

                }else if(image.getName().equals("snake")){

                    Anchor anchor = image.createAnchor(image.getCenterPose());
                    createModel(anchor , "model.sfb");
                }
            }
        }
    }

    private void createModel(Anchor anchor, String imageUri) {

        ModelRenderable.builder()
                .setSource(this, Uri.parse(imageUri))
                .build()
                .thenAccept(modelRenderable -> placeModel(modelRenderable,anchor) );

    }

    private void placeModel(ModelRenderable modelRenderable, Anchor anchor) {

        AnchorNode anchorNode = new AnchorNode(anchor);
        anchorNode.setRenderable(modelRenderable);
        arFragment.getArSceneView().getScene().addChild(anchorNode);
    }
}
