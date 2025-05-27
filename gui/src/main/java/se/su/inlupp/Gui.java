package se.su.inlupp;

import javafx.application.Application;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
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
import javafx.stage.Window;
import javafx.stage.WindowEvent;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.image.BufferedImage;
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
  private Map<Circle, String> circleToPlace = new HashMap<>();

  private Stage stage;
  private FileChooser fileChooser;
  private ImageView imageView;
  private Pane center;
  private HBox buttonPane;

  private Dialog<Boolean> connectionDialog;
  private TextField nameInput;
  private TextField timeInput;

  private Dialog<ButtonType> pathDialog;
  private TextArea pathText;

  private Button newPlace;

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
    exitItem.setOnAction(new ExitItemHandler());

    newPlace = new Button("New Place");
    Button findPath = new Button("Find Path");
    Button showConnection = new Button("Show Connection");
    Button newConnection = new Button("New Connection");
    Button changeConnection = new Button("Change Connection");
    buttonPane.getChildren().addAll(findPath, showConnection, newPlace, newConnection, changeConnection);
    setButtonsDisable(true);

    newPlace.setOnAction(new NewPlaceHandler());
    newConnection.setOnAction(new NewConnectionHandler());
    showConnection.setOnAction(new ShowConnectionHandler());
    changeConnection.setOnAction(new ChangeConnectionHandler());
    findPath.setOnAction(new FindPathHandler());

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

    //Path dialog
    pathDialog = new Dialog<>();
    pathDialog.setTitle("Path");
    pathText = new TextArea();
    pathText.setWrapText(true);
    pathText.setEditable(false);
    pathDialog.getDialogPane().getButtonTypes().add(ButtonType.OK);
    pathDialog.getDialogPane().setContent(pathText);

    Scene scene = new Scene(root, 650, 820);
    stage.setScene(scene);
    stage.setOnCloseRequest(new ExitHandler());
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
          circleToPlace = new HashMap<>();
          setButtonsDisable(false);
          isChanged = true;
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
      if (imageView.getImage() == null) {
        alertError("File is empty!");
        return;
      }
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
      if (imageView.getImage() != null) {
          try {
            WritableImage image = center.snapshot(null, null);
            BufferedImage bufferedImage = SwingFXUtils.fromFXImage(image, null);
              ImageIO.write(bufferedImage, "png", new File("capture.png"));
          } catch (IOException e) {
              alertError("Unable to save image!");
              System.err.println(e.getMessage());
          }
      } else {
        alertError("No image to save!");
      }
    }
  }

  class ExitItemHandler implements EventHandler<ActionEvent> {
    @Override
    public void handle(ActionEvent event) {
      stage.fireEvent(new WindowEvent(stage, WindowEvent.WINDOW_CLOSE_REQUEST));
    }
  }

  class ExitHandler implements EventHandler<WindowEvent> {
    @Override
    public void handle(WindowEvent event) {
      if (isChanged) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setHeaderText("Unsaved changes! Exit anyway?");
        alert.setTitle("Exit");
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get().equals(ButtonType.CANCEL)) {
          event.consume();
        }
      }
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
      circleToPlace = new HashMap<>();
      String imagePath = reader.readLine();
      if (imagePath == null) {
        alertError("Unable to load image");
        return;
      }
      Image image = new Image(imagePath);
      if (!image.isError()) {
        setImageView(image);
        String line = reader.readLine();
        if (line.isBlank()) return;
        String[] info = line.split(";");
        for (int i = 0; i < info.length; i += 3) {
          String place = info[i];
          double x = Double.parseDouble(info[i + 1]);
          double y = Double.parseDouble(info[i + 2]);
          addAndDrawPlace(place, x, y);
        }

        line = reader.readLine();
        while (line != null) {
          info = line.split(";");
          int weight = Integer.parseInt(info[3]);
          addAndDrawConnection(info[0], info[1], info[2], weight);
          line = reader.readLine();
        }
        isChanged = false;
      }
    } catch (IOException e) {
      alertError("An error occurred while opening file");
    }
  }

  private void setImageView(Image image) {
    center.getChildren().clear();
    center.getChildren().add(imageView);
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

      if (name.isPresent()) {
        if (!name.get().isBlank()) {
          if (!placeMap.containsKey(name.get())) {
            addAndDrawPlace(name.get(), event.getX(), event.getY());
            isChanged = true;
          } else {
            alertError("\"" + name.get() + "\"" + " already exists!");
          }
        } else {
          alertError("Name can't be empty!");
        }
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
        String place1 = circleToPlace.get(selectedPlace1);
        String place2 = circleToPlace.get(selectedPlace2);
        if (graph.getEdgeBetween(place1, place2) != null) {
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

  class ShowConnectionHandler implements EventHandler<ActionEvent> {
    @Override
    public void handle(ActionEvent event) {
      if (selectedPlace1 != null && selectedPlace2 != null) {
        String place1 = circleToPlace.get(selectedPlace1);
        String place2 = circleToPlace.get(selectedPlace2);
        if (graph.getEdgeBetween(place1, place2) != null) {
          showConnection(place1, place2);
        } else {
          alertError("No connection between places!");
        }
      } else {
        alertError("Two places must be selected!");
      }
    }
  }

  class ChangeConnectionHandler implements EventHandler<ActionEvent> {
    @Override
    public void handle(ActionEvent event) {
      if (selectedPlace1 != null && selectedPlace2 != null) {
        String place1 = circleToPlace.get(selectedPlace1);
        String place2 = circleToPlace.get(selectedPlace2);
        if (graph.getEdgeBetween(place1, place2) != null) {
          changeConnection(place1, place2);
        } else {
          alertError("No connection between places!");
        }
      } else {
        alertError("Two places must be selected!");
      }
    }
  }

  class FindPathHandler implements EventHandler<ActionEvent> {
    @Override
    public void handle(ActionEvent event) {
      if (selectedPlace1 != null && selectedPlace2 != null) {
        String place1 = circleToPlace.get(selectedPlace1);
        String place2 = circleToPlace.get(selectedPlace2);
        List<Edge<String>> path = graph.getPath(place1, place2);
        if (path != null) {
          pathDialog.setHeaderText(String.format("The path from %s to %s", place1, place2));
          pathText.clear();
          int totalTime = 0;
          for (Edge<String> e : path) {
            pathText.appendText(e.toString() + "\n");
            totalTime += e.getWeight();
          }
          pathText.appendText(Integer.toString(totalTime));
        } else {
          pathText.setText(String.format("No path found from %s to %s", place1, place2));
        }
        pathDialog.show();
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
    circleToPlace.put(place, name);
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
    nameInput.setDisable(false);
    timeInput.setDisable(false);
    nameInput.requestFocus();
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

  private void showConnection(String place1, String place2) {
    connectionDialog.setHeaderText(String.format("Connection from %s to %s.", place1, place2));
    Edge<String> edge = graph.getEdgeBetween(place1, place2);
    timeInput.setText(Integer.toString(edge.getWeight()));
    nameInput.setText(edge.getName());
    nameInput.setDisable(true);
    timeInput.setDisable(true);
    connectionDialog.show();
  }

  private void changeConnection(String place1, String place2) {
    connectionDialog.setHeaderText(String.format("Change connection from %s to %s.", place1, place2));
    Edge<String> edge = graph.getEdgeBetween(place1, place2);
    timeInput.setText(Integer.toString(edge.getWeight()));
    nameInput.setText(edge.getName());
    nameInput.setDisable(true);
    timeInput.setDisable(false);

    Optional<Boolean> result = connectionDialog.showAndWait();
    if (result.isPresent() && result.get()) {
      int time = -1;
      try {
        time = Integer.parseInt(timeInput.getText());
        if (time > -1) {
          edge.setWeight(time);
          graph.getEdgeBetween(place2, place1).setWeight(time);
          isChanged = true;
        }
        else alertError("Time must be positive integer");
      } catch (NumberFormatException e) {
        alertError("Invalid value for time");
      }
    }
  }

  private void addAndDrawConnection(String place1, String place2, String name, int weight) {
    if (graph.getEdgeBetween(place1, place2) == null) graph.connect(place1, place2, name, weight);
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