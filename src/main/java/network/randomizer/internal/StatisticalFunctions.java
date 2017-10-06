/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package network.randomizer.internal;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyNetwork;

/**
 *
 * @author gabriele
 */
public class StatisticalFunctions {
        
    public List<CyNetwork> allthenets;
        
    StatisticalFunctions(RandomizerCore core){
        System.out.println("statistics");
        allthenets = core.cyApplicationManager.getSelectedNetworks();
    }
    
    public ArrayList<ArrayList<Double>> getCentrality(String whichcentrality, List<CyNetwork> whichnet){
        ArrayList<ArrayList<Double>> centralities = new ArrayList();
        ArrayList<Double> tmp = new ArrayList();
        CyColumn col;
        int l = whichnet.size();
        //given a centrality
        for(CyNetwork net : whichnet){//iterate over the networks
            tmp.clear();
            col = net.getDefaultNodeTable().getColumn(whichcentrality);//get the centrality j from the network i
            List<Object> values = col.getValues(col.getType());//get the values for that column
            int e = values.size();
            if(!values.isEmpty()){                 
                for(int k=0; k<e; k++){
                    tmp.add(Double.parseDouble(values.get(k).toString()));
                }
            }
            else{return null;}//have to check whether the list are filled with something
            centralities.add(tmp);
        }
        return centralities;
    }
       
       
    public List<String> getColumnNames(List<CyNetwork> real, List<CyNetwork> rnd){
        //this methods gets all the columns names of all the selected networks in order to find all the common attributes
        //attributes are integer, long or double and are not called name, suid, selected or sharedname
        List<String> centralities = new ArrayList();
        Collection<CyColumn> tmp;
        for (CyNetwork net : real) {
            tmp = net.getDefaultNodeTable().getColumns();
            for(CyColumn col : tmp){
                if(!col.getName().matches("name") && !col.getName().matches("SUID") && !col.getName().matches("selected") && !col.getName().matches("shared name") && (col.getType() == (Integer.class) || col.getType() == (Long.class) || col.getType() == (Double.class))){
                    centralities.add(col.getName());
                }
            }
        }
        for (CyNetwork net : rnd) {
            tmp = net.getDefaultNodeTable().getColumns();
            for(CyColumn col : tmp){
                if(!col.getName().matches("name") && !col.getName().matches("SUID") && !col.getName().matches("selected") && !col.getName().matches("shared name") && (col.getType() == (Integer.class) || col.getType() == (Long.class) || col.getType() == (Double.class))){
                    centralities.add(col.getName());
                }
            }
        }
        return centralities;
    }
    
    public List<String> compareWhat(List<String> listofcentrs, int howmanynets){
        //this methods finds the centralities that occurs homanynets times that means all the networks share that attribute hence it can be compare along all the networks.
        List<String> thesecentralities = new ArrayList();
        for(String str : listofcentrs){
            if(howmanynets==(Collections.frequency(listofcentrs, str))){
                if(!thesecentralities.contains(str)){
                    thesecentralities.add(str);
                }
            }
        }
        return thesecentralities;
    } 
        
    
    static class DistanceMatrix{
        public double[][] distMatrix;
        private int height, width;

        public DistanceMatrix(int height, int width) {
            this.height = height;
            this.width = width;
            distMatrix = new double[height][width];
        }
        public void set(int i, int j, Double value){
            if(i < height && j < width) distMatrix[i][j] = value;
            else System.out.println("DistanceMatrix.set out of bounds!");
        }
        public Double get(int i, int j){
            if(i < height && j < width) return distMatrix[i][j];
            System.out.println("DistanceMatrix.get out of bounds!");
            return -1D;
        }
    }
    
    public boolean createHeat(DistanceMatrix dstmt) throws IOException{
        
        boolean its_ok = true;
        double[][] heatmap=null;    
        //HeatChart chart = new HeatChart(dstmt.distMatrix);
        //chart.setLowValueColour(Color.BLUE);
        //chart.setHighValueColour(Color.RED);
        // Customise the chart.
        //chart.setTitle("This is my chart title");
        //chart.setXAxisLabel("X Axis");
        //chart.setYAxisLabel("Y Axis");// Output the chart to a file.
        //chart.saveToFile(new File("my_chart.png"));                                   
        return its_ok;    
    }
    
    
    /**
     * 
     * Calculates a distance matrix between two given sets of networks.
     * Currently, this method SORTS ALL THE LISTS GIVEN TO IT, without copying them!!!
     * @param verticalGroup - each element of this group matches one ROW of the returned matrix (elements are placed on the VERTICAL edge of the matrix)
     * @param horizontalGroup  - each element of this group matches one COLUMN of the returned matrix (elements are placed on the HORIZONTAL edge of the matrix)
     * @return - element (i,j) of the returned matrix is the distance between verticalGroup(i) and horizontalGroup(j)
     */
    public DistanceMatrix getDistanceMatrix(ArrayList<ArrayList<Double>> verticalGroup, ArrayList<ArrayList<Double>> horizontalGroup){
        int nv = verticalGroup.size();
        int nh = horizontalGroup.size();
        DistanceMatrix matrix = new DistanceMatrix(nv, nh);
        
        // sort all lists
        for (int i = 0; i < nv; i++) {
            Collections.sort(verticalGroup.get(i));
        }
        for (int i = 0; i < nh; i++) {
            Collections.sort(horizontalGroup.get(i));
        }
        
        // calculate KS
        for (int i = 0; i < nv; i++) {
            for (int j = 0; j < nh; j++) {
                double dist = KS_Test(verticalGroup.get(i), horizontalGroup.get(j));
                matrix.set(i, j, dist);
            }
        }
        return matrix;
    }
    
    // Two-sample Kolmogorov-Smirnov test
    //ordered input presumed!
    public double KS_Test(ArrayList<Double> first, ArrayList<Double> second){
        //maximum distance between distributions
        double dist = 0;
        
        //first distribution size and current count
        double n1 = first.size();
        int count1 = 0;
        
        //second distribution size and current count
        double n2 = second.size();
        int count2 = 0;

        //continue while both lists are nonempty
        double tempdist;
        while(count1 < n1 && count2 < n2){
            //if front of the first list is next smallest value
            if(first.get(count1) < second.get(count2)){
                count1++;
            }
            //if front of the second list is next smallest value
            else if(first.get(count1) > second.get(count2)){
                count2++;
            }
            //if both fron values are next smallest values
            else{
                count1++;
                count2++;
            }
            //current distance
            tempdist = Math.abs((count1/n1) - (count2/n2));
            //update max distance
            if(tempdist > dist) dist = tempdist;
        }        
        return dist;
    }    
    
    
    
    
    // -----------------------------------------------------
    //                -- OUTPUT METHODS --
    // -----------------------------------------------------
    
    
    // SINGLE REAL NETWORK
    
    /**
     * Generate and save an output file for the case when there is only one real network.
     * @param filePath - path for the output file, together with its name.
     *                   NOTE: OS specific path delimiters may produce bugs!
     *                         If paths are not built automatically for file-chooser, 
     *                         File.separator object should be used instead of '/' and '\'
     * @param realName name of the real network
     * @param randomNames names of the random networks
     * @param centralityNames names of centrality measures
     * @param distMatrices distance matrices in the same order as centrality names (one matrix per centrality measure).
     *                     Each matrix is of size 1xR, where R is the number of random networks.
     * @return True if writing passed without errors, false otherwise.
     */
    public boolean singleRealGenerateOutput(String filePath, 
                                             String realName, 
                                             List<String> randomNames, 
                                             List<String> centralityNames, 
                                             List<DistanceMatrix> distMatrices){
        int R = randomNames.size();
        int C = centralityNames.size();
        // output length approximation
        int initialLength = 1000 + R * 100 + C * 120 + R * C * 5;
        StringBuilder output = new StringBuilder(initialLength);
        String separator = ",";
        
        output.append("# Network comparison report file generated by Cytoscape - Randomizer app\n\n> Real network\n");
        output.append(realName);
        output.append("\n\n> Random networks\n");
        
        boolean first = true;
        for (String name : randomNames) {
            if(!first) output.append(separator);
            output.append(name);
            first = false;
        }
        
        output.append("\n\n> Centralities\n");
        
        first = true;
        for (String name : centralityNames) {
            if(!first) output.append(separator);
            output.append(name);
            first = false;
        }
        
        output.append("\n\n\n> Average difference across all centralities between real\nand random networks\n\nrandom_net,avg_diff\n");
        
        double[] averages = new double[R];
        for (int i = 0; i < R; i++) {
            averages[i] = 0;
        }
        for (DistanceMatrix matrix : distMatrices) {
            for (int i = 0; i < R; i++) {
                averages[i] += matrix.get(0, i);
            }
        }
        int index = 0;
        for (String name : randomNames) {
            output.append(name);
            output.append(separator);
            Double value = averages[index++]/C;
            output.append(doubleToString(value));
            output.append("\n");
        }
        
        
        output.append("\n\n\n> Difference between real network and its most similar random network\nfor each centrality\n\ncentrality,random_net,diff\n");
        // If slow, this should be replaced with parallel iterators!
        for (int i = 0; i < C; i++) {
            output.append(centralityNames.get(i));
            output.append(separator);
            DistanceMatrix matrix = distMatrices.get(i);
            int randId = 0;
            Double minVal = Double.MAX_VALUE;
            for (int j = 0; j < R; j++) {
                if(matrix.get(0,j) < minVal){
                    minVal = matrix.get(0,j);
                    randId = j;
                }
            }
            output.append(randomNames.get(randId));
            output.append(separator);
            output.append(doubleToString(minVal));
            output.append("\n");
        }
        
        
        
        output.append("\n\n\n> Difference matrix between real and random networks for each centrality\n\ncentrality,");
        first = true;
        for (String name : randomNames) {
            if(!first) output.append(separator);
            output.append(name);
            first = false;
        }
        output.append("\n");
        
        
        // If slow, this should be replaced with parallel iterators!
        for (int i = 0; i < C; i++) {
            output.append(centralityNames.get(i));
            output.append(separator);
            DistanceMatrix matrix = distMatrices.get(i);
            output.append(doubleToString(matrix.get(0,0)));
            for (int j = 1; j < R; j++) {
                output.append(separator);
                output.append(doubleToString(matrix.get(0,j)));
            }
            output.append("\n");
        }

        return writeToFile(filePath, output.toString());
    }
    
    
    
    
     
    
    
    
    // MULTIPLE REAL NETWORKS
    
    /**
     * Generate and save an output file for the case when there are multiple real networks.
     * @param filePath - path for the output file, together with its name.
     *                   NOTE: OS specific path delimiters may produce bugs!
     *                         If paths are not built automatically for file-chooser, 
     *                         File.separator object should be used instead of '/' and '\'
     * @param realNames names of the real networks
     * @param randomNames names of the random networks
     * @param centralityNames names of centrality measures
     * @param distMatrices distance matrices in the same order as centrality names (one matrix per centrality measure).
     *                     Each matrix is of size NxR, where N is the number of real, and R is the number of random networks.
     * @return True if writing passed without errors, false otherwise.
     */
    public boolean multipleRealGenerateOutput(String filePath, 
                                             List<String> realNames, 
                                             List<String> randomNames, 
                                             List<String> centralityNames, 
                                             List<DistanceMatrix> distMatrices){
        int N = realNames.size();
        int R = randomNames.size();
        int C = centralityNames.size();
        // output length approximation
        int initialLength = 1000 + N * 160 + R * 90 + C * 40 + N * C * 120 + N * R * 10;
        StringBuilder output = new StringBuilder(initialLength);
        String separator = ",";
        
        output.append("# Network comparison report file generated by Cytoscape - Randomizer app\n\n> Real networks\n");
        
        boolean first = true;
        for (String name : realNames) {
            if(!first) output.append(separator);
            output.append(name);
            first = false;
        }
        
        output.append("\n\n> Random networks\n");
        
        first = true;
        for (String name : randomNames) {
            if(!first) output.append(separator);
            output.append(name);
            first = false;
        }
        
        output.append("\n\n> Centralities\n");
        
        first = true;
        for (String name : centralityNames) {
            if(!first) output.append(separator);
            output.append(name);
            first = false;
        }
        
        output.append("\n\n\n> Average difference across all centralities between real networks\nand their most similar random network\n\nreal_net,random_net,avg_diff\n");
        
        
        for (int realId = 0; realId < N; realId++) {
            output.append(realNames.get(realId));
            output.append(separator);
            
            double[] averages = new double[R];
            for (int i = 0; i < R; i++) {
                averages[i] = 0;
            }
            for (DistanceMatrix matrix : distMatrices) {
                for (int i = 0; i < R; i++) {
                    averages[i] += matrix.get(realId, i);
                }
            }
            int randId = 0;
            Double minVal = Double.MAX_VALUE;
            for (int i = 0; i < R; i++) {
                if(averages[i] < minVal){
                    minVal = averages[i];
                    randId = i;
                }
            }
            output.append(randomNames.get(randId));
            output.append(separator);
            minVal /= C;
            output.append(doubleToString(minVal));
            output.append("\n");
        }
        
        output.append("\n\n>Difference between real networks and its most similar random network\nfor each centrality\n\ncentrality,real_net,random_net,diff\n");
        // If slow, this should be replaced with parallel iterators!
        for (int i = 0; i < C; i++) {
            for (int realId = 0; realId < N; realId++) {
                output.append(centralityNames.get(i));
                output.append(separator);
                output.append(realNames.get(realId));
                output.append(separator);
                DistanceMatrix matrix = distMatrices.get(i);
                int randId = 0;
                Double minVal = Double.MAX_VALUE;
                for (int j = 0; j < R; j++) {
                    if(matrix.get(0,j) < minVal){
                        minVal = matrix.get(realId,j);
                        randId = j;
                    }
                }
                output.append(randomNames.get(randId));
                output.append(separator);
                output.append(doubleToString(minVal));
                output.append("\n");
            }
        }
        
        
        
        output.append("\n\n\n> Average difference matrix between real and random networks across all centralities\n\nreal_net");
        for (String name : randomNames) {
            output.append(separator);
            output.append(name);
        }
        output.append("\n");
        
        
        // If slow, this should be replaced with parallel iterators!
        for (int realId = 0; realId < N; realId++) {
            output.append(realNames.get(realId));
            
            double[] averages = new double[R];
            for (int i = 0; i < R; i++) {
                averages[i] = 0;
            }
            for (DistanceMatrix matrix : distMatrices) {
                for (int i = 0; i < R; i++) {
                    averages[i] += matrix.get(realId, i);
                }
            }
            for (int i = 0; i < R; i++) {
                averages[i] /= C;
                output.append(separator);
                output.append(doubleToString(averages[i]));
            }
            output.append("\n");
        }

        return writeToFile(filePath, output.toString());
    }
            
    private static String doubleToString(Double value){
        return String.format(Locale.ENGLISH,"%.5f", value);
    }
    
    private static boolean writeToFile(String filePath, String output){
        try (Writer writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(filePath), "utf-8"))) {
            writer.write(output);
        } catch(IOException e){
            System.out.println("IOException at StatisticalFunctions.writeToFile while trying to write to file '" + filePath + "'\n Exception details: " + e.toString());
            return false;
        }
        return true;
    }
    
}
