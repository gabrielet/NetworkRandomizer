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
import javax.swing.JOptionPane;
import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.swing.CySwingApplication;
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
    private int min, max, nodes;
    private boolean directed;
    private String path;
    int answer;
    CyNetwork weightednet;
    ArrayList<Integer> weights = new ArrayList();
    
    
    public MultiplicationModel(RandomizerCore core, boolean drct, String file){
        super(core);
        cyApplicationManager = core.cyApplicationManager;
        network = core.cyApplicationManager.getCurrentNetwork();        
        cyDesktopService = core.cyDesktopService;
        nodes = network.getNodeCount();
        directed = drct;
        path = file;
    }

    @Override
    protected final void initializeSpecifics() {
        System.out.println("initializeSpecificsMultiModel");
        min = Integer.MAX_VALUE;
        max = 0;
        int counter = 1, len;
        //recovering info about attributes table
        Scanner scanner;
        try{
            scanner = new Scanner(new File(path));
            while(scanner.hasNextInt()){
                int next = scanner.nextInt();
                weights.add(next);
                counter = counter + 1;
            }
            len = fileLength(counter);
            System.out.println("len "+len);
            if(len == 1){
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
                answer = whatToDo();
            }
            else{System.out.print("choose file again");}
        }
        catch (FileNotFoundException ex) {
            JOptionPane.showMessageDialog(this.cyDesktopService.getJFrame(), "File not found!", "Randomizer", JOptionPane.WARNING_MESSAGE);
        }
    }

    @Override
    protected String getModelName() {
        return("Multiplication Model");
    }
    
    @Override
    public void Execute(){
        System.out.println("ans "+answer);
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
        int tmpnodes, edges;
        tmpnodes = max*network.getNodeCount()+network.getNodeCount();
        edges = (nodes*(nodes-1))/2; //the number of edges a fully connected networks has
        int ans = JOptionPane.showOptionDialog(this.cyDesktopService.getJFrame(),
                "with a max = "+max+ " and nodes = " + network.getNodeCount() + " then by multiplying we will have up to " +tmpnodes+" nodes and "+edges+" edges", "NetworkRandomizer",
                JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE,
                null, options, options[0]);
        return ans;
    }
    
    public int fileLength(int count){
        Object[] options = {"Let me double check","Everything is fine"};
        int ans = JOptionPane.showOptionDialog(this.cyDesktopService.getJFrame(),"found "+count+" values in the file. Is that correct or something is missing?", "NetworkRandomizer",
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
        List<CyEdge> neighbourlist;
        CyEdge currentedge, newedge;
        for(int i=0; i<nodes; i++){//for all the nodes
            currentnode = newnodeslist.get(i);
            currentweight = wgt.get(i);
            for(int j = 0; j<currentweight; j++){//each nodes had #currentweight new copies
                neighbourlist = wnet.getAdjacentEdgeList(currentnode, CyEdge.Type.ANY);
                newnode = wnet.addNode();
                wnet.getRow(newnode).set(CyNetwork.NAME, wnet.getDefaultNodeTable().getRow(currentnode.getSUID()).get("name", String.class)+"_child_"+j);
                for(int l=0; l<neighbourlist.size(); l++){
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