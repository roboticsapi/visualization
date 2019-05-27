/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/. 
 *
 * Copyright 2010-2017 ISSE, University of Augsburg 
 */

package org.roboticsapi.feature.visualization.viewer.javafx;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Properties;

import org.roboticsapi.feature.visualization.viewer.javafx.view.CameraProperties;
import org.roboticsapi.feature.visualization.viewer.navigation.engine.NavigationProperties;
import org.roboticsapi.feature.visualization.viewer.navigation.event.EventListener.KeyButton;
import org.roboticsapi.feature.visualization.viewer.navigation.event.EventListener.MouseButton;

import javafx.geometry.BoundingBox;
import javafx.geometry.Bounds;

public final class Configuration extends Properties
		implements NavigationProperties, WindowProperties, CameraProperties {
	private static final long serialVersionUID = 1L;

	private final File file;

	public static enum Action {
		MOVE_UP, MOVE_DOWN, MOVE_LEFT, MOVE_RIGHT, MOVE_FORWARD, MOVE_BACKWARD, ROTATE_LOOK, ROTATE_ORBIT, FOCUS, PAN,
		MOVEMENT_SPEED, ZOOM_SPEED, ANGULAR_SPEED, OBSERVED_HOSTS, WINDOW_BOUNDS, WINDOW_MAXIMIZED, CAMERA_STARTPOSITION
	}

	public Configuration(File file) {
		super(createDefaultProperties());
		this.file = file;
		loadFromFile();
	}

	private static Properties createDefaultProperties() {
		Properties p = new Properties();
		p.setProperty(Action.MOVE_UP.name(), KeyButton.Q.name());
		p.setProperty(Action.MOVE_DOWN.name(), KeyButton.E.name());
		p.setProperty(Action.MOVE_LEFT.name(), KeyButton.A.name());
		p.setProperty(Action.MOVE_RIGHT.name(), KeyButton.D.name());
		p.setProperty(Action.MOVE_FORWARD.name(), KeyButton.W.name());
		p.setProperty(Action.MOVE_BACKWARD.name(), KeyButton.S.name());

		p.setProperty(Action.ROTATE_LOOK.name(), MouseButton.PRIMARY.name());
		p.setProperty(Action.ROTATE_ORBIT.name(), MouseButton.SECONDARY.name());
		p.setProperty(Action.FOCUS.name(), MouseButton.SECONDARY.name());
		p.setProperty(Action.PAN.name(), MouseButton.SECONDARY.name());

		p.setProperty(Action.MOVEMENT_SPEED.name(), "" + 0.07);
		p.setProperty(Action.ZOOM_SPEED.name(), "" + 20);
		p.setProperty(Action.ANGULAR_SPEED.name(), "" + 0.003);

		p.setProperty(Action.OBSERVED_HOSTS.name(), "localhost");

		// Window properties
		p.setProperty(Action.WINDOW_BOUNDS.name(), "");
		p.setProperty(Action.WINDOW_MAXIMIZED.name(), "false");

		return p;
	}

	private void loadFromFile() {
		if (file == null)
			return;
		FileInputStream in = null;
		try {
			in = new FileInputStream(file);
			loadFromXML(in);
		} catch (IOException e) {
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (Exception e) {
				}
			}
		}
	}

	private void storeToFile() {
		if (file == null)
			return;
		FileOutputStream out = null;
		try {
			out = new FileOutputStream(file);
			storeToXML(out, "");
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (out != null) {
				try {
					out.close();
				} catch (Exception e) {
				}
			}
		}
	}

	public KeyButton getKeyButton(Action key) {
		String buttonName = getProperty(key.name());
		if (buttonName == null)
			return null;
		try {
			return KeyButton.valueOf(buttonName);
		} catch (Exception e) {
			return null;
		}
	}

	public void setKeyButton(Action key, KeyButton keyButton) {
		setProperty(key.name(), keyButton == null ? "" : keyButton.name());
		storeToFile();
	}

	public MouseButton getMouseButton(Action key) {
		String buttonName = getProperty(key.name());
		if (buttonName == null)
			return null;
		try {
			return MouseButton.valueOf(buttonName);
		} catch (Exception e) {
			return null;
		}
	}

	public void setMouseButton(Action key, MouseButton mouseButton) {
		setProperty(key.name(), mouseButton == null ? "" : mouseButton.name());
		storeToFile();
	}

	private double getDouble(Action key) {
		String value = getProperty(key.name());
		if (value == null)
			return 0;
		try {
			return Double.valueOf(value);
		} catch (Exception e) {
			return 0;
		}
	}

	public void setDouble(Action key, double value) {
		setProperty(key.name(), "" + value);
		storeToFile();
	}

	private List<String> getStringList(Action key) {
		String value = getProperty(key.name());
		if (value == null || value.isEmpty())
			return new ArrayList<>();
		return Arrays.asList(value.split("[,]"));
	}

	private void setStringList(Action key, Collection<String> value) {
		setProperty(key.name(), String.join(",", value));
		storeToFile();
	}

	@Override
	public double getMovementSpeed() {
		return getDouble(Action.MOVEMENT_SPEED);
	}

	@Override
	public double getZoomSpeed() {
		return getDouble(Action.ZOOM_SPEED);
	}

	@Override
	public double getAngularSpeed() {
		return getDouble(Action.ANGULAR_SPEED);
	}

	@Override
	public KeyButton getMoveUpButton() {
		return getKeyButton(Action.MOVE_UP);
	}

	public void setMoveUpButton(KeyButton keyButton) {
		setKeyButton(Action.MOVE_UP, keyButton);
	}

	@Override
	public KeyButton getMoveDownButton() {
		return getKeyButton(Action.MOVE_DOWN);
	}

	public void setMoveDownButton(KeyButton keyButton) {
		setKeyButton(Action.MOVE_DOWN, keyButton);
	}

	@Override
	public KeyButton getMoveLeftButton() {
		return getKeyButton(Action.MOVE_LEFT);
	}

	public void setMoveLeftButton(KeyButton keyButton) {
		setKeyButton(Action.MOVE_LEFT, keyButton);
	}

	@Override
	public KeyButton getMoveRightButton() {
		return getKeyButton(Action.MOVE_RIGHT);
	}

	public void setMoveRightButton(KeyButton keyButton) {
		setKeyButton(Action.MOVE_RIGHT, keyButton);
	}

	@Override
	public KeyButton getMoveForwardButton() {
		return getKeyButton(Action.MOVE_FORWARD);
	}

	public void setMoveForwardButton(KeyButton keyButton) {
		setKeyButton(Action.MOVE_FORWARD, keyButton);
	}

	@Override
	public KeyButton getMoveBackwardButton() {
		return getKeyButton(Action.MOVE_BACKWARD);
	}

	public void setMoveBackwardButton(KeyButton keyButton) {
		setKeyButton(Action.MOVE_BACKWARD, keyButton);
	}

	@Override
	public MouseButton getRotateLookButton() {
		return getMouseButton(Action.ROTATE_LOOK);
	}

	@Override
	public MouseButton getRotateOrbitButton() {
		return getMouseButton(Action.ROTATE_ORBIT);
	}

	@Override
	public MouseButton getFocusButton() {
		return getMouseButton(Action.FOCUS);
	}

	@Override
	public MouseButton getPanButton() {
		return getMouseButton(Action.PAN);
	}

	public List<String> getObservedHosts() {
		return getStringList(Action.OBSERVED_HOSTS);
	}

	public void setObservedHosts(Collection<String> observedHosts) {
		setStringList(Action.OBSERVED_HOSTS, observedHosts);
	}

	@Override
	public Bounds getWindowBounds() {
		String result = getProperty(Action.WINDOW_BOUNDS.name());
		if (result == null)
			return null;
		try {
			String[] split = result.split("[,]");
			if (split.length != 4)
				return null;
			Integer top = Integer.valueOf(split[0]);
			Integer right = Integer.valueOf(split[1]);
			Integer widht = Integer.valueOf(split[2]);
			Integer height = Integer.valueOf(split[3]);
			return new BoundingBox(top, right, widht, height);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public void setWindowBounds(Bounds bounds) {
		setProperty(Action.WINDOW_BOUNDS.name(), String.join(",", new String[] { "" + (int) bounds.getMinX(),
				"" + (int) bounds.getMinY(), "" + (int) bounds.getWidth(), "" + (int) bounds.getHeight() }));
		storeToFile();
	}

	@Override
	public boolean getWindowMaximized() {
		String result = getProperty(Action.WINDOW_MAXIMIZED.name());
		if ("true".equalsIgnoreCase(result))
			return true;
		return false;
	}

	public void setWindowMaximized(boolean maximized) {
		setProperty(Action.WINDOW_MAXIMIZED.name(), maximized ? "true" : "false");
		storeToFile();
	}

	@Override
	public CameraPosition getCameraStartposition() {
		String result = getProperty(Action.CAMERA_STARTPOSITION.name());
		CameraPosition defaultValue = new CameraPosition(1.5, -3.5, 2, Math.toRadians(111), Math.toRadians(22));
		if (result == null)
			return defaultValue;
		try {
			String[] split = result.split("[,]");
			if (split.length != 5)
				return null;
			double x = Double.valueOf(split[0]);
			double y = Double.valueOf(split[1]);
			double z = Double.valueOf(split[2]);
			double yaw = Double.valueOf(split[3]);
			double pitch = Double.valueOf(split[4]);
			return new CameraPosition(x, y, z, yaw, pitch);
		} catch (Exception e) {
			e.printStackTrace();
			return defaultValue;
		}
	}

	@Override
	public void setCameraStartposition(double x, double y, double z, double yaw, double pitch) {
		setProperty(Action.CAMERA_STARTPOSITION.name(),
				String.join(",", new String[] { "" + x, "" + y, "" + z, "" + yaw, "" + pitch }));
		storeToFile();
	}

}
