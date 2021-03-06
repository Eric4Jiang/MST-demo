import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Graph extends JPanel implements MouseListener {
    // Colors for animations
    public static Color DEFAULT_C = Color.BLACK,
                        HIGHLIGHT = Color.GREEN,
                        FINAL_C = Color.RED,
                        CURRENT_C = Color.BLUE;

    // Nodes on the graph
    public ArrayList<Node> nodes = new ArrayList<>();
    public Map nameToNode = new HashMap<String, Node>();

    // Edges on the graph
    public ArrayList<Edge> edges = new ArrayList<>();
    public Map<String, Edge> nameToEdge = new HashMap<>();
    public Node edge_node1;

    // MST animations variables (kruskal + prim)
    public String method = "kruskal";
    public SwingWorker kruskal = new Kruskal(this);
    public SwingWorker prim = new Prim(this);
    public boolean paused = true;

    // Searching animation variables (BFS + DFS)
    public SwingWorker BFS = new BreadthFirstSearch(this);
    public SwingWorker DFS = new DepthFirstSearch(this);
    public Node startNode = null;
    public Node desiredNode = null;

    // Used to easily determine what stage of our animation we're on.
    public int graph_state = -1; // -1 -> Do nothing
                                 //  0 -> Adding nodes
                                 //  1 -> Forming edges. 0 nodes selected
                                 //  2 -> Forming edges. 1 node selected
                                 //  3 -> Choosing animation algorithm
                                 //  4 -> Choose start node, BFS and DPS
                                 //  5 -> Choose ending node, BFS and DPS
                                 //  10 -> ANIMATION TIME

    public final int GRAPH_SIZE = 1000;

    public Graph() {
        this.addMouseListener(this);

        // init graph
        setLayout(null); // absolute layout
        setBackground(Color.WHITE);
        setPreferredSize(new Dimension(GRAPH_SIZE, GRAPH_SIZE)); // calls paintcomponent
    }

    public void setGraphState(int state) {
        System.out.println("state = " + state);
        // don't reset colors when forming edges
//        if (state != 2 && state != 5) { // edge_node1 != null) {
//            resetNodes();
//            resetEdges();
//        }
        this.graph_state = state;
    }

    public int getGraphState() {
        return this.graph_state;
    }

    public boolean isPaused() {
        return this.paused;
    }

    /**
     * Called every time a change is made to update graph
     *
     * @param g - graphics object
     */
    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
//        Graphics2D g2d = (Graphics2D) g;

        // draw edges
        ((Graphics2D) g).setStroke(new BasicStroke(10));
        for(Edge e : edges) {
            Node n1 = e.n1;
            Node n2 = e.n2;
            g.setColor(e.color);
            g.drawLine((int)n1.vertex.getX(), (int)n1.vertex.getY(), (int)n2.vertex.getX(), (int)n2.vertex.getY());
        }
    }

    public void refreshGraph() {
        repaint();
        revalidate();
    }

    // Set all nodes to default colors and not visited
    public void resetNodes() {
        for (Node n : nodes) {
            n.setColor(DEFAULT_C);
            n.setVisited(false);
        }
        refreshGraph();
    }

    // Set all Edges to default color
    public void resetEdges() {
        for (Edge e : edges) {
            e.setColor(DEFAULT_C);
        }
        refreshGraph();
    }

    public void addNode(Node n) {
        nameToNode.put(n.getName(), n);
        this.nodes.add(n);
        add(n);
        refreshGraph();
    }

    public void addEdge(Edge e) {
        nameToEdge.put(e.name, e);
        this.edges.add(e);
        refreshGraph();
    }

    /**
     * Handles node creation.
     * Once "Add Node" has been selected, a click anywhere on the graph
     * will spawn a node Jpanel there, and therefore prevent multiple nodes
     * from being at the same location.
     *
     * @param e - where mouse was clicked on graph
     */
    @Override
    public void mouseClicked(MouseEvent e) {
        if (graph_state == 0) {
            // create a node where mouse was clicked
            Node n = new Node(e.getX(), e.getY(), this);
            addNode(n);
        }
    }

    @Override
    public void mousePressed(MouseEvent e) {

    }

    @Override
    public void mouseReleased(MouseEvent e) {

    }

    @Override
    public void mouseEntered(MouseEvent e) {

    }

    @Override
    public void mouseExited(MouseEvent e) {

    }

    // ============================ Solving Graph =================================

    /**
     * @param n1 - name of first node
     * @param n2 - name of second node
     * @return - Instance of edge that connects the first and second node.
     *           If no edge found, return null.
     */
    public Edge findEdge(Node n1, Node n2) {
        String s1 = n1.getName();
        String s2 = n2.getName();
        String name1 = s1 + s2;
        String name2 = s2 + s1;

        if (!nameToEdge.containsKey(name1)) {
            return nameToEdge.get(name2);
        }

        return nameToEdge.get(name1);
    }

    /**
     * finds all nodes connected to n by an edge
     *
     * @param n - node to find edges for
     * @return - List of MyPair. MyPair contains a node and an edge,
     *           which will be directly connected to n.
     */
    public ArrayList<MyPair> adjacent(Node n) {
        ArrayList<MyPair> adj = new ArrayList<>();
        for(Edge e : edges) {
            if(e.n1.name.equals(n.name)) {
                adj.add(new MyPair(e.n2, e));
            } else if(e.n2.name.equals(n.name)) {
                adj.add(new MyPair(e.n1, e));
            }
        }
        for(MyPair p : adj) {
            System.out.println("Adjacent: " + p.edge.name);
        }

        return adj;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    /**
     * Starts an animation determined by <method>
     * <method> - the algorithm chosen by the user
     *
     * @return - List of edges that represent MST.
     */
    public void animateGraphAlgorithm() {
        if (edges.isEmpty())
            return;

        // Have to create new worker for every new animation
        if (this.method.equals("kruskal")) {
            kruskal = new Kruskal(this);
            kruskal.execute();
        } else if (this.method.equals("prim")) {
            prim = new Prim(this);
            prim.execute();
        } else if (this.method.equals("BFS")) {
            BFS = new BreadthFirstSearch(this);
            BFS.execute();
        } else if (this.method.equals("DFS")) {
            DFS = new DepthFirstSearch(this);
            DFS.execute();
        }
    }

    public void killAllAnimation() {
        kruskal.cancel(true);
        prim.cancel(true);
        BFS.cancel(true);
        DFS.cancel(true);
    }

    public void startAnimation() {
        this.paused = false;
    }

    public void stopAnimation() {
        this.paused = true;
    }

}
