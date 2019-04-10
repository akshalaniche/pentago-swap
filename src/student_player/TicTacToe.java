package student_player;

import java.util.*;

import pentago_swap.PentagoBoardState;
import pentago_swap.PentagoBoardState.Piece;
import pentago_swap.PentagoCoord;

public class TicTacToe {
	private Piece[][] board;
	private ArrayList<TicTacToe> children;
	private PentagoCoord move;		//Coordinate where the last player put a piece
	private Piece playerColour; 	//player whose turn it is going to be now 
	private int score;				// score of the node in minimax tree
	private boolean player;			//is the node corresponding to the student player?
	
	/**
	 * creates a root node
	 * @param curr
	 * @param center
	 * @param colour
	 */
	public TicTacToe(PentagoBoardState curr, PentagoCoord center, Piece colour){
		board = new Piece[3][3];
		for (int i = 0; i < 3; i++){
			for (int j = 0; j < 3; j++){
				board[i][j] = curr.getPieceAt(center.getX() + i -1, center.getY() + j - 1);
			}
		}
		children = new ArrayList<TicTacToe>();
		move = null; 
		score = 0;
		player = true;
		playerColour = colour; 
	}
	
	/**
	 * Creates a child node from a parent, with a marble added in the board
	 * assumes that the person whose turn it is supposed to be is playing
	 * @throws IllegalArgumentException if the location is occupied
	 * @param parent
	 * @param coord
	 * @param player
	 */
	private TicTacToe(TicTacToe parent, PentagoCoord coord, Piece player) {
		Piece col = player == Piece.WHITE ? Piece.WHITE : Piece.BLACK;	 //move just played
		Piece colour = player == Piece.BLACK ? Piece.WHITE : Piece.BLACK;
		board = new Piece[3][3];
		for (int i = 0; i < 3; i++){
			for (int j = 0; j < 3; j++){
				board[i][j] = parent.board[i][j];
			}
		}

		if (board[coord.getX()][coord.getY()] != Piece.EMPTY){
			throw new IllegalArgumentException ("Occupied move");
		}
		
		board[coord.getX()][coord.getY()] = col;
		this.move = coord;
		this.playerColour = colour;
		this.player = !parent.player;
		children = new ArrayList<TicTacToe>();
		score = 0;
	}
	
	/*
	 * Getters for the location of the last marble added 
	 * Throws NullPointerException if used on root 
	 */
	
	public int getX(){
		return this.move.getX();
	}
	
	public int getY(){
		return this.move.getY();
	}
	
	/**
	 * Generates all children of a node 
	 * Shuffles them for the purpose of the alpha-beta pruning
	 */
	private void children(){
		for (int i = 0; i < 3; i++){
			for (int j = 0; j < 3; j++){
				try {
					TicTacToe child = new TicTacToe (this, new PentagoCoord(i, j), this.playerColour);
					this.children.add(child);
				}
				catch (IllegalArgumentException e){
					//Ignore the child if it's not a valid move (location occupied)
				}
			}
		}
		Collections.shuffle(children);
	}
	
	/**
	 * Generate the subtree starting at this node
	 * Doesn't generate a subtree rooted at a child if the child is a winning or losing board.
	 */
	public void generateTree(){
		this.children();
		if (this.children.size() > 0){
			for (TicTacToe child: children){
				if (!child.win(Piece.BLACK) && !child.win(Piece.WHITE))
					child.generateTree();
			}
		}
	}
	
	/**
	 * Minimax algorithm with alpha beta pruning
	 * @param alpha
	 * @param beta
	 * @param endTime
	 * @return
	 */
	public int alphaBeta(int alpha, int beta, long endTime) {
		//Early stopping if time limit has been reached, or base case of leaf
		if (this.children.size() == 0 || System.currentTimeMillis() >= endTime){
			Piece c;
			if ((this.player && this.playerColour == Piece.WHITE) ||
				(!this.player && this.playerColour == Piece.BLACK)){
				c = Piece.WHITE;
			}
			else {
				c = Piece.BLACK;
			}
			this.score = this.scoreLeaf(c);
			return this.score;
		}
		
		if (this.player){
			//max node
			for (TicTacToe child: this.children){
				alpha = Math.max(alpha, child.alphaBeta(alpha, beta, endTime));
				if (alpha >= beta) {
					this.score = beta;
					return beta;
				}
			}
			this.score = alpha;
			return alpha;
		}
		else {
			//min node
			for (TicTacToe child: this.children){
				beta = Math.min(beta, child.alphaBeta(alpha, beta, endTime));
				if (alpha >= beta){
					this.score = alpha;
					return alpha;
				}
			}
			this.score = beta;
			return beta;
		}
	}
	
	/**
	 * Scores the board of a leaf (win or lose/draw)
	 * To not waste moves, the win is only considered if it uses the center. 
	 * @param c
	 * @return
	 */
	public int scoreLeaf(Piece c){
		if (this.win(c)){
			return MyTools.WIN_SCORE;
		}
		return 0;
	}
	
	/**
	 * Checks if the board is a winning board for colour c
	 * @param c
	 * @return
	 */
	public boolean win(Piece c){
		return ((board[1][1] == c) && ((board[0][0] == c && board[2][2] == c) ||
				(board[0][1] == c && board[2][1] == c) ||
				(board[2][0] == c && board[0][2] == c) ||
				(board[1][0] == c && board[1][2] == c) ));
	}
	
	/**
	 * Returns the child that gives the best outcome according to minimax with alpha beta pruning
	 * @return
	 */
	public TicTacToe getBestChild(){
		return Collections.max(this.children, new Comparator<TicTacToe>(){
			public int compare (TicTacToe a, TicTacToe b){
				return (new Integer(a.score)).compareTo(new Integer(b.score));
			}
		});
	}
	
}
