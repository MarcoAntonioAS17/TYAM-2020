package mx.uv.fiee.iinf.gallerydemo;

import android.Manifest;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;
import androidx.exifinterface.media.ExifInterface;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class SavingActivity extends Activity {
    public static final int REQUEST_CAMERA_OPEN = 4001;
    public static final int REQUEST_PERMISSION_CAMERA = 3001;
    public static final int REQUEST_IMAGE_CAMERA = 2001;
    ImageView iv;

    LocationManager locationManager;

    boolean gotLocation = false;

    double longitude = 0.0;
    double latitude = 0.0;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_saving);

        iv = findViewById(R.id.ivSource);


        Button button = findViewById(R.id.btnSave);
        button.setOnClickListener(v -> {

            int perm = checkSelfPermission(Manifest.permission.CAMERA);
            if (perm != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(
                        new String[]{Manifest.permission.CAMERA},
                        REQUEST_PERMISSION_CAMERA
                );

                return;
            }

            abrirCamara();


            //}

        });
    }

    void abrirCamara() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        startActivityForResult(intent, REQUEST_IMAGE_CAMERA);

    }

    /**
     * Obtiene un objeto de mapa de bits a partir del objeto Drawable (canvas) recibido.
     *
     * @param drble Drawable que contiene la imagen deseada.
     * @return objeto de mapa de bits con la estructura de la imagen.
     */
    private Bitmap getBitmapFromDrawable(Drawable drble) {
        // debido a la forma que el sistema dibuja una imagen en un el sistema gráfico
        // es necearios realzar comprobaciones para saber del tipo de objeto Drawable
        // con que se está trabajando.
        //
        // si el objeto recibido es del tipo BitmapDrawable no se requieren más conversiones
        if (drble instanceof BitmapDrawable) {
            return ((BitmapDrawable) drble).getBitmap();
        }

        // en caso contrario, se crea un nuevo objeto Bitmap a partir del contenido
        // del objeto Drawable
        Bitmap bitmap = Bitmap.createBitmap(drble.getIntrinsicWidth(), drble.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drble.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drble.draw(canvas);

        return bitmap;
    }

    /**
     * Almacena el mapa de bits recibido en el almacenamiento externo, dentro de la carpeta destinada
     * para contener archivos de imagen.
     *
     * @param bitmap imagen en mapa de bits a guardar.
     */
    void saveImage(Bitmap bitmap) {
        ContentResolver resolver = getContentResolver();

        ContentValues values = new ContentValues();
        values.put(MediaStore.MediaColumns.DISPLAY_NAME, "myOtherPic.jpg");

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            values.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES);
            values.put(MediaStore.MediaColumns.IS_PENDING, true);
        } else {
            String pictureDirectory =
                    String.format("%s/%s", Environment.getExternalStorageDirectory(), Environment.DIRECTORY_PICTURES);
            values.put(MediaStore.MediaColumns.DATA, pictureDirectory);
        }

        Uri targetUri;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            targetUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        } else {
            targetUri = MediaStore.Files.getContentUri("external");
        }

        Uri imageUri = resolver.insert(targetUri, values);

        try {
            OutputStream fos = resolver.openOutputStream(imageUri);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            fos.flush();
            fos.close();

            if (Build.VERSION.SDK_INT == Build.VERSION_CODES.Q) {
                values = new ContentValues();
                values.put(MediaStore.Images.ImageColumns.IS_PENDING, false);
                resolver.update(imageUri, values, null, null);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_PERMISSION_CAMERA) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                abrirCamara();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_IMAGE_CAMERA && resultCode == RESULT_OK) {
            Bitmap bitmap = (Bitmap) data.getExtras().get("data");
            iv.setImageBitmap(bitmap);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                saveImage(bitmap);
            } else {
                String imageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).toString();
                File file = new File(imageDir, System.currentTimeMillis() + ".jpg");

                try {
                    OutputStream fos = new FileOutputStream(file);
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
                    fos.flush();
                    fos.close();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }

                saveLocation();
            }
            Toast.makeText(getApplicationContext(), "Imagen guardada", Toast.LENGTH_SHORT).show();

        }
    }

    private void saveLocation() {
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        // Create a criteria object to retrieve provider
        Criteria criteria = new Criteria();

        // Get the name of the best provider
        String provider = locationManager.getBestProvider(criteria, true);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        locationManager.requestLocationUpdates(provider, 1000, 0.0f, mLocationListener); // (String) provider, time in milliseconds when to check for an update, distance to change in coordinates to request an update, LocationListener.
    }

    public boolean validLatLng (double lat, double lng) {
        if(lat != 0.0 && lng != 0.0){
            this.gotLocation = true;
            return true;
        } else return false;
    }

    public boolean haveLocation() {
        return this.gotLocation;
    }

    LocationListener mLocationListener = new LocationListener() {
        public void onLocationChanged (Location location){
            if (!haveLocation() && validLatLng(location.getLatitude(), location.getLongitude())) {
                //System.out.println("got new location");
                //Log.i("mLocationListener", "Got location");   // for logCat should ->  import android.util.Log;

                // Stops the new update requests.
                locationManager.removeUpdates(mLocationListener);
                double longitude = location.getLongitude();
                double latitude = location.getLatitude();
                File f = new File(Environment.getExternalStorageDirectory(), "/PhotoGPSApp/Attachment" + ".jpg");
                geoTag(f.getAbsolutePath(), latitude, longitude);

            }
        }

        public void onStatusChanged(java.lang.String s, int i, android.os.Bundle bundle) {
        }

        public void onProviderEnabled(java.lang.String s){
        }

        public void onProviderDisabled(java.lang.String s){
        }

    };

    public void geoTag(String filename, double latitude, double longitude){
        ExifInterface exif;

        try {
            exif = new ExifInterface(filename);
            int num1Lat = (int)Math.floor(latitude);
            int num2Lat = (int)Math.floor((latitude - num1Lat) * 60);
            double num3Lat = (latitude - ((double)num1Lat+((double)num2Lat/60))) * 3600000;

            int num1Lon = (int)Math.floor(longitude);
            int num2Lon = (int)Math.floor((longitude - num1Lon) * 60);
            double num3Lon = (longitude - ((double)num1Lon+((double)num2Lon/60))) * 3600000;

            exif.setAttribute(ExifInterface.TAG_GPS_LATITUDE, num1Lat+"/1,"+num2Lat+"/1,"+num3Lat+"/1000");
            exif.setAttribute(ExifInterface.TAG_GPS_LONGITUDE, num1Lon+"/1,"+num2Lon+"/1,"+num3Lon+"/1000");


            if (latitude > 0) {
                exif.setAttribute(ExifInterface.TAG_GPS_LATITUDE_REF, "N");
            } else {
                exif.setAttribute(ExifInterface.TAG_GPS_LATITUDE_REF, "S");
            }

            if (longitude > 0) {
                exif.setAttribute(ExifInterface.TAG_GPS_LONGITUDE_REF, "E");
            } else {
                exif.setAttribute(ExifInterface.TAG_GPS_LONGITUDE_REF, "W");
            }

            exif.saveAttributes();
            Log.e("PictureActivity", "Saved");
        } catch (IOException e) {
            Log.e("PictureActivity", e.getLocalizedMessage());
        }

    }
}
