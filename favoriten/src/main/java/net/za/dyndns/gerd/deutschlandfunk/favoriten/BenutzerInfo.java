package net.za.dyndns.gerd.deutschlandfunk.favoriten;

import android.app.DialogFragment;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Created by hanno on 24.08.14.
 */
public class BenutzerInfo extends DialogFragment {
  private Context context;
  private String suchbegriff;
  public BenutzerInfo(Context context, String suchbegriff) {
    this.suchbegriff = suchbegriff;
    this.context = context;
  }

  @Override
  public View onCreateView(LayoutInflater inflater, final ViewGroup container,
                           Bundle savedInstanceState) {
    //final Context context = this.context;
    View rootView = inflater.inflate(R.layout.anzeigefragment, container,
        false);
    getDialog().setTitle("BenutzerInfo.java");
    final TextView textView = (TextView) rootView.findViewById(R.id.anzeigefragment_textview);
    textView.setText("suchbegriff=\"" + suchbegriff + "\"\n");
    textView.append("<h1>" + context.getFilesDir().toString()+ "</h1>");
    for (String dateiname : context.fileList()) {
      textView.append(dateiname + "\n");      
    }
    textView.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
            Toast.makeText(context, "berührt", Toast.LENGTH_SHORT).show();
      }
    });

    Button speichern = (Button) rootView.findViewById(R.id.speichern);
    speichern.setText(R.string.speichern);
    speichern.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        Toast.makeText(context, R.string.speichern, Toast.LENGTH_SHORT).show();
        ((WahlActivity) getActivity()).doPositiverKlick();
      }
    });

    Button löschen = (Button) rootView.findViewById(R.id.löschen);
    löschen.setText(R.string.löschen);
    löschen.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        Toast.makeText(context, R.string.löschen, Toast.LENGTH_SHORT).show();
        ((WahlActivity) getActivity()).doNegativerKlick();
      }
    });
    return rootView;
  }

}
