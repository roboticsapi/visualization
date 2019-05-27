/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/. 
 *
 * Copyright 2010-2017 ISSE, University of Augsburg 
 */

package org.roboticsapi.feature.visualization.viewer.javafx.lookup;

import org.roboticsapi.feature.visualization.RapiInfo;

public class Application {

	public final String type;
	public final int port;
	public final String name;

	private Application(String type, int port, String name) {
		this.type = type;
		this.port = port;
		this.name = name;
	}

	protected static Application fromRapiInfo(RapiInfo rapiInfo) {
		return new Application(rapiInfo.type, rapiInfo.port, rapiInfo.name);
	}

	protected boolean describesSameServer(RapiInfo rapiInfo) {
		return type.equals(rapiInfo.type) && port == rapiInfo.port;
	}
}