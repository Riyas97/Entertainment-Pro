//@@author pdotdeep

package entertainment.pro.storage.user;

import entertainment.pro.commons.exceptions.DuplicateEntryException;
import entertainment.pro.model.MovieInfoObject;
import entertainment.pro.model.MovieModel;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class BlacklistTest {

    @Test
    public void addToBlacklistKeyWord_validInputs_success() throws DuplicateEntryException {


        Blacklist.addToBlacklistKeyWord("joker");
        Blacklist.addToBlacklistKeyWord("washington DC");
        Blacklist.addToBlacklistKeyWord("harry");

        assertEquals(true, Blacklist.getBlackListKeyWords().contains("joker"));
        assertEquals(true, Blacklist.getBlackListKeyWords().contains("washington dc"));
        assertEquals(true, Blacklist.getBlackListKeyWords().contains("harry"));

    }

    @Test
    public void addToBlacklistKeyWord_invalidInputs_throws_DuplicateEntryException() {

        assertThrows(DuplicateEntryException.class, () -> {
            Blacklist.addToBlacklistKeyWord("batman");
            Blacklist.addToBlacklistKeyWord("batman");
        });

        assertThrows(DuplicateEntryException.class, () -> {
            Blacklist.addToBlacklistKeyWord("joker");
            Blacklist.addToBlacklistKeyWord("joker");
        });

    }

    @Test
    public void addToBlacklistKeyWord_invalidInputs() throws DuplicateEntryException {

        Blacklist.addToBlacklistKeyWord(" ");
        assertEmptyAll();
    }

    @Test
    public void addToBlacklistMoviesID_validInputs_success() throws DuplicateEntryException {

        Blacklist.addToBlacklistMoviesID(new MovieInfoObject(1234, "movie 1", true));
        Blacklist.addToBlacklistMoviesID(new MovieInfoObject(2345, "movie 2", true));
        Blacklist.addToBlacklistMoviesID(new MovieInfoObject(3456, "movie 3", true));


        assertEquals(true, blackListMoviesContains("movie 1"));
        assertEquals(true, blackListMoviesContains("movie 2"));
        assertEquals(true, blackListMoviesContains("movie 3"));

    }


    @Test
    public void addToBlacklistMoviesID_invalidInputs_failure() throws DuplicateEntryException {

        Blacklist.addToBlacklistMoviesID(null);
        assertEmptyAll();
        Blacklist.addToBlacklistMoviesID(new MovieInfoObject(-1, "", true));

        assertEmptyAll();


    }


    @Test
    public void addToBlacklistMoviesID_invalidInputs_throws_DuplicateEntryException() {

        assertThrows(DuplicateEntryException.class, () -> {
            Blacklist.addToBlacklistMoviesID(new MovieInfoObject(10, "test", true));
            Blacklist.addToBlacklistMoviesID(new MovieInfoObject(10, "test", true));
        });

        assertEquals(1, Blacklist.getBlackListMovies().size());


    }

    /**
     * function checks if key exists in blacklist movies arraylist.
     * @param key key to be checked
     * @return true if key exists in blacklist movies arraylist
     */
    public boolean blackListMoviesContains(String key) {
        for (MovieModel m : Blacklist.getBlackListMovies()) {
            if (m.getTitle().toLowerCase().equals(key)) {
                return true;
            }
        }

        return false;
    }

    @Test
    public void addToBlacklistMovie_validInputs_success() throws DuplicateEntryException {

        Blacklist.addToBlacklistMovie("joker");
        Blacklist.addToBlacklistMovie("washington DC");
        Blacklist.addToBlacklistMovie("harry");

        assertEquals(true, Blacklist.getBlackListMoviesTitle().contains("joker"));
        assertEquals(true, Blacklist.getBlackListMoviesTitle().contains("washington dc"));
        assertEquals(true, Blacklist.getBlackListMoviesTitle().contains("harry"));

    }

    @Test
    public void addToBlacklistMovie_invalidInputs_failure() throws DuplicateEntryException {

        Blacklist.addToBlacklistMovie(" ");
        assertEmptyAll();

    }

    @Test
    public void addToBlacklistMovie_invalidInputs_failure_throws_DuplicateEntryException() {

        assertThrows(DuplicateEntryException.class, () -> {
            Blacklist.addToBlacklistMovie("joker");
            Blacklist.addToBlacklistMovie("joker");
            Blacklist.addToBlacklistMovie("joker");

            assertEquals(1, Blacklist.getBlackListMovies().size());
        });



    }

    /**
     * function assert that all 3 arraylists in blacklist are empty.
     */
    public void assertEmptyAll() {
        assertEquals(0, Blacklist.getBlackListKeyWords().size());
        assertEquals(0, Blacklist.getBlackListMovies().size());
        assertEquals(0, Blacklist.getBlackListMoviesTitle().size());
    }

    @Test
    public void removeFromBlacklistKeyWord_validInputs_success() throws DuplicateEntryException {

        Blacklist.addToBlacklistKeyWord("joker");
        Blacklist.addToBlacklistKeyWord("washington DC");
        Blacklist.addToBlacklistKeyWord("harry");

        assertEquals(true, Blacklist.getBlackListKeyWords().contains("joker"));
        assertEquals(true, Blacklist.getBlackListKeyWords().contains("washington dc"));
        assertEquals(true, Blacklist.getBlackListKeyWords().contains("harry"));

        Blacklist.removeFromBlacklistKeyWord("joker");
        assertEquals(false, Blacklist.getBlackListKeyWords().contains("joker"));
        Blacklist.removeFromBlacklistKeyWord("washington DC");
        assertEquals(false, Blacklist.getBlackListKeyWords().contains("washington dc"));
        Blacklist.removeFromBlacklistKeyWord("HArry");
        assertEquals(false, Blacklist.getBlackListKeyWords().contains("harry"));

    }

    @Test
    public void removeFromBlacklistKeyWord_invalidInputs_failure() {
        assertEquals(false, Blacklist.removeFromBlacklistKeyWord("joker"));
        assertEquals(false, Blacklist.removeFromBlacklistKeyWord(""));
    }

    @Test
    public void removeFromBlacklistMovieTitle_validInputs_success() throws DuplicateEntryException {

        Blacklist.addToBlacklistMovie("joker");
        Blacklist.addToBlacklistMovie("washington DC");
        Blacklist.addToBlacklistMoviesID(new MovieInfoObject(1234, "movie 1", true));
        Blacklist.addToBlacklistMoviesID(new MovieInfoObject(2345, "movie 2", true));


        System.out.println(Blacklist.printList());

        Blacklist.removeFromBlacklistMovieTitle("movie 1");
        Blacklist.removeFromBlacklistMovieTitle("joker");
        Blacklist.removeFromBlacklistMovieTitle("washington dc");
        Blacklist.removeFromBlacklistMovieTitle("movie 2");
        assertEquals(false, Blacklist.getBlackListMoviesTitle().contains("joker"));
        assertEquals(false, Blacklist.getBlackListMoviesTitle().contains("washington dc"));
        assertEquals(false, blackListMoviesContains("movie 1"));
        assertEquals(false, blackListMoviesContains("movie 2"));


        System.out.println(Blacklist.printList());


    }

    @Test
    public void removeFromBlacklistMovieTitle_invalidInputs_failure() {
        assertEquals(false, Blacklist.removeFromBlacklistMovieTitle("joker"));
        assertEquals(false, Blacklist.removeFromBlacklistMovieTitle(""));
    }

    @Test
    public void removeFromBlacklistMovies_validInputs_success() throws DuplicateEntryException {

        Blacklist.addToBlacklistMovie("joker");
        Blacklist.addToBlacklistMovie("washington DC");
        Blacklist.addToBlacklistMoviesID(new MovieInfoObject(1234, "movie 1", true));
        Blacklist.addToBlacklistMoviesID(new MovieInfoObject(2345, "movie 2", true));

        System.out.println(Blacklist.printList());

        Blacklist.removeFromBlacklistMovies(new MovieInfoObject(1234, "movie 1", true));
        Blacklist.removeFromBlacklistMovies(new MovieInfoObject(2345, "movie 2", true));
        Blacklist.removeFromBlacklistMovies(new MovieInfoObject(9999, "joker", true));
        Blacklist.removeFromBlacklistMovies(new MovieInfoObject(6666, "washington DC", true));
        System.out.println(Blacklist.printList());

        assertEquals(false, Blacklist.getBlackListMoviesTitle().contains("joker"));
        assertEquals(false, Blacklist.getBlackListMoviesTitle().contains("washington dc"));
        assertEquals(false, blackListMoviesContains("movie 1"));
        assertEquals(false, blackListMoviesContains("movie 2"));




    }

    @Test
    public void removeFromBlacklistMovies_invalidInputs_failure() {
        assertEquals(false, Blacklist.removeFromBlacklistMovies(new MovieInfoObject(10,
                "test", true)));
        assertEquals(false, Blacklist.removeFromBlacklistMovies(new MovieInfoObject(-1,
                "", true)));
    }

    @Test
    public void getBlackListHints_validInputs_success() throws DuplicateEntryException {

        Blacklist.addToBlacklistKeyWord("batman");
        Blacklist.addToBlacklistKeyWord("harry potter");
        Blacklist.addToBlacklistMovie("joker");
        Blacklist.addToBlacklistMovie("washington DC");
        Blacklist.addToBlacklistMoviesID(new MovieInfoObject(1234, "movie 1", true));
        Blacklist.addToBlacklistMoviesID(new MovieInfoObject(2345, "movie 2", true));

        assertEquals("joker", Blacklist.getBlackListHints("jok").get(0));
        assertEquals("harry potter", Blacklist.getBlackListHints("Har").get(0));
        assertEquals("movie 1", Blacklist.getBlackListHints("mov").get(0));
        assertEquals(2, Blacklist.getBlackListHints("mov").size());


    }

    @Test
    public void getBlackListHints_invalidInputs_failure() throws DuplicateEntryException {

        Blacklist.addToBlacklistKeyWord("batman");
        Blacklist.addToBlacklistKeyWord("harry potter");
        Blacklist.addToBlacklistMovie("joker");
        Blacklist.addToBlacklistMovie("washington DC");
        Blacklist.addToBlacklistMoviesID(new MovieInfoObject(1234, "movie 1", true));
        Blacklist.addToBlacklistMoviesID(new MovieInfoObject(2345, "movie 2", true));


        assertEquals(0, Blacklist.getBlackListHints("obama").size());
        assertEquals(6, Blacklist.getBlackListHints("").size());


    }


    @Test
    public void clearBlacklist_test() {
        Blacklist.clearBlacklist();
        assertEmptyAll();
    }


    @AfterEach
    public void clearBlackList() {
        Blacklist.clearBlacklist();
    }

}

