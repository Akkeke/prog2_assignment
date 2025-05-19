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
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.util.Optional;

public class Gui extends Application {
  private static final Color BACKGROUND_COLOR = Color.rgb(60, 60, 70);

  private Graph<String> graph;

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

    findPath = new Button("Find Path");
    showConnection = new Button("Show Connection");
    newPlace = new Button("New Place");
    newConnection = new Button("New Connection");
    changeConnection = new Button("Change Connection");
    buttonPane.getChildren().addAll(findPath, showConnection, newPlace, newConnection, changeConnection);
    setButtonsDisable(true);

    newPlace.setOnAction(new NewPlaceHandler());

    Scene scene = new Scene(root, 700, 900);
    stage.setScene(scene);
    stage.show();
  }

  class NewMapHandler implements EventHandler<ActionEvent> {
    @Override
    public void handle(ActionEvent event) {
      File file = fileChooser.showOpenDialog(stage);
      if (file != null) {
        Image image = new Image(file.toURI().toString());
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
      TextInputDialog nameInput = new TextInputDialog("");
      nameInput.setTitle("Name");
      nameInput.setHeaderText("Enter name of the place:");

      Circle dot = new Circle(8, Color.GREEN);
      dot.relocate(event.getX() - dot.getRadius(), event.getY() - dot.getRadius());
      center.getChildren().add(dot);

      Optional<String> name = nameInput.showAndWait();
      if (name.isEmpty() | name.get().isBlank()) {
        center.getChildren().remove(dot);
      } else {
        dot.setFill(Color.RED);
      }
      center.setOnMouseClicked(null);
      center.setCursor(null);
      newPlace.setDisable(false);
    }
  }

  private void setButtonsDisable(boolean value) {
    buttonPane.getChildren().forEach(btn -> btn.setDisable(value));
  }


  public static void main(String[] args) {
    launch(args);
  }
}