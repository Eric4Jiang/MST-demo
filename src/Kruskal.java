import javax.lang.model.type.NullType;
import javax.swing.*;
import java.util.*;
import java.util.List;

public class Kruskal extends SwingWorker<Boolean, NullType>{

    Graph graph;

    public int SLEEP_TIME = 1000;

    public Map<String, String> PARENT;
    public Map<String, Integer> RANK;

    public ArrayList<Edge> MST;

    Kruskal(Graph graph) {
        this.graph = graph;

        PARENT = new HashMap<>();
        RANK = new HashMap<>();
        MST = new ArrayList<>();

        if (this.graph.nodes.size() > 10) {
            this.SLEEP_TIME /= 2;
        }
    }

    /**
     * Called when this class is executed
     *
     * Finds MST via Kruskal's algorithm.
     * Continuously appends the lowest weight edges onto distinct forests (groups of vertices and edges).
     * If an edge is connecting two distinct forests, combine the forests.
     * Repeat until there is one distinct forest that contains all the vertices with no cycles
     *
     * @return - Minimum Spanning tree of graph
     */
    @Override
    protected Boolean doInBackground() throws Exception {
        ArrayList<Edge> Edges = graph.edges;
        ArrayList<Node> Nodes = graph.nodes;

        Collections.sort(Edges);

        for (Node n : Nodes) {
            PARENT.put(n.name, n.name);
            RANK.put(n.name, 0);
        }
        for (Edge e : Edges) {
            // gui paused
            while(graph.isPaused()) {
                Thread.sleep(1);
            }

            // pauses gui to highlight current edge
            e.setColor(Graph.HIGHLIGHT);
            publish();
            Thread.sleep(SLEEP_TIME);

            String root1 = Find(e.n1.name);
            String root2 = Find(e.n2.name);

            // found tree with different root connected by Edge e.
            if (root1 != root2) {
                MST.add(e);
                Union(root1, root2);

                // highlights edge as part of MST
                e.setColor(Graph.FINAL_C);
                e.n1.setColor(Graph.FINAL_C);
                e.n2.setColor(Graph.FINAL_C);
                publish();
            } else {
                // edge is not part of MST
                e.setColor(Graph.DEFAULT_C);
                publish();
            }
            Thread.sleep(SLEEP_TIME);
        }

        return true;
    }

    /**
     * Called every time publish() is called.
     * Can safely update GUI here.
     * Just refreshes the graph. The edge color is set beforehand
     *
     * @param chunks - Edge that is being processed
     */
    @Override
    protected void process(List<NullType> chunks) {
        graph.refreshGraph();
    }

    @Override
    protected void done() {

    }

    // finds the root of a vertex by traversing through
    private String Find(String node) {
        if(PARENT.get(node).equals(node)) //found root
            return PARENT.get(node);
        else
            return Find(PARENT.get(node)); // traverse list to find parent of tree
    }

    // Combine the two distinct forests by setting assigning
    // the larger tree's root as the smaller tree's new root.
    private void Union(String root1, String root2) {
        if(RANK.get(root1) > RANK.get(root2)) {
            PARENT.put(root2, PARENT.get(root1));
        } else if(RANK.get(root1) < RANK.get(root2)) {
            PARENT.put(root1, PARENT.get(root2));
        } else {
            PARENT.put(root1, PARENT.get(root2));
            RANK.put(root1, RANK.get(root1) + 1);
        }
    }

    public ArrayList<Edge> getMST() {
        return this.MST;
    }

}
