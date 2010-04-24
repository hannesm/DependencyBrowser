import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;

import y.base.EdgeCursor;
import y.base.Node;
import y.base.NodeCursor;
import y.layout.circular.CircularLayouter;
import y.layout.organic.SmartOrganicLayouter;
import y.view.Arrow;
import y.view.EdgeRealizer;
import y.view.Graph2D;
import y.view.LineType;
import y.view.NodeRealizer;


public class Layouter {
	protected Graph2D graph;
	private HashMap<String, Node> sym_node = new HashMap<String, Node>();
	private HashMap<String, Boolean> node_present = new HashMap<String, Boolean>();
	private HashMap<String, ArrayList<String>> to_edges = new HashMap<String, ArrayList<String>>();
	private HashMap<String, ArrayList<String>> from_edges = new HashMap<String, ArrayList<String>>();	
	protected CircularLayouter circular;
	private DemoBase db;
	public boolean change;
	
	public Layouter (DemoBase d) {
		graph = new Graph2D();
		circular = new CircularLayouter();
		circular.setLayoutStyle(CircularLayouter.BCC_COMPACT);
		circular.setPartitionLayoutStyle(CircularLayouter.PARTITION_LAYOUTSTYLE_DISK);
		db = d;
	}
	
	protected String text (Node n) {
		NodeRealizer nr = graph.getRealizer(n);
		return nr.getLabelText();
	}
	
	private ArrayList<String> fromE (Node n) {
		if (! from_edges.containsKey(text(n)) || from_edges.get(text(n)) == null)
			from_edges.put(text(n), new ArrayList<String>());
		return from_edges.get(text(n));
	}
	
	private ArrayList<String> toE (Node n) {
		if (! to_edges.containsKey(text(n)) || to_edges.get(text(n)) == null)
			to_edges.put(text(n), new ArrayList<String>());
		return to_edges.get(text(n));
	}

	
	public Node ensureNode (String s) {
		if (! node_present.get(s)) {
			Node n = graph.createNode();
			change = true;
			NodeRealizer real = graph.getRealizer(n);
			real.setLabelText(s);
			real.setWidth(real.getLabel().getWidth() + 2 * 20);
			sym_node.put(s, n);
			node_present.put(s, true);
			for (String t : toE(n))
				if (sym_node.containsKey(t) && sym_node.get(t) != null) {
					graph.createEdge(sym_node.get(t), n);
					fromE(sym_node.get(t)).remove(text(n));
				}
			to_edges.remove(s);
			for (String t : fromE(n))
				if (sym_node.containsKey(t) && sym_node.get(t) != null) {
					graph.createEdge(n, sym_node.get(t));
					toE(sym_node.get(t)).remove(text(n));
				}
			from_edges.remove(s);
		}
		return sym_node.get(s);
	}
	
	public void insertNode (String s) {
		assert(sym_node.containsKey(s) == false);
		node_present.put(s, false);
	}
	
	public void insertEdge (String s, String e) {
		Node st = ensureNode(s);
		assert(st != null);
		Node en = ensureNode(e);
		assert(en != null);
		graph.createEdge(st, en);
		EdgeRealizer er = graph.getRealizer(graph.lastEdge());
		er.setArrow(Arrow.DELTA);
		change = true;
	}

	public void select (Node n) {
		graph.setSelected(n, true);
		for (EdgeCursor ec = n.edges(); ec.ok(); ec.next()) {
			graph.getRealizer(ec.edge()).setLineType(LineType.LINE_2);
			graph.getRealizer(ec.edge().opposite(n)).setFillColor(Color.DARK_GRAY);
		}
		db.view.repaint();
	}
	
	public Node getSelected () {
		if (graph.selectedNodes().size() == 1)
			return graph.selectedNodes().node();
		return null;
	}
	
	public void unselectNeighbours () {
		for (NodeCursor nc = graph.selectedNodes(); nc.ok(); nc.next()) {
			Node n = nc.node();
			for (EdgeCursor ec = n.edges(); ec.ok(); ec.next()) {
				graph.getRealizer(ec.edge()).setLineType(LineType.LINE_1);
				graph.getRealizer(ec.edge().opposite(n)).setFillColor(graph.getDefaultNodeRealizer().getFillColor());
			}
		}
	}

	public void unselect () {
		unselectNeighbours();
		for (NodeCursor nc = graph.selectedNodes(); nc.ok(); nc.next()) {
			Node n = nc.node();
			graph.setSelected(n, false);
		}
		db.view.repaint();
	}

	public void findSubString (String search) {
		ArrayList<Node> selection = new ArrayList<Node>();
		for (String s : sym_node.keySet())
			if (search.charAt(0) == '^') {
				if (s.startsWith(search.substring(1)))
					selection.add(sym_node.get(s));
			} else if (search.charAt(search.length() - 1) == '$') {
				if (s.endsWith(search.substring(0, search.length() - 1)))
					selection.add(sym_node.get(s));
			} else if (s.contains(search))
				selection.add(sym_node.get(s));
		unselect();
		for (Node n : selection)
			graph.setSelected(n, true);
	}
	
	public void getInitial() {
		db.receive("edges", "dependency-browser");
	/*	for (String s : node_present.keySet())
			if (! s.contains("test"))
				if (s.contains("dfmc") && !s.contains("dfmc-projects"))
					db.receive("edges", s); */
	}
	
	public void hide (Node n) {
		assert(node_present.get(text(n)) == true);
		ArrayList<String> toedges = toE(n);
		for (EdgeCursor ec = n.inEdges(); ec.ok(); ec.next()) {
			toedges.add(text(ec.edge().source()));
			fromE(ec.edge().source()).add(text(n));
		}
		ArrayList<String> fromedges = fromE(n);
		for (EdgeCursor ec = n.outEdges(); ec.ok(); ec.next()) {
			fromedges.add(text(ec.edge().target()));
			toE(ec.edge().target()).add(text(n));
		}
		sym_node.remove(text(n));
		node_present.put(text(n), false);
		graph.removeNode(n);
	}
	
	public ArrayList<String> getExtensions (Node n) {
		ArrayList<String> res = new ArrayList<String>();
		if (from_edges.containsKey(text(n)) && from_edges.get(text(n)) != null)
			res.addAll(from_edges.get(text(n)));
		if (to_edges.containsKey(text(n)) && to_edges.get(text(n)) != null)
			res.addAll(to_edges.get(text(n)));
		return res;
	}
}
