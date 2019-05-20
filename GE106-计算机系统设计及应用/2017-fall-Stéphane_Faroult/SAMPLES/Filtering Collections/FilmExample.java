import java.util.ArrayList;

class Film {
     private String    title;
     private String    countries;
     private int       year;
     private double    billionRMB;         

     // Constructor
     public Film(String title,
                 String countries,
                 int    year,
                 double billionRMB) {
        this.title = new String(title);
        this.countries = new String(countries);
        this.year = year;
        this.billionRMB = billionRMB;
     }

     // Getter for the test
     int getYear() {
       return year;
     }

     public String toString() {
       return title + " (" + countries + " - " + year + ")"
                    + "\t" + Double.toString(billionRMB) + " billion RMB";
     }

}

// Interface defined for filtering
interface SelectFilm { boolean test(Film film);}

public class FilmExample {
    private static ArrayList<Film> films = new ArrayList<Film>();

    static void loadFilms() {
        // Chinese Box-Office, now outdated ("Wolf 2" well ahead, as well
        // as "Never Say Die")
        films.add(new Film("美人魚","China,Hong Kong",2016,3.393));
        films.add(new Film("The Fate of the Furious",
                           "United States",2017,2.651));
        films.add(new Film("捉妖记","China,Hong Kong",2015,2.440));
        films.add(new Film("Furious 7","United States",2015,2.427));
        films.add(new Film("Transformers: Age of Extinction",
                           "United States,China",2014,1.978));
        films.add(new Film("功夫瑜伽","China,India",2017,1.753));
        films.add(new Film("寻龙诀","China",2015,1.683));
        films.add(new Film("西游伏妖篇","China,Hong Kong",2017,1.657));
        films.add(new Film("港囧","China",2015,1.614));
        films.add(new Film("Zootopia","United States",2016,1.530));
        films.add(new Film("Warcraft","United States",2016,1.472));
        films.add(new Film("Avengers: Age of Ultron",
                           "United States",2015,1.464));
        films.add(new Film("夏洛特烦恼","China",2015,1.442));
        films.add(new Film("Jurassic World","United States",2015,1.421));
        films.add(new Film("Avatar","United States",2009,1.340));
        films.add(new Film("人再囧途之泰囧","China",2012,1.272));
        films.add(new Film("西游·降魔篇","China,Hong Kong",2013,1.247));
        films.add(new Film("Captain America: Civil War",
                           "United States",2016,1.246));
        films.add(new Film("西游记之孫悟空三打白骨精",
                           "China,Hong Kong",2016,1.201));
        films.add(new Film("湄公河行动","China,Hong Kong",2016,1.184));
    }

    static void showFilms(SelectFilm tester) {
        for (Film f: films) {
            if (tester.test(f)) {
                System.out.println(f);
            }
        }
    }

    public static void main(String args[]) {
        loadFilms();
        System.out.println("\n2016 films");
        System.out.println("----------");
        showFilms(new SelectFilm() {
            public boolean test(Film f) {
                return f.getYear() == 2016;
            }
        });
    }
}

