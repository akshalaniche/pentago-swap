package student_player;

import java.util.*;

import boardgame.Board;
import pentago_swap.PentagoBoardState;
import pentago_swap.PentagoMove;
import student_player.MyTools;

public class Node{
    	private PentagoBoardState state;
    	private Node parent;
    	private List<Node> children;
    	private List<Node> explored;
    	private PentagoMove move; //move to get to this node from the parent
    	private int visit; //number of times this node had been visited.
    	private int score;
    	    	
    	public Node(PentagoBoardState state){
    		this.state = state;
    		parent = null;
    		children = new ArrayList<Node>();
    		explored = new ArrayList<Node>();
    		move = null;
    		visit = 0;
    		score = 0;
    	}
    	
    	public Node(PentagoBoardState state, Node parent, PentagoMove move){
    		this.state = state;
    		this.parent = parent;
    		children = new ArrayList<Node>();
    		explored = new ArrayList<Node>();
    		this.move = move;
    		visit = 0;
    		score = 0;
    	}
    	
    	public Node explore(int i){
    		Node toExp =  this.children.get(i);
    		this.explored.add(toExp);
    		return toExp;
    	}
    	
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
    	
    	public void expandChildren(int max, Random rand){
    		if (state.gameOver()) return;
    		
    		ArrayList<PentagoMove> moves = this.state.getAllLegalMoves();
    		Collections.shuffle(moves, rand); 	//randomizes the iteration
    		Iterator<PentagoMove> iter = moves.iterator();
    		
			PentagoMove move;
			Node child;
			PentagoBoardState nextState;
			
			int counter = 0;
    		while (iter.hasNext() && counter < max){
    			move = iter.next();
    			nextState = (PentagoBoardState) state.clone();
    			nextState.processMove(move);
    			child = new Node(nextState, this, move);
    			this.children.add(child);
    			counter ++;
    		}
    	}
    	
        private static double uctScore (int parentVisit, int score, int visit){
        	double winRate = (double) score / (double) visit;
        	double exp = Math.log((double) parentVisit) / (double) visit;
        	exp = MyTools.EXP_PARAM * Math.sqrt(exp);
        	return winRate + exp;
        }
        
        public Node selectPromisingNode() {
        	Node node = this;
        	while (node.getChildArray().size() != 0) {
        		node = node.findBestChildNodeWithUCT();
        	}
        	return node;
        }
        
        public Node findBestChildNodeWithUCT(){
        	int parentVisit = this.getVisit();
        	return Collections.max(this.getChildArray(),
        			Comparator.comparing(c -> uctScore(parentVisit, 
        					c.getScore(), c.getVisit() + 1)));
        }
    	
    	private static int scoreDraw(int player, PentagoBoardState state){
    		return 0;
    	}
    	
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
    				return -MyTools.WIN_SCORE;
    			}
    		}
    		else return scoreDraw(player, state);
    	}
    	
    	public int rollout(int player){
    		PentagoBoardState rolloutState = (PentagoBoardState) this.state.clone();
    		while (!rolloutState.gameOver()){
    			rolloutState.processMove((PentagoMove) rolloutState.getRandomMove());
    		}
    		return scoreBoard(player, rolloutState);
    	}
    	
    	public void backPropagation(int score){
    		this.visit = this.visit + 1;
    		this.score = this.score + score;
    		if (this.parent != null){
    			this.parent.backPropagation(score);
    		}
    	}
    	
    	public PentagoMove getBestMove(){
    		Node bestChild = Collections.max(this.explored,
        			Comparator.comparing(c -> 
        					(double) c.getScore() / (double) c.getVisit()));
    		return bestChild.getMove();
    	}
    }
