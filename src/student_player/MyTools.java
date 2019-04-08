package student_player;

import java.util.*;

import pentago_swap.PentagoMove;
import pentago_swap.PentagoBoardState;
import pentago_swap.PentagoBoardState.Quadrant;

public class MyTools {
    public static double getSomething() {
        return Math.random();
    }
        
    private static int MAX = 100;
    private static int TURN = 10;
    public final static double EXP_PARAM = 1.5;
	public final static int WIN_SCORE = 10000;

    
    /**
     * Generates 4 opening moves, with random quadrant swap.
     * The moves returned place a piece in the center of one of the four quadrants.
     * @param rand
     * @param playerID
     * @return
     */
    public static PentagoMove[] getOpening(Random rand, int playerID){
    	int[] centers = {1, 4};
    	Quadrant[] quads = Quadrant.values();
    	PentagoMove[] openingMoves = new PentagoMove[4];
    	for (int i = 0; i < 4; i++){
    		int quadA = rand.nextInt(4);
    		int quadB = rand.nextInt(3);
    		if (quadB >= quadA) quadB++;
    		openingMoves[i] = new PentagoMove(centers[i%2], centers[i/2], quads[quadA], quads[quadB], playerID);
    		System.out.println(openingMoves[i].toPrettyString());
    	}
    	return openingMoves;
    }
    
    
    public static PentagoMove MCTS(PentagoBoardState curr, int player, int turn, long endTime, Random rand){

    	Node promisingNode, nodeToExplore;
    	int opp = 1 - player;
    	int max = Integer.MAX_VALUE;
    	if (turn < TURN){
    		max = MyTools.MAX;
    	}
    	
    	Node root = new Node(curr);
    	root.expandChildren(max, rand);
    	
    	while (System.currentTimeMillis() < endTime){
    		promisingNode = root.selectPromisingNode();
    		promisingNode.expandChildren(max, rand);
    		nodeToExplore = promisingNode;
    		int size = nodeToExplore.getChildArray().size();
    		if (size > 0){
    			nodeToExplore = nodeToExplore.getChildArray().get(rand.nextInt(size));
    		}
    		int result = nodeToExplore.rollout(player);
    		nodeToExplore.backPropagation(result);
    	}
    	
    	return root.getBestMove();
      }
}