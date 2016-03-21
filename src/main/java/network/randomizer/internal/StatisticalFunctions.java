/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package network.randomizer.internal;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyNetwork;

/**
 *
 * @author gabriele
 */
public class StatisticalFunctions {
        
    public List<CyNetwork> allthenets;
        
    StatisticalFunctions(RandomizerCore core){
        System.out.println("statistics");
        allthenets = core.cyApplicationManager.getSelectedNetworks();
    }
    
    /*public ArrayList<ArrayList<Double>> getCentrality(List<String> whichcentrality, CyNetwork whichnet){
        ArrayList<ArrayList<Double>> centralities = new ArrayList<>();
        ArrayList<Double> tmp;
        CyColumn col;
        int l = whichcentrality.size();
        for(int i=0; i<l; i++){
            System.out.println("i "+i);
            //check if col exists!!!!!!!!!!!!!!!
            tmp = new ArrayList<>();
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
    }*/
    
    public List<String> getColumnNames(List<CyNetwork> real, List<CyNetwork> rnd){
        //this methods gets all the columns names of all the selected networks in order to find all the common attributes
        //attributes are integer, long or double and are not called name, suid, selected or sharedname
        List<String> centralities = new ArrayList();
        Collection<CyColumn> tmp;
        for (CyNetwork net : real) {
            tmp = net.getDefaultNodeTable().getColumns();
            for(CyColumn col : tmp){
                if(!col.getName().matches("name") && !col.getName().matches("SUID") && !col.getName().matches("selected") && !col.getName().matches("shared name") && (col.getType() == (Integer.class) || col.getType() == (Long.class) || col.getType() == (Double.class))){
                    centralities.add(col.getName());
                }
            }
        }
        for (CyNetwork net : rnd) {
            tmp = net.getDefaultNodeTable().getColumns();
            for(CyColumn col : tmp){
                if(!col.getName().matches("name") && !col.getName().matches("SUID") && !col.getName().matches("selected") && !col.getName().matches("shared name") && (col.getType() == (Integer.class) || col.getType() == (Long.class) || col.getType() == (Double.class))){
                    centralities.add(col.getName());
                }
            }
        }
        return centralities;
    }
    
    public List<String> compareWhat(List<String> listofcentrs, int howmanynets){
        //this methods finds the centralities that occurs homanynets times that means all the networks share that attribute hence it can be compare along all the networks.
        List<String> thesecentralities = new ArrayList();
        for(String str : listofcentrs){
            if(howmanynets==(Collections.frequency(listofcentrs, str))){
                thesecentralities.add(str);
            }
        }
        return thesecentralities;
    } 
    
    /**
     * 
     * Calculates a distance matrix between two given sets of networks.
     * Currently, this method SORTS ALL THE LISTS GIVEN TO IT, without copying them!!!
     * @param verticalGroup - each element of this group matches one ROW of the returned matrix (elements are placed on the VERTICAL edge of the matrix)
     * @param horizontalGroup  - each element of this group matches one COLUMN of the returned matrix (elements are placed on the HORIZONTAL edge of the matrix)
     * @return - element (i,j) of the returned matrix is the distance between verticalGroup(i) and horizontalGroup(j)
     */
    private double[][] getDistanceMatrix(ArrayList<LinkedList<Double>> verticalGroup, ArrayList<LinkedList<Double>> horizontalGroup){
        int nv = verticalGroup.size();
        int nh = horizontalGroup.size();
        double[][] matrix = new double[nv][nh];
        
        // sort all lists
        for (int i = 0; i < nv; i++) {
            
            //is this method working the same??????
            
            Collections.sort(verticalGroup.get(i));
            //verticalGroup.get(i).sort(null);
        }
        for (int i = 0; i < nh; i++) {
            Collections.sort(horizontalGroup.get(i));
            //horizontalGroup.get(i).sort(null);
        }
        
        for (int i = 0; i < nv; i++) {
            matrix[i][i] = 0;
            for (int j = i+1; j < nh; j++) {
                double dist = KS_Test(verticalGroup.get(i), horizontalGroup.get(j));
                //distance is simetric
                matrix[i][j] = dist;
                matrix[j][i] = dist;
            }
        }
        return matrix;
    }
    
    // Two-sample Kolmogorov�Smirnov test
    //ordered input presumed!
    private double KS_Test(LinkedList<Double> first, LinkedList<Double> second){
        //maximum distance between distributions
        double dist = 0;
        
        //first distribution size and current count
        double n1 = first.size();
        double count1 = 0;
        
        //second distribution size and current count
        double n2 = second.size();
        double count2 = 0;

        //continue while both lists are nonempty
        double tempdist;
        while(!first.isEmpty() && !second.isEmpty()){
            //if front of the first list is next smallest value
            if(first.peek() < second.peek()){
                first.pop();
                count1++;
            }
            //if front of the second list is next smallest value
            else if(first.peek() > second.peek()){
                second.pop();
                count2++;
            }
            //if both fron values are next smallest values
            else{
                first.pop();
                second.pop();
                count1++;
                count2++;
            }
            //current distance
            tempdist = Math.abs((count1/n1) - (count2/n2));
            //update max distance
            if(tempdist > dist) dist = tempdist;

        }        
        return dist;
    }    
}
