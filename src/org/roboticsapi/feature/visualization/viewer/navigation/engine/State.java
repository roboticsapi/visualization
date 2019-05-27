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

public abstract class State {

	/**
	 * this function will be done excatly 1 time, when the state is entered. this
	 * funtion does the initial work for a state (for excample to show a menue or to
	 * paint a point) and to give needed information to the state
	 * 
	 * @param key
	 * @param xMouse         TODO
	 * @param yMouse         TODO
	 * @param selectedObject TODO
	 */
	public abstract void activate(Set<Button> key, NavigationInterface visualization, int xMouse, int yMouse,
			Object3D selectedObject);

	/**
	 * this function will be done excatly 1 time, when the state is left. this
	 * funtion does clear everything for the next state(for excample to delete a
	 * menue or to paint a point)
	 */
	public abstract void deactivate();

	public void onKeyDown(KeyButton keyButton) {

	}

	public void onKeyUp(KeyButton keyButton) {

	}

	public void onMouseDown(MouseButton mouseButton, int xAxis, int yAxis) {

	}

	public void onMouseUp(MouseButton mouseButton, int xAxis, int yAxis) {

	}

	public void onMouseMove(int xAxis, int yAxis) {

	}

	public void onMouseWheel(int direction) {

	}

	@Override
	public String toString() {

		return "" + this.getClass().getSimpleName();

	}

}
