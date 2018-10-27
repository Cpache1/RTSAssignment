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
    private UnitTypeTable utt;
    private PathFinding pathFinding;
    private int calls; //number of times the action was called
    private int playerID;

    //info needed
    private int resourcesAvailable;
    private int opponentsResources;
    private int noUnits;
    private int noWorkers;
    private int noSoldiers;
    private int noLight;
    private int noHeavy;
    private int noRanged;
    private int enemyNoWorkers;
    private int enemyNoSoldiers;
    private int noNeutralUnits;

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
    }

    /**
     * It is updating all the info necessary for the transitions.
     * @param player
     * @param gs
     */
    private void updateInfo(int player, GameState gs){
        //System.out.println("\nAt game tick [" + gs.getTime() + "]:");
        resourcesAvailable = gs.getPlayer(player).getResources();
        //System.out.println("Resources available for me: " + resourcesAvailable);
        opponentsResources = gs.getPlayer(1-player).getResources();
        //System.out.println("Resources available for opponent: " + opponentsResources);

        //our units
        noUnits = 0;
        noSoldiers = 0;
        noWorkers = 0;

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
                        System.out.println("Worker at (" + u.getX() + "," + u.getY() + ") with ID " + u.getID() + " has no assignment");
                    }
                //count soldiers
                }else if (u.getType().name.equals("Light") || u.getType().name.equals("Heavy")
                        || u.getType().name.equals("Ranged")){
                    noSoldiers++;
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
        System.out.println("There are " + noWorkers + " workers (ours); " + noSoldiers +
                " soldiers belonging to us " + enemyNoSoldiers + " soldiers (them) " +
                enemyNoWorkers + "workers (them).");


        //System.out.println("Neutral unit types: " + neutralTypes);
    }

    public PlayerAction getAction(int player, GameState gs) throws Exception {
        calls ++;
        playerID = player;
        //System.out.println("PlayerID: " + playerID + " Actions Called: " + calls);
        PlayerAction actionObj = new PlayerAction();

        if(gs.canExecuteAnyAction(player)) {
            updateInfo(player,gs);

            if (calls <= 400) {
                actionObj = new EconomyRushModified(utt).getAction(player, gs);
            }else{
                actionObj = new EconomyRushModified(utt).getAction(player, gs);
            }
        }

        return actionObj;
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


