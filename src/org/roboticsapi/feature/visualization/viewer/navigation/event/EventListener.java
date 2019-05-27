/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/. 
 *
 * Copyright 2010-2017 ISSE, University of Augsburg 
 */

package org.roboticsapi.feature.visualization.viewer.navigation.event;

public interface EventListener {

	public void onKeyDown(KeyButton keyButton);

	public void onKeyUp(KeyButton keyButton);

	public void onMouseDown(MouseButton mouseButton, int xAxis, int yAxis);

	public void onMouseUp(MouseButton mouseButton, int xAxis, int yAxis);

	public void onMouseMove(int xAxis, int yAxis);

	public void onMouseWheel(int direction);

	public class Event {

		private Button button;
		private boolean down;
		private int xAxis;
		private int yAxis;
		private int wheelDirection;

		private Event(Button key, boolean down) {
			this.button = key;
			this.down = down;
		}

		private Event(MouseButton mouse, boolean down, int xAxis, int yAxis) {
			this.button = mouse;
			this.down = down;
			this.xAxis = xAxis;
			this.yAxis = yAxis;
		}

		private Event(int wheelDirection) {
			this.wheelDirection = wheelDirection;
		}

		private Event(int xAxis, int yAxis) {
			this.xAxis = xAxis;
			this.yAxis = yAxis;
		}

		public static Event getKeyEvent(KeyButton key, boolean down) {
			return new Event(key, down);
		}

		public static Event getMouseEvent(MouseButton mouse, boolean down, int xAxis, int yAxis) {
			return new Event(mouse, down, xAxis, yAxis);
		}

		public static Event getMouseWheelEvent(int direction) {
			return new Event(direction);
		}

		public static Event getMouseMoveEvent(int xAxis, int yAxis) {
			return new Event(xAxis, yAxis);
		}

		public Button getButton() {
			return button;
		}

		public boolean isDown() {
			return down;
		}

		public int getXPosition() {
			return xAxis;
		}

		public int getYPosition() {
			return yAxis;
		}

		public int getWheelDirection() {
			return wheelDirection;
		}

	}

	public interface Button {
	}

	public static enum MouseButton implements Button {
		PRIMARY, SECONDARY, MIDDLE;
	}

	public static enum KeyButton implements Button {
		SHIFT, ALT, CONTROL, ESCAPE, SPACE, PLUS, MINUS, UP, DOWN, LEFT, RIGHT, PAGE_UP, PAGE_DOWN, DIGIT0, DIGIT1,
		DIGIT2, DIGIT3, DIGIT4, DIGIT5, DIGIT6, DIGIT7, DIGIT8, DIGIT9, A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P,
		Q, R, S, T, U, V, W, X, Y, Z;
	}
}
