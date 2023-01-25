package tipsystem.utils;
import java.util.List;

import tipsystem.tips.TIPSSplashActivity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

public class OrientationManager {
	private static Sensor sensor;
	private static SensorManager sensorManager;
	// 하나 이상의 리스터를 사용하려면 OrientationListener 배열을 사용해도 된다.
	private static OrientationListener listener;
	/** Orientation Sensor가 지워되는지 여부 */
	private static Boolean supported;
	/** Orientation Sensor 가 실행중인지 여부 */
	private static boolean running = false;
	

	/** 폰의 사이드 */
	enum Side {
	TOP,
	BOTTOM,
	LEFT,
	RIGHT;
	}
	/**
	* 매니저가 방위변경을 리스닝하고 있으면 true를 반환
	*/
	public static boolean isListening() {
	return running;
	}
	/**
	* 리스너 등록 해제
	*/
	public static void stopListening() {
	running = false;
	try {
	if (sensorManager != null && sensorEventListener != null) {
	sensorManager.unregisterListener(sensorEventListener);
	}
	} catch (Exception e) {}
	}
	/**
	* 하나이상의 방위센서가 사용가능하면 true를 반환한다.
	*/
	public static boolean isSupported() {
		if (supported == null) {
			//sensorManager = (SensorManager) TIPSSplashActivity.getContext().getSystemService(Context.SENSOR_SERVICE);
			List<Sensor> sensors = sensorManager.getSensorList(
			Sensor.TYPE_ORIENTATION);
			supported = new Boolean(sensors.size() > 0);
		} else {
			supported = Boolean.FALSE;
		}
		return supported;
	}
	/**
	* 리스너를 등록하고 스타트 시킨다.
	*/
	public static void startListening(OrientationListener orientationListener) {

		//sensorManager = (SensorManager) TIPSSplashActivity.getContext().getSystemService(Context.SENSOR_SERVICE);
		List<Sensor> sensors = sensorManager.getSensorList(
		Sensor.TYPE_ORIENTATION);
		if (sensors.size() > 0) {
			sensor = sensors.get(0);
			running = sensorManager.registerListener(
			sensorEventListener, sensor,
			SensorManager.SENSOR_DELAY_NORMAL);
			listener = orientationListener;
		}
	}
	/**
	* 방위센서로부터 발생되는 이벤트를 받아서 처리할 리스너설정
	*/
	private static SensorEventListener sensorEventListener =
	new SensorEventListener() {
	/** The side that is currently up */
	private Side currentSide = null;
	private Side oldSide = null;
	private float azimuth;
	private float pitch;
	private float roll;
	public void onAccuracyChanged(Sensor sensor, int accuracy) {}
	public void onSensorChanged(SensorEvent event) {
	azimuth = event.values[0]; // azimuth
	pitch = event.values[1]; // pitch
	roll = event.values[2]; // roll
	if (pitch < -45 && pitch > -135) {
	// 위를 보고있다.
	currentSide = Side.TOP;
	} else if (pitch > 45 && pitch < 135) {
	// 아래를 보고 있다.
	currentSide = Side.BOTTOM;
	} else if (roll > 45) {
	// 오른쪽이 올라갔다.
	currentSide = Side.RIGHT;
	} else if (roll < -45) {
	// 왼쪽이 올라갔다.
	currentSide = Side.LEFT;
	}
	if (currentSide != null && !currentSide.equals(oldSide)) {
	switch (currentSide) {
	case TOP :
	listener.onTopUp();
	break;
	case BOTTOM :
	listener.onBottomUp();
	break;
	case LEFT:
	listener.onLeftUp();
	break;
	case RIGHT:
	listener.onRightUp();
	break;
	}
	oldSide = currentSide;
	}
	// forwards orientation to the OrientationListener
	listener.onOrientationChanged(azimuth, pitch, roll);
	}
	};
	}