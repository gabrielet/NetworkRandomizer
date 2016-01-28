/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package network.randomizer.internal;

/**
 *
 * @author gabriele
 */
public class ThreadEngine extends Thread{
    
    private SimulationAlgorithm simulation;
    
    public ThreadEngine() {
    }
    
    public ThreadEngine(SimulationAlgorithm sim) {
        simulation = sim;
    }
    
    @Override
    public void run(){
        System.out.println("il thread è partito");
        simulation.ExecuteSimulationAlgorithm();
    }

    public void endprogram() {
        simulation.endalgorithm();
    }
    
}
