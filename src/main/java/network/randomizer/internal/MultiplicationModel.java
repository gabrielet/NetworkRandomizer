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
import java.util.Random;
import java.util.Scanner;
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
        network = core.getCurrentnetwork();        
        cyDesktopService = core.cyDesktopService;
        nodes = network.getNodeCount();
        directed = drct;
        path = file;
    }

    @Override
    protected final void initializeSpecifics() {
        System.out.println("initializeSpecificsMultiModel");
        min = Integer.MAX_VALUE;
        max = 1;
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
            else{JOptionPane.showMessageDialog(this.cyDesktopService.getJFrame(), "Empty file!", "Randomizer", JOptionPane.WARNING_MESSAGE);}
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
                "with a max = "+max+ " and nodes = " + network.getNodeCount() + " then by multiplying we will have up to " +tmpnodes+" nodes", "NetworkRandomizer",
                JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE,
                null, options, options[0]);
        return ans;
    }
    
    public int fileLength(int count){
        //checking if the file contains something, and, if yes, if it is correct!
        if(count > 1){
            System.out.println("greater than one");
            Object[] options = {"Let me double check","Everything is fine"};
            int ans = JOptionPane.showOptionDialog(this.cyDesktopService.getJFrame(),"found "+count+" values in the file. Is that correct or something is missing?", "NetworkRandomizer",
                    JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE,
                    null, options, options[0]);
            return ans;
        }
        else{System.out.println("lower than one");return 0;}
    }
    
    public ArrayList<Integer> randomWeigths(int min, int max, int nodes){
        //generating a number of copies for each node in the network, between min and max
        ArrayList randvalues = new ArrayList();
        Random rand = new Random();
        for(int i = 0; i<nodes; i++){
            randvalues.add(rand.nextInt((max-min)+1)+min);
        }
        System.out.println(randvalues.toString());
        return randvalues;
    }
    
    public CyNetwork weighNetDirected(ArrayList<Integer> wgt, CyNetwork currentnet){
        System.out.println("directed");
        CyNode currentnode, newnode;        
        String label;
        int currentweight;
        List<CyEdge> neighbourlist;
        CyEdge currentedge, newedge;
        List<CyNode> nodeslist = currentnet.getNodeList();
        List<CyEdge> edgeslist = currentnet.getEdgeList();
        CyNetwork wnet = copyOfExistingNetwork(nodeslist,edgeslist,false);
        wnet.getRow(wnet).set(CyNetwork.NAME, "Multiplied network");//copy of the existing network created
        List<CyNode> newnodeslist = wnet.getNodeList();
        List<CyEdge> newedgeslist = wnet.getEdgeList();
        //adding edges names to edgetable, with respect to the new copied network
        for(int i=0; i<newedgeslist.size(); i++){
            //String source = wnet.getDefaultNodeTable().getRow(newedgeslist.get(i).getSource().getSUID()).get("name", String.class);
            //String target = wnet.getDefaultNodeTable().getRow(newedgeslist.get(i).getTarget().getSUID()).get("name", String.class);            
            //wnet.getRow(newedgeslist.get(i)).set("interaction", createInteraction(source, target, wnet));
            wnet.getRow(newedgeslist.get(i)).set("interaction", "pp");
        }
        for(int i=0; i<nodes; i++){//for all the nodes
            currentnode = newnodeslist.get(i);
            currentweight = wgt.get(i);
            for(int j = 1; j<currentweight; j++){//each nodes has #currentweight new copies
                //but as below, j must be equal to one which is the minimum number of copies
                //a node can have
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
                        //wnet.getRow(newedge).set("interaction", createInteraction(newnode, neighbourlist.get(k).getTarget(), wnet));
                        wnet.getRow(newedge).set("interaction", "pp");
                    }
                    else{/*otherwise the source of the edge is the target of the current node, hence the new edge
                        will be generated from the target and goes to the newnode*/
                        newedge = wnet.addEdge(neighbourlist.get(k).getSource(), newnode, true);
                        wnet.getRow(newedge).set(CyNetwork.NAME, wnet.getDefaultEdgeTable().getRow(currentedge.getSUID()).get("name", String.class));
                        //wnet.getRow(newedge).set("interaction", createInteraction(neighbourlist.get(k).getTarget(), newnode, wnet));
                        wnet.getRow(newedge).set("interaction", "pp");
                    }
                }
                newedge = wnet.addEdge(newnode, currentnode, true);//adds an edge between the new node and its original copy                
                wnet.getRow(newedge).set(CyNetwork.NAME, wnet.getDefaultNodeTable().getRow(newedge.getSource().getSUID()).get("name", String.class)+" (undirected) "+ wnet.getDefaultNodeTable().getRow(newedge.getTarget().getSUID()).get("name", String.class));
                //wnet.getRow(newedge).set("interaction", createInteraction(newedge.getSource(), newedge.getTarget(), wnet));
                wnet.getRow(newedge).set("interaction", "pp");
                newedge = wnet.addEdge(currentnode, newnode, true);//in both directions
                wnet.getRow(newedge).set(CyNetwork.NAME, wnet.getDefaultNodeTable().getRow(newedge.getTarget().getSUID()).get("name", String.class)+" (undirected) "+ wnet.getDefaultNodeTable().getRow(newedge.getSource().getSUID()).get("name", String.class));
                //wnet.getRow(newedge).set("interaction", createInteraction(newedge.getTarget(), newedge.getSource(), wnet));
                wnet.getRow(newedge).set("interaction", "pp");
            }
        }     
        return wnet;
    }
    
    public CyNetwork weighNetUndirected(ArrayList<Integer> wgt, CyNetwork currentnet){
        System.out.println("undirected");
        CyNode currentnode, newnode;
        int currentweight;
        List<CyEdge> neighbourlist;
        CyEdge currentedge, newedge;
        List<CyNode> nodeslist = currentnet.getNodeList();
        List<CyEdge> edgeslist = currentnet.getEdgeList();
        CyNetwork wnet = copyOfExistingNetwork(nodeslist,edgeslist,false);
        wnet.getRow(wnet).set(CyNetwork.NAME, "Multiplied network");
        List<CyNode> newnodeslist = wnet.getNodeList();
        List<CyEdge> newedgeslist = wnet.getEdgeList();
        //adding edges names to edgetable, with respect to the new copied network
        for(int i=0; i<newedgeslist.size(); i++){
            //String source = wnet.getDefaultNodeTable().getRow(newedgeslist.get(i).getSource().getSUID()).get("name", String.class);
            //String target = wnet.getDefaultNodeTable().getRow(newedgeslist.get(i).getTarget().getSUID()).get("name", String.class);            
            //wnet.getRow(newedgeslist.get(i)).set("interaction", createInteraction(source, target, wnet));
            wnet.getRow(newedgeslist.get(i)).set("interaction", "pp");
        }
        for(int i=0; i<nodes; i++){//for all the nodes
            currentnode = newnodeslist.get(i);
            currentweight = wgt.get(i);
            for(int j = 1; j<currentweight; j++){//each nodes had #currentweight new copies
                //but j must be equal to one which is the lowest number of copies
                //i.e. if j is one and the file contains only one, then the method returns
                //the network from the input withoud new nodes
                neighbourlist = wnet.getAdjacentEdgeList(currentnode, CyEdge.Type.ANY);
                newnode = wnet.addNode();
                wnet.getRow(newnode).set(CyNetwork.NAME, wnet.getDefaultNodeTable().getRow(currentnode.getSUID()).get("name", String.class)+"_child_"+j);
                for(int l=0; l<neighbourlist.size(); l++){
                        currentedge = neighbourlist.get(l);
                        if(currentedge.getSource().equals(currentnode)){/*if the source of the edge
                        is the currentnode then the new edge will be directed from the newnode to its target*/
                        newedge = wnet.addEdge(newnode, neighbourlist.get(l).getTarget(), true);
                        wnet.getRow(newedge).set(CyNetwork.NAME, wnet.getDefaultEdgeTable().getRow(currentedge.getSUID()).get("name", String.class));
                        //wnet.getRow(newedge).set("interaction", createInteraction(newnode, neighbourlist.get(l).getTarget(), wnet));
                        wnet.getRow(newedge).set("interaction", "pp");

                    }
                    else{/*otherwise the source of the edge is the target of the current node, hence the new edge
                        will be generated from the target and goes to the newnode*/
                        newedge = wnet.addEdge(neighbourlist.get(l).getSource(), newnode, true);
                        wnet.getRow(newedge).set(CyNetwork.NAME, wnet.getDefaultEdgeTable().getRow(currentedge.getSUID()).get("name", String.class));
                        //wnet.getRow(newedge).set("interaction", createInteraction(neighbourlist.get(l).getTarget(), newnode, wnet));
                        wnet.getRow(newedge).set("interaction", "pp");
                    }
                }
                newedge = wnet.addEdge(newnode, currentnode, true);//adds an edge between the new node and its original copy
                wnet.getRow(newedge).set(CyNetwork.NAME, wnet.getDefaultNodeTable().getRow(newedge.getSource().getSUID()).get("name", String.class)+" (undirected) "+ wnet.getDefaultNodeTable().getRow(newedge.getTarget().getSUID()).get("name", String.class));
                //wnet.getRow(newedge).set("interaction", createInteraction(newedge.getSource(), newedge.getTarget(), wnet));
                wnet.getRow(newedge).set("interaction", "pp");
            }
        }
        return wnet;
    }
    
}