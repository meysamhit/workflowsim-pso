/**
 * Copyright 2012-2013 University Of Southern California
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.workflowsim.planning;


import org.workflowsim.CondorVM;
import org.workflowsim.Task;

import net.sourceforge.jswarm_pso.Neighborhood;
import net.sourceforge.jswarm_pso.Neighborhood1D;
import net.sourceforge.jswarm_pso.Swarm;
import net.sourceforge.jswarm_pso.example_2.SwarmShow2D;

/**
 * The PSO planning algorithm.
 *
 * @author Meysam Farsi , meysam.hit@gmail.com
 * @date Feb 23, 2018
 */
public class PSOAlgorithm extends BasePlanningAlgorithm {

    /**
     * The main function
     */
    @Override
    public void run() {

        int minPos = 1;
        int MaxPos = getVmList().size();

        int maxParticle = getTaskList().size(); 

        MyParticle.NUMBER_OF_DIMENTIONS = maxParticle;

        Swarm swarm = new Swarm(Swarm.DEFAULT_NUMBER_OF_PARTICLES, new MyParticle(),
                new MyFitnessFunction(getTaskList(), getVmList()));

        // Use neighborhood
        Neighborhood neigh = new Neighborhood1D(Swarm.DEFAULT_NUMBER_OF_PARTICLES / 5, true);
        swarm.setNeighborhood(neigh);
        swarm.setNeighborhoodIncrement(0.9);

        // Set position (and velocity) constraints. I.e.: where to look for solutions
        swarm.setInertia(0.9);
        swarm.setMaxPosition(MaxPos);
        swarm.setMinPosition(minPos);
        swarm.setMaxMinVelocity(0.5);

        int numberOfIterations = 100;
        boolean showGraphics = false;

        if (showGraphics) {
            int displayEvery = numberOfIterations / 100 + 1;
            SwarmShow2D ss2d = new SwarmShow2D(swarm, numberOfIterations, displayEvery, true);
            ss2d.run();
        } else {
            // Optimize (and time it)
            for (int i = 0; i < numberOfIterations; i++) {
                swarm.evolve();
            }
        }

        double[] psoAnswer = swarm.getBestPosition();

        int i = 0;
        for (Task task : getTaskList()) {
            int vmID = (int) Math.round(psoAnswer[i] - 1);
            CondorVM vm = (CondorVM) getVmList().get(vmID);
            task.setVmId(vm.getId());
            i++;
        }

        System.out.println(swarm.toStringStats());

    }
}
