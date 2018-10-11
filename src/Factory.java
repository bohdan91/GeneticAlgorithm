import java.util.Arrays;
import java.util.concurrent.Exchanger;
import java.util.concurrent.ThreadLocalRandom;

/**
 * This is an emulation of a factory building with random stations being placed randomly
 * then using genetic algorithm randomly change locations of stations and accept ones
 * that increase overall affinity of placing stations on factory.
 * Affinity benefits if stations with more diverse ids are placed next to each other.
 */
public class Factory implements Runnable{
    private int [][] spots; //main array of rows and columns of factory
    private int [] stations; //initial set of stations
    private int currentAffinity; //last calculated affinity
    private Exchanger<int[]> exchanger; //exchanger is used to exchange some row with some other thread
    private GUI gui; //reference to GUI


    /**
     *
     * @param width width of factory
     * @param height height of factory
     * @param stations set of initial stations
     * @param exchanger reference to exchanger
     * @param gui reference to GUI object
     */
    Factory(int width, int height, int[] stations, Exchanger <int[]> exchanger, GUI gui){
        this.exchanger = exchanger;
        this.gui = gui;
        spots = new int[height][width];
        this.stations = stations;
        spots = fillSpots(spots);
        currentAffinity = calculateAffinity(spots);
    }

    /**
     * This is a main loop of modifying a factory
     */
    @Override
    public void run() {
        int fails = 0;
        //loop executes until thread will be terminated be executor
        for(;;){
            //do a random swap
            if (!randomSwap()){
                //swap failed to create a factory with better affinity
                ++fails;
                //when number of consecutive swaps reached n - add factory to GUI queue of
                //possibly best solutions, exchange some row with other thread, reset fails
                //and restart the loop
                if(fails == 100){
                    try {
                        gui.addFactoryToShow(new PrintFactory(spots, calculateAffinity(spots)));
                        int randRow = ThreadLocalRandom.current().nextInt(spots.length);
                        spots[randRow] = exchanger.exchange(spots[randRow]);
                        currentAffinity = calculateAffinity();
                        fails = 0;
                        continue;
                    } catch (InterruptedException e){
                        e.printStackTrace();
                    }

                    break;
                }
            } else {
                //random swap generated a factory with better affinity - reset fail count
                fails = 0;
            }
        }
    }


    /**
     * This method simply fills factory with set of stations
     * @param spots factory to fill
     * @return
     */
    private int[][] fillSpots(int[][] spots){

        int count = 0;
        for(int row = 0; row < spots.length; row ++){
            for(int spot = 0; spot < spots[row].length; spot++){
                spots[row][spot] = stations[count];
                ++count;
            }
        }
        return spots;
    }

    /**
     * Prints factory to console, only used for debugging
     */
    public synchronized void printFactory(){
        for(int row = 0; row < spots.length; row ++) {
            for (int spot = 0; spot < spots[row].length; spot++) {
                System.out.print(spots[row][spot] + " ");
            }
            System.out.println();
        }
    }

    //ignore empty spots
    public int calculateAffinity(){
        return calculateAffinity(spots);
    }

    /**
     * This method calculates the affinity of factory
     * Greater the difference between stations ids - better
     * Second step neighbors have half of influence
     * @param spots
     * @return
     */
    public static int calculateAffinity(int[][] spots){
        int totalDifference = 0;
        //iterate through the factory and add difference with neighbors
        for(int row = 0; row < spots.length; row ++) {
            for (int spot = 0; spot < spots[row].length; spot++) {
                int spotNum = spots[row][spot];
                if(spotNum != 0) {
                    if (spot - 1 >= 0) {
                            totalDifference += getDifference(spotNum, spots[row][spot - 1]);
                    }
                    if (spot + 1 < spots[row].length) {
                            totalDifference += getDifference(spotNum, spots[row][spot + 1]);
                    }
                    if (row - 1 >= 0) {
                            totalDifference += getDifference(spotNum, spots[row - 1][spot]);
                    }
                    if (row + 1 < spots.length) {
                            totalDifference += getDifference(spotNum, spots[row + 1][spot]);
                    }
                    if (spot - 2 >= 0) {
                            totalDifference += getDifference(spotNum, spots[row][spot - 2]) / 2;
                    }
                    if (spot + 2 < spots[row].length) {
                            totalDifference += getDifference(spotNum, spots[row][spot + 2]) / 2;
                    }
                    if (row - 2 >= 0) {
                            totalDifference += getDifference(spotNum, spots[row - 1][spot]) / 2;
                    }
                    if (row + 2 < spots.length) {
                            totalDifference += getDifference(spotNum, spots[row + 2][spot]) / 2;
                    }
                }

            }
        }
        return totalDifference;
    }

    /**
     * This method swaps two spots on factory (could be empty spots as well)
     * then if affinity of new factory is greater keeps changes
     * else reverses changes
     * Also there is some chance that swap will be made even if new affinity is lower
     * @return true if generated factory has better affinity
     */
    public boolean randomSwap(){
        //generate random coordinates to swap
        int row1 = getRandRow();
        int col1 = getRandCol();

        int row2 = getRandRow();
        int col2 = getRandCol();

        //swap two spots
        int temp = spots[row1][col1];
        spots[row1][col1] = spots[row2][col2];
        spots[row2][col2] = temp;

        //calculate new affinity
        int newAffinity = calculateAffinity();
        //if new affinity is greater keeps changes and save new affinity
        if(newAffinity > currentAffinity){
            currentAffinity = newAffinity;
            return true;
        } else {
            //with n% chance sill save a changes even if new affinity is lower
            //this step actually improves overall performance
            if(ThreadLocalRandom.current().nextInt(100) < 1){
                currentAffinity = newAffinity;
            }else {
                //reverse changes made to factory
                spots[row2][col2] = spots[row1][col1];
                spots[row1][col1] = temp;
            }
            return false;
        }


    }

    /**
     * Generate random row number
     * @return random row number
     */
    private int getRandRow(){
        return ThreadLocalRandom.current().nextInt(spots.length);
    }

    /**
     * Generate random column number
     * @return random column number
     */
    private int getRandCol(){
        return ThreadLocalRandom.current().nextInt(spots[0].length);
    }

    /**
     * This method calculates absolute difference between two spots id's
     * @param num1 first spot id
     * @param num2 second spot id
     * @return absolute difference
     */
    public static int getDifference(int num1, int num2){
        if(num1 == 0|| num2 == 0){
            return 0;
        }
        if(num1 > num2){
            return num1 - num2;
        } else if(num2 > num1){
            return num2 - num1;
        } else {
            return 0;
        }
    }


}







