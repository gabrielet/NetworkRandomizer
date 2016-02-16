/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package network.randomizer.internal;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.SortedSet;
import java.util.TreeSet;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;

/**
 *
 * @author Ivan
 */
public class BarabasiAlbertModel extends AbstractModel{
    
    private int N;
    private int m;
    private int m0;

    public BarabasiAlbertModel(RandomizerCore core, int N, int m) {
        super(core);
        this.N = N;
        this.m = m;
        this.m0 = 2*m;
        if(m0 < 3) m0 = 6;
    }
    
    public BarabasiAlbertModel(RandomizerCore core) {
        super(core);
        this.N = core.currentnetwork.getNodeCount();
        this.m = core.currentnetwork.getEdgeCount()/N;
        this.m0 = 2*m;
        if(m0 < 3) m0 = 6;
    }

    @Override
    protected void initializeSpecifics() {
    }

    @Override
    public void Execute() {
        if(N < 0 || m >= N || m < 0){
            return;
        }
        CyNetwork net = generateEmptyNetwork(N);
        // array of all the nodes in the network
        ArrayList<CyNode> nodes = new ArrayList<>(net.getNodeList());
        
        // array of each edge-node incidence, saving nodes only
        // this makes preferential attachment O(1) per edge
        ArrayList<Integer> incidences = new ArrayList<>(2*N*m);
        
        // connect initial m0 nodes
        for (Integer i = 0; i < m0; i++) {
            Integer j = (i+1)%m0;
            incidences.add(i);
            incidences.add(j);
            CyEdge edge = net.addEdge(nodes.get(i), nodes.get(j), false);
            String name = i.toString() + "_" + j.toString();
            net.getRow(edge).set(CyNetwork.NAME, name);
        }
        
        // preferential attachment
        for (Integer i = m0; i < N; i++) {
            HashSet<Integer> currentNodeNeighbours = new HashSet<>(m);
            while(currentNodeNeighbours.size() != m){
                int incPos = random.nextInt(incidences.size());
                int j = incidences.get(incPos);
                currentNodeNeighbours.add(j);
            }
            for (Integer j : currentNodeNeighbours) {
                incidences.add(i);
                incidences.add(j);
                CyEdge edge = net.addEdge(nodes.get(i), nodes.get(j), false);
                String name = i.toString() + "_" + j.toString();
                net.getRow(edge).set(CyNetwork.NAME, name);
            }
        }
        
        
        
        // send network to cytoscape
        pushNetwork(net);
    }

    @Override
    protected String getModelName() {
        return "BarabasiAlbert";
    }
    
}
