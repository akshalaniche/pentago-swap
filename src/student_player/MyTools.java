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
       
    public static boolean tictactoe = false;
    public final static double EXP_PARAM = 1.5;
	public final static int WIN_SCORE = 10;
	private static PentagoCoord[] centers;
	private static double OPP_COUNT = 0.25;
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
        
    public static PentagoMove ticTacToe(PentagoBoardState curr, int player, int turn, Random rand){
    	Piece playerColour = player == 0 ? Piece.WHITE : Piece.BLACK;
    	HashMap<PentagoCoord, Double> taken = new HashMap<PentagoCoord, Double>(4);
    	//centers of the quadrants that are my colour
    	for (int i = 0; i < 4; i++){
    		if (curr.getPieceAt(centers[i]) == playerColour){
    			taken.put(centers[i], 0.0);
    		}
    	}


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
    	
    	//find the quadrant where you will align 3 the fastest 
    	//the quadrant with the most of my pieces and fewer opponent pieces
        List<Map.Entry<PentagoCoord, Double>> best = new ArrayList<Map.Entry<PentagoCoord, Double>>(taken.entrySet());
        Collections.sort(best, 
	        		new Comparator<Map.Entry<PentagoCoord, Double> >() { 
			            public int compare(Map.Entry<PentagoCoord, Double> o1,  
			                               Map.Entry<PentagoCoord, Double> o2) 
			            { 
			                return - (o1.getValue()).compareTo(o2.getValue()); 
			            } 
        }); 
        
        PentagoCoord bestCenter = best.get(0).getKey();
                
        TicTacToe root = new TicTacToe(curr, bestCenter, playerColour); //root of alpha beta pruned tree for tic tac toe
    	root.generateTree();
    	int bestScore = root.alphaBeta(-20, 20);
    	TicTacToe next = root.getBestChild();
    	
    	if (next.win(playerColour)){
    		//Either we have completed a line of 3, or the best move will never lead to a line of 3
    		MyTools.tictactoe = true; 	
    	}
    	
    	int a = rand.nextInt(4);
    	int b = rand.nextInt(3);
    	if (a == b) b++;
    	
    	PentagoMove move = new PentagoMove(next.getX() -1 + bestCenter.getX(), next.getY() - 1 + bestCenter.getY(), 
    										quads[a], quads[b], player);
    	return move;
    }
    
    public static PentagoMove MCTS(PentagoBoardState curr, int player, int turn, long endTime, Random rand){

    	Node promisingNode, nodeToExplore;
    	
    	Node root = new Node(curr);
    	root.expandChildren(rand);
    	while (System.currentTimeMillis() < endTime){
    		promisingNode = root.selectPromisingNode();

    		promisingNode.expandChildren(rand);

    		nodeToExplore = promisingNode;
    		int size = nodeToExplore.getChildArray().size();
    		if (size > 0){
    			nodeToExplore = nodeToExplore.explore(rand.nextInt(size));
    		}

    		int result = nodeToExplore.rollout(player);

    		nodeToExplore.backPropagation(result);
    	}
    	
    	return root.getBestMove();
      }
}