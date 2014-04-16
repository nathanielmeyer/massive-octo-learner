package edu.gatech.cs7641.assignment3;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

public class T3AlgorithmDeathmatch {
	public static void main(String[] args) throws IOException {
		String[] states = initStates();
		int[] vi;
		try {
			vi = loadVIPolicy(states);
		} catch (FileNotFoundException e) {
			throw new RuntimeException("Value iteration utility file not found. Run T3ValueIterationExercise to generate it.",e);
		}
		int[] pi;
		try {
			pi = loadPIPolicy(states);
		} catch (FileNotFoundException e) {
			throw new RuntimeException("Policy iteration policy file not found. Run T3ValueIterationExercise to generate it.",e);
		}
		System.out.println("Value iteration (X) opens against policy iteration (O)\n");
		T3Board match1 = visualizePolicyThroughOptimalGame(states, vi, pi);
		if(match1.hasWon(T3Board.X)) {
			System.out.println("Value iteration wins the match.\n");
		}else if (match1.hasWon(T3Board.O)) {
			System.out.println("Policy iteration wins the match.\n");
		} else {
			System.out.println("The match ended in a draw.\n");
		}

		System.out.println("Policy iteration (X) opens against value iteration (O)\n");
		T3Board match2 = visualizePolicyThroughOptimalGame(states, pi, vi);
		if(match2.hasWon(T3Board.X)) {
			System.out.println("Policy iteration wins the match.\n");
		}else if (match2.hasWon(T3Board.O)) {
			System.out.println("Value iteration wins the match.\n");
		} else {
			System.out.println("The match ended in a draw.\n");
		}
		
		int delta=0;
		for (int i=0;i<states.length; i++) {
			if (pi[i]!=vi[i]) delta++;
		}
		System.out.printf("The policies differ by only %d/%d decisions.\n",delta,states.length);
	}
	
	private static T3Board visualizePolicyThroughOptimalGame(String[] states,
			int[] policy1, int[] policy2) {
		T3Board game = new T3Board();
		boolean turn = true;
		while (!game.isTerminal()) {
			String q = game.lowestEquivalentBoard();
			int s = search(states, q);
			game = new T3Board(states[s]);
			if (turn) {
				System.out.println(game.toGlyph() + "\n");
			}
			int a = turn?policy1[s]:policy2[s];
			int row = a / 3 + 1;
			int col = a % 3 + 1;
			game.markSpace(row, col, T3Board.X);
			game.switchSeats();
			turn = !turn;
		}
		System.out.println(game.toGlyph());
		return game;
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
	
	private static String[] initStates() throws IOException {
		String[] states;
		try {
			 states = loadStates();
		} catch (FileNotFoundException e) {
			throw new RuntimeException("State space file not found. Run T3StateExplorer to generate it.",e);
		}
		return states;
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

	private static int[] loadVIPolicy(String[] states)
			throws FileNotFoundException, IOException {
		BufferedReader in = new BufferedReader(new FileReader(
				"output/t3utility.vi.csv"));
		int[] policy = readPolicyFromUtility(states, in);
		return policy;
	}

	private static int[] loadPIPolicy(String[] states)
			throws FileNotFoundException, IOException {
		BufferedReader in = new BufferedReader(new FileReader(
				"output/t3utility.pi.csv"));

		int[] policy = readPolicyFromUtility(states, in);
		return policy;
	}

	private static int[] readPolicyFromUtility(String[] states,
			BufferedReader in) throws IOException {
		int[] policy = new int[states.length];
		double[][] utility = new double[states.length][];
		for (int i=0;i<utility.length;i++) utility[i]=new double[9];
		Arrays.fill(policy, -1);
		String line;
		while ((line = in.readLine()) != null) {
			String[] f = line.split(",");
			String s = f[0].replaceAll("\"", "");
			int i;
			for (i = 0; i < states.length; i++) {
				if (states[i].equals(s))
					break;
			}
			utility[i][Integer.parseInt(f[1])] = Double.parseDouble(f[2]);
		}
		in.close();
		in = null;
		for (int i=0;i<states.length;i++) {
			int p=0;
			for (int j=1;j<9;j++) {
				if(utility[i][j]>utility[i][p])p=j;
			}
			policy[i]=p;
		}
		return policy;
	}
}
