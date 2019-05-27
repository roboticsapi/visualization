/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/. 
 *
 * Copyright 2010-2017 ISSE, University of Augsburg 
 */

package org.roboticsapi.feature.visualization.viewer.javafx.nodes;

import java.util.ArrayList;
import java.util.List;

import org.roboticsapi.feature.visualization.viewer.javafx.model.RapiFrame;
import org.roboticsapi.feature.visualization.viewer.javafx.model.RemoteSceneImpl.FrameListener;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;

public class FrameTreePane extends TreeView<RapiFrame> implements FrameListener {

	public interface FrameSelectionListener {
		public void frameSelected(RapiFrame frame);
	}

	private TreeItem<RapiFrame> root = new TreeItem<>(null);
	private List<FrameSelectionListener> listeners = new ArrayList<>();

	public void addSelectionListener(FrameSelectionListener listener) {
		listeners.add(listener);
	}

	public void removeSelectionListener(FrameSelectionListener listener) {
		listeners.remove(listener);
	}

	public FrameTreePane() {
		setRoot(root);
		setShowRoot(false);
		getSelectionModel().selectedItemProperty().addListener(new ChangeListener<TreeItem<RapiFrame>>() {
			@Override
			public void changed(ObservableValue<? extends TreeItem<RapiFrame>> observable, TreeItem<RapiFrame> oldValue,
					TreeItem<RapiFrame> newValue) {
				for (FrameSelectionListener listener : listeners)
					listener.frameSelected(newValue == null ? null : newValue.getValue());
			}
		});
	}

	@Override
	public void frameAdded(RapiFrame frame) {
		frame.addListener(this::updateFrame);
		updateFrame(frame);
	}

	public void updateFrame(RapiFrame frame) {
		Platform.runLater(() -> {
			removeTreeItemForFrame(frame);
			if (frame.getParentFrame() != null || frame.isRoot())
				addTreeItemForFrame(frame);
		});
	}

	private TreeItem<RapiFrame> addTreeItemForFrame(RapiFrame frame) {
		TreeItem<RapiFrame> existing = findItem(frame);
		if (existing != null)
			return existing;
		RapiFrame parent = frame.getParentFrame();
		if (parent == null && !frame.isRoot())
			return null;
		TreeItem<RapiFrame> parentItem = addTreeItemForFrame(parent);
		if (parentItem == null)
			return null;
		TreeItem<RapiFrame> added = new TreeItem<>(frame);
		parentItem.getChildren().add(added);
		if (parentItem != null && parentItem.getParent() == root)
			parentItem.setExpanded(true);
		return added;
	}

	@Override
	public void frameRemoved(RapiFrame frame) {
		frame.removeListener(this::updateFrame);
		Platform.runLater(() -> removeTreeItemForFrame(frame));
	}

	public void removeTreeItemForFrame(RapiFrame frame) {
		TreeItem<RapiFrame> item = findItem(frame);
		if (item == null) {
			item = findItem(frame, root);
		}
		if (item == null)
			return;
		item.getParent().getChildren().remove(item);
	}

	private TreeItem<RapiFrame> findItem(RapiFrame frame, TreeItem<RapiFrame> parent) {
		for (TreeItem<RapiFrame> child : parent.getChildren()) {
			if (child.getValue() == frame)
				return child;
			TreeItem<RapiFrame> ret = findItem(frame, child);
			if (ret != null)
				return ret;
		}
		return null;
	}

	private TreeItem<RapiFrame> findItem(RapiFrame frame) {
		if (frame == null)
			return root;
		TreeItem<RapiFrame> parent = findItem(frame.getParentFrame());
		if (parent == null)
			return null;
		for (TreeItem<RapiFrame> child : parent.getChildren()) {
			if (child.getValue() == frame)
				return child;
		}
		return null;
	}

	public void deselect() {
		Platform.runLater(() -> {
			getSelectionModel().clearSelection();
		});
	}

}
