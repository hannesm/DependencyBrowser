import java.util.ArrayList;

import y.base.Node;


public final class Commands {

	public static void processCommand(Symbol comm, ArrayList answer, Layouter graph) {
		System.out.println("executing " + answer);
		if (comm.isEqual("new-nodes"))
			newnodes(answer, graph);
		if (comm.isEqual("new-edges"))
			newedges(answer, graph);
		if (comm.isEqual("new-nodes-with-edge"))
			newnodeswithedge(answer, graph);
	}

	private static void newnodes (ArrayList answer, Layouter graph) {
		assert(answer.size() == 2);
		assert(answer.get(1) instanceof ArrayList);
		ArrayList nodes = (ArrayList)answer.get(1);
		for (Object o : nodes) {
			assert(o instanceof String);
			graph.insertNode((String)o);
		}
	}
	
	private static void newedges (ArrayList answer, Layouter graph) {
		assert(answer.size() == 3);
		assert(answer.get(1) instanceof String);		
		assert(answer.get(2) instanceof ArrayList);
		String from = (String)answer.get(1);
		ArrayList nodes = (ArrayList)answer.get(2);
		for (Object o : nodes) {
			assert(o instanceof String);
			graph.insertEdge(from, (String)o);
		}
	}
	
	private static void newnodeswithedge (ArrayList answer, Layouter graph) {
		assert(answer.size() == 3);
		assert(answer.get(1) instanceof String);		
		assert(answer.get(2) instanceof ArrayList);
		for (Object o : (ArrayList)answer.get(2)) {
			assert(o instanceof String);
			graph.insertNode((String)o);
		}
		newedges(answer, graph);
	}
}
