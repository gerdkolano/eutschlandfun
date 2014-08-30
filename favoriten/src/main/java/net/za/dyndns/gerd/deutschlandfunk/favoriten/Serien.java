package net.za.dyndns.gerd.deutschlandfunk.favoriten;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

/**
 * Created by hanno on 2014-08-28 10:10.
 */
public class Serien {
  private TreeSet<Serie> serie;
  private TreeSet<Serie> geordneteMenge;
  private String filename;
  private Activity activity;
  private Context context;
  private int debug;
  private DownloadDlfunk downloadDlfunk;
  private MediaPlayer mediaPlayer;
  private int debugSchranke = 3;

  public Serien(Activity activity, Context context, int debug, MediaPlayer mediaPlayer) {
    this.activity = activity;
    this.context = context;
    this.debug = debug;
    this.mediaPlayer = mediaPlayer;
    this.filename = "serien.txt";
    serie = new TreeSet<Serie>();
    this.geordneteMenge = new TreeSet<Serie>();
    erzeugeSerien();
  }

  public int size() {
    return serie.size();
  }

  public TreeSet<Serie> getSerie() {
    return serie;
  }

  private String dateiname(String suchbegriff) {
    return suchbegriff.replaceAll("[^A-Za-z_0-9]", "_") + "-v2.txt";
  }

  public Serien erzeugeSerien() {
    //loescheDieSeriendatei();
    if (!ladeAusDerSeriendatei()) {
      this.serie.add(new Serie("Forschung aktuell", "searchterm=forschung+aktuell"));
      this.serie.add(new Serie("Computer und Kommunikation", "searchterm=computer+und+kommunikation"));
      this.serie.add(new Serie("Wissenschaft im Brennpunkt", "broadcast_id=155"));
      this.serie.add(new Serie("Wirtschaft und Gesellschaft einzelne", "broadcast_id=162"));
      this.serie.add(new Serie("Wirtschaft und Gesellschaft komplett", "searchterm=wirtschaft+und+gesellschaft+komplette"));
      this.serie.add(new Serie("Kultur heute", "searchterm=Kultur+Heute"));
      retteInDieSeriendatei();
      if (debug > 2) Log.i("SE10", "Lies \"serien\" aus dem Programmtext");
    }
    return this;
  }

  public boolean loescheDieSeriendatei() {
    if (debug > 2) Log.i("SE30", "lösche " + filename);
    boolean erg = context.deleteFile(filename);
    if (erg) {
      if (debug > 0) Log.i("SE31", filename + " gelöscht.");
    } else {
      if (debug > 0) Log.i("SE32", "kann " + filename + " nicht löschen.");
    }
    return erg;
  }

  private boolean ladeAusDerSeriendatei() {
    FileInputStream fstream;
    try {
      fstream = context.openFileInput(filename);
    } catch (FileNotFoundException e) {
      return false;
    } catch (Exception e) {
      e.printStackTrace();
      return false;
    }
    try {
      BufferedReader br = new BufferedReader(new InputStreamReader(fstream));
      if (debug > 2) Log.i("SE20", "Lies \"serien\" aus " + filename);
      String strLine;
      //Read File Line By Line
      int zeile = 0;
      Serie neueSerie = new Serie();
      while ((strLine = br.readLine()) != null) {
        // Print the content on the logcat
        if (debug > debugSchranke) Log.i("SE" + String.format("%2d", zeile), strLine);
        // Sammle die einzelnen Felder
        if (neueSerie.allesGesammelt(strLine, zeile)) {
          this.serie.add(neueSerie);
          neueSerie = new Serie();
        }
        zeile++;
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
    //Close the input stream
    try {
      fstream.close();
    } catch (Exception e) {
      e.printStackTrace();
    }
    return true;
  }

  public void retteInDieSeriendatei() {
    FileOutputStream outputStream;
    if (debug > 2) Log.i("SE40", "Erzeuge " + filename);
    int nummer = 0;
    try {
      outputStream = context.openFileOutput(filename, Context.MODE_PRIVATE);
      for (Serie was : this.serie) {
        if (debug > debugSchranke) Log.i(String.format("Sr%2d", nummer),
            was.getMenschenlesbarerName()
                + " "
                + was.getSuchbegriff()
        );
        outputStream.write(was.zuRetten().getBytes());
        nummer++;
      }
      outputStream.close();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public void append(Serie serie) {
    this.serie.add(serie);
    retteInDieSeriendatei();
  }

  public void add(Serie serie) {
    FileOutputStream outputStream;
    try {
      outputStream = context.openFileOutput(filename, Context.MODE_PRIVATE | Context.MODE_APPEND);
      outputStream.write(serie.zuRetten().getBytes());
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  int seitennummer = 1;

  public void stelleSerienauswahlbuttonsHer() {
    String KEINE = "keine Vorwahl";
    Serien serien = new Serien(activity, context, debug, mediaPlayer);
    if (debug > debugSchranke)
      Log.i("SR10", " Erstelle " + serien.size() + " Serienauswahlbuttons.");
    LinearLayout layout = (LinearLayout) activity.findViewById(R.id.welcheSerie);
    int nummer = 0;
    for (Serie serie : serien.getSerie()) {
      Button Taste = new Button(context);
      Taste.setLayoutParams(
          new ViewGroup.LayoutParams(
              ViewGroup.LayoutParams.MATCH_PARENT,
              ViewGroup.LayoutParams.WRAP_CONTENT
          )
      );
      final String menschenlesbarerName = serie.getMenschenlesbarerName();
      String suchwort = serie.getSuchbegriff();
      SerienClickHandler handler = new SerienClickHandler(menschenlesbarerName, suchwort);
      //handler.set(menschenlesbarerName, suchbegriff);
      if (debug > 1)
        Taste.setText("Serie-" + nummer + " " + menschenlesbarerName + " : " + suchwort);
      else
        Taste.setText(menschenlesbarerName);
      Taste.setId(160847 + nummer++);
      if (debug > debugSchranke) Log.i("SR20", " " + nummer
          + " \"" + menschenlesbarerName + "\" " + suchwort);
      Taste.setOnClickListener(handler);
      layout.addView(Taste);
    }
    if (debug > 2) Log.i("SR90", serien.size() + " Serienauswahlbuttons hergestellt.");
    SharedPreferences mySharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
    String gespeicherterBegriff = mySharedPrefs.getString("sendungsnamePreff", KEINE);
    if (!gespeicherterBegriff.equals(KEINE)) loadPage(gespeicherterBegriff, seitennummer);
    if (debug > 2) Log.i("SR99", "gespeicherterBegriff=" + gespeicherterBegriff);
  }

  class SerienClickHandler implements View.OnClickListener {
    String menschenlesbarerName, klickwort;

    SerienClickHandler(String menschenlesbarerName, String suchbegriff) {
      this.menschenlesbarerName = menschenlesbarerName;
      this.klickwort = suchbegriff;
    }

    public void set(String menschenlesbarerName, String suchbegriff) {
      this.menschenlesbarerName = menschenlesbarerName;
      this.klickwort = suchbegriff;
    }

    public void onClick(View v) {
      loadPage(klickwort, seitennummer);
    }
  }

  public void loadPage(String suchbegriff, int seitennummer) {
    /*
    if (((sPref.equals(ANY)) && (wifiConnected || mobileConnected))
        || ((sPref.equals(WIFI)) && (wifiConnected))) {
      */
    if (true /*|| this.wifiConnected || this.mobileConnected*/) {
      if (debug > debugSchranke) Log.i("SR20", "loadPage " + suchbegriff + " " + seitennummer);
      //
      //AsyncTask subclass
      //activity this
      //context WahlActivity.this
      //
      //DownloadXmlTask downloadXmlTask = (DownloadXmlTask)
      new DownloadXmlTask(
          activity, context, this.debug,
          downloadDlfunk, mediaPlayer
      ).execute(suchbegriff); // dort ruft doInBackground ladeXmlseiten(http...?drau:suchbegriff)
      if (debug > debugSchranke)
        Log.i("SR30", "Im Hintergrund downloadXmlTask.execute(" + suchbegriff + ") " + seitennummer);

      SharedPreferences mySharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);

      SharedPreferences.Editor editor = mySharedPrefs.edit();
      editor.putString("sendungsnamePreff", suchbegriff);
      if (editor.commit()) {
        if (debug > debugSchranke)
          Log.i("SR40", "sendungsnamePreff->" + suchbegriff + " commit'ed");
      } else {
        if (debug > debugSchranke)
          Log.i("SR50", "sendungsnamePreff->" + suchbegriff + " nicht commit'ed");
      }
    } else {
      showErrorPage();
    }
  }

  // Displays an error if the app is unable to load content.
  private void showErrorPage() {
    activity.setContentView(R.layout.activity_wahl);

    // The specified network connection is not available. Displays error message.
    //TextView myTextView = (TextView) this.findViewById(R.id.textView1);
    //myTextView.setText(this.getResources().getString(R.string.connection_error));
  }


}
