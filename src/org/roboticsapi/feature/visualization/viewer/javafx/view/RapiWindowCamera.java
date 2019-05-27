/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/. 
 *
 * Copyright 2010-2017 ISSE, University of Augsburg 
 */

package org.roboticsapi.feature.visualization.viewer.javafx.view;

import org.roboticsapi.feature.visualization.viewer.navigation.visualization.Transformation;

import javafx.application.Platform;
import javafx.scene.PerspectiveCamera;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Translate;

/**
 * Represents the perspective camera for a RapiWindowTab
 * 
 * @see RapiWindowTab
 */
@SuppressWarnings("restriction")
public class RapiWindowCamera {
	private Translate transform;
	private Rotate a;
	private Rotate b;
	private Rotate c;

	private Transformation transformation;

	private final PerspectiveCamera camera;

	public RapiWindowCamera(PerspectiveCamera camera, Transformation cameraStartPosition) {
		this.camera = camera;
		this.transformation = cameraStartPosition;

		Rotate transA = new Rotate(90, Rotate.Z_AXIS);
		Rotate transB = new Rotate(180, Rotate.Y_AXIS);
		Rotate transC = new Rotate(90, Rotate.X_AXIS);

		this.transform = new Translate(0, 0, 0);
		this.a = new Rotate(0, Rotate.Z_AXIS);
		this.b = new Rotate(0, Rotate.Y_AXIS);
		this.c = new Rotate(0, Rotate.X_AXIS);

		camera.getTransforms().addAll(transform, a, b, c, transA, transB, transC);
		update(transformation);
	}

	public void update(Transformation trans) {
		transformation = trans;
		Platform.runLater(() -> {
			transform.setX(trans.getPosition().getX());
			transform.setY(trans.getPosition().getY());
			transform.setZ(trans.getPosition().getZ());
			a.setAngle(Math.toDegrees(trans.getOrientation().getA()));
			b.setAngle(Math.toDegrees(trans.getOrientation().getB()));
			c.setAngle(Math.toDegrees(trans.getOrientation().getC()));
		});
	}

	public Transformation getTransformation() {
		return transformation;
	}

	public PerspectiveCamera getCamera() {
		return camera;
	}

}
