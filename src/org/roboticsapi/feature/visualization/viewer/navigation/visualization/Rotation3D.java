/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/. 
 *
 * Copyright 2010-2017 ISSE, University of Augsburg 
 */

package org.roboticsapi.feature.visualization.viewer.navigation.visualization;

public class Rotation3D {

	private final Matrix3x3 matrix;

	public Rotation3D() {
		this(0, 0, 0);
	}

	public Rotation3D(double a, double b, double c) {
		final double sa = Math.sin(a), sb = Math.sin(b), sc = Math.sin(c);
		final double ca = Math.cos(a), cb = Math.cos(b), cc = Math.cos(c);

		matrix = new Matrix3x3(ca * cb, ca * sb * sc - sa * cc, ca * sb * cc + sa * sc, sa * cb, sa * sb * sc + ca * cc,
				sa * sb * cc - ca * sc, -sb, cb * sc, cb * cc);
	}

	private Rotation3D(Matrix3x3 matrix) {
		this.matrix = matrix;
	}

	private static boolean near(final double a, final double b) {
		return Math.abs(a - b) < 0.001;
	}

	/**
	 * Retrieves the yaw angle
	 * 
	 * @return yaw angle in rad
	 */
	public double getA() {
		final double b = getB();
		if (near(Math.abs(b), Math.PI / 2)) {
			return 0;
		}
		return Math.atan2(matrix.get(1, 0), matrix.get(0, 0));
	}

	/**
	 * Retrieves the pitch angle
	 * 
	 * @return pitch angle in rad
	 */
	public double getB() {
		return Math.atan2(-matrix.get(2, 0),
				Math.sqrt(matrix.get(0, 0) * matrix.get(0, 0) + matrix.get(1, 0) * matrix.get(1, 0)));
	}

	/**
	 * Retrieves the roll angle
	 * 
	 * @return roll angle in rad
	 */
	public double getC() {
		final double b = getB();
		if (near(b, Math.PI / 2)) {
			return Math.atan2(matrix.get(0, 1), matrix.get(1, 1));
		}
		if (near(b, -Math.PI / 2)) {
			return -Math.atan2(matrix.get(0, 1), matrix.get(1, 1));
		}
		return Math.atan2(matrix.get(2, 1), matrix.get(2, 2));
	}

	public Matrix3x3 getMatrix() {
		return matrix;
	}

	public Rotation3D multiply(Rotation3D other) {
		return new Rotation3D(getMatrix().multiply(other.getMatrix()));
	}

	public Rotation3D invert() {
		Matrix3x3 rot = matrix.transpose();
		return new Rotation3D(rot);
	}

	/**
	 * Applies the rotation to a given vector
	 * 
	 * @param pos vector to rotate
	 * @return new, rotated vector
	 */
	public Point3D apply(final Point3D pos) {
		return matrix.apply(pos);
	}

}
