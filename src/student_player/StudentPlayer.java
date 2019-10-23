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
        //Allows to limit the time spent in loops when searching for the best move
    	long startTime = System.currentTimeMillis();
    	long endTime = startTime + 1800 ;

        int turn = boardState.getTurnNumber();
        int player = boardState.getTurnPlayer();
        
        //First turn, initialize the static variables
        if (turn == 0){
        	rand = new Random();
        	this.openingMoves = MyTools.getOpening(rand, player);
        }

        // For the first three turns, adopt an offensive strategy that aims to occupy the centers of the quadrants if available
        if (turn < 3) {
        	int i = 0;
        	while (i < 4) {
        		PentagoMove move = this.openingMoves[i];
        		if (boardState.isLegal(move)){
        			return move;
        		}
        		i++;
        	}
        }
        
        //For the first ten moves (excluding moves where the agent aims for the quadrant centers, for 2 or 3 turns)
        //aim to align 3 marbles inside a quadrant using the centers that are occupied by my marble
        //Once that is achieved, abandon this strategy
        //Offensive strategy
       /* if (turn < 10 && !MyTools.tictactoe) {
        	PentagoMove move = MyTools.ticTacToe(boardState, player, turn, endTime, rand);
        	return move;
        }*/
        
        //For the rest of the game, adopt a neutral strategy that depends on Monte Carlo Tree Search to find the best move
        //The scoring adapts for making this strategy both offensive and defensive
        Move myMove = MyTools.MCTS((PentagoBoardState) boardState.clone(), 
        							player, turn, endTime, rand) ;
        
        // Return your move to be processed by the server.
        return myMove;
    }
}