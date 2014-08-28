package net.za.dyndns.gerd.deutschlandfunk.favoriten;

import android.util.Log;
import android.widget.Button;

/**
 * Created by hanno on 06.06.14.
 * This class represents a single entry (post) in the XML feed.
 * It includes the data members "title," "link," and "zeitstempel."
 */
public class Entry implements Comparable<Entry> { // benötigt compareTo(Entry entry)
  public String title;
  public String autor;
  public int seitenanzahl;
  public int seitennummer;
  public String link;
  public String duration;
  public String zeitstempel;
  public String htmlDarstellung;
  public Button taste;
  public int debug;
  public static final int siebenZeilen = 7;

  public Entry(String title, String autor,
               int seitenanzahl, int seitennummer,
               String zeitstempel, String link, String duration, int debug) {
    this.title = title;
    this.autor = autor;
    this.seitenanzahl = seitenanzahl;
    this.seitennummer = seitennummer;
    this.zeitstempel = zeitstempel;
    this.link = link;
    this.duration = duration;
    this.htmlDarstellung = "";
    this.debug = debug;
  }

  public Entry(int debug) {
    this.title = null;
    this.autor = null;
    this.seitenanzahl = 0;
    this.seitennummer = 0;
    this.zeitstempel = null;
    this.link = null;
    this.duration = null;
    this.htmlDarstellung = "";
    this.taste = null;
    this.debug = debug;
  }

  @Override
  public int compareTo(Entry entry) {  // für "implements Comparable<Entry>"
    //return this.link.compareTo(entry.link);
    return entry.link.compareTo(this.link);
  }

  public void setTaste(Button taste) {
    this.taste = taste;
  }

  public void machHtml(
      boolean verweisPref,
      boolean zeitstempelPref,
      boolean autorPref) {
    StringBuilder htmlInhalt = new StringBuilder("");
    htmlInhalt.append("<p>");
//      htmlInhalt.append(this.seitennummer + "/");
//      htmlInhalt.append(this.seitenanzahl + " ");
    if (verweisPref) htmlInhalt.append(link + " ");
    htmlInhalt.append("<a href='" + link + "'>");
    htmlInhalt.append(title + "</a></p>");
    // If the user set the preference to include zeitstempel text,
    // adds it to the display.
    if (zeitstempelPref) htmlInhalt.append("Sendezeit: " + zeitstempel);
    if (autorPref) htmlInhalt.append(" Autor: " + autor);
    htmlDarstellung = htmlInhalt.toString();
  }

  public String machDateiname() {
    String zwerg = link;
    zwerg = zwerg.replaceFirst(".*/.*?_", "");
    zwerg = zwerg.replaceFirst("([0-9_]*)_.*(\\..*)", "$1$2");
    return zwerg;
  }

  public CharSequence buttontext(boolean autor, boolean zeit) {
    if (debug>1)
      return ""
              + (this.seitennummer + 1) + "/"
              + this.seitenanzahl + " "
              + this.autor + " "
              + this.title + " -> "
              + this.machDateiname();
    else
      return ""
          + (zeit ?(this.zeitstempel + " \u2014 "):"") // em-dash
          + (autor?(this.autor       + " \u2014 "):"") // em-dash
              + this.title;
  }

  public String zuRetten() {
    // Schreibe siebenZeilen Zeilen
    String erg = String.format("%s\n%s\n%d\n%d\n%s\n%s\n%s\n",
        this.title,
        this.autor,
        this.seitenanzahl,
        this.seitennummer,
        this.zeitstempel,
        this.link,
        this.duration
    );
    return erg;
  }

  public void ladeNach(String strLine, int zeile) {
    // Print the content on the console
    if (debug>8) Log.i("Lade", strLine);
    //System.out.println(strLine);
    switch (zeile % siebenZeilen) {
      case 0:
        title = strLine;
        break;
      case 1:
        autor = strLine;
        break;
      case 2:
        try {
          seitenanzahl = Integer.parseInt(strLine);
        } catch (NumberFormatException e) {
          seitenanzahl = 0;
        }
        break;
      case 3:
        try {
          seitennummer = Integer.parseInt(strLine);
        } catch (NumberFormatException e) {
          seitennummer = 0;
        }
        break;
      case 4:
        zeitstempel = strLine;
        break;
      case 5:
        link = strLine;
        break;
      case 6:
        duration = strLine;
        break;
      default:
        break;
    }
  }

}

