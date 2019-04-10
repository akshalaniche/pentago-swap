package student_player;

import java.util.*;

import pentago_swap.PentagoMove;
import pentago_swap.PentagoBoardState;
import pentago_swap.PentagoCoord;
import pentago_swap.PentagoBoardState.Piece;
import pentago_swap.PentagoBoardState.Quadrant;

public class MyTools {
    public static double getSomething() {
        return Math.random();
    }
       
    /** Signals whether an alignment of 3 in the promising quadrant has been achieved */
    public static boolean tictactoe = false;
    
    /** Exploration hyper parameter for UCT bound */
    public final static double EXP_PARAM = 1.5;
    
    /** Score that is awarded to a winning board in MCTS */
	public final static int WIN_SCORE = 10;
	
	/** Malus for opponent marbles in a quadrant in the early phase (trying to achieve a 3LINE) */
	private static double OPP_COUNT = 0.25;

	/** Arrays to save function calls */
	private static PentagoCoord[] centers;
	private static Quadrant[] quads;
    
    /**
     * Generates 4 opening moves, with random quadrant swap.
     * The moves returned place a piece in the center of one of the four quadrants.
     * @param rand
     * @param playerID
     * @return
     */
    public static PentagoMove[] getOpening(Random rand, int playerID){
		tictactoe = false;
    	int[] centers = {1, 4};
    	quads = Quadrant.values();
    	PentagoMove[] openingMoves = new PentagoMove[4];
    	MyTools.centers = new PentagoCoord[4];
    	for (int i = 0; i < 4; i++){
    		int quadA = rand.nextInt(4);
    		int quadB = rand.nextInt(3);
    		if (quadB >= quadA) quadB++;
    		MyTools.centers[i] = new PentagoCoord(centers[i%2], centers[i/2]);
    		openingMoves[i] = new PentagoMove(centers[i%2], centers[i/2], quads[quadA], quads[quadB], playerID);
    	}
    	return openingMoves;
    }
        
    /**
     * For the early game, select the most promising quadrant in which my agent has occupied the center
     * In that quadrant, try to achieve a line of 3, using the center marble
     * Similar to using Minimax with alpha beta pruning on Tic Tac Toe
     * @param curr
     * @param player
     * @param turn
     * @param endTime
     * @param rand
     * @return
     */
    public static PentagoMove ticTacToe(PentagoBoardState curr, int player, int turn, long endTime, Random rand){
    	Piece playerColour = player == 0 ? Piece.WHITE : Piece.BLACK;
    	HashMap<PentagoCoord, Double> taken = new HashMap<PentagoCoord, Double>(4);
    	//Find the quadrants in which the center is occupied by my marble.
    	for (int i = 0; i < 4; i++){
    		if (curr.getPieceAt(centers[i]) == playerColour){
    			taken.put(centers[i], 0.0);
    		}
    	}

    	//count the pieces on each of the selected quadrants:
    	//my pieces count +1, the opponent's pieces count - OPP_COUNT
    	for (PentagoCoord center : taken.keySet()){
    		int count = 0;
    		int oppCount = 0;
    		int x = center.getX();
    		int y = center.getY();
    		for (int i = -1; i < 1; i++){
    			for (int j = -1; j < 1; j++){
    				Piece loc = curr.getPieceAt(x + i, y + j);
    				if (loc == playerColour){ count ++;}
    				else if (loc != Piece.EMPTY) {
    					oppCount ++;
    				}
    			}
    		}
    		taken.put(center, count - oppCount * MyTools.OPP_COUNT);
    	}
    	
    	//find the quadrant where you can align 3 the fastest 
    	//the quadrant with the most of my pieces and only a few opponent pieces
        List<Map.Entry<PentagoCoord, Double>> best = new ArrayList<Map.Entry<PentagoCoord, Double>>(taken.entrySet());
        Collections.sort(best, 
	        		new Comparator<Map.Entry<PentagoCoord, Double>>() { 
			            public int compare(Map.Entry<PentagoCoord, Double> o1,  
			                               Map.Entry<PentagoCoord, Double> o2) 
			            { 
			                return - (o1.getValue()).compareTo(o2.getValue()); 
			            } 
        }); 
        
        PentagoCoord bestCenter = best.get(0).getKey();
               
        //Do alpha beta pruning (minimax) to find the move that will lead to aligning 3 in the quadrant 
        TicTacToe root = new TicTacToe(curr, bestCenter, playerColour); //root of alpha beta pruned tree for tic tac toe
    	root.generateTree();
    	int bestScore = root.alphaBeta(-20, 20, endTime - 100);
    	TicTacToe next = root.getBestChild();
    	
    	if (next.win(playerColour)){
    		//Either we have completed a line of 3, or the best move will never lead to a line of 3
    		MyTools.tictactoe = true; 	
    	}
    	
    	//random quadrant swap 
    	int a = rand.nextInt(4);
    	int b = rand.nextInt(3);
    	if (a == b) b++;
    	
    	PentagoMove move = new PentagoMove(next.getX() -1 + bestCenter.getX(), next.getY() - 1 + bestCenter.getY(), 
    										quads[a], quads[b], player);
    	return move;
    }
    
    /**
     * Function that uses the Monte Carlo Tree Search to find the best move 
     * @param curr Current state of the board
     * @param player My player number
     * @param turn	Which turn it is
     * @param endTime	How much time is allowed for the loop
     * @param rand	Random number generator (for reproducibility)
     * @return
     */
    public static PentagoMove MCTS(PentagoBoardState curr, int player, int turn, long endTime, Random rand){

    	Node promisingNode, nodeToExplore;
    	
    	//Create the root of the MC tree, generate its children
    	Node root = new Node(curr);
    	root.expandChildren(player);
    	
    	//Loop for the MCTS algorithm (will run until we risk timing out)
    	while (System.currentTimeMillis() < endTime){
    		//Use the UCT (Upper Confidence Tree) bound to find the most promising leaf to expand
    		promisingNode = root.selectPromisingNode();
    		promisingNode.expandChildren(player);
    		
    		//Select a random move to explore from the promising node
    		nodeToExplore = promisingNode;
    		int size = nodeToExplore.getChildArray().size();
    		if (size > 0){
    			nodeToExplore = nodeToExplore.explore(rand.nextInt(size));
    		}

    		//Simulate a random game from that node and back propagate the result through the tree
    		int result = nodeToExplore.rollout(player);

    		nodeToExplore.backPropagation(result);
    	}
    	
    	//Find the best move so far
    	return root.getBestMove();
      }
}