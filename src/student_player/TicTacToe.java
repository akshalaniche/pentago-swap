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
	private int score;
	private boolean player;			//is the node corresponding to the student player?
	
	//creates a root node
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
	
	//assumes correct move ordering
	public TicTacToe(TicTacToe parent, PentagoCoord coord, Piece player) {
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
	
	public int getX(){
		return this.move.getX();
	}
	
	public int getY(){
		return this.move.getY();
	}
	
	private void children(){
		for (int i = 0; i < 3; i++){
			for (int j = 0; j < 3; j++){
				try {
					TicTacToe child = new TicTacToe (this, new PentagoCoord(i, j), this.playerColour);
					this.children.add(child);
				}
				catch (IllegalArgumentException e){
				}
			}
		}
		Collections.shuffle(children);
	}
	
	public void generateTree(){
		this.children();
		if (this.children.size() > 0){
			for (TicTacToe child: children){
				child.generateTree();
			}
		}
	}
	
	public int alphaBeta(int alpha, int beta) {
		if (this.children.size() == 0){
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
				alpha = Math.max(alpha, child.alphaBeta(alpha, beta));
				if (alpha >= beta) {
					this.score = beta;
					return beta;
				}
			}
			this.score = alpha;
			return alpha;
		}
		else {
			for (TicTacToe child: this.children){
				beta = Math.min(beta, child.alphaBeta(alpha, beta));
				if (alpha >= beta){
					this.score = alpha;
					return alpha;
				}
			}
			this.score = beta;
			return beta;
		}
	}
	
	//to not waste moves, the win is only considered if it uses the center. 
	public int scoreLeaf(Piece c){
		if (this.win(c))
			{
				return 10;
			}
		return 0;
	}
	
	public boolean win(Piece c){
		return ((board[1][1] == c) && ((board[0][0] == c && board[2][2] == c) ||
				(board[0][1] == c && board[2][1] == c) ||
				(board[2][0] == c && board[0][2] == c) ||
				(board[1][0] == c && board[1][2] == c) ));
	}
	
	public TicTacToe getBestChild(){
		return Collections.max(this.children, new Comparator<TicTacToe>(){
			public int compare (TicTacToe a, TicTacToe b){
				return (new Integer(a.score)).compareTo(new Integer(b.score));
			}
		});
	}
	
}
