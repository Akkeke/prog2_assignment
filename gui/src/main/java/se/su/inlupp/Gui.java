package se.su.inlupp;

import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Pair;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class Gui extends Application {
  private static final Color BACKGROUND_COLOR = Color.rgb(60, 60, 70);
  private static final Color PLACE_COLOR_PENDING = Color.rgb(230, 194, 16);
  private static final Color PLACE_COLOR_STANDARD = Color.rgb(42, 224, 36);
  private static final Color PLACE_COLOR_SELECTED = Color.rgb(36, 180, 224);

  private final Graph<String> graph = new ListGraph<>();
  private final Map<Circle, String> placeMap = new HashMap<>();

  private Stage stage;
  private FileChooser fileChooser;
  private ImageView imageView;
  private Pane center;
  private HBox buttonPane;

  private Button findPath;
  private Button showConnection;
  private Button newPlace;
  private Button newConnection;
  private Button changeConnection;

  private Circle selectedPlace1;
  private Circle selectedPlace2;

  public void start(Stage stage) {
    this.stage = stage;
    fileChooser = new FileChooser();
    imageView = new ImageView();
    center = new Pane();
    buttonPane = new HBox();

    BorderPane root = new BorderPane();
    VBox topPane = new VBox();
    MenuBar menuBar = new MenuBar();
    topPane.getChildren().addAll(menuBar, buttonPane);
    center.getChildren().add(imageView);
    root.setCenter(center);
    root.setTop(topPane);

    topPane.setSpacing(20);
    buttonPane.setPadding(new Insets(10));
    buttonPane.setSpacing(20);
    buttonPane.setAlignment(Pos.CENTER);
    root.setBackground(Background.fill(BACKGROUND_COLOR));

    Menu fileMenu = new Menu("File");
    MenuItem newMapItem = new MenuItem("New Map");
    MenuItem openItem = new MenuItem("Open");
    MenuItem saveItem = new MenuItem("Save");
    MenuItem saveImageItem = new MenuItem("Save Image");
    MenuItem exitItem = new MenuItem("Exit");
    menuBar.getMenus().add(fileMenu);
    fileMenu.getItems().addAll(newMapItem, openItem, saveItem, saveImageItem, exitItem);

    newMapItem.setOnAction(new NewMapHandler());
    openItem.setOnAction(new OpenHandler());
    saveItem.setOnAction(new SaveHandler());
    saveImageItem.setOnAction(new SaveImageHandler());
    exitItem.setOnAction(new ExitHandler());

    findPath = new Button("Find Path");
    showConnection = new Button("Show Connection");
    newPlace = new Button("New Place");
    newConnection = new Button("New Connection");
    changeConnection = new Button("Change Connection");
    buttonPane.getChildren().addAll(findPath, showConnection, newPlace, newConnection, changeConnection);
    setButtonsDisable(true);

    newPlace.setOnAction(new NewPlaceHandler());
    newConnection.setOnAction(new NewConnectionHandler());

    Scene scene = new Scene(root, 700, 900);
    stage.setScene(scene);
    stage.show();

    showNewConnectionDialog("Paris", "Madrid");
  }

  class NewMapHandler implements EventHandler<ActionEvent> {
    @Override
    public void handle(ActionEvent event) {
      File file = fileChooser.showOpenDialog(stage);
      if (file != null) {
        Image image = new Image(file.toURI().toString());
        if (image.isError()) {
          alertError("Unable to load image file!");
          return;
        }
        imageView.setImage(image);
        center.setMinSize(image.getWidth(), image.getHeight());
        center.setMaxSize(image.getWidth(), image.getHeight());
        setButtonsDisable(false);
      }
    }
  }

  class NewPlaceHandler implements EventHandler<ActionEvent> {
    @Override
    public void handle(ActionEvent event) {
      center.setCursor(Cursor.CROSSHAIR);
      center.setOnMouseClicked(new NewPlaceClickHandler());
      newPlace.setDisable(true);
    }
  }

  class NewPlaceClickHandler implements EventHandler<MouseEvent> {
    @Override
    public void handle(MouseEvent event) {
      TextInputDialog nameInput = new TextInputDialog();
      nameInput.setTitle("Name");
      nameInput.setHeaderText("");
      nameInput.setContentText("Name of place:");

      Circle dot = new Circle(8, PLACE_COLOR_PENDING);
      dot.relocate(event.getX() - dot.getRadius(), event.getY() - dot.getRadius());
      center.getChildren().add(dot);

      Optional<String> name = nameInput.showAndWait();
      if (name.isEmpty() || name.get().isBlank()) {
        center.getChildren().remove(dot);
      } else {
        dot.setFill(PLACE_COLOR_STANDARD);
        Text nameTag = new Text(name.get());
        nameTag.setFont(Font.font("System", FontWeight.EXTRA_BOLD, 14));
        nameTag.relocate(event.getX() - 8, event.getY() + 5);
        center.getChildren().add(nameTag);

        dot.setOnMouseClicked(new SelectPlaceHandler());
        placeMap.put(dot, name.get());
        graph.add(name.get());
      }
      center.setOnMouseClicked(null);
      center.setCursor(null);
      newPlace.setDisable(false);
    }
  }

  class SelectPlaceHandler implements EventHandler<MouseEvent> {
    @Override
    public void handle(MouseEvent event) {
      if (event.getSource() instanceof Circle place) {
        if (place.equals(selectedPlace1)) {
          if (selectedPlace2 != null) {
            selectedPlace1 = selectedPlace2;
            selectedPlace2 = null;
          } else {
            selectedPlace1 = null;
          }
          place.setFill(PLACE_COLOR_STANDARD);
        } else if (place.equals(selectedPlace2)) {
          selectedPlace2 = null;
          place.setFill(PLACE_COLOR_STANDARD);
        } else if (selectedPlace1 == null) {
          selectedPlace1 = place;
          place.setFill(PLACE_COLOR_SELECTED);
        } else if (selectedPlace2 == null) {
          selectedPlace2 = place;
          place.setFill(PLACE_COLOR_SELECTED);
        }
      }
    }
  }

  class NewConnectionHandler implements EventHandler<ActionEvent> {
    @Override
    public void handle(ActionEvent event) {
      if (selectedPlace1 != null && selectedPlace2 != null) {
        String place1 = placeMap.get(selectedPlace1);
        String place2 = placeMap.get(selectedPlace2);
        if (graph.pathExists(place1, place2)) {
          alertError("Connection already exists!");
        } else {
          showNewConnectionDialog(place1, place2);
        }
      } else {
        alertError("Two places must be selected!");
      }
    }
  }

  private void setButtonsDisable(boolean value) {
    buttonPane.getChildren().forEach(btn -> btn.setDisable(value));
  }

  private void alertError(String text) {
    Alert alert = new Alert(Alert.AlertType.ERROR, text);
    alert.setHeaderText("");
    alert.showAndWait();
  }

  private void showNewConnectionDialog(String place1, String place2) {
    Dialog<Pair<String, String>> dialog = new Dialog<>();
    dialog.setTitle("Connection");
    dialog.setHeaderText(String.format("Connection from %s to %s", place1, place2));

    VBox vBox = new VBox();
    dialog.getDialogPane().setContent(vBox);
    TextField nameInput = new TextField();
    nameInput.setPromptText("Name:");
    TextField timeInput = new TextField();
    timeInput.setPromptText("Time:");
    vBox.getChildren().addAll(nameInput, timeInput);
    dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
    vBox.requestFocus();

    Optional<Pair<String, String>> result = dialog.showAndWait();
    if (result.isPresent()) {

    }
  }

  private void save(String fileName)
  {
    try
    {
      File newFile = new File(fileName + ".graf");
      if(!newFile.createNewFile())
      {
        alertError("File already exists");
      }
      write(newFile);
    }
    catch (IOException e)
    {
      alertError("An error occurred");
    }
  }

  private void write(File targetFile)
  {
    try (BufferedWriter writer = new BufferedWriter(new FileWriter(targetFile)))
    {
      writer.write("file: " + imageView.getImage().getUrl());
      writer.newLine();

      for (Map.Entry<Circle, String> kv : placeMap.entrySet()) {
        Circle dot = kv.getKey();
        writer.write(String.format("%s;%.1f;%.1f;", kv.getValue(), dot.getLayoutX(), dot.getLayoutX()));

    }
      writer.newLine();

      //for ()
    }

    catch (IOException e)
    {

    }
  }

  public static void main(String[] args) {
    launch(args);
  }
}