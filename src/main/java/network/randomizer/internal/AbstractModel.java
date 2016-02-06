/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package network.randomizer.internal;

import java.util.ArrayList;
import javax.swing.JOptionPane;
import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.CyNode;
import org.cytoscape.view.model.CyNetworkView;
import java.sql.Timestamp;
import java.util.Date;
import java.util.Random;


/**
 * AbstractModel class represents a frame for all of the models.
 * To be executed by the thread engine.
 * @author ivan
 */
public abstract class AbstractModel {
    
    // random to be used throughout the app, so to avoid seed repetition
    protected Random random;
       
    private final RandomizerCore randomizerCore;
    private CyNetwork currentNetwork;
    private CyNetworkView currentNetworkView;
    private CyApplicationManager cyApplicationManager;
    private CySwingApplication cyDesktopService;
    private CyNetworkFactory cyNetworkFactory;
    private CyNetworkManager cyNetworkManager;
    
    
    public boolean stop;
    
    public AbstractModel(RandomizerCore core){
        randomizerCore = core;
    }
    
    public void InitializeAndExecute(){
        Initialize();
        Execute();
    }
    
    /**
     * Initialization independent of the model.
     */
    public void Initialize(){
        cyApplicationManager = randomizerCore.cyApplicationManager;
        currentNetwork = randomizerCore.cyApplicationManager.getCurrentNetwork();       
        currentNetworkView = randomizerCore.cyApplicationManager.getCurrentNetworkView();
        cyDesktopService = randomizerCore.cyDesktopService;
        cyNetworkFactory = randomizerCore.cyNetworkFactory;
        cyNetworkManager = randomizerCore.cyNetworkManager;
        random = randomizerCore.random;
        initializeSpecifics();
        System.out.println("Model initialized");
    }
    
    /**
     * Initialization specific of each model, called from Initialize. 
     * If unused, leave empty.
     */
    abstract protected void initializeSpecifics();
    
    /**
     * Main execution point.
     * This method is called by the ThreadEngine after Initialization.
     * TODO: Should this be safe on multiple calls?
     */
    public abstract void Execute();

    /**
     * Generate a network with a given number of nodes and no edges.
     * For network naming convention, see <code>getStandardNetworkName</code> method.
     * Nodes would be named by integers from 0 to <code>(numbderOfNodes - 1)</code>.
     * @param numberOfNodes
     * @return CyNetwork generated
     */
    protected CyNetwork generateEmptyNetwork(int numberOfNodes){
        CyNetwork net = cyNetworkFactory.createNetwork();
        net.getRow(net).set(CyNetwork.NAME, getStandardNetworkName());
        for (Integer i = 0; i < numberOfNodes; i++) {
            CyNode node = net.addNode();
            net.getRow(node).set(CyNetwork.NAME, i.toString());
        }
        return net;
    }
    
    /**
     * Send network to Cytoscape.
     * @param net network to be sent to Cytoscape.
     */
    protected void pushNetwork(CyNetwork net){
        cyNetworkManager.addNetwork(net);
        System.out.println("Network with " + net.getNodeCount() + " nodes and " + net.getEdgeCount() + " edges added.");
    }

    /**
     * Model name to be used for network naming
     * @return 
     */
    protected abstract String getModelName();
    
    /**
     * Network naming convention is "model_time_rand" where:
     * model is the name returned by <code>getModelName</code> method,
     * time is the current time in milliseconds returned by the system and
     * rand is a random three-digit number.
     * @return network name following the naming convention.
     */
    protected String getStandardNetworkName(){
        Long time = java.lang.System.currentTimeMillis();
        Integer rand = random.nextInt(1000);
        String name = getModelName() + "_" + time.toString() + "_" + String.format("%03d", rand);
        return name;
    }
    
    /**
     * TODO: if performance is important, we can generate a large bit-vector and then read from it.
     * @param probabilityOfTrue
     * @return True with a given probability, False otherwise.
     */
    protected boolean randomBoolean(float probabilityOfTrue) {
        return random.nextFloat() < probabilityOfTrue;
    }
    
    protected void endalgorithm() {
        stop = true;
    }
        
}