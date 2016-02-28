/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package network.randomizer.internal;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;

/**
 *
 * @author Ivan
 */
public class DegreePreservingModel extends AbstractModel{

    private final float ITERFACTOR = 10;
    private final float MAXTRIES = 5;
    int iters;
    CyNetwork net;
    
    public DegreePreservingModel(RandomizerCore core, int maxIters) {
        super(core);
        iters = maxIters;
    }
    public DegreePreservingModel(RandomizerCore core) {
        super(core);
        iters = 0;
    }

    @Override
    protected void initializeSpecifics() {
    }

    @Override
    public void Execute() throws Exception {       
        net = copyOfCurrentNetwork(false);
        net.getRow(net).set(CyNetwork.NAME, getStandardNetworkName());
        int numOfEdges = net.getEdgeCount();
        if(iters == 0){
            iters = (int)(numOfEdges * ITERFACTOR);
        }
        List<CyEdge> CyEdges = net.getEdgeList();
        ArrayList<Long> SUIDs = new ArrayList<>(numOfEdges);
        
        for (CyEdge edge : CyEdges) {
            SUIDs.add(edge.getSUID());
        }

        int tryNum = 0;
        while(tryNum++ < MAXTRIES){
            
            for (int i = 0; i < iters; i++) {
                int firstId = random.nextInt(numOfEdges);
                int secondId = random.nextInt(numOfEdges);
                if(firstId == secondId) continue;
                
                long firstSUID = SUIDs.get(firstId);
                long secondSUID = SUIDs.get(secondId);
                
                CyEdge first = net.getEdge(firstSUID);
                CyEdge second = net.getEdge(secondSUID);
                
                CyNode fa = first.getSource();
                CyNode fb = first.getTarget();
                CyNode sa = second.getSource();
                CyNode sb = second.getTarget();
                
                if(fa.equals(fb) || fa.equals(sa) || fa.equals(sb) || fb.equals(sa) || fb.equals(sb) || sa.equals(sb)) continue;
                
                if(net.containsEdge(fa, sb) || net.containsEdge(sa, fb)) continue;
                
                List<CyEdge> toRemove = new LinkedList<>();
                toRemove.add(first);
                toRemove.add(second);
                net.removeEdges(toRemove);
                
                CyEdge newFirst = net.addEdge(fa, sb, false);
                CyEdge newSecond = net.addEdge(sa, fb, false);
                
                SUIDs.set(firstId, newFirst.getSUID());
                SUIDs.set(secondId, newSecond.getSUID());
            }

            if(isConnected()) break;
        }
        
        
        
        pushNetwork(net);
    }
    
    private boolean isConnected(){
        return true;
    }

    @Override
    protected String getModelName() {
        return "DegreePreserving";
    }
    
}
