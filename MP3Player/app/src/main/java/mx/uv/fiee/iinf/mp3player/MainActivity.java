package mx.uv.fiee.iinf.mp3player;

import android.app.Activity;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.PersistableBundle;
import android.util.Log;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import java.io.IOException;

public class MainActivity extends Activity {
    MediaPlayer player;
    SeekBar sbProgress;
    int selected_music;

    @Override
    protected void onCreate (@Nullable Bundle savedInstanceState) {
        super.onCreate (savedInstanceState);
        setContentView (R.layout.activity_main);

        Log.d("TYAM","OnCreate");

        sbProgress = findViewById (R.id.sbProgress);

        player = new MediaPlayer ();
        player.setOnPreparedListener (mediaPlayer -> {
            sbProgress.setMax (mediaPlayer.getDuration ());
            mediaPlayer.start ();
        });

        Button btnAudio1 = findViewById (R.id.btnAudio1);
        btnAudio1.setOnClickListener (v -> {
            Log.d("TYAM","Music 1");
            if (player.isPlaying ()) {
                player.stop();
                player.seekTo(0);
                sbProgress.setProgress(0);
            }
            selected_music=1;
            Uri mediaUri = Uri.parse ("android.resource://" + getBaseContext ().getPackageName () + "/" + R.raw.mr_blue_sky);

            try {

                player.setDataSource(getBaseContext(), mediaUri);
                player.prepare ();
                Toast.makeText (getApplicationContext (), "Now playing: Mr. Blue Sky", Toast.LENGTH_LONG).show ();
            } catch (IOException ex) { ex.printStackTrace (); }

        });

        Button btnAudio2 = findViewById (R.id.btnAudio2);
        btnAudio2.setOnClickListener (v -> {

            if (player.isPlaying ()) {
                player.stop();
                player.seekTo(0);
                sbProgress.setProgress(0);
            }
            selected_music=2;
            Uri mediaUri = Uri.parse ("android.resource://" + getBaseContext ().getPackageName () + "/" + R.raw.lake_shore_drive);

            try {
                player.setDataSource(getBaseContext(), mediaUri);
                player.prepare ();
                Toast.makeText (getApplicationContext (), "Now playing: Lake Shoe Drive", Toast.LENGTH_LONG).show ();
            } catch (IOException ex) { ex.printStackTrace (); }

        });

        Button btnAudio3 = findViewById (R.id.btnAudio3);
        btnAudio3.setOnClickListener (v -> {

            if (player.isPlaying ()) {
                player.stop ();
                player.seekTo (0);
                sbProgress.setProgress (0);
            }
            selected_music=3;
            Uri mediaUri = Uri.parse ("android.resource://" + getBaseContext ().getPackageName () + "/" + R.raw.fox_on_the_run);

            try {
                player.setDataSource(getBaseContext(), mediaUri);
                player.prepare();
                Toast.makeText (getApplicationContext (), "Now playing: Fox On The Run", Toast.LENGTH_LONG).show ();
            } catch (IOException ex) { ex.printStackTrace (); }

        });

        sbProgress.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if(fromUser){
                    Log.d("TYAM","OnProgressChanged");
                    if(player.isPlaying()){
                        player.pause();
                    }
                    player.seekTo(progress);
                    seekBar.setProgress(progress);
                    player.start();
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        Handler mHandler = new Handler();
        //Make sure you update Seekbar on UI thread
        MainActivity.this.runOnUiThread(new Runnable() {

            @Override
            public void run() {
                if(player != null){
                    int mCurrentPosition = player.getCurrentPosition();
                    sbProgress.setProgress(mCurrentPosition);
                }
                mHandler.postDelayed(this, 1000);
            }
        });

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // cleanup
        super.onStop();
        if (player.isPlaying ()) {
            player.stop ();
            player.release ();
        }

        player = null;
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d("TYAM","OnStart");

        SharedPreferences pref = getSharedPreferences ("MP3_SharedPreferences", 0);

        int sb_progress = pref.getInt("SB_PROGRESS",-1);
        int mp_progress = pref.getInt("MP_PROGRESS",-1);
        int music = pref.getInt("SElECTED_MUSIC",-1);

        if(mp_progress!=-1 && sb_progress!=-1 && music!=-1){
            
            Uri mediaUri = null;
            Toast.makeText (getApplicationContext (), "Now playing "+music, Toast.LENGTH_LONG).show ();
            switch (music){
                case 1:
                    mediaUri= Uri.parse ("android.resource://" + getBaseContext ().getPackageName () + "/" + R.raw.mr_blue_sky);
                    break;
                case 2:
                    mediaUri = Uri.parse ("android.resource://" + getBaseContext ().getPackageName () + "/" + R.raw.lake_shore_drive);
                    break;
                case 3:
                    mediaUri = Uri.parse ("android.resource://" + getBaseContext ().getPackageName () + "/" + R.raw.fox_on_the_run);
                    break;
                default:
                    return;
            }
            sbProgress.setProgress(sb_progress);

            try {
                player.setDataSource(getBaseContext(), mediaUri);
                player.prepare ();
                player.seekTo(mp_progress);
                player.start();

                Toast.makeText (getApplicationContext (), "Now playing: Mr. Blue Sky", Toast.LENGTH_LONG).show ();
            } catch (IOException ex) { ex.printStackTrace (); }

        }


    }
    @Override
    protected void onResume() {
        super.onResume();
        Log.d("TYAM", "OnResume");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d("TYAM", "OnPause");
    }

    @Override
    protected void onRestart(){
        super.onRestart();
        Log.d("TYAM", "OnRestart");
    }

    protected void onStop() {
        super.onStop();
        Log.d ("TYAM", "OnStop");

        SharedPreferences.Editor editor = getSharedPreferences ("MP3_SharedPreferences", MODE_PRIVATE).edit ();

        int sb_progress = sbProgress.getProgress();
        int mp_progress = player.getCurrentPosition();

        editor.putInt("SB_PROGRESS",sb_progress);
        editor.putInt("MP_PROGRESS",mp_progress);
        editor.putInt("SElECTED_MUSIC",selected_music);


        editor.apply ();
    }

}
