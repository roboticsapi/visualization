/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/. 
 *
 * Copyright 2010-2017 ISSE, University of Augsburg 
 */

package org.roboticsapi.feature.visualization.viewer.navigation.visualization;

public class Point3D {

	private final double x;
	private final double y;
	private final double z;

	public Point3D() {
		this(0, 0, 0);
	}

	public Point3D(double x, double y, double z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}

	public double getX() {
		return x;
	}

	public double getY() {
		return y;
	}

	public double getZ() {
		return z;
	}

	public double getLength() {
		return Math.sqrt(x * x + y * y + z * z);
	}

	public Point3D invert() {
		return new Point3D(-x, -y, -z);
	}

	public Point3D add(Point3D other) {
		return new Point3D(x + other.x, y + other.y, z + other.z);
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + "[x:" + x + " y:" + y + " z:" + z + "]";
	}

}
