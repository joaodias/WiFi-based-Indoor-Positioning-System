package joaodias.thesis_02.Utils;

/**
 * Created by joaodias on 10/1/15.
 */

import java.util.ArrayList;
import joaodias.thesis_02.Structures.DataHandle;
import joaodias.thesis_02.Structures.DataObserved;
import joaodias.thesis_02.Structures.LocDistance;

public class Algorithms {


    public String map_mmse(boolean isWeighted, DataHandle dh, DataObserved datao, ArrayList<String> ObservedMacList, double sGreek) {

        ArrayList<String> RSS_Values;
        double curResult = 0.0d;
        String myLocation = null;
        double highestProbability = Double.NEGATIVE_INFINITY;
        ArrayList<LocDistance> LocDistance_Results_List = new ArrayList<LocDistance>();

        // Find the location of user with the highest probability
        for (int i = 0; i<dh.getPositions().size(); i++) {
            curResult = calculateProbability(i, ObservedMacList, datao, dh, sGreek);

            if (curResult == Double.NEGATIVE_INFINITY)
                return null;
            else if (curResult > highestProbability) {
                highestProbability = curResult;
                myLocation = dh.getPositions().get(i);
            }

            if (isWeighted)
                LocDistance_Results_List.add(0, new LocDistance(curResult, dh.getPositions().get(i)));
        }

        if (isWeighted)
            myLocation = calculateWeightedAverageProbabilityLocations(LocDistance_Results_List);

        return myLocation;
    }


    public static double calculateProbability(int pos_index, ArrayList<String> ObservedMac, DataObserved data_observed, DataHandle data_handler, double sGreek) {

        double finalResult = 1;
        float v1;
        float v2;
        double temp;
        String str;
        String x = data_handler.getX(pos_index);
        String y = data_handler.getY(pos_index);

        for (int i = 0; i < ObservedMac.size(); ++i) {
            try {
                str = data_handler.getRSSI(ObservedMac.get(i), x, y);
                v1 = Float.valueOf(str.trim()).floatValue();
                str = data_observed.getRssi_observed(ObservedMac.get(i));
                v2 = Float.valueOf(str.trim()).floatValue();

                temp = v1 - v2;

                temp *= temp;

                temp = -temp;

                temp /= (sGreek * sGreek);
                temp = Math.exp(temp);

                finalResult *= temp;
            }catch(RuntimeException e){}
        }
        return finalResult;
    }

    public static String calculateWeightedAverageProbabilityLocations(ArrayList<LocDistance> LocDistance_Results_List) {

        double sumProbabilities = 0.00f;
        double WeightedSumX = 0.00f;
        double WeightedSumY = 0.00f;
        double NP;
        float x, y;
        String[] LocationArray = new String[2];

        // Calculate the sum of all probabilities
        for (int i = 0; i < LocDistance_Results_List.size(); ++i)
            sumProbabilities += LocDistance_Results_List.get(i).getDistance();

        // Calculate the weighted (Normalized Probabilities) sum of X and Y
        for (int i = 0; i < LocDistance_Results_List.size(); ++i) {
            LocationArray = LocDistance_Results_List.get(i).getLocation().split("&");

            try {
                x = Float.valueOf(LocationArray[0].trim()).floatValue();
                y = Float.valueOf(LocationArray[1].trim()).floatValue();
            } catch (Exception e) {
                return null;
            }

            NP = LocDistance_Results_List.get(i).getDistance() / sumProbabilities;

            WeightedSumX += (x * NP);
            WeightedSumY += (y * NP);

        }
        WeightedSumX = Math.round(WeightedSumX*100.0)/100.0;
        WeightedSumY = Math.round(WeightedSumY*100.0)/100.0;

        return WeightedSumX + "&" + WeightedSumY;
    }

    public double calcErrors(String estimated_location, String testX, String testY){ //Calculate Euclidean distance between two points
        String temp [] = estimated_location.split("&");

        double estimatedX = Double.parseDouble(temp[0]);
        double estimatedY = Double.parseDouble(temp[1]);
        double realX = Double.parseDouble(testX);
        double realY = Double.parseDouble(testY);

        double resultX = realX - estimatedX;
        double resultY = realY - estimatedY;

        resultX *= resultX;
        resultY *= resultY;

        double fr = resultX + resultY;

        fr = Math.sqrt(fr);

        fr = Math.round(fr*100.0)/100.0;

        return fr;
    }
}

