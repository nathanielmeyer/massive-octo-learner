package edu.gatech.cs7641.assignment3.assets;

import java.util.Random;

import burlap.oomdp.auxiliary.DomainGenerator;
import burlap.oomdp.core.Attribute;
import burlap.oomdp.core.Attribute.AttributeType;
import burlap.oomdp.core.Domain;
import burlap.oomdp.core.ObjectClass;
import burlap.oomdp.core.ObjectInstance;
import burlap.oomdp.core.PropositionalFunction;
import burlap.oomdp.core.State;
import burlap.oomdp.core.Value;
import burlap.oomdp.singleagent.Action;
import burlap.oomdp.singleagent.GroundedAction;
import burlap.oomdp.singleagent.RewardFunction;
import burlap.oomdp.singleagent.SADomain;

public class AssetDomain implements DomainGenerator {

	private static final String PF_IS_HOLDING_ASSET = "PFIsHoldingAsset";

	private static final String SELL_ASSET = "sellAsset";
	private static final String PRICE_OF_ASSET = "priceOfAsset";
	private static final String HOLDING_ASSET = "holdingAsset";
	private static final String CASHBALANCE = "cashbalance";
	private static final String PORTFOLIO = "portfolio";
	private static final String MARKET = "market";
	private static final String CASH = "cash";

	public class HoldingAssetPF extends PropositionalFunction {
	
		private int assetIndex;
	
		public HoldingAssetPF(String name, Domain domain,
				String[] parameterClasses, int assetIndex) {
			super(name, domain, parameterClasses);
			this.assetIndex = assetIndex;
		}
	
		@Override
		public boolean isTrue(State s, String[] params) {
			return s.getObject(params[0]).getValues().get(assetIndex)
					.getRealVal() > 0;
		}
	
	}

	public class SellAction extends Action {

		private int assetIndex;

		public SellAction(Domain domain, String name, int assetIndex) {
			super(name, domain, "");
			this.assetIndex = assetIndex;
		}

		@Override
		protected State performActionHelper(State s, String[] params) {
			AssetDomain.this.sellAsset(s, assetIndex);
			return null;
		}

		@Override
		public boolean applicableInState(State s, String[] params) {
			PropositionalFunction pf = domain.getPropFunction(PF_IS_HOLDING_ASSET+assetIndex);
			return pf.isTrue(s, params);
		}

	}

	private int assets;

	public AssetDomain(int numAssets) {
		this.assets = numAssets;
	}

	public boolean isHoldingAsset(State s, int assetIndex) {
		return s.getObjectsOfTrueClass(PORTFOLIO).get(0).getValues()
				.get(assetIndex).getRealVal() > 0;
	}

	public void sellAsset(State s, int assetIndex) {
		ObjectInstance portfolio = s.getObjectsOfTrueClass(PORTFOLIO).get(0);
		ObjectInstance market = s.getObjectsOfTrueClass(MARKET).get(0);
		ObjectInstance cash = s.getObjectsOfTrueClass(CASH).get(0);
		double quantity = portfolio.getValues().get(assetIndex).getRealVal();
		double price = market.getValues().get(assetIndex).getRealVal();
		double balance = cash.getValues().get(0).getRealVal();
		cash.getValues().get(0).setValue(balance + price * quantity);
		portfolio.getValues().get(assetIndex).setValue(0d);
	}

	public Domain generateDomain() {
		Domain domain = new SADomain();
		ObjectClass portfolio = new ObjectClass(domain, PORTFOLIO, false);
		ObjectClass market = new ObjectClass(domain, MARKET, false);
		ObjectClass cash = new ObjectClass(domain, CASH, false);
		cash.addAttribute(new Attribute(domain, CASHBALANCE,
				AttributeType.REALUNBOUND));
		for (int i = 0; i < assets; i++) {
			portfolio.addAttribute(new Attribute(domain, HOLDING_ASSET + i,
					AttributeType.BOOLEAN));
			Attribute price = new Attribute(domain, PRICE_OF_ASSET + i,
					AttributeType.REAL);
			price.setLims(0, Double.MAX_VALUE);
			market.addAttribute(price);
			Action sellAction = new AssetDomain.SellAction(domain, SELL_ASSET
					+ i, i);
			domain.addAction(sellAction);
			PropositionalFunction pf = new AssetDomain.HoldingAssetPF(
					PF_IS_HOLDING_ASSET + i, domain, new String[] { PORTFOLIO }, i);
			domain.addPropositionalFunction(pf);
		}
		domain.addObjectClass(portfolio);
		domain.addObjectClass(market);
		domain.addObjectClass(cash);
		return domain;
	}

	public State getHoldingAllAssetsState(Domain domain) {
		State s = new State();
		ObjectInstance portfolio = new ObjectInstance(domain.getObjectClass(PORTFOLIO),PORTFOLIO+0);
		for(Value v:portfolio.getValues()) v.setValue(1);
		s.addObject(portfolio);
		ObjectInstance market = new ObjectInstance(domain.getObjectClass(MARKET),MARKET+0);
		marketInitialize(market);
		s.addObject(market);
		ObjectInstance cash = new ObjectInstance(domain.getObjectClass(CASH),CASH+0);
		for(Value v:cash.getValues()) v.setValue(0);
		s.addObject(cash);
		return s;
	}

	private void marketInitialize(ObjectInstance market) {
		Random r = new Random();
		for(Value v:market.getValues()) v.setValue(r.nextDouble()*10);
	}
	
	private void marketUpdate(ObjectInstance market) {
		for(Value v:market.getValues()) v.setValue(marketUpdate(v));
	}

	private String marketUpdate(Value v) {
		Random r = new Random();
		v.setValue(v.getRealVal()*(1+(r.nextDouble()-0.5)));
		return null;
	}

}
