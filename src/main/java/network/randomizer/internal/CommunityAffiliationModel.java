/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package network.randomizer.internal;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;

/**
 * Community affiliation graph model implements a network generation model as described
 * in "Community-Affiliation Graph Model for Overlapping Network Community Detection" (2012)
 * by J. Yang and J. Leskovec.
 * 
 * @author Ivan
 */
public class CommunityAffiliationModel extends AbstractModel{
    
    // Community data file path
    String communityDataPath;
    // p value for each community, similar to ER model p value.
    LinkedList<Float> communityP;
    // neighbours of each node
    HashMap<String, LinkedList<String>> neighbourhood;

    /**
     * CommunityData file format expects one line for each community. Each line starts with a p value of that community (float),
     * followed by a space-separated IDs of nodes which are a part of that community.
     * @param core
     * @param communityDataPath
     * @throws Exception 
     */
    public CommunityAffiliationModel(RandomizerCore core, String communityDataPath) throws Exception {
        super(core);
        this.communityDataPath = communityDataPath;
    }

    @Override
    protected void initializeSpecifics() {
    }

    @Override
    public void Execute() throws Exception{
        communityP = new LinkedList<>();
        neighbourhood = new HashMap<>();
        try (BufferedReader br = new BufferedReader(new FileReader(communityDataPath))) {
            String line;
            
            // add edges for each community (line in the community data file)
            while ((line = br.readLine()) != null) {
                String[] lineElements = line.split(" ");
                
                // probability of an edge between each two nodes in a community
                float p = Float.parseFloat(lineElements[0]);
                
                // nodes in a current community
                LinkedList<String> currentCommunity = new LinkedList<>();
                
                // for each node in a community, add edges to current members with probability p
                for (int i = 1; i < lineElements.length; i++) {
                    boolean alreadyInCommunity = false;
                    String node = lineElements[i];
                    // if new node, construct its neighbour list
                    if(!neighbourhood.containsKey(node)){
                        neighbourhood.put(node, new LinkedList<String>());
                    }
                    // connect to current members
                    for (String communityNode : currentCommunity) {
                        if(node.equals(communityNode)) {
                            alreadyInCommunity = true;
                            continue;
                        }
                        if(randomBoolean(p)){
                            neighbourhood.get(node).add(communityNode);
                        }
                    }
                    if(!alreadyInCommunity) currentCommunity.add(node);
                }
            }
        } catch(FileNotFoundException e){
            throw new Exception("File not found!", e);
        } catch(NumberFormatException e){
            throw new Exception("File format error!", e);
        }

        // map node names to internal node Ids (used in the nodes array)
        HashMap<String, Integer> nodeNameToId = new HashMap<>();
        int nodeId = 0;
        for (String nodeName : neighbourhood.keySet()) {
            nodeNameToId.put(nodeName, nodeId);
            nodeId++;
        }
        
        int N = neighbourhood.size();
        CyNetwork net = generateEmptyNetwork(N);
        ArrayList<CyNode> nodes = new ArrayList<>(net.getNodeList());
        
        for (Map.Entry<String, LinkedList<String>> neighbours : neighbourhood.entrySet()) {
            String nodeName1 = neighbours.getKey();
            Integer nodeId1 = nodeNameToId.get(nodeName1);
            net.getRow(nodes.get(nodeId1)).set(CyNetwork.NAME, nodeName1);
            for (String nodeName2 : neighbours.getValue()) {
                Integer nodeId2 = nodeNameToId.get(nodeName2);
                if(!net.containsEdge(nodes.get(nodeId1), nodes.get(nodeId2))){
                    CyEdge edge = net.addEdge(nodes.get(nodeId1), nodes.get(nodeId2), false);
                    String name = nodeName1 + "_" + nodeName2;
                    net.getRow(edge).set(CyNetwork.NAME, name);
                }
            }
        }
        pushNetwork(net);

    }

    @Override
    protected String getModelName() {
        return "CommunityAffiliation";
    }
    
}
