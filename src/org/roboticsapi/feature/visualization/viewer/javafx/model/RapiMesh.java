/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/. 
 *
 * Copyright 2010-2017 ISSE, University of Augsburg 
 */

package org.roboticsapi.feature.visualization.viewer.javafx.model;

import org.roboticsapi.feature.visualization.viewer.navigation.visualization.Object3D;

import javafx.scene.Node;

/**
 * Represents the visual model
 */
public class RapiMesh extends Object3D {

	private final Node[] nodes;
	private final String name;
	private final int parentID;
	private final int id;

	public RapiMesh(String name, Node[] nodes, int id, int parentID) {
		this.name = name;
		this.nodes = nodes;
		this.parentID = parentID;
		this.id = id;

	}

	public String getName() {
		return name;
	}

	public Node[] getNodes() {
		return nodes;
	}

	public int getParentID() {
		return parentID;
	}

	public int getID() {
		return id;
	}
}
