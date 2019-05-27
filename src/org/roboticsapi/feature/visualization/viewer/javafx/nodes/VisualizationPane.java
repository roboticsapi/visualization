/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/. 
 *
 * Copyright 2010-2017 ISSE, University of Augsburg 
 */

package org.roboticsapi.feature.visualization.viewer.javafx.nodes;

import java.io.InputStream;

import org.roboticsapi.feature.visualization.viewer.javafx.Configuration;
import org.roboticsapi.feature.visualization.viewer.javafx.model.RapiFrame;
import org.roboticsapi.feature.visualization.viewer.javafx.nodes.FrameTreePane.FrameSelectionListener;
import org.roboticsapi.feature.visualization.viewer.javafx.view.RapiWindowCamera;
import org.roboticsapi.feature.visualization.viewer.navigation.engine.NavigationManager;
import org.roboticsapi.feature.visualization.viewer.navigation.javafx.JavaFXEventManager;
import org.roboticsapi.feature.visualization.viewer.navigation.visualization.NavigationInterface;
import org.roboticsapi.feature.visualization.viewer.navigation.visualization.Object3D;
import org.roboticsapi.feature.visualization.viewer.navigation.visualization.Point3D;
import org.roboticsapi.feature.visualization.viewer.navigation.visualization.Transformation;

import com.sun.javafx.geom.PickRay;
import com.sun.javafx.geom.Vec3d;
import com.sun.javafx.geom.transform.Affine3D;

import javafx.application.Platform;
import javafx.scene.AmbientLight;
import javafx.scene.DepthTest;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.PerspectiveCamera;
import javafx.scene.PointLight;
import javafx.scene.SceneAntialiasing;
import javafx.scene.SubScene;
import javafx.scene.control.Control;
import javafx.scene.control.Skin;
import javafx.scene.control.SkinBase;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Cylinder;
import javafx.scene.shape.MeshView;
import javafx.scene.shape.Sphere;
import javafx.scene.shape.TriangleMesh;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Transform;
import javafx.scene.transform.Translate;

@SuppressWarnings("restriction")
public class VisualizationPane extends Control implements FrameSelectionListener {

	private final NavigationManager navigationManager;
	private final RapiWindowCamera camera;
	private final Group world3d;
	private Node rootNode = null;
	private Sphere dragPoint = null;
//	private Sphere dragSphere = null;
	private Group frame = null;

	private class VisualizationPaneSkin extends SkinBase<VisualizationPane> {
		protected VisualizationPaneSkin(VisualizationPane control) {
			super(control);
			addEventFilter(MouseEvent.MOUSE_PRESSED, e -> getSkinnable().requestFocus());
			consumeMouseEvents(false);
		}
	}

	@Override
	protected Skin<?> createDefaultSkin() {
		return new VisualizationPaneSkin(this);
	}

	public VisualizationPane(Node rootNode, Configuration configuration) {
		setMinWidth(20);
		setMinHeight(20);

		world3d = createWorld3d();
		setVisualizationNode(rootNode);

		// Setup camera
		PerspectiveCamera perspectiveCamera = new PerspectiveCamera(true);
		perspectiveCamera.setFarClip(80);
		camera = new RapiWindowCamera(perspectiveCamera, configuration.getCameraStartposition().toTransformation());

		// Setup 3d scene
		SubScene subScene = new SubScene(world3d, 10000, 10000, true, SceneAntialiasing.BALANCED);
		subScene.setFill(Color.ALICEBLUE);
		subScene.setCamera(perspectiveCamera);
		subScene.widthProperty().bind(widthProperty());
		subScene.heightProperty().bind(heightProperty());
		getChildren().add(subScene);

		// add key- und mouselistener...
		JavaFXEventManager eventManager = new JavaFXEventManager(this);
		navigationManager = new NavigationManager(new MyNavigationInterface(), eventManager, configuration);

	}

	private Group createWorld3d() {
		Group world3d = new Group();

		// Add light
		double ambient = 0.65;
		AmbientLight ambientLight = new AmbientLight(Color.color(ambient, ambient, ambient));
		double point = 0.4;
		PointLight pointlight = new PointLight(Color.color(point, point, point));
		pointlight.setTranslateX(30);
		pointlight.setTranslateY(30);
		pointlight.setTranslateZ(100);
		world3d.getChildren().addAll(pointlight, ambientLight);

		// Ground
		TriangleMesh trmesh = new TriangleMesh();
		MeshView meshView = new MeshView(trmesh);
		float scale = 30;
		float texscale = 201;
		trmesh.getTexCoords().addAll(0, 0, texscale, 0, 0, texscale, texscale, texscale);
		trmesh.getPoints().addAll(-scale, -scale, 0.0f, scale, -scale, 0.0f, -scale, scale, 0.0f, scale, scale, 0.0f);
		trmesh.getFaces().addAll(2, 2, 0, 0, 1, 1, 1, 1, 3, 3, 2, 2);
		PhongMaterial mat = new PhongMaterial();
		final InputStream resourceAsStream = RapiWindowCamera.class.getResourceAsStream("diffuse.png");
		Image diffuseMap = new Image(resourceAsStream);
		mat.setDiffuseMap(diffuseMap);
		meshView.setMaterial(mat);
		world3d.getChildren().add(meshView);

		dragPoint = new Sphere(0.01);
		dragPoint.setMaterial(new PhongMaterial(new Color(1, 0, 0, 1)));
//		dragPoint.setDepthTest(DepthTest.DISABLE);
		dragPoint.setVisible(false);

//		dragSphere = new Sphere(0.1);
//		dragSphere.setMaterial(new PhongMaterial(new Color(1, 1, 1, 1)));
//		dragSphere.setVisible(false);
//		world3d.getChildren().add(dragSphere);

		frame = new Group();
		Cylinder x = new Cylinder(0.005, 1);
		x.setMaterial(new PhongMaterial(Color.RED));
		x.getTransforms().addAll(new Rotate(-90, Rotate.Z_AXIS), new Translate(0, 0.505, 0));
		Cylinder y = new Cylinder(0.005, 1);
		y.setMaterial(new PhongMaterial(Color.GREEN));
		y.getTransforms().addAll(new Translate(0, 0.505, 0));
		Cylinder z = new Cylinder(0.005, 1);
		z.setMaterial(new PhongMaterial(Color.BLUE));
		z.getTransforms().addAll(new Rotate(90, Rotate.X_AXIS), new Translate(0, 0.505, 0));
		Sphere p = new Sphere(0.01);
		p.setMaterial(new PhongMaterial(Color.WHITE));
		frame.getChildren().addAll(x, y, z, p);
		frame.setVisible(false);
		frame.setDepthTest(DepthTest.DISABLE);
		world3d.getChildren().add(frame);

		return world3d;
	}

	public final void setVisualizationNode(Node rootNode) {
		Platform.runLater(() -> {
			if (this.rootNode != null) {
				world3d.getChildren().remove(this.rootNode);
			}
			world3d.getChildren().add(rootNode);
			this.rootNode = rootNode;
		});
		// group3d.getChildren().clear();
		// subScene.setFill(Color.DARKGRAY);
		// if (rootNode!=null) {
		// group3d.getChildren().add(meshView);
		// group3d.getChildren().addAll(pointlight, ambientLight);
		// subScene.setFill(Color.ALICEBLUE);
		// group3d.getChildren().add(rootNode);
		// }
		// });
		//
	}

	public final Image createSnapshot() {
		WritableImage writableImage = new WritableImage((int) getWidth(), (int) getHeight());
		snapshot(null, writableImage);
		return writableImage;
	}

	public final void dispose() {
		navigationManager.dispose();
	}

	private final class MyNavigationInterface implements NavigationInterface {

		@Override
		public synchronized Transformation getCamera() {
			return camera.getTransformation();
		}

		@Override
		public synchronized Object3D getFirstCollisionObject(int xposition, int yposition) {
			// double farClip = 99999;
			// boolean fixedEye = true;
			// PickRay picRay = PickRay.computePerspectivePickRay(xposition,
			// yposition, fixedEye, getWidth(), getHeight(), fieldOfViewRadians,
			// verticalFieldOfView, cameraTransform, 0, farClip, null);

			// javafx.geometry.Point3D o =
			// CameraHelper.pickProjectPlane(camera.getCamera(), xposition,
			// yposition);
			// System.out.println(o);
			// // set Target and Direction
			// Point3D t =
			// javafx.geometry.Point3D.ZERO.add(target2.getTranslateX(),
			// target2.getTranslateY(), target2.getTranslateZ()),
			// d = t.subtract(o);
			// //Build the Ray
			// Ray r = new Ray(o, d);
			// double dist = t.distance(o);
			// // If ray intersects node, spawn and animate
			// if (target2.getBoundsInParent().contains(r.project(dist))) {
			// return
			// animateRayTo(r, target2, Duration.seconds(2));
			// }
			//

			return null;
		}

		@Override
		public synchronized Object3D[] getObjectsInRange(int xposition, int yposition, int height, int width) {
			return new Object3D[0];
		}

		@Override
		public synchronized Point3D getFirstCollisionPoint(int xposition, int yposition, boolean includeGround) {
			PerspectiveCamera cam = camera.getCamera();
			Transform t = cam.getLocalToSceneTransform();
			Affine3D a = new Affine3D(t.getMxx(), t.getMxy(), t.getMxz(), t.getTx(), t.getMyx(), t.getMyy(), t.getMyz(),
					t.getTy(), t.getMzx(), t.getMzy(), t.getMzz(), t.getTz());
			PickRay ray = PickRay.computePerspectivePickRay((double) xposition, (double) yposition,
					cam.isFixedEyeAtCameraZero(), getWidth(), getHeight(), Math.toRadians(cam.getFieldOfView()),
					cam.isVerticalFieldOfView(), a, cam.getNearClip(), cam.getFarClip(), null);

			Vec3d origin = ray.getOrigin(null);
			Vec3d direction = ray.getDirection(null);

			if (includeGround) {
				if (direction.z < 0) {
					direction.mul(-origin.z / direction.z);
					origin.add(direction);
					return new Point3D(origin.x, origin.y, origin.z);
				}
			}
			return null;
		}

		@Override
		public synchronized void setCamera(Transformation calculatedFrame) {
			camera.update(calculatedFrame);
		}

		@Override
		public synchronized void drawDragPoint(Point3D point) {
			world3d.getChildren().remove(dragPoint);

			if (point == null) {
				dragPoint.setVisible(false);
//				dragSphere.setVisible(false);
			} else {
				dragPoint.setTranslateX(point.getX());
				dragPoint.setTranslateY(point.getY());
				dragPoint.setTranslateZ(point.getZ());
				dragPoint.setVisible(true);

//				dragSphere.setTranslateX(point.getX());
//				dragSphere.setTranslateY(point.getY());
//				dragSphere.setTranslateZ(point.getZ());
//				dragSphere.setVisible(true);

				// add it as last to bring it to front (when depth test disabled)
				world3d.getChildren().add(dragPoint);
			}
		}

		@Override
		public synchronized void setHighlighted(Object3D object, boolean visible) {
		}

	}

	@Override
	public void frameSelected(RapiFrame frame) {
		if (frame == null) {
			this.frame.setVisible(false);
		} else {
			this.frame.getTransforms().clear();
			this.frame.getTransforms().addAll(frame.getTransforms());
			this.frame.setVisible(true);
			world3d.getChildren().remove(this.frame);
			world3d.getChildren().add(this.frame);
		}
	}

}
