/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/. 
 *
 * Copyright 2010-2017 ISSE, University of Augsburg 
 */

package org.roboticsapi.feature.visualization.viewer.navigation.visualization;

public class Transformation {

	private final Point3D translation;
	private final Rotation3D rotation;

	public Transformation(double x, double y, double z, double a, double b, double c) {
		this(new Point3D(x, y, z), new Rotation3D(a, b, c));
	}

	public Transformation(Point3D position, Rotation3D orientation) {
		this.translation = position;
		this.rotation = orientation;
	}

	public Transformation(Point3D position) {
		this(position, new Rotation3D());
	}

	public Transformation(Rotation3D orientation) {
		this(new Point3D(), orientation);
	}

	public Point3D getPosition() {
		return translation;
	}

	public double getX() {
		return getPosition().getX();
	}

	public double getY() {
		return getPosition().getY();
	}

	public double getZ() {
		return getPosition().getZ();
	}

	public Rotation3D getOrientation() {
		return rotation;
	}

	/**
	 * Retrieves the yaw angle
	 * 
	 * @return yaw angle in rad
	 */
	public double getA() {
		return getOrientation().getA();
	}

	/**
	 * Retrieves the pitch angle
	 * 
	 * @return pitch angle in rad
	 */
	public double getB() {
		return getOrientation().getB();
	}

	/**
	 * Retrieves the roll angle
	 * 
	 * @return roll angle in rad
	 */
	public double getC() {
		return getOrientation().getC();
	}

	/**
	 * Combines two transformations
	 *
	 * @param other other translation to execute afterwards
	 * @return new, combined transformation
	 */
	public Transformation multiply(final Transformation other) {
		return new Transformation(rotation.apply(other.translation).add(translation),
				rotation.multiply(other.rotation));
	}

	public Transformation multiply(Rotation3D other) {
		return new Transformation(translation, rotation.multiply(other));
	}

	public Transformation multiply(Point3D other) {
		return new Transformation(rotation.apply(other).add(translation), rotation);
	}

	/**
	 * Calculates the inverted transformation
	 *
	 * @return new, inverted transformation
	 */
	public Transformation invert() {
		final Rotation3D rot = rotation.invert();
		return new Transformation(rot.apply(translation).invert(), rot);
	}

	@Override
	public String toString() {
		return "Transformation [x:" + translation.getX() + " y:" + translation.getY() + " z:" + translation.getZ()
				+ " a:" + rotation.getA() + " b:" + rotation.getB() + " c:" + rotation.getC() + "]";
	}

}