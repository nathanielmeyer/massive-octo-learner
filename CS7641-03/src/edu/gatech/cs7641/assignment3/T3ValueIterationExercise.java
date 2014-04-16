package edu.gatech.cs7641.assignment3;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

public class T3ValueIterationExercise {
	private static final double INITIAL_VALUE = -0.5;
	private static final double WIN = 1;
	private static final double LOSE = -1;
	private static final double DRAW = 0;
	private static final int SAMPLE_SIZE = 10000000;
	private static final double PLAYER_GREED = .8;
	private static final double OPPONENT_GREED = .8;
	private static final double GAMMA = 0.8;

	public static void main(String[] args) throws FileNotFoundException,
			IOException {
		String[] states = loadStates();
		double[][] utility = new double[states.length][];
		initialize(states, utility);
		Random random = new Random();

		// Estimate values under starting policy
		updateUtility(states, utility, random);

		saveEstimatedUtility(states, utility);
		visualizePolicyThroughOptimalGame(states, utility);

	}

	private static void saveEstimatedUtility(String[] states, double[][] utility)
			throws IOException {
		FileWriter out = new FileWriter("output/t3utility.vi.csv");
		for (int i = 0; i < states.length; i++) {
			for (int j = 0; j < 9; j++) {
				out.write(String.format("\"%s\",%d,%1.2f\n", states[i], j,
						utility[i][j]));
			}
		}
		out.close();
	}

	private static void visualizePolicyThroughOptimalGame(String[] states,
			double[][] utility) {
		T3Board game = new T3Board();
		boolean turn = true;
		while (!game.isTerminal()) {
			String q = game.lowestEquivalentBoard();
			int s = search(states, q);
			game = new T3Board(states[s]);
			if (turn) {
				T3Board vis = new T3Board(states[s]);
				for (int i = 0; i < 9; i++) {
					int row = i / 3 + 1;
					int col = i % 3 + 1;
					vis.markSpace(row, col,
							String.format("%1.0f", (utility[s][i]) * 9.0)
									.charAt(0));
				}
				System.out.println(vis.toGlyph() + "\n");
			}
			int a = policy(s, states, utility);
			int row = a / 3 + 1;
			int col = a % 3 + 1;
			game.markSpace(row, col, T3Board.X);
			game.switchSeats();
			turn = !turn;
		}
		System.out.println(game.toGlyph());
	}

	private static void updateUtility(String[] states, double[][] utility,
			Random random) {
		for (int i = 0; i < SAMPLE_SIZE; i++) {
			train(states, utility, random);
		}
	}

	private static void train(String[] states, double[][] utility, Random random) {
		int s1 = random.nextInt(states.length);
		int randomAction;
		do {
			randomAction = random.nextInt(9);
		} while (!(states[s1].charAt(randomAction) == T3Board.E));
		int a1 = (random.nextDouble() > PLAYER_GREED) ? randomAction
				: policy(s1, states, utility);
		T3Board sim = new T3Board(states[s1]);
		// System.out.println(sim.toGlyph());
		int row1 = a1 / 3 + 1;
		int col1 = a1 % 3 + 1;
		// System.out.printf("board[%d] -> %d, %d\n\n", policy[s], row,
		// col);
		sim.markSpace(row1, col1, T3Board.X);
		// System.out.println(sim.toGlyph() + "\n");
		double reward = 0;
		if (sim.isTerminal()) {
			// after our turn, only win or draw possible
			reward = sim.hasWon(T3Board.X) ? WIN : DRAW;
			// update utility
			utility[s1][a1] = GAMMA * reward + (1 - GAMMA) * utility[s1][a1];
			// System.out.println("Terminal state on players turn.  Value of s1/a1 updated to "+utility[s1][a1]);
		} else {
			// opponent's turn
			sim.switchSeats();
			// find policy for opponent
			String q = sim.lowestEquivalentBoard();
			int s2 = search(states, q);
			sim = new T3Board(q);
			do {
				randomAction = random.nextInt(9);
			} while (!(q.charAt(randomAction) == T3Board.E));
			int a2 = (random.nextDouble() > OPPONENT_GREED) ? randomAction
					: policy(s2, states,utility);
			int row2 = a2 / 3 + 1;
			int col2 = a2 % 3 + 1;
			if (!sim.markSpace(row2, col2, T3Board.X))
				throw new RuntimeException("Why?");
			if (sim.isTerminal()) {
				// after opponents turn, only lose or draw possible
				reward = sim.hasWon(T3Board.X) ? LOSE : DRAW;
				// update utility
				utility[s1][a1] = GAMMA * reward + (1.0 - GAMMA)
						* utility[s1][a1];
				// System.out.println("Terminal state on opponents turn.  Value of s1/a1 updated to "+
				// utility[s1][a1]);
			} else {
				// player's turn
				sim.switchSeats();
				String r = sim.lowestEquivalentBoard();
				int s3 = 0;
				try {
					s3 = search(states, r);
				} catch (RuntimeException ex) {
					T3Board tmp = new T3Board(states[s2]);
					tmp.switchSeats();
					System.err.println(tmp.toGlyph() + "\n");
					System.err.println("Mark " + a2 + " -> " + row2 + ", "
							+ col2);
					tmp.markSpace(row2, col2, T3Board.O);
					System.err.println(tmp.toGlyph() + "\n");
					throw (ex);
				}
				int a3 = policy(s3, states, utility);
				// update utility
				utility[s1][a1] = (1.0 - GAMMA) * utility[s1][a1] + GAMMA
						* utility[s3][a3];

				// if (random.nextDouble() < 1.0 / SAMPLE_SIZE)
				// System.out.println(sim.toGlyph());
			}
		}
	}

	private static int policy(int state, String[] states, double[][] utility) {
		int action=0;
		for (int i=1; i<9; i++) if(utility[state][i]>utility[state][action]) action=i;
		return action;
	}

	private static int search(String[] states, String q) {
		boolean found = false;
		int s2;
		for (s2 = 0; s2 < states.length; s2++) {
			if (states[s2].equals(q)) {
				found = true;
				break;
			}
		}
		if (!found)
			throw new RuntimeException("Non-terminal state not found! |" + q
					+ "|\n" + (new T3Board(q)).toGlyph());
		return s2;
	}

	private static String[] loadStates() throws FileNotFoundException,
			IOException {
		BufferedReader in = new BufferedReader(new FileReader(
				"output/t3statespace"));
		ArrayList<String> stateSpace = new ArrayList<String>();
		String line;
		while ((line = in.readLine()) != null) {
			stateSpace.add(line);
		}
		in.close();
		in = null;
		String[] states = new String[stateSpace.size()];
		states = stateSpace.toArray(states);
		return states;
	}

	private static void initialize(String[] states, double[][] utility) {
		// Initialize policy and value
		for (int i = 0; i < states.length; i++) {
			// initialize values to INITIAL_VALUE
			utility[i] = new double[9];
			for (int j = 0; j < 9; j++) {
				utility[i][j] = states[i].charAt(j) == T3Board.E ? INITIAL_VALUE
						: 2 * LOSE;
			}
		}
	}
}
