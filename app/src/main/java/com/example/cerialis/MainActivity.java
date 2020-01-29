package com.example.cerialis;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.net.Uri;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.google.ar.core.Anchor;
import com.google.ar.core.AugmentedImage;
import com.google.ar.core.AugmentedImageDatabase;
import com.google.ar.core.Config;
import com.google.ar.core.Frame;
import com.google.ar.core.Session;
import com.google.ar.core.TrackingState;
import com.google.ar.sceneform.AnchorNode;
import com.google.ar.sceneform.FrameTime;
import com.google.ar.sceneform.Scene;
import com.google.ar.sceneform.rendering.ModelRenderable;

import java.util.Collection;

public class MainActivity extends AppCompatActivity implements Scene.OnUpdateListener{

    private CustomArFragment arFragment;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        arFragment = (CustomArFragment) getSupportFragmentManager().findFragmentById(R.id.fragment);
        arFragment.getArSceneView().getScene().addOnUpdateListener(this);


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
