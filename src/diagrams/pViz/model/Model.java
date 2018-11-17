package diagrams.pViz.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import diagrams.pViz.app.Controller;
import diagrams.pViz.gpml.Anchor;
import diagrams.pViz.gpml.GPMLPoint;
import diagrams.pViz.gpml.GPMLPoint.ArrowType;
import diagrams.pViz.view.Layer;
import diagrams.pViz.view.Pasteboard;
import diagrams.pViz.view.VNode;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.scene.Group;
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
import model.bio.BiopaxRecord;
import model.bio.Species;
import model.bio.XRefableSetRecord;
import util.StringUtil;

public class Model
{
/*
 *  Model - the set of record lists	
 *  
 *  We need to keep track of species, nodes, edges, genelists, pathways, references, comments, groups
 */
	private Controller controller;
	public Controller getController() { return controller; } 
	private Map<String, DataNode> dataNodeMap = FXCollections.observableHashMap();
	private int nodeCounter = 0;
	public Collection<DataNode> getNodes()			{ return dataNodeMap.values();	}
	public Map<String, DataNode> getDataNodeMap() {		return dataNodeMap;	}

//	private Map<String, DataNodeState> stateMap = FXCollections.observableHashMap();
//	public Collection<DataNodeState> getStates()			{ return stateMap.values();	}
//	public Map<String, DataNodeState> getStatesNodeMap() {		return stateMap;	}

	private Map<String, DataNodeGroup> groupMap = FXCollections.observableHashMap();
	public Collection<DataNodeGroup> getGroups()			{ return groupMap.values();	}
	public Map<String, DataNodeGroup> getGroupMap() {		return groupMap;	}

	private Map<String, DataNodeState> stateMap = FXCollections.observableHashMap();
	public Collection<DataNodeState> getStates()			{ return stateMap.values();	}
	public Map<String, DataNodeState> getStateMap() {		return stateMap;	}
	public void addState(String graphRef, DataNodeState statenode) {
		stateMap.put(graphRef, statenode);
		
	}

	public Collection<Interaction> getEdges()			{ return interactionMap.values();	}
	private Map<String, Interaction> interactionMap = FXCollections.observableHashMap();
	public Map<String, Interaction> getInteractions()			{ return interactionMap;	}
	public List<Interaction> getInteractionList(String nodeId)			
	{ 
		List<Interaction> hits = new ArrayList<Interaction>();
		for (Interaction e : interactionMap.values())
			if (e.touches(nodeId))
					hits.add(e);
		return hits;	
	}
	public Interaction getInteraction(String edgeId)	{ 	return interactionMap.get(edgeId);	}
	
	private Map<String, DataNode> shapes = new HashMap<String,DataNode>();
	public Map<String, DataNode> getShapes()	{ return shapes; }
	public DataNode findShape(String s ) 		{ return shapes.get(s);	}
	public void addShape(DataNode s ) 			{ shapes.put(s.getGraphId(),s);	}

	private Map<String, DataNode> labels = new HashMap<String,DataNode>();
	public Map<String, DataNode> getLabels()	{ return labels; }
	public DataNode findLabel(String s ) 		{ return labels.get(s);	}
	public void addLabel(DataNode d)			{ labels.put(d.getGraphId(),  d); }


	private String title = "PathVisio Mockup";
	public void setTitle(String val) {		title = val;		}
	public String getTitle() 		{		return title;		}
	// **-------------------------------------------------------------------------------

	public Model(Controller ct)
	{
		controller = ct;
	}
	//-------------------------------------------------------------------------
	private Species species = Species.Unspecified;
	public Species getSpecies() {
		if (species == null) 
			species = Species.Unspecified;
		return species;
	}
	public void setSpecies(Species s) {			species = s;	}
	// **-------------------------------------------------------------------------------
	public String saveState()
	{
		String header = docHeader();
		StringBuilder saver = new StringBuilder(header);
		serializeComments(saver);
		serializeNodes(saver);
		serializeEdges(saver);
		serializeReferences(saver);
		saver.append("</Pathway>\n");
		return saver.toString();
	}
	String[] pathwayAttributes = {"Name", "Organism", "License"};
	private static String xmlHeader = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> \n";
	private static String namespace = "xmlns=\"http://pathvisio.org/GPML/2013a\"\n";
	
	private String docHeader() {		return xmlHeader +  "<Pathway >";	}
	
	private void serializeComments(StringBuilder saver) {
		for (CommentRecord rec : getComments())
			saver.append(rec.toGPML());
	}
	private void serializeReferences(StringBuilder saver) {
		for (BiopaxRecord rec : getReferences())
			saver.append(rec.toGPML());
	}
	private void serializeNodes(StringBuilder saver) {
		for (DataNode node : getNodes())
			saver.append(node.toGPML());
		}
	private void serializeEdges(StringBuilder saver) {
		for (Interaction edge : getEdges())
			saver.append(edge.toGPML());
	}
	public void serializeGroups(StringBuilder bldr)
	{
		for (String key : groupMap.keySet())
			bldr.append(groupMap.get(key).toGPML());
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
	
	List<CommentRecord> comments = new ArrayList<CommentRecord>();
	public void addComment(String source, String text) {		comments.add(new CommentRecord(source, text));	}
	public void clearComments() {		comments.clear();			}
	public String getCommentsStr() {
		StringBuilder b = new StringBuilder();
		for (CommentRecord c : comments)
			b.append(c.getText()).append("\n");
		return b.toString();
	}
	public List<CommentRecord> getComments() {		return comments;	}

	//--------------------------------------------------------------------
	//--------------------------------------------------------------------
	ObservableList<BiopaxRecord> references = FXCollections.observableArrayList();
	public void addRef(BiopaxRecord ref) 	{	if (!refExists(ref.getId()))	references.add(ref);		}
	private boolean refExists(String ref) 	{	return findRef(ref) != null;	}
	private BiopaxRecord findRef(String ref) {
		if (ref == null) return null;
		for (BiopaxRecord r : references)
			if (ref.equals(r.getRdfid())) return r;
		return null;
	}
	public int getNReferences() 			{	return references.size();	}
	public BiopaxRecord getReference(String ref) 	{	return findRef(ref);		}
	public BiopaxRecord getReference(int i) {	return references.get(i);	}
	public void clearRefs() 				{	references.clear();	}
	public List<BiopaxRecord> getReferences() { return references;	}
	private void readReferences(String state) {
	}
	// **-------------------------------------------------------------------------------
		public void addResource(DataNode mnode)		
	{  
		if (mnode != null) 
			addResource(mnode.get("GraphId"), mnode);
	}
	public void addResource(String key, DataNode n)		
	{  
		if (key == null) System.err.println("NULL KEY");
		if (dataNodeMap.get(key) == null)
			dataNodeMap.put(key, n);
	}

	private void readNodes(String state) {
	}
	public DataNode findDataNode(String nameOrId)
	{
		if (nameOrId == null) return null;
		String name = nameOrId.trim();
		for (DataNode g : getNodes())
		{
			if (name.equalsIgnoreCase(g.getName())) return g;
			if (name.equalsIgnoreCase(g.getGraphId())) return g;
		}
		return null;
	}
	// **-------------------------------------------------------------------------------
	public Interaction addInteraction(DataNode start, DataNode end, String activeLayer)		
	{  
		AttributeMap attributes = new AttributeMap();
		attributes.put("Layer", activeLayer);
		Interaction edge = new Interaction(this, start.getStack(), end.getStack(), attributes);
		addEdge(edge);
		return edge;
	}
	// comes from user dragging / connecting
	public Interaction addIteraction(VNode start, VNode end)		
	{  
		if (start == null || end == null) return null;
		AttributeMap attributes = new AttributeMap();
		attributes.put("Color", Color.RED.toString());
		attributes.put("Layer", start.getLayerName());
		List<GPMLPoint> pts = new ArrayList<GPMLPoint>();
		pts.add(new GPMLPoint(start.center()));
		pts.add(new GPMLPoint(end.center()));
		Interaction edge = new Interaction(this, start, end, attributes);
		addEdge(edge);
		return edge;
	}
	
	public void setAnchorVisibility(boolean visible)			
	{ 
		for (Edge e : getEdges())
			e.getEdgeLine().setAnchorVis(visible);	
	}
	private void readEdges(String state) {
	}
	// **-------------------------------------------------------------------------------
	public Anchor findAnchorById(String graphId)
	{
		for (Interaction inter : interactionMap.values())
		{
			Anchor a = inter.getEdgeLine().findAnchor(graphId);
			if (a != null) 
				return a;
		}
		return null;
	}
	public Interaction findInteractionById(String graphId)
	{
		for (Interaction inter : interactionMap.values())
			if (graphId.equals(inter.getGraphId()))
					return inter;
		return null;
	}
	
	
	public List< Interaction> findInteractionsByNode(DataNode node)
	{
		List< Interaction> hits = new ArrayList<Interaction>();
		for (Interaction inter : interactionMap.values())
			if (inter.isStart(node) || inter.isEnd(node))
				hits.add(inter);
		return hits;
	}
	
	public List< Interaction> findInteractionsByNodes(DataNode src, DataNode target)
	{
		List< Interaction> hits = new ArrayList<Interaction>();
		for (Interaction inter : interactionMap.values())
			if (inter.isStart(src) || inter.isEnd(target))
				hits.add(inter);
		return hits;
	}
	public void removeEdges(DataNode node)		
	{  
		for (Interaction e : getEdges())
		{
			if (e == null) continue;
			if (e.isStart(node) || e.isEnd(node))
			{				
				e.removeListeners();
				e.getEdgeLine().dispose();
				interactionMap.remove(e);
			}
		}
//		List<Edge> okEdges = edgeTable.stream().filter(new TouchingNodeFilter(node)).collect(Collectors.toList());
//		edgeTable.clear();
//		edgeTable.addAll(okEdges);
	}
	// **-------------------------------------------------------------------------------
	
	public void removeNode(VNode node)		
	{  
		if (node == null) return;
		if ("Marquee".equals(node.getId())) return;
		
		removeEdges(node.modelNode());
		dataNodeMap.remove(node.modelNode().getGraphId());
	}

	public DataNode getResourceByKey(String key)				
	{
		 if (key == null) return null;
		 for (DataNode n : dataNodeMap.values())
		 {
			 String name = "" +n.get("TextLabel");
			if (name.equals(key)) return n;
		 }
		 DataNode n = dataNodeMap.get(key);	
		 return n;
	}

	public DataNode getDataNode(String key)				
	{
		 if (key == null) return null;
		 if (key.startsWith("\""))  // if its in quotes, strip them
		 {
			 int len = key.length();
			 key = key.substring(1,len-1);
		 }
		 DataNode n = dataNodeMap.get(key);	
		 return n;
	}
	
	public String cloneResourceId(String oldId)
	{
		return gensym(oldId.substring(0,1));
	}
	//------------------------------------------------------------------------- GROUPS
//	Map<String, GPMLGroup> groups = new HashMap<String, GPMLGroup>();
	// move to GPML
	public void addGroup(DataNodeGroup grp) {
		groupMap.put(grp.getGraphId(),grp);
	}
//	public Collection<GPMLGroup> getGroups() { return groups.values();	}
	
	// **-------------------------------------------------------------------------------
	public List<Interaction> connectSelectedNodes()		
	{  
		List<Interaction> edges = new ArrayList<Interaction>();
		List<VNode> selection = controller.getSelection();
		for (int i=0; i<selection.size()-1; i++)
		{
			VNode start = selection.get(i);
			if (start.getShape() instanceof Line) continue;
			for (int j=0; j < selection.size(); j++)
			{
				if (i == j) continue;
				VNode end = selection.get(j);
				if (end.getShape() instanceof Line) continue;		//TODO add anchor
				
				if (downRightAndClose(start, end) || selection.size() == 2)
					edges.add(new Interaction(this, start, end, null));
			}
		}
		return edges;
	}
	private boolean downRightAndClose(VNode start, VNode end) {
		double startX = start.getLayoutX();
		double startY = start.getLayoutY();
		double endX = end.getLayoutX();
		double endY = end.getLayoutY();

		double SLOP = 20;
		if (endY < startY - SLOP) return false;
		if (endX < startX - SLOP) return false;
		if (endY - startY > 2 * start.getHeight()) return false;
		if (endX - startX > 2 * start.getWidth()) return false;

		return true;
	}
	// **-------------------------------------------------------------------------------
	public boolean containsEdge(Edge a) {
		for (Edge ed : getEdges())
		{
			if (a == ed) return true;
			if (a.getSourceid().equals(ed.getSourceid()))
				if (a.getTargetid().equals(ed.getTargetid()))
					return true;
		}
		return false;
	}

	public Edge addEdge(DataNode start, DataNode end)		
	{  
		AttributeMap attributes = new AttributeMap();
		String activeLayer = start.getStack().getLayerName();
		attributes.put("Layer", activeLayer);
		String linetype = controller.getActiveLineType();
		ArrowType arrow = controller.getActiveArrowType();
		attributes.put("ArrowType", arrow.toString());
		attributes.put("LineType", linetype);
		Interaction edge = new Interaction(this, start.getStack(), end.getStack(), attributes);
		controller.addInteraction(edge);
		edge.connect();
		return edge;
	}
	
	public void addEdge(Interaction e)			{  interactionMap.put(e.get("GraphId"), e);	}
	// **-------------------------------------------------------------------------------
	public void removeEdge(Edge edge)			{  		interactionMap.remove(edge);	}
	
	public void connectAllEdges() {
		for (int z = interactionMap.size()-1; z >= 0; z--)
		{
			Edge e = interactionMap.get(z);
			e.connect();
		}
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
		for (Edge e : getEdges())
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
		if (node instanceof Group)			buff.append(StringUtil.spaces(indent) + describe(node) + "\n");	
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
	static public String describe(DataNode node)	{	return node.toGPML();	}
	static public String describe(Layer node)	{	return node.getName();	}
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

	
	public void setColorByValue() {
//		clearColors();
		for (DataNode node : getDataNodeMap().values())
		{
			Object val = node.get("value");
			if (val != null)
			{
				double d = StringUtil.toDouble("" + val);
				if (!Double.isNaN(d) && 0 <= d && 1 >= d)
				{
					Color gray = new Color(d,d,d, 1);
					Shape shape = node.getStack().getFigure();
					if (shape != null)
					{
						shape.setFill(gray);			// TODO set the attribute
						if (gray.getRed() < 0.4 || gray.getBlue() < 0.4 || gray.getGreen() < 0.4)
						{
							node.getStack().getTextField().setStyle("-fx-text-fill: white");
						}
					}
				}
			}
		}
	}	
	public void clearColors() {
			
		for (DataNode node : getDataNodeMap().values())
		{
			node.remove("value");
			Shape shape = node.getStack().getFigure();
			if (shape != null)
			{	
				shape.setFill(Color.WHITE);
				node.getStack().getTextField().setStyle("-fx-text-fill: black");
			}
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
		System.out.println(dataNodeMap.keySet().size());
		for (String key : dataNodeMap.keySet())
		{
			String s;
			DataNode node = dataNodeMap.get(key);
			if (node != null)
			{
				s=  String.format("GraphId: %s \t(%4.1f, %4.1f) \t %s ", node.getGraphId(), 
						node.getDouble("CenterX"),
						node.getDouble("CenterY"),
						node.get("TextLabel"));
				}
			else s = key;
			System.out.println(key + "\n" + s);
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
//						node.getDouble("CenterX"),
//						node.getDouble("CenterY"),
//						node.get("TextLabel"));
//				}
//			else s = key;
//			System.out.println(s);
//		}
//	}

	public void resetEdgeTable()
	{
		System.out.println("resetEdgeTable: " + getEdges().size());
		for (Interaction inter : getEdges())
		{
			inter.dump();
			inter.connect();		
		}
	}

	public void dumpViewHierarchy() {
		String out = "\n" + traverseSceneGraph( getController().getPasteboard());
		System.out.println(out);
	}
	public void dumpEdgeTable() {
		System.out.println("\n" + interactionMap.size());
		for (Edge e : getEdges())
			System.out.println(e);
		}
	public XRefableSetRecord getXRec() {
		XRefableSetRecord set = new XRefableSetRecord("XREFS");
		set.getXRefableSet().addAll(getNodes());
		return set;
	}
//---------------------------------------------------------------------------------------------
	 class Reference
	 {
		 String id;
		 String db;
		 String dbid;
		 
		 Reference(String a, String b, String c)
		 {
			 id = a;
			 db = b;
			 dbid =c ;
		 }
	 };
	 
	 List<Reference> filterByDB(List<Reference> inList, String db)
	 {
		 List<Reference> subset = new ArrayList<Reference>();		
		 for (Reference r : inList)
			 if (db.equals(r.db))
				 subset.add(r);
		 return subset;
	 }
	 
	 String idListToString(List<Reference> inList)
	 {
		 StringBuilder build = new StringBuilder();
		 for (Reference r : inList)
			 build.append(r.id).append("\t");
		 return StringUtil.chopLast(build.toString());
	 }
	 
	public void annotateIdentifiers() {
		 System.out.println("Annotate");
		 Set<String> dbs = new HashSet<String>();		
		 List<Reference> refs = new ArrayList<Reference>();		
		 for (DataNode n : dataNodeMap.values())
		 {
			 String type = n.getType();		if (StringUtil.isEmpty(type)) continue;
			 String db = n.getDatabase();	if (StringUtil.isEmpty(db)) continue;
			 String dbid = n.getDbid();	 	if (StringUtil.isEmpty(dbid)) continue;
			 String id = n.getGraphId();	if (StringUtil.isEmpty(id)) continue;
			 refs.add(new Reference(id, db, dbid));
			 dbs.add(db);
		 }
		Iterator<String> iter = dbs.iterator();
		while (iter.hasNext())
		{
			String db = iter.next();
			List<Reference> match = filterByDB(refs, db);
			String idlist = idListToString(match);
		}
	}

	
}



