package assignment;

import ai.abstraction.*;
import ai.abstraction.pathfinding.PathFinding;
import ai.core.AIWithComputationBudget;
import rts.units.UnitTypeTable;

import ai.abstraction.pathfinding.PathFinding;
import ai.core.AI;
import ai.core.AIWithComputationBudget;
import ai.core.ParameterSpecification;
import rts.GameState;
import rts.PlayerAction;
import rts.units.Unit;
import rts.units.UnitTypeTable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class QMHassanPachecoAhmedWright extends AIWithComputationBudget {
    public UnitTypeTable utt;
    private PathFinding pathFinding;
    private int calls; //number of times the action was called
    private int playerID;

    //info needed
    private int resourcesAvailable;
    private int opponentsResources;
    private int noUnits;
    private int noWorkers;
    private int noSoldiers;
    private int noBases;
    private int enemyNoWorkers;
    private int enemyNoSoldiers;
    private int noNeutralUnits;
    private int enemySoldiersThreshold, soldiersThreshold,workerThreshold;
    private int timeThreshold;
    private boolean baseAtFullHealth;

    //state and map related
    int current;
    int mapWidth, mapHeight;
    boolean isMapBig;

    /**
     * Constructs the controller with the specified time and iterations budget
     *
     * @param timeBudget       time in milisseconds
     * @param iterationsBudget number of allowed iterations
     * @param utt Table that defines the unit types for this game.
     * @param pathFinding Instance for performing path planning queries.
     */
    public QMHassanPachecoAhmedWright(int timeBudget, int iterationsBudget, UnitTypeTable utt, PathFinding pathFinding) {
        super(timeBudget, iterationsBudget);
        this.pathFinding = pathFinding;
        this.utt = utt;
        this.calls = 0;

        this.playerID = 0; //revise later
        //fsm = new FSM();
        current = 0;

    }

    /**
     * It is updating all the info necessary for the transitions.
     * @param player
     * @param gs
     */
    private void updateInfo(int player, GameState gs){
        //gets the height and width of the map - used for the thresholds
        mapHeight = gs.getPhysicalGameState().getHeight();
        mapWidth = gs.getPhysicalGameState().getWidth();
        setThresholds();

        //Gets the resources available to us and opponent.
        //System.out.println("\nAt game tick [" + gs.getTime() + "]:");
        resourcesAvailable = gs.getPlayer(player).getResources();
        //System.out.println("Resources available for me: " + resourcesAvailable);
        opponentsResources = gs.getPlayer(1-player).getResources();
        //System.out.println("Resources available for opponent: " + opponentsResources);

        //our units
        noUnits = 0;
        noSoldiers = 0;
        noWorkers = 0;
        noBases = 0;

        //enemy units
        enemyNoSoldiers = 0;
        enemyNoWorkers = 0;

        //neutral
        noNeutralUnits = 0;
        HashSet<String> neutralTypes = new HashSet<>();

        //iterate through all units in the games
        for (Unit u : gs.getUnits()) {

            //if belonging to us
            if (u.getPlayer() == player) {
                noUnits ++;

                //count workers and check what they're doing
                if (u.getType().name.equals("Worker")) {
                    //System.out.println("Worker available actions: " + u.getUnitActions(gs));
                    noWorkers++;
                    //if worker doing nothing
                    if (gs.getActionAssignment(u) == null) {
                        //System.out.println("Worker at (" + u.getX() + "," + u.getY() + ") with ID " + u.getID() + " has no assignment");
                    }
                //count soldiers
                }else if (u.getType().name.equals("Light") || u.getType().name.equals("Heavy")
                        || u.getType().name.equals("Ranged")){
                    noSoldiers++;
                //if it is a base then count it and check its stats
                } else if (u.getType().name.equals("Base")){
                    noBases++;
                    if (u.getHitPoints()==u.getMaxHitPoints())
                        baseAtFullHealth = true;
                    else
                        baseAtFullHealth=false;
                }

            //if belonging to enemy
            } else if (u.getPlayer() == 1 - player) {
                //count enemy workers
                if (u.getType().name.equals("Worker"))
                    enemyNoWorkers++;
                //count enemy soldiers
                else if (u.getType().name.equals("Light") || u.getType().name.equals("Heavy")
                    || u.getType().name.equals("Ranged"))
                    enemyNoSoldiers++;

            //if belonging to none
            } else {
                noNeutralUnits ++;
                neutralTypes.add(u.getType().name);
            }
        }
//        System.out.println("There are " + noWorkers + " workers (ours); " + noSoldiers +
//                " soldiers belonging to us " + enemyNoSoldiers + " soldiers (them) " +
//                enemyNoWorkers + "workers (them).");


        //System.out.println("Neutral unit types: " + neutralTypes);
    }

    public PlayerAction getAction(int player, GameState gs) throws Exception {
        calls ++;
        playerID = player;
        //System.out.println("PlayerID: " + playerID + " Actions Called: " + calls);
        PlayerAction actionObj = new PlayerAction();

        //if there are actions to execute
        if(gs.canExecuteAnyAction(player)) {
            //update all the info
            updateInfo(player,gs);

            //get the next state with that info
            int state = getState(gs.getTime());

            //apply this state
            switch (state) {
                case 0:
                    actionObj = new EconomyRushModified(utt).getAction(player, gs);
                    //System.out.println(current);
                    break;
                case 1:
                    actionObj = new SoldierRush(utt).getAction(player, gs);
                    //System.out.println(current);
                    break;
                case 2:
                    actionObj = new SoldierDefense(utt).getAction(player, gs);
                    //System.out.println(current);
                    break;
                case 3:
                    actionObj = new SuicideSquad(utt).getAction(player, gs);
                    //System.out.println(current);
                    break;
            }
        }


        //System.out.println(current);
        return actionObj;
    }

    /**
     * Setting the thresholds for the state condition changers.
     */
    private void setThresholds(){
        //set if map is big or not
        if(mapHeight*mapWidth<=16*16)
            isMapBig = false;
        else
            isMapBig = true;

        //if the map is small
        if (!isMapBig){
            workerThreshold = 3;
            enemySoldiersThreshold = 2;
            soldiersThreshold = 5;
            timeThreshold = 1000;
        }
        else{ //if it is big
            workerThreshold = 5;
            enemySoldiersThreshold = 4;
            soldiersThreshold = 8;
            timeThreshold = 2000;
        }

    }

    /**
     * Changes the sate taking into account the current parameters and
     * thresholds
     * @return the state
     */
    private int getState(int currentTime){

        //if we do not have bases then sends everything to attack
        if (noBases == 0){
            current = 4;
        }
        else{
            switch (current) {
                case 0:
                    //when enemies have more soldiers or more than the threshold, we attack more
                    if (enemyNoSoldiers>noSoldiers ||
                            enemyNoSoldiers >= enemySoldiersThreshold) {
                        current = 1;
                    }
                    //but if we have not been attacked for a long time and we have more soldiers, create a defense
                    else if (currentTime>timeThreshold & baseAtFullHealth)
                        current = 2;
                        break;
                case 1:
                    //if we have a small amount of workers they must have a priority
                    if (noWorkers<workerThreshold)
                        current = 0;
                    else if (currentTime>timeThreshold & baseAtFullHealth) //same as above
                        current = 2;
                        break;
                case 2:
                    //if we have a good amount of soldiers then attack will all of them
                    if (noSoldiers>=soldiersThreshold){
                        current = 1;
                    }
                    break;
            }
        }
        return current;
    }


    @Override
    public void reset() {
        this.calls = 0;
    }


    @Override
    public AI clone() {
        QMHassanPachecoAhmedWright myClone = new QMHassanPachecoAhmedWright(TIME_BUDGET, ITERATIONS_BUDGET, utt, pathFinding);
        myClone.calls = calls;
        myClone.playerID = playerID;
        myClone.current = current;
        //myClone.fsm.current = fsm.current;
        return myClone;
    }

    @Override
    public List<ParameterSpecification> getParameters() {
        return new ArrayList<>();
    }

    @Override
    public void preGameAnalysis(GameState gs, long milliseconds, String readWriteFolder) throws Exception {
    }

}

/**
 * The "wrapper" class - Finite State Machine - not in use for now
 */
class FSM {

    // The transitions table
    private int[][] transition = {{0}, {0}, {0}};
    // The current state
    int current = 0;

    private void next(int msg) {
        current = transition[current][msg];
    }


}

