/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/. 
 *
 * Copyright 2010-2017 ISSE, University of Augsburg 
 */

package org.roboticsapi.feature.visualization.viewer.javafx.view;

import org.roboticsapi.feature.visualization.viewer.navigation.visualization.Transformation;

public interface CameraProperties {

	public static class CameraPosition {
		public final double x;
		public final double y;
		public final double z;
		public final double yaw;
		public final double pitch;

		public CameraPosition(double x, double y, double z, double yaw, double pitch) {
			this.x = x;
			this.y = y;
			this.z = z;
			this.yaw = yaw;
			this.pitch = pitch;
		}

		public Transformation toTransformation() {
			return new Transformation(x, y, z, yaw, pitch, 0);
		}
	}

	public CameraPosition getCameraStartposition();

	public default void setCameraStartposition(CameraPosition cameraPosition) {
		setCameraStartposition(cameraPosition.x, cameraPosition.y, cameraPosition.z, cameraPosition.yaw,
				cameraPosition.pitch);
	}

	public void setCameraStartposition(double x, double y, double z, double yaw, double pitch);

}
