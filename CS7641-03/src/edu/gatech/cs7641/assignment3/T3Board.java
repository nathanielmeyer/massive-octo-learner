package edu.gatech.cs7641.assignment3;

import java.util.Arrays;

import org.apache.commons.lang3.ArrayUtils;

public class T3Board {
	/**
	 * Empty space
	 */
	public static final char E = ' ';
	/**
	 * X in space
	 */
	public static final char X = 'X';
	/**
	 * O in space
	 */
	public static final char O = 'O';
	char[] board;

	public T3Board() {
		board = new char[9];
		Arrays.fill(board, E);
	}

	/**
	 * @param row
	 *            The row of the desired space (1-3)
	 * @param col
	 *            The column of the desired space (1-3)
	 * @param mark
	 *            The mark to set in the desired space
	 * @returns True if the mark was set, false otherwise
	 */
	public boolean markSpace(int row, int col, char mark) {
		if (isMarkInSpace(row, col, E)) {
			board[(row - 1) * 3 + (col - 1)] = mark;
			return true;
		} else
			return false;
	}

	/**
	 * @param row
	 *            The row of the desired space (1-3)
	 * @param col
	 *            The column of the desired space (1-3)
	 * @return the integer value {X,O} of the mark occupying the space, or E if
	 *         the space is unoccupied
	 */
	public int getMarkInSpace(int row, int col) {
		return board[(row - 1) * 3 + (col - 1)];
	}

	/**
	 * @param row
	 *            The row of the desired space (1-3)
	 * @param col
	 *            The column of the desired space (1-3)
	 * @param mark
	 *            The mark to look for in the desired space
	 * @return
	 */
	public boolean isMarkInSpace(int row, int col, int mark) {
		return getMarkInSpace(row, col) == mark;
	}

	/**
	 * @param mark
	 *            The mark to check
	 * @return A vector of the number of marks held by the given mark in each of
	 *         the eight possible runs, or zero if the run is blocked by the
	 *         opposing mark.
	 */
	public int[] getRunLengths(final char mark) {
		int[] runs = new int[8];
		int marks;
		boolean blocked;
		/* columns */
		for (int col = 1; col <= 3; col++) {
			marks = 0;
			blocked = false;
			for (int row = 1; row <= 3; row++) {
				if (isMarkInSpace(row, col, mark)) {
					marks++;
				} else if (!isMarkInSpace(row, col, E)) {
					blocked = true;
				}
			}
			runs[col - 1] = blocked ? 0 : marks;
		}
		/* rows */
		for (int row = 1; row <= 3; row++) {
			marks = 0;
			blocked = false;
			for (int col = 1; col <= 3; col++) {
				if (isMarkInSpace(row, col, mark)) {
					marks++;
				} else if (!isMarkInSpace(row, col, E)) {
					blocked = true;
				}
			}
			runs[2 + row] = blocked ? 0 : marks;
		}
		/* diagonal 1 */
		marks = 0;
		blocked = false;
		for (int i = 1; i <= 3; i++) {
			if (isMarkInSpace(i, i, mark)) {
				marks++;
			} else if (!isMarkInSpace(i, i, E)) {
				blocked = true;
			}
		}
		runs[6] = blocked ? 0 : marks;
		/* diagonal 2 */
		marks = 0;
		blocked = false;
		for (int i = 1; i <= 3; i++) {
			if (isMarkInSpace(i, 4 - i, mark)) {
				marks++;
			} else if (!isMarkInSpace(i, 3 - i, E)) {
				blocked = true;
			}
		}
		runs[7] = blocked ? 0 : marks;
		return runs;
	}

	/**
	 * Swap all marks with opponent to switch seats
	 */
	public void switchSeats() {
		for (int i = 0; i < board.length; i++) {
			if (board[i] == E)
				continue;
			if (board[i] == X)
				board[i] = O;
			else
				board[i] = X;
		}
	}

	public String toString() {
		return new String(board);
	}

	public String toGlyph() {
		return String.format("%c | %c | %c\n" + "- + - + -\n" + "%c | %c | %c\n"
				+ "- + - + -\n" + "%c | %c | %c\n", board[0], board[1], board[2],
				board[3], board[4], board[5], board[6], board[7], board[8]);
	}

	public T3Board(String board) {
		this.board = board.toCharArray();
	}

	public T3Board(T3Board board2) {
		this.board = board2.toString().toCharArray();
	}

	public void rotateClockwise() {
		char[] newBoard = new char[9];
		newBoard[0] = board[6];
		newBoard[1] = board[3];
		newBoard[2] = board[0];
		newBoard[3] = board[7];
		newBoard[4] = board[4];
		newBoard[5] = board[1];
		newBoard[6] = board[8];
		newBoard[7] = board[5];
		newBoard[8] = board[2];
		board = newBoard;
		/*
		 * 0,1,2 6,3,0 3,4,5 7,4,1 6,7,8 8,5,2
		 */
	}

	public void reflectAboutDiagonal() {
		char[] newBoard = new char[9];
		newBoard[0] = board[0];
		newBoard[1] = board[3];
		newBoard[2] = board[6];
		newBoard[3] = board[1];
		newBoard[4] = board[4];
		newBoard[5] = board[7];
		newBoard[6] = board[2];
		newBoard[7] = board[5];
		newBoard[8] = board[8];
		board = newBoard;
		/*
		 * 0,1,2 0,3,6 
		 * 3,4,5 1,4,7 
		 * 6,7,8 2,5,8
		 */
	}

	public void reflectAboutOtherDiagonal() {
		char[] newBoard = new char[9];
		newBoard[0] = board[8];
		newBoard[1] = board[5];
		newBoard[2] = board[2];
		newBoard[3] = board[7];
		newBoard[4] = board[4];
		newBoard[5] = board[1];
		newBoard[6] = board[6];
		newBoard[7] = board[3];
		newBoard[8] = board[0];
		board = newBoard;
		/*
		 * 0,1,2 8,5,2 3,4,5 7,4,1 6,7,8 6,3,0
		 */
	}

	public void flipAboutX() {
		char[] newBoard = new char[9];
		newBoard[0] = board[6];
		newBoard[1] = board[7];
		newBoard[2] = board[8];
		newBoard[3] = board[3];
		newBoard[4] = board[4];
		newBoard[5] = board[5];
		newBoard[6] = board[0];
		newBoard[7] = board[1];
		newBoard[8] = board[2];
		board = newBoard;
		/*
		 * 0,1,2 6,7,8 3,4,5 3,4,5 6,7,8 0,1,2
		 */
	}

	public void flipAboutY() {
		char[] newBoard = new char[9];
		newBoard[0] = board[2];
		newBoard[1] = board[1];
		newBoard[2] = board[0];
		newBoard[3] = board[5];
		newBoard[4] = board[4];
		newBoard[5] = board[3];
		newBoard[6] = board[8];
		newBoard[7] = board[7];
		newBoard[8] = board[6];
		board = newBoard;
		/*
		 * 0,1,2 2,1,0 3,4,5 5,4,3 6,7,8 8,7,6
		 */
	}

	public boolean isTerminal() {
		if (hasWon(X))
			return true;
		if (hasWon(O))
			return true;
		for (char space: board) if (space==E) return false;
		return true;
	}

	public boolean hasWon(char mark) {
		int[] runs = getRunLengths(mark);
		for (int run : runs)
			if (run == 3)
				return true;
		return false;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof T3Board) {
			T3Board board = (T3Board) obj;
			if (ArrayUtils.isEquals(board.board, this.board))
				return true; // same board
			for (String equivalent: board.equivalentBoards())
				if (ArrayUtils.isEquals(equivalent.toCharArray(), this.board))
					return true; // same board
		}
		return false;
	}

	@Override
	public int hashCode() {
		return lowestEquivalentBoard().hashCode();
	}

	public String lowestEquivalentBoard() {
		String[] hashes = equivalentBoards();
		int min=0;
		for(int i=1;i<hashes.length;i++){
			if(hashes[i].compareTo(hashes[min])<0) min=i;
		}
		return hashes[min];
	}

	public String[] equivalentBoards() {
		String[] hashes = new String[8];
		hashes[0]=this.toString();
		// rotations
		T3Board compare = new T3Board(this);
		compare.rotateClockwise();
		hashes[1]=compare.toString();
		compare.rotateClockwise();
		hashes[2]=compare.toString();
		compare.rotateClockwise();
		hashes[3]=compare.toString();
		// reflections
		compare = new T3Board(this);
		compare.reflectAboutDiagonal();
		hashes[4]=compare.toString();
		compare.rotateClockwise();
		hashes[5]=compare.toString();
		compare.rotateClockwise();
		hashes[6]=compare.toString();
		compare.rotateClockwise();
		hashes[7]=compare.toString();
		return hashes;
	}

	public T3Board collapse() {
		this.board=lowestEquivalentBoard().toCharArray();
		return this;
	}

	
}
