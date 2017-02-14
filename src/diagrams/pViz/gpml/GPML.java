package diagrams.pViz.gpml;

import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.junit.Assert;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.NodeList;

import diagrams.pViz.app.Controller;
import diagrams.pViz.model.Edge;
import diagrams.pViz.model.MNode;
import diagrams.pViz.model.Model;
import diagrams.pViz.view.VNode;
import javafx.collections.FXCollections;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.paint.Color;
import javafx.scene.shape.Shape;
import model.AttributeMap;
import model.bio.BiopaxRecord;
import model.bio.Gene;
import model.bio.GeneListRecord;
import model.bio.ReferenceListRecord;
import model.bio.Species;
import util.FileUtil;
import util.StringUtil;

public class GPML {

//	private Controller controller;
	private Model model;
	String activeLayer = "Content";
	public GPML(Model m, String layer) {
		model = m;
		if (layer != null) activeLayer = layer;
		Assert.assertNotNull(m);
	}
	private Controller getController() { 	return model.getController();	}
	//----------------------------------------------------------------------------
	public static GeneListRecord readGeneList(File file, Species inSpecies)
	{
		org.w3c.dom.Document doc = FileUtil.openXML(file);
		if (doc == null) return null;
		List<Gene> list = FXCollections.observableArrayList();
		GeneListRecord record = new GeneListRecord(file.getName());
		record.setSpecies(inSpecies.common());
		record.setName(file.getName());
		
		NodeList nodes = doc.getElementsByTagName("DataNode");
		int len = nodes.getLength();
		for (int i=0; i<len; i++)
		{
			org.w3c.dom.Node domNode = nodes.item(i);
			NamedNodeMap nodemap = domNode.getAttributes();
			org.w3c.dom.Node type = nodemap.getNamedItem("Type");
			if ("GeneProduct".equals(type.getNodeValue()) || "Protein".equals(type.getNodeValue()))
			{
				String textLabel = nodemap.getNamedItem("TextLabel").getNodeValue();
				Gene existing = Model.findInList(list, textLabel);
				if (existing == null)
					list.add(new Gene(record, textLabel));
				System.out.println(textLabel + " " + ((existing == null) ? "unique" : "found"));
			}
		}
		record.setGeneList(list);
		return record;
	}



	public void read(org.w3c.dom.Document doc)
	{
//		if (doc != null) return;
		model.clearComments();
		Controller controller = model.getController();
		NodeList nodes = doc.getElementsByTagName("Pathway");
		System.out.println(nodes.getLength());
		for (int i=0; i<nodes.getLength(); i++)
		{
			org.w3c.dom.Node domNode = nodes.item(i);
			System.out.println(domNode.toString());
			NamedNodeMap nodemap = domNode.getAttributes();
			String key = "", val = "";
			for (int j=0; j<nodemap.getLength(); j++)
			{
				org.w3c.dom.Node grandchild = nodemap.item(j);
				if (grandchild == null) continue;
				key = grandchild.getNodeName();
				val = grandchild.getNodeValue();
				if ("Name".equals(key) || "Organism".equals(key))
					model.addComment(key, val);
				if ("Name".equals(key)) 
					model.setTitle(val);
				System.out.println(key + ": " + val);
			}
		
		}
		Label labl = new Label(model.getCommentsStr());
		controller.add(labl);
		labl.setLayoutX(10);
		labl.setLayoutY(10);
		
		parseDataNodes(doc.getElementsByTagName("DataNode"));
		handleLabels(doc.getElementsByTagName("Label"));
		handleBiopax(doc.getElementsByTagName("Biopax"));
		handleGroups(doc.getElementsByTagName("Group"));
//		handleLabels(doc.getElementsByTagName("InfoBox"));
		parseEdges(doc.getElementsByTagName("Interaction"));
		parseShapes(doc.getElementsByTagName("Shape"));
		parseStateNodes(doc.getElementsByTagName("State"));
		getController().getPasteboard().restoreBackgroundOrder();
//		getController().getPasteboard().getChildren().sort(c);
//		List<Node> sorted = getController().getPasteboard().getChildren().stream()
//        	.sorted(Comparator.comparing(null).reversed())
//        	.peek(System.out::println)
//        	.collect(Collectors.toCollection(()->FXCollections.observableArrayList()));
	}
	
	Comparator<Node> c = new Comparator<Node>()
	{
		@Override public int compare(Node o1, Node o2) {
			if (o1 instanceof VNode && o2 instanceof VNode)
				return ((VNode)o2).compareTo((VNode)o1);
			return 0;
		}
	};
	
	private void parseDataNodes(NodeList nodes) {
		for (int i=0; i<nodes.getLength(); i++)
		{
			org.w3c.dom.Node child = nodes.item(i);
			MNode node = parseGPML(child, model, activeLayer);
			model.addResource(node.getId(), node);
			getController().add(node.getStack());
			System.out.println("adding: " + node);
		}
	}
	
	private void parseStateNodes(NodeList nodes) {
		for (int i=0; i<nodes.getLength(); i++)
		{
			AttributeMap attrMap = new AttributeMap();
			org.w3c.dom.Node child = nodes.item(i);
			String name = child.getNodeName();
			String state = "" +child.getAttributes().getNamedItem("GraphRef");
			attrMap.put("State", state);
			attrMap.put("TextLabel", "" +child.getAttributes().getNamedItem("TextLabel"));
			attrMap.put("GraphId", "" +child.getAttributes().getNamedItem("GraphId"));
			if ("Graphics".equals(name))
				attrMap.add(child.getAttributes());
			
			model.applyState(state, attrMap);
		}
	}
	
	
	
	private void parseEdges(NodeList edges) {
		System.err.println("-------------------------------------");
		System.out.println("Edges: "+ edges.getLength());
		String layerName = activeLayer; 
		for (int i=0; i<edges.getLength(); i++)
		{
			org.w3c.dom.Node child = edges.item(i);
			String name = child.getNodeName();
			System.out.println(name + " " + child.getAttributes().getNamedItem("GraphId"));
			
			Edge edge = parseGPMLEdge(child, model, activeLayer);
			System.out.println(edge);
			if (edge != null)
				getController().add(0, edge, layerName);
		}
		for (Edge e : model.getEdgeList())
			e.connect();
	}
	
	private void parseShapes(NodeList shapes) {
		for (int i=0; i<shapes.getLength(); i++)
		{
			org.w3c.dom.Node child = shapes.item(i);
			MNode node = parseGPML(child, model, activeLayer);
			
			if (node != null)
				getController().add(0, node.getStack());
		}
	}
	// **-------------------------------------------------------------------------------
	/*
	 * 	convert an org.w3c.dom.Node to a local MNode.  
	 * 
	 */
	public MNode parseGPML(org.w3c.dom.Node datanode, Model m, String activeLayer) {
//		Model m = ctrl.getModel();
		AttributeMap attrMap = new AttributeMap();
		attrMap.put("Layer", activeLayer);
		attrMap.add(datanode.getAttributes());
		NodeList elems = datanode.getChildNodes();
		for (int i=0; i<elems.getLength(); i++)
		{
			org.w3c.dom.Node child = elems.item(i);
			String name = child.getNodeName();
			if ("#text".equals(name)) continue;
			if ("BiopaxRef".equals(name))
			{
				String ref = child.getTextContent();
				if (!StringUtil.isEmpty(ref))
					attrMap.put("BiopaxRef", ref);
			}
			attrMap.add(child.getAttributes());			// NOTE: multiple Attribute elements will get overridden!
//			System.out.println(name);
		}
		attrMap.put("Resizable", "false");
		return  new MNode(attrMap, m);
	}
		//----------------------------------------------------------------------------
	public Edge parseGPMLEdge(org.w3c.dom.Node edgeML, Model m, String activeLayer) {
	try
	{
		AttributeMap attrMap = new AttributeMap();
		attrMap.put("Layer", activeLayer);
		attrMap.add(edgeML.getAttributes());
		List<GPMLPoint> points = new ArrayList<GPMLPoint>();
		List<Anchor> anchors = new ArrayList<Anchor>();
		NodeList elems = edgeML.getChildNodes();
		String startId="", endId="";
		MNode endNode = null, startNode = null;
		for (int i=0; i<elems.getLength(); i++)
		{
			org.w3c.dom.Node n = elems.item(i);
			String name = n.getNodeName();
			if ("Graphics".equals(name))
			{
				attrMap.add(n.getAttributes());
				NodeList pts = n.getChildNodes();
				for (int j=0; j<pts.getLength(); j++)
				{
					org.w3c.dom.Node pt = pts.item(j);
					if ("Point".equals(pt.getNodeName()))
						points.add(new GPMLPoint(pt));
					if ("Anchor".equals(pt.getNodeName()))
					{
						Anchor gpt = new Anchor(pt, m);
						anchors.add(gpt);
					}
				}
			}
			else if ("Xref".equals(name))	
				attrMap.add(n.getAttributes());
			else if ("BiopaxRef".equals(name))	
				attrMap.put("BiopaxRef", n.getTextContent());
		}
		int z = points.size();
		if (z > 1)
		{
			startId = points.get(0).getGraphRef();
			startNode = m.getResource(startId);
			attrMap.put("start", startId);
			GPMLPoint lastPt = points.get(z-1);
			endId = lastPt.getGraphRef();
			attrMap.put("end", endId);
			endNode = m.getResource(endId);
			if (startNode != null && endNode != null) 
				return new Edge(m, startNode.getStack(), endNode.getStack(), attrMap, points, anchors);	
			System.err.println("no end node found: " + endId);
		}
		return new Edge(attrMap, m);
	}
	catch(Exception e)
	{
		e.printStackTrace();
		return null;
	}
	}	
	
	public MNode parseGPMLLabel(org.w3c.dom.Node labelNode) {
		AttributeMap attrMap = new AttributeMap();
		NodeList elems = labelNode.getChildNodes();
		attrMap.add(labelNode.getAttributes());
		String txt = attrMap.get("TextLabel");
		if (txt == null) txt = "Undefined";
//		Label label = new Label(txt);
//		label.setManaged(false);
		String name = "";
		for (int i=0; i<elems.getLength(); i++)
		{
			org.w3c.dom.Node child = elems.item(i);
			name = child.getNodeName();
//			if (name != null && name.equals("TextLabel")) 
//				label.setText(child.getNodeValue());
			if (name != null && name.equals("Attribute")) 
			{
				NamedNodeMap attrs = child.getAttributes();
				String key = "", val = "";
				for (int j=0; j<attrs.getLength(); j++)
				{
					org.w3c.dom.Node grandchild = attrs.item(j);
					String grandname = grandchild.getNodeName();
					{
						if ("Key".equals(grandname))	key = grandchild.getNodeValue();
						if ("Value".equals(grandname))	val = grandchild.getNodeValue();
					}
				}
				if (StringUtil.hasText(key) && StringUtil.hasText(val))
				{
					if (key.startsWith("org.pathvisio."))
						key = key.substring(14);
//					label.setText(key + ":\n" + val);
//					label.setTextFill(Color.CHOCOLATE);
					attrMap.put("TextLabel", key + ":\n" + val);
					
				}
			}
			if ("Graphics".equals(name))
					attrMap.add(child.getAttributes());
		}
//		attrMap.put("ShapeType", "Label");
		String shapeType = attrMap.get("ShapeType");
		if (shapeType == null)
			attrMap.put("ShapeType", "None");
				
		return new MNode(attrMap, model);
	}


//	private void applyGraphicsNode(Label label, org.w3c.dom.Node child) {
//		NamedNodeMap attrs = child.getAttributes();
//		String name = "";
//		for (int i=0; i<attrs.getLength(); i++)
//		{
//			org.w3c.dom.Node item = attrs.item(i);
//			String val = item.getNodeValue();
//			double d = StringUtil.toDouble(val);
//			name = item.getNodeName();
//			
//			if ("CenterX".equals(name)) 		 label.setLayoutX(d);
//			else if ("CenterY".equals(name)) 	 label.setLayoutY(d);
//			else if ("Width".equals(name)) 		 {	label.maxWidth(d); label.prefWidth(d);}
//			else if ("Height".equals(name)) 	{	label.maxHeight(d); label.prefHeight(d);}
//			else if ("ZOrder".equals(name)) 	{}
////			else if ("Color".equals(name)) {	label.setBorder(Borders.coloredBorder(val));}
//			else if ("Color".equals(name)) 		label.setTextFill(Color.web(val));
//			else if ("FillColor".equals(name)) 	label.setBackground(Backgrounds.colored(val));
//			else if ("FontSize".equals(name)) 	{}
//			else if ("FontWeight".equals(name)) {}
//			else if ("Valign".equals(name)) 	{}
////			else if ("ShapeType".equals(name)) 	
////			{	if ("RoundedRectangle".equals(val)) {}		}
//		}
//		double w = StringUtil.toDouble(attrs.getNamedItem("Width").getNodeValue());
//		double h = StringUtil.toDouble(attrs.getNamedItem("Height").getNodeValue());
////		label.getWidth();
////		double h = label.getWidth();
//		label.setLayoutX(label.getLayoutX() - w / 2.);
//		label.setLayoutY(label.getLayoutY() - h / 2.);
//	}
	//----------------------------------------------------------------------------

	private void handleBiopax(NodeList elements) {
		for (int i=0; i<elements.getLength(); i++)
		{
			org.w3c.dom.Node child = elements.item(i);
			String name = child.getNodeName();
			System.out.println(name);
			
			for (int j=0; j<child.getChildNodes().getLength(); j++)
			{
				org.w3c.dom.Node gchild = child.getChildNodes().item(j);
				String jname = gchild.getNodeName();
//				System.out.println(name);
				if ("bp:PublicationXref".equals(jname))
				{
					BiopaxRecord ref = new BiopaxRecord(gchild);
					model.addRef(ref);
					System.out.println(ref);					//TODO
				}
			}
		}
	}
	//----------------------------------------------------------------------------
	private void handleLabels(NodeList elements) {
		for (int i=0; i<elements.getLength(); i++)
		{
			org.w3c.dom.Node child = elements.item(i);
			String name = child.getNodeName();
			System.out.println(name);
			MNode label = parseGPMLLabel(child);
			if (label != null)
			{
				model.addResource(label);
				model.getController().add(label.getStack());
//				label.getStack().toBack();
			}
		}
	}
//	private void add(Node n)		{ 		controller.add(n);		}
//	private NodeFactory getNodeFactory() {		return controller.getNodeFactory();	}
	//----------------------------------------------------------------------------
	private void handleGroups(NodeList elements) {			//TODO
		for (int i=0; i<elements.getLength(); i++)
		{
			org.w3c.dom.Node child = elements.item(i);
			NamedNodeMap attrs = child.getAttributes();
			String name = child.getNodeName();
			System.out.println(name);
			if ("#text".equals(name)) continue;
			if ("Group".equals(name))
			{
				System.out.println(name + " " + attrs.getNamedItem("GraphId"));
				String groupId = "";
				if (attrs.getNamedItem("GroupId") != null)
					groupId = attrs.getNamedItem("GroupId").getNodeValue();
				String graphId = "";
				if (attrs.getNamedItem("GraphId") != null)
					graphId = attrs.getNamedItem("GraphId").getNodeValue();
				model.addGroup(groupId, graphId);
			}
		}
		for (GPMLGroup group : model.getGroups())
		{
			String id = group.getAttributeMap().get("GroupId");
			for (MNode node : model.getNodes())
				if (id.equals(node.getAttributeMap().get("GroupRef")))
					group.addToGroup(node);
			group.calcBounds();
			VNode stack = group.getStack();
			stack.setBounds(group.getBounds());
			model.getController().add(stack);
			Shape shape = group.getStack().getFigure();
			if (shape != null)
			{
				shape.setStyle("-fx-stroke-dash-array: 10 10;");
				shape.setFill(Color.TRANSPARENT);
			}
			stack.toBack();
			
		}
	}
//	
	//----------------------------------------------------------------------------
//	http://fxexperience.com/2011/12/styling-fx-buttons-with-css/
		
		public static String asFxml(String gpmlTag) {
		
		if (gpmlTag == null) return null;
		if ("Color".equals(gpmlTag))		return "-fx-border-color";
		if ("FillColor".equals(gpmlTag))	return "-fx-background-color";
		if ("LineThickness".equals(gpmlTag)) return "-fx-stroke-width";
		if ("Opacity".equals(gpmlTag))		return "-fx-opacity";
		if ("FontSize".equals(gpmlTag))		return "-fx-font-size";
		if ("Valign".equals(gpmlTag))		return "-fx-row-valignment";
		
		return null;
	}
	
	// UNUSED ??
//	
//	public Shape shapeFromGPML(String gpmlStr,  AttributeMap attrMap, boolean addHandlers) {
//		String txt = gpmlStr.trim();
//		if (txt.startsWith("<DataNode "))
//		{
//			String attrs = txt.substring(10, txt.indexOf(">"));
//			attrMap.addGPML(attrs);
//			String graphics =  txt.substring(10 + txt.indexOf("<Graphics "), txt.indexOf("</Graphics>"));
//			String xref = txt.substring(10 + txt.indexOf(6 + "<Xref "), txt.indexOf("</Xref>"));
//			attrMap.addGPML(graphics);
//			attrMap.addGPML(xref);
//		}
//		String shapeType = attrMap.get("ShapeType");
//		Shape newShape = controller.getNodeFactory().getShapeFactory().makeNewShape(shapeType, attrMap, addHandlers); 
//		return newShape;
//	}

//	public static Edge createEdge(String txt, AttributeMap attrMap, Model model) 
//	{
//		String graphics =  txt.substring(10 + txt.indexOf("<Graphics "), txt.indexOf("</Graphics>"));
//		String xref = txt.substring(10 + txt.indexOf(6 + "<Xref "), txt.indexOf("</Xref>"));
//		attrMap.addGPMLEdgeInfo(graphics);
//		attrMap.addGPML(xref);
//		return new Edge(attrMap, model);
//	}

	//----------------------------------------------------------------------------
//	public Node[] makeTestItems() {
//		Node a,b,c,d,e,f,g,h,i,j,k, l;
//		ShapeFactory factory = controller.getNodeFactory().getShapeFactory();
//		AttributeMap attrMap = new AttributeMap();
//		
//		attrMap.putCircle(new Circle(150, 150, 60, Color.AQUA));
//		attrMap.put("TextLabel", "Primary");
//		Label label = factory.createLabel("Primary", Color.AQUA);
//		a = label;
//		Circle cir = (Circle) factory.makeNewShape(Tool.Circle, attrMap);
//		b = cir;
//		label.layoutXProperty().bind(cir.centerXProperty().subtract(label.widthProperty().divide(2.)));
//		label.layoutYProperty().bind(cir.centerYProperty().subtract(label.heightProperty().divide(2.)));
//		
//		attrMap = new AttributeMap();
//		attrMap.putCircle(new Circle(180, 450, 60, Color.AQUA));
//		attrMap.put("TextLabel", "Secondary");
//		c = factory.makeNewShape(Tool.Circle, attrMap);
//		attrMap = new AttributeMap();
//		attrMap.putCircle(new Circle(150, 300, 20, Color.BEIGE));
//		attrMap.put("TextLabel", "Tertiary");
//		d = factory.makeNewShape(Tool.Circle, attrMap);
//		attrMap = new AttributeMap();
//		attrMap.putRect(new Rectangle(250, 50, 30, 30));
//		e = factory.makeNewShape(Tool.Rectangle, attrMap);
//		attrMap = new AttributeMap();
//		attrMap.putRect(new Rectangle(250, 450, 30, 50));
//		f = factory.makeNewShape(Tool.Rectangle, attrMap);
//		
//		new Edge(b, c);
//		new Edge(d, b);
//		new Edge(f, b);
//		new Edge(f, c);
//		new Edge(e, f);
//		new Edge(e, d);
//		return new Node[] { a,b,c,d,e,f };
//	}

}
