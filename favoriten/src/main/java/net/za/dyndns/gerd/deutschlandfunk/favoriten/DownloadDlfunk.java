package net.za.dyndns.gerd.deutschlandfunk.favoriten;

import android.app.Activity;
import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.Handler;
import android.os.PowerManager;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by hanno on 01.06.14.
 */

// Implementation of AsyncTask used to download XML feed from srv.deutschlandradio.de.
// <String, Integer, String>
// <String, für Deklaration doInBackground(String... urls) und
//          für Verwendung  doInBackground.execute(param1, param2, ...)
// Integer, für Progress
// String>  für onPostExecute(String result)
//
public class DownloadDlfunk extends AsyncTask<String, Integer, File>
    implements MediaPlayer.OnPreparedListener {
  private Activity activity;
  private Context context;
  private MediaPlayer mediaPlayer;
  private SeekBar seekBar;
  private TextView ladeOderSpielFortschritt;
  private final Handler handler;
  private Runnable wiederkehrend;
  private int debug;
  private String zieldateiname;
  private final int LADE = 2222;
  private final int SPIELE = 3333;

  // Konstruktor wird verwendet in MachSendungsButtons
  // als new DownloadDlfunk(
  //                      activity, context, debug, mediaPlayer)
  //                      .execute(quellurl, zieldateiname, duration);

  DownloadDlfunk(Activity activity, Context context, int debug, MediaPlayer mediaPlayer) {
    this.activity = activity;
    this.context = context;
    this.debug = debug;
    this.mediaPlayer = mediaPlayer;
    handler = new Handler();
    wiederkehrend = null;
    zieldateiname = "";
    ladeOderSpielFortschritt = (TextView) this.activity.findViewById(R.id.fortschritt);
  }

  public MediaPlayer getMediaPlayer() {
    return mediaPlayer;
  }

  public String toString() {
    return "Hanno " + super.toString();
  }

  public void stoppeWiedergabe() {
    if (debug>0) Log.i("F090", "Stoppe vielleicht mediaPlayer");
    if (mediaPlayer != null) {
      if (debug>0) Log.i("F091", "Stoppe mediaPlayer");
      if (mediaPlayer.isPlaying()) {
        mediaPlayer.stop();
        if (debug>0) Log.i("F092", "Stoppe alten MediaPlayer");
      }
      /*
      mediaPlayer.release();
      mediaPlayer = null;
      if (handler != null) handler.removeCallbacks(wiederkehrend);
      */
    }
  }

  // Zielverzeichnis sdcard/Music/deutschlandfunk
  @Override
  protected File doInBackground(String... urls) {
    // gerufen von  downloadDlfunk.execute() in MachSendungsButtons
    // urls[i] sind die Parameter von execute(quellurl, zieldateiname, duration)
    String quellurl = urls[0];
    String zieldateiname = urls[1];
    String duration = urls[2];
    // Dies ruft hole()
    // hole() ruft copyStream
    // copyStream kann für eine Ladefortschrittsanzeige die Bytes mitzählen
    this.zieldateiname = zieldateiname;
    File erg = hole("deutschlandfunk", quellurl, zieldateiname, duration);
    return erg;
    // Dies steht in downloadDlfunk.get() zur Verfügung.
    // get() waits if necessary for the computation to complete,
    // and then retrieves its result.
  }

  @Override
  protected void onCancelled(File file) {
    super.onCancelled(file);
    if (mediaPlayer != null) {
      if (debug>0) Log.i("F094", "Stoppe mediaPlayer");
      if (mediaPlayer.isPlaying())
        mediaPlayer.stop();
    }
  }

  @Override
  protected void onCancelled() {
    super.onCancelled();
    if (mediaPlayer != null) {
      if (debug>0) Log.i("F096", "Stoppe mediaPlayer");
      if (mediaPlayer.isPlaying())
        mediaPlayer.stop();
    }
  }

  private String hms(int msek) {
    int sek = msek / 1000;
    int min = sek / 60;
    sek %= 60;
    int std = min / 60;
    min %= 60;
    return String.format("%02d:%02d:%02d", std, min, sek);
  }

  @Override
  protected void onProgressUpdate(Integer... publishedProgress) {
    // publishProgress liefert das hier verwendete Array publishedProgress
    int art = publishedProgress[0];
    switch (art) {
      case LADE:
        ladeOderSpielFortschritt.setText(String.format("%02d%% mp3-Ladefortschritt %d von %d",
            publishedProgress[1], publishedProgress[2], publishedProgress[3]));
        break;
      case SPIELE:
        int msek = publishedProgress[1];
        int max = publishedProgress[2];
        int jetzt = mediaPlayer.getCurrentPosition();
        if (debug>9) Log.i("F 00", " CurPos=" + jetzt + " Update  =" + msek);
        double prozentsatz = 100.0 * jetzt / max;
        seekBar.setProgress(jetzt); // max wird in "onPrepared(" gesetzt.
        ladeOderSpielFortschritt.setText(String.format(
            "%04.1f%% schon %s noch %s von %s - %s mp3-Abspielfortschritt",
            prozentsatz,
            hms(msek),
            hms(max - msek),
            hms(max),
            zieldateiname
            ));
        break;
      default:
        break;
    }
  }

  /**
   * Called when MediaPlayer is ready
   */
  public void onPrepared(MediaPlayer abspieler) {
    final int debug = this.debug;
    final MediaPlayer fabspieler = abspieler;
    fabspieler.start();
    boolean isPaused = !fabspieler.isPlaying();
    seekBar = (SeekBar) this.activity.findViewById(R.id.SeekBar01);
    //
    // ladeOderSpielFortschritt = (TextView) this.activity.findViewById(R.id.fortschritt);
    if (debug>8) Log.i("F030", seekBar.toString());
    if (fabspieler != null) {
      final int dauer = fabspieler.getDuration();
      final int divisor = fabspieler.getDuration() / 100;

      seekBar.setMax(dauer);
      if (debug>0) Log.i("F040", "Dauer = " + dauer + "ms Current = " + fabspieler.getCurrentPosition() + "ms");
      //final Handler handler = new Handler();

      //final boolean variante1 = true;
      final boolean variante1 = false;
      final int deltaMillisekunden = 1000;
      if (!isPaused) {
        wiederkehrend = new Runnable() {
          @Override
          public void run() {
            int jetzt = fabspieler.getCurrentPosition();
            if (debug>9) Log.i("F 50", " CurPos=" + jetzt
                + " Progress=" + seekBar.getProgress()
                + " Max=" + seekBar.getMax());
            if (variante1) {
              seekBar.setProgress(jetzt);
            } else {
              publishProgress(SPIELE, jetzt, dauer); // für onProgressUpdate
            }
            if (fabspieler.isPlaying())
              handler.postDelayed(this, deltaMillisekunden); //  EVERY_SECOND);
          }
        };
        handler.post(wiederkehrend);
        if (debug>8) Log.i("F060", "Progress-Takt " + (deltaMillisekunden / 1000) + " Sekunden");
      }

      seekBar.setOnTouchListener(new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
          // This is event handler thumb moving event
          //seekChange(v);
          if (fabspieler.isPlaying()) {
            SeekBar seekBar = (SeekBar) v;
            if (debug>8) Log.i("F080", seekBar.toString());
            fabspieler.seekTo(seekBar.getProgress());
          }
          if (debug>8) Log.i("F070", event.toString());
          return false;
        }
      });
    }
  }

  @Override
  protected void onPostExecute(File result) {
    if (result == null) return;
    Toast.makeText(context, "f214 Versuche abzuspielen: " + result.toString(),
        Toast.LENGTH_SHORT).show();
    Uri myUri = Uri.fromFile(result);
    if (mediaPlayer == null) {
      mediaPlayer = new MediaPlayer(); // idle state
      if (debug>0) Log.i("f215", "Nimm neuen MediaPlayer");
    } else {
      if (debug>0) Log.i("f216", "Nimm alten MediaPlayer");
      boolean spielt = false;
      try {
        spielt = mediaPlayer.isPlaying();
        if (debug>2) Log.i("f217", "Spielt alter MediaPlayer?");
      } catch (Exception e) {
        if (debug>2) Log.i("f218", e.toString());
      }
      if (debug>0) Log.i("f219", spielt ? "spielt" : "spielt nicht");
      try {
        if (spielt) {
          mediaPlayer.stop();
          if (debug>0) Log.i("f220", "Stoppe alten MediaPlayer");
        }
      } catch (Exception e) {
        if (debug>0) Log.i("f221", e.toString());
      }
      try {
        mediaPlayer.reset();
        if (debug>0) Log.i("f222", "Reset alten MediaPlayer");
      } catch (Exception e) {
        if (debug>0) Log.i("f223", e.toString());
      }
      /*
      mediaPlayer.release();
      mediaPlayer = null;
      if (handler != null) handler.removeCallbacks(wiederkehrend);
      mediaPlayer = new MediaPlayer(); // idle state
      */
      if (debug>0) Log.i("f224", "MediaPlayer idle");
    }
    try {
      mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
    } catch (Exception e) {
      if (debug>0) Log.i("f226", e.toString());
    }
    /* keine Wirkung
    mediaPlayer.setWakeMode(context, PowerManager.PARTIAL_WAKE_LOCK);
    mediaPlayer.setScreenOnWhilePlaying(true);
    */
    try {
      mediaPlayer.setDataSource(context, myUri); // initialized state
    } catch (Exception e) {
      Log.i("f228", e.toString());
    }
    boolean asynchron = true; // false schaltet progress bar ab
    if (asynchron) {
      mediaPlayer.setOnPreparedListener(this);
      mediaPlayer.prepareAsync(); // prepare async to not block main thread
      if (debug>0) Log.i("f229", "asynchron abspielen " + myUri.toString());
    } else {
      try {
        mediaPlayer.prepare(); // prepared state, we can call start()
      } catch (IOException e) {
        e.printStackTrace();
      }
      if (debug>0) Log.i("f230", "synchron " + myUri.toString());
      mediaPlayer.start();
    }
  }

  private void copyStream(InputStream is, OutputStream os, String duration) {
    // gerufen von hole()
    final int buffer_size = 16*1024;
    int bytesSchon = 0;
    int bytesneu = 0;
    int iduration = 1;
    try {
      iduration = new Integer(duration);
    } catch (Exception e) {
      e.printStackTrace();
    }
    //iduration *= (16*1024);
    // (bytesSchon+=bytesneu)/(16*1024) * 100 / iduration
    int divisor = (16*1024) * iduration / 100;
    byte[] bytes = new byte[buffer_size];
    try {
      while ((bytesneu = is.read(bytes, 0, buffer_size)) > -1) {
        publishProgress(LADE, (bytesSchon+=bytesneu)/divisor, bytesSchon/(16*1024), iduration); // für onProgressUpdate
        os.write(bytes, 0, bytesneu);
      }
    } catch (Exception ex) {
      if (debug>0) Log.i("F099", "copyStream" + ex.toString());
    }
    if (debug>0) Log.i("F100", "bytesneu = " + bytesneu);
  }

  private File getAlbumStorageDir(String albumName) {
    // Get the name of the user's public MUSIC directory.
    File file = new File(Environment.getExternalStoragePublicDirectory(
        Environment.DIRECTORY_MUSIC), albumName);
    if (!file.exists()) {
      file.mkdir();
      if (debug>0) Log.i("F110", "Stelle Directory " + albumName + " her");
    }
    if (debug>0) Log.i("F120", "Finde Directory " + albumName + " vor");
    return file;
  }

  // Given a string representation of a URL, sets up a connection and
  // copies an input stream to a file.
  private File hole(String zielVerzeichnis,
                    String quellUrl, String zielDatei, String duration) {
    // gerufen von doInBackground
    File ausgabedatei = null;
    try {
      String storageState = Environment.getExternalStorageState();
      if (!storageState.equals(Environment.MEDIA_MOUNTED)) {
        if (debug>0) Log.i("F130", "hole " + quellUrl);
        return ausgabedatei;
        //return "Leider nicht Environment.MEDIA_MOUNTED";
      }
      // from web to local file
      // adb shell ls -al sdcard/Music/deutschlandfunk
      File ausgabeverzeichnis = getAlbumStorageDir(zielVerzeichnis);
      ausgabedatei = new File(ausgabeverzeichnis, zielDatei);
      String zielPfad = ausgabedatei.toString();
      if (ausgabedatei.exists()) {
        if (debug>0) Log.i("F140", "Datei " + zielPfad + " existiert bereits");
        return ausgabedatei;
        //return "Datei " + zielPfad + " existiert bereits";
      }
      URL imageUrl = new URL(quellUrl);
      HttpURLConnection conn = null;
      conn = (HttpURLConnection) imageUrl.openConnection();
      conn.setConnectTimeout(30000);
      conn.setReadTimeout(30000);
      conn.setRequestMethod("GET");
      conn.setDoInput(true);
      conn.setInstanceFollowRedirects(true);
      /*
      Starts the query
      */
      conn.connect();
      InputStream is = null;
      is = conn.getInputStream();
      OutputStream os = null;
      os = new FileOutputStream(ausgabedatei);

      // 2014-07-28
      if (debug>0) Log.i("F145", "Ladefortschritt. duration=" + duration);
      // ladeOderSpielFortschritt.setText(String.format("F145 Ladefortschritt"));
      // Only the original thread that created a view hierarchy can touch its views.

      //TextView ladeFortschritt;
      //ladeFortschritt = (TextView) this.activity.findViewById(R.id.fortschritt);
      //ladeFortschritt.setText(String.format("F145 Ladefortschritt"));
      // Only the original thread that created a view hierarchy can touch its views.

      copyStream(is, os, duration);
      os.close();
      if (debug>0) Log.i("F150", "Datei " + zielPfad + " erzeugt");
      return ausgabedatei;
      //return "Datei " + zielPfad + " erzeugt";
    } catch (IOException e) {
      e.printStackTrace();
      return ausgabedatei;
      //return "Datei " + zielDatei + " nicht erzeugt";
    }
  }
}
