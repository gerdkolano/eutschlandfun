package net.za.dyndns.gerd.deutschlandfunk.favoriten;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.preference.PreferenceManager;
import android.app.FragmentManager;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Surface;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import static android.view.View.*;

/* Erprobe git 2014-08-28
*
*   pushd /zoe-home/zoe-hanno/android/Deutschlandfunk
*   git commit
*   git push -u origin master
*/
public class
    WahlActivity
    extends ActionBarActivity
    implements BenutzerEdit.EditNameDialogListener {
  private int debug;
  // Whether there is a Wi-Fi connection.
  private static boolean wifiConnected = false;
  // Whether there is a mobile connection.
  private static boolean mobileConnected = false;
  private int prefSeite;
  private int seitenanzahl;
  private String suchbegriff;
  private MediaPlayer mediaPlayer;
  private SharedPreferences mySharedPrefs;
  private int debugSchranke;
  private Button dialogEditButton;
  private Button dialogInfoButton;
  private Button dialogFrageButton;
  private FragmentManager fm = getFragmentManager();

  public WahlActivity() {
    seitenanzahl = 1;
    prefSeite = 3;
    debug = 1; // debug=1 macht die Bedienelemente etwas schwatzhafter
    debugSchranke = -1;
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    if (debug > 0) Log.i("W---", "---------------------");
    if (debug > 0) Log.i("W000", "onCreate");
    // enables the activity icon as a 'home' button.
    // required if "android:targetSdkVersion" > 14
    getActionBar().setHomeButtonEnabled(true);
    mediaPlayer = new MediaPlayer(); // idle state
  }

  @Override
  protected void onRestart() {
    super.onRestart();
    if (debug > 0) Log.i("W---", "+++++++++++++++++++++");
    if (debug > 0) Log.i("W005", "onRestart DLF");

    //loadPage(suchbegriff, prefSeite);
  }

  // Refreshes the display if the network connection and the
  // pref settings allow it.
  @Override
  public void onStart() {
    super.onStart();

    //showEditDialog();

    // Gets the user's network preference settings
    mySharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);

    if (debug > 0) Log.i("W010", "onStart DLF is about to become visible");
    // Teste mal edit und commit
    if (false) {
      SharedPreferences.Editor editor = mySharedPrefs.edit();
      editor.putString("seitennummerPref", "2");
      //editor.putString("sendungsnamePreff", "kultur");
      if (editor.commit()) {
        if (debug > 8) Log.i("W011", "gut");
      } else {
        if (debug > 8) Log.i("W011", "schlecht");
      }
      String nummertext = mySharedPrefs.getString("seitennummerPref", "1");
      try {
        this.prefSeite = Integer.parseInt(nummertext);
        if (debug > 8) Log.i("W013", "nummertext = " + nummertext);
      } catch (NumberFormatException e) {
        //Will Throw exception!
        //do something! anything to handle the exception.
        if (debug > 8) Log.i("WE13", nummertext + "!" + e.toString());
      }
    }

    String nochEineSerie = mySharedPrefs.getString("nochEineSeriePref", "mySharedPrefs liefert nichts");
    if (debug > debugSchranke) Log.i("W014", "nochEineSerie = " + nochEineSerie);

    String debugtext = mySharedPrefs.getString("debugPref", "9");
    try {
      this.debug = Integer.parseInt(debugtext);
      if (debug > 8) Log.i("W014", "debugtext = " + debugtext);
    } catch (NumberFormatException e) {
      if (debug > 8) Log.i("WE14", debugtext + "!" + e.toString());
    }

    //requestFeature();

    setContentView(R.layout.activity_wahl); // für findViewById


    // Locate the buttons in activity_main.xml
    dialogEditButton = (Button) findViewById(R.id.dialogEditButton);
    dialogInfoButton = (Button) findViewById(R.id.dialogInfoButton);
    dialogFrageButton = (Button) findViewById(R.id.dialogFrageButton);

    // Capture button clicks
    dialogEditButton.setOnClickListener(new OnClickListener() {
      public void onClick(View arg0) {
        //showEditDialog();
        BenutzerEdit dialogEditFragment = new BenutzerEdit();
        // Show DialogFragment
        dialogEditFragment.show(fm, "Edit Dialog Fragment");
      }
    });

    // Capture button clicks
    dialogInfoButton.setOnClickListener(new OnClickListener() {
      public void onClick(View arg0) {
        String gespeicherterBegriff
            = mySharedPrefs.getString("sendungsnamePreff", "keine Vorwahl");
        BenutzerInfo dialogInfoFragment
            = new BenutzerInfo(WahlActivity.this, gespeicherterBegriff);
        // Show DialogFragment
        dialogInfoFragment.show(fm, "Info Dialog Fragment");
      }
    });

    // Capture button clicks
    dialogFrageButton.setOnClickListener(new OnClickListener() {
      public void onClick(View arg0) {
        (new FireMissilesDialogFragment(WahlActivity.this, ""))
            .show(fm, "FireMissilesDialogFragment");

      }
    });

    //(new FireMissilesDialogFragment(this, "")).show(fm, "FireMissilesDialogFragment");

    //addKeyListener();
    Button taste = (Button) findViewById(R.id.testTaste1);
    if (debug > 1)
      taste.setText(String.format("WahlActivity.onStart: Stoppe den Mediaplayer. debug=%d", debug));
    else
      taste.setText(String.format("Stoppe die Wiedergabe"));

    taste.setOnClickListener(
        new OnClickListener() {

          @Override
          public void onClick(View view) {
            if (debug > 2) Log.i("W001", "Mp " + mediaPlayer.toString());
            MediaPlayer.TrackInfo[] trackInfo;
            if (mediaPlayer.isPlaying()) {
              trackInfo = mediaPlayer.getTrackInfo();
              for (int ii = 0; ii < trackInfo.length; ii++) {
                if (debug > 2) Log.i(String.format("w%3d", ii), trackInfo[ii].toString());
              }
              mediaPlayer.stop();
            }
            Toast.makeText(WahlActivity.this,
                "W001 " + mediaPlayer.toString(), Toast.LENGTH_SHORT).show();
          }
        }
    );
    //erzeugeSerien();
    stelleSerienauswahlbuttonsHer();

    Display dp = getWindowManager().getDefaultDisplay();
    if (dp != null) {
      int rotation = dp.getRotation();
      double winkel = 0.0;
      switch (rotation) {
        case Surface.ROTATION_0:
          winkel = 0.0;
          break;
        case Surface.ROTATION_90:
          winkel = 90.0;
          break;
        case Surface.ROTATION_180:
          winkel = 180.0;
          break;
        case Surface.ROTATION_270:
          winkel = 270.0;
          break;
      }
      if (debug > 1) Log.i("W016", "Rotation hat "
          + winkel + "°, "
          + rotation + " natural rotation");
    }
    updateConnectedFlags();
    //this.getBaseContext();
  }

  @Override
  protected void onResume() {
    if (debug > 1) Log.i("W020", "onResume DLF has become visible");
    super.onResume();
  }

  @Override
  protected void onPause() {
    super.onPause();
    if (debug > 1) Log.i("W070", "onPause Another activity than DLF is taking focus");
  }

  @Override
  protected void onStop() {
    if (debug > 1) Log.i("W080", "onStop DLF is no longer visible");
    super.onStop();
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    if (debug > 1) Log.i("W090", "onDestroy DLF destroyed");
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {

    // Inflate the menu; this adds items to the action bar if it is present.
    getMenuInflater().inflate(R.menu.wahl, menu);
    if (debug > 1) Log.i("W089", "onCreateOptionsMenu DLF");
    return true;
  }

  // Handles the user's menu selection.
  // Berühre drei Klötzchen rechts oben auf dem Bildschirm.
  // Beschreibung in res/menu/wahl.xml
  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    if (debug > 1) Log.i("o010", "MenuItem id=" + item.getItemId() % 100);
    switch (item.getItemId()) {
      case android.R.id.home:
        // Berühre das DLF-Icon links oben auf dem Bildschirm
        if (debug > 1) Log.i("o011", "startActivityAfterCleanup(WahlActivity.class)");
        // ProjectsActivity is my 'home' activity
        startActivityAfterCleanup(WahlActivity.class);
        return true;
      case R.id.action_settings: // Settings res/xml/preferences.xml
        if (debug > 1) Log.i("o012", "Settings");
        //Intent settingsActivity = new Intent(getBaseContext(), SettingsActivity.class);
        //startActivity(settingsActivity);
        //Intent setPreferenceActivity = new Intent(getBaseContext(), SetPreferenceActivity.class);
        Intent settingsActivity = new Intent(getBaseContext(),
            SetPreferenceActivity.class);
        startActivity(settingsActivity);
        return true;
      case R.id.refresh:
        stelleSerienauswahlbuttonsHer(); // loadPage();
        return true;
      case R.id.vorigeSeite:
        this.prefSeite = ((this.prefSeite > 1) ? --this.prefSeite : this.seitenanzahl);
        stelleSerienauswahlbuttonsHer(); // loadPage();
        return true;
      case R.id.nächsteSeite:
        this.prefSeite++;
        stelleSerienauswahlbuttonsHer(); // loadPage();
        return true;
      case R.id.seitennummer:
        stelleSerienauswahlbuttonsHer(); // loadPage();
        return true;
      default:
        return super.onOptionsItemSelected(item);
    }
  }

  private void startActivityAfterCleanup(Class<?> cls) {
    //if (projectsDao != null) projectsDao.close();
    Intent intent = new Intent(getApplicationContext(), cls);
    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
    startActivity(intent);
  }


  private void stelleSerienauswahlbuttonsHer() {
/*
    new MachSerienauswahlButtons(WahlActivity.this, this,
        this.wifiConnected, this.mobileConnected,
        this.debug,
        mediaPlayer)
*/
    new Serien(WahlActivity.this, this,
        this.debug,
        mediaPlayer)
        .stelleSerienauswahlbuttonsHer();
  }

  // Checks the network connection and sets the wifiConnected and mobileConnected
  // variables accordingly.
  private void updateConnectedFlags() {
    ConnectivityManager connMgr =
        (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

    NetworkInfo activeInfo = connMgr.getActiveNetworkInfo();
    if (activeInfo != null && activeInfo.isConnected()) {
      wifiConnected = activeInfo.getType() == ConnectivityManager.TYPE_WIFI;
      mobileConnected = activeInfo.getType() == ConnectivityManager.TYPE_MOBILE;
    } else {
      wifiConnected = false;
      mobileConnected = false;
    }
  }

  @Override
  public void onFinishEditDialog(String inputText) {
    Serie serie = new Serie(inputText);
    suchbegriff = serie.getSuchbegriff();
    Toast.makeText(this, "WahlActivity.onFinishEditDialog: "
        + suchbegriff, Toast.LENGTH_SHORT).show();
    if (debug > debugSchranke) Log.i("W035", "suchbegriff=\"" + suchbegriff + "\"");
    if (!suchbegriff.equals("")) {
      // Anzeigen der Buttons zum Abspielen je einer der Sendungen,
      // die zu diesem Suchhbegriff gefunden werden.
      Serien serien;
      serien = new Serien(WahlActivity.this, this,
          this.debug,
          mediaPlayer);
      serien.loadPage(suchbegriff, 0);
      // Füge diesen Suchbegriff dem Objekt serien hinzu
      //serien = new Serien(WahlActivity.this, this, debug, mediaPlayer);
      serien.append(serie);
    }
  }

  public void doPositiverKlick() {
    if (debug > debugSchranke) Log.i("W040", "doPositiverKlick");
  }

  public void doNegativerKlick() {
    if (debug > debugSchranke) Log.i("W040", "doNegativerKlick");
  }

/*
  private void showEditDialog() {
    FragmentManager fm = getFragmentManager();
    BenutzerEdit editNameDialog = new BenutzerEdit();
    editNameDialog.show(fm, "dialogfragment");
  }

private EditText edittext;

 public void addKeyListener() {

	// get edittext component
	edittext = (EditText) findViewById(R.id.editText);

	// add a keylistener to keep track user input
	edittext.setOnKeyListener(new View.OnKeyListener() {
	public boolean onKey(View v, int keyCode, KeyEvent event) {

		// if keydown and "enter" is pressed
		if ((event.getAction() == KeyEvent.ACTION_DOWN)
			&& (keyCode == KeyEvent.KEYCODE_ENTER)) {

			// display a floating message
			Toast.makeText(WahlActivity.this,
				edittext.getText(), Toast.LENGTH_LONG).show();
			return true;

		} else if ((event.getAction() == KeyEvent.ACTION_DOWN)
			&& (keyCode == KeyEvent.KEYCODE_9)) {

			// display a floating message
			Toast.makeText(WahlActivity.this,
				"Number 9 was pressed!", Toast.LENGTH_LONG).show();
			return true;
		}

		return false;
	}
 });
}
*/
}

