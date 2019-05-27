/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/. 
 *
 * Copyright 2010-2017 ISSE, University of Augsburg 
 */

package org.roboticsapi.feature.visualization.viewer.navigation.engine;

import java.util.Set;

import org.roboticsapi.feature.visualization.viewer.navigation.event.EventListener.Button;
import org.roboticsapi.feature.visualization.viewer.navigation.event.EventListener.MouseButton;
import org.roboticsapi.feature.visualization.viewer.navigation.visualization.NavigationInterface;
import org.roboticsapi.feature.visualization.viewer.navigation.visualization.Object3D;
import org.roboticsapi.feature.visualization.viewer.navigation.visualization.Point3D;
import org.roboticsapi.feature.visualization.viewer.navigation.visualization.Transformation;

public class ObjectState extends State {

	private final NavigationProperties configuration;

	private final double minTilt;
	private final double maxTilt;
	private final double minDistanceToGround;

	private NavigationInterface visualization;
	private Set<Button> keys;
	private int xMouse, yMouse;
	private Point3D rotationPoint;

	public ObjectState(NavigationProperties configuration, double minTilt, double maxTilt, double minDistanceToGround) {
		this.configuration = configuration;
		this.minTilt = minTilt;
		this.maxTilt = maxTilt;
		this.minDistanceToGround = minDistanceToGround;
	}

	@Override
	public void activate(Set<Button> keys, NavigationInterface visualization, int xMouse, int yMouse,
			Object3D selectedObject) {
		// TODO brauche objektmittelpunkt
		rotationPoint = new Point3D(0, 0, 0);
		this.visualization = visualization;
		this.keys = keys;
		this.xMouse = xMouse;
		this.yMouse = yMouse;
		visualization.setCamera(TransformationUtil.orientateCameraToPoint(visualization.getCamera(), rotationPoint,
				minTilt, maxTilt, minDistanceToGround));
		visualization.drawDragPoint(rotationPoint);
	}

	public void onMouseWheel(int direction) {
		visualization.setCamera(visualization.getCamera()
				.multiply(new Transformation(direction * configuration.getZoomSpeed(), 0, 0, 0, 0, 0)));
	}

	public void onMouseDown(MouseButton mouseButton, int xAxis, int yAxis) {
		keys.add(mouseButton);
	}

	public void onMouseUp(MouseButton mouseButton, int xAxis, int yAxis) {
		keys.remove(mouseButton);

	}

	public void onMouseMove(int x, int y) {
		int dY = yMouse - y;
		int dX = xMouse - x;
		if (keys.contains(configuration.getRotateLookButton()) || keys.contains(configuration.getRotateOrbitButton())) {
			visualization.setCamera(TransformationUtil.rotateArroundPoint(visualization.getCamera(), dX, dY,
					rotationPoint, configuration.getAngularSpeed(), minTilt, maxTilt, minDistanceToGround));
		} else if (keys.contains(configuration.getPanButton())) {
			panView(dX, dY);
		}

		xMouse = x;
		yMouse = y;
	}

	// pan view + rotationPoint
	private void panView(int dX, int dY) {

		Transformation camera = visualization.getCamera();
		Transformation rotPoint = new Transformation(rotationPoint, camera.getOrientation());

		double zGlobalDelta = 0, yLocalDelta = 0;
		zGlobalDelta = dY * configuration.getAngularSpeed();
		yLocalDelta = dX * configuration.getAngularSpeed();

		rotPoint = new Transformation(rotPoint.getPosition().add(new Point3D(0, 0, zGlobalDelta)),
				rotPoint.getOrientation());
		rotPoint = rotPoint.multiply(new Transformation(0, yLocalDelta, 0, 0, 0, 0));
		rotationPoint = rotPoint.getPosition();
		visualization.drawDragPoint(rotationPoint);

		camera = new Transformation(camera.getPosition().add(new Point3D(0, 0, zGlobalDelta)), camera.getOrientation());
		camera = camera.multiply(new Transformation(0, yLocalDelta, 0, 0, 0, 0));
		visualization.setCamera(camera);
	}

	@Override
	public void deactivate() {
		visualization.drawDragPoint(null);
	}

}
