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
import javafx.scene.shape.Line;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.*;
import java.util.*;

public class Gui extends Application {
  private static final Color BACKGROUND_COLOR = Color.rgb(60, 60, 70);
  private static final Color PLACE_COLOR_PENDING = Color.rgb(230, 194, 16);
  private static final Color PLACE_COLOR_STANDARD = Color.rgb(42, 224, 36);
  private static final Color PLACE_COLOR_SELECTED = Color.rgb(36, 180, 224);
  private static final double PLACE_SIZE = 8;
  private static final int FONT_SIZE = 14;
  private static final int LINE_STROKE_WIDTH = 3;

  private Graph<String> graph = new ListGraph<>();
  private Map<String, Circle> placeMap = new HashMap<>();

  private Stage stage;
  private FileChooser fileChooser;
  private ImageView imageView;
  private Pane center;
  private HBox buttonPane;

  private Dialog<Boolean> connectionDialog;
  private TextField nameInput;
  private TextField timeInput;

  private Button findPath;
  private Button showConnection;
  private Button newPlace;
  private Button newConnection;
  private Button changeConnection;

  private Circle selectedPlace1;
  private Circle selectedPlace2;

  private boolean isChanged = false;

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

    topPane.setSpacing(10);
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

    //Connection dialog
    connectionDialog = new Dialog<>();
    connectionDialog.setTitle("Connection");

    VBox vBox = new VBox();
    connectionDialog.getDialogPane().setContent(vBox);
    nameInput = new TextField();
    Label nameLabel = new Label("Name:");
    timeInput = new TextField();
    Label timeLabel = new Label("Time:");
    vBox.getChildren().addAll(nameLabel, nameInput, timeLabel, timeInput);
    connectionDialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
    connectionDialog.setResultConverter(btn -> btn == ButtonType.OK ? true: null);

    Scene scene = new Scene(root, 650, 820);
    stage.setScene(scene);
    stage.show();
  }

  //Menyns funktionalitet

  class NewMapHandler implements EventHandler<ActionEvent> {
    @Override
    public void handle(ActionEvent event) {
      File file = fileChooser.showOpenDialog(stage);
      if (file != null) {
        Image image = new Image(file.toURI().toString());
        if (!image.isError()) {
          setImageView(image);
          selectedPlace1 = null;
          selectedPlace2 = null;
          graph = new ListGraph<>();
          placeMap = new HashMap<>();
          setButtonsDisable(false);
          isChanged = false;
        } else {
          alertError("Unable to load image file!");
        }
      }
    }
  }

  class OpenHandler implements EventHandler<ActionEvent> {
    @Override
    public void handle(ActionEvent event) {
      File file = fileChooser.showOpenDialog(stage);
      if (file != null && file.getName().endsWith(".graf")) {
        open(file);
        setButtonsDisable(false);
        selectedPlace1 = null;
        selectedPlace2 = null;
      }
    }
  }

  class SaveHandler implements EventHandler<ActionEvent> {
    @Override
    public void handle(ActionEvent event) {
      File file = fileChooser.showSaveDialog(stage);
      if (file != null) {
        if (!file.getName().endsWith(".graf")) {
          file = new File(file.getAbsolutePath() + ".graf");
        }
        save(file);
      }
    }
  }

  class SaveImageHandler implements EventHandler<ActionEvent> {
    @Override
    public void handle(ActionEvent event) {

    }
  }

  class ExitHandler implements EventHandler<ActionEvent> {
    @Override
    public void handle(ActionEvent event) {

    }
  }

  private void save(File file) {
    try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
      writer.write(imageView.getImage().getUrl());
      writer.newLine();
      StringJoiner sj = new StringJoiner(";");

      for (Map.Entry<String, Circle> kv : placeMap.entrySet()) {
        Circle circle = kv.getValue();
        sj.add(String.format(Locale.US, "%s;%.1f;%.1f", kv.getKey(), circle.getLayoutX(), circle.getLayoutY()));
      }
      writer.write(sj.toString());
      writer.newLine();

      for (String node : graph.getNodes()) {
        for (Edge<String> edge : graph.getEdgesFrom(node)) {
          writer.write(String.format("%s;%s;%s;%d", node, edge.getDestination(), edge.getName(), edge.getWeight()));
          writer.newLine();
        }
      }
      isChanged = false;
    } catch (IOException e) {
      alertError("An error occurred while saving file");
    }
  }

  private void open(File file) {
    try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
      graph = new ListGraph<>();
      placeMap = new HashMap<>();
      String imagePath = reader.readLine();
      Image image = new Image(imagePath);
      if (!image.isError()) {
        setImageView(image);
        String[] info = reader.readLine().split(";");
        for (int i = 0; i < info.length; i += 3) {
          String place = info[i];
          double x = Double.parseDouble(info[i + 1]);
          double y = Double.parseDouble(info[i + 2]);
          addAndDrawPlace(place, x, y);
        }

        String line = reader.readLine();
        while (line != null) {
          info = line.split(";");
          int weight = Integer.parseInt(info[3]);
          addAndDrawConnection(info[0], info[1], info[2], weight);
          line = reader.readLine();
          System.out.println(line);
        }
        isChanged = false;
      }
    } catch (IOException e) {
      alertError("An error occurred while opening file");
    }
  }

  private void setImageView(Image image) {
    imageView.setImage(image);
    center.setMinSize(image.getWidth(), image.getHeight());
    center.setMaxSize(image.getWidth(), image.getHeight());
  }

  //Knapparnas funktionalitet

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

      Circle dot = new Circle(PLACE_SIZE, PLACE_COLOR_PENDING);
      dot.relocate(event.getX() - dot.getRadius(), event.getY() - dot.getRadius());
      center.getChildren().add(dot);

      Optional<String> name = nameInput.showAndWait();
      center.getChildren().remove(dot);

      if (name.isPresent() && !name.get().isBlank()) {
        addAndDrawPlace(name.get(), event.getX(), event.getY());
        isChanged = true;
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
        String place1 = "";
        String place2 = "";
        for (String s : placeMap.keySet()) {
          Circle circle = placeMap.get(s);
          if (circle == selectedPlace1) place1 = s;
          if (circle == selectedPlace2) place2 = s;
          if (!place1.isBlank() && !place2.isBlank()) break;
        }
        if (graph.pathExists(place1, place2)) {
          alertError("Connection already exists!");
        } else {
          if (newConnection(place1, place2)) {
            Line line = new Line();
            line.setStartX(selectedPlace1.getLayoutX());
            line.setStartY(selectedPlace1.getLayoutY());
            line.setEndX(selectedPlace2.getLayoutX());
            line.setEndY(selectedPlace2.getLayoutY());
            line.setStrokeWidth(3);
            line.setMouseTransparent(true);
            center.getChildren().add(line);
            isChanged = true;
          }
        }
      } else {
        alertError("Two places must be selected!");
      }
    }
  }

  private void addAndDrawPlace(String name, double x, double y) {
    Circle place = new Circle(PLACE_SIZE, PLACE_COLOR_STANDARD);
    place.relocate(x - place.getRadius(), y - place.getRadius());
    place.setCursor(Cursor.HAND);
    place.setOnMouseClicked(new SelectPlaceHandler());
    Text nameTag = new Text(name);
    nameTag.setFont(Font.font("System", FontWeight.EXTRA_BOLD, FONT_SIZE));
    nameTag.relocate(x - 8, y + 5);
    center.getChildren().addAll(place, nameTag);
    placeMap.put(name, place);
    graph.add(name);
  }

  private void setButtonsDisable(boolean value) {
    buttonPane.getChildren().forEach(btn -> btn.setDisable(value));
  }

  private void alertError(String text) {
    Alert alert = new Alert(Alert.AlertType.ERROR, text);
    System.err.println(text);
    alert.setHeaderText("");
    alert.showAndWait();
  }

  private boolean newConnection(String place1, String place2) {
    connectionDialog.setHeaderText(String.format("New connection from %s to %s.", place1, place2));
    timeInput.clear();
    nameInput.clear();
    Optional<Boolean> result = connectionDialog.showAndWait();
    if (result.isPresent() && result.get()) {
      String name = nameInput.getText();
      int time = -1;
      try {
        time = Integer.parseInt(timeInput.getText());
      } catch (NumberFormatException e) {
        alertError("Invalid value for time");
        return false;
      }
      if (time > -1 && !name.isBlank()) {
        graph.connect(place1, place2, name, time);
        return  true;
      } else {
        alertError("Invalid input");
      }
    }
    return false;
  }

  private void addAndDrawConnection(String place1, String place2, String name, int weight) {
    if (!graph.pathExists(place1, place2)) graph.connect(place1, place2, name, weight);
    Circle dot1 = placeMap.get(place1);
    Circle dot2 = placeMap.get(place2);
    Line line = new Line();
    line.setStartX(dot1.getLayoutX());
    line.setStartY(dot1.getLayoutY());
    line.setEndX(dot2.getLayoutX());
    line.setEndY(dot2.getLayoutY());
    line.setStrokeWidth(LINE_STROKE_WIDTH);
    line.setMouseTransparent(true);
    center.getChildren().add(line);
  }

  public static void main(String[] args) {
    launch(args);
  }
}