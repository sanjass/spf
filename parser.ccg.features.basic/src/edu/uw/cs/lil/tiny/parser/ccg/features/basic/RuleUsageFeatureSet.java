/*******************************************************************************
 * UW SPF - The University of Washington Semantic Parsing Framework
 * <p>
 * Copyright (C) 2013 Yoav Artzi
 * <p>
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or any later version.
 * <p>
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * <p>
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 51
 * Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 ******************************************************************************/
package edu.uw.cs.lil.tiny.parser.ccg.features.basic;

import java.util.LinkedList;
import java.util.List;

import edu.uw.cs.lil.tiny.data.IDataItem;
import edu.uw.cs.lil.tiny.explat.IResourceRepository;
import edu.uw.cs.lil.tiny.explat.ParameterizedExperiment.Parameters;
import edu.uw.cs.lil.tiny.explat.resources.IResourceObjectCreator;
import edu.uw.cs.lil.tiny.explat.resources.usage.ResourceUsage;
import edu.uw.cs.lil.tiny.parser.ccg.IParseStep;
import edu.uw.cs.lil.tiny.parser.ccg.model.parse.IParseFeatureSet;
import edu.uw.cs.lil.tiny.utils.hashvector.HashVectorFactory;
import edu.uw.cs.lil.tiny.utils.hashvector.IHashVector;
import edu.uw.cs.lil.tiny.utils.hashvector.IHashVectorImmutable;
import edu.uw.cs.lil.tiny.utils.hashvector.KeyArgs;
import edu.uw.cs.utils.composites.Pair;
import edu.uw.cs.utils.composites.Triplet;

/**
 * Computes features over using type-shifting rules.
 * 
 * @author Yoav Artzi
 * @param <DI>
 *            Data item for inference.
 * @param <MR>
 *            Meaning representation.
 */
public class RuleUsageFeatureSet<DI extends IDataItem<?>, MR> implements
		IParseFeatureSet<DI, MR> {
	
	private static final String	FEATURE_TAG			= "RULE";
	
	private static final long	serialVersionUID	= -2924052883973590335L;
	private final double		scale;
	
	public RuleUsageFeatureSet(double scale) {
		this.scale = scale;
	}
	
	@Override
	public List<Triplet<KeyArgs, Double, String>> getFeatureWeights(
			IHashVector theta) {
		final List<Triplet<KeyArgs, Double, String>> weights = new LinkedList<Triplet<KeyArgs, Double, String>>();
		for (final Pair<KeyArgs, Double> feature : theta.getAll(FEATURE_TAG)) {
			weights.add(Triplet.of(feature.first(), feature.second(),
					(String) null));
		}
		return weights;
	}
	
	@Override
	public boolean isValidWeightVector(IHashVectorImmutable update) {
		// No protected features
		return true;
	}
	
	@Override
	public double score(IParseStep<MR> obj, IHashVector theta, DI dataItem) {
		return setFeats(obj.getRuleName(), HashVectorFactory.create())
				.vectorMultiply(theta);
		
	}
	
	@Override
	public void setFeats(IParseStep<MR> obj, IHashVector feats, DI dataItem) {
		setFeats(obj.getRuleName(), feats);
		
	}
	
	private IHashVectorImmutable setFeats(String ruleName, IHashVector features) {
		if (ruleName.startsWith("shift")) {
			features.set(FEATURE_TAG, ruleName,
					features.get(FEATURE_TAG, ruleName) + 1.0 * scale);
		}
		return features;
	}
	
	public static class Creator<DI extends IDataItem<?>, MR> implements
			IResourceObjectCreator<RuleUsageFeatureSet<DI, MR>> {
		
		@Override
		public RuleUsageFeatureSet<DI, MR> create(Parameters params,
				IResourceRepository repo) {
			return new RuleUsageFeatureSet<DI, MR>(
					params.contains("scale") ? Double.valueOf(params
							.get("scale")) : 1.0);
		}
		
		@Override
		public String type() {
			return "feat.rules.count";
		}
		
		@Override
		public ResourceUsage usage() {
			return new ResourceUsage.Builder(type(), RuleUsageFeatureSet.class)
					.setDescription(
							"Feature set that provides features that count the number of times type-shifting rules are used. Feature tag: RULE.")
					.addParam("scale", "double",
							"Scaling factor for scorer output").build();
		}
		
	}
	
}
