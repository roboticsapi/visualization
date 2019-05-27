/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/. 
 *
 * Copyright 2010-2017 ISSE, University of Augsburg 
 */

package org.roboticsapi.feature.visualization.viewer.javafx.nodes;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.imageio.ImageIO;

import org.roboticsapi.feature.visualization.RAPILogger;
import org.roboticsapi.feature.visualization.rmi.RmiVisualizationClientAccepter;
import org.roboticsapi.feature.visualization.viewer.javafx.Configuration;
import org.roboticsapi.feature.visualization.viewer.javafx.lookup.Application;
import org.roboticsapi.feature.visualization.viewer.javafx.lookup.FxLookupClient;
import org.roboticsapi.feature.visualization.viewer.javafx.lookup.LookupListener;
import org.roboticsapi.feature.visualization.viewer.javafx.model.RemoteSceneImpl;
import org.roboticsapi.feature.visualization.viewer.javafx.model.RmiRemoteSceneFactoryImpl;
import org.roboticsapi.feature.visualization.viewer.javafx.nodes.ConnectionOverviewInterface.AppID;
import org.roboticsapi.feature.visualization.viewer.javafx.nodes.ConnectionOverviewInterface.ControlListener;
import org.roboticsapi.feature.visualization.viewer.javafx.nodes.ConnectionOverviewInterface.ControlListener.SelectionItem;
import org.roboticsapi.feature.visualization.viewer.javafx.nodes.ConnectionOverviewInterface.HostID;
import org.roboticsapi.feature.visualization.viewer.javafx.nodes.ConnectionOverviewInterface.SceneID;
import org.roboticsapi.feature.visualization.viewer.javafx.nodes.ConnectionOverviewInterface.UnknownIdentifierException;

import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.BorderPane;

@SuppressWarnings("restriction")
public class RapiVisualizationPane extends BorderPane {

	public enum DISPLAY_MODE {
		VERTICAL_SPLIT // TODO:
	}

	private static final int LOOKUP_PORT = 4320;

	private final FrameTreePane frameTree;
	private final SplitPane splitPane;
	private final ConnectionOverviewPane connectorPane;
	private final Map<SceneID, VisualizationPane> visualizationPanes = new HashMap<>();
	private final Controller controller;
	private SelectionItem[] oldSelection = new SelectionItem[0];
	private String recordPath = null;

	private final Configuration configuration;

	private AppID initialAutoconnect = null;

	public RapiVisualizationPane(DISPLAY_MODE displayMode, Configuration configuration) {
		// TODO: layout gemäß displayMode
		this.configuration = configuration;

		controller = new Controller();
		connectorPane = new ConnectionOverviewPane(controller);
		frameTree = new FrameTreePane();
		splitPane = new SplitPane(connectorPane);
		setCenter(splitPane);

		for (String host : configuration.getObservedHosts()) {
			connectorPane.addServer(host, false);
		}
	}

	public void shutdown() {
		controller.shutdown();
	}

	private class Controller implements ControlListener {
		private final Map<HostID, String> hostNames = new LinkedHashMap<>();
		private final Map<HostID, HostRapiMonitor> hostRapiMonitors = new HashMap<>();
		private final Map<HostID, FxLookupClient> hostLookupMonitors = new HashMap<>();

		private final Map<AppID, Registry> appConnections = new HashMap<>();
		private final Map<AppID, AppSceneMonitor> appSceneMonitors = new HashMap<>();

		@Override
		public void newServerAdded(HostID hostId, String host) {
			HostRapiMonitor job = new HostRapiMonitor(hostId, connectorPane);
			FxLookupClient lookup = new FxLookupClient(host, LOOKUP_PORT, job);
			hostNames.put(hostId, host);
			hostRapiMonitors.put(hostId, job);
			hostLookupMonitors.put(hostId, lookup);

			configuration.setObservedHosts(hostNames.values());
		}

		@Override
		public void beforeServerRemoved(HostID hostId) {
			hostLookupMonitors.remove(hostId).shutdown();
			hostRapiMonitors.remove(hostId);
			hostNames.remove(hostId);

			configuration.setObservedHosts(hostNames.values());
		}

		public void shutdown() {
			// Verbindungen zu Lookup-Servern schließen
			while (!hostLookupMonitors.isEmpty()) {
				hostLookupMonitors.remove(hostLookupMonitors.keySet().iterator().next()).shutdown();
			}
			// Verbindungen zu Rapi-Instanzen schließen
			while (!appConnections.isEmpty()) {
				try {
					AppID appId = appConnections.keySet().iterator().next();
					appConnections.remove(appId);
					AppSceneMonitor appSceneMonitor = appSceneMonitors.remove(appId);
					AppSceneMonitor.unexportObject(appSceneMonitor, true);
				} catch (Exception e) {
					RAPILogger.logException(getClass(), e);
				}
			}
		}

		@Override
		public void switchAutodiscover(boolean on) {
			// TODO: remove
		}

		private void setThumbnailFromVis(SceneID sceneId, Node content) throws UnknownIdentifierException {
			// Set thumbnail
			WritableImage snapshot = content.snapshot(null, null);
			connectorPane.setSceneThumbnail(sceneId, snapshot);
		}

		private Map<SceneID, RemoteSceneImpl> shownScenes = new HashMap<>();

		@Override
		public void selectionChanged(SelectionItem[] selectedSceneIds) {
			for (SelectionItem oldSel : oldSelection) {
				// Set thumbnail
				SceneID sceneId = oldSel.getSceneId();
				VisualizationPane content = visualizationPanes.get(sceneId);
				frameTree.removeSelectionListener(content);
				if (shownScenes.containsKey(sceneId)) {
					shownScenes.get(sceneId).removeFrameListener(frameTree);
					shownScenes.get(sceneId).setOnUpdated(null);
				}
				shownScenes.remove(sceneId);
				try {
					setThumbnailFromVis(sceneId, content);
				} catch (UnknownIdentifierException e) {
				}
			}

			// Wenn nur eine Szene selektiert: zeige entsprechendes Gui-Element
			// Wenn mehrere Szenen selektiert: zeige neues Gui-Element mit
			// Kamera-Ausrichtung der als erstes ausgewählten szene
			if (selectedSceneIds.length == 0)
				splitPane.setContent(null);
			else if (selectedSceneIds.length == 1) {
				SelectionItem id = selectedSceneIds[0];
				try {
					RemoteSceneImpl scene = appSceneMonitors.get(id.getApplicationId()).getScene(id.getSceneId());
					VisualizationPane content = visualizationPanes.get(selectedSceneIds[0].getSceneId());
					frameTree.addSelectionListener(content);
					// TODO: visualizationPanes wieder löschen, wenn
					// disconnected
					content.setVisualizationNode(scene.getJavaFXScene());
					splitPane.setContent(content);
					shownScenes.put(id.getSceneId(), scene);
					scene.addFrameListener(frameTree);
					// Set thumbnail
					setThumbnailFromVis(id.getSceneId(), content);
					// record scene
					recordSceneIfRequested(scene, content);
				} catch (UnknownIdentifierException e) {
				}
			} else {
				Group collection = new Group();
				for (SelectionItem id : selectedSceneIds) {
					try {
						RemoteSceneImpl scene = appSceneMonitors.get(id.getApplicationId()).getScene(id.getSceneId());
						collection.getChildren().add(scene.getJavaFXScene());
					} catch (UnknownIdentifierException e) {
					}
				}
				VisualizationPane pane = new VisualizationPane(collection, configuration);
				splitPane.setContent(pane);
			}

			oldSelection = selectedSceneIds;
		}

		Executor videoExporter = new ThreadPoolExecutor(0, 4, 2, TimeUnit.SECONDS, new LinkedBlockingQueue<>(20));
		int videoFrame = 0;
		long lastFrame = 0;

		private void recordSceneIfRequested(RemoteSceneImpl scene, VisualizationPane content) {
			scene.setOnUpdated(() -> {
				if (recordPath == null)
					return;

				if (System.currentTimeMillis() > lastFrame + 50) {
					lastFrame = System.currentTimeMillis();

					WritableImage snapshot = content.snapshot(null, null);
					int frame = videoFrame++;
					videoExporter.execute(() -> {
						BufferedImage rawimg = SwingFXUtils.fromFXImage(snapshot, null);
						BufferedImage img = new BufferedImage(rawimg.getWidth(), rawimg.getHeight(),
								BufferedImage.TYPE_3BYTE_BGR);
						img.getGraphics().drawImage(rawimg, 0, 0, null);
						try {

							ImageIO.write(img, "jpg", new File(recordPath, frame + ".jpg"));
						} catch (IOException e) {
							e.printStackTrace();
						}
					});
				}
			});
		}

		private Application getApplication(HostID hostId, AppID appId) throws UnknownIdentifierException {
			return hostRapiMonitors.get(hostId).getApplication(appId);
		}

		@Override
		public void connectionStateRequested(HostID hostId, AppID applicationId, boolean connect) {
			if (connect) {
				// bereits verbunden?
				if (appConnections.containsKey(applicationId))
					return;

				try {
					Registry registry = LocateRegistry.getRegistry(hostNames.get(hostId),
							getApplication(hostId, applicationId).port);
					RmiVisualizationClientAccepter server = (RmiVisualizationClientAccepter) registry
							.lookup(RmiVisualizationClientAccepter.RMI_NAME);
					AppSceneMonitor appFactory = new AppSceneMonitor(applicationId, connectorPane);
					appConnections.put(applicationId, registry);
					appSceneMonitors.put(applicationId, appFactory);
					server.registerRemoteVisualizationClient(appFactory);
				} catch (Exception e) {
					RAPILogger.logException(getClass(), e);
				}
			} else {
				AppSceneMonitor appSceneMonitor = appSceneMonitors.remove(applicationId);
				appConnections.remove(applicationId);
				if (appSceneMonitor == null)
					return;
				try {
					AppSceneMonitor.unexportObject(appSceneMonitor, true);
				} catch (Exception e) {
					RAPILogger.logException(getClass(), e);
				}
				appSceneMonitor.removeAllScenes();
			}
		}

		private class HostRapiMonitor implements LookupListener {

			private final HostID hostId;
			private final ConnectionOverviewPane connector;
			private final Map<Application, AppID> applications = new HashMap<>();

			public HostRapiMonitor(HostID hostId, ConnectionOverviewPane connector) {
				this.hostId = hostId;
				this.connector = connector;
			}

			public Application getApplication(AppID appId) throws UnknownIdentifierException {
				for (Map.Entry<Application, AppID> app : applications.entrySet()) {
					if (app.getValue() == appId)
						return app.getKey();
				}
				throw new UnknownIdentifierException(appId);
			}

			@Override
			public void rapiInstanceAdded(Application info) {
				try {

					AppID appId = connector.addApplication(hostId, info.name);
					applications.put(info, appId);

					// Automatically connect to very first application appearing
					// in list
					if (applications.size() == 1) {
						initialAutoconnect = appId;
						connector.setApplicationActive(appId, true);
					}
				} catch (UnknownIdentifierException e) {
					e.printStackTrace();
				}
			}

			@Override
			public void rapiInstanceRemoved(Application info) {
				try {
					AppID appId = applications.remove(info);
					connector.removeApplication(appId);
				} catch (UnknownIdentifierException e) {
					e.printStackTrace();
				}
			}

			@Override
			public void lookupServerStateChanged(boolean online) {
				// TODO:Nicht verbundene, darunterliegende Rapi-Instanzen
				// (Applications) entfernen

				// Offline markieren
				try {
					connector.setServerState(hostId, online);
				} catch (UnknownIdentifierException e) {
					e.printStackTrace();
				}
			}
		}

		private class AppSceneMonitor extends RmiRemoteSceneFactoryImpl {
			private static final long serialVersionUID = 1L;
			private final AppID appId;
			private final Map<RemoteSceneImpl, SceneID> scenes = new HashMap<>();
			private boolean alive = true;

			public AppSceneMonitor(AppID appId, ConnectionOverviewPane connectionOverviewPane) throws RemoteException {
				super();
				this.appId = appId;
			}

			protected void onSceneAdded(RemoteSceneImpl remoteScene) {
				if (!alive)
					return;
				try {
					SceneID sceneId = connectorPane.addScene(appId, remoteScene.getName(), "");
					scenes.put(remoteScene, sceneId);
					visualizationPanes.put(sceneId, new VisualizationPane(remoteScene.getJavaFXScene(), configuration));
					Platform.runLater(() -> {
						try {
							Group javaFXScene = remoteScene.getJavaFXScene();
							// Scene s = new Scene(javaFXScene,
							// ConnectionOverviewPane.THUMBNAIL_WIDTH,
							// ConnectionOverviewPane.THUMBNAIL_HEIGHT);
							WritableImage snapshot = javaFXScene.snapshot(null, null);
							connectorPane.setSceneThumbnail(sceneId, snapshot);
						} catch (UnknownIdentifierException e) {
						}
					});

					// Autoselect very first scene
					if (initialAutoconnect == appId && scenes.size() == 1) {
						Platform.runLater(() -> {
							connectorPane.setSelection(new SceneID[] { sceneId });
						});
					}
				} catch (Exception e) {
					RAPILogger.logException(getClass(), e);
				}
			}

			protected void onSceneRemoved(RemoteSceneImpl scene) {
				if (!alive)
					return;
				removeScene(scene);
			}

			private void removeScene(RemoteSceneImpl scene) {
				try {
					connectorPane.removeScene(scenes.remove(scene));
				} catch (UnknownIdentifierException e) {
					RAPILogger.logException(getClass(), e);
				}
			}

			public RemoteSceneImpl getScene(SceneID sceneId) throws UnknownIdentifierException {
				for (Map.Entry<RemoteSceneImpl, SceneID> scene : scenes.entrySet()) {
					if (scene.getValue() == sceneId)
						return scene.getKey();
				}
				throw new UnknownIdentifierException(sceneId);
			}

			public void removeAllScenes() {
				alive = false;
				while (!scenes.isEmpty()) {
					removeScene(scenes.keySet().iterator().next());
				}
			}
		}

	}

	public void setRecordPath(String path) {
		this.recordPath = path;
	}

	public void showFrames(boolean selected) {
		if (selected) {
			setRight(frameTree);
		} else {
			frameTree.deselect();
			setRight(null);
		}

	}

}
