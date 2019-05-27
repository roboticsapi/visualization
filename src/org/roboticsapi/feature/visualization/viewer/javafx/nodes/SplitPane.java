/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/. 
 *
 * Copyright 2010-2017 ISSE, University of Augsburg 
 */

package org.roboticsapi.feature.visualization.viewer.javafx.nodes;

import javafx.animation.Animation;
import javafx.animation.Transition;
import javafx.beans.binding.Bindings;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.util.Duration;

public class SplitPane extends Pane {

	private static final long ANIMATION_TIME = 200;
	private static final double OVERVIEW_WIDTH = 220;

	private final Cursor cursor_default = Cursor.HAND;
	private final Cursor cursor_resize = Cursor.H_RESIZE;

	private double dragXStart;
	private boolean dragged = false;
	private boolean hidden = false;

	private Animation animation = null;
	private final DoubleProperty fadePosition = new SimpleDoubleProperty(0);

	private final BorderPane center = new BorderPane();

	public static enum Direction {
		LEFT, RIGHT, TOP, BOTTOM;
		private boolean isHorizontal() {
			return this == Direction.LEFT || this == Direction.RIGHT;
		}
	}

	public SplitPane(Node split) {
		this(split, null);
	}

	public SplitPane(Node split, Node content) {
		this(Direction.LEFT, split, content);
	}

	public SplitPane(Direction direction, Node split, Node content) {
		BorderPane left = new BorderPane(split);

		Button splitter = new Button();
		splitter.setFocusTraversable(false);
		splitter.setCursor(cursor_default);

		center.setCenter(content);

		if (direction.isHorizontal()) {
			splitter.prefHeightProperty().bind(heightProperty());
			splitter.setPrefWidth(1);
			splitter.setMaxWidth(1);
			center.prefHeightProperty().bind(heightProperty());
			left.prefHeightProperty().bind(heightProperty());
			left.setPrefWidth(OVERVIEW_WIDTH);
			left.maxWidthProperty().bind(widthProperty().subtract(splitter.widthProperty()));
			left.minWidthProperty().bind(Bindings.createDoubleBinding(() -> {
				return Math.min(OVERVIEW_WIDTH, getWidth() - splitter.getWidth());
			}, widthProperty(), splitter.widthProperty()));
		} else {
			splitter.prefWidthProperty().bind(widthProperty());
			splitter.setPrefHeight(1);
			splitter.setMaxHeight(1);
			center.prefWidthProperty().bind(widthProperty());
			left.prefWidthProperty().bind(widthProperty());
			left.setPrefHeight(OVERVIEW_WIDTH);
			left.maxHeightProperty().bind(heightProperty().subtract(splitter.heightProperty()));
			left.minHeightProperty().bind(Bindings.createDoubleBinding(() -> {
				return Math.min(OVERVIEW_WIDTH, getHeight() - splitter.getHeight());
			}, heightProperty(), splitter.heightProperty()));
		}

		if (direction == Direction.LEFT) {
			splitter.translateXProperty().bind(left.translateXProperty().add(left.widthProperty()));
			center.translateXProperty().bind(splitter.translateXProperty().add(splitter.widthProperty()));
			center.prefWidthProperty()
					.bind(widthProperty().subtract(splitter.translateXProperty().add(splitter.widthProperty())));
			left.translateXProperty().bind(fadePosition.multiply(left.widthProperty()));
		} else if (direction == Direction.RIGHT) {
			splitter.translateXProperty().bind(left.translateXProperty().subtract(splitter.widthProperty()));
			center.setTranslateX(0);
			center.prefWidthProperty().bind(splitter.translateXProperty());
			left.translateXProperty().bind(widthProperty().subtract(left.widthProperty())
					.subtract(fadePosition.multiply(left.widthProperty())));
		} else {
			throw new IllegalArgumentException();
		}

		getChildren().addAll(left, splitter, center);

		splitter.setOnAction(e -> {
			if (dragged)
				return;
			if (hidden)
				fadeIn();
			else
				fadeOut();
			hidden = !hidden;
		});
		splitter.setOnMousePressed(e -> {
			if (hidden)
				return;
			splitter.setCursor(cursor_resize);
			dragXStart = e.getSceneX() - left.getWidth();
		});
		splitter.setOnMouseDragged(e -> {
			if (hidden)
				return;
			double newX = e.getSceneX() - dragXStart;
			left.setPrefWidth(Math.max(0, newX));
			dragged = true;
		});
		splitter.setOnMouseReleased(e -> {
			splitter.setCursor(cursor_default);
			dragged = false;
		});

	}

	public void setContent(Node content) {
		center.setCenter(content);
	}

	private void fadeIn() {
		if (animation != null)
			animation.stop();

		animation = new Transition() {
			{
				setCycleDuration(new Duration(ANIMATION_TIME));
			}

			@Override
			protected void interpolate(double frac) {
				fadePosition.set(frac - 1.0);
			}
		};
		animation.play();
	}

	private void fadeOut() {
		if (animation != null)
			animation.stop();

		animation = new Transition() {
			{
				setCycleDuration(new Duration(ANIMATION_TIME));
			}

			@Override
			protected void interpolate(double frac) {
				fadePosition.set(-frac);
			}
		};
		animation.play();
	}
}