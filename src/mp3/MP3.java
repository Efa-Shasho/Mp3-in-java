package mp3;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.util.Duration;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import static javafx.application.Application.launch;
import javafx.geometry.Insets;

public class MP3 extends Application {

    // Fields
    private ArrayList<File> songs;
    private ArrayList<File> favorites;
    private int songNumber;
    private Media media;
    private MediaPlayer mediaPlayer;
    private Timer timer;
    private TimerTask task;
    private boolean running;

    private ProgressBar songProgressBar;
    private ComboBox<String> speedBox;
    private Slider volumeSlider;
    private ListView<String> favoritesList;
    private ListView<String> musicList;
    private Label timeLabel;
    private Label songLabel;

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("DANKIRA player");
         
        // Initialize
        initializeMedia();

        // Create UI
        createUI(primaryStage);

        // Set up close event
        setCloseEvent(primaryStage);
    }

    public static void main(String[] args) {
        launch(args);
    }

    // Play media methods
    public void playMedia() {
        changeSpeed(null);
        beginTimer();
        mediaPlayer.play();
        updateSongLabel(); // Update the song label when a new media is played
    }

    public void pauseMedia() {
        cancelTimer();
        mediaPlayer.pause();
    }

    public void resetMedia() {
        mediaPlayer.seek(Duration.seconds(0));
    }

    public void previousMedia() {
        if (songNumber > 0) {
            songNumber--;
        } else {
            songNumber = songs.size() - 1;
        }
        updateMediaAndPlay();
    }

    public void nextMedia() {
        if (songNumber < songs.size() - 1) {
            songNumber++;
        } else {
            songNumber = 0;
        }
        updateMediaAndPlay();
    }

    public void addToFavorites() {
        if (media != null) {
            favorites.add(songs.get(songNumber));
            favoritesList.getItems().add(songs.get(songNumber).getName());
            System.out.println("Added to favorites: " + songs.get(songNumber).getName());
        }
    }

    public void changeSpeed(javafx.event.ActionEvent event) {
        Platform.runLater(() -> {
            String selectedSpeed = speedBox.getValue();
            if (selectedSpeed == null) {
                mediaPlayer.setRate(1.0);
            } else {
                double speedPercentage = Double.parseDouble(selectedSpeed.substring(0, selectedSpeed.length() - 1));
                double rate = 1.0 + (speedPercentage / 100.0 - 1.0);
                mediaPlayer.setRate(rate);
            }
        });
    }

    // Timer methods
    public void beginTimer() {
        timer = new Timer();
        task = new TimerTask() {
            public void run() {
                running = true;
                double current = mediaPlayer.getCurrentTime().toSeconds();
                double end = media.getDuration().toSeconds();
                songProgressBar.setProgress(current / end);
                Platform.runLater(() -> {
                    updateTimeLabel(current, end);
                });
                if (current / end == 1) {
                    cancelTimer();
                }
            }
        };
        timer.scheduleAtFixedRate(task, 0, 1000);
    }

    public void cancelTimer() {
        running = false;
        timer.cancel();
    }

  // Update media and play
private void updateMediaAndPlay() {
    mediaPlayer.stop();
    if (running) {
        cancelTimer();
    }

    media = new Media(songs.get(songNumber).toURI().toString());
    mediaPlayer = new MediaPlayer(media);
    mediaPlayer.setVolume(volumeSlider.getValue() * 0.01);

    mediaPlayer.setOnEndOfMedia(() -> {
        // Play the next song when the current one ends
        if (songNumber < songs.size() - 1) {
            songNumber++;
        } else {
            // If it's the last song, go back to the first one
            songNumber = 0;
        }

    });

    mediaPlayer.setOnPlaying(() -> {
        running = true;
        beginTimer();
        updateSongLabel(); // Update the song label when a new media is played
    });

    mediaPlayer.play();
}


    // UI initialization
    private void initializeMedia() {
        songs = new ArrayList<>();
        favorites = new ArrayList<>();
        File directory = new File("music");

        if (!directory.exists() || !directory.isDirectory()) {
            System.err.println("The 'music' directory does not exist or is not a directory.");
            return;
        }

        File[] files = directory.listFiles();

        if (files != null) {
            for (File file : files) {
                songs.add(file);
            }
        }

        if (songs.isEmpty()) {
            System.err.println("No music files found in the 'music' directory.");
            return;
        }

        media = new Media(songs.get(songNumber).toURI().toString());
        mediaPlayer = new MediaPlayer(media);
    }

    // I have created the  UI 
    private void createUI(Stage primaryStage) {
     songLabel = new Label("MP3 Player");
    songLabel.setStyle("-fx-font-size: 22; -fx-text-fill:BLUE; -fx-font-weight: bold;");
    songProgressBar = new ProgressBar(0);
    songProgressBar.setStyle("-fx-accent: #00FF00;");
    songProgressBar.setMaxWidth(Double.MAX_VALUE);

       Button playButton = new Button("PLAY");
    Button pauseButton = new Button("PAUSE");
    Button resetButton = new Button("RESTART");
    Button previousButton = new Button("<<");
    Button nextButton = new Button(">>");
        speedBox = new ComboBox<>();
        speedBox.setPromptText("SPEED");
        volumeSlider = new Slider(0, 100, 50);
        Button addToFavoritesButton = new Button("➕");
        Button listAllMusicButton = new Button("List All Music");
        Button favoritesToggleButton = new Button("❤");
favoritesToggleButton.setStyle("-fx-text-fill:red;");
        playButton.setOnAction(e -> playMedia());
        pauseButton.setOnAction(e -> pauseMedia());
        resetButton.setOnAction(e -> resetMedia());
        previousButton.setOnAction(e -> previousMedia());
        nextButton.setOnAction(e -> nextMedia());
        addToFavoritesButton.setOnAction(e -> addToFavorites());
        listAllMusicButton.setOnAction(e -> toggleMusicList());
        favoritesToggleButton.setOnAction(e -> toggleFavoritesList());

        speedBox.getItems().addAll("25%", "50%", "75%", "100%", "125%", "150%", "175%", "200%");
        speedBox.setOnAction(this::changeSpeed);

        volumeSlider.valueProperty().addListener((observable, oldValue, newValue) ->
                mediaPlayer.setVolume(volumeSlider.getValue() * 0.01));

        musicList = new ListView<>();
        ObservableList<String> musicItems = FXCollections.observableArrayList(getMusicFileNames());
        musicList.setItems(musicItems);
        musicList.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        musicList.setVisible(false);

        favoritesList = new ListView<>();
        ObservableList<String> favoritesItems = FXCollections.observableArrayList();
        favoritesList.setItems(favoritesItems);
        favoritesList.setVisible(false);

        timeLabel = new Label("00:00 / 00:00");
    timeLabel.setStyle("-fx-background-color:white ; -fx-text-fill:black; ");  // Set background color for the timer

    GridPane buttonGrid = new GridPane();
    buttonGrid.setStyle("-fx-background-color: black;");  // Set background color for the button grid
    buttonGrid.setHgap(10);
    buttonGrid.setVgap(10);
    buttonGrid.setPadding(new Insets(10));

    buttonGrid.addRow(0, playButton, pauseButton, resetButton, previousButton, nextButton, speedBox,
            volumeSlider, addToFavoritesButton, listAllMusicButton, favoritesToggleButton);

    VBox vbox = new VBox();
    vbox.setStyle("-fx-background-color: #000000;");  // Set background color for the main vbox
    vbox.getChildren().addAll(songLabel, musicList, favoritesList, songProgressBar, buttonGrid, timeLabel);
    vbox.setAlignment(Pos.BOTTOM_CENTER);



       musicList.setOnMouseClicked(event -> {
    String selectedItem = musicList.getSelectionModel().getSelectedItem();
    if (selectedItem != null) {
        int selectedIndex = musicList.getSelectionModel().getSelectedIndex();
        songNumber = selectedIndex;
        updateMediaAndPlay();
        playMedia();  // Add this line to automatically start playing the selected song
    }
});


        songProgressBar.setOnMouseClicked(event -> {
            if (media != null) {
                double mouseX = event.getX();
                double progressBarWidth = songProgressBar.getWidth();
                double seekTime = (mouseX / progressBarWidth) * media.getDuration().toSeconds();
                mediaPlayer.seek(Duration.seconds(seekTime));
            }
        });

        Scene scene = new Scene(vbox, 800, 400);
        primaryStage.setScene(scene);
        primaryStage.show();
        favoritesList.setOnMouseClicked(event -> {
    String selectedItem = favoritesList.getSelectionModel().getSelectedItem();
    if (selectedItem != null) {
        int selectedIndex = favoritesList.getSelectionModel().getSelectedIndex();
        if (selectedIndex >= 0 && selectedIndex < favorites.size()) {
            songNumber = songs.indexOf(favorites.get(selectedIndex));
            updateMediaAndPlay();
            playMedia(); // Automatically start playing the selected favorite song
        }
    }
});

    }

    private void setCloseEvent(Stage primaryStage) {
        primaryStage.setOnCloseRequest(event -> {
            mediaPlayer.dispose();
            Platform.exit();
            System.exit(0);
        });
    }

    private List<String> getMusicFileNames() {
        List<String> fileNames = new ArrayList<>();
        for (File file : songs) {
            fileNames.add(file.getName());
        }
        return fileNames;
    }

    private void toggleMusicList() {
        musicList.setVisible(!musicList.isVisible());
    }

    private void toggleFavoritesList() {
        favoritesList.setVisible(!favoritesList.isVisible());
    }

    private void updateTimeLabel(double current, double end) {
        int currentMinutes = (int) (current / 60);
        int currentSeconds = (int) (current % 60);
        int endMinutes = (int) (end / 60);
        int endSeconds = (int) (end % 60);
        String currentTimeString = String.format("%02d:%02d", currentMinutes, currentSeconds);
        String endTimeString = String.format("%02d:%02d", endMinutes, endSeconds);
        timeLabel.setText(currentTimeString + " / " + endTimeString);
    }

    // Update the song label with the currently playing music
    private void updateSongLabel() {
        Platform.runLater(() -> {
            if (songNumber < songs.size()) {
                String currentSongName = songs.get(songNumber).getName();
                songLabel.setText("Now Playing: " + currentSongName);
            }
        });
    }
}
