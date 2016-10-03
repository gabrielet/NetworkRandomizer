/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package network.randomizer.internal;

import java.util.ArrayList;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;

/**
 *
 * @author gabriele
 */
public class RandomizeCurrent extends AbstractModel{
    
    private int n;
    private int M;
    private CyNetwork currentNet;

    public RandomizeCurrent(RandomizerCore core){
        super(core);
        n = core.getCurrentnetwork().getNodeCount();
        M = core.getCurrentnetwork().getEdgeCount();
        currentNet = core.getCurrentnetwork();        
    }

    @Override
    protected void initializeSpecifics() {
        // nothing to do here;
    }

    @Override
    public void Execute() throws Exception {
        ArrayList<String> names = new ArrayList<>();
        System.out.println("Copying the nodes names");
        for(CyNode node : currentNet.getNodeList()){
            names.add(currentNet.getDefaultNodeTable().getRow(node.getSUID()).get("name", String.class));
        }
        CyNetwork net = generateNetworkFromNodeList(names);
        ArrayList<CyNode> nodes = new ArrayList<>(net.getNodeList());
        int maxEdges = n*(n-1)/2;
        if(M > maxEdges) return;

        // reservoir sampling of edges
        ArrayList<Integer> edges = new ArrayList<>(M);
        for (int i = 0; i < M; i++) {
            edges.add(i);
        }
        for (int i = M; i < maxEdges; i++) {
            int j = random.nextInt(i+1);
            if(j < M){
                edges.set(j, i);
            }
        }
        
        for (Integer edge : edges) {
            Integer i = (int)(0.5 + 0.5 * Math.sqrt(1 + 8*edge));
            Integer j = edge - i*(i-1)/2;
            CyEdge addedEdge = net.addEdge(nodes.get(i), nodes.get(j), false);
            net.getRow(addedEdge).set(CyNetwork.NAME, net.getDefaultNodeTable().getRow(addedEdge.getTarget().getSUID()).get("name", String.class) + " pp " + net.getDefaultNodeTable().getRow(addedEdge.getSource().getSUID()).get("name", String.class));
            net.getRow(addedEdge).set("interaction", "pp");
        }        
        pushNetwork(net);
    }

    @Override
    protected String getModelName() {
        return "Randomize current network";
    }
    
}
