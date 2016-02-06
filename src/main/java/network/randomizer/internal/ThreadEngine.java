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
    
    private AbstractModel randomizer;
    
    public ThreadEngine() {
    }
    
    public ThreadEngine(AbstractModel randomizer) {
        this.randomizer = randomizer;
    }
    
    @Override
    public void start(){
        System.out.println("thread started");
        randomizer.InitializeAndExecute();
    }

    public void endprogram() {
        randomizer.endalgorithm();
    }
    
}
