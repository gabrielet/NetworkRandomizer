/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package network.randomizer.internal;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.view.model.CyNetworkView;

/**
 *
 * @author gabriele
 */
public class ShortPathsMultiplication {
    
    public CyNetwork network;
    public CyNetworkView networkview;
    public CyApplicationManager cyApplicationManager;
    public RandomizerCore randomizerCore;
    public boolean stop;
    
    public ShortPathsMultiplication(RandomizerCore core){
        randomizerCore = core;
    }
    
    public void ExecuteMultiplication(){
        
    }
}