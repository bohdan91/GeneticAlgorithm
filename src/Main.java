import java.util.concurrent.Exchanger;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadLocalRandom;

public class Main {

    /**
     * initial setup and parameters
     * @param args
     */
    public static void main(String[] args) {

        int width = 8; //width of factory
        int height = 4; //height of factory
        int holeProbability = 20;   //probability that hole i.e. empty spot (0) will be generated
        int numberOfThreads = 32;   //number of parallel threads generated
        //executor is used to manage start and stop of threads
        ExecutorService executor = Executors.newFixedThreadPool(numberOfThreads);
        GUI gui = new GUI(width, height, executor);
        //exchanger will be used to exchange parts of solutions between threads
        Exchanger<int[]> exchanger = new Exchanger();
        //create initial stations
        int[] stations = new int[width * height];
        for (int i = 0; i < stations.length; i++){
            //with predefined probability create a hole
            if (ThreadLocalRandom.current().nextInt(100) < holeProbability){
                stations[i] = 0;
            } else {
                //create a random station number between 1 and 9
                stations[i] = ThreadLocalRandom.current().nextInt(9) + 1;
            }
        }

        //start specified number of threads
        for(int i = 0; i < numberOfThreads; i++){
            executor.execute(new Factory(width,height, stations, exchanger, gui));
        }



    }
}
