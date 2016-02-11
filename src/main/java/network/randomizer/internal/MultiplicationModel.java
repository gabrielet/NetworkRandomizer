/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package network.randomizer.internal;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import javax.swing.JOptionPane;
import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.view.model.CyNetworkView;

/**
 *
 * @author gabriele
 */
public class MultiplicationModel extends AbstractModel{
    
    private CyNetwork network;
    private CyNetworkView networkview;
    private CyApplicationManager cyApplicationManager;
    private CySwingApplication cyDesktopService;
    private int min = 1000000, max = 0, nodes;
    private boolean directed;
    
    public MultiplicationModel(RandomizerCore core, boolean drct){
        super(core);
        cyApplicationManager = core.cyApplicationManager;
        network = core.cyApplicationManager.getCurrentNetwork();       
        networkview = core.cyApplicationManager.getCurrentNetworkView();
        cyDesktopService = core.cyDesktopService;
        nodes = network.getNodeCount();
        directed = drct;
    }

    @Override
    protected void initializeSpecifics() {
        System.out.println("initializeSpecificsMultiModel");
    }

    @Override
    protected String getModelName() {
        return("Multiplication Model"); //To change body of generated methods, choose Tools | Templates.
    }
    
    @Override
    public void Execute(){
        //recovering info about attributes table
        ArrayList weights;
        CyNetwork weightednet = null;
        int ncols = network.getDefaultNodeTable().getColumns().size();
        Object[] nomi = network.getDefaultNodeTable().getColumns().toArray();
        //computing max and min
        for(int i=0; i<ncols; i++){
            if(!nomi[i].toString().matches("SUID") && !nomi[i].toString().matches("shared name") && !nomi[i].toString().matches("selected") && !nomi[i].toString().matches("name")){
                CyColumn col = network.getDefaultNodeTable().getColumn(nomi[i].toString());
                for(int j=0; j<col.getValues(col.getType()).size(); j++){
                    int currentValue = Integer.parseInt(col.getValues(col.getType()).get(j).toString());
                    if(currentValue < min){
                        min = currentValue;
                    }
                    if(currentValue > max){
                        max = currentValue;
                    }
                }
            }
        }
        int answer = whatToDo();
        if(answer == 0){
            System.out.print("Doing nothing special with " + network.toString());
            weightednet = network;
        }        
        //else go on with the network generation
        else{
            weights = randomWeigths(min, max, nodes);
            if(directed == true){
                weightednet = weighNetDirected(weights,network);
            }
            else{
                weightednet = weighNetUndirected(weights,network);
            }
        }
        pushNetwork(weightednet);
    }
    
    public int whatToDo(){
        Object[] options = {"Abort","Continue"};
        int ans = JOptionPane.showOptionDialog(this.cyDesktopService.getJFrame(),
                "with a max = "+max+ " and nodes = " + network.getNodeCount() + " then by multiplying we will have up to " +(max*network.getNodeCount()+network.getNodeCount()) +" nodes", "CentiScaPe",
                JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE,
                null, options, options[0]);
        return ans;
    }
    
    public ArrayList randomWeigths(int min, int max, int nodes){
        ArrayList randvalues = new ArrayList();
        for(int i = 0; i<nodes; i++){
            randvalues.add(ThreadLocalRandom.current().nextInt(min, max + 1));
        }
        return randvalues;
    }
    
    public CyNetwork weighNetDirected(ArrayList<Integer> wgt, CyNetwork currentnet){
        System.out.println("directed");
        CyNode currentnode, newnode;
        List<CyNode> nodeslist = currentnet.getNodeList();
        List<CyEdge> edgeslist = currentnet.getEdgeList();
        CyNetwork wnet = copyOfExistingNetwork(nodeslist,edgeslist,false);
        wnet.getRow(wnet).set(CyNetwork.NAME, "Multiplied network");//copy of the existing network created
        List<CyNode> newnodeslist = wnet.getNodeList();
        String label;
        int currentweight;
        List<CyEdge> neighbourlist;
        for(int i=0; i<nodes; i++){//for all the nodes
            currentnode = newnodeslist.get(i);
            currentweight = wgt.get(i);
            for(int j = 0; j<currentweight; j++){//each nodes has #currentweight new copies
                neighbourlist = wnet.getAdjacentEdgeList(currentnode, CyEdge.Type.ANY);
                label = wnet.getDefaultNodeTable().getRow(currentnode.getSUID()).get("name", String.class)+"_child_"+j;
                newnode = wnet.addNode();
                wnet.getRow(newnode).set(CyNetwork.NAME, label);
                for(int k=0; k<neighbourlist.size(); k++){
                    if(neighbourlist.get(k).equals(currentnode)){/*if the source of the edge
                        is the currentnode then the new edge will be directed from the newnode to its target*/
                        wnet.addEdge(newnode, neighbourlist.get(k).getTarget(), true);
                    }
                    else{/*otherwise the source of the edge is the target of the current node, hence the new edge
                        will be generated from the target and goes to the newnode*/
                        wnet.addEdge(neighbourlist.get(k).getSource(), newnode, true);
                    }
                }
                wnet.addEdge(newnode, currentnode, true);//adds an edge between the new node and its original copy
                wnet.addEdge(currentnode, newnode, true);//in both directions
            }
        }     
        return wnet;
    }
    
    public CyNetwork weighNetUndirected(ArrayList<Integer> wgt, CyNetwork currentnet){
        System.out.println("undirected");
        CyNode currentnode, newnode;
        List<CyNode> nodeslist = currentnet.getNodeList();
        List<CyEdge> edgeslist = currentnet.getEdgeList();
        CyNetwork wnet = copyOfExistingNetwork(nodeslist,edgeslist,false);
        wnet.getRow(wnet).set(CyNetwork.NAME, "Multiplied network");
        List<CyNode> newnodeslist = wnet.getNodeList();
        String label;
        int currentweight;
        List<CyNode> neighbourlist;
        for(int i=0; i<nodes; i++){//for all the nodes
            currentnode = newnodeslist.get(i);
            currentweight = wgt.get(i);
            for(int j = 0; j<currentweight; j++){//each nodes had #currentweight new copies
                neighbourlist = wnet.getNeighborList(currentnode, CyEdge.Type.ANY);
                label = wnet.getDefaultNodeTable().getRow(currentnode.getSUID()).get("name", String.class)+"_child_"+j;
                newnode = wnet.addNode();
                wnet.getRow(newnode).set(CyNetwork.NAME, label);
                for(int l=0; l<neighbourlist.size(); l++){
                        wnet.addEdge(newnode, neighbourlist.get(l), true);                  
                }
                wnet.addEdge(newnode, currentnode, true);//adds an edge between the new node and its original copy
            }
        }
        return wnet;
    }
    
}