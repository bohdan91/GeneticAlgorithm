/**
 * This is a data structure for sending factory array along with it's affinity
 * mostly made for convenience and copying array to avoid changing it's content by
 * some thread after it was send to GUI
 */
public class PrintFactory {

    private int[][] spots;
    private int score;

    /**
     *
     * @param spots contents of generated "factory"
     * @param score affinity of "factory"
     */
    public PrintFactory(int[][] spots, int score){

        this.spots = new int[spots.length][spots[0].length];
        //copying content of array to avoid changing content after being sent due to pass by reference
        for(int row = 0; row < spots.length; row ++) {
            for (int spot = 0; spot < spots[row].length; spot++) {
                this.spots[row][spot] = spots[row][spot];
            }
        }
        this.score = score;
    }

    public int[][] getSpots() {
        return spots;
    }

    public int getScore() {
        return score;
    }

}
