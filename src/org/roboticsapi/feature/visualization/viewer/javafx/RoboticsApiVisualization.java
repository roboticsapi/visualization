/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/. 
 *
 * Copyright 2010-2017 ISSE, University of Augsburg 
 */

package org.roboticsapi.feature.visualization.viewer.javafx;

import java.io.File;
import java.util.function.BiFunction;

import org.roboticsapi.feature.visualization.viewer.javafx.Configuration.Action;
import org.roboticsapi.feature.visualization.viewer.javafx.nodes.RapiVisualizationPane;
import org.roboticsapi.feature.visualization.viewer.javafx.view.CameraProperties;
import org.roboticsapi.feature.visualization.viewer.javafx.view.CameraProperties.CameraPosition;
import org.roboticsapi.feature.visualization.viewer.navigation.event.EventListener.KeyButton;
import org.roboticsapi.feature.visualization.viewer.navigation.event.EventListener.MouseButton;
import org.roboticsapi.feature.visualization.viewer.navigation.javafx.JavaFXEventManager;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValueBase;
import javafx.event.Event;
import javafx.geometry.BoundingBox;
import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TabPane.TabClosingPolicy;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.stage.DirectoryChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class RoboticsApiVisualization extends Application {

	public static void main(String[] args) {
		launch();
	}

	@Override
	public void start(Stage primaryStage) throws Exception {
		Configuration configuration = new Configuration(new File("rapivis.config"));

		RapiVisualizationPane viewer = new RapiVisualizationPane(RapiVisualizationPane.DISPLAY_MODE.VERTICAL_SPLIT,
				configuration);
		BorderPane borderPane = new BorderPane(viewer);

		borderPane.setTop(createMenuBar(primaryStage, viewer, configuration));
		Scene scene = new Scene(borderPane, 1200, 700);

		primaryStage.setTitle("Robotics API Visualization");
		primaryStage.setScene(scene);
		primaryStage.getIcons().addAll(new Image(getClass().getResourceAsStream("icon_16.png")),
				new Image(getClass().getResourceAsStream("icon_32.png")));
		primaryStage.show();

		Bounds bounds = configuration.getWindowBounds();
		if (bounds != null) {
			primaryStage.setX(bounds.getMinX());
			primaryStage.setY(bounds.getMinY());
			primaryStage.setWidth(bounds.getWidth());
			primaryStage.setHeight(bounds.getHeight());
		}
		primaryStage.setMaximized(configuration.getWindowMaximized());

		primaryStage.setOnCloseRequest(e -> {
			if (primaryStage.isIconified())
				return;
			configuration.setWindowBounds(new BoundingBox(primaryStage.getX(), primaryStage.getY(),
					primaryStage.getWidth(), primaryStage.getHeight()));
			configuration.setWindowMaximized(primaryStage.isMaximized());
			// viewer.shutdown(); //TODO:
			System.exit(0);
		});
	}

	private Node createMenuBar(Stage parentStage, RapiVisualizationPane viewer, Configuration configuration) {
		MenuBar menuBar = new MenuBar();

		Menu menuFile = new Menu("File");
		MenuItem exit = new MenuItem("Exit");
		exit.setOnAction(e -> {
			Platform.exit();
		});
		menuFile.getItems().addAll(exit);
		Menu window = new Menu("Window");
		MenuItem preferences = new MenuItem("Preferences");
		preferences.setOnAction(e -> {
			showPreferencesPage(parentStage, configuration);
		});

		CheckMenuItem frames = new CheckMenuItem("Show Frames");
		frames.setOnAction(e -> {
			viewer.showFrames(frames.isSelected());
		});

		MenuItem record = new MenuItem("Record View...");
		record.setOnAction(e -> {
			DirectoryChooser chooser = new DirectoryChooser();
			File path = chooser.showDialog(parentStage);
			viewer.setRecordPath(path == null ? null : path.getAbsolutePath());
		});
		window.getItems().addAll(preferences, frames, record);

		Menu help = new Menu("Help");
		MenuItem about = new MenuItem("About...");
		about.setOnAction(e -> {
			getHostServices().showDocument("https://www.roboticsapi.org");
		});
		help.getItems().addAll(about);

		menuBar.getMenus().addAll(menuFile, window, help);
		return menuBar;
	}

	private void showPreferencesPage(Stage parentStage, Configuration configuration) {
		Stage dialog = new Stage();
		dialog.initOwner(parentStage);
		dialog.initModality(Modality.APPLICATION_MODAL);
		dialog.setTitle("Preferences");
		TabPane tabPane = new TabPane();
		tabPane.setTabClosingPolicy(TabClosingPolicy.UNAVAILABLE);

		Tab keyMappingTab = new Tab("action mapping", createKeyMappingPage(dialog, configuration));

		Tab navigationSettingsTab = new Tab("navigation settings", createcameraSettingsPage(dialog, configuration));

		Tab lightSettingsTab = new Tab("light", new BorderPane());

		tabPane.getTabs().addAll(keyMappingTab, navigationSettingsTab, lightSettingsTab);

		dialog.setScene(new Scene(tabPane, 400, 500));
		dialog.getIcons().addAll(parentStage.getIcons());
		dialog.showAndWait();
	}

	private Node createKeyMappingPage(Stage parentStage, Configuration configuration) {
		TableView<Mapping> table = new TableView<>();
		table.setEditable(true);

		TableColumn<Mapping, String> descriptionCol = new TableColumn<>("description");
		descriptionCol.setCellValueFactory(param -> param.getValue().descriptionProperty);

		TableColumn<Mapping, Mapping> keyCol = new TableColumn<>("action");
		keyCol.setCellValueFactory(param -> param.getValue());
		keyCol.setCellFactory(param -> new TableCell<Mapping, Mapping>() {
			private final Label label = new Label();

			protected void updateItem(Mapping item, boolean empty) {
				if (empty) {
					setGraphic(null);
					return;
				}
				if (item instanceof KeyMapping) {
					label.textProperty().bind(item.keyProperty);
					setGraphic(label);
				} else if (item instanceof MouseMapping) {
					ComboBox<MouseButton> box = new ComboBox<>();
					box.getItems().setAll(MouseButton.values());
					box.getSelectionModel().select(configuration.getMouseButton(item.action));
					box.setOnAction(event -> {
						MouseButton newMouseButton = box.getSelectionModel().getSelectedItem();
						configuration.setMouseButton(item.action, newMouseButton);
						item.keyProperty.setValue(newMouseButton.name());
					});
					setGraphic(box);
				} else {
					setGraphic(null);
				}
			}
		});
		TableColumn<Mapping, Mapping> teachCol = new TableColumn<>("");
		teachCol.setCellValueFactory(param -> param.getValue());
		teachCol.setCellFactory(param -> new TableCell<Mapping, Mapping>() {
			protected void updateItem(Mapping item, boolean empty) {
				if (empty) {
					setGraphic(null);
					return;
				}
				if (item instanceof KeyMapping) {
					Button button = new Button("...");
					button.setOnAction(e -> {
						showChangeInputKeyDialog(item, parentStage, configuration);
					});
					setGraphic(button);
				} else {
					setGraphic(null);
				}
			}
		});

		table.getColumns().add(descriptionCol);
		table.getColumns().add(keyCol);
		table.getColumns().add(teachCol);

		table.getItems().add(new KeyMapping(configuration, Action.MOVE_FORWARD));
		table.getItems().add(new KeyMapping(configuration, Action.MOVE_BACKWARD));
		table.getItems().add(new KeyMapping(configuration, Action.MOVE_LEFT));
		table.getItems().add(new KeyMapping(configuration, Action.MOVE_RIGHT));
		table.getItems().add(new KeyMapping(configuration, Action.MOVE_UP));
		table.getItems().add(new KeyMapping(configuration, Action.MOVE_DOWN));

		table.getItems().add(new MouseMapping(configuration, Action.ROTATE_LOOK));
		table.getItems().add(new MouseMapping(configuration, Action.ROTATE_ORBIT));

		return table;
	}

	private void showChangeInputKeyDialog(Mapping item, Stage parentStage, Configuration configuration) {
		Stage dialog = new Stage();
		dialog.initOwner(parentStage);
		dialog.initModality(Modality.APPLICATION_MODAL);
		dialog.setTitle("");
		Label label = new Label("Please press any action...");
		label.setPadding(new Insets(10, 20, 10, 20));
		dialog.setScene(new Scene(label, 200, 60));
		dialog.getIcons().addAll(parentStage.getIcons());

		configuration.setKeyButton(item.action, null);
		item.keyProperty.setValue("");

		dialog.addEventFilter(Event.ANY, (event) -> {
			if (event.getEventType() != KeyEvent.KEY_PRESSED)
				return;
			KeyButton button = JavaFXEventManager.convertJavafXKeyCodeToKeyButton(((KeyEvent) event).getCode());
			if (button == null)
				return;
			configuration.setKeyButton(item.action, button);
			item.keyProperty.setValue(button.name());
			dialog.close();
		});
		dialog.showAndWait();

	};

	private static abstract class Mapping extends ObservableValueBase<Mapping> {

		private final StringProperty keyProperty;
		private final Action action;
		private final StringProperty descriptionProperty;

		public Mapping(Configuration configuration, Action action, String buttonName) {
			this.action = action;
			descriptionProperty = new SimpleStringProperty(action.name());
			keyProperty = new SimpleStringProperty(buttonName);
		}

		@Override
		public final Mapping getValue() {
			return this;
		}
	}

	private static class KeyMapping extends Mapping {
		public KeyMapping(Configuration configuration, Action action) {
			super(configuration, action, getKeyButtonName(configuration, action));
		}

		private static String getKeyButtonName(Configuration configuration, Action action) {
			KeyButton keyButton = configuration.getKeyButton(action);
			return keyButton == null ? null : keyButton.name();
		}
	}

	private static class MouseMapping extends Mapping {
		public MouseMapping(Configuration configuration, Action action) {
			super(configuration, action, getMouseButtonName(configuration, action));
		}

		private static String getMouseButtonName(Configuration configuration, Action action) {
			MouseButton mouseButton = configuration.getMouseButton(action);
			return mouseButton == null ? null : mouseButton.name();
		}
	}

	private Node createcameraSettingsPage(Stage parentStage, CameraProperties configuration) {
		TableView<CameraCoord> table = new TableView<>();
		table.setEditable(true);

		TableColumn<CameraCoord, String> descriptionCol = new TableColumn<>("description");
		descriptionCol.setCellValueFactory(param -> param.getValue().description);

		TableColumn<CameraCoord, CameraCoord> keyCol = new TableColumn<>("value");
		keyCol.setCellValueFactory(param -> param.getValue());
		keyCol.setCellFactory(param -> new TableCell<CameraCoord, CameraCoord>() {
			protected void updateItem(CameraCoord item, boolean empty) {
				if (empty) {
					setGraphic(null);
					return;
				}
				TextField textField = new TextField("" + item.value);
				SimpleStringProperty previousText = new SimpleStringProperty(textField.getText());
				textField.focusedProperty().addListener((ChangeListener<Boolean>) (observable, oldValue, newValue) -> {
					if (newValue) {
						previousText.set(textField.getText());
					} else {
						// focus lost!
						try {
							double value = Double.valueOf(textField.getText());
							configuration.setCameraStartposition(item.convert(value));
						} catch (Exception e) {
							textField.setText(previousText.get());
						}
					}
				});
				setGraphic(textField);
			}
		});

		table.getColumns().add(descriptionCol);
		table.getColumns().add(keyCol);

		CameraPosition cp = configuration.getCameraStartposition();
		table.getItems().add(new CameraCoord(configuration, "X:", cp.x, (c, v) -> {
			return new CameraPosition(v, c.y, c.z, c.yaw, c.pitch);
		}));
		table.getItems().add(new CameraCoord(configuration, "Y:", cp.y, (c, v) -> {
			return new CameraPosition(c.x, v, c.z, c.yaw, c.pitch);
		}));
		table.getItems().add(new CameraCoord(configuration, "Z:", cp.z, (c, v) -> {
			return new CameraPosition(c.x, c.y, v, c.yaw, c.pitch);
		}));
		table.getItems().add(new CameraCoord(configuration, "YAW:", Math.toDegrees(cp.yaw), (c, v) -> {
			return new CameraPosition(c.x, c.y, c.z, Math.toRadians(v), c.pitch);
		}));
		table.getItems().add(new CameraCoord(configuration, "PITCH:", Math.toDegrees(cp.pitch), (c, v) -> {
			return new CameraPosition(c.x, c.y, c.z, c.yaw, Math.toRadians(v));
		}));

		return table;
	}

	private class CameraCoord extends ObservableValueBase<CameraCoord> {
		private final CameraProperties configuration;
		public final SimpleStringProperty description;
		public final double value;
		private final BiFunction<CameraPosition, Double, CameraPosition> fun;

		public CameraCoord(CameraProperties configuration, String description, double value,
				BiFunction<CameraPosition, Double, CameraPosition> fun) {
			this.configuration = configuration;
			this.description = new SimpleStringProperty(description);
			this.value = value;
			this.fun = fun;
		}

		public CameraPosition convert(double value) {
			return fun.apply(configuration.getCameraStartposition(), value);
		}

		@Override
		public CameraCoord getValue() {
			return this;
		}
	}

}
