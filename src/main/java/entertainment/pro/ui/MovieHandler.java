package entertainment.pro.ui;

import entertainment.pro.commons.strings.PromptMessages;
import entertainment.pro.commons.exceptions.Exceptions;
import entertainment.pro.commons.exceptions.EmptyCommandException;
import entertainment.pro.commons.exceptions.MissingInfoException;
import entertainment.pro.commons.exceptions.InvalidFormatCommandException;
import entertainment.pro.logic.cinemarequesterapi.CinemaRetrieveRequest;
import entertainment.pro.logic.contexts.CommandContext;
import entertainment.pro.logic.contexts.ContextHelper;
import entertainment.pro.logic.contexts.SearchResultContext;
import entertainment.pro.logic.execution.CommandStack;
import entertainment.pro.logic.movierequesterapi.RequestListener;
import entertainment.pro.logic.movierequesterapi.RetrieveRequest;
import entertainment.pro.model.MovieInfoObject;
import entertainment.pro.model.PageTracker;
import entertainment.pro.model.UserProfile;
import entertainment.pro.model.SearchProfile;
import entertainment.pro.model.Playlist;
import entertainment.pro.model.PlaylistMovieInfoObject;
import entertainment.pro.storage.user.Blacklist;
import entertainment.pro.storage.user.ProfileCommands;
import entertainment.pro.storage.utils.EditProfileJson;
import entertainment.pro.storage.utils.EditPlaylistJson;
import entertainment.pro.storage.utils.BlacklistStorage;
import entertainment.pro.storage.utils.HelpStorage;

import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Paint;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import entertainment.pro.logic.parsers.CommandParser;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This is main page of GUI.
 */
public class MovieHandler extends Controller implements RequestListener {

    @FXML
    public ScrollPane moviesScrollPane;

    @FXML
    Label userAdultLabel2;
    @FXML
    Label sortAlphaOrderLabel;
    @FXML
    Label sortLatestDateLabel;
    @FXML
    Label sortHighestRatingLabel;
    @FXML
    Label userNameLabel;
    @FXML
    Label userAgeLabel;
    @FXML
    private Label statusLabel;
    @FXML
    private Label userPlaylistsLabel;

    @FXML
    public Text autoCompleteText = new Text();
    @FXML
    public Text generalFeedbackText = new Text();

    @FXML
    private TextFlow genreListText;

    @FXML
    public TextField searchTextField;

    @FXML
    private ProgressBar progressBar;

    private static final Logger logger = Logger.getLogger(MovieHandler.class.getName());
    private boolean isViewMoreInfoPage = false;
    UserProfile userProfile;
    private ArrayList<String> playlists;
    private String playlistName = "";
    private PageTracker pageTracker = new PageTracker();
    public FlowPane moviesFlowPane = new FlowPane();
    private VBox playlistVBox = new VBox();
    public static ArrayList<MovieInfoObject> mMovies = new ArrayList<>();
    private double[] imagesLoadingProgress;
    public static RetrieveRequest mMovieRequest;
    private static CinemaRetrieveRequest mCinemaRequest;
    private int index = 0;
    static String command = "";
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

    private static final String UNAVAILABLE_INFO = "Unavailable";
    private static final String DECIMAL_FORMAT = "%.2f";

    /**
     * Initializes the Search Profile.
     */
    public SearchProfile searchProfile = new SearchProfile(name, age, genrePreference, genreRestriction, isAdultEnabled,
            playlist, sortByAlphaOrder, sortByRating, sortByReleaseDate, searchEntryName, isMovie);

    /**
     * Responsible for retrieving the Search Profile.
     * @return Search Profile
     */
    public SearchProfile getSearchProfile() {
        return searchProfile;
    }

    /**
     * Responsible for returning the boolean status of isViewMoreInfo.
     * @return The boolean status of isViewMoreInfo.
     */
    public boolean isViewMoreInfoPage() {
        return isViewMoreInfoPage;
    }

    /**
     * This function is called when JavaFx runtime when view is loaded.
     * Responsible for setting the components in the UI.
     */
    @FXML
    public void setLabels() throws IOException {
        logger.log(Level.INFO, PromptMessages.SETTING_LABELS_UI);
        EditProfileJson editProfileJson = new EditProfileJson();
        userProfile = editProfileJson.load();
        userNameLabel.setText(userProfile.getUserName());
        userAgeLabel.setText(Integer.toString(userProfile.getUserAge()));
        playlists = userProfile.getPlaylistNames();
        ProfileCommands command = new ProfileCommands(userProfile);
        userPlaylistsLabel.setText(Integer.toString(userProfile.getPlaylistNames().size()));

        //setting adult label
        if (command.getAdultLabel().equals("allow")) {
            userAdultLabel2.setStyle("-fx-text-fill: \"#48C9B0\";");
        }
        if (command.getAdultLabel().equals("restrict")) {
            userAdultLabel2.setStyle("-fx-text-fill: \"#EC7063\";");
        }
        userAdultLabel2.setText(command.getAdultLabel());

        //setting text for preference & restrictions
        Text preferences = new Text(command.convertToLabel(userProfile.getGenreIdPreference()));
        preferences.setFill(Paint.valueOf("#48C9B0"));
        Text restrictions = new Text(command.convertToLabel(userProfile.getGenreIdRestriction()));
        restrictions.setFill(Paint.valueOf("#EC7063"));
        genreListText.getChildren().clear();
        genreListText.getChildren().addAll(preferences, restrictions);
        genreListText.setLineSpacing(4);
        updateSortInterface(userProfile);
    }


    /**
     * Responsible for calling JavaFX runtime when view is loaded.
     */
    @FXML
    public void initialize() throws IOException, Exceptions {
        setLabels();
        mMovieRequest = new RetrieveRequest(this);
        mMovieRequest.setSearchProfile(searchProfile);
        mCinemaRequest = new CinemaRetrieveRequest(this);
        logger.setLevel(Level.ALL);
        logger.log(Level.INFO, "MAIN UI INITIALISED");
        CommandContext.initialiseContext();
        BlacklistStorage bp = new BlacklistStorage();
        bp.load();
        HelpStorage.initialiseAllHelp();
        setUpEventSearchField();
        mMovieRequest.beginSearchRequest(RetrieveRequest.MoviesRequestType.CURRENT_MOVIES);
        setEventsScrollPane();
    }

    /**
     * Responsible for setting up the keypad events for search text field.
     */
    private void setUpEventSearchField() {
        searchTextField.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            if (event.getCode() == KeyCode.TAB) {
                setAutoCompleteText(ContextHelper.getAllHints(searchTextField.getText(), this));
                event.consume();
            } else if (event.getCode().equals(KeyCode.ALT_GRAPH) || event.getCode().equals(KeyCode.ALT)) {
                searchTextField.clear();
                String cmd = CommandStack.nextCommand();
                if (cmd == null) {
                    setAutoCompleteText("You don't have any commands in history!");
                } else {
                    searchTextField.clear();
                    searchTextField.setText(cmd);
                }
                searchTextField.positionCaret(searchTextField.getText().length());
            } else if (event.getCode() == KeyCode.ENTER) {
                clearAutoCompleteFeedbackText();
                setGeneralFeedbackText(PromptMessages.WAIT_FOR_APP_TO_PROCESS);
                command = searchTextField.getText();
                try {
                    CommandParser.parseCommands(command, this);
                } catch (IOException | Exceptions e) {
                    logger.log(Level.SEVERE, "Exception in parsing command" + e);
                } catch (EmptyCommandException e) {
                    logger.log(Level.SEVERE, PromptMessages.MISSING_COMMAND + e);
                    setGeneralFeedbackText(PromptMessages.MISSING_COMMAND);
                } catch (MissingInfoException e) {
                    setGeneralFeedbackText(PromptMessages.MISSING_ARGUMENTS);
                }
                clearSearchTextField();
            } else if (event.getCode().equals(KeyCode.DOWN)) {
                moviesScrollPane.requestFocus();
                moviesFlowPane.getChildren().get(0).setStyle("-fx-border-color: white");
            }
        });
        //Real time changes to text field
        searchTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            System.out.println("textfield changed from " + oldValue + " to " + newValue);
        });

        System.out.println(generalFeedbackText.getText());
    }

    /**
     * Responsible for setting up keypad events for scrollpane.
     */
    public void setEventsScrollPane() {
        moviesScrollPane.setOnKeyPressed(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent event) {
                if (event.getCode().equals(KeyCode.UP)) {
                    double num = (double) moviesScrollPane.getVvalue();
                    num *= 10;
                    if (num == 0) {
                        searchTextField.requestFocus();
                    }
                    moviesFlowPane.getChildren().get(index).setStyle("-fx-border-color: black");
                    index = 0;
                } else if (event.getCode().equals(KeyCode.RIGHT)) {
                    if (pageTracker.getCurrentPage().equals("playlistList")) {
                        int size = playlistVBox.getChildren().size();
                        if ((index + 1) != size) {
                            playlistVBox.getChildren().get(index).requestFocus();
                            index += 1;
                            if (index != 0) {
                                playlistVBox.getChildren().get(index - 1).setStyle("-fx-border-color: black");
                            }
                            playlistVBox.getChildren().get(index).setStyle("-fx-border-color: white");
                            if (index % 3 == 0) {
                                moviesScrollPane.setVvalue((double) (index + 1) / size);
                            }
                        }
                    } else {
                        int size = moviesFlowPane.getChildren().size();
                        if ((index + 1) != size) {
                            moviesFlowPane.getChildren().get(index).requestFocus();
                            index += 1;
                            if (index != 0) {
                                moviesFlowPane.getChildren().get(index - 1).setStyle("-fx-border-color: black");
                            }
                            moviesFlowPane.getChildren().get(index).setStyle("-fx-border-color: white");
                            if (index % 4 == 0) {
                                moviesScrollPane.setVvalue((double) (index + 1) / size);
                            }
                        }
                    }
                } else if (event.getCode().equals(KeyCode.LEFT)) {
                    if (pageTracker.getCurrentPage().equals("playlistList")) {
                        if (index != 0) {
                            playlistVBox.getChildren().get(index - 1).requestFocus();
                            index -= 1;
                            playlistVBox.getChildren().get(index + 1).setStyle("-fx-border-color: black");
                            playlistVBox.getChildren().get(index).setStyle("-fx-border-color: white");
                            double size = playlistVBox.getChildren().size();
                            if (index % 3 == 0) {
                                moviesScrollPane.setVvalue((index + 1) / size);
                            }
                        } else {
                            searchTextField.requestFocus();
                            playlistVBox.getChildren().get(index).setStyle("-fx-border-color: black");
                        }
                    } else {
                        if (index != 0) {
                            moviesFlowPane.getChildren().get(index - 1).requestFocus();
                            index -= 1;
                            moviesFlowPane.getChildren().get(index + 1).setStyle("-fx-border-color: black");
                            moviesFlowPane.getChildren().get(index).setStyle("-fx-border-color: white");
                            double size = moviesFlowPane.getChildren().size();
                            if (index % 4 == 0) {
                                moviesScrollPane.setVvalue((index + 1) / size);
                            }
                        } else {
                            searchTextField.requestFocus();
                            moviesFlowPane.getChildren().get(index).setStyle("-fx-border-color: black");
                        }
                    }
                } else if (event.getCode().equals(KeyCode.ENTER)) {
                    try {
                        switch (pageTracker.getCurrentPage()) {
                            case "mainPage":
                                showMovie(index);
                                break;
                            case "playlistInfo":
                                Playlist playlist1 = new EditPlaylistJson(playlistName).load();
                                playlistMoviePosterClicked(playlist1.getMovies().get(index));
                                break;
                            case "playlistList":
                                Playlist playlist2 = new EditPlaylistJson(playlists.get(index)).load();
                                playlistPaneClicked(playlist2);
                                break;
                            default:
                                break;
                        }


                    } catch (Exceptions | IOException exceptions) {
                        exceptions.printStackTrace();
                    }
                    index = 0;
                }
            }
        });
    }

    /**
     * This function is called to print message in UI when data for the movies/tv shows has been fetched from the API.
     * @param message String to be printed.
     */
    @Override
    public void requestCompleted(String message) {
        setGeneralFeedbackText(message);
    }

    /**
     * This function is called to print message in UI when data for the
     * movies/tv shows has been fetched from the local files.
     * @param message String to be printed.
     */
    @Override
    public void requestTimedOut(String message) {
        setGeneralFeedbackText(message);
    }

    /**
     * This function is called to print message in UI when no results is found.
     */
    @Override
    public void emptyResults() {
        setGeneralFeedbackText(PromptMessages.NO_RESULTS_FOUND);
    }

    /**
     * Responsible for filtering the search results to remove blacklist items.
     * @param moviesInfo ArrayList containing all the seacrh results.
     */
    public void obtainedResultsData(ArrayList<MovieInfoObject> moviesInfo) {
        isViewMoreInfoPage = false;
        logger.log(Level.INFO, PromptMessages.REMOVE_BLACLISTED_ITEMS_FROM_SEARCH);
        ArrayList<MovieInfoObject> filteredMovies = Blacklist.filter(moviesInfo);
        final ArrayList<MovieInfoObject> MoviesFinal = filteredMovies;
        mMovies.clear();
        System.out.println("cleared");
        SearchResultContext.addResults(MoviesFinal);
        mMovies = MoviesFinal;
        try {
            displayItems();
        } catch (Exceptions exceptions) {
            exceptions.printStackTrace();
        }
    }

    /**
     * Responsible for displaying the search results in the UI.
     * @throws Exceptions when there is nothing to show.
     */
    public void displayItems() throws Exceptions {
        if (mMovies.size() == 0) {
            setGeneralFeedbackText(PromptMessages.VIEW_BACK_FAILURE);
            throw new Exceptions(PromptMessages.VIEW_BACK_FAILURE);
        }
        if (isViewMoreInfoPage) {
            setGeneralFeedbackText(PromptMessages.VIEW_BACK_SUCCESS);
            isViewMoreInfoPage = false;
        }
        imagesLoadingProgress = new double[mMovies.size()];
        Platform.runLater(() -> {
            try {
                buildMoviesFlowPane(mMovies);
                pageTracker.setToMainPage();
            } catch (Exceptions exceptions) {
                exceptions.printStackTrace();
            }
        });
    }


    /**
     * This function initalizes the progress bar and extracts movie posters fro every movie.
     *
     * @param movies is a array containing details about every movie/tv show that is being displayed.
     */
    private void buildMoviesFlowPane(ArrayList<MovieInfoObject> movies) throws Exceptions {
        // Setup progress bar and status label
        progressBar.setProgress(0.0);
        progressBar.setVisible(true);
        statusLabel.setText("Loading..");
        moviesFlowPane = new FlowPane(Orientation.HORIZONTAL);
        moviesFlowPane.setHgap(4);
        moviesFlowPane.setVgap(10);
        moviesFlowPane.setPadding(new Insets(10, 8, 4, 8));
        moviesFlowPane.prefWrapLengthProperty().bind(moviesScrollPane.widthProperty());   // bind to scroll pane width
        //mMoviesFlowPane.getChildren().add(generalFeedbackLabel);
        for (int i = 0; i < movies.size(); i++) {
            AnchorPane posterPane = buildMoviePosterPane(movies.get(i), i + 1);
            moviesFlowPane.getChildren().add(posterPane);
        }
        moviesScrollPane.setFitToWidth(true);
        moviesScrollPane.setContent(moviesFlowPane);
        moviesScrollPane.setVvalue(0);
    }


    /**
     * Reponsible for building the movie posters in the UI.
     * @param movie a object that contains information about a movie
     * @param index a unique number assigned to every movie/tv show that is being displayed.
     * @return Anchorpane consisting of the movie poster, name and the unique id.
     */
    private AnchorPane buildMoviePosterPane(MovieInfoObject movie, int index) throws Exceptions {
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getClassLoader().getResource("MoviePoster.fxml"));
            AnchorPane posterView = loader.load();
            //posterView.setOnScroll();
            posterView.setOnMouseClicked((mouseEvent) -> {
                try {
                    moviePosterClicked(movie);
                } catch (Exceptions exceptions) {
                    exceptions.printStackTrace();
                }
            });

            // set the movie info
            MoviePosterController controller = loader.getController();
            try {
                if (movie.getFullPosterPathInfo() != null) {
                    Image posterImage = new Image(movie.getFullPosterPathInfo(), true);
                    posterImage.progressProperty().addListener((observable, oldValue, newValue) -> {
                        try {
                            updateProgressBar(movie, newValue.doubleValue());
                        } catch (Exceptions exceptions) {
                            exceptions.printStackTrace();
                        }
                    });
                    controller.getPosterImageView().setImage(posterImage);

                } else {
                    File fakePoster = new File("./data/FakeMoviePoster.png");
                    Image posterImage = new Image(fakePoster.toURI().toString());
                    posterImage.progressProperty().addListener((observable, oldValue, newValue) -> {
                        try {
                            updateProgressBar(movie, newValue.doubleValue());
                        } catch (Exceptions exceptions) {
                            exceptions.printStackTrace();
                        }
                    });
                    controller.getPosterImageView().setImage(posterImage);
                }
            } catch (NullPointerException ex) {
                ex.printStackTrace();
            }
            controller.getMovieTitleLabel().setText(movie.getTitle());
            controller.getMovieNumberLabel().setText(Integer.toString(index));
            return posterView;
        } catch (IOException ex) {
            //Ui.printLine();
        }

        return null;
    }

    /**
     * This function updates the progress bar as the movie poster is being displayed.
     *
     * @param movie    Object that contains all the information about a particular movie.
     * @param progress contains the progress value.
     */
    private void updateProgressBar(MovieInfoObject movie, double progress) throws Exceptions {
        // update the progress for that movie in the array
        int index = mMovies.indexOf(movie);
        if (index >= 0) {
            imagesLoadingProgress[index] = progress;
        }

        double currentTotalProgress = 0.0;
        for (double value : imagesLoadingProgress) {
            currentTotalProgress += value;
        }

        progressBar.setProgress((currentTotalProgress / mMovies.size()));

        if (currentTotalProgress >= mMovies.size()) {
            progressBar.setVisible(false);
            statusLabel.setText("");
        }
    }


    /**
     * Responsible for displaying more information about a movie/TV show item.
     * @param num the movie/TV show item that user want to know mpre information about.
     * @throws Exceptions when user enter an invalid command.
     */
    public void showMovie(int num) throws Exceptions {
        try {
            MovieInfoObject movie = mMovies.get(num - 1);
            moviePosterClicked(movie);
        } catch (IndexOutOfBoundsException e) {
            logger.log(Level.WARNING, PromptMessages.INVALID_FORMAT);
            setGeneralFeedbackText(PromptMessages.INVALID_FORMAT);
            throw new InvalidFormatCommandException(PromptMessages.INVALID_FORMAT);
        }
    }


    private void buildPlaylistVBox(ArrayList<String> playlists) throws IOException {
        // Setup progress bar and status label
        progressBar.setProgress(0.0);
        progressBar.setVisible(true);
        statusLabel.setText("Loading..");
        playlistVBox.getChildren().clear();
        moviesScrollPane.setHvalue(0.5);
        moviesScrollPane.setVvalue(0.5);

        int count = 1;
        if (playlists.isEmpty()) {
            Label emptyLabel = new Label("u do not have any playlist currently :( "
                    + "\n try making some using command: playlist create <playlist name>");
            playlistVBox.getChildren().add(emptyLabel);
        } else {
            for (String log : playlists) {
                Playlist playlist = new EditPlaylistJson(log).load();
                AnchorPane playlistPane = buildPlaylistPane(playlist, count);
                playlistVBox.getChildren().add(playlistPane);
                count++;
            }
        }
        moviesScrollPane.setContent(playlistVBox);
        moviesScrollPane.setVvalue(0);
        pageTracker.setToPlaylistList();
    }

    private AnchorPane buildPlaylistPane(Playlist playlist, int i) {
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getClassLoader().getResource("PlaylistPane.fxml"));
            AnchorPane playlistPane = loader.load();
            playlistPane.setOnMouseClicked((mouseEvent) -> {
                playlistPaneClicked(playlist);
            });
            // set the movie info
            PlaylistController controller = loader.getController();
            controller.setVBoxColour(i);
            controller.setTextColour();
            controller.getPlaylistNameLabel().setText(playlist.getPlaylistName());
            if (playlist.getDescription().trim().length() == 0) {
                controller.getPlaylistDescriptionLabel().setText("*this playlist does not have a description :(*");
            } else {
                controller.getPlaylistDescriptionLabel().setText(playlist.getDescription());
            }
            controller.getPlaylistMoviesLabel()
                    .setText("No. of movies: " + Integer.toString(playlist.getMovies().size()));
            return playlistPane;
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return null;
    }

    private void playlistPaneClicked(Playlist playlist) {
        buildPlaylistInfo(playlist);
        playlistName = playlist.getPlaylistName();
        mMovies = convert(playlist.getMovies());
    }

    private void buildPlaylistInfo(Playlist playlist) {
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getClassLoader().getResource("PlaylistInfo.fxml"));
            AnchorPane playlistPane = loader.load();
            PlaylistInfoController controller = loader.getController();
            controller.getPlaylistNameLabel().setText(playlist.getPlaylistName());
            if (playlist.getDescription().trim().length() == 0) {
                controller.getPlaylistDescriptionLabel().setStyle("-fx-font-style: italic");
                controller.getPlaylistDescriptionLabel().setText("*this playlist does not have a description :(*");
            } else {
                controller.getPlaylistDescriptionLabel().setText(playlist.getDescription());
            }
            if (playlist.getMovies().size() != 0) {
                controller.getPlaylistInfoVBox().getChildren().add(buildPlaylistMoviesFlowPane(playlist.getMovies()));
            } else {
                Label emptyMoviesLabel = new Label(playlist.getPlaylistName() + " does not contain any movies :(");
                controller.getPlaylistInfoVBox().getChildren().add(2, emptyMoviesLabel);
            }
            moviesScrollPane.setContent(controller.getPlaylistInfoVBox());
            pageTracker.setToPlaylistInfo();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private FlowPane buildPlaylistMoviesFlowPane(ArrayList<PlaylistMovieInfoObject> movies) {
        // Setup progress bar and status label
        progressBar.setProgress(0.0);
        progressBar.setVisible(true);
        statusLabel.setText("Loading..");
        mMovies = convert(movies);

        moviesFlowPane = new FlowPane(Orientation.HORIZONTAL);
        moviesFlowPane.setHgap(4);
        moviesFlowPane.setVgap(10);
        moviesFlowPane.setPadding(new Insets(10, 8, 4, 8));
        moviesFlowPane.prefWrapLengthProperty().bind(moviesScrollPane.widthProperty());   // bind to scroll pane width

        for (int i = 0; i < movies.size(); i++) {
            AnchorPane posterPane = buildPlaylistMoviePosterPane(movies.get(i), i + 1);
            moviesFlowPane.getChildren().add(posterPane);
        }
        return moviesFlowPane;
    }

    private AnchorPane buildPlaylistMoviePosterPane(MovieInfoObject movie, int index) {
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getClassLoader().getResource("MoviePoster.fxml"));
            AnchorPane posterView = loader.load();
            //posterView.setOnScroll();
            posterView.setOnMouseClicked((mouseEvent) -> {
                try {
                    playlistMoviePosterClicked(movie);
                } catch (Exceptions exceptions) {
                    exceptions.printStackTrace();
                }
            });

            // set the movie info
            MoviePosterController controller = loader.getController();
            try {
                Image posterImage = new Image("/images/FakeMoviePoster.png");
                posterImage.progressProperty().addListener((observable, oldValue, newValue) -> {
                    try {
                        updateProgressBar(movie, newValue.doubleValue());
                    } catch (Exceptions exceptions) {
                        exceptions.printStackTrace();
                    }
                });
                controller.getPosterImageView().setImage(posterImage);
            } catch (NullPointerException ex) {
                ex.printStackTrace();
            }
            controller.getMovieTitleLabel().setText(movie.getTitle());
            controller.getMovieNumberLabel().setText(Integer.toString(index));
            return posterView;
        } catch (IOException ex) {
            //Ui.printLine();
        }

        return null;
    }


    //@@author nwenhui
    /**
     * Shows playlists.
     */
    public void showPlaylistList() throws IOException {
        playlist = userProfile.getPlaylistNames();
        buildPlaylistVBox(playlists);
    }

    private ArrayList<MovieInfoObject> convert(ArrayList<PlaylistMovieInfoObject> toConvert) {
        ArrayList<MovieInfoObject> converted = new ArrayList<>();
        boolean isMovie = false;
        for (PlaylistMovieInfoObject log : toConvert) {
            converted.add(new MovieInfoObject(log.getId(), log.getTitle(),
                    isMovie, log.getReleaseDateInfo(), log.getSummaryInfo(), log.getFullPosterPathInfo(),
                    log.getFullBackdropPathInfo(), log.getRatingInfo(), log.getGenreIdInfo(),
                    log.isAdultContent()));

        }
        return converted;
    }


    /**
     * This function is called when the user wants to see more information about a movie.
     */
    public void playlistMoviePosterClicked(MovieInfoObject movie) throws Exceptions {
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getClassLoader().getResource("PlaylistMoreInfo.fxml"));
            AnchorPane posterView = loader.load();
            PlaylistMovieController controller = loader.getController();

            controller.getMovieTitleLabel().setText(movie.getTitle());
            controller.getMovieRatingLabel().setText(String.format("%.2f", movie.getRatingInfo()));
            if (movie.getReleaseDateInfo() != null) {
                Date date = movie.getReleaseDateInfo();
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(date);
                String printDate = new SimpleDateFormat("dd/MM/yyyy").format(date);
                controller.getMovieDateLabel().setText(printDate);
            } else {
                controller.getMovieDateLabel().setText("N/A");
            }
            Text summaryText = new Text(movie.getSummaryInfo());
            controller.getMovieSummaryLabel().setText(movie.getSummaryInfo());
            ArrayList<Long> genreList = movie.getGenreIdInfo();
            String genres = "";
            for (int i = 0; i < genreList.size(); i++) {
                if (genreList.size() == 0) {
                    genres = "no genres";
                }
                if (i != genreList.size() - 1) {
                    genres += ProfileCommands.findGenreName(genreList.get(i).intValue());
                    genres += " , ";
                } else {
                    genres += ProfileCommands.findGenreName(genreList.get(i).intValue());
                }
            }
            controller.getMovieGenresLabel().setText(genres);
            moviesScrollPane.setContent(controller.getPlaylistMovieInfoAnchorPane());
            //mMoviesFlowPane.getChildren().add(posterView);
            // mMoviesScrollPane.setContent(mMoviesFlowPane);
            moviesScrollPane.setVvalue(0);
            pageTracker.setToPlaylistMovieInfo();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * This function clears the search text field when called.
     */
    public void clearSearchTextField() {
        searchTextField.setText("");
    }

    /**
     * This function is called to update the search text field.
     * @param updateStr The string to be written in the search text field.
     */
    public void updateTextField(String updateStr) {
        searchTextField.setText(searchTextField.getText() + updateStr);
        searchTextField.positionCaret(searchTextField.getText().length());
    }


    /**
     * Sets text in the UI under generalFeedbackText.
     * @param txt The text to be printed.
     */
    public void setGeneralFeedbackText(String txt) {
        generalFeedbackText.setText(txt + "\n");
    }


    /**
     * Clears the text displayed under the generalFeedbackText.
     */
    public void clearGeneralFeedbackText() {
        generalFeedbackText.setText("");
    }

    /**
     * CLears the text stored under the autoCompleteText.
     */
    public void clearAutoCompleteFeedbackText() {
        autoCompleteText.setText("Press 'tab' to enable/view auto-complete options available for you...");
    }

    public void setAutoCompleteText(String text) {
        autoCompleteText.setText(text);
    }

    /**
     * Updates the text in the autoCompleteText component in the UI.
     * @param txtArr The text to be writen in the autoCompleteText.
     */
    public void setAutoCompleteText(ArrayList<String> txtArr) {
        String output = "";
        Set<String> hashSet = new HashSet<String>();
        for (String s : txtArr) {
            hashSet.add(s);
        }

        for (String s:hashSet) {
            output += s;
            output += "\n";

        }
        autoCompleteText.setText(output);
    }

    /**
     * Retrieves the RetrieveRequest class.
     *
     * @return the RetrieveRequest class.
     */
    public RetrieveRequest getApiRequester() {
        return mMovieRequest;
    }

    /**
     * Retrieves the cinemaRetrieveRequest class.
     * @return the cinemaRetrieveRequest class
     */
    public CinemaRetrieveRequest getCinemaApiRequester() {
        return mCinemaRequest;
    }

    /**
     * Retrieves the Command class.
     * @return the Command class
     */
    public static String getCommands() {
        return command;
    }

    /**
     * Retrieves the UserProfile class.
     * @return the UserProfile class
     */
    public UserProfile getUserProfile() {
        return userProfile;
    }

    /**
     * Retrieves the Arraylist of the movie objects to be displayed in the UI.
     * @return The Arraylist of the movie objects to be displayed in the UI.
     */
    public ArrayList<MovieInfoObject> getmMovies() {
        return mMovies;
    }

    /**
     * Called to set the search profile for a particular search request.
     * @param searchProfile The search profile for a particular search request.
     */
    public void setSearchProfile(SearchProfile searchProfile) {
        mMovieRequest.setSearchProfile(searchProfile);
    }

    /**
     * Called to update the user's preferred sort label.
     */
    private void updateSortInterface(UserProfile userProfile) {
        logger.log(Level.INFO, PromptMessages.UPDATING_SORT_IN_UI);
        if (userProfile.isSortByAlphabetical()) {
            sortAlphaOrderLabel.setText("Y");
            sortLatestDateLabel.setText("N");
            sortHighestRatingLabel.setText("N");
        } else if (userProfile.isSortByLatestRelease()) {
            sortAlphaOrderLabel.setText("N");
            sortLatestDateLabel.setText("Y");
            sortHighestRatingLabel.setText("N");
        } else if (userProfile.isSortByHighestRating()) {
            sortAlphaOrderLabel.setText("N");
            sortLatestDateLabel.setText("N");
            sortHighestRatingLabel.setText("Y");
        } else {
            sortAlphaOrderLabel.setText("N");
            sortLatestDateLabel.setText("N");
            sortHighestRatingLabel.setText("N");
        }
    }


    public void setPlaylistName(String name) {
        playlistName = name;
    }

    public String getPlaylistName() {
        return playlistName;
    }

    /**
     * Called to refresh the gui page so it reflects user's changes.
     */
    public void refresh() throws IOException {
        userProfile = new EditProfileJson().load();
        playlists = userProfile.getPlaylistNames();
        switch (pageTracker.getCurrentPage()) {
        case "playlistList":
            EditProfileJson editProfileJson = new EditProfileJson();
            buildPlaylistVBox(editProfileJson.load().getPlaylistNames());
            break;
        case "playlistInfo":
            EditPlaylistJson editPlaylistJson = new EditPlaylistJson(playlistName);
            buildPlaylistInfo(editPlaylistJson.load());
            break;
        default:
            break;
        }
    }

    public PageTracker getPageTracker() {
        return pageTracker;
    }

    /**
     * to go back to playlist info page from playlistmovieinfo page.
     */
    public void backToPlaylistInfo() throws IOException {
        if (pageTracker.isPlaylistMovieInfo()) {
            pageTracker.setToPlaylistInfo();
            refresh();
        }
    }

    //@@author riyas97
    private void getInfo(InfoController controller, MovieInfoObject movie) throws Exceptions, IOException {
        controller.getMovieTitleLabel().setText(movie.getTitle());
        controller.getMovieRatingLabel().setText(String.format(DECIMAL_FORMAT, movie.getRatingInfo()));
        if (movie.getReleaseDateInfo() != null) {
            Date date = movie.getReleaseDateInfo();
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);
            String printDate = new SimpleDateFormat("dd/MM/yyyy").format(date);
            controller.getMovieReleaseDateLabel().setText(printDate);
        } else {
            controller.getMovieReleaseDateLabel().setText(UNAVAILABLE_INFO);
        }
        try {
            Image posterImage = new Image(movie.getFullBackdropPathInfo(), true);
            controller.getMovieBackdropImageView().setImage(posterImage);
        } catch (NullPointerException ex) {
            ex.printStackTrace();
        }

        controller.getMovieSummaryLabel().setText(movie.getSummaryInfo());
        mMovieRequest.beginMoreInfoRequest(movie);
        ArrayList<String> castStrings = movie.getCastInfo();
        String cast = "";
        if (castStrings.size() == 0) {
            cast = UNAVAILABLE_INFO;
        } else {
            for (int i = 0; i < castStrings.size(); i += 1) {
                cast += castStrings.get(i);
                cast += ", ";
            }
        }
        controller.getMovieCastLabel().setText(cast);
        controller.getMovieCertLabel().setText(movie.getCertInfo());

        ArrayList<Long> genres = movie.getGenreIdInfo();
        String genreText = "";
        for (int i = 0; i < genres.size(); i += 1) {
            Long getGenre = genres.get(i);
            int convertGenre = getGenre.intValue();
            String genreAdd = ProfileCommands.findGenreName(convertGenre);
            if (!genreAdd.equals("0")) {
                // System.out.println(ProfileCommands.findGenreName(convertGenre));
                genreText += ProfileCommands.findGenreName(convertGenre);
            }
            if (i != genres.size() - 1) {
                genreText += ", ";
            }

        }
        controller.getMovieGenresLabel().setText(genreText);
    }

    /**
     * This function is called when the user wants to see more information about a movie.
     * @param movie Object that contains all the informations about a movie/TV show.
     */
    public void moviePosterClicked(MovieInfoObject movie) throws Exceptions {
        try {
            //mMainApplication.transitToMovieInfoController(movie);
            logger.log(Level.INFO, PromptMessages.DISPLAYING_MORE_INFO);
            moviesFlowPane.getChildren().clear();
            moviesFlowPane = new FlowPane(Orientation.HORIZONTAL);
            moviesFlowPane.setHgap(4);
            moviesFlowPane.setVgap(10);
            moviesFlowPane.setPadding(new Insets(10, 8, 4, 8));
            moviesFlowPane.prefWrapLengthProperty().bind(moviesScrollPane.widthProperty());
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getClassLoader().getResource("MoreInfo.fxml"));
            AnchorPane posterView = loader.load();
            InfoController controller = loader.getController();
            getInfo(controller, movie);
            moviesFlowPane.getChildren().add(posterView);
            moviesScrollPane.setContent(moviesFlowPane);
            moviesScrollPane.setVvalue(0);
            setGeneralFeedbackText(PromptMessages.TO_VIEW_BACK_SEARCHES);
            isViewMoreInfoPage = true;
            clearAutoCompleteFeedbackText();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}