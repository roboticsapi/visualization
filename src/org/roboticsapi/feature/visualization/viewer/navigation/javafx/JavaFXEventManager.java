/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/. 
 *
 * Copyright 2010-2017 ISSE, University of Augsburg 
 */

package org.roboticsapi.feature.visualization.viewer.navigation.javafx;

import java.util.ArrayList;
import java.util.List;

import org.roboticsapi.feature.visualization.viewer.navigation.event.EventListener;
import org.roboticsapi.feature.visualization.viewer.navigation.event.EventListener.KeyButton;
import org.roboticsapi.feature.visualization.viewer.navigation.event.EventManager;

import javafx.event.Event;
import javafx.geometry.Point2D;
import javafx.scene.Cursor;
import javafx.scene.Parent;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;

public final class JavaFXEventManager implements EventManager {

	protected List<EventListener> listeners = new ArrayList<EventListener>();

	private double mousex = 0;
	private double mousey = 0;
	double mouseaxisx = 0;
	double mouseaxisy = 0;

	private interface ListenerNotifier {
		public void run(EventListener l);
	}

	private void notifyListener(ListenerNotifier ln) {
		for (EventListener listener : listeners) {
			try {
				ln.run(listener);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public JavaFXEventManager(Parent parent) {
		parent.addEventFilter(Event.ANY, (event) -> {
			Point2D parentOffset = parent.localToScene(0, 0);
			if (event.getEventType() == KeyEvent.KEY_PRESSED) {
				notifyListener(l -> l.onKeyDown(convertJavafXKeyCodeToKeyButton(((KeyEvent) event).getCode())));
			} else if (event.getEventType() == KeyEvent.KEY_RELEASED) {
				notifyListener(l -> l.onKeyUp(convertJavafXKeyCodeToKeyButton(((KeyEvent) event).getCode())));
			} else if (event.getEventType() == MouseEvent.MOUSE_PRESSED) {
				parent.setCursor(Cursor.NONE);
				MouseEvent mouseEvent = (MouseEvent) event;
				int x = (int) (mouseEvent.getSceneX() - parentOffset.getX()),
						y = (int) (mouseEvent.getSceneY() - parentOffset.getY());
				notifyListener(l -> {
					l.onMouseDown(getEventListenerMouseButton(mouseEvent.getButton()), x, y);
				});
				mousex = x;
				mousey = y;
			} else if (event.getEventType() == MouseEvent.MOUSE_RELEASED) {
				parent.setCursor(Cursor.DEFAULT);
				MouseEvent mouseEvent = (MouseEvent) event;
				int x = (int) (mouseEvent.getSceneX() - parentOffset.getX()),
						y = (int) (mouseEvent.getSceneY() - parentOffset.getY());
				notifyListener(l -> l.onMouseUp(getEventListenerMouseButton(mouseEvent.getButton()), x, y));
			} else if (event.getEventType() == MouseEvent.MOUSE_DRAGGED) {
				MouseEvent mouseEvent = (MouseEvent) event;
				int x = (int) (mouseEvent.getSceneX() - parentOffset.getX()),
						y = (int) (mouseEvent.getSceneY() - parentOffset.getY());

				mouseaxisx += x - mousex;
				mouseaxisy += y - mousey;

				mousex = x;
				mousey = y;

				notifyListener(l -> l.onMouseMove((int) mouseaxisx, (int) mouseaxisy));
			} else if (event.getEventType() == ScrollEvent.SCROLL) {
				notifyListener(l -> l.onMouseWheel((int) ((ScrollEvent) event).getDeltaX()));
			}
			event.consume();
		});
	}

	@Override
	public void addListener(EventListener listener) {
		listeners.add(listener);
	}

	@Override
	public void removeListener(EventListener listener) {
		listeners.remove(listener);
	}

	public static EventListener.KeyButton convertJavafXKeyCodeToKeyButton(KeyCode keyCode) {
		try {
			return KeyButton.valueOf(keyCode.name());
		} catch (Exception e) {
		}
		return null;
	}

	private EventListener.MouseButton getEventListenerMouseButton(javafx.scene.input.MouseButton mouseButton) {
		if (mouseButton == javafx.scene.input.MouseButton.PRIMARY)
			return EventListener.MouseButton.PRIMARY;
		if (mouseButton == javafx.scene.input.MouseButton.SECONDARY)
			return EventListener.MouseButton.SECONDARY;
		if (mouseButton == javafx.scene.input.MouseButton.MIDDLE)
			return EventListener.MouseButton.MIDDLE;
		return null;
	}

}
