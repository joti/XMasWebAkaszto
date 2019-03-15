package mgbeans;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.RequestScoped;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;
import javax.servlet.ServletContext;
import model.Word;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.input.SAXBuilder;

/**
 * @author Joti
 *
 * Játékszabály: - a feladványok a karácsonyi időszakhoz kapcsolódnak - minden feladvány esetében -
 * minden eltalált betűvel eggyel nő a díszek száma, a rossz tippekkel viszont csökken - egy
 * karácsonyfán 12 dísz helyezhető el - miután a 12. dísz is felkerül a fára, az adott fa díszítése
 * befejeződik, és a későbbiekben hibás tippekkel sem kerülhet le róla dísz - összesen 7 fa
 * díszíthető fel, ha mind a 7 feldíszítése sikerül, akkor a játék véget ér
 */
@ManagedBean
@SessionScoped
public class Feladvany implements Serializable {

  private static final String betuk = "AÁBCDEÉFGHIÍJKLMNOÓÖŐPQRSTUÚÜŰVWXYZ";
  private static final int maxTippek = 8;
  private static final String[] fakepek = {"tree00.png", // 0. elem: a fa még nem jelenik meg
    "tree01.png", "tree10.png", "tree11.png", "tree12.png", "tree13.png", "tree14.png", "tree15.png", // 1-7. elem: 0-6 db dísz
    "tree16.png", "tree17.png", "tree18.png", "tree19.png", "tree20.png", "treefinal.png", // 8-13. elem: 7-12 db dísz
    "treefinal.gif"}; // 14. elem: teljesen feldíszített fa animációja 

  private List<Word> feladvanyok;
  private List<Word> osszesfeladvany;

  private String temakor;
  private String megfejtes;
  private int szint;

  private String aktualis;
  private String tipp;
  private String hiba;
  private Set<String> tippek;
  private Set<String> rosszTippek;
  private int eredmeny; // -1: játék kezdete előtt, 0: feladvány folyamatban, 1: feladvány megfejtve, 2: a játékos kiesett, 3: maximális pontszám miatt vége a játéknak
  private int pontszam;

  public Feladvany() {
    System.out.println("első debug");
    tippek = new HashSet<>();
    rosszTippek = new HashSet<>();
    eredmeny = -1;

    String relativeWebPath = "/resources/words.xml";
    FacesContext facesContext = FacesContext.getCurrentInstance();
    ServletContext servletContext = (ServletContext) facesContext.getExternalContext().getContext();
    String absoluteDiskPath = servletContext.getRealPath(relativeWebPath);
    System.out.println(absoluteDiskPath);
    File file = new File(absoluteDiskPath);

    SAXBuilder sb = new SAXBuilder();
    Document document;
    try {
      document = sb.build(file);
      Element root = document.getRootElement();

      List<Element> elements = root.getChildren();
      osszesfeladvany = new ArrayList<>();
      feladvanyok = new ArrayList<>();

      for (Element element : elements) {
        String topic = element.getChildText("TOPIC");
        String text = element.getChildText("TEXT");
        int level = Integer.parseInt(element.getChildText("LEVEL"));
        System.out.println(text);

        Word w = new Word(topic, text, level);

        feladvanyok.add(w);
        osszesfeladvany.add(w);
      }
      System.out.println(feladvanyok.size());
      //ujJatek();

    } catch (Exception ex) {
      hiba += ex.toString();
      System.out.println(hiba);
    }
  }

  public void ujJatek() {
    pontszam = 0;
    ujFeladvany();
  }

  public void ujFeladvany() {
    Random rnd = new Random();
    Word w = feladvanyok.get(rnd.nextInt(feladvanyok.size()));
    feladvanyok.remove(w);

    if (feladvanyok.size() == 0) {
      feladvanyok.addAll(osszesfeladvany);
    }

    megfejtes = w.getText().toUpperCase();
    temakor = w.getTopic();
    szint = w.getLevel() + 1;
    System.out.println(temakor + ": " + megfejtes + " (" + szint + ")");

    aktualis = "";
    hiba = "";
    eredmeny = 0;
    tippek.clear();
    rosszTippek.clear();

    for (int i = 0; i < megfejtes.length(); i++) {
      if (megfejtes.substring(i, i + 1).equals(" ")) {
        aktualis += " ";
      } else if (betuk.contains(megfejtes.substring(i, i + 1))) {
        aktualis += "_";
      } else {
        aktualis += megfejtes.substring(i, i + 1);
      }
    }
    System.out.println(aktualis);
  }

  public void ujTipp() {
    hiba = "";
    boolean talalt = false;
    System.out.println("tipp" + tipp);
    tipp = tipp.toUpperCase();

    if (tipp.length() != 1) {
      hiba = "Adj meg egy betűt!";
      tipp = "";
      return;
    }

    if (!betuk.contains(tipp)) {
      hiba = "Adj meg egy betűt!";
      System.out.println(hiba);
      tipp = "";
      return;
    }

    if (tippek.contains(tipp)) {
      hiba = "Erre a betűre (" + tipp + ") már tippeltél korábban.\nVálassz egy másik betűt!";
      System.out.println(hiba);
      tipp = "";
      return;
    }

    tippek.add(tipp);

    for (int i = 0; i < megfejtes.length(); i++) {
      if (megfejtes.substring(i, i + 1).equalsIgnoreCase(tipp)) {
        System.out.println(aktualis);
        System.out.println(i);
        System.out.println(aktualis.substring(0, i));
        System.out.println(aktualis.substring(i + 1));
        aktualis = aktualis.substring(0, i) + tipp + aktualis.substring(i + 1);
        talalt = true;
        System.out.println(aktualis);
      }
    }
    if (talalt) {
      if (megfejtes.equals(aktualis)) {
        eredmeny = 1;
        pontszam += szint;
        if (pontszam >= 60) {
          pontszam = 60;
          eredmeny = 3;
        }
      }
    } else {
      rosszTippek.add(tipp);
      if (rosszTippek.size() == 3) {
        szint--;
      } else if (rosszTippek.size() > maxTippek - 1) {
        eredmeny = 2;
        aktualis = megfejtes;
      }
    }
    tipp = "";
    System.out.println("Eredmény:" + eredmeny + ", pontszám: " + pontszam);
  }

  public String getFormazottTemakor() {
    String formazott;
    if (rosszTippek.size() < 3 && eredmeny == 0) {
      formazott = "???";
    } else {
      formazott = temakor;
    }
    return formazott;
  }

  public String getFormazottAktualis() {
    String formazott = aktualis.substring(0, 1).toUpperCase();
    for (int i = 1; i < aktualis.length(); i++) {
      if (aktualis.substring(i, i + 1).equals(";")) {
        formazott += "    \n";
      } else {
        if (aktualis.substring(i, i + 1).equals(" ")) {
          formazott += " ";
        }
        formazott += " " + aktualis.substring(i, i + 1).toUpperCase();
      }
    }
    System.out.println("Formázott feladvány: " + formazott);
    return formazott;
  }

  public String getFormazottRosszTippek() {
    String formazott = "";
    for (int i = 0; i < betuk.length(); i++) {
      if (rosszTippek.contains(betuk.substring(i, i + 1))) {
        formazott += betuk.substring(i, i + 1) + " ";
      }
    }
    for (int i = 0; i < maxTippek - rosszTippek.size(); i++) {
      formazott += "_ ";
    }
    return formazott;
  }

  public String getMegfejtes() {
    return megfejtes;
  }

  public void setMegfejtes(String megfejtes) {
    this.megfejtes = megfejtes;
  }

  public String getAktualis() {
    return aktualis;
  }

  public void setAktualis(String aktualis) {
    this.aktualis = aktualis;
  }

  public Set<String> getTippek() {
    return tippek;
  }

  public void setTippek(Set<String> tippek) {
    this.tippek = tippek;
  }

  public String getTipp() {
    return tipp;
  }

  public void setTipp(String tipp) {
    this.tipp = tipp;
  }

  public Set<String> getRosszTippek() {
    return rosszTippek;
  }

  public void setHibak(Set<String> rosszTippek) {
    this.rosszTippek = rosszTippek;
  }

  public String getHiba() {
    return hiba;
  }

  public void setHiba(String hiba) {
    this.hiba = hiba;
  }

  public String getFakep(int sorszam) {
    int allapot = 0;
    String kep = "";

    if (12 * (sorszam + 1) <= pontszam) {
      allapot = 14;
    } else if (12 * sorszam <= pontszam) {
      allapot = pontszam % 12 + 1;
    }

    kep = fakepek[allapot];
    return kep;
  }

  public String getEredmenySzoveg() {
    switch (eredmeny) {
      case 3:
        return "Maximális pontszám. Gratulálok!";
      case 2:
        return "Vége a játéknak!";
      default:
        return "";
    }
  }

  public String getSubmitButtonClass() {
    if (eredmeny == 0) {
      return "button";
    } else {
      return "buttondisabled";
    }
  }

  public String getNewGameButtonClass() {
    if (eredmeny == 0) {
      return "invisible";
    } else {
      return "button";
    }
  }

  public int getEredmeny() {
    return eredmeny;
  }

  public void setEredmeny(int eredmeny) {
    this.eredmeny = eredmeny;
  }

  public String getTemakor() {
    return temakor;
  }

  public void setTemakor(String temakor) {
    this.temakor = temakor;
  }

  public int getSzint() {
    return szint;
  }

  public void setSzint(int szint) {
    this.szint = szint;
  }

  public int getPontszam() {
    return pontszam;
  }

  public void setPontszam(int pontszam) {
    this.pontszam = pontszam;
  }

}
