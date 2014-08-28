package net.za.dyndns.gerd.deutschlandfunk.favoriten;

/**
 * Created by hanno on 06.06.14.
 */
public class Sendung {
  private String name;
  private String suchbegriff;

  Sendung(String name, String Suchbegriff) {
    this.name = name;
    this.suchbegriff = suchbegriff;
  }

  public String getName() {
    return this.name;
  }

  public String getSuchbegriff() {
    return this.suchbegriff;
  }

}
