package edu.gatech.cs7641.assignment3;

import java.awt.Color;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JLabel;

import edu.gatech.cs7641.assignment3.test.BasicBehavior;
import burlap.behavior.singleagent.EpisodeAnalysis;
import burlap.behavior.singleagent.EpisodeSequenceVisualizer;
import burlap.behavior.singleagent.Policy;
import burlap.behavior.singleagent.auxiliary.StateReachability;
import burlap.behavior.singleagent.auxiliary.performance.LearningAlgorithmExperimenter;
import burlap.behavior.singleagent.auxiliary.performance.PerformanceMetric;
import burlap.behavior.singleagent.auxiliary.performance.TrialMode;
import burlap.behavior.singleagent.auxiliary.valuefunctionvis.ValueFunctionVisualizerGUI;
import burlap.behavior.singleagent.auxiliary.valuefunctionvis.common.ArrowActionGlyph;
import burlap.behavior.singleagent.auxiliary.valuefunctionvis.common.LandmarkColorBlendInterpolation;
import burlap.behavior.singleagent.auxiliary.valuefunctionvis.common.PolicyGlyphPainter2D;
import burlap.behavior.singleagent.auxiliary.valuefunctionvis.common.StateValuePainter2D;
import burlap.behavior.singleagent.auxiliary.valuefunctionvis.common.PolicyGlyphPainter2D.PolicyGlyphRenderStyle;
import burlap.behavior.singleagent.learning.GoalBasedRF;
import burlap.behavior.singleagent.learning.LearningAgent;
import burlap.behavior.singleagent.learning.LearningAgentFactory;
import burlap.behavior.singleagent.learning.tdmethods.QLearning;
import burlap.behavior.singleagent.learning.tdmethods.SarsaLam;
import burlap.behavior.singleagent.planning.OOMDPPlanner;
import burlap.behavior.singleagent.planning.QComputablePlanner;
import burlap.behavior.singleagent.planning.StateConditionTest;
import burlap.behavior.singleagent.planning.commonpolicies.GreedyQPolicy;
import burlap.behavior.singleagent.planning.deterministic.DeterministicPlanner;
import burlap.behavior.singleagent.planning.deterministic.SDPlannerPolicy;
import burlap.behavior.singleagent.planning.deterministic.TFGoalCondition;
import burlap.behavior.singleagent.planning.deterministic.informed.Heuristic;
import burlap.behavior.singleagent.planning.deterministic.informed.astar.AStar;
import burlap.behavior.singleagent.planning.deterministic.uninformed.bfs.BFS;
import burlap.behavior.singleagent.planning.deterministic.uninformed.dfs.DFS;
import burlap.behavior.singleagent.planning.stochastic.valueiteration.ValueIteration;
import burlap.behavior.statehashing.DiscreteStateHashFactory;
import burlap.domain.singleagent.gridworld.GridWorldDomain;
import burlap.domain.singleagent.gridworld.GridWorldStateParser;
import burlap.domain.singleagent.gridworld.GridWorldVisualizer;
import burlap.oomdp.auxiliary.StateGenerator;
import burlap.oomdp.auxiliary.StateParser;
import burlap.oomdp.auxiliary.common.ConstantStateGenerator;
import burlap.oomdp.core.ObjectInstance;
import burlap.oomdp.core.State;
import burlap.oomdp.core.TerminalFunction;
import burlap.oomdp.singleagent.RewardFunction;
import burlap.oomdp.singleagent.SADomain;
import burlap.oomdp.singleagent.common.SinglePFTF;
import burlap.oomdp.singleagent.common.UniformCostRF;
import burlap.oomdp.singleagent.common.VisualActionObserver;
import burlap.oomdp.visualizer.Visualizer;

public class GridWorldScaleExperiment {

	private static final boolean LEARNING_OBSERVER_ON = false;
	private static final boolean EPISODE_VIS_ON = false;
	private static final boolean POLICY_VIS_ON = true;
	private static final boolean PLOTTER_ON = true;
	private static final boolean EXPERIMENTER_ON = true;
	GridWorldDomain gwdg;
	SADomain domain;
	StateParser sp;
	RewardFunction rf;
	TerminalFunction tf;
	StateConditionTest goalCondition;
	State initialState;
	DiscreteStateHashFactory hashingFactory;

	public static void main(String[] args) {
		JFrame mainFrame = new JFrame();
		mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		mainFrame.add(new JLabel("Close to exit"));
		mainFrame.pack();
		mainFrame.setVisible(true);

		// init
		GridWorldScaleExperiment example = new GridWorldScaleExperiment();
		String outputPath = "output/";

		// algorithms
//		example.BFSExample(outputPath);
//		example.DFSExample(outputPath);
//		example.AStarExample(outputPath);
//		example.ValueIterationExample(outputPath);
//		example.QLearningExample(outputPath);
//		example.SarsaLearningExample(outputPath);

		// visualize results
		if (EPISODE_VIS_ON)
			example.visualize(outputPath);
		if (EXPERIMENTER_ON)
			example.experimenterAndPlotter();

	}

	public void SarsaLearningExample(String outputPath) {

		if (!outputPath.endsWith("/")) {
			outputPath = outputPath + "/";
		}

		// discount= 0.99; initialQ=0.0; learning rate=0.5; lambda=1.0
		LearningAgent agent = new SarsaLam(domain, rf, tf, 0.99,
				hashingFactory, 0., 0.5, 1.0);

		// run learning for 100 episodes
		EpisodeAnalysis ea = null;
		for (int i = 0; i < 100; i++) {
			ea = agent.runLearningEpisodeFrom(initialState);
			// ea.writeToFile(String.format("%sSLAM-ep%03d", outputPath, i),
			// sp);
			// System.out.println(i + ": " + ea.numTimeSteps());
		}
		ea.writeToFile(outputPath + "SarsaResult", sp);

	}

	public void QLearningExample(String outputPath) {

		if (!outputPath.endsWith("/")) {
			outputPath = outputPath + "/";
		}

		// creating the learning algorithm object; discount= 0.99; initialQ=0.0;
		// learning rate=0.9
		LearningAgent agent = new QLearning(domain, rf, tf, 0.99,
				hashingFactory, 0., 0.9);

		// run learning for 100 episodes
		EpisodeAnalysis ea = null;
		for (int i = 0; i < 100; i++) {
			ea = agent.runLearningEpisodeFrom(initialState);
			// ea.writeToFile(String.format("%sQL-ep%03d", outputPath, i), sp);
			// System.out.println(i + ": " + ea.numTimeSteps());
		}
		ea.writeToFile(outputPath + "QLResult", sp);
	}

	public void ValueIterationExample(String outputPath) {

		if (!outputPath.endsWith("/")) {
			outputPath = outputPath + "/";
		}

		OOMDPPlanner planner = new ValueIteration(domain, rf, tf, 0.99,
				hashingFactory, 0.001, 100);
		planner.planFromState(initialState);

		// create a Q-greedy policy from the planner
		Policy p = new GreedyQPolicy((QComputablePlanner) planner);

		// record the plan results to a file
		p.evaluateBehavior(initialState, rf, tf).writeToFile(
				outputPath + "VIResult", sp);
		if (POLICY_VIS_ON)
			this.valueFunctionVisualize((QComputablePlanner) planner, p);

	}

	public void AStarExample(String outputPath) {
		if (!outputPath.endsWith("/")) {
			outputPath = outputPath + "/";
		}
		Heuristic mdistHeuristic = new Heuristic() {
			@Override
			public double h(State s) {
				String an = GridWorldDomain.CLASSAGENT;
				String ln = GridWorldDomain.CLASSLOCATION;
				ObjectInstance agent = s.getObjectsOfTrueClass(an).get(0);
				ObjectInstance location = s.getObjectsOfTrueClass(ln).get(0);
				// get agent position
				int ax = agent.getDiscValForAttribute(GridWorldDomain.ATTX);
				int ay = agent.getDiscValForAttribute(GridWorldDomain.ATTY);
				// get location position
				int lx = location.getDiscValForAttribute(GridWorldDomain.ATTX);
				int ly = location.getDiscValForAttribute(GridWorldDomain.ATTY);
				// compute Manhattan distance
				double mdist = Math.abs(ax - lx) + Math.abs(ay - ly);
				return -mdist;
			}
		};
		// provide A* the heuristic as well as the reward function so that it
		// can keep
		// track of the actual cost
		DeterministicPlanner planner = new AStar(domain, rf, goalCondition,
				hashingFactory, mdistHeuristic);
		planner.planFromState(initialState);
		// capture the computed plan in a partial policy
		Policy p = new SDPlannerPolicy(planner);
		// record the plan results to a file
		p.evaluateBehavior(initialState, rf, tf).writeToFile(
				outputPath + "AStarResult", sp);
	}

	public void DFSExample(String outputPath) {

		if (!outputPath.endsWith("/")) {
			outputPath = outputPath + "/";
		}

		// DFS ignores reward; it just searches for a goal condition satisfying
		// state
		DeterministicPlanner planner = new DFS(domain, goalCondition,
				hashingFactory, 10000);// max depth to prevent infinite runs
		planner.planFromState(initialState);

		// capture the computed plan in a partial policy
		Policy p = new SDPlannerPolicy(planner);

		// record the plan results to a file
		p.evaluateBehavior(initialState, rf, tf).writeToFile(
				outputPath + "DFSResult", sp);

	}

	public void BFSExample(String outputPath) {

		if (!outputPath.endsWith("/")) {
			outputPath = outputPath + "/";
		}

		// BFS ignores reward; it just searches for a goal condition satisfying
		// state
		DeterministicPlanner planner = new BFS(domain, goalCondition,
				hashingFactory);
		planner.planFromState(initialState);

		// capture the computed plan in a partial policy
		Policy p = new SDPlannerPolicy(planner);

		// record the plan results to a file
		p.evaluateBehavior(initialState, rf, tf).writeToFile(
				outputPath + "BFSResult", sp);

	}

	public GridWorldScaleExperiment() {
		// create the domain
		gwdg = new GridWorldDomain(11, 11);
		gwdg.setMapToFourRooms();
		domain = (SADomain) gwdg.generateDomain();

		// create the state parser
		sp = new GridWorldStateParser(domain);

		// define the task
		rf = new UniformCostRF();
		tf = new SinglePFTF(
				domain.getPropFunction(GridWorldDomain.PFATLOCATION));
		goalCondition = new TFGoalCondition(tf);

		// set up the initial state of the task
		initialState = GridWorldDomain.getOneAgentOneLocationState(domain);
		GridWorldDomain.setAgent(initialState, 0, 0);
		GridWorldDomain.setLocation(initialState, 0, 10, 10);

		// set up the state hashing system
		hashingFactory = new DiscreteStateHashFactory();
		hashingFactory
				.setAttributesForClass(
						GridWorldDomain.CLASSAGENT,
						domain.getObjectClass(GridWorldDomain.CLASSAGENT).attributeList);

		// set up an observer to watch
		if (LEARNING_OBSERVER_ON) {
			VisualActionObserver observer = new VisualActionObserver(domain,
					GridWorldVisualizer.getVisualizer(domain, gwdg.getMap()));
			domain.setActionObserverForAllAction(observer);
			observer.initGUI();
		}
	}

	public void visualize(String outputPath) {
		Visualizer v = GridWorldVisualizer.getVisualizer(domain, gwdg.getMap());
		@SuppressWarnings("unused")
		EpisodeSequenceVisualizer evis = new EpisodeSequenceVisualizer(v,
				domain, sp, outputPath);
	}

	public void valueFunctionVisualize(QComputablePlanner planner, Policy p) {
		List<State> allStates = StateReachability.getReachableStates(
				initialState, (SADomain) domain, hashingFactory);
		LandmarkColorBlendInterpolation rb = new LandmarkColorBlendInterpolation();
		rb.addNextLandMark(0., Color.RED);
		rb.addNextLandMark(1., Color.BLUE);

		StateValuePainter2D svp = new StateValuePainter2D(rb);
		svp.setXYAttByObjectClass(GridWorldDomain.CLASSAGENT,
				GridWorldDomain.ATTX, GridWorldDomain.CLASSAGENT,
				GridWorldDomain.ATTY);

		PolicyGlyphPainter2D spp = new PolicyGlyphPainter2D();
		spp.setXYAttByObjectClass(GridWorldDomain.CLASSAGENT,
				GridWorldDomain.ATTX, GridWorldDomain.CLASSAGENT,
				GridWorldDomain.ATTY);
		spp.setActionNameGlyphPainter(GridWorldDomain.ACTIONNORTH,
				new ArrowActionGlyph(0));
		spp.setActionNameGlyphPainter(GridWorldDomain.ACTIONSOUTH,
				new ArrowActionGlyph(1));
		spp.setActionNameGlyphPainter(GridWorldDomain.ACTIONEAST,
				new ArrowActionGlyph(2));
		spp.setActionNameGlyphPainter(GridWorldDomain.ACTIONWEST,
				new ArrowActionGlyph(3));
		spp.setRenderStyle(PolicyGlyphRenderStyle.DISTSCALED);

		ValueFunctionVisualizerGUI gui = new ValueFunctionVisualizerGUI(
				allStates, svp, planner);
		gui.setSpp(spp);
		gui.setPolicy(p);
		gui.setBgColor(Color.GRAY);
		gui.initGUI();
	}

	public void experimenterAndPlotter() {

		// custom reward function for more interesting results
		final RewardFunction rf = new GoalBasedRF(this.goalCondition, 5., -0.1);

		/**
		 * Create factories for Q-learning agent and SARSA agent to compare
		 */

		LearningAgentFactory qLearningFactory = new LearningAgentFactory() {

			@Override
			public String getAgentName() {
				return "Q-learning";
			}

			@Override
			public LearningAgent generateAgent() {
				return new QLearning(domain, rf, tf, 0.99, hashingFactory, 0.3,
						0.1);
			}
		};

		LearningAgentFactory sarsaLearningFactory = new LearningAgentFactory() {

			@Override
			public String getAgentName() {
				return "SARSA";
			}

			@Override
			public LearningAgent generateAgent() {
				return new SarsaLam(domain, rf, tf, 0.99, hashingFactory, 0.0,
						0.1, 1.);
			}
		};

		StateGenerator sg = new ConstantStateGenerator(this.initialState);

		LearningAlgorithmExperimenter exp = new LearningAlgorithmExperimenter(
				this.domain, rf, sg, 10, 100, qLearningFactory,
				sarsaLearningFactory);

		exp.setUpPlottingConfiguration(500, 250, 2, 1000,
				TrialMode.MOSTRECENTANDAVERAGE,
				PerformanceMetric.CUMULATIVESTEPSPEREPISODE,
				PerformanceMetric.AVERAGEEPISODEREWARD);

		exp.toggleVisualPlots(PLOTTER_ON);

		exp.startExperiment();

		exp.writeStepAndEpisodeDataToCSV("expData");
	}
}
