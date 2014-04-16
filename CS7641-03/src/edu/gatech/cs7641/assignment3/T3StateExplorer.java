package edu.gatech.cs7641.assignment3;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Random;

public class T3StateExplorer {
	public static void main(String[] args) throws IOException {
		ArrayList<T3Board> states = new ArrayList<T3Board>();
		int leaves = visitAllReachableStates(new T3Board(), states);
		System.out.println("Leaf nodes in game tree: " + leaves);
		System.out.println("Branches in game tree: " + states.size());
		HashSet<String> unique = new HashSet<String>();
		for (T3Board state : states)
			unique.add(state.toString());
		System.out.println("Unique reachable states: " + unique.size());
		HashSet<T3Board> reduced = new HashSet<T3Board>();
		for (T3Board board : states)
			if (!board.isTerminal())
				reduced.add(new T3Board(board.lowestEquivalentBoard()));
		System.out.println("Reduced states: " + reduced.size());
		int saPairs = countStatesAndActions(reduced);
		System.out.println("Reduced state-action pairs: " + saPairs);
		System.out.println();

		System.out.println("Small sample of reduced states");
		System.out.println();
		Random random = new Random();
		T3Board check = null;
		File statespace = new File("output/t3statespace");
		if (statespace.exists())
			statespace.delete();
		statespace.getParentFile().mkdirs();
		FileWriter out = new FileWriter(statespace);
		for (T3Board board : reduced) {
			out.write(board.toString() + "\n");
			if (random.nextDouble() < 0.005) {
				System.out.println(board.toGlyph());
				check = board;
			}
		}
		out.close();

		System.out.println("Symmetry check of last sample");
		System.out.println();
		for (String sym : check.equivalentBoards()) {
			System.out.println((new T3Board(sym)).toGlyph());
		}

	}

	private static ArrayList<T3Board> directlyReachableStates(T3Board board) {
		ArrayList<T3Board> drs = new ArrayList<T3Board>();

		// look for reachable states
		for (int row = 1; row <= 3; row++) {
			for (int col = 1; col <= 3; col++) {
				T3Board next = new T3Board(board);
				// if space is available, mark it
				if (next.markSpace(row, col, T3Board.X)) {
					// switch X and O in next stage, so the agent is always X
					next.switchSeats();
					// add it to the list
					drs.add(next);
				}
			}
		}
		return drs;
	}

	private static int visitAllReachableStates(final T3Board board,
			final ArrayList<T3Board> states) {
		states.add(board);
		int leaves = 0;
		if (!board.isTerminal()) {
			// look for reachable states
			for (T3Board next : directlyReachableStates(board))
				leaves += visitAllReachableStates(next, states);

			return leaves;
		} else {
			return 1;
		}
	}

	private static int countStatesAndActions(final HashSet<T3Board> unique) {
		int stateActionPairs = 0;
		for (T3Board board : unique) {
			stateActionPairs += directlyReachableStates(board).size();
		}
		return stateActionPairs; // all reachable states plus this state
	}

}
