package diagrams.pViz.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import diagrams.pViz.app.Controller;
import diagrams.pViz.gpml.GPMLGroup;
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
import model.bio.Gene;
import model.bio.GeneListRecord;
import model.bio.Species;
import services.bridgedb.BridgeDbIdMapper;
import services.bridgedb.MappingSource;
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
	private Map<String, MNode> resourceMap = FXCollections.observableHashMap();
	private int nodeCounter = 0;
	public Collection<MNode> getNodes()			{ return resourceMap.values();	}

	private List<Edge> edgeList = FXCollections.observableArrayList();
	public List<Edge> getEdgeList()			{ return edgeList;	}
	public List<Edge> getEdgeList(String nodeId)			
	{ 
		List<Edge> hits = new ArrayList<Edge>();
		for (Edge e : edgeList)
			if (e.touches(nodeId))
					hits.add(e);
		return hits;	
	}
	private Map<String, Shape> shapes = new HashMap<String,Shape>();
	public Map<String, Shape> getShapes()	{ return shapes; }
	public Shape findShape(String s ) 		{ return shapes.get(s);	}

	public Map<String, MNode> getResourceMap() {		return resourceMap;	}

	private String title = "PathVisio Mockup";
	public void setTitle(String val) {		title = val;		}
	public String getTitle() 		{		return title;		}
	// **-------------------------------------------------------------------------------

	public Model(Controller ct)
	{
		controller = ct;
	}
	//-------------------------------------------------------------------------
	private Species species;
	public Species getSpecies() {
		if (species == null) 
			species = Species.Human;
		return species;
	}
	public void setSpecies(Species s) {			species = s;	}
	public void addGenes(List<Gene> inList) {
		for (Gene g: inList)
			if (null == findInList(genes, g.getName()))
				genes.add(g);
	}
	// **-------------------------------------------------------------------------------
	public String saveState()
	{
		String header = docHeader();
		StringBuilder saver = new StringBuilder(header);
		serializeComments(saver);
		serializeNodes(saver);
		serializeEdges(saver);
		serializeReferences(saver);
		return saver.toString();
	}
	String[] pathwayAttributes = {"Name", "Organism", "License"};
	private static String xmlHeader = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> \n";
	private static String namespace = "xmlns=\"http://pathvisio.org/GPML/2013a\"\n";
	
	private String docHeader() {		return xmlHeader +  "<Pathway ";	}
	
	private void serializeComments(StringBuilder saver) {
		for (CommentRecord rec : getComments())
			saver.append(rec.toGPML());
	}
	private void serializeReferences(StringBuilder saver) {
		for (BiopaxRecord rec : getReferences())
			saver.append(rec.toGPML());
	}
	private void serializeNodes(StringBuilder saver) {
		for (MNode node : getResourceMap().values())
			saver.append(node.toGPML());
		}
	private void serializeEdges(StringBuilder saver) {
		for (Edge edge : getEdgeList())
			saver.append(edge.toGPML());
	}
	public void serializeGroups(StringBuilder bldr)
	{
		for (GPMLGroup g : getGroups())
			bldr.append(g.toGPML());
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

	// **-------------------------------------------------------------------------------
	List<Gene> genes = FXCollections.observableArrayList();
	GeneListRecord geneListRecord = null;
	public GeneListRecord getGeneList() {	return	geneListRecord ; }
	public void setGeneList(GeneListRecord rec, List<Gene> gs) {		genes = gs;	geneListRecord = rec; }
	public int getNGenes() 				{	return	genes.size();	}
	public void addGene(Gene g) 		{	genes.add(g);	}
	public void clearGenes() 			{	genes.clear();	}
	public List<Gene> getGenes() 		{	return genes;	}
//	public List<Gene> getGeneList() 		{		return new GeneList(genes, getSpecies());	}
	public Gene findGene(String string) {
		if (StringUtil.isEmpty(string)) return null;
		for (Gene g : getGenes())
			if (string.equals(g.getName()))
				return g;
		return null;
	}
	public Gene findGene(Gene other) {
		if (other == null) return null;
		return findGene(other.getName());
	}
	public boolean add(Gene g)
	{
		if ( find(g.getName()) == null) 
			return genes.add(g);
		return false;
	}
	
	public boolean add(GeneListRecord g)
	{
		boolean anyTrue = false;
		for (Gene gene : g.getGeneList())
			anyTrue |= add(gene);
		return anyTrue;
	}
	
	public List<Gene> intersection(List<Gene> other)
	{
		List<Gene> intersection = FXCollections.observableArrayList();
		for (Gene g : genes)
			if (findInList(other, g.getName()) == null)
				intersection.add(g);
		return intersection;
	}
	
	public List<Gene> union(List<Gene> other)
	{
		List<Gene> union = FXCollections.observableArrayList();
		union.addAll(genes);
		for (Gene g : other)
			if (findInList(genes, g.getName()) == null)
				union.add(g);
		return union;
	}
	static public Gene findInList(List<Gene> list, String nameOrId)
	{
		if (nameOrId == null) return null;
		String name = nameOrId.trim();
		for (Gene g : list)
		{
			if (name.equals(g.getName())) return g;			//IgnoreCase
//			if (name.equalsIgnoreCase(g.getId())) return g;
		}
		return null;
	}
	public Gene find(String nameOrId)
	{
		if (nameOrId == null) return null;
		String name = nameOrId.trim();
		for (Gene g : genes)
		{
			if (name.equalsIgnoreCase(g.getName())) return g;
			if (name.equalsIgnoreCase(g.getId())) return g;
		}
		return null;
	}
	public Gene find(Gene g)	{		return find(g.getName());	}

	//--------------------------------------------------------------------

	static String TAB = "\t";
	static String NL = "\n";
	public static String BDB = "http://webservice.bridgedb.org/";
	public void fillIdlist()
	{
		if (species == null) 
			species = Species.Human;
		StringBuilder str = new StringBuilder();
		for (Gene g : genes)
		{
			if (StringUtil.hasText(g.getIdlist())) continue;
			String name = g.getName();
			MappingSource sys = MappingSource.guessSource(species, name);
			str.append(name + TAB + sys.system() + NL);
		}
		try
		{
			List<String> output = BridgeDbIdMapper.post(BDB, species.common(), "xrefsBatch", "", str.toString());
			for (String line : output)
			{
				String [] flds = line.split("\t");
				String name = flds[0];
				String allrefs = flds[2];
				for (Gene g : genes)
				{
					if (!g.getName().equals(name)) continue;
					System.out.println("setting ids for " + name );	
					g.setIdlist(allrefs);
					g.setEnsembl(BridgeDbIdMapper.getEnsembl(allrefs));
				}
			}
		}
		catch(Exception ex) 
		{ 
			System.err.println(ex.getMessage());	
		}
	}
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
	public Edge addEdge(MNode start, MNode end, String activeLayer)		
	{  
		AttributeMap attributes = new AttributeMap();
		attributes.put("Layer", activeLayer);
		Edge edge = new Edge(this, start.getStack(), end.getStack(), attributes, null, null);
		addEdge(edge);
		return edge;
	}
	
	public void addEdge(Edge e)			{  edgeList.add(e);	}
	public void setAnchorVisibility(boolean visible)			
	{ 
		for (Edge e : edgeList)
			e.getEdgeLine().setAnchorVis(visible);	
	}
	private void readEdges(String state) {
	}
	// **-------------------------------------------------------------------------------
	
//	EventListener connector = 
//	
	public void removeEdges(Node node)		
	{  
		for (int z = edgeList.size()-1; z >= 0; z--)
		{
			Edge e = edgeList.get(z);
			if (e.isStart(node) || e.isEnd(node))
			{
				e.removeListeners();
				e.getEdgeLine().dispose();
				edgeList.remove(e);
			}
		}
//		List<Edge> okEdges = edgeTable.stream().filter(new TouchingNodeFilter(node)).collect(Collectors.toList());
//		edgeTable.clear();
//		edgeTable.addAll(okEdges);
	}
	// **-------------------------------------------------------------------------------
	
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
	//------------------------------------------------------------------------- GROUPS
	Map<String, GPMLGroup> groups = new HashMap<String, GPMLGroup>();
	
	public void addGroup(String groupId, String graphId) {
		AttributeMap attr = new AttributeMap();
		attr.put("LineStyle", "Broken");
		attr.put("GraphId", graphId);
		attr.put("GroupId", groupId);
		groups.put(groupId, new GPMLGroup(attr, getController()));
	}
	public Collection<GPMLGroup> getGroups() { return groups.values();	}
	
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
				if (end.getShape() instanceof Line) continue;		//TODO add anchor
				edges.add(new Edge(this, start, end, null, null, null));
			}
		}
		return edges;
	}
	public void removeEdge(Edge edge)			{  		edgeList.remove(edge);	}
	public void connectAllEdges() {
		for (int z = edgeList.size()-1; z >= 0; z--)
		{
			Edge e = edgeList.get(z);
			e.connect(true);
			e.connect(false);
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
		for (Edge e : edgeList)
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
	static public String describe(MNode node)	{	return node.toGPML();	}
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
		System.out.println(resourceMap.keySet().size());
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
		System.out.println("resetEdgeTable: " + edgeList.size());
		for (Edge e : edgeList)
		{
			VNode start = e.getStartNode();
			if (start == null) 
			{
				String startId = e.getAttributes().get("start");
				if (startId != null)
				{
					MNode startMNode = getResource("start");
					start = (startMNode == null) ? null : startMNode.getStack();
				}
			}
			EdgeLine edgeline = e.getEdgeLine();
			VNode end = e.getEndNode();
			if (end == null) 
			{
				String endId = e.getAttributes().get("end");
				if (endId != null)
				{
					MNode endMNode = getResource("end");
					end = (endMNode == null) ? null : endMNode.getStack();
				}
			}
			if (edgeline == null) continue;
			e.connect();
//			e.addListeners();
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
		System.out.println("\n" + edgeList.size());
		for (Edge e : edgeList)
			System.out.println(e);
		}
	
	public void applyState(String state, AttributeMap attrMap) {
		
		for (MNode node : resourceMap.values())
		{
			if (node.getId().equals(state))
				for (String key : attrMap.keySet())
					node.attributes.put("State" + key, attrMap.get(key));
		}
		for (Edge edge : edgeList)
		{
			if (edge.references(state))
				for (String key : attrMap.keySet())
					edge.getAttributes().put("State" + key, attrMap.get(key));
		}
	}
	
}



