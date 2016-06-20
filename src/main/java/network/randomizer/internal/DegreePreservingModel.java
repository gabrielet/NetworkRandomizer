/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package network.randomizer.internal;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;

/**
 * Degree preserving model randomizes the edges of a given network, keeping its node degrees preserved.
 * @author Ivan
 */
public class DegreePreservingModel extends AbstractModel{

    // 
    // 10 seems sufficient, see:
    // Pinar, Ray, Seshadri (2012) "Are we there yet? When to stop a Markov chain while generating random graphs"
    
    /**
     * Number of initial edge swaps is calculated by |E| * ITERFACTOR
     * For closer examination of sufficient values for the factor, see arXiv:1202.3473
     */
    private final float ITERFACTOR = 10;
    // Number of tries to make network connected, after initial swaps are made.
    private final float MAXTRIES = 20;
    // Number of swaps to make at each try, to make the network connected.
    private final int SWAPSEACHTRY = 100;
    
    int iters;
    CyNetwork net;
    
    public DegreePreservingModel(RandomizerCore core, int maxIters) {
        super(core);
        iters = maxIters;
    }
    public DegreePreservingModel(RandomizerCore core) {
        super(core);
        iters = 0;
    }

    @Override
    protected void initializeSpecifics() {
    }

    @Override
    public void Execute() throws Exception {  
        // copy of the nertwork to rewire
        net = copyOfCurrentNetwork(false);
        net.getRow(net).set(CyNetwork.NAME, getStandardNetworkName());
        
        List<CyEdge> newedgeslist = net.getEdgeList();
        //adding edges names to edgetable, with respect to the new copied network
        for(int i=0; i<newedgeslist.size(); i++){
            //String source = net.getDefaultNodeTable().getRow(newedgeslist.get(i).getSource().getSUID()).get("name", String.class);
            //String target = net.getDefaultNodeTable().getRow(newedgeslist.get(i).getTarget().getSUID()).get("name", String.class);            
            //net.getRow(newedgeslist.get(i)).set("interaction", createInteraction(source, target, net));
            net.getRow(newedgeslist.get(i)).set("interaction", "pp");
        }
        
        int numOfEdges = net.getEdgeCount();
        
        // if number of iterations is not set by user
        if(iters == 0){
            iters = (int)(numOfEdges * ITERFACTOR);
        }
        
        // network edges
        List<CyEdge> CyEdges = net.getEdgeList();
        // array of SUIDs of network edges, to be used for geting a random edge
        ArrayList<Long> SUIDs = new ArrayList<>(numOfEdges);
        for (CyEdge edge : CyEdges) {
            SUIDs.add(edge.getSUID());
        }

        // did algorithm finish with a connected network
        boolean succes = false;
        int tryNum = 0;
        // initial number of swaps
        int numOfSwaps = iters;
        while(tryNum++ < MAXTRIES){
            
            for (int i = 0; i < numOfSwaps; i++) {
                // get random SUID indices
                int firstId = random.nextInt(numOfEdges);
                int secondId = random.nextInt(numOfEdges);
                if(firstId == secondId) continue;
                
                // get SUIDs from random indices
                long firstSUID = SUIDs.get(firstId);
                long secondSUID = SUIDs.get(secondId);
                
                // get edges from random SUIDs
                CyEdge first = net.getEdge(firstSUID);
                CyEdge second = net.getEdge(secondSUID);
                
                // get nodes from random edges
                CyNode fa = first.getSource();
                CyNode fb = first.getTarget();
                CyNode sa = second.getSource();
                CyNode sb = second.getTarget();
                
                // if edges share a node, or if they are adjecent to one another, go to the next iteration
                if(fa.equals(fb) || fa.equals(sa) || fa.equals(sb) || fb.equals(sa) || fb.equals(sb) || sa.equals(sb)) continue;
                if(net.containsEdge(fa, sb) || net.containsEdge(sa, fb)) continue;
                
                // remove old edges
                List<CyEdge> toRemove = new LinkedList<>();
                toRemove.add(first);
                toRemove.add(second);
                net.removeEdges(toRemove);
                
                // add new edges (swapped)
                CyEdge newFirst = net.addEdge(fa, sb, false);
                CyEdge newSecond = net.addEdge(sa, fb, false);
                
                // update SUIDs of old edges to new ones
                SUIDs.set(firstId, newFirst.getSUID());
                SUIDs.set(secondId, newSecond.getSUID());
            }

            // if network is connected, algorithm is done
            if(isConnected()) {
                succes = true;
                break;
            }
            
            // set number of swaps to number of additional swaps to try and connect the network
            numOfSwaps = SWAPSEACHTRY;
        }
        
        if(!succes){
            System.out.println("Degree preserving shuffle unsuccessful (resulting network is not connected)!");
        }
        
        // send the network to the cytoscape
        pushNetwork(net);
    }
    
    
    /**
     * Checks if the network used is connected using BFS.
     * @return true if network is connected, false otherwise
     */
    private boolean isConnected(){
        // number of unchecked nodes
        int nodeCount = net.getNodeCount();
        if(nodeCount == 0) return true;
        
        // map saves if the node is checked or not
        HashMap<CyNode, Boolean> nodes = new HashMap<>(nodeCount);
        for (CyNode node : net.getNodeList()) {
            nodes.put(node, false);
        }
        // stack to be used for the BFS
        LinkedList<CyNode> stack = new LinkedList<>();
        
        // initial BFS node
        CyNode first = net.getNodeList().get(0);
        //nodes.replace(first, true);
        if(nodes.containsKey(first)) nodes.put(first,true);
        stack.add(first);
        nodeCount--;
        
        // BFS
        while(!stack.isEmpty()){
            CyNode current = stack.pop();
            List<CyNode> neighbours = net.getNeighborList(current, CyEdge.Type.ANY);
            for (CyNode neighbour : neighbours) {
                if(!nodes.get(neighbour)){
                    stack.add(neighbour);
                    //nodes.replace(neighbour, true);
                    if(nodes.containsKey(neighbour)) nodes.put(neighbour,true);
                    nodeCount--;
                }
            }
        }
        // if all nodes are checked, network is connected
        return nodeCount == 0;
    }

    @Override
    protected String getModelName() {
        return "DegreePreserving";
    }
    
}
