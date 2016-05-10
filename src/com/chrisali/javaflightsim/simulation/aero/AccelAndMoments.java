package com.chrisali.javaflightsim.simulation.aero;

import java.util.EnumMap;
import java.util.Set;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

import com.chrisali.javaflightsim.simulation.aircraft.Aircraft;
import com.chrisali.javaflightsim.simulation.aircraft.MassProperties;
import com.chrisali.javaflightsim.simulation.controls.FlightControls;
import com.chrisali.javaflightsim.simulation.enviroment.EnvironmentParameters;
import com.chrisali.javaflightsim.simulation.integration.Integrate6DOFEquations;
import com.chrisali.javaflightsim.simulation.integration.IntegrateGroundReaction;
import com.chrisali.javaflightsim.simulation.integration.SaturationLimits;
import com.chrisali.javaflightsim.simulation.propulsion.Engine;
import com.chrisali.javaflightsim.utilities.Utilities;

/**
 * Calculates total accelerations and moments experienced by the aircraft in the simulation. The constructor creates an
 * {@link Aerodynamics} object to calculate aerodynamic forces and moments, which are then added to other various forces 
 * (ground reaction, wind, engine, etc) to yield accelerations and moments used by {@link Integrate6DOFEquations} in its 
 * numerical integration
 * @see Source: <i>Small Unmanned Aircraft: Theory and Practice by Beard, R.W. and McLain, T.W.</i>
 */
public class AccelAndMoments {
	
	private Aerodynamics aero;
	
	/**
	 * Creates an object whose purpose is to calculate total accelerations and moments. It uses the {@link Aircraft} argument 
	 * to create an {@link Aerodynamics} object which calculates aerodynamic forces and moments associated with the Aircraft
	 * object passed in
	 *  
	 * @param aircraft
	 */
	public AccelAndMoments(Aircraft aircraft) {this.aero = new Aerodynamics(aircraft);}
	
	
	/**
	 * Calculates the total linear acceleration experienced by the aircraft (ft/sec^2)
	 * 
	 * @param windParameters
	 * @param angularRates
	 * @param environmentParameters
	 * @param controls
	 * @param alphaDot
	 * @param engineList
	 * @param aircraft
	 * @param groundReaction
	 * @return linearAccelerations
	 */
	public double[] calculateLinearAccelerations(double[] windParameters,
										         double[] angularRates,
										         EnumMap<EnvironmentParameters, Double> environmentParameters,
										         EnumMap<FlightControls, Double> controls,
										         double alphaDot,
										         Set<Engine> engineList,
										         Aircraft aircraft,
										         IntegrateGroundReaction groundReaction) {
		
		Vector3D aeroForceVector = new Vector3D(aero.calculateBodyForces(windParameters, 
																	     angularRates, 
																	     environmentParameters, 
																	     controls, 
																	     alphaDot));
		
		Vector3D groundForceVector = new Vector3D(groundReaction.getTotalGroundForces());
		
		// Create a vector of engine force, iterate through engineList and add the thrust of each engine in list
		Vector3D engineForce = new Vector3D(0, 0, 0);
		for (Engine engine : engineList)
			engineForce = engineForce.add(new Vector3D(engine.getThrust()));

		double[] tempLinearAccel = aeroForceVector
										.add(engineForce)
										.add(groundForceVector)
										.scalarMultiply(1/aircraft.getMassProperty(MassProperties.TOTAL_MASS))
										.toArray(); 
		
		return SaturationLimits.limitLinearAccelerations(tempLinearAccel);
	}
	
	/**
	 * Calculates the total moment experienced by the aircraft (lb ft)
	 * 
	 * @param windParameters
	 * @param angularRates
	 * @param environmentParameters
	 * @param controls
	 * @param alphaDot
	 * @param engineList
	 * @param aircraft
	 * @param groundReaction
	 * @return totalMoments
	 */
	public double[] calculateTotalMoments(double[] windParameters,
										  double[] angularRates,
										  EnumMap<EnvironmentParameters, Double> environmentParameters,
										  EnumMap<FlightControls, Double> controls,
										  double alphaDot,
										  Set<Engine> engineList,
										  Aircraft aircraft,
										  IntegrateGroundReaction groundReaction) {

		Vector3D aeroForceVector = new Vector3D(aero.calculateBodyForces(windParameters, 
																	     angularRates, 
																	     environmentParameters, 
																	     controls, 
																	     alphaDot));
		
		// Apache Commons vector methods only accept primitive double[] arrays
		Vector3D acVector = new Vector3D(Utilities.unboxDoubleArray(aircraft.getAerodynamicCenter()));
		Vector3D cgVector = new Vector3D(Utilities.unboxDoubleArray(aircraft.getCenterOfGravity()));
		
		Vector3D aeroForceCrossProd = Vector3D.crossProduct(aeroForceVector, acVector.subtract(cgVector));
		
		Vector3D aeroMomentVector = new Vector3D(aero.calculateAeroMoments(windParameters, 
																		   angularRates, 
																		   environmentParameters, 
																		   controls, 
																		   alphaDot)); 
		
		Vector3D groundMomentVector = new Vector3D(groundReaction.getTotalGroundMoments());
		
		// Create a vector of engine moment, iterate through engineList and add the moment of each engine in list
		Vector3D engineMoment = new Vector3D(0, 0, 0);
		for (Engine engine : engineList)
			engineMoment = engineMoment.add(new Vector3D(engine.getEngineMoment()));
		
		double[] tempTotalMoments = aeroMomentVector
										.add(engineMoment)
										.add(aeroForceCrossProd)
										.add(groundMomentVector)
										.toArray();
		
		return SaturationLimits.limitTotalMoments(tempTotalMoments); 
	}
}
