/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/. 
 *
 * Copyright 2010-2017 ISSE, University of Augsburg 
 */

package org.roboticsapi.feature.visualization.viewer.javafx.lookup;

import java.util.ArrayList;
import java.util.List;

import org.roboticsapi.feature.visualization.LookupClient;
import org.roboticsapi.feature.visualization.RAPILogger;
import org.roboticsapi.feature.visualization.RapiInfo;
import org.roboticsapi.feature.visualization.rmi.RmiVisualizationClientAccepterImpl;

public class FxLookupClient extends LookupClient {

	private final LookupListener l;
	private final List<Application> currentInstances = new ArrayList<>();

	public FxLookupClient(String host, int port, LookupListener l) {
		super(host, port);
		this.l = l;
	}

	@Override
	public synchronized void connectionStateChanged(boolean online) {
		try {
			l.lookupServerStateChanged(online);
		} catch (Exception e) {
			RAPILogger.logException(getClass(), e);
		}
	}

	@Override
	public synchronized void newConfiguration(RapiInfo[] servers) {
		List<Application> newInstances = new ArrayList<>();

		for (RapiInfo newServer : servers) {
			if (!RmiVisualizationClientAccepterImpl.RMI_SERVER_TYPE.equals(newServer.type))
				continue;

			boolean found = false;
			for (Application oldApp : new ArrayList<>(currentInstances)) {
				if (oldApp.describesSameServer(newServer)) {
					currentInstances.remove(oldApp);
					newInstances.add(oldApp);
					found = true;
					break;
				}
			}

			// Neuer Server wurde in alter Liste nicht gefunden.
			if (!found) {
				Application newApp = Application.fromRapiInfo(newServer);
				newInstances.add(newApp);
				try {
					l.rapiInstanceAdded(newApp);
				} catch (Exception e) {
					RAPILogger.logException(getClass(), e);
				}
			}
		}

		// Alle alten Server, die nicht mehr aktuell sind, entfernen
		while (!currentInstances.isEmpty()) {
			Application oldApp = currentInstances.remove(0);
			try {
				l.rapiInstanceRemoved(oldApp);
			} catch (Exception e) {
				RAPILogger.logException(getClass(), e);
			}
		}

		currentInstances.addAll(newInstances);
	}

}
