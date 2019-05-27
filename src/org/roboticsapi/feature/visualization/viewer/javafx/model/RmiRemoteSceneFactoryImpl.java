/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/. 
 *
 * Copyright 2010-2017 ISSE, University of Augsburg 
 */

package org.roboticsapi.feature.visualization.viewer.javafx.model;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.LinkedList;

import org.roboticsapi.feature.visualization.VisualizationClientScene;
import org.roboticsapi.feature.visualization.rmi.RmiVisualizationClient;
import org.roboticsapi.feature.visualization.rmi.RmiVisualizationClientScene;

/**
 * Implementation of the RemoteSceneFactory
 * 
 * @see org.roboticsapi.visualization.remote.RemoteSceneFactory*
 */
public abstract class RmiRemoteSceneFactoryImpl extends UnicastRemoteObject implements RmiVisualizationClient {

	protected RmiRemoteSceneFactoryImpl() throws RemoteException {
		super();
	}

	private static final long serialVersionUID = 1L;

	private LinkedList<RemoteSceneImpl> remoteScenes = new LinkedList<>();

	protected abstract void onSceneAdded(RemoteSceneImpl scene);

	protected abstract void onSceneRemoved(RemoteSceneImpl scene);

	@Override
	public final RmiVisualizationClientScene createScene(String name, boolean allowSelection) throws RemoteException {
		// TODO: allowSelection??
		RemoteSceneImpl remote = new RemoteSceneImpl(name);
		remoteScenes.add(remote);
		onSceneAdded(remote);
		return remote;
	}

	@Override
	public final boolean deleteScene(VisualizationClientScene scene) throws RemoteException {
		if (scene instanceof RemoteSceneImpl) {
			RemoteSceneImpl impl = (RemoteSceneImpl) scene;
			impl.deleteScene();
			onSceneRemoved(impl);
			return true;
		}
		return false;
	}

}
