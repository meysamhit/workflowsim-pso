/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.workflowsim.planning;

import java.util.HashMap;
import java.util.ArrayList;
import java.util.Iterator;
import org.workflowsim.CondorVM;
import net.sourceforge.jswarm_pso.FitnessFunction;
import java.util.List;
import org.workflowsim.FileItem;
import org.workflowsim.Task;
import org.workflowsim.utils.Parameters;

/**
 * The PSO Fitness Function.
 *
 * @author Meysam Farsi , meysam.hit@gmail.com
 * @date Feb 23, 2018
 */
class TaskTime {

    public double startTime;
    public double stopTime;
    public double totalExcuteTime;
    public boolean isMeet;
}

class VM {

    public double VMBusyTime;
}

public class MyFitnessFunction extends FitnessFunction {
    //-------------------------------------------------------------------------
    // Methods
    //-------------------------------------------------------------------------

    List availableVMs;
    List avaliableTasks;
    List avalibaleTaskSorted;
    List Tasks;

    public List getVmList() {
        return availableVMs;
    }

    public List getTaskList() {
        return avaliableTasks;
    }

    public MyFitnessFunction(List TaskList, List VMList) {
        availableVMs = VMList;
        avaliableTasks = TaskList;
    }

    public boolean checkDuplicate(ArrayList<Integer> list, int value) {
        return list.contains(value);
    }

    /**
     * Evaluates a particles at a given position
     *
     * @param position : Particle's position
     * @return Fitness function for a particle
     */
    @Override
    public double evaluate(double position[]) {

        List<TaskTime> taskTime = new ArrayList<>();
        List<VM> vmTime = new ArrayList<>();

        for (Iterator it = getVmList().iterator(); it.hasNext();) {
            Object vmList = it.next();
            VM v = new VM();
            v.VMBusyTime = 0;
            vmTime.add(v);
        }

        int p = 0;
        for (Iterator it = getTaskList().iterator(); it.hasNext();) {

            TaskTime t = new TaskTime();
            Task task = (Task) it.next();

            int pos = (int) Math.round(position[p] - 1);
            CondorVM vm = (CondorVM) getVmList().get(pos);

            if (task.getParentList().isEmpty()) // no parrent
            {

                t.startTime = 0.0;
                double timeToExecute = task.getCloudletLength() / vm.getCurrentRequestedTotalMips();
                t.stopTime = timeToExecute * 1.02;
                t.totalExcuteTime = t.stopTime - t.startTime;
                t.isMeet = true;
                t.stopTime += getInputSize(task) / vm.getBw();

                taskTime.add(t);

                VM tmVM = new VM();
                tmVM.VMBusyTime = t.stopTime;
                vmTime.set(vm.getId(), tmVM);

            } else {

                //find max parrent time
                double maxTime = -1;
                for (Task parrentTask : task.getParentList()) {
                    if (taskTime.get(parrentTask.getCloudletId() - 1).stopTime > maxTime) {
                        maxTime = taskTime.get(parrentTask.getCloudletId() - 1).stopTime;
                    }

                }

                double tmp = vmTime.get(vm.getId()).VMBusyTime;
                if (tmp > maxTime) {
                    maxTime = tmp;
                }

                double timeToExecute = (task.getCloudletLength() / vm.getCurrentRequestedTotalMips());
                TaskTime t2 = new TaskTime();
                t2.startTime = maxTime;
                t2.stopTime = t2.startTime + (timeToExecute) * 1.02;
                t2.totalExcuteTime = t2.stopTime - t2.startTime;

                double maxTransferTime = 0;
                for (Task parrentTask : task.getParentList()) {
                    if (pos != (int) Math.round(position[parrentTask.getCloudletId() - 1] - 1)) {
                        tmp = getOutputSize(parrentTask) / vm.getBw();

                        maxTransferTime += tmp;

                    }
                }

                double inputFileTime = (getInputSize(task) / vm.getBw());
                //System.out.println("ID::: "+task.getCloudletId()+"--Time ::: "+inputFileTime);
                //t2.stopTime += inputFileTime;
                t2.stopTime += maxTransferTime;
                taskTime.add(t2);

                VM tmVM = new VM();
                tmVM.VMBusyTime = t2.stopTime;
                vmTime.set(vm.getId(), tmVM);
                //vmTime.get(vm.getId()).VMBusyTime = t2.stopTime; 
            }

            p++;
        }

        //find max fitness 
        double maxTaskTime = -1;
        for (TaskTime task : taskTime) {

            if (task.stopTime > maxTaskTime) {
                maxTaskTime += task.stopTime;
            }
        }

        maxTaskTime = taskTime.get(taskTime.size() - 1).stopTime;
        //System.out.println("fitness : "+maxTaskTime);
        //System.out.println("position : "+ Arrays.toString(position));
        return (maxTaskTime) * -1.0;

    }

    private double getOutputSize(Task t) {
        double outputSize = 0;
        for (Iterator it = t.getFileList().iterator(); it.hasNext();) {
            FileItem f = (FileItem) it.next();

            if (f.getType() == Parameters.FileType.OUTPUT) {
                outputSize += f.getSize();
            }
        }
        return outputSize / 1000000;
    }

    private double getInputSize(Task t) {
        double inputSize = 0;
        for (Iterator it = t.getFileList().iterator(); it.hasNext();) {
            FileItem f = (FileItem) it.next();
            if (f.getType() == Parameters.FileType.INPUT) {
                inputSize += f.getSize();
            }
        }
        return inputSize / 1000000;
    }
}
