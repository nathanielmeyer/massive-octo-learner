package edu.gatech.cs7641.assignment3.assets;

import burlap.behavior.singleagent.Policy;
import burlap.behavior.singleagent.planning.OOMDPPlanner;
import burlap.behavior.singleagent.planning.QComputablePlanner;
import burlap.behavior.singleagent.planning.commonpolicies.GreedyQPolicy;
import burlap.behavior.singleagent.planning.stochastic.valueiteration.ValueIteration;
import burlap.behavior.statehashing.StateHashFactory;
import burlap.behavior.statehashing.StateHashTuple;
import burlap.oomdp.auxiliary.StateParser;
import burlap.oomdp.auxiliary.common.StateYAMLParser;
import burlap.oomdp.core.Domain;
import burlap.oomdp.core.State;
import burlap.oomdp.core.TerminalFunction;
import burlap.oomdp.singleagent.RewardFunction;

public class AssetSellingProblem {

	private static final boolean POLICY_VIS_ON = false;

	public static void main(String[] args) {
		int assets = 10;
		AssetDomain generator = new AssetDomain(assets);
		Domain domain = generator.generateDomain();
		StateParser parser = new StateYAMLParser(domain);
		State initialState = generator.getHoldingAllAssetsState(domain);
		System.out.println(parser.stateToString(initialState));

		RewardFunction rf = new CashBalanceRewardFunction();
		TerminalFunction tf = new TerminalFunction() {

			@Override
			public boolean isTerminal(State s) {
				// TODO Auto-generated method stub
				return true;
			}

		};
		StateHashFactory hashingFactory = new StateHashFactory() {

			@Override
			public StateHashTuple hashState(State s) {
				StateHashTuple hash = new StateHashTuple(s) {

					@Override
					public void computeHashCode() {
						if (s == null)
							this.hashCode = 0;
						else
							this.hashCode = this.s.toString().hashCode();
					}

				};
				return hash;
			}

		};
		OOMDPPlanner planner = new ValueIteration(domain, rf, tf, 0.99,
				hashingFactory, 0.001, 100);
		planner.planFromState(initialState);

		// create a Q-greedy policy from the planner
		Policy p = new GreedyQPolicy((QComputablePlanner) planner);

		String outputPath = null;
		// record the plan results to a file
		p.evaluateBehavior(initialState, rf, tf).writeToFile(
				outputPath + "VIResult", parser);
	}

}
