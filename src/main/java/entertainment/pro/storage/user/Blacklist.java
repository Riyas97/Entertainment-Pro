//@@author pdotdeep

package entertainment.pro.storage.user;

import entertainment.pro.commons.exceptions.DuplicateEntryException;
import entertainment.pro.model.MovieInfoObject;
import entertainment.pro.model.MovieModel;
import entertainment.pro.storage.utils.BlacklistStorage;

import java.util.ArrayList;

/**
 * Class Maintains Blacklisted items.
 *
 */
public class Blacklist {

    private static ArrayList<String>  blackListKeyWords = new ArrayList<>();
    private static ArrayList<MovieModel>  blackListMovies = new ArrayList<>();
    private static ArrayList<String>  blackListMoviesTitle = new ArrayList<>();


    /**
     * Gets the blacklistkeywords arraylist.
     * @return the arraylist of blacklisted keywords
     */
    public static ArrayList<String> getBlackListKeyWords() {
        return (ArrayList<String>) blackListKeyWords;
    }

    /**
     * Gets the blacklistMovieTitles arraylist.
     * @return the arraylist of blacklisted Movie titles
     */
    public static ArrayList<String> getBlackListMoviesTitle() {
        return (ArrayList<String>) blackListMoviesTitle.clone();
    }

    /**
     * Gets the blacklistMovies arraylist.
     * @return the arraylist of blacklisted movie objects
     */
    public static ArrayList<MovieModel> getBlackListMovies() {

        return (ArrayList<MovieModel>) blackListMovies.clone();
    }


    /**
     * initialises the blacklisted words from the file.
     */
    public static void initialiseAll(ArrayList<String> keywords, ArrayList<String> movieTitles,
                                     ArrayList<MovieModel> movies) {
        initialiseBlackListKey(keywords);
        initialiseBlackListMovieID(movies);
        initialiseBlackListMovieTitles(movieTitles);
    }

    /**
     * Initialise keywords arraylist from file data.
     */
    private static void initialiseBlackListKey(ArrayList<String> keywords) {
        blackListKeyWords = (ArrayList<String>) keywords.clone();
    }

    /**
     * Initialise MovieTitles arraylist from file data.
     */
    private static void initialiseBlackListMovieTitles(ArrayList<String> movieTitles) {
        blackListMoviesTitle = (ArrayList<String>) movieTitles.clone();
    }


    /**
     * Initialise MovieID arraylist from file data.
     */
    private static void initialiseBlackListMovieID(ArrayList<MovieModel> movies) {
        for (MovieModel m : movies) {
            blackListMovies.add(m);
        }
    }

    /**
     * Adding keywords to blacklist.
     *
     * @param movie command that was entered by the user in split array form
     */
    public static void addToBlacklistKeyWord(String movie) throws DuplicateEntryException {
        if (movie.trim().equals("")) {
            return;
        }

        if (blackListKeyWords.contains(movie.toLowerCase())) {
            throw new DuplicateEntryException("blacklist");
        }
        blackListKeyWords.add(movie.toLowerCase());
        saveBlackList();
    }

    /**
     * Adding Movies to blacklist.
     * @param mo movie object
     */
    public static void addToBlacklistMoviesID(MovieInfoObject mo) throws DuplicateEntryException {
        if (mo == null || mo.getTitle().equals("")) {
            return;
        }
        for (MovieModel mm: blackListMovies) {
            if (mm.getTitle().toLowerCase().trim().equals(mo.getTitle().toLowerCase().trim())) {
                throw new DuplicateEntryException("blacklist");
            }
        }
        blackListMovies.add(new MovieModel(mo.getId(), mo.getTitle().toLowerCase()));
        saveBlackList();
    }

    /**
     * Adding Movies to blacklist.
     */
    public static void addToBlacklistMovie(String movie) throws DuplicateEntryException {
        if (movie.trim().equals("")) {
            return;
        }
        if (blackListMoviesTitle.contains(movie.toLowerCase().trim())) {

            throw new DuplicateEntryException("blacklist");
        }
        blackListMoviesTitle.add(movie.toLowerCase());
        saveBlackList();
    }

    /**
     * Save blackilist to JSON File.
     */
    public static void saveBlackList() {
        try {
            BlacklistStorage allbl = new BlacklistStorage();
            allbl.updateBlacklistFile(blackListKeyWords, blackListMovies, blackListMoviesTitle);

        } catch (Exception e) {
            //TODO ADD exception handling
        }


    }

    /**
     * removes keyword from blacklist.
     * @return true if keyword successfully removed
     */
    public static boolean removeFromBlacklistKeyWord(String keyword)  {
        if (keyword.trim().equals("")) {
            return false;
        }
        ArrayList<String> newKeywords = (ArrayList<String>) blackListKeyWords.clone();
        for (String mo : newKeywords) {
            if (mo.toLowerCase().contains(keyword.toLowerCase()) && blackListKeyWords.contains(mo)) {
                blackListKeyWords.remove(mo);
                return true;
            }
        }

        saveBlackList();
        return false;


    }

    /**
     * removes movie from blacklist.
     * @param movie movie title
     * @return true if keyword successfully removed
     */
    public static boolean removeFromBlacklistMovieTitle(String movie)  {
        if (movie.trim().equals("")) {
            return false;
        }

        boolean statTitle = removeMovieTitle(movie);
        boolean statObj = removeMovieObj(movie);
        saveBlackList();
        return statObj || statTitle;

    }

    /**
     * removes movie from blacklist.
     * @param movie movie object
     * @return true if keyword successfully removed
     */
    public static boolean removeFromBlacklistMovies(MovieInfoObject movie)  {
        if (movie == null) {
            return false;
        }

        boolean statTitle = removeMovieTitle(movie.getTitle());
        boolean statObj = removeMovieObjById(movie);
        saveBlackList();
        return statObj || statTitle;

    }

    /**
     * removes movie title from blacklistMovieTitle Arraylist.
     * @param movie movie title
     * @return true if keyword successfully removed
     */
    private static boolean removeMovieTitle(String movie)  {

        if (blackListMoviesTitle.contains(movie.toLowerCase())) {
            blackListMoviesTitle.remove(movie.toLowerCase());
            return true;
        } else {
            return false;
        }
    }

    /**
     * removes movie title from blacklistMovies Arraylist.
     * @param movie movie title
     * @return true if keyword successfully removed
     */
    private static boolean removeMovieObj(String movie)  {

        for (MovieModel mo : blackListMovies) {
            if (mo.getTitle().toLowerCase().equals(movie.toLowerCase())) {
                blackListMovies.remove(mo);
                return true;
            }
        }
        return false;
    }

    /**
     * removes movie title from blacklistMovies Arraylist.
     * @param movie movie object
     * @return true if keyword successfully removed
     */
    private static boolean removeMovieObjById(MovieInfoObject movie)  {

        for (MovieModel mo : blackListMovies) {
            if (mo.getId() == movie.getId()) {
                blackListMovies.remove(mo);
                return true;
            }
        }
        return false;
    }


    /**
     * Clears blacklist.
     */
    public static void clearBlacklist() {
        blackListMoviesTitle.clear();
        blackListMovies.clear();
        blackListKeyWords.clear();
        saveBlackList();
    }

    /**
     * Function to print blacklist on screen.
     * @return String crafted from blacklisted words
     */
    public static String printList() {
        String feedback = "Blacklisted Keywords: \n";
        int i  = 1;
        for (String e : blackListKeyWords) {
            feedback += String.valueOf(i);
            feedback += ") ";
            feedback += e;
            feedback += "\t\t";
            i++;
            if (i % 4 == 0) {
                feedback += "\n";
            }
        }

        feedback += "\nBlacklisted Movies: \n";
        i  = 1;
        for (String e : blackListMoviesTitle) {
            feedback += String.valueOf(i);
            feedback += ") ";
            feedback += e;
            feedback += "\t\t";
            i++;
            if (i % 4 == 0) {
                feedback += "\n";
            }
        }

        for (MovieModel e : blackListMovies) {
            feedback += String.valueOf(i);
            feedback += ") ";
            feedback += e.getTitle();
            feedback += "\t\t";
            i++;
            if (i % 4 == 0) {
                feedback += "\n";
            }
        }

        return feedback;
    }



    /**
     * gets possible predictions from list of blacklisted items.
     *
     * @param keyword the keyword used for predictions.
     */
    public static ArrayList<String> getBlackListHints(String keyword) {
        keyword = keyword.toLowerCase();
        ArrayList<String> hints = new ArrayList<>();
        for (String a: blackListKeyWords) {
            if (a.toLowerCase().startsWith(keyword)) {
                hints.add(a);
            }
        }

        for (String a: blackListMoviesTitle) {
            if (a.toLowerCase().startsWith(keyword)) {
                hints.add(a);
            }
        }

        for (MovieModel a: blackListMovies) {
            if (a.getTitle().toLowerCase().startsWith(keyword)) {
                hints.add(a.getTitle());
            }
        }

        return hints;
    }


    /**
     * Filters search results to exclude blacklisted items.
     * @return filtered search results.
     */
    public static ArrayList<MovieInfoObject> filter(ArrayList<MovieInfoObject> miMovies) {
        miMovies = filterByKeyword(miMovies);
        miMovies = filterById(miMovies);
        miMovies = filterByTitle(miMovies);

        return miMovies;
    }

    /**
     * Filter search results by blacklisted keywords.
     * @return filtered search results.
     */
    private static ArrayList<MovieInfoObject> filterByKeyword(ArrayList<MovieInfoObject> movies) {
        ArrayList<MovieInfoObject> filteredMovies = new ArrayList<>();
        for (MovieInfoObject o : movies) {
            boolean isBlacklisted = false;
            for (String e : blackListKeyWords) {
                if (o.getTitle().toLowerCase().contains(e.toLowerCase())) {
                    isBlacklisted = true;
                    break;
                }
            }
            if (!isBlacklisted) {
                filteredMovies.add(o);
            }
        }
        return filteredMovies;
    }

    /**
     * Filter search results by blacklisted movie Ids.
     * @return filtered search results.
     */
    private static ArrayList<MovieInfoObject> filterById(ArrayList<MovieInfoObject> movies) {
        ArrayList<MovieInfoObject> filteredMovies = new ArrayList<>();
        for (MovieInfoObject o : movies) {
            boolean isBlacklisted = false;
            for (MovieModel e : blackListMovies) {
                if (o.getId() == e.getId()) {
                    isBlacklisted = true;
                }
            }
            if (!isBlacklisted) {
                filteredMovies.add(o);
            }
        }


        return filteredMovies;
    }

    /**
     * Filter search results by blacklisted movie Titles.
     * @return filtered search results.
     */
    private static ArrayList<MovieInfoObject> filterByTitle(ArrayList<MovieInfoObject> movies) {
        ArrayList<MovieInfoObject> filteredMovies = new ArrayList<>();
        for (MovieInfoObject o : movies) {
            boolean isBlacklisted = false;
            for (String e : blackListMoviesTitle) {
                if (o.getTitle().toLowerCase().equals(e.toLowerCase())) {
                    isBlacklisted = true;
                }
            }
            if (!isBlacklisted) {
                filteredMovies.add(o);
            }
        }


        return filteredMovies;
    }


}

