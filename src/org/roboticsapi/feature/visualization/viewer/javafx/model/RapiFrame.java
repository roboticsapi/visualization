/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/. 
 *
 * Copyright 2010-2017 ISSE, University of Augsburg 
 */

package org.roboticsapi.feature.visualization.viewer.javafx.model;

import java.util.ArrayList;
import java.util.List;

import javafx.scene.transform.Transform;

/**
 * Represents a Frame in the frame graph
 */
public class RapiFrame {
	private final String name;
	private boolean root;
	protected List<RapiRelation> relations = new ArrayList<>();
	private RapiRelation parentRelation = null;
	private List<FrameListener> listeners = new ArrayList<>();

	public interface FrameListener {
		void frameChanged(RapiFrame frame);
	}

	public RapiFrame(String name, boolean root) {
		this.name = name;
		this.root = root;
	}

	public RapiFrame(String name) {
		this(name, false);
	}

	public boolean hasParent() {
		return root || parentRelation != null;
	}

	public void dropParent(List<RapiFrame> retDropped) {
		if (!hasParent())
			return;
		if (!parentRelation.getOther(this).hasParent()) {
			parentRelation = null;
			notifyChanged();
			retDropped.add(this);
			dropParentFromLinked(retDropped);
		}
	}

	private void notifyChanged() {
		for (FrameListener listener : listeners)
			listener.frameChanged(this);
	}

	private void dropParentFromLinked(List<RapiFrame> retDropped) {
		for (RapiRelation relation : relations)
			relation.getOther(this).dropParent(retDropped);
	}

	public void addParent(List<RapiFrame> retAdded) {
		if (hasParent())
			return;
		for (RapiRelation relation : relations) {
			if (relation.getOther(this).hasParent()) {
				parentRelation = relation;
				notifyChanged();
				retAdded.add(this);
				addParentToLinked(retAdded);
				return;
			}
		}
	}

	private void addParentToLinked(List<RapiFrame> retAdded) {
		for (RapiRelation relation : relations)
			relation.getOther(this).addParent(retAdded);
	}

	public void addRelation(RapiFrame to, double x, double y, double z, double a, double b, double c) {
		if (getRelation(to) != null)
			return;
		RapiRelation relation = new RapiRelation(this, to, x, y, z, a, b, c);
		relations.add(relation);
		to.relations.add(relation);
		relationAdded(relation);
		to.relationAdded(relation);
	}

	public void removeRelation(RapiFrame to) {
		RapiRelation relation = getRelation(to);
		relations.remove(relation);
		to.relations.remove(relation);
		relationRemoved(relation);
		to.relationRemoved(relation);
	}

	private void relationAdded(RapiRelation relation) {
		if (parentRelation == null && relation.getOther(this).hasParent()) {
			parentRelation = relation;
			notifyChanged();

			addParentToLinked(new ArrayList<>());
		}
	}

	private void relationRemoved(RapiRelation relation) {
		if (relation == parentRelation) {
			parentRelation = null;
			notifyChanged();

			// disconnect all linked frames
			List<RapiFrame> disconnected = new ArrayList<>();
			disconnected.add(this);
			dropParentFromLinked(disconnected);

			// check if any is still connected and reconnect
			List<RapiFrame> reconnected = new ArrayList<>();
			for (RapiFrame candidate : disconnected) {
				candidate.addParent(reconnected);
				if (!reconnected.isEmpty())
					return;
			}
		}
	}

	public RapiRelation getRelation(RapiFrame to) {
		for (RapiRelation relation : relations) {
			if (relation.getFrom() == this && relation.getTo() == to) {
				return relation;
			}
		}
		return null;
	}

	public List<Transform> getTransforms() {
		List<Transform> result = new ArrayList<>();
		if (root) {
			return result;
		} else {
			RapiRelation _parentRelation = parentRelation;
			if (_parentRelation == null) {
				return result;
			} else if (_parentRelation.getTo() == this) {
				result.addAll(_parentRelation.getFrom().getTransforms());
				result.addAll(_parentRelation.getTransform());
			} else {
				result.addAll(_parentRelation.getTo().getTransforms());
				result.addAll(_parentRelation.getInverseTransform());
			}
		}
		return result;
	}

	public String getName() {
		return name;
	}

	@Override
	public String toString() {
		return name;
	}

	public void updateRelation(RapiFrame to, double x, double y, double z, double a, double b, double c) {
		RapiRelation relation = getRelation(to);
		if (relation != null)
			relation.updateTransformation(x, y, z, a, b, c);
	}

	public void performUpdate() {
		for (RapiRelation relation : relations)
			relation.performUpdate();
	}

	public void addListener(FrameListener listener) {
		listeners.add(listener);
	}

	public void removeListener(FrameListener listener) {
		listeners.remove(listener);
	}

	public RapiFrame getParentFrame() {
		return parentRelation == null ? null : parentRelation.getOther(this);
	}

	public boolean isRoot() {
		return root;
	}
}
