package com.chrisali.javaflightsim.interfaces;

import com.chrisali.javaflightsim.initializer.LWJGLSwingSimulationController;
import com.chrisali.javaflightsim.simulation.setup.SimulationConfiguration;

/**
 * Controls the starting and stopping of the simulation component of JavaFlightSimulator.
 * Add configuration and OTW/network repeater initialization options to an implementation
 * of this interface to as needed.
 * 
 * @see LWJGLSwingSimulationController
 * @author Chris Ali
 *
 */
public interface SimulationController {
	
	public SimulationConfiguration getConfiguration();
	
	public void plotSimulation();
	
	public void initializeConsole();
	
	public boolean isPlotWindowVisible();
	
	public void startSimulation();

	public void stopSimulation();
}