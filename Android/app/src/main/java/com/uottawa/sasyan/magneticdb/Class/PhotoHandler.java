/**
 * Created by valentin on 28/05/2015.
 */
package com.uottawa.sasyan.magneticdb.Class;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Picture;
import android.hardware.Camera;
import android.media.ExifInterface;
import android.os.Environment;
import android.provider.MediaStore;
import android.widget.Toast;

import com.uottawa.sasyan.magneticdb.PictureActivity;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

public class PhotoHandler implements Camera.PictureCallback {
    private final Context context;
    private final PictureActivity activity;

    public PhotoHandler(PictureActivity activity){
        this.context = (Context)activity;
        this.activity = activity;
    }

    public void onPictureTaken(byte[] bytes, Camera camera) {
        Settings settings = new Settings(this.context);
        File pictureFileDir = Environment.getExternalStoragePublicDirectory(settings.getFolderPicture());

        if (!pictureFileDir.exists() && !pictureFileDir.mkdirs()) {
            Toast.makeText(context, "Can't create directory to save image.", Toast.LENGTH_LONG).show();
            return;
        }

        Bitmap bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        int width = bmp.getWidth();
        int height = bmp.getHeight();
        Matrix matrix = new Matrix();
        matrix.postRotate(this.activity.angleOrientation);
        Bitmap rotatedBitmap = Bitmap.createBitmap(bmp, 0, 0, width, height, matrix, true);

        SimpleDateFormat dateFormat = new SimpleDateFormat(settings.getDateFormat());
        String date = dateFormat.format(new Date());
        String photoFile = "Picture_" + date + ".jpg";

        String filename = pictureFileDir.getPath() + File.separator + photoFile;

        File pictureFile = new File(filename);

        try {
            FileOutputStream fos = new FileOutputStream(pictureFile);
            boolean result = rotatedBitmap.compress(Bitmap.CompressFormat.JPEG, 90, fos);
            fos.write(bytes);
            fos.close();
            if (result) {
                // Now we add exif information :
                ExifInterface exif = new ExifInterface(filename);
                exif.setAttribute(ExifInterface.TAG_GPS_LATITUDE, this.activity.gps.getLatTagGPS());
                exif.setAttribute(ExifInterface.TAG_GPS_LONGITUDE, this.activity.gps.getLonTagGPS());
                exif.setAttribute(ExifInterface.TAG_GPS_LATITUDE_REF, this.activity.gps.getLatRefTagGps());
                exif.setAttribute(ExifInterface.TAG_GPS_LONGITUDE_REF, this.activity.gps.getLonRefTagGps());
                exif.setAttribute(ExifInterface.TAG_FLASH, this.activity.camera.getParameters().getFlashMode());
                exif.saveAttributes();
                Toast.makeText(context, "New Image saved with the EXIF information: " + photoFile, Toast.LENGTH_LONG).show();
                activity.isFinish();
            }
            else {
                Toast.makeText(context, "Couldn't save image:" + photoFile, Toast.LENGTH_LONG).show();
                //camera.startPreview();
            }
        } catch (Exception error) {
            Toast.makeText(context, "Image could not be saved.", Toast.LENGTH_LONG).show();
        }
    }
}