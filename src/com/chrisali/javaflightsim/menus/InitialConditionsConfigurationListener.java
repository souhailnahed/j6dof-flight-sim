package com.chrisali.javaflightsim.menus;

import java.util.EventListener;

public interface InitialConditionsConfigurationListener extends EventListener {
	public void initialConditonsConfigured(double[] coordinates, double heading, double altitude, double airspeed);
}
