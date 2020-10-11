package mx.uv.fiee.iinf.mp3player;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicReference;

public class DetailsActivity extends Activity {

    public static final String Clave = "mx.uv.fiee.iinf.mp3player.DetailsActivity";
    ImageButton ImgButton;
    MediaPlayer player;
    Thread posThread;
    Uri mediaUri;
    int pos;
    SeekBar sbProgress;

    TextView txtAutor, txtArchivo;

    @Override
    protected void onCreate (@Nullable Bundle savedInstanceState) {
        super.onCreate (savedInstanceState);
        setContentView (R.layout.activity_details);

        sbProgress= findViewById (R.id.sbProgress);
        txtArchivo = findViewById(R.id.Archivo);
        txtAutor = findViewById(R.id.Autor);

        ImgButton = findViewById(R.id.imgButton);
        AtomicReference<Drawable> drw = new AtomicReference<>(getResources().getDrawable(R.drawable.ic_pause_black_48dp, getTheme()));

        ImgButton.setImageDrawable(drw.get());

        sbProgress.setOnSeekBarChangeListener (new MySeekBarChangeListener ());

        player = new MediaPlayer ();

        player.setOnPreparedListener (mediaPlayer -> {
            posThread = new Thread (() -> {
                try {
                    while (player.isPlaying ()) {
                        Thread.sleep (1000);
                        sbProgress.setProgress (player.getCurrentPosition ());
                    }
                } catch (InterruptedException in) { in.printStackTrace (); }
            });

            sbProgress.setMax (mediaPlayer.getDuration ());
            if (pos > -1) mediaPlayer.seekTo (pos);
            mediaPlayer.start ();
            posThread.start ();
        });

        ImgButton.setOnClickListener(v-> {

            if(player.isPlaying()){
                drw.set(getResources().getDrawable(R.drawable.ic_play_arrow_black_48dp, getTheme()));
                player.pause();
            }else{
                drw.set(getResources().getDrawable(R.drawable.ic_pause_black_48dp, getTheme()));
                player.start();
            }
            ImgButton.setImageDrawable(drw.get());
        });

    }

    @Override
    protected void onSaveInstanceState (@NonNull Bundle outState) {
        super.onSaveInstanceState (outState);

        outState.putString ("SONG", mediaUri != null ? mediaUri.toString (): "");
        outState.putInt ("PROGRESS", player != null ?  player.getCurrentPosition () : -1);
        outState.putBoolean ("ISPLAYING", player != null && player.isPlaying ());

        Toast.makeText(getApplicationContext(),"Hola",Toast.LENGTH_LONG).show();

        if (player.isPlaying ()) {
            posThread.interrupt ();

            player.stop ();
            player.seekTo (0);
            player.release ();
            player = null;
        }
    }

    @Override
    protected void onRestoreInstanceState (@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState (savedInstanceState);

        mediaUri = Uri.parse (savedInstanceState.getString ("SONG"));
        pos = savedInstanceState.getInt ("PROGRESS");
        boolean isPlaying = savedInstanceState.getBoolean ("ISPLAYING");

        if (player == null) return;

        try {
            player.reset ();
            player.setDataSource (getBaseContext (), mediaUri);
            if (isPlaying) player.prepareAsync ();
        } catch (IOException | IllegalStateException ioex) {
            ioex.printStackTrace ();
        }
    }

    @Override
    protected void onDestroy () {
        super.onDestroy();
        // cleanup

        if (player != null && player.isPlaying ()) {
            player.stop ();
            player.release ();
        }
        posThread.interrupt();
        player = null;
    }

    @Override
    protected void onResume() {

        super.onResume ();
        if (player.isPlaying ()) {
            posThread.interrupt ();
            player.stop ();
            player.seekTo (0);
            sbProgress.setProgress (0);
            pos = -1;
        }
        try {
            player.setDataSource(getBaseContext (), mediaUri);
            player.prepare ();
        } catch (IOException ex) { ex.printStackTrace (); }
    }

    @Override
    protected void onStart() {
        super.onStart ();

        Intent intent = getIntent ();
        if (intent != null) {
            String audio = intent.getStringExtra (Clave);
            mediaUri = Uri.parse(audio);

            String [] columns = { MediaStore.Audio.Media.DISPLAY_NAME,MediaStore.Audio.Artists.ARTIST};

            //Ubicamos el cursor en el audio de la direccion recuperada
            Cursor returnCursor = getContentResolver().query(mediaUri,columns,null,null,null);

            //Obtenemos el index de los datos deseados
            int nameIndex = returnCursor.getColumnIndexOrThrow (MediaStore.Audio.Media.DISPLAY_NAME);
            int ArtistIndex = returnCursor.getColumnIndexOrThrow (MediaStore.Audio.Artists.ARTIST);
            returnCursor.moveToFirst();

            //Obtenemos el nombre del archivo y el autor y lo asignamos a los TextView
            txtArchivo.setText(returnCursor.getString(nameIndex));
            txtAutor.setText(returnCursor.getString(ArtistIndex));

        }

    }

    //A la escucha de cambios en el SeekBar
    class MySeekBarChangeListener implements SeekBar.OnSeekBarChangeListener {

        @Override
        public void onProgressChanged (SeekBar seekBa, int i, boolean b) {
            if (b) {
                boolean bandera=false;
                if(player.isPlaying()) {
                    player.pause();
                    bandera=true;
                }
                player.seekTo (i);
                if(bandera)
                    player.start ();
            }
        }

        @Override
        public void onStartTrackingTouch (SeekBar seekBar) {}

        @Override
        public void onStopTrackingTouch (SeekBar seekBar) {}

    }
}
