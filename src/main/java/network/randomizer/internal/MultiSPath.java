/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package network.randomizer.internal;

import java.util.ArrayList;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;

/**
 *
 * @author gabriele
 */
public class MultiSPath {
    
    private final CyNode node;
    private String nodename;
    private int cost;
    private final MultiSPath predecessor;
    private final ArrayList predecessorArrayList;
    private int shortestpathcount;
    CyNetwork currentnetwork;
    private final long nodesuid;
           
    /**
     * Creates a new instance of MultiSPath
     */
    public MultiSPath(CyNode Node, MultiSPath Predecessor) {
        node = Node;
        cost = 1;
        predecessor = Predecessor;
        predecessorArrayList = new ArrayList();
        predecessorArrayList.add(Predecessor);
        shortestpathcount = 0;
        nodesuid = Node.getSUID();
    }
    
    public MultiSPath(CyNode Node, int n, CyNetwork currentnetwork) {
        node = Node;
        this.currentnetwork = currentnetwork;
        nodename =  currentnetwork.getRow(node).get("name",String.class);
        nodesuid = Node.getSUID();      
        cost = n;
        predecessor = null;
        predecessorArrayList = new ArrayList();
        shortestpathcount = 0;        
    }

    // set the cost to Cost
    public void setCost(int Cost) {
        cost = Cost;
    }
    // set the predecessor to newPredecessor
    public void addPredecessor(MultiSPath newPredecessor){
        predecessorArrayList.add(newPredecessor);
    }
    
    public MultiSPath getPredecessor(int i) {
        return (MultiSPath)predecessorArrayList.get(i);
    }
    
    public int PredecessorArrayListSize() {
        return predecessorArrayList.size();
    }
    
    // return the node name of the MultiSPath instance    
    public CyNode getNode() {
        return this.node;  
    }
    public int getCost()  {
        return this.cost;
    }
    
    public String getName() {
        return nodename;
    }
    
    public Long getSUID() {
        return nodesuid;
    }
    public void setShortestPathCount(int i) {
        shortestpathcount = i;
    }
    public void incrementShortestPathCount() {
        shortestpathcount++;
    }
    public double getShortestPathCount() {
        return shortestpathcount;
    }
    public String predecessortoString() {
        String predecessorString = " no predecessor ";
        for (int i = 0; i < predecessorArrayList.size(); i++) {
            if (i == 0) { 
                predecessorString = " " + getPredecessor(i).node.getNetworkPointer().getRow(node).get("name",String.class);
            }
            else {
                predecessorString = predecessorString + " " + getPredecessor(i).node.getNetworkPointer().getRow(node).get("name",String.class);
            }
        }
        return predecessorString;
    }
    
    @Override
    public String toString() {
        String PathString;
        PathString = "origine = " + node.getSUID()+ " costo = " + cost;
        if (cost == 0) {PathString = PathString + " Root and Target are the same ";}
        else
            if (!predecessorArrayList.isEmpty()) {
                {PathString = PathString + "predecessori= " + predecessortoString();} }
        return PathString;
    }
    
    public void removeAllPredecessors() {
        predecessorArrayList.clear();
    }    
}