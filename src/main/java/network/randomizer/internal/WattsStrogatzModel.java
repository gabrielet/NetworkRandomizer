/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package network.randomizer.internal;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.SortedSet;
import java.util.TreeSet;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;

/**
 *
 * @author Ivan
 */
public class WattsStrogatzModel extends AbstractModel{

    private int N;
    private int K;
    private float beta;
    
    
    public WattsStrogatzModel(RandomizerCore core, int N, int K, float beta) {
        super(core);
        this.N = N;
        this.K = K;
        this.beta = beta;
    }

    @Override
    protected void initializeSpecifics() {
    }

    @Override
    public void Execute() {
        if(N < 0 || K >= N || K < 0 || beta < 0 || beta > 1){
            return;
        }
        CyNetwork net = generateEmptyNetwork(N);
        
        ArrayList<CyNode> nodes = new ArrayList<>(net.getNodeList());
        int Khalf = K/2;
        
        ArrayList<TreeSet<Integer>> neighbourhood = new ArrayList<>(N);
        
        LinkedList<Edge> edges = new LinkedList<>();
        
        for (int i = 0; i < N; i++) {
            neighbourhood.add(new TreeSet<Integer>());
        }
        
        
        // generate lattice
        for (int i = 0; i < N; i++) {
            for (int j = 0; j < Khalf; j++) {
                int a = i;
                int b = (i+j+1)%N;
                neighbourhood.get(a).add(b);
                neighbourhood.get(b).add(a);
                edges.add(new Edge(a, b));
            }
        }
        
        // rewire
        for (Edge edge : edges) {
            if(randomBoolean(beta)){
                int i = edge.a;
                int j = edge.b;
                SortedSet<Integer> neighbours = neighbourhood.get(i);
                neighbours.add(i);
                int numOfNeighbours = neighbours.size();
                int newJ = random.nextInt(N - numOfNeighbours);
                for (Integer neighbour : neighbours) {
                    if(newJ >= neighbour){
                        newJ++;
                    }
                    else break;
                }
                neighbours.remove(i);
                neighbours.remove(j);
                neighbourhood.get(j).remove(i);
                neighbours.add(newJ);
                neighbourhood.get(newJ).add(i);
            }
        }
        
        for (Integer i = 0; i < N; i++) {
            for (Integer j : neighbourhood.get(i).tailSet(i)) {
                CyEdge edge = net.addEdge(nodes.get(i), nodes.get(j), false);
                // Not sure about this naming!
                String name = i.toString() + "_" + j.toString();
                net.getRow(edge).set(CyNetwork.NAME, name);
            }
        }
        
        pushNetwork(net);
        
    }

    @Override
    protected String getModelName() {
        return "WattsStrogatz";
    }
    
}
