package student_player;

import java.util.*;

import boardgame.Board;
import pentago_swap.PentagoBoardState;
import pentago_swap.PentagoMove;
import student_player.MyTools;

/**
 * Class for implementing the Monte Carlo Tree Search 
 * @author Akshal Aniche
 *
 */
public class Node{
		/** State of the board at the node */
    	private PentagoBoardState state;
    	/** Parent of the node, null if root */
    	private Node parent;
    	/** List of successor states accessible with a move from current state */
    	private List<Node> children;
    	
    	/** List of children that are selected at least once during the loop 
    	 *  when trying to find the most promising leaf. 
    	 *  Those are the nodes that will potentially be the best move. */
    	private List<Node> explored;
    	private PentagoMove move; //move to get to this node from the parent
    	private int visit; //number of times this node had been visited.
    	private int score; //score of the node
    	    	
    	/** Constructor for root node */
    	public Node(PentagoBoardState state){
    		this.state = state;
    		parent = null;
    		children = new ArrayList<Node>();
    		explored = new ArrayList<Node>();
    		move = null;
    		visit = 0;
    		score = 0;
    	}
    	
    	/** Constructor for child node */
    	public Node(PentagoBoardState state, Node parent, PentagoMove move){
    		this.state = state;
    		this.parent = parent;
    		children = new ArrayList<Node>();
    		explored = new ArrayList<Node>();
    		this.move = move;
    		visit = 0;
    		score = 0;
    	}
    	
    	/**
    	 * Explore child i (if child i is selected randomly) 
    	 * Side effects: Adds child i to explored
    	 * @param i
    	 * @return
    	 */
    	public Node explore(int i){
    		Node toExp =  this.children.get(i);
    		this.explored.add(toExp);
    		return toExp;
    	}
    
    	/**
    	 * Adds node to explored
    	 * Doesn't check if node is in children (only use is in this class)
    	 * @param node
    	 */
    	private void explore(Node node){
    		this.explored.add(node);
    	}
    	
    	/*
    	 * Getters 
    	 */
    	
    	public List<Node> getChildArray(){
    		return this.children;
    	}
    	
    	public int getScore(){
    		return score;
    	}
    	
    	public int getVisit(){
    		return visit;
    	}
    	
    	public PentagoMove getMove(){
    		return move;
    	}
    	
    	/**
    	 * Generates all of the successors of this 
    	 * Simulates one rollout from each successor and backpropagates the result
    	 * @param player
    	 */
    	public void expandChildren(int player){
    		if (state.gameOver()) return;
    		
    		ArrayList<PentagoMove> moves = this.state.getAllLegalMoves();
    		Iterator<PentagoMove> iter = moves.iterator();
    		
			PentagoMove move;
			Node child;
			PentagoBoardState nextState;
			
    		while (iter.hasNext()){
    			move = iter.next();
    			nextState = (PentagoBoardState) state.clone();
    			nextState.processMove(move);
    			child = new Node(nextState, this, move);
    			this.children.add(child);
    			int result = child.rollout(player);
    			child.backPropagation(result);
    		}
    	}
    	
    	/**
    	 * Finds the leaf that is the most promising to explore according to the UCT bound
    	 * @return leaf
    	 */
        public Node selectPromisingNode() {
        	Node node = this;
        	Node temp;
        	while (node.getChildArray().size() != 0) {
        		temp = node.findBestChildNodeWithUCT();
        		node.explore(temp);
        		node = temp;
        	}
        	return node;
        }
        
        /**
         * Finds the child that has the highest UCT bound
         * @return
         */
        public Node findBestChildNodeWithUCT(){
        	int parentVisit = this.visit;
        	return Collections.max(this.getChildArray(),
        			Comparator.comparing(c -> uctScore(parentVisit, 
        					c.getScore(), c.getVisit())));
        }
    	
        /**
         * Implements the simulation phase of MCTS
         * @param player
         * @return
         */
    	public int rollout(int player){
    		PentagoBoardState rolloutState = (PentagoBoardState) this.state.clone();
    		while (!rolloutState.gameOver()){
    			rolloutState.processMove((PentagoMove) rolloutState.getRandomMove());
    		}
    		return scoreBoard(player, rolloutState);
    	}
    	
    	/**
    	 * Backprogagation phase of the MCTS
    	 * @param score to propagate
    	 */
    	public void backPropagation(int score){
    		this.visit = this.visit + 1;
    		this.score = this.score + score;
    		if (this.parent != null){
    			this.parent.backPropagation(score);
    		}
    	}
    	
    	/**
    	 * Returns the move that generates the successor of this node with the highest win rate after MCTS loop
    	 * @return
    	 */
    	public PentagoMove getBestMove(){
    		Node bestChild = Collections.max(this.explored,
        			Comparator.comparing(c -> 
        					(double) c.getScore() / (double) c.getVisit()));
    		return bestChild.getMove();
    	}
    	
    	/**
    	 * Scores a draw board
    	 * TODO: implement an evaluation function that considers the different alignments
    	 * @param player
    	 * @param state
    	 * @return
    	 */
    	private static int scoreDraw(int player, PentagoBoardState state){
    		return MyTools.WIN_SCORE/2;
    	}
    	
    	/**
    	 * Scores a finished board (win, lose, draw)
    	 * @param player
    	 * @param state
    	 * @return
    	 */
    	private static int scoreBoard(int player, PentagoBoardState state){
    		if (state.gameOver()){
    			int winner = state.getWinner();
    			if (winner == Board.DRAW){
    				return scoreDraw(player, state);
    			}
    			else if (winner == player){
    				return MyTools.WIN_SCORE;
    			}
    			else {
    				return 0; //-MyTools.WIN_SCORE;
    			}
    		}
    		else return scoreDraw(player, state);
    	}

    	
    	/**
    	 * Computes the UCT bound given the score, the number of times a node was visited 
    	 * and the number of times its parent was visited.
    	 * Static
    	 * @param parentVisit
    	 * @param score
    	 * @param visit
    	 * @return
    	 */
        private static double uctScore (int parentVisit, int score, int visit){
        	double winRate = (double) score / (double) visit;
        	double exp = Math.log((double) parentVisit) / (double) visit;
        	exp = MyTools.EXP_PARAM * Math.sqrt(exp);
        	return winRate + exp;
        }
        
    }
