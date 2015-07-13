package com.uottawa.sasyan.magneticdb;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.hardware.Camera;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.OrientationEventListener;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Toast;

import com.uottawa.sasyan.magneticdb.Class.GPS;
import com.uottawa.sasyan.magneticdb.Class.PhotoHandler;

import org.json.JSONObject;

import java.io.IOException;
import java.util.List;


public class PictureActivity extends Activity implements SurfaceHolder.Callback {
    private OrientationEventListener oL = null;
    private SurfaceView surfaceCamera;
    private boolean isPreview;
    private int cameraId = 0;

    public GPS gps = new GPS(0,0,0,0);
    public int angleOrientation = 0;
    public Camera camera;
    public Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_picture);
        context = (Context)this;

        // Recuperation of position:
        Intent intent = getIntent();
        try {
            JSONObject json = new JSONObject(intent.getExtras().getString("extraJSON"));
            JSONObject latLon = json.getJSONObject("latLon");
            this.gps.update(latLon.getDouble("lat"), latLon.getDouble("lon"), 0, 0);
        } catch (org.json.JSONException e) {
            Toast.makeText(this, String.format(getString(R.string.impossible_getLatlon), e.toString()), Toast.LENGTH_SHORT).show();
        }

        // Initialisation of photo tacker:
        surfaceCamera = (SurfaceView) findViewById(R.id.surfaceView);
        oL = new OrientationEventListener(this) {
            public void onOrientationChanged(int orientation) {
                setCameraDisplayOrientation(PictureActivity.this, cameraId, camera);
            }
        };

        // Methode d'initialisation de la camera
        InitializeCamera();

        surfaceCamera.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                // Auto focus then Picture
                if (camera != null) {
                    camera.autoFocus(myAutoFocusCallback);
                }
            }
        });
    }

    // Retour sur l'application
    @Override
    public void onResume() {
        super.onResume();
        camera = Camera.open();
    }

    // Mise en pause de l'application
    @Override
    public void onPause() {
        super.onPause();

        if (camera != null) {
            camera.release();
            camera = null;
        }
    }

    public void isFinish() {
        this.finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_picture, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.menu_picture) {
            // Auto focus then Picture:
            camera.autoFocus(myAutoFocusCallback);
            return true;
        }
        if (id == R.id.menu_back) {
            this.finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /*** LA CAMERA ***/

    public void setCameraDisplayOrientation(Activity activity, int cameraId, android.hardware.Camera camera) {
        android.hardware.Camera.CameraInfo info = new android.hardware.Camera.CameraInfo();
        android.hardware.Camera.getCameraInfo(cameraId, info);
        int rotation = activity.getWindowManager().getDefaultDisplay().getRotation();
        int degrees = 0;
        switch (rotation) {
            case Surface.ROTATION_0:
                degrees = 0;
                break;
            case Surface.ROTATION_90:
                degrees = 90;
                break;
            case Surface.ROTATION_180:
                degrees = 180;
                break;
            case Surface.ROTATION_270:
                degrees = 270;
                break;
        }

        int result;
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            result = (info.orientation + degrees) % 360;
            result = (360 - result) % 360; // compensate the mirror
        } else { // back-facing
            result = (info.orientation - degrees + 360) % 360;
        }
        if (camera != null) {camera.setDisplayOrientation(result);}
        this.angleOrientation = result;
    }

    public void InitializeCamera() {
        surfaceCamera.getHolder().addCallback(this);
        surfaceCamera.getHolder().setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    }

    public void savePicture() {
        try {
            PhotoHandler ph = new PhotoHandler(this);
            camera.takePicture(null, null, ph);
        } catch (Exception e) {
            Toast.makeText(this, "Impossible de sauvegarder la photo : " + e.toString(), Toast.LENGTH_SHORT).show();
        }
    }

    Camera.AutoFocusCallback myAutoFocusCallback = new Camera.AutoFocusCallback(){
        @Override
        public void onAutoFocus(boolean arg0, Camera arg1) {
            // Auto focus done, we can take the picture:
            savePicture();
        }
    };

    /** LA SURFACE **/

    public void surfaceCreated(SurfaceHolder holder) {
        oL.enable();
        if (camera == null) {
            camera = Camera.open();
        }
    }

    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

        // Si le mode preview est lance alors nous le stoppons
        if (isPreview) {
            camera.stopPreview();
        }
        // Nous recuperons les parametres de la camera
        Camera.Parameters parameters = camera.getParameters();

        // Nous changeons la taille
        parameters.setPreviewSize(parameters.getPreviewSize().width, parameters.getPreviewSize().height);
        parameters.setGpsLatitude(this.gps.getLat());
        parameters.setGpsLongitude(this.gps.getLon());
        parameters.setGpsAltitude(this.gps.getAlt());

        // Get the best size for previsualisation:
        Camera.Size size;
        size = getBestSize(parameters.getSupportedPreviewSizes());
        parameters.setPreviewSize(size.width, size.height);
        // Get the best picture size for the files :
        size = getBestSize(parameters.getSupportedPictureSizes());
        parameters.setPictureSize(size.width, size.height);

        // Nous appliquons nos nouveaux parametres
        camera.setParameters(parameters);

        try { // Nous attachons notre previsualisation de la camera au holder de la surface
            camera.setPreviewDisplay(surfaceCamera.getHolder());
        } catch (IOException e) {
            // TODO: handle exception
        }

        // Nous lancons la preview
        camera.startPreview();

        isPreview = true;
    }

    public Camera.Size getBestSize(List<Camera.Size> sizes) {
        Camera.Size bestSize = sizes.get(0);
        for(int i = 1; i < sizes.size(); i++){
            if((sizes.get(i).width * sizes.get(i).height) > (bestSize.width * bestSize.height)){
                bestSize = sizes.get(i);
            }
        }
        return bestSize;
    }

    public void surfaceDestroyed(SurfaceHolder holder) {
        // Nous arretons la camera et nous rendons la main
        if (camera != null) {
            camera.stopPreview();
            isPreview = false;
            camera.release();
        }
        oL.disable();
    }
}




