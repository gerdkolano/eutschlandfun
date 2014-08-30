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
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.util.Iterator;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * Created by hanno on 19.08.14.
 */
public class Menge {

  private SortedSet<Entry> geordneteMenge;
  private String filename;
  private Activity activity;
  private Context context;
  private int debug;
  private DownloadDlfunk downloadDlfunk;
  private MediaPlayer mediaPlayer;
  public int seitenanzahl;

  public Menge() {
    this.geordneteMenge = new TreeSet<Entry>();
    this.seitenanzahl = 1;
  }

  private String dateiname(String suchbegriff) {
    return suchbegriff.replaceAll("[^A-Za-z_0-9]", "_") + "-v2.txt";
  }

  public String getFilename() {
    return this.filename;
  }

  public Menge(Activity activity, Context context, int  debug, MediaPlayer mediaPlayer, String suchbegriff) {
    this.activity = activity;
    this.context = context;
    this.mediaPlayer = mediaPlayer;
    this.filename = dateiname(suchbegriff);
    this.geordneteMenge = new TreeSet<Entry>();
    this.debug = debug;
    this.seitenanzahl = 1;

    ladeAusDatei();
  }

  public SortedSet get() {
    return geordneteMenge;
  }

  public int size() {
    return geordneteMenge.size();
  }

  public Iterator iterator() {
    return geordneteMenge.iterator();
  }

  public boolean isEmpty() {
    return geordneteMenge.isEmpty();
  }

  public Entry erst() {
    return geordneteMenge.first();
  }

  public void add(Entry entry) {
    geordneteMenge.add(entry);
  }

  public void addAll(Menge menge) {
    geordneteMenge.addAll(menge.get());
  }

  private void ladeAusDatei() {
    FileInputStream fstream;
    if(debug>0) Log.i("M010", "Lies aus " + filename);
    try {
      fstream = context.openFileInput(filename);
    } catch (FileNotFoundException e) {
      return;
    } catch (Exception e) {
      e.printStackTrace();
      return;
    }
    try {
      //DataInputStream in = new DataInputStream(fstream);
      BufferedReader br = new BufferedReader(new InputStreamReader(fstream));
      String strLine;
      //Read File Line By Line
      int zeile = 0;
      Entry entry = new Entry(debug);
      while ((strLine = br.readLine()) != null) {
        // Print the content on the logcat
        if (debug>8) Log.i("L" + String.format("%3d", zeile), strLine);
        // Sammle die einzelnen Felder
        if (entry.allesGesammelt(strLine, zeile)) {
          this.add(entry);
          seitenanzahl = entry.seitenanzahl;
          entry = new Entry(debug);
        }
        zeile++;
/*
        entry.ladeNach(strLine, zeile);
        if (zeile % Entry.siebenZeilen == 0) {
          this.add(entry);
          seitenanzahl = entry.seitenanzahl;
          entry = new Entry(debug);
        }
*/
      }
      //Close the input stream
      fstream.close();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  void zeigeDateiAbbild() {
    FileInputStream fstream;
    if(debug>2) Log.i("M020", filename + " in " + context.getFilesDir());
    try {
      fstream = context.openFileInput(filename);
      //DataInputStream in = new DataInputStream(fstream);
      BufferedReader br = new BufferedReader(new InputStreamReader(fstream));
      String strLine;
      int nummer = 0;
      //Read File Line By Line
      while ((strLine = br.readLine()) != null) {
        // Print the content on the console
        if (debug>8) Log.i("z" + String.format("%3d", nummer), strLine);
        nummer++;
        //System.out.println(strLine);
      }
      //Close the input stream
      fstream.close();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  boolean loescheDateiAbbild() {
    if(debug>2) Log.i("M030", "lösche " + filename);
    boolean erg = context.deleteFile(filename);
    if (erg) {
      if(debug>0) Log.i("M031", filename + " gelöscht.");
    } else {
      if(debug>0) Log.i("M032", "kann " + filename + " nicht löschen.");
    }
    return erg;
  }

  void logge() {
    int nummer = 0;
    Iterator it = this.iterator();
    while (it.hasNext()) {
      Entry was = ((Entry) it.next());
      Log.i(String.format("m%3d", nummer), was.machDateiname());
      nummer++;
    }
  }

  void retteInDatei() {
    FileOutputStream outputStream;
    if (debug>2) Log.i("M040", filename + context);
    int nummer = 0;
    try {
      outputStream = context.openFileOutput(filename, Context.MODE_PRIVATE);
      for (Iterator it = this.iterator();it.hasNext();) {
        Entry was = (Entry) it.next();
        if (debug>8) Log.i(String.format("r%3d", nummer), was.machDateiname());
        outputStream.write(was.zuRetten().getBytes());
        nummer++;
      }
      outputStream.close();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  void retteInDatei(Context context, int debug, String suchbegriff) {
    this.context = context;
    this.debug = debug;
    this.filename = dateiname(suchbegriff);
    FileOutputStream outputStream;
    if (debug>2) Log.i("M040", filename + context);
    int nummer = 0;
    try {
      outputStream = context.openFileOutput(filename, Context.MODE_PRIVATE);
      for (Iterator it = this.iterator();it.hasNext();) {
        Entry was = (Entry) it.next();
        if (debug>8) Log.i(String.format("r%3d", nummer), was.machDateiname());
        outputStream.write(was.zuRetten().getBytes());
        nummer++;
      }
      outputStream.close();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
  // gerufen von downloadXmlTask.onPostExecute
  public DownloadDlfunk machSendungsbuttons() {
    LinearLayout layout = (LinearLayout) activity.findViewById(R.id.welcherTag);
    SharedPreferences mySharedPrefs;
    mySharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
    layout.removeAllViews();
    int nummer = 0;
    for (Iterator it = this.iterator();it.hasNext();) {
      Entry entry = ((Entry) it.next());
      if (debug>7) Log.i(String.format("b%3d", nummer), entry.machDateiname());
      Button Taste = new Button(activity);
      Taste.setLayoutParams(
          new ViewGroup.LayoutParams(
              ViewGroup.LayoutParams.MATCH_PARENT,
              ViewGroup.LayoutParams.WRAP_CONTENT
          )
      );
      final String quellurl = entry.link;
      final String duration = entry.duration;
      final String zieldateiname = entry.machDateiname();
      if (debug>1)
        Taste.setText("Sendung-"+ entry.buttontext(true, true));
      else
        Taste.setText(entry.buttontext(
            mySharedPrefs.getBoolean("autorPref", false),
            mySharedPrefs.getBoolean("zeitstempelPref", false)));
      Taste.setId(16081947 + nummer);
      if (debug>8) Log.i("E010", quellurl + " -> " + zieldateiname);
      Taste.setOnClickListener(
          new View.OnClickListener() {
            public void onClick(View v) {
              // Perform action on click
              try {
                if(downloadDlfunk!=null) {
                  if (debug>8) Log.i("E020", "downloadDlfunk.stoppeWiedergabe()");
                  downloadDlfunk.stoppeWiedergabe();
                }
                    downloadDlfunk = (DownloadDlfunk)
                    new DownloadDlfunk(
                        activity, context, debug, mediaPlayer)
                        .execute(new String[]{quellurl, zieldateiname, duration});
              } catch (Exception e) {
                e.printStackTrace();
              } finally {
                if (debug>8) Log.i("E030", downloadDlfunk.toString());
              }
              if (debug>8) Log.i("E040", "Lade " + zieldateiname + " herunter");
              Toast.makeText(context, "Lade " + zieldateiname + " herunter", Toast.LENGTH_SHORT).show();
            }
          }
      );
      //entry.setTaste(Taste);
      layout.addView(Taste);
      nummer++;
    }
    return downloadDlfunk;
  }
}
