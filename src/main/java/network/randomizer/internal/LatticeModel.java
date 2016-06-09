/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package network.randomizer.internal;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;

/**
 * Lattice graph model generates a regular lattice graph of a given dimensions.
 * Optionally, it generates a lattice which has the opposite nodes connected, creating a hyper torus.
 * @author Ivan
 */
public class LatticeModel extends AbstractModel{
    
    private int numOfDim;
    private List<Integer> dimensions;
    private boolean isTorus;

    public LatticeModel(RandomizerCore core, List<Integer> dimensions, boolean isTorus) {
        super(core);
        this.numOfDim = dimensions.size();
        this.dimensions = dimensions;
        this.isTorus = isTorus;
    }

    @Override
    protected void initializeSpecifics() {
    }

    CyNetwork net;
    ArrayList<CyNode> nodes;
    
    @Override
    public void Execute() {
        if(dimensions.size() < 1) return;
        int numOfnodes = 1;
        for (Integer dimension : dimensions) {
            if(dimension < 1) return;
            numOfnodes *= dimension;
        }
        // network
        net = generateEmptyNetwork(numOfnodes);
        // array of all the nodes in the network
        nodes = new ArrayList<>(net.getNodeList());

        //start the recursive hypercube construction
        connectHyperPlane(0, numOfnodes, 0);

        // send network to cytoscape
        pushNetwork(net);
    }
    
    // partitioning and connecting the hyperplane into the hypercube 
    // starting with a line (offset 0, len N, dimId 0)
    // from-inclusive, to-exclusive
    private void connectHyperPlane(int offset, int len, int dimId){
        if(dimId >= dimensions.size()) return;
        // size of current dimension on which we are partitioning the hyperplane
        int size = dimensions.get(dimId);
        // width of the hyperplane
        int width = len/size;
        int loopEnd = isTorus ? len : len-width;
        for (int i = 0; i < loopEnd; i++) {
            addEdge(offset+i, offset+((i+width)%len));
        }
        for (int i = 0; i < size; i++) {
            connectHyperPlane(offset+(i*width), width, dimId+1);
        }
        
    }

    private void addEdge(Integer i, Integer j){
        CyEdge edge = net.addEdge(nodes.get(i), nodes.get(j), false);
        net.getRow(edge).set(CyNetwork.NAME, getEdgeName(nodes.get(i), nodes.get(j), net));
    }

    @Override
    protected String getModelName() {
        if(isTorus) return numOfDim + "DTorusLattice";
        return numOfDim + "DCubeLattice";
    }
    
}
