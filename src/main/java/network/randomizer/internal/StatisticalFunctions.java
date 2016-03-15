/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package network.randomizer.internal;

import java.util.ArrayList;
import java.util.List;
import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyNetwork;

/**
 *
 * @author gabriele
 */
public class StatisticalFunctions {
        
    public List<CyNetwork> allthenets;
    
    /*i was thinking about merging the two methods without instatiating the constructor
    and using a static method for the extraction of the columns you need. But then i 
    realised that maybe better to instatiate an object which allow to get the info about
    the network(s) and use for something you may need. Don't know which option is better!
    */
        
    StatisticalFunctions(RandomizerCore core){
        System.out.println("statistics");
        allthenets = core.cyApplicationManager.getSelectedNetworks();
    }
    
    public ArrayList<ArrayList<Double>> getCentrality(List<String> whichcentrality, CyNetwork whichnet){
        ArrayList<ArrayList<Double>> centralities = new ArrayList();
        ArrayList<Double> tmp;
        CyColumn col;
        int l = whichcentrality.size();
        for(int i=0; i<l; i++){
            System.out.println("i "+i);
            //check if col exists!!!!!!!!!!!!!!!
            tmp = new ArrayList();
            col = whichnet.getDefaultNodeTable().getColumn(whichcentrality.get(i));//get a centrality column
            List<Object> values = col.getValues(col.getType());//get the values for that column
            int e = values.size();
            if(!values.isEmpty()){
                for(int j=0; j<e; j++){
                    System.out.println("j "+j);
                    tmp.add(Double.parseDouble(values.get(j).toString()));
                }
                centralities.add(tmp);
            }
            else{return null;}
        }
        return centralities;
    }
    
    public List<String> compareWhat(List<CyNetwork> listofnets){
        List<String> thesecentralities = new ArrayList();
        
        
        return thesecentralities;
    }
}
