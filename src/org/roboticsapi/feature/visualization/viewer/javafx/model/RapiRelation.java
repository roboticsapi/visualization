/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/. 
 *
 * Copyright 2010-2017 ISSE, University of Augsburg 
 */

package org.roboticsapi.feature.visualization.viewer.javafx.model;

import java.util.Arrays;
import java.util.List;

import javafx.scene.transform.Rotate;
import javafx.scene.transform.Transform;
import javafx.scene.transform.Translate;

public class RapiRelation {

	private final Rotate rotatez = new Rotate(0, Rotate.Z_AXIS);
	private final Rotate rotatey = new Rotate(0, Rotate.Y_AXIS);
	private final Rotate rotatex = new Rotate(0, Rotate.X_AXIS);
	private final Translate translate = new Translate(0, 0, 0);
	private final Rotate invRotatez = new Rotate(0, Rotate.Z_AXIS);
	private final Rotate invRotatey = new Rotate(0, Rotate.Y_AXIS);
	private final Rotate invRotatex = new Rotate(0, Rotate.X_AXIS);
	private final Translate invTranslate = new Translate(0, 0, 0);
	private RapiFrame from;
	private RapiFrame to;
	private double x, y, z, a, b, c;

	public RapiRelation(RapiFrame from, RapiFrame to, double x, double y, double z, double a, double b, double c) {
		this.from = from;
		this.to = to;
		updateTransformation(x, y, z, a, b, c);
		performUpdate();
	}

	public void updateTransformation(double x, double y, double z, double a, double b, double c) {
		this.x = x;
		this.y = y;
		this.z = z;
		this.a = a;
		this.b = b;
		this.c = c;
	}

	public void performUpdate() {
		translate.setX(x);
		translate.setY(y);
		translate.setZ(z);
		rotatez.setAngle(Math.toDegrees(a));
		rotatey.setAngle(Math.toDegrees(b));
		rotatex.setAngle(Math.toDegrees(c));

		invTranslate.setX(-x);
		invTranslate.setY(-y);
		invTranslate.setZ(-z);
		invRotatez.setAngle(Math.toDegrees(-a));
		invRotatey.setAngle(Math.toDegrees(-b));
		invRotatex.setAngle(Math.toDegrees(-c));
	}

	public List<Transform> getTransform() {
		return Arrays.asList(translate, rotatez, rotatey, rotatex);
	}

	public List<Transform> getInverseTransform() {
		return Arrays.asList(invRotatex, invRotatey, invRotatez, invTranslate);
	}

	public RapiFrame getFrom() {
		return from;
	}

	public RapiFrame getTo() {
		return to;
	}

	public RapiFrame getOther(RapiFrame frame) {
		if (frame == from)
			return to;
		else
			return from;
	}

	@Override
	public String toString() {
		return from + " -> " + to;
	}
}
