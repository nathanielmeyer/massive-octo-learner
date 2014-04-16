package edu.gatech.cs7641.assignment3;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

public class T3PolicyIterationExercise {
	private static final double INITIAL_VALUE = -0.5;
	private static final double WIN = 1;
	private static final double LOSE = -1;
	private static final double DRAW = 0;
	private static final int SAMPLE_SIZE = 10000;
	private static final double PLAYER_GREED = .8;
	private static final double OPPONENT_GREED = .8;
	private static final double GAMMA = 0.1;
	private static final int MAX_ITERATIONS = 1000;

	public static void main(String[] args) throws IOException {
		String[] states = loadStates();
		int[] policy = new int[states.length];
		double[][] utility = new double[states.length][];
		initialize(states, policy, utility);
		Random random = new Random();

		int delta = 0;
		for (int i = 0; i < MAX_ITERATIONS; i++) {
			// Estimate values
			updateUtility(states, policy, utility, random);
			// Chose a better policy
			int[] newPolicy = readPolicyFromUtility(states, utility);
			if (random.nextDouble() < Math.max(1.0 / i,1/100)) {
				delta = 0;
				for (int k = 0; k < newPolicy.length; k++)
					if (newPolicy[k] != policy[k])
						delta++;
				System.out.println(i + " policy delta: " + delta);
			}
			policy = newPolicy;
		}
		System.out.println("Final policy delta: " + delta);

		visualizePolicyThroughOptimalGame(states, policy, utility);

		saveOptimalPolicy(states, policy);
		saveEstimatedUtility(states, utility);
	}

	private static void saveEstimatedUtility(String[] states, double[][] utility)
			throws IOException {
		FileWriter out = new FileWriter("output/t3utility.pi.csv");
		for (int i = 0; i < states.length; i++) {
			for (int j = 0; j < 9; j++) {
				out.write(String.format("\"%s\",%d,%1.2f\n", states[i], j,
						utility[i][j]));
			}
		}
		out.close();
	}

	private static void saveOptimalPolicy(String[] states, int[] policy)
			throws IOException {
		FileWriter out = new FileWriter("output/t3policy.pi.csv");
		for (int i = 0; i < states.length; i++) {
			for (int j = 0; j < 9; j++) {
				out.write(String.format("\"%s\",%d\n", states[i], policy[i]));
			}
		}
		out.close();
	}

	private static int[] readPolicyFromUtility(String[] states,
			double[][] utility) throws IOException {
		int[] policy = new int[states.length];
		Arrays.fill(policy, -1);
		for (int i = 0; i < states.length; i++) {
			int p = 0;
			for (int j = 1; j < 9; j++) {
				if (utility[i][j] > utility[i][p])
					p = j;
			}
			policy[i] = p;
		}
		return policy;
	}

	private static void visualizePolicyThroughOptimalGame(String[] states,
			int[] policy, double[][] utility) {
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
					vis.markSpace(
							row,
							col,
							String.format("%d", Math.round(utility[s][i] * 9.0))
									.charAt(0));
				}
				System.out.println(vis.toGlyph() + "\n");
			}
			int a = policy[s];
			int row = a / 3 + 1;
			int col = a % 3 + 1;
			game.markSpace(row, col, T3Board.X);
			game.switchSeats();
			turn = !turn;
		}
		System.out.println(game.toGlyph());
	}

	private static void updateUtility(String[] states, int[] policy,
			double[][] utility, Random random) {
		for (int i = 0; i < SAMPLE_SIZE; i++) {
			train(states, policy, utility, random);
		}
	}

	private static void train(String[] states, int[] policy,
			double[][] utility, Random random) {
		int s1 = random.nextInt(states.length);

		int randomAction;
		do {
			randomAction = random.nextInt(9);
		} while (!(states[s1].charAt(randomAction) == T3Board.E));
		int a1 = (random.nextDouble() > PLAYER_GREED) ? randomAction
				: policy[s1];
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
					: policy[s2];
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
				int a3 = policy[s3];
				// update utility
				utility[s1][a1] = (1.0 - GAMMA) * utility[s1][a1] + GAMMA
						* utility[s3][a3];

				// if (random.nextDouble() < 1.0 / SAMPLE_SIZE)
				// System.out.println(sim.toGlyph());
			}
		}
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

	private static void initialize(String[] states, int[] policy,
			double[][] utility) {
		// Initialize policy and value
		for (int i = 0; i < states.length; i++) {
			// Choose first available empty space in state
			for (int j = 0; j < 9; j++) {
				if (states[i].charAt(j) == T3Board.E) {
					policy[i] = j;
					break;
				}
			}
			// initialize values to INITIAL_VALUE
			utility[i] = new double[9];
			for (int j = 0; j < 9; j++) {
				utility[i][j] = states[i].charAt(j) == T3Board.E ? INITIAL_VALUE
						: 2 * LOSE;
			}
		}
	}
}
