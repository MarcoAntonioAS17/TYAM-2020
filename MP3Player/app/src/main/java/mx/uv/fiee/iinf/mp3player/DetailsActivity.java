package mx.uv.fiee.iinf.mp3player;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
<<<<<<< HEAD
import android.util.Log;
=======
import android.provider.MediaStore;
>>>>>>> 7f0ca7d... Actividad MP3 Player v1.1
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicReference;

public class DetailsActivity extends Activity {
<<<<<<< HEAD
    private static final String SONG_KEY = "song";
    private static final String PROGRESS_KEY = "progress";
    private static final String ISPLAYING_KEY = "isplaying";

=======

    public static final String Clave = "mx.uv.fiee.iinf.mp3player.DetailsActivity";
    ImageButton ImgButton;
>>>>>>> 7f0ca7d... Actividad MP3 Player v1.1
    MediaPlayer player;
    Thread posThread;
    Uri mediaUri;
    int pos;
<<<<<<< HEAD
    boolean updateProgressBar;
=======
    SeekBar sbProgress;

    TextView txtAutor, txtArchivo;
>>>>>>> 7f0ca7d... Actividad MP3 Player v1.1

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

<<<<<<< HEAD
        // hilo adicional para controlar la actualización de la barra de progreso
        posThread = new Thread (() -> {
            try {
                while (true) {
                    if (updateProgressBar) {
=======
        player = new MediaPlayer ();

        player.setOnPreparedListener (mediaPlayer -> {
            posThread = new Thread (() -> {
                try {
                    while (player.isPlaying ()) {
>>>>>>> 7f0ca7d... Actividad MP3 Player v1.1
                        Thread.sleep (1000);
                        sbProgress.setProgress (player.getCurrentPosition ());
                    }
                }
            } catch (InterruptedException in) { in.printStackTrace (); }
        });

        player = new MediaPlayer ();

        // establece el manejador del evento de reproducción finalizada
        player.setOnCompletionListener (mediaPlayer -> {
            // operaciones de limpieza cuando termina la reproducción
            updateProgressBar = false;
            //if (posThread != null) posThread.interrupt ();
            sbProgress.setProgress (0);
        });

        // manejador del evento media player preparado
        player.setOnPreparedListener (mediaPlayer -> {
            sbProgress.setMax (mediaPlayer.getDuration ());
            if (pos > -1) mediaPlayer.seekTo (pos);
            mediaPlayer.start ();
            posThread.start ();
            updateProgressBar = true;
        });

<<<<<<< HEAD
        // botón play
        ImageButton btnAudio1 = findViewById (R.id.btnAudio1);
        btnAudio1.setOnClickListener (view -> {
            // verifica si el player se encuentra en un estado de pausa
            if (!player.isLooping () && player.getCurrentPosition () > 1) {
                player.start ();
                player.seekTo (pos);
                updateProgressBar = true;
                return;
            }

            // si es la primera vez que se carga el archivo de audio
            // se invoca al método de preparación del media player
            try {
                player.setDataSource (getBaseContext (), mediaUri);
                player.prepare ();
            } catch (IOException ex) { ex.printStackTrace (); }

        });

        // botón pausar
        ImageButton btnAudio2 = findViewById (R.id.btnAudio2);
        btnAudio2.setOnClickListener (view -> {
            if (player.isPlaying ()) {
                pos = player.getCurrentPosition ();
                player.pause ();
                updateProgressBar = false;
            }
=======
        ImgButton.setOnClickListener(v-> {

            if(player.isPlaying()){
                drw.set(getResources().getDrawable(R.drawable.ic_play_arrow_black_48dp, getTheme()));
                player.pause();
            }else{
                drw.set(getResources().getDrawable(R.drawable.ic_pause_black_48dp, getTheme()));
                player.start();
            }
            ImgButton.setImageDrawable(drw.get());
>>>>>>> 7f0ca7d... Actividad MP3 Player v1.1
        });

    }

    @Override
    protected void onSaveInstanceState (@NonNull Bundle outState) {
        super.onSaveInstanceState (outState);

        outState.putString (SONG_KEY, mediaUri != null ? mediaUri.toString (): "");
        outState.putInt (PROGRESS_KEY, player != null ?  player.getCurrentPosition () : -1);
        outState.putBoolean (ISPLAYING_KEY, player != null && player.isPlaying ());

<<<<<<< HEAD
        posThread.interrupt ();
=======
        Toast.makeText(getApplicationContext(),"Hola",Toast.LENGTH_LONG).show();

        if (player.isPlaying ()) {
            posThread.interrupt ();
>>>>>>> 7f0ca7d... Actividad MP3 Player v1.1

        if (player.isPlaying ()) {
            player.stop ();
            player.seekTo (0);
        }

        player.release ();
        player = null;
    }

    @Override
    protected void onRestoreInstanceState (@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState (savedInstanceState);

        mediaUri = Uri.parse (savedInstanceState.getString (SONG_KEY));
        pos = savedInstanceState.getInt (PROGRESS_KEY);
        boolean isPlaying = savedInstanceState.getBoolean (ISPLAYING_KEY);
        updateProgressBar = isPlaying;

        if (player == null) return;

        try {
            player.reset ();

            if (isPlaying) {
                player.setDataSource (getBaseContext (), mediaUri);
                player.prepareAsync();
            }
        } catch (IOException | IllegalStateException ioex) {
            ioex.printStackTrace ();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (posThread.isAlive ()) posThread.interrupt ();
    }

    @Override
    protected void onDestroy () {
        super.onDestroy();
        // cleanup
        if (player != null) {
            if (player.isPlaying ()) {
                player.stop ();
            }

            player.release ();
        }
        posThread.interrupt();
        player = null;
    }

    @Override
<<<<<<< HEAD
    protected void onStart () {
=======
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
>>>>>>> 7f0ca7d... Actividad MP3 Player v1.1
        super.onStart ();

        Intent intent = getIntent ();
        if (intent != null) {
<<<<<<< HEAD
            String audio = intent.getStringExtra ("AUDIO");
            mediaUri = Uri.parse (audio);
=======
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

>>>>>>> 7f0ca7d... Actividad MP3 Player v1.1
        }

    }

<<<<<<< HEAD
    /**
     * Clase que implementa a la interfaz OnSeekBarChangeListener para responder
     * al evento de búsqueda en la barra de progreso
     */
    class MySeekBarChangeListener implements SeekBar.OnSeekBarChangeListener {

        @Override
        public void onProgressChanged (SeekBar seekBar, int i, boolean b) {
            if (b) { // si el evento fue disparado por el usuario, se reposiciona el audio
                player.pause ();
=======
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
>>>>>>> 7f0ca7d... Actividad MP3 Player v1.1
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
