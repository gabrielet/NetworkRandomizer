/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package network.randomizer.internal;

import java.util.ArrayList;
import javax.swing.JOptionPane;
import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.view.model.CyNetworkView;

/**
 *
 * @author gabriele
 */
public class SimulationAlgorithm {
    
    public CyNetwork network;
    public CyNetworkView networkview;
    public CyApplicationManager cyApplicationManager;
    public RandomizerCore randomizerCore;
    public CySwingApplication cyDesktopService;
    public boolean stop;
    
    public SimulationAlgorithm(RandomizerCore core){
        randomizerCore = core;
    }
    
    public void ExecuteSimulationAlgorithm(){
        cyApplicationManager = randomizerCore.cyApplicationManager;
        network = randomizerCore.cyApplicationManager.getCurrentNetwork();       
        networkview = randomizerCore.cyApplicationManager.getCurrentNetworkView();
        cyDesktopService = randomizerCore.cyDesktopService;
        int min = 1000000, max = 0;
        
        //devo recuperare le informazioni relative alla tabella attributi!        
        int ncols = network.getDefaultNodeTable().getColumns().size();
        System.out.println("the table has " + ncols  + " columns!");
        Object[] nomi = network.getDefaultNodeTable().getColumns().toArray();
        
        //calcolo del minimo e del massimo        
        for(int i=0; i<ncols; i++){
            if(!nomi[i].toString().matches("SUID") && !nomi[i].toString().matches("shared name") && !nomi[i].toString().matches("selected") && !nomi[i].toString().matches("name")){
                //System.out.println(network.getDefaultNodeTable().getColumn("name").toString() + " " + nomi[i].toString());
                CyColumn col = network.getDefaultNodeTable().getColumn(nomi[i].toString());
                for(int j=0; j<col.getValues(col.getType()).size(); j++){
                    if((Integer)col.getValues(col.getType()).get(j) < min){
                        min = (Integer)col.getValues(col.getType()).get(j);
                    }
                    if((Integer)col.getValues(col.getType()).get(j) > max){
                        max = (Integer)col.getValues(col.getType()).get(j);
                    }
                }
            }
        }
        //System.out.println("min "+min);
        Object[] options = {"Abort","Continue"};
        Integer answer;
        answer = JOptionPane.showOptionDialog(this.cyDesktopService.getJFrame(),
                "with a max = "+max+ " and nodes = " + network.getNodeCount() + " then by multiplying we will have up to " + max *network.getNodeCount() +" nodes", "CentiScaPe",
                JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE,
                null, options, options[0]);
        if(answer == 1){
            System.out.print("Doing nothing special with " + network.toString());
        }
    }
    
    public ArrayList rg_Sim(){
        ArrayList results = null;
        return results;
    }
    
    public ArrayList centralitySum(){
        ArrayList results = null;
        return results;
    }
    
    public ArrayList centralityPositions(){
        ArrayList results = null;
        return results;
    }

    void endalgorithm() {
        stop = true;
    }
    
}