package joaodias.thesis_02.Utils;

import java.util.ArrayList;

public class Operations {

    /**
     * Calculates the average of a given set of signal strengths present in a 
     * vector.
     * 
     * @param rss vector with the WiFi signal strengths.
     * @return the average of the signal strengths inside the vector.
     */
    public double calc_vect_average(ArrayList<Integer> rss){
        double av = 0;
        for (int i = 0; i<rss.size(); i++){
            av += rss.get(i);
        }
        av /= rss.size();
        return av;
    }

    /**
     * Calculates the standard deviation of the signal strength values inside 
     * the signal strength vector. It takes as input the average of the signal 
     * strength and the signal strength vector.
     *  
     * @param average average of the signal stregnths inside the vector.
     * @param rss vector with the WiFi signal strengths.
     * @return the standard deviation of the signal strengths vector.
     */
    public double calc_vect_stdev(double average, ArrayList<Integer> rss){
        double sumsq = 0.0;
        for (int i = 0; i < rss.size(); i++)
            sumsq += Math.sqrt(Math.abs(average - rss.get(i)));
        double variance = sumsq / (rss.size());

        return Math.sqrt(variance);
    }

    /**
     * Calculates the average of a given set of signal strengths present in a 
     * vector.
     * 
     * @param marks vector with the WiFi signal strengths.
     * @return the average of the signal strengths inside the vector.
     */
    public double calculateAverage(ArrayList<Integer> marks) {
        Integer sum = 0;
        if(!marks.isEmpty()) {
            for (Integer mark : marks) {
                sum += mark;
            }
            return sum.doubleValue() / marks.size();
        }
        return sum;
    }
}
