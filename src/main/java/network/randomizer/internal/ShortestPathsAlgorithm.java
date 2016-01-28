/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package network.randomizer.internal;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ArrayList;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyRow;

/**
 *
 * @author gabriele
 */
public class ShortestPathsAlgorithm {    

    static int contachiamate = 0;
    static CyNetwork currentnetwork;
    static LinkedList targetlist;

    /**
     * Creates a new instance of MultiShortestPathTreeAlgorithm
     */
    public ShortestPathsAlgorithm() {
    }

    static ArrayList ExecuteMultiShortestPathTreeAlgorithm(CyNetwork network, CyNode root, boolean directed, boolean weighted, String weightColumn){
        currentnetwork = network;
        //Declaration of set and list used in the algorithm 
        //PathSet is the set of the shortest path
        HashSet PathSet;
        //TempSet is the set of temporary Shortest path
        HashSet TempSet;
        //Queue is the list of the temporary shortest path
        LinkedList Queue;
        //The array of the final results
        ArrayList ShortestPathArrayList = new ArrayList();
        //get the list of selected nodes
        // Create the set of the Shortest Path
        PathSet = new HashSet();
        //create the set of temporary Shortest path
        TempSet = new HashSet();
        //create the list of the temporary shortest path
        Queue = new LinkedList();
        //initialize shortest path Queue and Tempset
        //initialize(TempSet,Queue,network,view,root);
        initialize(TempSet, Queue, network, root);
        // Start the Core of the multi shortestpath algorithm
        ShortestPathCore(PathSet, TempSet, Queue, network, root, directed, weighted, weightColumn);
        //now go through our PathSet and select the MultiSPath corresponding to the target node
        
        for (Iterator i = PathSet.iterator(); i.hasNext();) {
            MultiSPath tmpspath = (MultiSPath) i.next();
            ShortestPathList prova = new ShortestPathList();
            ShortestPathArrayList.add(prova);
            createShortestPathArrayList(prova, tmpspath, ShortestPathArrayList);
        }
        return ShortestPathArrayList;
    }
    
    //initialize shortest path
    static void initialize(HashSet TempSet, LinkedList Queue, CyNetwork network, CyNode root) {
        //create the root Shortest Path (cost=o, predecessor = null)
        MultiSPath primospath = new MultiSPath(root, 0, network);
        //initialize shortest path for each node
        //predecessor is null and shortestpath cost is the maximum
        //i.e. number of node plus 1. for root node cost is 0;
        int numberofnodes = network.getNodeCount();
        //add the root Shortest Path to the Queue
        Queue.add(primospath);
        //iterate on all the nodes of the view        
        for (Iterator i = network.getNodeList().listIterator(); i.hasNext();) {
            CyNode currentnode = (CyNode) i.next();
            // initialize all the node except of root
            if (!currentnode.equals(root)) {
                MultiSPath currentSPath = new MultiSPath(currentnode, numberofnodes + 1, network);
                TempSet.add(currentSPath);
            }
        }
    }

    static void ShortestPathCore(HashSet PathSet, HashSet TempSet, LinkedList Queue, CyNetwork network, CyNode root, boolean directed, boolean weighted, String weightColumn) {
        //Iterate the minimum path algorithm on the Queue list
        int adjSize=1;
        CyEdge.Type type=CyEdge.Type.ANY;
        int min, minIndex;        
        for (Iterator i = Queue.iterator(); i.hasNext();) {
            minIndex=0;
            min=((MultiSPath) Queue.get(0)).getCost();
            
            //get the first element of the Queue and add it to the set of the Shortest Path  
            for ( int j=0;j<Queue.size();j++) {
                int c = ((MultiSPath) Queue.get(j)).getCost();
                if(min>c)
                {
                    min=c;
                    minIndex=j;      		        		 
                }        		 
            }            
            MultiSPath currentSPath = (MultiSPath) Queue.remove(minIndex);
            if (!currentSPath.getNode().equals(root)) {
                PathSet.add(currentSPath);
            }
            // get the neighbors of the selected MultiSPath node
            List neighbors;
            if (directed==true)
                neighbors = network.getNeighborList(currentSPath.getNode(), CyEdge.Type.OUTGOING);
            else
                neighbors = network.getNeighborList(currentSPath.getNode(), CyEdge.Type.ANY);
            // and iterate over the neighbors
            for (Iterator ni = neighbors.iterator(); ni.hasNext();) { 
                CyNode neighbor = (CyNode) ni.next();
                //relax the currentSPath with its neighbors
                if(weighted){
                    CyEdge e= network.getConnectingEdgeList(currentSPath.getNode(), neighbor, type).get(0);
                    CyRow r1 = network.getDefaultEdgeTable().getRow(e.getSUID());
                    adjSize=r1.get(weightColumn, Integer.class);
                }
                relax(currentSPath, neighbor, TempSet, Queue, adjSize);
            }
        }
    }    

    static void relax(MultiSPath CurrentSPath, CyNode NeighBor, HashSet TempSet, LinkedList Queue, int adjSize) {
        // verify if NeighBor is in the TempSet end put the corresponding MultiSPath in NeighborSPAth
        MultiSPath NeighborSPath = findSPath(NeighBor, TempSet);
        // if yes then add NeighborSPath to the Queue and set the cost and predecessor
        if (NeighborSPath != null) {
            //   int distance = getDistance(CurrentSPath.getNode(),NeighBor);
            NeighborSPath.setCost(CurrentSPath.getCost() + adjSize);
            NeighborSPath.addPredecessor(CurrentSPath);
            Queue.addLast(NeighborSPath);
            // then remove it from the TempSet
            TempSet.remove(NeighborSPath);
        }
        else {
            // if Neighbor is not in TempSet verify if it is in Queue
            NeighborSPath = findSPath(NeighBor, Queue);
            // if yes put it in NeighborSPath
            if (NeighborSPath != null) {
                // then verify if its cost is greater then the one of the current SPath
                if (NeighborSPath.getCost() == CurrentSPath.getCost()+adjSize) {
                    // if yes we have found a new minimium shortestpath so we have another predecessor
                    // for neighbor. we update cost(useless) and we add the new predecessor
                    NeighborSPath.setCost(CurrentSPath.getCost() + adjSize);
                    NeighborSPath.addPredecessor(CurrentSPath);                             
                }                
                else if (NeighborSPath.getCost() > CurrentSPath.getCost()+adjSize) {
                    // if yes we have found a new minimium shortestpath so we have another predecessor
                    // for neighbor. we update cost(useless) and we add the new predecessor                      
                    NeighborSPath.removeAllPredecessors();
                    NeighborSPath.setCost(CurrentSPath.getCost() + adjSize);
                    NeighborSPath.addPredecessor(CurrentSPath);
                }
            }
        }
    }
    // Verify if Node is in TempSet and return the corresponding
    // MultiSPath element
    static MultiSPath findSPath(CyNode Node, Collection TempSet) {        
        MultiSPath foundSPath = null;       
        for (Iterator i = TempSet.iterator(); i.hasNext();) {
            MultiSPath tempSPath = (MultiSPath) i.next();
            if (Node.equals(tempSPath.getNode())) {
                foundSPath = tempSPath;
                break;
            }
        }
        return foundSPath;
    }

    // create the array ShortestPathArrayList of the shortest path exploring the predecessor tree from the
    // tmpspath element. tmpspath at the first call is the target element.
    // a array element of type LinkedList is created for each shortest path found.
    // spathlist represent the shortestpath explored at the calling time. Initially is empty
    static void createShortestPathArrayList(ShortestPathList spathlist, MultiSPath tmpspath, ArrayList ShortestPathArrayList) {
        //set arraysize equals to the size of the array predecessor
        int arraysize = tmpspath.PredecessorArrayListSize();
        // add the element tmpspath to the spathlist representing the current shortestpath
        spathlist.addFirst(tmpspath);
        // if tmpspath has one or more predecessors we have not finished
        if (arraysize != 0) {
            // we create a copy of spathlist
            ShortestPathList tmplist = (ShortestPathList) spathlist.clone();
            for (int i = 0; i < arraysize; i++) {
                // the first predecessor build the Shortestpath in the current element of the array
                if (i == 0) {
                    MultiSPath newMultiSPath = tmpspath.getPredecessor(i);
                    contachiamate++;
                    if (contachiamate % 100 == 0) {
                    }
                    createShortestPathArrayList(spathlist, newMultiSPath, ShortestPathArrayList);
                } else {
                    //if there are more predecessors we have to build a new array element
                    //for each predecessor exceeding the first. This because each predecessor contributes
                    //for a different shortest path
                    MultiSPath newMultiSPath = tmpspath.getPredecessor(i);
                    ShortestPathList newlist = (ShortestPathList) tmplist.clone();
                    ShortestPathArrayList.add(newlist);
                    contachiamate++;
                    /*if (contachiamate % 100 == 0) {
                        System.out.println("contachiamate=" + contachiamate);
                    }*/
                    createShortestPathArrayList(newlist, newMultiSPath, ShortestPathArrayList);
                }
            }
        }
    }
}