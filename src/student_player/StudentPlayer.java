package student_player;

import java.util.Random;

import boardgame.Move;

import pentago_swap.PentagoPlayer;
import pentago_swap.PentagoBoardState;
import pentago_swap.PentagoMove;

/** A player file submitted by a student. */
public class StudentPlayer extends PentagoPlayer {

    /**
     * You must modify this constructor to return your student number. This is
     * important, because this is what the code that runs the competition uses to
     * associate you with your agent. The constructor should do nothing else.
     */
    public StudentPlayer() {
        super("260726335");
    }

    private static Random rand;
    private PentagoMove[] openingMoves;
    /**
     * This is the primary method that you need to implement. The ``boardState``
     * object contains the current state of the game, which your agent must use to
     * make decisions.
     */
    public Move chooseMove(PentagoBoardState boardState) {
        // You probably will make separate functions in MyTools.
        // For example, maybe you'll need to load some pre-processed best opening
        // strategies...
    	long startTime = System.currentTimeMillis();
    	long endTime = startTime + 1800 ;

        int turn = boardState.getTurnNumber();
        int player = boardState.getTurnPlayer();
        
        if (turn == 0){
        	rand = new Random(2019);
        	this.openingMoves = MyTools.getOpening(rand, player);
        }
        if (turn < 4) {
        	int i = 0;
        	while (i < 4) {
        		PentagoMove move = this.openingMoves[i];
        		if (boardState.isLegal(move)){
        			return move;
        		}
        		i++;
        	}
        }
        
        Move myMove = MyTools.MCTS((PentagoBoardState) boardState.clone(), 
        							player, turn, endTime, rand) ;
        
        // Return your move to be processed by the server.
        return myMove;
    }
}