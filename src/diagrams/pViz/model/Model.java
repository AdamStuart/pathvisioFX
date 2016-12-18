package diagrams.pViz.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import diagrams.pViz.app.Controller;
import diagrams.pViz.view.Pasteboard;
import diagrams.pViz.view.VNode;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Polyline;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;
import javafx.scene.text.Text;
import model.AttributeMap;
import model.bio.BiopaxRef;
import model.bio.Gene;
import model.bio.GeneList;
import model.bio.Species;
import util.StringUtil;

public class Model
{
/*
 *  Model - the set of record lists	
 *  
 *  We need to keep track of species, nodes, edges, genelists, pathways, references, comments
 */
	private Controller controller;
	public Controller getController() { return controller; } 
	private Map<String, MNode> resourceMap = FXCollections.observableHashMap();
	private int nodeCounter = 0;
	private List<Edge> edgeTable = FXCollections.observableArrayList();
	private Map<String, Shape> shapes = new HashMap<String,Shape>();
	public Map<String, Shape> getShapes()	{ return shapes; }
	public Map<String, MNode> getResourceMap() {		return resourceMap;	}
	public Shape findShape(String s ) 	{ return shapes.get(s);	}
	String title = "PathVisio Mockup";
	public void setTitle(String val) {		title = val;		}
	public String getTitle() 		{		return title;		}

	public Model(Controller ct)
	{
		controller = ct;
	}
	// **-------------------------------------------------------------------------------
	public String saveState()
	{
		String header = docHeader();
		StringBuilder saver = new StringBuilder(header);
		serializeNodes(saver);
		serializeReferences(saver);
		serializeEdges(saver);
		serializeComments(saver);
		return saver.toString();
	}
	
	private void serializeNodes(StringBuilder saver) {
	}
	private void serializeReferences(StringBuilder saver) {
	}
	private void serializeEdges(StringBuilder saver) {
	}
	private void serializeShapes(StringBuilder saver) {
	}
	private void serializeComments(StringBuilder saver) {
	}
	private String docHeader() {
		return "Header";
	}
	//---------------------------------------------------------
	public void setState(String s)
	{
//		readNodes(s.);
//		readReferences(saver);
//		readEdges(saver);
//		readComments(saver);
	}
	// **-------------------------------------------------------------------------------
	
	List<String> comments = new ArrayList<String>();
	public void addComment(String key, String val) {		comments.add(key + ": " + val);	}
	public void clearComments() {		comments.clear();			}
	public String getComments() {
		StringBuilder b = new StringBuilder();
		for (String c : comments)
			b.append(c).append("\n");
		return b.toString();
	}
	private void readComments(String state) {
	}
	// **-------------------------------------------------------------------------------
	ObservableList<Gene> genes = FXCollections.observableArrayList();
	public void addGene(Gene g) 		{		genes.add(g);	}
	public void clearGenes() 			{		genes.clear();	}
	public List<Gene> getGenes() 		{		return genes;	}
	public GeneList getGeneList() 		{		return new GeneList(genes, getSpecies());	}

	// **-------------------------------------------------------------------------------
	ObservableList<BiopaxRef> references = FXCollections.observableArrayList();
	public void addRef(BiopaxRef ref) 	{	if (!refExists(ref))			references.add(ref);		}
	private boolean refExists(BiopaxRef ref) {
		String id = ref.getId();
		if (id == null) return false;
		for (BiopaxRef r : references)
			if (id.equals(r.getId())) return true;
		return false;
	}
	public int getNReferences() 		{	return references.size();	}
	public BiopaxRef getReference(int i) {	return references.get(i);	}
	public void clearRefs() 			{	references.clear();	}
	public List<BiopaxRef> getReferences() { return references;	}
	private void readReferences(String state) {
	}
	// **-------------------------------------------------------------------------------
	public void addResource(MNode mnode)		
	{  
		if (mnode != null) 
			addResource(mnode.getId(), mnode);
	}
	public void addResource(String key, MNode n)		
	{  
		if (resourceMap.get(key) == null)
			resourceMap.put(key, n);
	}

	private void readNodes(String state) {
	}
	// **-------------------------------------------------------------------------------
	public Edge addEdge(MNode start, MNode end)		
	{  
		Edge edge = new Edge(start.getStack(), end.getStack(), null, null, null);
		addEdge(edge);
		return edge;
	}
	
	public void addEdge(Edge e)			{  	edgeTable.add(e);	}
	public void setAnchorVisibility(boolean visible)			
	{ 
		for (Edge e : edgeTable)
			e.getEdgeLine().setAnchorVis(visible);	
	}
	private void readEdges(String state) {
	}
	// **-------------------------------------------------------------------------------
	public List<Edge> connectSelectedNodes()		
	{  
		List<Edge> edges = new ArrayList<Edge>();
		List<VNode> selection = controller.getSelection();
		for (int i=0; i<selection.size()-1; i++)
		{
			VNode start = selection.get(i);
			if (start.getShape() instanceof Line) continue;
			for (int j=i+1; j < selection.size(); j++)
			{
				VNode end = selection.get(j);
				if (end.getShape() instanceof Line) continue;
				edges.add(addEdge( start.getModel(), end.getModel()));
			}
		}
		return edges;
	}
	public void removeEdge(Edge edge)			{  		edgeTable.remove(edge);	}
	public void connectAllEdges() {
		for (int z = edgeTable.size()-1; z >= 0; z--)
		{
			Edge e = edgeTable.get(z);
			e.connect(true);
			e.connect(false);
		}
	}
	
//	EventListener connector = 
//	
	public void removeEdges(Node node)		
	{  
		for (int z = edgeTable.size()-1; z >= 0; z--)
		{
			Edge e = edgeTable.get(z);
			if (e.isStart(node) || e.isEnd(node))
			{
				e.removeListeners();
				e.getEdgeLine().dispose();
				edgeTable.remove(e);
			}
		}
//		List<Edge> okEdges = edgeTable.stream().filter(new TouchingNodeFilter(node)).collect(Collectors.toList());
//		edgeTable.clear();
//		edgeTable.addAll(okEdges);
	}
	// **-------------------------------------------------------------------------------

	public static class TouchingNodeFilter implements Predicate<Edge>
	{
		Node node;
		TouchingNodeFilter(Node n)				{	node = n;		}
		@Override public boolean test(Edge e)	{	return (!e.isStart(node) && !e.isEnd(node));	}
	}
	
	public void removeNode(VNode node)		
	{  
		if (node != null && ! "Marquee".equals(node.getId()))
			removeEdges(node);
	}

	public MNode getResourceByKey(String key)				
	{
		 if (key == null) return null;
		 for (MNode n : resourceMap.values())
		 {
			 String name = "" +n.getAttributeMap().get("TextLabel");
			if (name.equals(key)) return n;
		 }
		 MNode n = resourceMap.get(key);	
		 return n;
	}

	public MNode getResource(String key)				
	{
		 if (key == null) return null;
		 if (key.startsWith("\""))  
		 {
			 int len = key.length();
			 key = key.substring(1,len-1);
		 }
		 MNode n = resourceMap.get(key);	
		 return n;
	}
	
	public String cloneResourceId(String oldId)
	{
		return gensym(oldId.substring(0,1));
	}
	
	
// **-------------------------------------------------------------------------------
	static String XMLHEAD = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n";
	static String GraphicsHEAD = "<Graphics BoardWidth=\"%d\" BoardHeight=\"%d\" />\n";
	public StringBuilder traverseSceneGraph(Pane root)
	{
		StringBuilder buff = new StringBuilder(XMLHEAD);
		buff.append("<Pathway>\n");
//		Pasteboard board  = controller.getPasteboard();
		int width = (int) root.getWidth();
		int height = (int) root.getHeight();
		Bounds b = root.getLayoutBounds();
		width = (int) b.getWidth();
		height = (int)  b.getHeight();
		b = root.getBoundsInParent();
		width = (int) b.getWidth();
		height = (int)  b.getHeight();

		buff.append(String.format(GraphicsHEAD,	width, height));
		traverse(buff, root, 0);
		for (Edge e : edgeTable)
			buff.append(e.toString() + "\n");
		buff.append("</Pathway>\n");
		return buff;
	}
	
	static private void traverse(StringBuilder buff, Node node, int indent)
	{
//		VNode stack = node.getStack();
		if (Pasteboard.isMarquee(node)) return;
//		if (node instanceof Edge)			buff.append(describe(node));	
		if (node instanceof Shape)			buff.append(StringUtil.spaces(indent) + describe(node) + "\n");	
		if (node instanceof StackPane)		buff.append(StringUtil.spaces(indent) + describe(node) + "\n");
		if (node instanceof Parent)
			for (Node n : ((Parent) node).getChildrenUnmodifiable())
			{
				String id = n.getId();
				if (id == null)					continue;			// only propagate thru nodes with ids
				if ("Marquee".equals(id) )		continue;
				
				if (n instanceof Text)
				{
					String txt = ((Text) n).getText();
					if (txt.length() < 1) 	continue;				//System.out.println("Don't stream empty text");
				}
				traverse(buff, n, indent+1);
			}
	}
	// **-------------------------------------------------------------------------------
	static public String describe(MNode node)	{	return node.getStack().asGPML();	}
	static public String describe(Node node)	{	return node.getClass().getSimpleName() + ": " + node.getId() + " " +
				StringUtil.asString(node.getBoundsInParent());	}
	static String getBoundsString(double x, double y, double w, double h)	{
	 return String.format("x=%.1f, y=%.1f, width=%.1f, height=%.1f", x, y, w, h);
	}
	public String gensym(String prefix)	{		return (prefix == null ? "" : prefix ) + ++nodeCounter;	}
	
	
	int verbose = 0;
	public void setAttributes(Node shape, AttributeMap map)
	{
		if (verbose>0) System.out.println(map.toString());
		for (String k : map.keySet())
		{
			String val = map.get(k);
			if (k.equals("GraphId"))			shape.setId(val);
			double d = StringUtil.toDouble(val);			// exception safe:  comes back NaN if val is not a number
			if (shape instanceof Rectangle)
			{
				Rectangle r = (Rectangle) shape;
				if (k.equals("x"))				r.setX(d);
				else if (k.equals("y"))			r.setY(d);
				else if (k.equals("width"))		r.setWidth(d);
				else if (k.equals("height"))	r.setHeight(d);
			}
			if (shape instanceof Circle)
			{
				Circle circ = (Circle) shape;
				if (k.equals("centerX"))		circ.setCenterX(d);
				else if (k.equals("centerY"))	circ.setCenterY(d);
				else if (k.equals("radius"))	circ.setRadius(d);
			}
			if (shape instanceof Polygon)
			{
				Polygon poly = (Polygon) shape;
				if (k.equals("points"))			parsePolygonPoints(poly, map.get(k));
			}
			if (shape instanceof Polyline)
			{
				Polyline poly = (Polyline) shape;
				if (k.equals("points"))			parsePolylinePoints(poly, map.get(k));
			}
			if (shape instanceof Line)
			{
				Line line = (Line) shape;
				if (k.equals("startX"))			line.setStartX(d);
				else if (k.equals("startY"))	line.setStartY(d);
				else if (k.equals("endX"))		line.setEndX(d);
				else if (k.equals("endY"))		line.setEndY(d);
			}
			if (shape instanceof StackPane)
			{
				StackPane r = (StackPane) shape;
				if (k.equals("x"))				r.setLayoutX(d);
				else if (k.equals("y"))			r.setLayoutY(d);
				else if (k.equals("width"))		{ r.setMinWidth(d); r.setMaxWidth(d); r.prefWidth(d); }
				else if (k.equals("height"))	{ r.setMinHeight(d); r.setMaxHeight(d); r.prefHeight(d); }
				else if (k.equals("rotate"))	{ r.setRotate(d); }
				else if (k.equals("fill"))				
				{
					Background b = new Background(new BackgroundFill(Color.web(val), CornerRadii.EMPTY, Insets.EMPTY));
					r.setBackground(b);
				}
			}
			if (shape instanceof Shape)
			try
			{
				Shape sh = (Shape) shape;
				if (k.equals("fill") || k.equals("-fx-fill"))				
				{
					sh.setFill(Color.web(val));
					String lastTwoChars = val.substring(val.length()-2);
					int opac = Integer.parseInt(lastTwoChars, 16);
					shape.setOpacity(opac / 255.);
				}
				else if (k.equals("stroke")  || k.equals("-fx-stroke"))		sh.setStroke(Color.web(val));
				else if (k.equals("strokeWidth")  || k.equals("-fx-stroke-width"))	sh.setStrokeWidth(d);
//				else if (k.equals("selected"))		shape.setSelected(val);
			}
			catch (Exception e) { System.err.println("Parse errors: " + k); }
		}	
	}
	// **-------------------------------------------------------------------------------
	// Polygons and polylines are stored the same, but have different base types
	private void parsePolygonPoints(Polygon poly, String string)
	{
		parsePolyPoints(poly.getPoints(), string);
	}	
	private void parsePolylinePoints(Polyline poly, String string)
	{
		parsePolyPoints(poly.getPoints(), string);
	}	
	private void parsePolyPoints(ObservableList<Double> pts, String string)
	{
		String s = string.trim();
		s = s.substring(1, s.length()-1);
		String[] doubles = s.split(",");
		for (String d : doubles)
			pts.add(StringUtil.toDouble(d));
	}
	//-------------------------------------------------------------------------
	public void dumpNodeTable() {
		for (String key : resourceMap.keySet())
		{
			String s;
			MNode node = resourceMap.get(key);
			if (node != null)
			{
				s=  String.format("%s \t(%4.1f, %4.1f) \t %s ", node.getId(), 
						node.getAttributeMap().getDouble("CenterX"),
						node.getAttributeMap().getDouble("CenterY"),
						node.getAttributeMap().get("TextLabel"));
				}
			else s = key;
			System.out.println(s);
		}
	}

//	public void dumpAnchorTable() {
//		for (String key : getA.keySet())
//		{
//			String s;
//			MNode node = resourceMap.get(key);
//			if (node != null)
//			{
//				s=  String.format("%s \t(%4.1f, %4.1f) \t %s ", node.getId(), 
//						node.getAttributeMap().getDouble("CenterX"),
//						node.getAttributeMap().getDouble("CenterY"),
//						node.getAttributeMap().get("TextLabel"));
//				}
//			else s = key;
//			System.out.println(s);
//		}
//	}

	public void resetEdgeTable()
	{
		for (Edge e : edgeTable)
		{
			VNode start = e.getStartNode();
			if (start == null) continue;
			EdgeLine edgeline = e.getEdgeLine();
			if (edgeline == null) continue;
			e.connect();
//			Line line = edgeline.getLine();
//			line.setStartX(startPt.getX());
//			line.setStartY(startPt.getY());
//			line.setEndX(endPt.getX());
//			line.setEndY(endPt.getY());
		}
	}
	public void dumpViewHierarchy() {
		String out = "\n" + traverseSceneGraph( getController().getPasteboard());
		System.out.println(out);
	}
	public void dumpEdgeTable() {
		System.out.println("\n" + edgeTable.size());
		for (Edge e : edgeTable)
			System.out.println(e);
		}
	//-------------------------------------------------------------------------
	private Species species;
	public Species getSpecies() {
		if (species == null) 
			species = Species.Human;
		return species;
	}
	public void setSpecies(Species s) {			species = s;	}
}



