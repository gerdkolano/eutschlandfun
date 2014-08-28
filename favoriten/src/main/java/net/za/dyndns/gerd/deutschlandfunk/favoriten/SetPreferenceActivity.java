package net.za.dyndns.gerd.deutschlandfunk.favoriten;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

/**
 * Created by hanno on 08.06.14.
 */
public class SetPreferenceActivity extends Activity {

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
/*
* android.R.id.content gives you the root element of a view,
* without having to know its actual name/type/ID.
 */
    Log.i("Sprf", "start PreferenceActivity");
    getFragmentManager().beginTransaction().replace(android.R.id.content,
        new PrefsFragment()).commit();
    Log.i("Sprf", "stop PreferenceActivity");
  }
}
