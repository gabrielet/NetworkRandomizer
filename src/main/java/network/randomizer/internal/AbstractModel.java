/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package network.randomizer.internal;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.CyNode;
import org.cytoscape.view.model.CyNetworkView;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import org.cytoscape.model.CyEdge;

/**
 * AbstractModel class represents a frame for all of the models. To be executed
 * by the thread engine.
 *
 * @author ivan
 */
public abstract class AbstractModel {

    // simple Edge class to be use temporarly while generating a network (to avoid too many API calls)
    protected class Edge {

        public int a, b;

        public Edge(int ina, int inb) {
            a = ina;
            b = inb;
        }
    }

    // random to be used throughout the app, so to avoid seed repetition
    protected Random random;

    private final RandomizerCore randomizerCore;
    private CyNetwork currentNetwork;
    private CyNetworkView currentNetworkView;
    private CyApplicationManager cyApplicationManager;
    private CySwingApplication cyDesktopService;
    private CyNetworkFactory cyNetworkFactory;
    private CyNetworkManager cyNetworkManager;
    private HashMap<CyNode, CyNode> nodemap;

    public boolean stop;

    public AbstractModel(RandomizerCore core) {
        randomizerCore = core;
    }

    public void InitializeAndExecute() throws Exception {
        Initialize();
        Execute();
    }

    /**
     * Initialization independent of the model.
     */
    public void Initialize() {
        cyApplicationManager = randomizerCore.cyApplicationManager;
        currentNetwork = randomizerCore.getCurrentnetwork();
        currentNetworkView = randomizerCore.getCurrentnetworkView();
        cyDesktopService = randomizerCore.cyDesktopService;
        cyNetworkFactory = randomizerCore.cyNetworkFactory;
        cyNetworkManager = randomizerCore.cyNetworkManager;
        random = randomizerCore.random;
        initializeSpecifics();
        System.out.println("Model initialized");
    }

    /**
     * Initialization specific of each model, called from Initialize. If unused,
     * leave empty.
     */
    abstract protected void initializeSpecifics();

    /**
     * Main execution point. This method is called by the ThreadEngine after
     * Initialization. TODO: Should this be safe on multiple calls?
     */
    public abstract void Execute() throws Exception;

    /**
     * Generate a network with a given number of nodes and no edges. For network
     * naming convention, see <code>getStandardNetworkName</code> method. Nodes
     * would be named by integers from 0 to <code>(numbderOfNodes - 1)</code>.
     *
     * @param numberOfNodes
     * @return CyNetwork generated
     */
    protected CyNetwork generateEmptyNetwork(int numberOfNodes) {
        CyNetwork net = cyNetworkFactory.createNetwork();
        net.getRow(net).set(CyNetwork.NAME, getStandardNetworkName());
        for (Integer i = 0; i < numberOfNodes; i++) {
            CyNode node = net.addNode();
            net.getRow(node).set(CyNetwork.NAME, i.toString());
        }
        return net;
    }

    protected CyNetwork copyOfCurrentNetwork(boolean directed) {
        return copyOfExistingNetwork(currentNetwork.getNodeList(), currentNetwork.getEdgeList(), directed);
    }

    protected CyNetwork copyOfExistingNetwork(List<CyNode> nodelist, List<CyEdge> edgelist, boolean directed) {
        System.out.println("copying the network");
        nodemap = new HashMap<>();
        CyNetwork net = cyNetworkFactory.createNetwork();
        for (int i = 0; i < nodelist.size(); i++) {
            CyNode node = nodelist.get(i);
            CyNode addedNode = net.addNode();
            nodemap.put(node, addedNode);
            net.getRow(addedNode).set(CyNetwork.NAME, currentNetwork.getDefaultNodeTable().getRow(node.getSUID()).get("name", String.class));
        }
        for (int j = 0; j < edgelist.size(); j++) {
            CyEdge edge = edgelist.get(j);
            CyNode sourcenode = (CyNode) nodemap.get(edge.getSource());
            CyNode targetnode = (CyNode) nodemap.get(edge.getTarget());
            CyEdge newedge = net.addEdge(sourcenode, targetnode, edge.isDirected());
            net.getRow(newedge).set(CyNetwork.NAME, currentNetwork.getDefaultEdgeTable().getRow(edge.getSUID()).get("name", String.class));
        }
        return net;
    }

    /**
     * Send network to Cytoscape.
     *
     * @param net network to be sent to Cytoscape.
     */
    protected void pushNetwork(CyNetwork net) {
        cyNetworkManager.addNetwork(net);
        System.out.println("Network with " + net.getNodeCount() + " nodes and " + net.getEdgeCount() + " edges added.");
    }

    /**
     * Model name to be used for network naming
     *
     * @return
     */
    protected abstract String getModelName();

    /**
     * Network naming convention is "model_time_rand" where: model is the name
     * returned by <code>getModelName</code> method, time is the current time in
     * milliseconds returned by the system and rand is a random three-digit
     * number.
     *
     * @return network name following the naming convention.
     */
    protected String getStandardNetworkName() {
        Long time = java.lang.System.currentTimeMillis();
        Integer rand = random.nextInt(1000);
        String name = getModelName() + "_" + time.toString() + "_" + String.format("%03d", rand);
        return name;
    }

    /**
     * TODO: if performance is important, we can generate a large bit-vector and
     * then read from it.
     *
     * @param probabilityOfTrue
     * @return True with a given probability, False otherwise.
     */
    protected boolean randomBoolean(float probabilityOfTrue) {
        return random.nextFloat() < probabilityOfTrue;
    }

    protected void endalgorithm() {
        stop = true;
    }

    protected CyNetwork getCurrentNetwork() {
        return currentNetwork;
    }

    protected String getEdgeName(CyNode source, CyNode target, CyNetwork net) {
        String sourceName = net.getRow(source).get(CyNetwork.NAME, String.class);
        String targetName = net.getRow(target).get(CyNetwork.NAME, String.class);
        return "Edge_" + sourceName + "_" + targetName;
    }
    
    protected String createInteraction(CyNode source, CyNode target, CyNetwork net) {
        String sourceName = net.getRow(source).get(CyNetwork.NAME, String.class);
        String targetName = net.getRow(target).get(CyNetwork.NAME, String.class);
        return "Node_" + sourceName + " pp " + "Node_" + targetName;
    }
    
}
