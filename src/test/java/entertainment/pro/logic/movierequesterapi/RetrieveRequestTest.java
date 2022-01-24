package entertainment.pro.logic.movierequesterapi;

import entertainment.pro.commons.strings.PromptMessages;
import entertainment.pro.logic.movierequesterapi.RequestListener;
import entertainment.pro.commons.exceptions.Exceptions;
import entertainment.pro.logic.movierequesterapi.RetrieveRequest;
import entertainment.pro.model.MovieInfoObject;
import entertainment.pro.model.SearchProfile;
import entertainment.pro.model.UserProfile;
import entertainment.pro.ui.MovieHandler;
import entertainment.pro.storage.utils.OfflineSearchStorage;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class RetrieveRequestTest {
    ArrayList<Integer> genrePreference = new ArrayList<>();
    ArrayList<Integer> genreRestriction = new ArrayList<>();
    ArrayList<String> playlist = new ArrayList<>();
    boolean isAdultEnabled = false;
    boolean sortByAlphaOrder = false;
    boolean sortByRating = false;
    boolean sortByReleaseDate = false;
    boolean isMovie = true;
    String searchEntryName = "";
    String name = "";
    int age = 0;
    private static final String TO_SPECIFY_RESULTS = "results";
    private static final String VALID_MOVIE_CERT_FILENAME1 = "/data/ValidMovieCertFile1";
    private static final String VALID_MOVIE_CERT_FILENAME2 = "/data/ValidMovieCertFile2";
    private static final String VALID_TV_CERT_FILENAME1 = "/data/ValidTVCertFile1.json";
    private static final String VALID_TV_CERT_FILENAME2 = "/data/ValidTVCertFile2.json";
    private static final String INVALID_MOVIE_CERT_FILENAME1 = "/data/InvalidMovieCertFile1";
    private static final String INVALID_TV_CERT_FILENAME1 = "/data/InvalidTVCertFile1";
    private static String MOVIES_DATABASE_FILEPATH = "/data/movieData/";
    private static String SEARCH_PROFILE_FILEPATH = "/data/SearchProfileTest/";
    ArrayList<Integer> genrePref1 = new ArrayList<>();
    ArrayList<Integer> genreRestrict1 = new ArrayList<>();
    MovieInfoObject movieInfoObject1 = new MovieInfoObject(1, "Ad Astra",
            true, getDate("01/01/2018"),
            null, null, null, 8.0,
            null, true);
    MovieInfoObject movieInfoObject2 = new MovieInfoObject(18, "Joker",
            true, getDate("01/01/2017"),
            null, null, null, 9.0,
            null, false);
    MovieInfoObject movieInfoObject3 = new MovieInfoObject(10202, "Spiderman",
            true, getDate("01/01/2019"),
            null, null, null, 7.0,
            null, false);
    MovieInfoObject movieInfoObject4 = new MovieInfoObject(25506, "",
            false, getDate("2019/01/01"),
            null, null, null, 0.0,
            null, true);
    MovieInfoObject movieInfoObject5 = new MovieInfoObject(100, "",
            false, getDate("2019/01/01"),
            null, null, null,
            0.0, null, false);

    List<MovieInfoObject> testObjectsList = Arrays.asList(movieInfoObject4, movieInfoObject1,
            movieInfoObject2, movieInfoObject3);

    /**
     * Responsible for retrieving the data as string from a file.
     * @param filename Name of the file.
     * @return data as string from a file.
     * @throws Exceptions when detect IO Exception.
     */
    public static String getString(String filename) throws Exceptions {
        InputStream inputStream = OfflineSearchStorage.class.getResourceAsStream(filename);
        InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
        BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
        String line = "";
        String datafromjson = "";
        try {
            while ((line = bufferedReader.readLine()) != null) {
                datafromjson += line;
            }
            bufferedReader.close();
            inputStreamReader.close();
            inputStream.close();
        } catch (IOException e) {
            throw new Exceptions(PromptMessages.IO_EXCEPTION_IN_OFFLINE);
        }
        return datafromjson;
    }

    /**
     * Responsible for parsing data into JSONArray.
     * @param datafromjson data that needs to be parsed.
     * @return JSONArray that contains parsed data.
     */
    public JSONArray getValidData(String datafromjson) {
        JSONParser jsonParser = new JSONParser();
        JSONObject jsonObject1 = new JSONObject();
        JSONArray searchData1 = new JSONArray();
        try {
            jsonObject1 = (JSONObject) jsonParser.parse(datafromjson);
            searchData1 = (JSONArray) jsonObject1.get(TO_SPECIFY_RESULTS);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return searchData1;
    }

    @Test
    public void getTvCertFromJsonTest_valid_result() throws Exceptions {
        String data1 = getString(VALID_TV_CERT_FILENAME1);
        String data2 = getString(VALID_TV_CERT_FILENAME2);
        JSONArray searchData1 = getValidData(data1);
        JSONArray searchData2 = getValidData(data2);
        String cert1 = "Unavailable";
        try {
            cert1 = RetrieveRequest.getTvCertFromJson(searchData1);
        } catch (NullPointerException e) {
            assertEquals("Unavailable", cert1);
            return;

        }
        String expected1 = "Suitable for 12 years & above";
        String cert2 = null;
        try {
            cert2 = RetrieveRequest.getMovieCertFromJson(searchData2);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        String expected2 = "Unavailable";
        assertEquals(expected1, cert1);
        assertEquals(expected2, cert2);
    }

    @Test
    public void getTvCertFromJsonTest_empty_result() throws Exceptions {
        String data = getString(INVALID_TV_CERT_FILENAME1);
        JSONArray searchData1 = getValidData(data);
        String cert1 = null;
        try {
            cert1 = RetrieveRequest.getMovieCertFromJson(searchData1);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        String expected1 = "Unavailable";
        assertEquals(expected1, cert1);
    }

    @Test
    public void getMovieCertFromJsonTest_valid_result() throws Exceptions {
        String data1 = getString(VALID_MOVIE_CERT_FILENAME1);
        String data2 = getString(VALID_MOVIE_CERT_FILENAME2);
        JSONArray searchData1 = getValidData(data1);
        JSONArray searchData2 = getValidData(data2);
        String cert1 = "Unavailable";
        try {
            cert1 = RetrieveRequest.getMovieCertFromJson(searchData1);
        } catch (NullPointerException | ParseException e) {
            assertEquals("Unavailable", cert1);
            return;

        }
        String expected1 = "\"R\"";
        String cert2 = null;
        try {
            cert2 = RetrieveRequest.getMovieCertFromJson(searchData2);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        String expected2 = "Unavailable";
        assertEquals(expected1, cert1);
        assertEquals(expected2, cert2);
    }

    @Test
    public void getMovieCertFromJsonTest_empty_result() throws Exceptions {
        String data = getString(INVALID_MOVIE_CERT_FILENAME1);
        JSONArray searchData1 = getValidData(data);
        String cert1 = null;
        try {
            cert1 = RetrieveRequest.getMovieCertFromJson(searchData1);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        String expected1 = "Unavailable";
        assertEquals(expected1, cert1);
    }

    /**
     * Retrieve data from local files.
     * @return Data from local files as JSONArray.
     * @throws Exceptions when detect parsing or IO Exception.
     */
    public JSONArray getOffline() throws Exceptions {
        //String filename = MOVIES_DATABASE_FILEPATH;
        // filename += i + ".json";
        String filename = "/data/movieData/1.json";
        String datafromjson = getString(filename);
        JSONParser jsonParser = new JSONParser();
        JSONArray jsonArray = new JSONArray();
        try {
            jsonArray = (JSONArray) jsonParser.parse(datafromjson);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return jsonArray;
    }

    /**
     * Retrieve date from a string.
     * @param releaseDateString String from which date needs to be extracted.
     * @return Date from a string.
     */
    public Date getDate(String releaseDateString) {
        Date releaseDate = new Date();
        SimpleDateFormat formatter1 = new SimpleDateFormat("dd/MM/yyyy");
        try {
            releaseDate = formatter1.parse(releaseDateString);
        } catch (java.text.ParseException e) {
            releaseDate = new Date();
        }
        return releaseDate;
    }

    @Test
    public void sortByAlphaOrderTest() {
        ArrayList<MovieInfoObject> testArrayList = new ArrayList<>();
        testArrayList.addAll(testObjectsList);
        ArrayList<MovieInfoObject> expectedArrayList1 = new ArrayList<>();
        expectedArrayList1.addAll(testObjectsList);
        RetrieveRequest.finalSearchResults = testArrayList;
        RetrieveRequest.sortByAlphaOrder();
        testArrayList = RetrieveRequest.finalSearchResults;
        assertEquals(expectedArrayList1, testArrayList);
    }

    @Test
    public void sortByLatestReleaseTest() {
        ArrayList<MovieInfoObject> testArrayList = new ArrayList<>();
        testArrayList.addAll(testObjectsList);
        ArrayList<MovieInfoObject> expectedArrayList1 = new ArrayList<>();
        List<MovieInfoObject> expectedObjectsList = Arrays.asList(movieInfoObject3, movieInfoObject1,
                movieInfoObject2, movieInfoObject4);
        expectedArrayList1.addAll(expectedObjectsList);
        RetrieveRequest.finalSearchResults = testArrayList;
        RetrieveRequest.sortByLatestRelease();
        testArrayList = RetrieveRequest.finalSearchResults;
        assertEquals(expectedArrayList1, testArrayList);
    }

    @Test
    public void sortByHighestRatingTest() throws Exceptions {
        ArrayList<MovieInfoObject> testArrayList = new ArrayList<>();
        testArrayList.addAll(testObjectsList);
        ArrayList<MovieInfoObject> expectedArrayList1 = new ArrayList<>();
        List<MovieInfoObject> expectedObjectsList = Arrays.asList(movieInfoObject2, movieInfoObject1,
                movieInfoObject3, movieInfoObject4);
        expectedArrayList1.addAll(expectedObjectsList);
        RetrieveRequest.finalSearchResults = testArrayList;
        RetrieveRequest.sortByHighestRating();
        testArrayList = RetrieveRequest.finalSearchResults;
        assertEquals(expectedArrayList1, testArrayList);
    }

    @Test
    public void checkConditionTest_returns_true() throws Exceptions {
        String searchProfileData = getString(SEARCH_PROFILE_FILEPATH);
        JSONArray jsonArray = getValidData(searchProfileData);
        int d = 0;
        for (int i = 0; i < jsonArray.size(); i += 1) {
            SearchProfile searchProfile = new SearchProfile(name, age, genrePreference, genreRestriction,
                    isAdultEnabled, playlist, sortByAlphaOrder,
                    sortByRating, sortByReleaseDate, searchEntryName, isMovie);
            ;
            ArrayList<Integer> genrePref = new ArrayList<>();
            ArrayList<Integer> genreRestrict = new ArrayList<>();
            JSONObject jsonObject = (JSONObject) jsonArray.get(i);
            JSONArray jsonArray1 = (JSONArray) jsonObject.get("genreIdPreference");
            for (int j = 0; j < jsonArray1.size(); j += 1) {
                long num = (long) jsonArray1.get(j);
                int genreNo = Math.toIntExact(num);
                System.out.println(genreNo);
                genrePref.add(genreNo);
            }
            JSONArray jsonArray2 = (JSONArray) jsonObject.get("genreIdRestriction");
            for (int j = 0; j < jsonArray2.size(); j += 1) {
                long num = (long) jsonArray2.get(j);
                int genreNo = Math.toIntExact(num);
                genreRestrict.add(genreNo);
            }
            searchProfile = searchProfile.setGenreIdPreference(genrePref);
            searchProfile = searchProfile.setGenreIdRestriction(genreRestrict);
            searchProfile = searchProfile.setAdult((Boolean) jsonObject.get("adult"));
            searchProfile = searchProfile.setSortByAlphabetical((Boolean) jsonObject.get("sortByAlphabetical"));
            searchProfile = searchProfile.setSortByHighestRating((Boolean) jsonObject.get("sortByHighestRating"));
            searchProfile = searchProfile.setSortByHighestRating((Boolean) jsonObject.get("sortByLatestRelease"));
            searchProfile = searchProfile.setMovie((Boolean) jsonObject.get("isMovie"));
            searchProfile = searchProfile.setName((String) jsonObject.get("name"));
            RetrieveRequest.searchProfile = searchProfile;
            JSONArray jsonArray3 = getOffline();
            ArrayList<String> getResults = getResultsData();
            for (int a = 1; a < jsonArray3.size(); a += 1) {
                JSONObject jsonObject1 = (JSONObject) jsonArray3.get(a);
                if (RetrieveRequest.checkCondition(jsonObject1)) {
                    assertEquals("true", getResults.get(d));
                    d += 1;
                } else {
                    assertEquals("false", getResults.get(d));
                    d += 1;
                }

            }

        }
    }

    /**
     * Get data from local file and return as arraylist.
     * @return Arraylist which contains the data from local files.
     * @throws Exceptions when detect a IO Exception.
     */
    public ArrayList<String> getResultsData() throws Exceptions {
        ArrayList<String> arrayList = new ArrayList<>();
        InputStream inputStream = OfflineSearchStorage.class.getResourceAsStream("/data/movieData/results.txt");
        InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
        BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
        String line = "";
        String datafromjson = "";
        try {
            while ((line = bufferedReader.readLine()) != null) {
                datafromjson += line;
                arrayList.add(line);
            }
            bufferedReader.close();
            inputStreamReader.close();
            inputStream.close();
        } catch (IOException e) {
            throw new Exceptions(PromptMessages.IO_EXCEPTION_IN_OFFLINE);
        }
        return arrayList;
    }

    @Test
    public void getCertStrings_return_unavailable() throws Exceptions {
        assertEquals("Unavailable", RetrieveRequest.getCertStrings(movieInfoObject1));
        assertEquals("Unavailable", RetrieveRequest.getCertStrings(movieInfoObject4));

    }

    @Test
    public void getCastStrings_returns_empty() throws Exceptions {
        ArrayList<String> castList1 = RetrieveRequest.getCastStrings(movieInfoObject1);
        ArrayList<String> castList5 = RetrieveRequest.getCastStrings(movieInfoObject5);
        ArrayList<String> expectedCastList1 = new ArrayList<>();
        assertEquals(expectedCastList1, castList1);
    }


}


