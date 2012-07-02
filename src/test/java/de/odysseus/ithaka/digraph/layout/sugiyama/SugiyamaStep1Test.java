/*
 * Copyright 2008 Odysseus Software GmbH, Frankfurt am Main/Germany.
 */
package de.odysseus.ithaka.digraph.layout.sugiyama;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import de.odysseus.ithaka.digraph.Digraph;
import de.odysseus.ithaka.digraph.Digraphs;
import de.odysseus.ithaka.digraph.SimpleDigraph;
import de.odysseus.ithaka.digraph.SimpleDigraphAdapter;
import de.odysseus.ithaka.digraph.layout.LayoutDimension;
import de.odysseus.ithaka.digraph.layout.LayoutDimensionProvider;
import de.odysseus.ithaka.digraph.layout.sugiyama.SugiyamaArc;
import de.odysseus.ithaka.digraph.layout.sugiyama.SugiyamaNode;
import de.odysseus.ithaka.digraph.layout.sugiyama.SugiyamaStep1;

public class SugiyamaStep1Test {
	private Digraph<Integer,Boolean> empty = Digraphs.<Integer,Boolean>emptyDigraph();

	private LayoutDimensionProvider<Integer> dim = new LayoutDimensionProvider<Integer>() {
		@Override
		public LayoutDimension getDimension(Integer node) {
			return new LayoutDimension(String.valueOf(node).length(), 1);
		}
	};

	private Map<Integer,SugiyamaNode<Integer>> map(Digraph<SugiyamaNode<Integer>, SugiyamaArc<Integer,Boolean>> sugiyama) {
		Map<Integer,SugiyamaNode<Integer>> map = new HashMap<Integer, SugiyamaNode<Integer>>();
		for (SugiyamaNode<Integer> sugiyamaNode : sugiyama.vertices()) {
			map.put(sugiyamaNode.getVertex(), sugiyamaNode);
		}
		return map;
	}

	@Test public void testNodesAndEdges() {
		SimpleDigraph<Integer> dag = new SimpleDigraphAdapter<Integer>();
		dag.add(1, 2);
		dag.add(3);
		Digraph<SugiyamaNode<Integer>, SugiyamaArc<Integer,Boolean>> sugiyama =
			new SugiyamaStep1<Integer,Boolean>().createLayoutGraph(dag, dim, empty, 0);
		Map<Integer,SugiyamaNode<Integer>> map = map(sugiyama);
		assertEquals(3, map.size());
		assertTrue(map.keySet().contains(1));
		assertTrue(map.keySet().contains(2));
		assertTrue(map.keySet().contains(3));

		SugiyamaNode<Integer> node = map.get(1);
		assertNotNull(node);
		assertEquals(dim.getDimension(1), node.getDimension());
		assertFalse(node.isDummy());

		SugiyamaArc<Integer,Boolean> edge = sugiyama.get(map.get(1), map.get(2));
		assertNotNull(edge);
		assertSame(map.get(1), edge.getSource());
		assertSame(map.get(2), edge.getTarget());
		assertTrue(edge.getEdge().booleanValue());
		assertFalse(edge.isFeedback());
	}

	@Test public void testLayers111() {
		SimpleDigraph<Integer> dag = new SimpleDigraphAdapter<Integer>();
		dag.add(1, 2);
		dag.add(2, 3);
		Map<Integer,SugiyamaNode<Integer>> map =
			map(new SugiyamaStep1<Integer,Boolean>().createLayoutGraph(dag, dim, empty, 0));
		assertEquals(0, map.get(1).getLayer());
		assertEquals(1, map.get(2).getLayer());
		assertEquals(2, map.get(3).getLayer());
	}

	@Test public void testLayers120() {
		SimpleDigraph<Integer> dag = new SimpleDigraphAdapter<Integer>();
		dag.add(1, 2);
		dag.add(1, 3);
		Map<Integer,SugiyamaNode<Integer>> map =
			map(new SugiyamaStep1<Integer,Boolean>().createLayoutGraph(dag, dim, empty, 0));
		assertEquals(0, map.get(1).getLayer());
		assertEquals(1, map.get(2).getLayer());
		assertEquals(1, map.get(3).getLayer());
	}

	@Test public void testLayers210() {
		SimpleDigraph<Integer> dag = new SimpleDigraphAdapter<Integer>();
		dag.add(1, 2);
		dag.add(3, 2);
		Map<Integer,SugiyamaNode<Integer>> map =
			map(new SugiyamaStep1<Integer,Boolean>().createLayoutGraph(dag, dim, empty, 0));
		assertEquals(0, map.get(1).getLayer());
		assertEquals(1, map.get(2).getLayer());
		assertEquals(0, map.get(3).getLayer());
	}

	@Test public void testComponents() {
		SimpleDigraph<Integer> dag = new SimpleDigraphAdapter<Integer>();
		dag.add(1, 2);
		dag.add(1, 3);

		dag.add(4, 5);
		dag.add(6, 5);
		Map<Integer,SugiyamaNode<Integer>> map =
			map(new SugiyamaStep1<Integer,Boolean>().createLayoutGraph(dag, dim, empty, 0));
		assertEquals(0, map.get(1).getLayer());
		assertEquals(1, map.get(2).getLayer());
		assertEquals(1, map.get(3).getLayer());

		assertEquals(0, map.get(4).getLayer());
		assertEquals(1, map.get(5).getLayer());
		assertEquals(0, map.get(6).getLayer());
	}
}
