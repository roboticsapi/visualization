/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/. 
 *
 * Copyright 2010-2017 ISSE, University of Augsburg 
 */

package org.roboticsapi.feature.visualization.viewer.navigation.engine;

import org.roboticsapi.feature.visualization.viewer.navigation.event.EventListener.KeyButton;
import org.roboticsapi.feature.visualization.viewer.navigation.event.EventListener.MouseButton;

public interface NavigationProperties {

	public double getMovementSpeed();

	public double getZoomSpeed();

	public double getAngularSpeed();

	public KeyButton getMoveUpButton();

	public KeyButton getMoveDownButton();

	public KeyButton getMoveLeftButton();

	public KeyButton getMoveRightButton();

	public KeyButton getMoveForwardButton();

	public KeyButton getMoveBackwardButton();

	public MouseButton getRotateLookButton();

	public MouseButton getRotateOrbitButton();

	public MouseButton getFocusButton();

	public MouseButton getPanButton();

}
