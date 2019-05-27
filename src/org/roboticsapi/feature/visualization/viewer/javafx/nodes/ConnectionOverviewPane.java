/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/. 
 *
 * Copyright 2010-2017 ISSE, University of Augsburg 
 */

package org.roboticsapi.feature.visualization.viewer.javafx.nodes;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.roboticsapi.feature.visualization.viewer.javafx.nodes.ConnectionOverviewInterface.ControlListener.SelectionItem;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.BoundingBox;
import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.BorderStroke;
import javafx.scene.layout.BorderStrokeStyle;
import javafx.scene.layout.BorderWidths;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.Stage;

@SuppressWarnings("restriction")
public class ConnectionOverviewPane extends BorderPane implements ConnectionOverviewInterface {

	public static class MyApp extends Application implements ControlListener {
		private ConnectionOverviewPane viewer;

		@Override
		public void start(Stage primaryStage) throws Exception {
			viewer = new ConnectionOverviewPane(this);
			Scene scene = new Scene(viewer, 300, 400);
			primaryStage.setOnCloseRequest(h -> System.exit(0));
			primaryStage.setTitle("Robotics API JavaFX Visualization");
			primaryStage.setScene(scene);
			primaryStage.show();

			viewer.addServer("localhost", false);

			HostID id2 = viewer.addServer("137.250.170.54", false);
			viewer.setServerState(id2, true);
			AppID app1 = viewer.addApplication(id2, "Application 1");
			SceneID sc2_1_1 = viewer.addScene(app1, "Welt", "Scene 1");
			viewer.addScene(app1, "Youbot", "Scene 2");
			AppID app2 = viewer.addApplication(id2, "Application 2");
			viewer.addScene(app2, "Welt", "Scene 1");
			SceneID sc2_2_2 = viewer.addScene(app2, "Youbot", "Scene 2");
			SceneID sc2_2_3 = viewer.addScene(app2, "Quadcopter", "Scene 3");

			HostID id3 = viewer.addServer("137.250.170.101", false);
			AppID app3_1 = viewer.addApplication(id3, "Application 1");
			viewer.addScene(app3_1, "Welt", "Scene 1");
			SceneID sc3_1_2 = viewer.addScene(app3_1, "Quadcopter", "Scene 2");

			viewer.setSelection(new SceneID[] { sc2_1_1, sc2_2_2, sc2_2_3, sc3_1_2 });
		}

		@Override
		public void newServerAdded(HostID hostId, String host) {
			new Thread(new Runnable() {
				@Override
				public void run() {
					try {
						Thread.sleep((long) (1000 * Math.random()));
						viewer.setServerState(hostId, true);
						Thread.sleep((long) (2000 * Math.random()));
						AppID app1 = viewer.addApplication(hostId, "Application 1");
						viewer.addScene(app1, "Welt", "Scene 1");
						Thread.sleep((long) (500 * Math.random()));
						viewer.addScene(app1, "Youbot", "Scene 2");
						Thread.sleep((long) (100 * Math.random()));
						AppID app2 = viewer.addApplication(hostId, "Application 2");
						Thread.sleep((long) (500 * Math.random()));
						viewer.addScene(app2, "Welt", "Scene 1");
						viewer.addScene(app2, "Quadcopter", "Scene 2");
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}).start();
			System.out.println("Server added: " + host);
		}

		@Override
		public void beforeServerRemoved(HostID serverId) {
			System.out.println("Server removed");
		}

		@Override
		public void connectionStateRequested(HostID hostId, AppID applicationId, boolean connect) {
			System.out.println("Connection state requested: " + connect + " für " + applicationId);
		}

		@Override
		public void switchAutodiscover(boolean on) {
			System.out.println("Autodiscover " + (on ? "" : "de") + "activated");
		}

		@Override
		public void selectionChanged(SelectionItem[] selectedSceneIds) {
			System.out.println("Selection changed: " + Arrays.toString(selectedSceneIds));
		}
	}

	public static void main(String[] args) {
		Application.launch(MyApp.class);
	}

	private final ControlListener controlListener;
	private final ScrollPane scrollPane;
	private final VBox serverPane;
	private final Map<HostID, HostTab> hostTabs = new HashMap<>();

	public static final int THUMBNAIL_WIDTH = 48;
	public static final int THUMBNAIL_HEIGHT = 36;

	public ConnectionOverviewPane(ControlListener controlListener) {
		this.controlListener = controlListener;
		TextField serverTextField = new TextField();
		serverTextField.setPromptText("IP or server name");
		Button addServerButton = new Button("Add");
		serverTextField.textProperty().addListener((observable, oldValue, newValue) -> {
			if ("".equals(newValue))
				addServerButton.setDisable(true);
			else
				addServerButton.setDisable(false);
			// TODO: hostname oder IP überprüfen
		});
		serverTextField.setOnAction(a -> {
			a.consume();
			if (addServerButton.isDisabled())
				return;
			String serverIp = serverTextField.getText();
			serverTextField.clear();
			addServer(serverIp, true);
		});
		serverTextField.setText(" "); // enforce real change...
		serverTextField.setText("");
		addServerButton.setOnAction(a -> {
			String serverIp = serverTextField.getText();
			serverTextField.clear();
			addServer(serverIp, true);
		});
		HBox newServerPane = new HBox(10, serverTextField, addServerButton);
		HBox.setHgrow(serverTextField, Priority.ALWAYS);
		newServerPane.setPadding(new Insets(10, 10, 10, 10));
		this.setTop(newServerPane);

		serverPane = new VBox();
		scrollPane = new ScrollPane(serverPane);
		scrollPane.setFitToHeight(true);
		scrollPane.setFitToWidth(true);
		this.setCenter(scrollPane);
		addEventFilter(KeyEvent.ANY, (event) -> {
			if (event.getCode() == KeyCode.DOWN) {
				if (event.getEventType() == KeyEvent.KEY_PRESSED) {
					manageSelection(getNextSceneId(focussedScene), event.isShiftDown(), event.isControlDown());
				}
				event.consume();
			} else if (event.getCode() == KeyCode.UP) {
				if (event.getEventType() == KeyEvent.KEY_PRESSED) {
					manageSelection(getPreviousSceneId(focussedScene), event.isShiftDown(), event.isControlDown());
				}
				event.consume();
			}
		});
	}

	private interface SelectEventHandler {
		public void manageSelection(SceneID sceneId, boolean isShiftDown, boolean isControlDown);
	}

	private class HostTab extends TitledPane {
		private final HostID hostId;
		private final ImageView online, offline;
		private final BorderPane onlineStatePane;
		private final VBox contentPane;
		private final Map<AppID, ApplicationTab> applicationTabs = new HashMap<>();
		private final SelectEventHandler selectEventHandler;

		public HostTab(HostID hostId, String hostName, Runnable closeCallback, SelectEventHandler selectEventHandler) {
			this.hostId = hostId;
			this.selectEventHandler = selectEventHandler;
			setText(null);

			contentPane = new VBox(0);
			contentPane.setPadding(new Insets(0, 0, 0, 0));
			setContent(contentPane);

			this.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
			this.getStyleClass().add("hostPane");
			this.setAnimated(false);

			Label label = new Label(hostName);
			label.setStyle("-fx-text-fill: white;");
			Pane region = new Pane();
			HBox.setHgrow(region, Priority.ALWAYS);
			Button closeButton = new Button("",
					new ImageView(new Image(this.getClass().getResourceAsStream("trash.png"), 0, 0, true, true)));
			closeButton.setOnAction(l -> executeSafely(() -> closeCallback.run()));

			offline = new ImageView(new Image(this.getClass().getResourceAsStream("offline.png"), 0, 0, true, true));
			online = new ImageView(new Image(this.getClass().getResourceAsStream("online.png"), 0, 0, true, true));

			onlineStatePane = new BorderPane();

			HBox hbox = new HBox(10, onlineStatePane, label, region, closeButton);
			hbox.setAlignment(Pos.CENTER);
			this.setGraphic(hbox);

			hbox.minWidthProperty().bind(this.widthProperty().subtract(50));
			this.getStylesheets().add(this.getClass().getResource("ConnectionOverviewPaneStyle.css").toExternalForm());

			setServerState(false);
		}

		public void setServerState(boolean online) {
			executeAsFxThread(() -> onlineStatePane.setCenter(online ? this.online : offline));
		}

		public AppID addApplication(String name) {
			name = name.trim();
			AppID identifier = new AppID(name);
			ApplicationTab applicationTab = new ApplicationTab(identifier, name, selectEventHandler);
			applicationTabs.put(identifier, applicationTab);
			applicationTab.expandedProperty().addListener((observable, oldValue, newValue) -> {
				executeSafely(() -> controlListener.connectionStateRequested(hostId, identifier, newValue));
				checkSelection();
			});
			executeAsFxThread(() -> contentPane.getChildren().add(applicationTab));
			return identifier;
		}

		public void removeApplication(AppID appId) throws UnknownIdentifierException {
			ApplicationTab applicationTab = applicationTabs.get(appId);
			if (applicationTab == null)
				throw new UnknownIdentifierException(appId);
			executeSafely(() -> controlListener.connectionStateRequested(hostId, appId, false));
			applicationTabs.remove(appId);
			executeAsFxThread(() -> contentPane.getChildren().remove(applicationTab));
			checkSelection();
		}

		public ApplicationTab getApplicationTab(AppID appId) throws UnknownIdentifierException {
			ApplicationTab applicationTab = applicationTabs.get(appId);
			if (applicationTab == null)
				throw new UnknownIdentifierException(appId);
			return applicationTab;
		}

		public ApplicationTab getApplicationTab(SceneID identifier) throws UnknownIdentifierException {
			for (ApplicationTab applicationTab : applicationTabs.values()) {
				try {
					applicationTab.getSceneTab(identifier);
					return applicationTab;
				} catch (UnknownIdentifierException e) {
				}
			}
			throw new UnknownIdentifierException(identifier);
		}

		public HostID getHostId() {
			return hostId;
		}

	}

	private class ApplicationTab extends TitledPane {

		private final Map<SceneID, SceneTab> sceneTabs = new HashMap<>();
		private final VBox contentPane;
		private final SelectEventHandler selectEventHandler;
		private final AppID appId;

		public ApplicationTab(AppID appId, String applicationName, SelectEventHandler selectEventHandler) {
			this.appId = appId;
			this.selectEventHandler = selectEventHandler;
			setText(applicationName);
			contentPane = new VBox(0);
			contentPane.setPadding(new Insets(0, 0, 0, 0));
			setContent(contentPane);
			this.setAnimated(false);
			this.setExpanded(false);
			this.getStyleClass().add("applicationPane");
		}

		public SceneID addScene(String name, String description) {
			name = name.trim();
			SceneID identifier = new SceneID(name);
			SceneTab sceneTab = new SceneTab(name, description, (i, isShiftDown, isControlDown) -> selectEventHandler
					.manageSelection(identifier, isShiftDown, isControlDown));
			sceneTabs.put(identifier, sceneTab);
			executeAsFxThread(() -> contentPane.getChildren().add(sceneTab));
			return identifier;
		}

		public void removeScene(SceneID identifier) throws UnknownIdentifierException {
			SceneTab sceneTab = sceneTabs.remove(identifier);
			if (sceneTab == null)
				throw new UnknownIdentifierException(identifier);
			executeAsFxThread(() -> contentPane.getChildren().remove(sceneTab));
			checkSelection();
		}

		public SceneTab getSceneTab(SceneID identifier) throws UnknownIdentifierException {
			SceneTab sceneTab = sceneTabs.get(identifier);
			if (sceneTab == null)
				throw new UnknownIdentifierException(identifier);
			return sceneTab;
		}

		public void setConnectionState(boolean connected) {
			if (!connected) {
				executeAsFxThread(() -> setExpanded(false));
			}
			// TODO Auto-generated method stub

		}

		public AppID getApplicationId() {
			return appId;
		}
	}

	private class SceneTab extends HBox {
		private final BorderPane imagePane = new BorderPane();

		public SceneTab(String sceneName, String description, SelectEventHandler selectEventHandler) {
			super(20);
			setCursor(Cursor.HAND);
			imagePane.setMinSize(THUMBNAIL_WIDTH + 2, THUMBNAIL_HEIGHT + 2);
			imagePane.setMaxSize(THUMBNAIL_WIDTH + 2, THUMBNAIL_HEIGHT + 2);
			imagePane.setPadding(new Insets(1));
			imagePane.setStyle("-fx-background-color:#ffffff;");
			imagePane.setBorder(new Border(
					new BorderStroke(null, BorderStrokeStyle.SOLID, CornerRadii.EMPTY, BorderWidths.DEFAULT)));
			Label desc = new Label("(" + description + ")");
			desc.setFont(Font.font(desc.getFont().getSize() * 0.8));
			getChildren().addAll(imagePane, new VBox(0, new Label(sceneName), desc));
			setPadding(new Insets(8, 10, 8, 25));
			setOnMousePressed(mouseEvent -> {
				selectEventHandler.manageSelection(null, mouseEvent.isShiftDown(), mouseEvent.isControlDown());
			});
			setOnTouchPressed(touchEvent -> {
				selectEventHandler.manageSelection(null, touchEvent.isShiftDown(), touchEvent.isControlDown());
			});
		}

		public void setThumbnail(final Image thumbnail) {
			executeAsFxThread(() -> {
				if (thumbnail == null) {
					imagePane.setCenter(null);
					return;
				}

				final ImageView imageView = new ImageView(thumbnail);
				imageView.setPreserveRatio(true);
				imageView.setSmooth(true);
				imageView.setFitWidth(THUMBNAIL_WIDTH);
				imageView.setFitHeight(THUMBNAIL_HEIGHT);

				imagePane.setCenter(imageView);
			});
		}

		public void setSelected(boolean selected) {
			executeAsFxThread(() -> setStyle(
					"-fx-background-color:" + (selected ? "rgb(225,225,225)" : "rgb(255,255,255)") + ";"));
		}
	}

	@Override
	public HostID addServer(String host, boolean autoscroll) {
		host = host.trim();
		HostID identifier = new HostID(host);
		HostTab serverNode = new HostTab(identifier, host, () -> executeSafely(() -> removeServer(identifier)),
				this::manageSelection);
		hostTabs.put(identifier, serverNode);
		final String myhost = host;
		executeSafely(() -> controlListener.newServerAdded(identifier, myhost));
		executeAsFxThread(() -> serverPane.getChildren().add(serverNode));
		// TODO: autoscroll!!
		return identifier;
	}

	private HostTab getHostTab(HostID identifier) throws UnknownIdentifierException {
		HostTab hostTab = hostTabs.get(identifier);
		if (hostTab == null)
			throw new UnknownIdentifierException(identifier);
		return hostTab;
	}

	private HostTab getHostTab(AppID identifier) throws UnknownIdentifierException {
		for (HostTab t : hostTabs.values()) {
			try {
				t.getApplicationTab(identifier);
				return t;
			} catch (UnknownIdentifierException e) {
			}
		}
		throw new UnknownIdentifierException(identifier);
	}

	private ApplicationTab getApplicationTab(AppID identifier) throws UnknownIdentifierException {
		return getHostTab(identifier).getApplicationTab(identifier);
	}

	private ApplicationTab getApplicationTab(SceneID identifier) throws UnknownIdentifierException {
		for (HostTab t : hostTabs.values()) {
			try {
				return t.getApplicationTab(identifier);
			} catch (UnknownIdentifierException e) {
			}
		}
		throw new UnknownIdentifierException(identifier);
	}

	private SceneTab getSceneTab(SceneID identifier) throws UnknownIdentifierException {
		return getApplicationTab(identifier).getSceneTab(identifier);
	}

	@Override
	public void setApplicationActive(AppID appId, boolean active) throws UnknownIdentifierException {
		getApplicationTab(appId).setExpanded(active);
	}

	@Override
	public void setServerState(HostID hostId, boolean online) throws UnknownIdentifierException {
		getHostTab(hostId).setServerState(online);
	}

	@Override
	public AppID addApplication(HostID hostId, String name) throws UnknownIdentifierException {
		return getHostTab(hostId).addApplication(name);
	}

	@Override
	public void setConnectionState(AppID applicationId, boolean connected) throws UnknownIdentifierException {
		getApplicationTab(applicationId).setConnectionState(connected);
	}

	@Override
	public SceneID addScene(AppID applicationId, String name, String description) throws UnknownIdentifierException {
		return getApplicationTab(applicationId).addScene(name, description);
	}

	@Override
	public void setSceneThumbnail(SceneID sceneId, Image thumbnail) throws UnknownIdentifierException {
		getSceneTab(sceneId).setThumbnail(thumbnail);
	}

	private void checkSelection() {
		setSelection(currentSelection);
	}

	private SceneID[] currentSelection = new SceneID[0];

	private static final boolean sameContent(Object[] o1, Object[] o2) {
		if (o1.length != o2.length)
			return false;
		List<Object> buffer = new ArrayList<>();
		for (Object o : o2)
			buffer.add(o);

		for (Object o : o1) {
			if (!buffer.remove(o))
				return false;
		}
		return true;
	}

	@Override
	public void setSelection(SceneID[] sceneIds) {
		executeAsFxThread(() -> {
			Set<SceneID> selection = new HashSet<>();
			for (SceneID id : sceneIds)
				selection.add(id);

			Set<SelectionItem> actualNewSelection = new HashSet<>();
			Set<SceneID> _actualNewSelection = new HashSet<>();

			hostTabs.values().forEach(h -> {
				h.applicationTabs.values().forEach(a -> {
					a.sceneTabs.forEach((i, s) -> {
						boolean selected = selection.remove(i);
						s.setSelected(selected);
						if (selected) {
							SelectionItem sel = new SelectionItem() {
								@Override
								public SceneID getSceneId() {
									return i;
								}

								@Override
								public HostID getHostId() {
									return h.getHostId();
								}

								@Override
								public AppID getApplicationId() {
									return a.getApplicationId();
								}
							};
							actualNewSelection.add(sel);
							_actualNewSelection.add(i);
							executeAsFxThread(() -> {
								h.setExpanded(true);
								a.setExpanded(true);
							});
						}
					});
				});
			});

			SceneID[] _newSelection = _actualNewSelection.toArray(new SceneID[_actualNewSelection.size()]);
			SelectionItem[] newSelection = actualNewSelection.toArray(new SelectionItem[actualNewSelection.size()]);
			if (sameContent(currentSelection, _newSelection)) {
				return;
			}

			currentSelection = _newSelection;
			executeSafely(() -> controlListener.selectionChanged(newSelection));

			// TODO: scroll to selection even if it is bigger than 1
			if (newSelection.length != 1)
				return;
			SceneTab sceneTab;
			try {
				sceneTab = getSceneTab(newSelection[0].getSceneId());

				executeAsFxThread(() -> {
					double height = scrollPane.getContent().getBoundsInLocal().getHeight();
//			        double y = sceneTab.getBoundsInParent().getMaxY();
					Bounds b = localToParentRecursive(sceneTab, (Parent) scrollPane.getContent(),
							sceneTab.getBoundsInLocal());

					double topPoint = b.getMinY();
					double bottomPoint = b.getMaxY();

					double viewportHeight = scrollPane.getViewportBounds().getHeight();
					double currentScrollTop = scrollPane.getVvalue() * (height - viewportHeight);
					sceneTab.requestFocus();

					if (bottomPoint - topPoint > viewportHeight)
						return;
					double newScrollTop = currentScrollTop;
					if (topPoint < currentScrollTop)
						newScrollTop = topPoint;
					if (bottomPoint > currentScrollTop + viewportHeight)
						newScrollTop = bottomPoint - viewportHeight;
					scrollPane.setVvalue(newScrollTop / (height - viewportHeight));
				});
			} catch (UnknownIdentifierException e) {
			}
		});
	}

	private List<SceneID> getSceneList() {
		List<SceneID> result = new ArrayList<>();
		serverPane.getChildren().forEach(n -> {
			HostTab hostTab = (HostTab) n;
			hostTab.contentPane.getChildren().forEach(n2 -> {
				ApplicationTab applicationTab = (ApplicationTab) n2;
				applicationTab.contentPane.getChildren().forEach(n3 -> {
					SceneTab sceneTab = ((SceneTab) n3);
					applicationTab.sceneTabs.forEach((i, s) -> {
						if (sceneTab == s) {
							result.add(i);
						}
					});
				});
			});
		});
		return result;
	}

	private SceneID getNextSceneId(SceneID sceneId) {
		List<SceneID> sceneIds = getSceneList();
		if (sceneId == null) {
			if (sceneIds.size() == 0)
				return null;
			sceneId = sceneIds.get(0);
		}
		int nextIndex = Math.max(0, Math.min(sceneIds.indexOf(sceneId) + 1, sceneIds.size() - 1));
		if (nextIndex > sceneIds.size() - 1)
			return null;
		return sceneIds.get(nextIndex);
	}

	private SceneID getPreviousSceneId(SceneID sceneId) {
		List<SceneID> sceneIds = getSceneList();
		if (sceneId == null) {
			if (sceneIds.size() == 0)
				return null;
			sceneId = sceneIds.get(0);
		}
		int nextIndex = Math.max(0, Math.min(sceneIds.indexOf(sceneId) - 1, sceneIds.size() - 1));
		if (nextIndex > sceneIds.size() - 1)
			return null;
		return sceneIds.get(nextIndex);
	}

	private SceneID focussedScene = null;
	private final List<SceneID> selectedScenes = new ArrayList<>();

	private void manageSelection(SceneID sceneId, boolean isShiftDown, boolean isControlDown) {
		if (sceneId == null)
			return;

		final SceneID newFocussedScene;
		final List<SceneID> newSelectedScenes;

		if (focussedScene == null || (!isShiftDown && !isControlDown)) {
			// Simple change
			newFocussedScene = sceneId;
			newSelectedScenes = new ArrayList<>();
			newSelectedScenes.add(sceneId);

		} else if (!isShiftDown && isControlDown) {
			// Ctrl
			if (focussedScene == sceneId) {
				if (selectedScenes.size() <= 1) {
					// Keine Änderung
					newFocussedScene = focussedScene;
					newSelectedScenes = new ArrayList<>(selectedScenes);
				} else {
					newSelectedScenes = new ArrayList<>(selectedScenes);
					newSelectedScenes.remove(sceneId);
					newFocussedScene = newSelectedScenes.get(0);
				}
			} else {
				newFocussedScene = focussedScene;
				newSelectedScenes = new ArrayList<>(selectedScenes);
				if (newSelectedScenes.contains(sceneId))
					newSelectedScenes.remove(sceneId);
				else
					newSelectedScenes.add(sceneId);
			}

		} else if (isShiftDown && !isControlDown) {
			// Shift
			// TODO:
			newFocussedScene = sceneId;
			newSelectedScenes = new ArrayList<>();
			newSelectedScenes.add(sceneId);

		} else {
			// Shift + Ctrl
			// TODO:
			newFocussedScene = sceneId;
			newSelectedScenes = new ArrayList<>();
			newSelectedScenes.add(sceneId);
		}

		// Gibt es Änderungen?
		boolean change = false;
		l1: {
			if (focussedScene != newFocussedScene) {
				change = true;
				break l1;
			}
			List<SceneID> tmp = new ArrayList<>(selectedScenes);
			for (SceneID s : newSelectedScenes) {
				if (!tmp.remove(s)) {
					change = true;
					break l1;
				}
			}
			if (!tmp.isEmpty())
				change = true;
		}
		if (change) {
			focussedScene = newFocussedScene;
			selectedScenes.clear();
			selectedScenes.addAll(newSelectedScenes);
			setSelection(newSelectedScenes.toArray(new SceneID[newSelectedScenes.size()]));
		}
	}

	@Override
	public void removeServer(HostID serverId) throws UnknownIdentifierException {
		Node n = hostTabs.get(serverId);
		if (n == null)
			throw new UnknownIdentifierException(serverId);
		executeSafely(() -> controlListener.beforeServerRemoved(serverId));
		hostTabs.remove(serverId);
		executeAsFxThread(() -> serverPane.getChildren().remove(n));

	}

	@Override
	public void removeApplication(AppID applicationId) throws UnknownIdentifierException {
		getHostTab(applicationId).removeApplication(applicationId);
	}

	@Override
	public void removeScene(SceneID sceneId) throws UnknownIdentifierException {
		getApplicationTab(sceneId).removeScene(sceneId);
	}

	private interface RunnableWithException {
		public void run() throws Exception;
	}

	private final void executeSafely(RunnableWithException r) {
		try {
			r.run();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void executeAsFxThread(Runnable r) {
		if (Platform.isFxApplicationThread())
			r.run();
		else
			Platform.runLater(() -> r.run());
	}

	/**
	 * Transitively converts the coordinates from the node to an ancestor's
	 * coordinate system.
	 *
	 * @param node     The node the starting coordinates are local to.
	 * @param ancestor The ancestor to map the coordinates to.
	 * @param x        The X of the point to be converted.
	 * @param y        The Y of the point to be converted.
	 * @return The converted coordinates.
	 */
	private static Point2D localToParentRecursive(Node node, Parent ancestor, double x, double y) {
		Point2D p = new Point2D(x, y);
		Node cn = node;
		while (cn != null) {
			if (cn == ancestor) {
				return p;
			}
			p = cn.localToParent(p);
			cn = cn.getParent();
		}
		throw new IllegalStateException("The node is not a descedent of the parent.");
	}

	/**
	 * Transitively converts the coordinates of a bound from the node to an
	 * ancestor's coordinate system.
	 *
	 * @param node     The node the starting coordinates are local to.
	 * @param ancestor The ancestor to map the coordinates to.
	 * @param bounds   The bounds to be converted.
	 * @return The converted bounds.
	 */
	private static Bounds localToParentRecursive(Node node, Parent ancestor, Bounds bounds) {
		Point2D p = localToParentRecursive(node, ancestor, bounds.getMinX(), bounds.getMinY());
		return new BoundingBox(p.getX(), p.getY(), bounds.getWidth(), bounds.getHeight());
	}

}
