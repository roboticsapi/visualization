/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/. 
 *
 * Copyright 2010-2017 ISSE, University of Augsburg 
 */

package org.roboticsapi.feature.visualization.viewer.javafx.model;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.roboticsapi.feature.visualization.RAPILogger;
import org.roboticsapi.feature.visualization.rmi.RmiVisualizationClientScene;

import com.interactivemesh.jfx.importer.col.ColModelImporter;

import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Transform;
import javafx.scene.transform.Translate;

/**
 * Implementation of RemoteScene
 * 
 * @see org.roboticsapi.feature.visualization.VisualizationClientScene
 */
public class RemoteSceneImpl extends UnicastRemoteObject implements RmiVisualizationClientScene, Serializable {

	private static final long serialVersionUID = 1L;

	private long lastCheck = Long.MAX_VALUE;

	private HighlightListener onHighlight = null;
	private List<FrameListener> frameListeners = new ArrayList<>();

	/**
	 * used for the render loop
	 */
	private boolean alive = true;

	private int uid = 0;

	/**
	 * frame graph
	 */
	private Group rootGroup = new Group();
	private final Map<Integer, RapiFrame> frames = new HashMap<>();
	private final Map<Integer, RapiMesh> meshes = new HashMap<>();
	private String sceneName;
	private Runnable onUpdated = null;

	/**
	 * opens tab in JavaFX window and starts render loop
	 * 
	 * @param window    window instance
	 * @param sceneName name will be displayed in tab
	 */
	public RemoteSceneImpl(String sceneName) throws RemoteException {
		super();
		this.sceneName = sceneName;

		frames.put(0, new RapiFrame("World Origin", true));

		// Renderloop
		Thread t = new Thread(() -> {
			while (alive) {
				BooleanProperty finished = new SimpleBooleanProperty(false);
				Platform.runLater(() -> {
					synchronized (RemoteSceneImpl.this) {
						frames.values().forEach(frame -> frame.performUpdate());
					}
					if (rootGroup.getScene() != null && onUpdated != null) {
						try {
							onUpdated.run();
						} catch (Exception e) {
							RAPILogger.logException(this, e);
						}
					}

					finished.set(true);
				});

				while (!finished.get()) {
					if (!alive)
						return;
					try {
						Thread.sleep(10);
					} catch (InterruptedException e) {
					}
				}

				if (System.currentTimeMillis() - lastCheck > 5000)
					deleteScene();
			}
		});
		t.setDaemon(true);
		t.start();
	}

	public void setOnUpdated(Runnable onUpdated) {
		this.onUpdated = onUpdated;
	}

	private final int createUID() {
		uid += 1;
		return uid;
	}

	public Group getJavaFXScene() {
		return rootGroup;
	}

	@Override
	public synchronized boolean hasModel(String modelName) throws RemoteException {
		Path p = Paths.get(System.getProperty("java.io.tmpdir"), modelName + ".dae");
		return Files.exists(p);
	}

	@Override
	public synchronized void uploadModel(String modelName, byte[] modelData, Map<String, byte[]> auxFiles)
			throws RemoteException {
		try {
			Path p = Paths.get(System.getProperty("java.io.tmpdir"), modelName + ".dae");
			OutputStream fos = Files.newOutputStream(p);
			fos.write(modelData);
			fos.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public synchronized int addModel(String name, int parentID, String modelName, double x, double y, double z,
			double a, double b, double c) throws RemoteException {

		RapiFrame parent = frames.get(parentID);
		RapiMesh model = loadMeshFromName(modelName, name, parentID);
		Map<Node, List<Transform>> nodePosition = new HashMap<>();
		for (Node node : model.getNodes()) {
			ArrayList<Transform> transform = new ArrayList<>(node.getTransforms());
			transform.add(0, new Rotate(c, Rotate.X_AXIS));
			transform.add(0, new Rotate(b, Rotate.Y_AXIS));
			transform.add(0, new Rotate(a, Rotate.Z_AXIS));
			transform.add(0, new Translate(x, y, z));
			transform.add(new Rotate(-90, Rotate.X_AXIS));
			nodePosition.put(node, transform);
		}
		updateFrame(parent, model, nodePosition);
		parent.addListener(frame -> updateFrame(frame, model, nodePosition));

		int uid = model.getID();
		meshes.put(uid, model);

		return uid;
	}

	private void updateFrame(RapiFrame frame, RapiMesh model, Map<Node, List<Transform>> nodePosition) {
		Platform.runLater(() -> {
			synchronized (RemoteSceneImpl.this) {
				for (Node node : model.getNodes()) {
					if (frame.hasParent()) {
						node.getTransforms().clear();
						node.getTransforms().addAll(frame.getTransforms());
						node.getTransforms().addAll(nodePosition.get(node));
						node.setVisible(true);
					} else {
						node.setVisible(false);
					}
				}
			}
		});
	}

	private synchronized RapiMesh loadMeshFromName(String modelName, String name, int parentID) {
		ColModelImporter colImporter = new ColModelImporter();
		Path p = Paths.get(System.getProperty("java.io.tmpdir"), modelName + ".dae");
		colImporter.read(new File(p.toUri()));

		RapiMesh model = new RapiMesh(name, colImporter.getImport(), createUID(), parentID);

		Platform.runLater(() -> {
			rootGroup.getChildren().addAll(model.getNodes());
		});

		return model;
	}

	// ignoring scale
	@Override
	public synchronized int addBox(String name, int parentID, double sizeX, double sizeY, double sizeZ, double x,
			double y, double z, double a, double b, double c) throws RemoteException {
		System.out.println(
				"addBox() is not yet implemented! See org.roboticsapi.feature.visualization.viewer.javafx.model.RemoteSceneImpl");
		return 0;
	}

	@Override
	public synchronized int addSphere(String name, int parentID, double radius, double x, double y, double z, double a,
			double b, double c) throws RemoteException {
		System.out.println(
				"addSphere() is not yet implemented! See org.roboticsapi.feature.visualization.viewer.javafx.model.RemoteSceneImpl");
		return 0;
	}

	@Override
	public synchronized int addFrame(String name) throws RemoteException {
		RapiFrame frame = new RapiFrame(name);
		int uid = createUID();
		frames.put(uid, frame);
		for (FrameListener listener : frameListeners)
			listener.frameAdded(frame);
		return uid;
	}

	@Override
	public synchronized void removeFrame(int nodeID) throws RemoteException {
		RapiFrame frame = frames.get(nodeID);
		if (frame != null) {
			for (FrameListener listener : frameListeners)
				listener.frameRemoved(frame);
			frames.remove(nodeID);
		}
		for (Entry<Integer, RapiMesh> e : new HashSet<>(meshes.entrySet())) {
			if (e.getValue().getParentID() == nodeID) {
				int uid = e.getKey();
				removeMesh(e.getValue());
				meshes.remove(uid);
			}
		}
	}

	private void removeMesh(RapiMesh mesh) {
		Platform.runLater(() -> {
			rootGroup.getChildren().removeAll(mesh.getNodes());
		});
	}

	@Override
	public synchronized void addRelation(int from, int to, double x, double y, double z, double a, double b, double c)
			throws RemoteException {
		frames.get(from).addRelation(frames.get(to), x, y, z, a, b, c);
	}

	@Override
	public synchronized void removeRelation(int from, int to) throws RemoteException {
		frames.get(from).removeRelation(frames.get(to));
	}

	@Override
	public synchronized void updateTransformation(int parent, int child, double x, double y, double z, double a,
			double b, double c) throws RemoteException {
		frames.get(parent).updateRelation(frames.get(child), x, y, z, a, b, c);
	}

	@Override
	public synchronized boolean isValid() throws RemoteException {
		lastCheck = System.currentTimeMillis();
		return true;
	}

	@Override
	public synchronized void highlight() throws RemoteException {
		if (onHighlight != null)
			onHighlight.run(this);
	}

	public void setOnHighlight(HighlightListener onHighlight) {
		this.onHighlight = onHighlight;
	}

	public synchronized void addFrameListener(FrameListener listener) {
		frameListeners.add(listener);
		for (RapiFrame frame : frames.values()) {
			listener.frameAdded(frame);
		}
	}

	public synchronized void removeFrameListener(FrameListener listener) {
		frameListeners.remove(listener);
		for (RapiFrame frame : frames.values()) {
			listener.frameRemoved(frame);
		}
	}

	@Override
	public synchronized double getTransparency(int id) throws RemoteException {
		System.out.println(
				"getTransparency()) is not yet implemented! See org.roboticsapi.feature.visualization.viewer.javafx.model.RemoteSceneImpl");
		return 0;
	}

	@Override
	public synchronized double[] getColor(int id) throws RemoteException {
		System.out.println(
				"getColor()) is not yet implemented! See org.roboticsapi.feature.visualization.viewer.javafx.model.RemoteSceneImpl");
		return null;
	}

	@Override
	public synchronized void setTransparency(int id, double value) throws RemoteException {
		System.out.println(
				"setTransparency()) is not yet implemented! See org.roboticsapi.feature.visualization.viewer.javafx.model.RemoteSceneImpl");

	}

	@Override
	public synchronized void setColor(int id, double r, double g, double b) throws RemoteException {
		System.out.println(
				"setColor()) is not yet implemented! See org.roboticsapi.feature.visualization.viewer.javafx.model.RemoteSceneImpl");

	}

	@Override
	public synchronized void setScale(int id, double sx, double sy, double sz) throws RemoteException {
		System.out.println(
				"setScale()) is not yet implemented! See org.roboticsapi.feature.visualization.viewer.javafx.model.RemoteSceneImpl");

	}

	/**
	 * deletes the representing tab and stops the render loop
	 */
	public synchronized void deleteScene() {
		alive = false;
		frames.clear();
		meshes.clear();
	}

	@Override
	public String getName() throws RemoteException {
		return sceneName;
	}

	@Override
	public int addCylinder(String name, int parentID, double radius, double height, double x, double y, double z,
			double a, double b, double c) throws RemoteException {
		System.out.println(
				"addCylinder() is not yet implemented! See org.roboticsapi.feature.visualization.viewer.javafx.model.RemoteSceneImpl");

		return 0;
	}

	@Override
	public int addCapsule(String name, int parentID, double radius, double height, double x, double y, double z,
			double a, double b, double c) throws RemoteException {
		System.out.println(
				"addCylinder() is not yet implemented! See org.roboticsapi.feature.visualization.viewer.javafx.model.RemoteSceneImpl");

		return 0;
	}

	@Override
	public int getRootFrame() throws Exception {
		return 0;
	}

	public interface HighlightListener {
		void run(RemoteSceneImpl scene);
	}

	public interface FrameListener {
		void frameAdded(RapiFrame frame);

		void frameRemoved(RapiFrame frame);
	}

}
