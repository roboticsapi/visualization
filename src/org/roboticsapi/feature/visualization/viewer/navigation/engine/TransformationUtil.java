/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/. 
 *
 * Copyright 2010-2017 ISSE, University of Augsburg 
 */

package org.roboticsapi.feature.visualization.viewer.navigation.engine;

import org.roboticsapi.feature.visualization.viewer.navigation.visualization.Point3D;
import org.roboticsapi.feature.visualization.viewer.navigation.visualization.Rotation3D;
import org.roboticsapi.feature.visualization.viewer.navigation.visualization.Transformation;

public class TransformationUtil {

	/**
	 * changes the orientation of the camera accordingly to the mouse movement
	 */
	public static Transformation rotateCamera(Transformation camera, int dX, int dY, double angularSpeed,
			double minTilt, double maxTilt) {
		Rotation3D rot = camera.getOrientation();

		double a = rot.getA() + dX * angularSpeed;
		double b = crop(rot.getB() - dY * angularSpeed, minTilt, maxTilt);

		return new Transformation(camera.getPosition(), new Rotation3D(a, b, 0));
	}

	/**
	 * changes the Rotation3D of the Camera in the way that it centers the Point3D
	 * 
	 * @return the new Rotation3D
	 */

	public static Transformation orientateCameraToPoint(Transformation originToCamera, Point3D rotationPoint,
			double minTilt, double maxTilt, double minDistanceToGround) {
		Point3D pos = originToCamera.getPosition();

		double x = rotationPoint.getX() - pos.getX();
		double y = rotationPoint.getY() - pos.getY();
		double z = rotationPoint.getZ() - pos.getZ();

		double gradA = Math.acos(x / (Math.sqrt(x * x + y * y)));
		if (y < 0)
			gradA = 2 * Math.PI - gradA;

		double gradB = Math.acos(z / (Math.sqrt(x * x + y * y + z * z)));
		gradB = crop(gradB - Math.PI / 2, minTilt, maxTilt);

		return new Transformation(pos, new Rotation3D(gradA, gradB, 0));
	}

	public static Transformation rotateArroundPoint(Transformation originToCamera, int dX, int dY,
			Point3D rotationPoint, double angularSpeed, double minTilt, double maxTilt, double minDistanceToGround) {
		// z up, x aligned with camera x
		Transformation originToRotationPoint = new Transformation(rotationPoint,
				new Rotation3D(originToCamera.getA(), 0, 0));
		Transformation rotationPointToCamera = originToRotationPoint.invert().multiply(originToCamera);

		double oldTilt = rotationPointToCamera.getB();
		double newTilt = crop(oldTilt - dY * angularSpeed, minTilt, maxTilt);
		double tiltDelta = newTilt - oldTilt;

		// Ensure not to cross floor
		double rotationPointToCameraTiltAngle = Math.asin((originToCamera.getZ() - originToRotationPoint.getZ())
				/ rotationPointToCamera.getPosition().getLength());
		double minAngleNotToCrossFloor = Math.asin(
				(minDistanceToGround - originToRotationPoint.getZ()) / rotationPointToCamera.getPosition().getLength());
		double maxAllowedTiltDelta = minAngleNotToCrossFloor - rotationPointToCameraTiltAngle;
		tiltDelta = Math.max(tiltDelta, maxAllowedTiltDelta);

		Transformation originToNewCamera = originToRotationPoint
				.multiply(new Rotation3D(dX * angularSpeed, tiltDelta, 0)).multiply(rotationPointToCamera);
		return originToNewCamera;
	}

	public static Transformation zoomKameraFromWheel(Transformation originToCamera, double zoomSpeed) {
		return originToCamera.multiply(new Transformation(zoomSpeed, 0, 0, 0, 0, 0));
	}

	private static double crop(double b, double minTilt, double maxTilt) {
		return Math.max(minTilt, Math.min(maxTilt, b));
	}
}
