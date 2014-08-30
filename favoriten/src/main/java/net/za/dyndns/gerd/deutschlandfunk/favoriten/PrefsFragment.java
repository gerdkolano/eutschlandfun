package net.za.dyndns.gerd.deutschlandfunk.favoriten;

import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.util.Log;

/**
 * Created by hanno on 08.06.14.
 */
public class PrefsFragment extends PreferenceFragment {

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    Log.i("Pfr0", "PrefsFragment");
    // Load the preferences from an XML resource
    addPreferencesFromResource(R.xml.preferences); // Datei res/xml/preferences.xml
    Log.i("Pfr1", "PrefsFragment");
  }
}
