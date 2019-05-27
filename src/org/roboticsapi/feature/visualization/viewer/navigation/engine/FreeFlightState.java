/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/. 
 *
 * Copyright 2010-2017 ISSE, University of Augsburg 
 */

package org.roboticsapi.feature.visualization.viewer.navigation.engine;

import java.util.Set;

import org.roboticsapi.feature.visualization.viewer.navigation.event.EventListener.Button;
import org.roboticsapi.feature.visualization.viewer.navigation.event.EventListener.KeyButton;
import org.roboticsapi.feature.visualization.viewer.navigation.event.EventListener.MouseButton;
import org.roboticsapi.feature.visualization.viewer.navigation.visualization.NavigationInterface;
import org.roboticsapi.feature.visualization.viewer.navigation.visualization.Object3D;
import org.roboticsapi.feature.visualization.viewer.navigation.visualization.Point3D;
import org.roboticsapi.feature.visualization.viewer.navigation.visualization.Transformation;

public class FreeFlightState extends ThreadState {

	private final NavigationProperties configuration;

	private final double minTilt;
	private final double maxTilt;
	private final double minDistanceToGround;

	private Set<Button> keys;
	private NavigationInterface visualization;
	private int xMouse, yMouse;
	private Point3D rotationPoint;

	public FreeFlightState(NavigationProperties configuration, double minTilt, double maxTilt,
			double minDistanceToGround) {
		this.configuration = configuration;
		this.minTilt = minTilt;
		this.maxTilt = maxTilt;
		this.minDistanceToGround = minDistanceToGround;
	}

	@Override
	public void activate(Set<Button> keys, NavigationInterface visualization, int xMouse, int yMouse,
			Object3D selectedObject) {
		this.keys = keys;
		this.visualization = visualization;
		this.xMouse = xMouse;
		this.yMouse = yMouse;
	}

	@Override
	public void onMouseMove(int x, int y) {
		int dY = yMouse - y;
		int dX = xMouse - x;

		Transformation camera = visualization.getCamera();

		if (keys.contains(configuration.getRotateLookButton()) || keys.contains(configuration.getRotateOrbitButton())) {
			if (rotationPoint != null)
				camera = TransformationUtil.rotateArroundPoint(camera, dX, dY, rotationPoint,
						configuration.getAngularSpeed(), minTilt, maxTilt, minDistanceToGround);
			else
				camera = TransformationUtil.rotateCamera(camera, dX, dY, configuration.getAngularSpeed(), minTilt,
						maxTilt);
		}

		visualization.setCamera(camera);
		xMouse = x;
		yMouse = y;
	}

	public void onMouseWheel(int direction) {
//		TransformationUtil.zoomKameraFromWheel(direction * configuration.getZoomSpeed(), visualization);
	}

	@Override
	public void onKeyDown(KeyButton keyButton) {
		keys.add(keyButton);
	}

	public void onKeyUp(KeyButton keyButton) {
		keys.remove(keyButton);
	}

	public void onMouseDown(MouseButton mouseButton, int xAxis, int yAxis) {
		// add to used keys and if get rotationpoint if rotation is pressed
		keys.add(mouseButton);

		if (mouseButton == configuration.getRotateLookButton()) {
		}

		if (mouseButton == configuration.getRotateOrbitButton()) {
			rotationPoint = visualization.getFirstCollisionPoint(xAxis, yAxis, true);
			this.visualization.drawDragPoint(rotationPoint);
		}
	}

	public void onMouseUp(MouseButton mouseButton, int xAxis, int yAxis) {
		// remove from used keys and delete rotationpoint if rotation is released
		keys.remove(mouseButton);

		if (mouseButton == configuration.getRotateOrbitButton()) {
			rotationPoint = null;
			this.visualization.drawDragPoint(null);
		}
	}

	public void onThreadRunning() {
		// does the movement of the camera position in free flight mode

		Transformation camera = visualization.getCamera();

		double xGlobalDelta = 0, yGlobalDelta = 0, zGlobalDelta = 0, xLocalDelta = 0, yLocalDelta = 0, zLocalDelta = 0,
				aDelta = 0, bDelta = 0, cDelta = 0;

		if (keys.contains(configuration.getMoveUpButton())) {
			zLocalDelta += configuration.getMovementSpeed();
		}
		if (keys.contains(configuration.getMoveDownButton())) {
			zLocalDelta -= configuration.getMovementSpeed();
		}
		if (keys.contains(configuration.getMoveLeftButton())) {
			yLocalDelta += configuration.getMovementSpeed();
		}
		if (keys.contains(configuration.getMoveRightButton())) {
			yLocalDelta -= configuration.getMovementSpeed();
		}
		if (keys.contains(configuration.getMoveForwardButton())) {
			xLocalDelta += configuration.getMovementSpeed();
		}
		if (keys.contains(configuration.getMoveBackwardButton())) {
			xLocalDelta -= configuration.getMovementSpeed();
		}

		Transformation newCameraGloballyMoved = new Transformation(
				camera.getPosition().add(new Point3D(xGlobalDelta, yGlobalDelta, zGlobalDelta)),
				camera.getOrientation());
		Transformation newCamera = newCameraGloballyMoved
				.multiply(new Transformation(xLocalDelta, yLocalDelta, zLocalDelta, aDelta, bDelta, cDelta));

		// Don't allow to touch under floor
		double croppedZ = Math.max(minDistanceToGround, newCamera.getZ());
		double croppedB = Math.min(maxTilt, Math.max(minTilt, newCamera.getB()));

		newCamera = new Transformation(newCamera.getX(), newCamera.getY(), croppedZ, newCamera.getA(), croppedB, 0);
		visualization.setCamera(newCamera);
	}

	@Override
	public void deactivate() {

	}

}
