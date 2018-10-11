import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.Timer;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;


public class GUI {
    private JFrame mainFrame;
    private JPanel controlPanel;
    private JPanel spotPanel;
    private JLabel scoreLabel;
    private PrintFactory[] factories;
    //executor reference for ability to stop all running threads with a "Stop" button
    private ExecutorService executor;
    //lock is used to lock gui thread when new factory is being added or one of current factories
    //loads to view
    private final Lock lock = new ReentrantLock();
    private PrintFactory bestFactory;


    public GUI(int width, int height, ExecutorService executor){
        factories = new PrintFactory[10];
        this.executor = executor;
        prepareGUI(width, height);
        this.showGridLayout();
        startTimer();
    }

    private void prepareGUI(int width, int height){
        mainFrame = new JFrame("Genetic Algorithm Example");
        mainFrame.setSize(width * 40,height * 50);

        mainFrame.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent windowEvent){
                System.exit(0);
            }
        });
        controlPanel = new JPanel();
        controlPanel.setLayout(new FlowLayout());

        spotPanel = new JPanel();
        spotPanel.setLayout(new GridLayout(height,width,-1,-1));
        spotPanel.setBorder(BorderFactory.createEmptyBorder(2,2,2,2));

        for(int i = 0; i < 32; i++){
            JLabel spot = new JLabel(" ");
            spot.setHorizontalAlignment(JLabel.CENTER);
            spot.setBorder(BorderFactory.createLineBorder(Color.BLACK));
            spotPanel.add(spot);
        }


        mainFrame.add(spotPanel, BorderLayout.CENTER);
        mainFrame.add(controlPanel, BorderLayout.SOUTH);
        mainFrame.setVisible(true);
    }
    private void showGridLayout(){

        JPanel panel = new JPanel();
        //panel.setSize(300,300);
        GridLayout layout = new GridLayout(0,3);
        layout.setHgap(10);
        layout.setVgap(10);

        panel.setLayout(layout);
        scoreLabel = new JLabel("Score: ");
        panel.add(scoreLabel);

        JButton stopBtn = new JButton("Stop");
        stopBtn.addActionListener(e -> shutDown());
        panel.add(stopBtn);

        controlPanel.add(panel);

        mainFrame.setVisible(true);
    }

    /**
     * This method is used to find and display the best factory from array of factories
     */
    public void showBestFactory(){
        //find best factory
        lock.lock();
        try {
            //find best station of all
            if(factories[0] != null) {

                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        int bestScore = factories[0].getScore();
                        int bestId = 0;
                        for (int i = 0; i < factories.length; i++) {
                            if(factories[i]!=null) {
                                int currentScore = factories[i].getScore();
                                if (currentScore > bestScore) {
                                    bestId = i;
                                    bestScore = currentScore;
                                }
                            }
                        }
                        bestFactory = factories[bestId];
                        int[][] spots = factories[bestId].getSpots();

                        spotPanel.removeAll();
                        //place all stations
                        for (int row = 0; row < spots.length; row++) {
                            for (int spot = 0; spot < spots[row].length; spot++) {
                                JLabel point = new JLabel();
                                point.setText(Integer.toString(spots[row][spot]));
                                point.setHorizontalAlignment(JLabel.CENTER);
                                point.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
                                Font boldFont = new Font("System", Font.BOLD, 16);
                                point.setFont(boldFont);
                                spotPanel.add(point);
                            }
                        }
                        scoreLabel.setText("Score: " + factories[bestId].getScore());
                        spotPanel.updateUI();
                        controlPanel.updateUI();
                        mainFrame.repaint();
                        // SwingUtilities.invokeLater(()-> mainFrame.repaint());
                    }
                });

            }
        } catch (Exception e){
            e.printStackTrace();
        }
        finally {
            lock.unlock();
        }


    }

    /**
     * This method is used to add possibly a best solution from some thread
     * to ad array of factories if it's affinity is better than one of existing
     * factories
     * @param factory
     */
    public void addFactoryToShow(PrintFactory factory){
        lock.lock();
        try{

            for(int i = 0; i < factories.length; i ++){
                if(factories[i] == null) {
                    factories[i] = factory;
                    break;
                }else if (factories[i].getScore() == factory.getScore()){
                    factories[i] = factory;
                    break;
                } else if (factories[i].getScore() < factory.getScore()){
                    factories[i] = factory;
                    break;
                }
            }


        } finally {
            lock.unlock();
        }

    }

    /**
     * Timer with interval of 1 second to display currently the best solution
     */
    private void startTimer(){
        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                showBestFactory();
            }
        }, 1000,1000);
    }

    /**
     * This method stops all running threads
     */
    private void shutDown(){
        executor.shutdownNow();
        for(PrintFactory factory: factories){
            System.out.println(factory.getScore());
        }
        printFactories();
    }

    /**
     * This method is used to display all factories in array of best factories
     * used only for debugging purposes
     */
    public synchronized void printFactories(){
        for(int i = 0; i < factories.length; i++) {
            if (factories[i] !=null){
                int[][] spots = factories[i].getSpots();
                System.out.println("ID: " + i + "  Score: " + factories[i].getScore());
                for (int row = 0; row < spots.length; row++) {
                    for (int spot = 0; spot < spots[row].length; spot++) {
                        System.out.print(spots[row][spot] + " ");
                    }
                    System.out.println();
                }
            }
        }
    }
}
















