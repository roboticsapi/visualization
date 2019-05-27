/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/. 
 *
 * Copyright 2010-2017 ISSE, University of Augsburg 
 */

package org.roboticsapi.feature.visualization.viewer.navigation.engine;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.roboticsapi.feature.visualization.viewer.navigation.event.EventListener;
import org.roboticsapi.feature.visualization.viewer.navigation.event.EventListener.Button;
import org.roboticsapi.feature.visualization.viewer.navigation.event.EventListener.Event;
import org.roboticsapi.feature.visualization.viewer.navigation.event.EventManager;
import org.roboticsapi.feature.visualization.viewer.navigation.visualization.NavigationInterface;
import org.roboticsapi.feature.visualization.viewer.navigation.visualization.Object3D;

public final class NavigationManager {

	private final double MIN_TILT = -Math.toRadians(89);
	private final double MAX_TILT = Math.toRadians(89);
	private final double MIN_DISTANCE_TO_GROUND = 0.12;

	private final Set<Button> buttons;
	private final NavigationInterface visualization;
	private int xMouse, yMouse;
	private State currentState = null;
	private final State freeFlightState;
//	private final State objectState;
	private Object3D selectedObject;
	private final EventManager eventManager;

	private final EventListener eventListener = new MyEventListener();
	private final List<SelectionListener> selectionListeners = new ArrayList<SelectionListener>();

	public NavigationManager(NavigationInterface visualizationApi, EventManager eventManager,
			NavigationProperties configuration) {

		this.eventManager = eventManager;
		buttons = new HashSet<Button>();
		xMouse = 0;
		yMouse = 0;
		visualization = visualizationApi;

		freeFlightState = new FreeFlightState(configuration, MIN_TILT, MAX_TILT, MIN_DISTANCE_TO_GROUND);
//		objectState = new ObjectState(configuration, MIN_TILT, MAX_TILT, MIN_DISTANCE_TO_GROUND);

		setCurrentState(freeFlightState);

		Thread thread = new Thread(executor);
		thread.setDaemon(true);
		thread.setName("Visualization navigation event manager");
		thread.start();

		eventManager.addListener(eventListener);
	}

	// change the active state
	private void setCurrentState(State state) {
		synchronized (lock) {
			if (currentState != null) {
				currentState.deactivate();
			}
			currentState = state;
			currentState.activate(new HashSet<Button>(buttons), visualization, xMouse, yMouse, null);

			if (currentState instanceof ThreadState) {
				lock.notify();
			}
		}
	}

	/**
	 * changes the currentState dependent on the currentState and the Event
	 * 
	 * @param event which is triggered by the EventListener
	 * @return if the currentState has been changed return true, else false
	 */
	private boolean changeState(Event event) {

		// Track mouse position
		xMouse = event.getXPosition();
		yMouse = event.getYPosition();

		// needs to be done independent from the states
		stateIndependentActions(event);

//		if (event.isDown()) {
//			if (event.getButton() == KeyButton.SPACE && selectedObject != null) {
//				if (currentState.equals(freeFlightState)) {
//					setCurrentState(objectState);
//					return true;
//				}
//			} else if (event.getButton() == KeyButton.ESCAPE) {
//				if (currentState.equals(objectState)) {
//					setCurrentState(freeFlightState);
//					return true;
//				}
//			}
//
//		}

		return false;
	}

	public void stateIndependentActions(Event event) {
//		// do, not matter which state is used right now
//		if (event.isDown()) {
//			// adjust the movementspeed
//			if (event.getButton() == KeyButton.CONTROL) {
//				TransformationUtil.movementMultiplier *= TransformationUtil.multiplier;
//			} else if (event.getButton() == KeyButton.SHIFT) {
//				TransformationUtil.movementMultiplier /= TransformationUtil.multiplier;
//			} else if (event.getButton() == KeyButton.PLUS) {
//				TransformationUtil.multiplier++;
//			} else if (event.getButton() == KeyButton.MINUS) {
//				if (TransformationUtil.multiplier > 1)
//					TransformationUtil.multiplier--;
//			}
//			// deselect Object
//			else if (event.getButton() == KeyButton.ESCAPE && selectedObject != null) {
//				setSelectedAndNotifyListeners(null);
//			}
//
//			buttons.add(event.getButton());
//		} else {
//			if (event.getButton() == KeyButton.CONTROL) {
//				TransformationUtil.movementMultiplier /= TransformationUtil.multiplier;
//			}
//			if (event.getButton() == KeyButton.SHIFT) {
//				TransformationUtil.movementMultiplier *= TransformationUtil.multiplier;
//			}
//			buttons.remove(event.getButton());
//		}
	}

	public void addSelectionListener(SelectionListener l) {
		selectionListeners.add(l);
	}

	public void removeSelectionListener(SelectionListener l) {
		selectionListeners.remove(l);
	}

	public void setSelected(Object3D object3d) {
		if (selectedObject != null) {
			visualization.setHighlighted(selectedObject, false);
		}
		selectedObject = object3d;
		if (selectedObject != null) {
			visualization.setHighlighted(selectedObject, true);
		}
	}

	private void setSelectedAndNotifyListeners(Object3D object3d) {
		Object3D oldSelection = selectedObject;
		setSelected(object3d);
		if (oldSelection != selectedObject) {
			for (SelectionListener l : selectionListeners) {
				l.selectionChanged(selectedObject);
			}
		}
	}

	public void dispose() {
		eventManager.removeListener(eventListener);
		shutDown = true;
		synchronized (lock) {
			while (!isShutDown) {
				lock.notifyAll();
				try {
					lock.wait();
				} catch (InterruptedException e) {
				}
			}
		}
	}

	private boolean shutDown = false;
	private boolean isShutDown = false;
	private final Object lock = new Object();
	private final Thread executor = new Thread(new Runnable() {

		@Override
		public void run() {
			// If the Thread is used by a State, let it work, else go to Sleep
			// and
			// wait for a State to notify it
			while (true) {
				try {
					synchronized (lock) {
						if (shutDown) {
							isShutDown = true;
							lock.notify();
							return;
						}
						if (currentState instanceof ThreadState) {
							((ThreadState) currentState).onThreadRunning();
						} else {
							lock.wait();
						}
					}
					Thread.sleep(20);
				} catch (Exception e) {
					e.getStackTrace();
				}
			}

		}
	});

	private class MyEventListener implements EventListener {
		@Override
		public void onKeyDown(KeyButton keyButton) {
			if (!changeState(Event.getKeyEvent(keyButton, true))) {
				currentState.onKeyDown(keyButton);
			}
		}

		@Override
		public void onKeyUp(KeyButton keyButton) {
			if (!changeState(Event.getKeyEvent(keyButton, false))) {
				currentState.onKeyUp(keyButton);
			}
		}

		@Override
		public void onMouseDown(MouseButton mouseButton, int xAxis, int yAxis) {
			if (!changeState(Event.getMouseEvent(mouseButton, true, xAxis, yAxis))) {
				currentState.onMouseDown(mouseButton, xAxis, yAxis);
			}
			if (mouseButton == MouseButton.MIDDLE) {
				setSelectedAndNotifyListeners(visualization.getFirstCollisionObject(xAxis, yAxis));
			}
		}

		@Override
		public void onMouseUp(MouseButton mouseButton, int xAxis, int yAxis) {
			if (!changeState(Event.getMouseEvent(mouseButton, false, xAxis, yAxis))) {
				currentState.onMouseUp(mouseButton, xAxis, yAxis);
			}
		}

		@Override
		public void onMouseMove(int xAxis, int yAxis) {
			if (!changeState(Event.getMouseMoveEvent(xAxis, yAxis))) {
				currentState.onMouseMove(xAxis, yAxis);
			}
		}

		@Override
		public void onMouseWheel(int direction) {
			if (!changeState(Event.getMouseWheelEvent(direction))) {
				currentState.onMouseWheel(direction);
			}
		}
	}

}
