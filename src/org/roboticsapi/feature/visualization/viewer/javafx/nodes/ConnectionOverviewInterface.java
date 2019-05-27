/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/. 
 *
 * Copyright 2010-2017 ISSE, University of Augsburg 
 */

package org.roboticsapi.feature.visualization.viewer.javafx.nodes;

import java.util.Arrays;

import javafx.scene.image.Image;

@SuppressWarnings("restriction")
public interface ConnectionOverviewInterface {

	public HostID addServer(String host, boolean autoscroll);

	public void setServerState(HostID hostId, boolean online) throws UnknownIdentifierException;

	public AppID addApplication(HostID hostId, String name) throws UnknownIdentifierException;

	public void setApplicationActive(AppID appId, boolean active) throws UnknownIdentifierException;

	public void setConnectionState(AppID applicationId, boolean connected) throws UnknownIdentifierException;

	public SceneID addScene(AppID applicationId, String name, String description) throws UnknownIdentifierException;

	public void setSceneThumbnail(SceneID sceneId, Image thumbnail) throws UnknownIdentifierException;

	public void setSelection(SceneID[] sceneIds) throws UnknownIdentifierException;

	public void removeServer(HostID hostId) throws UnknownIdentifierException;

	public void removeApplication(AppID applicationId) throws UnknownIdentifierException;

	public void removeScene(SceneID sceneId) throws UnknownIdentifierException;

	public static interface ControlListener {

		public void newServerAdded(HostID hostId, String host);

		public void beforeServerRemoved(HostID hostId);

		public void connectionStateRequested(HostID hostId, AppID applicationId, boolean connect);

		public void switchAutodiscover(boolean on);

		public void selectionChanged(SelectionItem[] selectedSceneIds);

		public static interface SelectionItem {
			public SceneID getSceneId();

			public AppID getApplicationId();

			public HostID getHostId();
		}
	}

	public class UnknownIdentifierException extends Exception {
		private static final long serialVersionUID = 1L;

		public UnknownIdentifierException(Identifier identifier) {
			super("Unknown identifier '" + identifier + "'.");
		}

		public UnknownIdentifierException(SceneID[] identifiers) {
			super("Unknown identifiers: " + Arrays.toString(identifiers) + ".");
		}
	}

	public abstract class Identifier {
		private String name;

		public Identifier(String name) {
			this.name = name;
		}

		@Override
		public String toString() {
			return name;
		}
	}

	public final class HostID extends Identifier {
		public HostID(String name) {
			super(name);
		}
	}

	public final class AppID extends Identifier {
		public AppID(String name) {
			super(name);
		}
	}

	public final class SceneID extends Identifier {
		public SceneID(String name) {
			super(name);
		}
	}

}
