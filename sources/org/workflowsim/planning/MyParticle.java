/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.workflowsim.planning;
import net.sourceforge.jswarm_pso.Particle;

/**
 * The PSO Particle Class.
 * @author Meysam Farsi , meysam.hit@gmail.com
 * @date Feb 23, 2018
 */
public class MyParticle extends Particle {

	/** Number of dimentions for this particle */
	 static int NUMBER_OF_DIMENTIONS = 2 ;
	
	
	public MyParticle() {
		super(NUMBER_OF_DIMENTIONS); // Create a 2-dimentional particle

	}

}
