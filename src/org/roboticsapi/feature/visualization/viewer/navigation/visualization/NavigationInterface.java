/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/. 
 *
 * Copyright 2010-2017 ISSE, University of Augsburg 
 */

package org.roboticsapi.feature.visualization.viewer.navigation.visualization;

public interface NavigationInterface {

	/**
	 * gets the current Frame the visualization uses for camera
	 * 
	 * @return the current Frame of the camera in the visualization
	 */
	public Transformation getCamera();

	/**
	 * gets the object on a specific point which is determined by clicking
	 * 
	 * @param xposition the x-axis position from the MouseEvent
	 * @param yposition the y-axis position from the MouseEvent
	 * @return the first object that is hit by the line created through the pressed
	 *         point on the screen and orientation of the frustum
	 */
	public Object3D getFirstCollisionObject(int xposition, int yposition);

	/**
	 * gets all objects in a specific area determined by dragging the mouse
	 * 
	 * @param xposition the x-axis position from the MouseEvent at the start of the
	 *                  dragging
	 * @param yposition the y-axis position from the MouseEvent at the start of the
	 *                  dragging
	 * @param height    the difference of the y-axis start-point and end-point of
	 *                  the dragging
	 * @param width     the difference of the x-axis start-point and end-point of
	 *                  the dragging
	 * @return all objects that meet the lines created though the field (the x-axis,
	 *         y-axis, height, width) and the orientation of the frustum
	 */
	public Object3D[] getObjectsInRange(int xposition, int yposition, int height, int width);

	/**
	 * 
	 * @param xposition the x-axis position from the MouseEvent
	 * @param yposition the y-axis position from the MouseEvent
	 * @return the first point of the object that is hit by the line created through
	 *         the pressed point on the screen and orientation of the frustum
	 */
	public Point3D getFirstCollisionPoint(int xposition, int yposition, boolean includeGround);

	/**
	 * sets the camera to a specific Frame
	 * 
	 * @param calculatedFrame the Frame which the camera should be set to
	 */
	public void setCamera(Transformation calculatedFrame);

	/**
	 * instructs the visualization to draw or delete a point at a specific point
	 * 
	 * @param xposition the x-axis position from the MouseEvent
	 * @param yposition the y-axis position from the MouseEvent
	 */
	public void drawDragPoint(Point3D point);

	/**
	 * instructs the visalization to highlight a specific object or to undo it
	 * 
	 * @param object    the object that should be highlighted
	 * @param highlight determines whether the object should be highlighted or it
	 *                  should be undone
	 */
	public void setHighlighted(Object3D object, boolean visible);

}
