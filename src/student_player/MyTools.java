package student_player;

import java.util.Random;

import pentago_swap.PentagoMove;
import pentago_swap.PentagoBoardState.Quadrant;

public class MyTools {
    public static double getSomething() {
        return Math.random();
    }
    
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
    		//System.out.println(openingMoves[i].toPrettyString());
    	}
    	
    	return openingMoves;
    }
}