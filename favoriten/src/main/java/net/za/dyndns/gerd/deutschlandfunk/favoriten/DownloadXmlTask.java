package net.za.dyndns.gerd.deutschlandfunk.favoriten;

import android.app.Activity;
import android.content.Context;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * Created by hanno on 06.06.14.
 */

// Implementation of AsyncTask used to download XML feed from srv.deutschlandradio.de.
// <String, String, Entries>
// <String, für doInBackground und execute(Params )
//  String,    für Progress
//  Entries>  für onPostExecute(String result)
//
class DownloadXmlTask extends AsyncTask<String, String, Menge> {
  /*
   * Overrides
   * Entries doInBackground(String... urls)
   *         darin void publishProgress
   * UI void onPreExecute()
   * UI void onPostExecute(Menge alleSendungen)
   * UI void onProgressUpdate(String... publishedProgress)
   * noch zu erlernen : cancel, isCancelled, onCancelled
   */
  Activity activity;
  Context context;
  int debug;
  DownloadDlfunk downloadDlfunk;
  int seitennummer;
  private Menge allentries;
  private MediaPlayer mediaPlayer;
  private TextView ladeFortschritt;

  public Menge getEntries() {
    return allentries;
  }

  /*
  * gerufen als .execute() von
  */
  DownloadXmlTask(Activity activity, Context context, int debug,
                  int seitennummer, DownloadDlfunk downloadDlfunk, MediaPlayer mediaPlayer) {
    this.activity = activity;
    this.context = context;
    this.debug = debug;
    this.seitennummer = seitennummer;
    this.downloadDlfunk = downloadDlfunk;
    this.mediaPlayer = mediaPlayer;
    //activity.setContentView(R.layout.activity_wahl); // Entfernt frühere Eintragungen
    ladeFortschritt = (TextView) this.activity.findViewById(R.id.fortschritt);
    ladeFortschritt.setText(String.format("Ladefortschritt in DownloadXmlTask"));
  }

  public void stoppeWiedergabe() {
    Log.i("X098", "Stoppe vielleicht downloadDlfunk");
    if (downloadDlfunk != null) {
      Log.i("X099", "Stoppe downloadDlfunk");
      downloadDlfunk.stoppeWiedergabe();
    }
  }

  /*
   * Der von doInBackground gelieferte Wert wird von
   * onPostExecute(Menge result) weiterverarbeitet.
   * Der dritte Parameter AsyncTask<String, String, Entries>
   * bestimmt den Typ "Menge".
   */
  @Override
  protected Menge doInBackground(String... urls) { // DownloadXmlTask
    // ruft ladeXmlseiten
    // wird gerufen in MachSerienauswahlButtons
    // als
    // new DownloadXmlTask(
    //      activity, context, this.debug,
    //      seitennummer, downloadDlfunk, mediaPlayer
    //  ).execute(suchbegriff);
    String ServerURL =
        "http://srv.deutschlandradio.de/aodlistaudio.1706.de.rpc";
    String suchbegriff = urls[0];
    String myUrlString = ServerURL
        + "?drau:" + suchbegriff;
    // + "&drau:page=" + seitennummer;
    String seitennummerparameter = "&drau:page=";
    // Lade entries aus aus der Datei zum suchbegriff
    Menge menge = new Menge(activity, context, debug, mediaPlayer, suchbegriff);
    if (debug>7) menge.logge();
    if (debug>0) Log.i("X070", menge.size() + " Adressen in " + menge.seitenanzahl + " Seiten");
    publishProgress(
        ""
            + menge.size()
            + " Http-Mp3-Adressen aus der Datei "
            + menge.getFilename()
            + " geladen. "
            + menge.seitenanzahl
            + " Seiten, "
            + suchbegriff
    );
    if (menge.isEmpty()) {
      try {
        //xx menge = ladeXmlBeschreibungen(myUrlString, seitennummerparameter, true);
        menge.addAll(ladeXmlBeschreibungen(myUrlString, seitennummerparameter, true));
      } catch (IOException e) {
        //return new Sendungen(getResources().getString(R.string.connection_error));
      } catch (XmlPullParserException e) {
        //return new Sendungen(getResources().getString(R.string.xml_error));
      }
      // Wir wissen nun, wieviele Seiten der Server bereithält und
      // könnten sie alle holen.
      if (debug>0) Log.i("X080", menge.seitenanzahl + " Seiten");
      //xx menge.retteInDatei(context, debug, suchbegriff);
      menge.retteInDatei();
    }
    menge.zeigeDateiAbbild();
    return menge;
  }

  @Override
  protected void onPreExecute() { // DownloadXmlTask
    //activity.setContentView(R.layout.activity_wahl); // Entfernt frühere Eintragungen
    Button taste = (Button) activity.findViewById(R.id.testTaste2);
    taste.setText("DownloadXmlTask.onPreExecute()");
    taste.setOnClickListener(
        new View.OnClickListener() {

          @Override
          public void onClick(View view) {
            Log.i("W001", "Mp " + mediaPlayer.toString());
            int ii = 0;
            try {
              ii = mediaPlayer.getAudioSessionId();
            } catch (Exception e) {
              Log.i("W002", "Mp Error" + e.toString());
            }
            Log.i("W003", "Mp AudioSessionId " + ii);
            ii = 0;
            try {
              ii = mediaPlayer.getCurrentPosition();
            } catch (Exception e) {
              Log.i("W004", "Mp Error" + e.toString());
            }
            Log.i("W005", "Mp CurrentPosition " + ii);
            ii = 0;
            try {
              ii = mediaPlayer.getDuration();
            } catch (Exception e) {
              Log.i("W006", "Mp Error" + e.toString());
            }
            Log.i("W007", "Mp Duration " + ii);
            Toast.makeText(activity,
                "Mp" + mediaPlayer.toString(),
                Toast.LENGTH_SHORT).show();
          }

        }
    );
    //myTextView = (TextView) activity.findViewById(R.id.textView1);
  }

  @Override
  protected void onProgressUpdate(String... publishedProgress) { // DownloadXmlTask
    // publishProgress liefert die hier verwendeten publishedProgresses
    // super.onProgressUpdate(publishedProgress); // nichts wird sichtbar
    // ladeOderSpielFortschritt.setText(String.format(
    // "%02d%% Ladefortschritt von %d", result[0], result[1]));
    // myTextView.setText(publishedProgress[0]);
    String fortschritt = publishedProgress[0];
    if (debug>0) Log.i("X010", fortschritt);
    ladeFortschritt.setText(fortschritt);
    //super.onProgressUpdate(publishedProgress);
  }

  @Override
  protected void onPostExecute(Menge alleSendungen) { // DownloadXmlTask
    this.allentries = alleSendungen;
    // Erstelle je einen Button für jede Sendung.
    //010Sendungsbuttons(alleSendungen);
    if (debug>2) Log.i("X090", "Erste Http-Mp3-Adresse: " + alleSendungen.erst().link);

    Button taste = (Button) activity.findViewById(R.id.testTaste2);
    if (debug>1)
      taste.setText("DownloadXmlTask.onPostExecute() Lösche Verweise " + allentries.getFilename());
    else
      taste.setText("Lösche Verweise " + allentries.getFilename());

    taste.setOnClickListener(
        new View.OnClickListener() {

          @Override
          public void onClick(View view) {
            String worte;
            if (allentries.loescheDateiAbbild())
              worte = "Lösche " + allentries.getFilename();
            else
              worte = "Kann " + allentries.getFilename() + " nicht löschen";
            Toast.makeText(activity,
                worte,
                Toast.LENGTH_SHORT).show();
          }

        }
    );

    //downloadDlfunk = new MachSendungsButtons(activity, context, debug, mediaPlayer)
    //    .machSendungsbuttons(alleSendungen);
    downloadDlfunk = alleSendungen.machSendungsbuttons();
    if (debug>3) Log.i("X091", downloadDlfunk == null ? "downloadDlfunk==null" : downloadDlfunk.toString());
  }

  /**
   * Lädt XML von srv.deutschlandradio.de, parses it,
   * and combines it with HTML markup.
   * Liefert eine Liste "entries" aufgezeichneter Sendungen mit ihren Eigenschaften.
   * gerufen von doInBackground, ruft downloadUrl
   * Parst den von downloadUrl gelieferten Stream und
   * publiziert den Fortschritt mittels "publishProgress".
   */
  private Menge ladeXmlBeschreibungen(
      String urlString,
      String seitennummerparameter, boolean alleXmlseiten)
      throws XmlPullParserException, IOException {
    int seitennummer = 1;
    InputStream stream = null;
    DeutschlandradioXmlParser dlfunkXmlParser = new DeutschlandradioXmlParser(debug);
    Menge entries = null; // Liste aufgezeichneter Sendungen
    Menge einigeSendungen = null; // Liste einiger aufgezeichneter Sendungen
    Calendar rightNow = Calendar.getInstance();
    //DateFormat formatter = new SimpleDateFormat("MMM dd h:mmaa");
    DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH.mm.ss");
    int letzteNummer = 0;
    do {
      // Hole die Meta-Daten aufgezeichneter Sendungen aus dem Internet
      String urlStringMitSeite = urlString + seitennummerparameter + seitennummer;
      // http://srv.deutschlandradio.de/aodlistaudio.1706.de.rpc?drau:searchterm=forschung+aktuell&drau:page=4
      if (debug>8) Log.i("X040", urlStringMitSeite);
      stream = downloadUrl(urlStringMitSeite);
      einigeSendungen = dlfunkXmlParser.parseEine(stream);
      if (entries == null) {
        entries = einigeSendungen;
      } else {
        entries.addAll(einigeSendungen);
      }
      if (stream != null) stream.close();
      letzteNummer = entries.seitenanzahl;
      publishProgress(  // die Parameter werden von onProgressUpdate verarbeitet
          String.format("%02d%% %02d von %02d %s XML-Ladefortschritt",
              seitennummer * 100 / letzteNummer,
              seitennummer,
              letzteNummer,
              urlStringMitSeite)
      );
      seitennummer++;
    } while (alleXmlseiten && !entries.isEmpty() && seitennummer <= letzteNummer);
    if (debug>2)
      if (entries.isEmpty())
        Log.i("X050", (alleXmlseiten ? "alle" : "eine") + " " + 0 + " Seiten");
      else
        Log.i("X051", (alleXmlseiten ? "alle" : "eine") + " " + entries.seitenanzahl + " Seiten");
    return entries;
  }

  // Given a string representation of a URL, sets up a connection and gets
  // an input stream.
  private InputStream downloadUrl(String urlString) throws IOException {
    // gerufen von ladeXmlseiten
    if (debug>0) Log.i("X045", "streaming " + urlString);
    URL url = new URL(urlString);
    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
    conn.setReadTimeout(10000 /* milliseconds */);
    conn.setConnectTimeout(15000 /* milliseconds */);
    conn.setRequestMethod("GET");
    conn.setDoInput(true);
    // Starts the query
    conn.connect();
    //this.gelieferteHeader = conn.getHeaderFields().toString();
    InputStream stream;
    stream = conn.getInputStream();
    return stream;
  }
}
