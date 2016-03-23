/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package network.randomizer.internal;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.ThreadLocalRandom;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;

/**
 *
 * @author gabriele
 */
public class MultiplicationModel extends AbstractModel{
    
    private CyNetwork network;
    private CyApplicationManager cyApplicationManager;
    private CySwingApplication cyDesktopService;
    private int min = 1000000, max = 0, nodes;
    private boolean directed;
    //private String what;
    private String path;
    
    public MultiplicationModel(RandomizerCore core, boolean drct, String file){
        super(core);
        cyApplicationManager = core.cyApplicationManager;
        network = core.cyApplicationManager.getCurrentNetwork();        
        cyDesktopService = core.cyDesktopService;
        nodes = network.getNodeCount();
        directed = drct;
        //what = attribute;
        path = file;
    }

    @Override
    protected void initializeSpecifics() {
        System.out.println("initializeSpecificsMultiModel");
    }

    @Override
    protected String getModelName() {
        return("Multiplication Model");
    }
    
    @Override
    public void Execute() throws Exception{
        //recovering info about attributes table
        Scanner scanner;
        CyNetwork weightednet;
        ArrayList<Integer> weights = new ArrayList();
        try {
            scanner = new Scanner(new File(path));
            while(scanner.hasNextInt()){
                int next = scanner.nextInt();
                weights.add(next);
                System.out.println(next);
            }
            for(int j=0; j<weights.size(); j++){
                    int currentValue = weights.get(j);
                    if(currentValue < min){
                        min = currentValue;
                    }
                    if(currentValue > max){
                        max = currentValue;
                    }
            }
            System.out.println("min, max "+min+","+max);
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
            
        } catch (FileNotFoundException ex) {
            JOptionPane.showMessageDialog(this.cyDesktopService.getJFrame(), "File not Found method!", "Randomizer", JOptionPane.WARNING_MESSAGE);
        }
    }
    
    public int whatToDo(){
        Object[] options = {"Abort","Continue"};
        int ans = JOptionPane.showOptionDialog(this.cyDesktopService.getJFrame(),
                "with a max = "+max+ " and nodes = " + network.getNodeCount() + " then by multiplying we will have up to " +(max*network.getNodeCount()+network.getNodeCount()) +" nodes", "NetworkRandomizer",
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
        CyEdge currentedge, newedge;
        for(int i=0; i<nodes; i++){//for all the nodes
            currentnode = newnodeslist.get(i);
            currentweight = wgt.get(i);
            for(int j = 0; j<currentweight; j++){//each nodes has #currentweight new copies
                neighbourlist = wnet.getAdjacentEdgeList(currentnode, CyEdge.Type.ANY);
                label = wnet.getDefaultNodeTable().getRow(currentnode.getSUID()).get("name", String.class)+"_child_"+j;
                newnode = wnet.addNode();
                wnet.getRow(newnode).set(CyNetwork.NAME, label);
                for(int k=0; k<neighbourlist.size(); k++){
                    currentedge = neighbourlist.get(k);
                    if(currentedge.getSource().equals(currentnode)){/*if the source of the edge
                        is the currentnode then the new edge will be directed from the newnode to its target*/
                        newedge = wnet.addEdge(newnode, neighbourlist.get(k).getTarget(), true);
                        wnet.getRow(newedge).set(CyNetwork.NAME, wnet.getDefaultEdgeTable().getRow(currentedge.getSUID()).get("name", String.class));
                    }
                    else{/*otherwise the source of the edge is the target of the current node, hence the new edge
                        will be generated from the target and goes to the newnode*/
                        newedge = wnet.addEdge(neighbourlist.get(k).getSource(), newnode, true);
                        wnet.getRow(newedge).set(CyNetwork.NAME, wnet.getDefaultEdgeTable().getRow(currentedge.getSUID()).get("name", String.class));
                    }
                }
                newedge = wnet.addEdge(newnode, currentnode, true);//adds an edge between the new node and its original copy                
                wnet.getRow(newedge).set(CyNetwork.NAME, wnet.getDefaultNodeTable().getRow(newedge.getSource().getSUID()).get("name", String.class)+" (undirected) "+ wnet.getDefaultNodeTable().getRow(newedge.getTarget().getSUID()).get("name", String.class));
                newedge = wnet.addEdge(currentnode, newnode, true);//in both directions
                wnet.getRow(newedge).set(CyNetwork.NAME, wnet.getDefaultNodeTable().getRow(newedge.getTarget().getSUID()).get("name", String.class)+" (undirected) "+ wnet.getDefaultNodeTable().getRow(newedge.getSource().getSUID()).get("name", String.class));
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
        int currentweight;
        //List<CyNode> neighbourlist;
        List<CyEdge> neighbourlist;
        CyEdge currentedge, newedge;
        for(int i=0; i<nodes; i++){//for all the nodes
            currentnode = newnodeslist.get(i);
            currentweight = wgt.get(i);
            for(int j = 0; j<currentweight; j++){//each nodes had #currentweight new copies
                //neighbourlist = wnet.getNeighborList(currentnode, CyEdge.Type.ANY);
                neighbourlist = wnet.getAdjacentEdgeList(currentnode, CyEdge.Type.ANY);
                newnode = wnet.addNode();
                wnet.getRow(newnode).set(CyNetwork.NAME, wnet.getDefaultNodeTable().getRow(currentnode.getSUID()).get("name", String.class)+"_child_"+j);
                for(int l=0; l<neighbourlist.size(); l++){
                        //wnet.addEdge(newnode, neighbourlist.get(l), true);   
                        currentedge = neighbourlist.get(l);
                        if(currentedge.getSource().equals(currentnode)){/*if the source of the edge
                        is the currentnode then the new edge will be directed from the newnode to its target*/
                        newedge = wnet.addEdge(newnode, neighbourlist.get(l).getTarget(), true);
                        wnet.getRow(newedge).set(CyNetwork.NAME, wnet.getDefaultEdgeTable().getRow(currentedge.getSUID()).get("name", String.class));
                    }
                    else{/*otherwise the source of the edge is the target of the current node, hence the new edge
                        will be generated from the target and goes to the newnode*/
                        newedge = wnet.addEdge(neighbourlist.get(l).getSource(), newnode, true);
                        wnet.getRow(newedge).set(CyNetwork.NAME, wnet.getDefaultEdgeTable().getRow(currentedge.getSUID()).get("name", String.class));
                    }
                }
                newedge = wnet.addEdge(newnode, currentnode, true);//adds an edge between the new node and its original copy
                wnet.getRow(newedge).set(CyNetwork.NAME, wnet.getDefaultNodeTable().getRow(newedge.getSource().getSUID()).get("name", String.class)+" (undirected) "+ wnet.getDefaultNodeTable().getRow(newedge.getTarget().getSUID()).get("name", String.class));
            }
        }
        return wnet;
    }
    
}