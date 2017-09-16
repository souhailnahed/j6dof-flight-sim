/*******************************************************************************
 * Copyright (C) 2016-2017 Christopher Ali
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
 *  If you have any questions about this project, you can visit
 *  the project's GitHub repository at: http://github.com/chris-ali/j6dof-flight-sim/
 ******************************************************************************/
package com.chrisali.javaflightsim.simulation.utilities;

import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import com.chrisali.javaflightsim.simulation.flightcontrols.FlightControlType;
import com.chrisali.javaflightsim.simulation.setup.Options;

/**
 * Contains methods to modify the aircraft's flight controls to generate doublets for dynamic stability analysis,
 * or limiting flight control deflections
 */
public class FlightControlsUtilities {
	
	/**
	 * Main trim values of flight controls to determine default value if doublet input not underway
	 */
	private static Map<FlightControlType, Double> trimControls; 

	/**
	 * Initializes trimControls EnumMap in {@link FlightControlsUtilities}; needs to be called each time controls and
	 * initial conditions are changed so that new trim values can be read from InitialControls.txt
	 */
	public static void init() { trimControls = FileUtilities.readSimulationConfiguration().getInitialControls(); }
	
	/**
	 * Generates a control doublet in the positive and then negative direction, returning to trim value. The start
	 * time defines when the doublet should start, the duration indicates how long the control is held in that direction,
	 * and the amplitude the amount of deflection in one direction. controlInput uses {@link FlightControlType} to select
	 * the desired control to use as a doublet 
	 * 
	 * @param controls
	 * @param time
	 * @param doubletstartTime
	 * @param duration
	 * @param amplitude
	 * @param controlType
	 * @return flightControls EnumMap 
	 */
	public static Map<FlightControlType, Double> makeDoublet(Map<FlightControlType, Double> controls,
															 AtomicInteger actomicTime,
															 Integer doubletstartTime, 
															 Integer duration, 
															 double amplitude, 
															 FlightControlType controlType) {
		Integer time = actomicTime.get();
		Integer firstHalfEndTime = doubletstartTime + duration;
		Integer doubletEndTime   = doubletstartTime + (2 * duration);
		
		boolean startedFirstHalf = time.compareTo(doubletstartTime) == 1 || time.compareTo(doubletstartTime) == 0;
		boolean endedFirstHalf   = time.compareTo(firstHalfEndTime) == 1 || time.compareTo(firstHalfEndTime) == 0;
		
		boolean startedSecondHalf = time.compareTo(firstHalfEndTime) == 1 || time.compareTo(firstHalfEndTime) == 0;
		boolean endedSecondHalf   = time.compareTo(doubletEndTime)   == 1 || time.compareTo(doubletEndTime)   == 0;
		
		if (startedFirstHalf && !endedFirstHalf)
			controls.put(controlType,trimControls.get(controlType) + amplitude);
		else if (startedSecondHalf && !endedSecondHalf)
			controls.put(controlType,trimControls.get(controlType) - amplitude);
		else 
			controls.put(controlType,trimControls.get(controlType));

		return controls;
	}
	
	/**
	 *  Creates a series of doublets (aileron, rudder and then elevator) using the makeDoublet methods. It is used
	 *  when the simulation is set to {@link Options#ANALYSIS_MODE} to examine the transient dynamic response of 
	 *  the aircraft in the simulation
	 *  
	 * @param controls
	 * @param atomicTime
	 * @return flightControls EnumMap 
	 */
	public static Map<FlightControlType, Double> doubletSeries(Map<FlightControlType, Double> controls, AtomicInteger atomicTime) {
		
		int toMilliseconds = 1000;

		Integer duration 	  = (int)(0.5 * toMilliseconds);
		
		Integer aileronStart  = (int)(10.0 * toMilliseconds);
		Integer rudderStart   = (int)(14.0 * toMilliseconds);
		Integer elevatorStart = (int)(52.0 * toMilliseconds);
		
		// Update controls with an aileron doublet
		controls = makeDoublet(controls, 
							   atomicTime, 
							   aileronStart, 
							   duration, 
							   0.035, 
							   FlightControlType.AILERON);
		// Update controls with a rudder doublet
		controls = makeDoublet(controls, 
							   atomicTime, 
							   rudderStart, 
							   duration, 
							   0.035, 
							   FlightControlType.RUDDER);
		// Update controls with an elevator doublet
		controls = makeDoublet(controls, 
							   atomicTime, 
							   elevatorStart, 
							   duration, 
							   0.035, 
							   FlightControlType.ELEVATOR);
		
		return controls;
	}
	
	/**
	 *  Limit control inputs to sensible deflection values based on the minimum and maximum values defined for 
	 *  each member of {@link FlightControlType}
	 *  
	 * @param map
	 * @return flightControls EnumMap 
	 */
	public static Map<FlightControlType, Double> limitControls(Map<FlightControlType, Double> map) {
		// Loop through enum list; if value in EnumMap controls is greater/less than max/min specified in FlightControls enum, 
		// set that EnumMap value to Enum's max/min value
		for (FlightControlType flc : FlightControlType.values()) {
			if (map.get(flc) > flc.getMaximum())
				map.put(flc, flc.getMaximum());
			else if (map.get(flc) < flc.getMinimum())
				map.put(flc, flc.getMinimum());		
		}
		
		return map;
	}
}
