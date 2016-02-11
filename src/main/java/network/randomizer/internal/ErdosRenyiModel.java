/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package network.randomizer.internal;

import java.util.ArrayList;
import java.util.Objects;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;

/**
 *
 * @author Ivan
 */
public class ErdosRenyiModel extends AbstractModel{

    public enum ERType{ nM, np }
    
    private int n;
    private int M;
    private float p;
    ERType type;

    /**
     * @param n number of nodes
     * @param M number of edges in G(n,M) model type
     * @param p probability of an edge between each pair of nodes in G(n,p) model type
     * @param type type of model used: G(n,P) or G(n,M).
     * @param core randomizerCore
     */
    public ErdosRenyiModel(int n, int M, float p, ERType type, RandomizerCore core) {
        super(core);
        this.n = n;
        this.M = M;
        this.p = p;
        this.type = type;
    }
    
    /**
     * Construct a randomized version of a given network preserving the number of nodes and edges using the Erdos-Renyi G(n,M) model.
     * @param core randomizerCore from which a current network will be used to determine 
     * parameters for G(n,M) model (number of nodes and edges respectively).
     */
    public ErdosRenyiModel(RandomizerCore core) {
        super(core);
        type = ERType.nM;
        n = core.currentnetwork.getNodeCount();
        M = core.currentnetwork.getEdgeCount();
    }

    @Override
    protected void initializeSpecifics() {
        // nothing to do here
    }

    @Override
    public void Execute() {
        CyNetwork net = generateEmptyNetwork(n);
        ArrayList<CyNode> nodeList = new ArrayList<>(net.getNodeList());
        
        // if type to be used id G(n,M)
        if(type == ERType.nM){
            for (int i = 0; i < M; i++) {
                Integer node1 = random.nextInt(n);
                Integer node2 = random.nextInt(n);
                if(Objects.equals(node1, node2) || net.containsEdge(nodeList.get(node1), nodeList.get(node2))){
                    i--;
                    continue;
                }
                CyEdge edge = net.addEdge(nodeList.get(node1), nodeList.get(node2), false);
                // Not sure about this naming!
                String name = node1.toString() + "_" + node2.toString();
                net.getRow(edge).set(CyNetwork.NAME, name);
            }
        } 
        
        // if type to be used id G(n,p)
        else{
            for (Integer i = 0; i < n-1; i++) {
                for (Integer j = i+1; j < n; j++) {
                    if(randomBoolean(p)){
                        CyEdge edge = net.addEdge(nodeList.get(i), nodeList.get(j), false);
                        // Not sure about this naming!
                        String name = i.toString() + "_" + j.toString();
                        net.getRow(edge).set(CyNetwork.NAME, name);
                    }
                }
            }
        }
        pushNetwork(net);
    }
    
    
    @Override
    protected String getModelName() {
        return "ErdosRenyi";
    }
}
