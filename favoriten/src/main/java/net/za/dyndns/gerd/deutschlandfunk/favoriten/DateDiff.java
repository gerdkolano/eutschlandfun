package net.za.dyndns.gerd.deutschlandfunk.favoriten;

import junit.framework.ComparisonFailure;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

/**
 * Created by hanno on 27.08.14.
 * <p/>
 * DateDiff -- compute the difference between two dates.
 */
public class DateDiff {
  public static void main(String[] av) {
    /** The date at the end of the last century */
    Date d1 = new GregorianCalendar(2000, 11, 31, 23, 59).getTime();

    /** Today's date */
    Date today = new Date();

    // Get msec from each, and subtract.
    long diff = today.getTime() - d1.getTime();

    // Construct a new GregorianCalendar initialized to the current date
    Calendar heute = new GregorianCalendar();
    Holtermine holtermine = new Holtermine(heute);

    holtermine.add(2, "Gelber Sack", new GregorianCalendar(2014, 0, 2), new GregorianCalendar(2016, 0, 1));
    holtermine.add(4, "Berlin Recycling", new GregorianCalendar(2014, 0, 22), new GregorianCalendar(2016, 0, 1));
    holtermine.add(4, "Veolia", new GregorianCalendar(2014, 0, 14), new GregorianCalendar(2016, 0, 1));
    holtermine.add(4, "Alba Pappy", new GregorianCalendar(2014, 0, 8), new GregorianCalendar(2016, 0, 1));

    holtermine.zeige();
    System.out.println("Das 21ste Jahrhundert (am " + today + ") ist "
        + (diff / (1000 * 60 * 60 * 24)) + " Tage alt.");
  }
}

class EinTermin implements Comparable<EinTermin> {
  private String firma;
  private long zeitpunkt;

  EinTermin(String firma, long zeitpunkt) {
    this.firma = firma;
    this.zeitpunkt = zeitpunkt;
  }

  @Override
  public int compareTo(EinTermin n) {
    //int lastCmp = zeitpunkt.compareTo(n.zeitpunkt);
    int lastCmp = Long.compare(this.zeitpunkt, n.zeitpunkt);
    return (lastCmp != 0 ? lastCmp : this.firma.compareTo(n.firma));
  }

  public String toString() {
    Calendar calendar = new GregorianCalendar();
    calendar.setTime(new Date(zeitpunkt));
    return String.format("%04d-%02d-%02d %s",
        calendar.get(Calendar.YEAR),
        1 + calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH),
        firma
    );
  }
}

class Holtermine {
  private List<EinTermin> abholtermine;
  Calendar abHeute;
  long heuteMs;

  Holtermine() {
    abholtermine = new ArrayList<EinTermin>();
    heuteMs = Long.MIN_VALUE;
  }

  Holtermine(Calendar abHeute) {
    this();
    this.abHeute = abHeute;
    heuteMs = abHeute.getTimeInMillis();
  }

  public void adda(String firma, Calendar anfang, Calendar ende) {
    long msJeVierWochen = 4L * 7l * 86400l * 1000L;
    long anfangMs, endeMs;
    anfangMs = anfang.getTime().getTime();
    endeMs = ende.getTime().getTime();
    for (long zeitpunkt = anfangMs;
         zeitpunkt < endeMs;
         zeitpunkt += msJeVierWochen) {
      if (zeitpunkt > heuteMs)
        add(new EinTermin(firma, zeitpunkt));
    }
  }

  public void add(long intervall, String firma, Calendar anfang, Calendar ende) {
    long msJeVierWochen = intervall * 7l * 86400l * 1000L;
    long anfangMs, endeMs;
    anfangMs = anfang.getTime().getTime();
    endeMs = ende.getTime().getTime();
    long vierWochen = (heuteMs - anfangMs) / msJeVierWochen;
    anfangMs += (vierWochen + 1) * msJeVierWochen;
    for (long zeitpunkt = anfangMs;
         zeitpunkt < endeMs;
         zeitpunkt += msJeVierWochen) {
      add(new EinTermin(firma, zeitpunkt));
    }
  }

  public void add(EinTermin einTermin) {
    abholtermine.add(einTermin);
  }

  public void zeige() {
    Collections.sort(abholtermine);
    for (EinTermin einTermin : abholtermine) {
      System.out.println(einTermin.toString());
    }
  }

  public Date getEasterDate(int year) {
    Date result = null;

    int a = year % 19;
    int b = year / 100;
    int c = year % 100;
    int d = b / 4;
    int e = b % 4;
    int f = (b + 8) / 25;
    int g = (b - f + 1) / 3;
    int h = (19 * a + b - d - g + 15) % 30;
    int i = c / 4;
    int k = c % 4;
    int l = (32 + 2 * e + 2 * i - h - k) % 7;
    int m = (a + 11 * h + 22 * l) / 451;
    int p = (h + l - 7 * m + 114) % 31;

    int month = (h + l - 7 * m + 114) / 31;
    int day = p + 1;

    GregorianCalendar gc = new GregorianCalendar(year, month - 1, day);
    result = gc.getTime();

    return result;
  }

  public int getEasterSundayMonth(int y) {
    int a = y % 19;
    int b = y / 100;
    int c = y % 100;
    int d = b / 4;
    int e = b % 4;
    int g = (8 * b + 13) / 25;
    int h = (19 * a + b - d - g + 15) % 30;
    int j = c / 4;
    int k = c % 4;
    int m = (a + 11 * h) / 319;
    int r = (2 * e + 2 * j - k - h + m + 32) % 7;
    int n = (h - m + r + 90) / 25;
    int p = (h - m + r + n + 19) % 32;
    int month = n;
    int day = p;
    return p;
  }

}

/*
http://www.merlyn.demon.co.uk/estralgs.txt
https://de.wikipedia.org/wiki/Gau%C3%9Fsche_Osterformel
Wikipedia Lichtenberg
 1. die Säkularzahl:                                       K(X) = X div 100
 2. die säkulare Mondschaltung:                            M(K) = 15 + (3K + 3) div 4 − (8K + 13) div 25
 3. die säkulare Sonnenschaltung:                          S(K) = 2 − (3K + 3) div 4
 4. den Mondparameter:                                     A(X) = X mod 19
 5. den Keim für den ersten Vollmond im Frühling:        D(A,M) = (19A + M) mod 30
 6. die kalendarische Korrekturgröße:                    R(D,A) = (D + A div 11) div 29[12]
 7. die Ostergrenze:                                    OG(D,R) = 21 + D − R
 8. den ersten Sonntag im März:                         SZ(X,S) = 7 − (X + X div 4 + S) mod 7
 9. die Entfernung des Ostersonntags von der
    Ostergrenze (Osterentfernung in Tagen):           OE(OG,SZ) = 7 − (OG − SZ) mod 7
10. das Datum des Ostersonntags als Märzdatum
    (32. März = 1. April usw.):                              OS = OG + OE
 */