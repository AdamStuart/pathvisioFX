package diagrams.pViz.gpml;

import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.Assert;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.NodeList;

import diagrams.pViz.app.Controller;
import diagrams.pViz.gpml.GPMLPoint.ArrowType;
import diagrams.pViz.model.DataNode;
import diagrams.pViz.model.DataNodeGroup;
import diagrams.pViz.model.DataNodeState;
import diagrams.pViz.model.Interaction;
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
import model.bio.GeneSetRecord;
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
	public static GeneSetRecord readGeneList(File file, Species inSpecies)
	{
		org.w3c.dom.Document doc = FileUtil.openXML(file);
		if (doc == null) return null;
		List<Gene> list = FXCollections.observableArrayList();
		GeneSetRecord record = new GeneSetRecord(file.getName());
		record.setSpecies(inSpecies.common());
		record.setName(file.getName());
		
		NodeList nodes = doc.getElementsByTagName("DataNode");
		int len = nodes.getLength();
		for (int i=0; i<len; i++)
		{
			org.w3c.dom.Node domNode = nodes.item(i);
			NamedNodeMap nodemap = domNode.getAttributes();
			org.w3c.dom.Node type = nodemap.getNamedItem("Type");
			String val = type == null ? "" : type.getNodeValue();
			org.w3c.dom.Node id = nodemap.getNamedItem("GraphId");
			String graphid = id == null ? "" : id.getNodeValue();
//			if ("GeneProduct".equals(val) || "Protein".equals(val))
//			{
//				String textLabel = nodemap.getNamedItem("TextLabel").getNodeValue();
//				Gene existing = Model.findInList(list, textLabel);
//				if (existing == null)
//					list.add(new Gene(record, textLabel, graphid));
//				System.out.println(textLabel + " " + ((existing == null) ? "unique" : "found"));
//			}
		}
		record.setGeneSet(list);
		return record;
	}



	public void read(org.w3c.dom.Document doc)
	{
//		if (doc != null) return;
		model.clearComments();
		Controller controller = getController();
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
		controller.addExternalNode(labl);
		labl.setLayoutX(10);
		labl.setLayoutY(10);
		
		parseDataNodes(doc.getElementsByTagName("DataNode"));
		parseLabels(doc.getElementsByTagName("Label"));
		parseBiopax(doc.getElementsByTagName("Biopax"));
		parseShapes(doc.getElementsByTagName("Shape"));
		parseStateNodes(doc.getElementsByTagName("State"));
		parseGroups(doc.getElementsByTagName("Group"));
//		handleLabels(doc.getElementsByTagName("InfoBox"));
		parseInteractions(doc.getElementsByTagName("Interaction"));
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
			DataNode node = parseGPMLDataNode(child, model, activeLayer);
			getController().addDataNode(node);
			System.out.println("adding: " + node + "\n");
		}
	}
	
	private void parseStateNodes(NodeList nodes) {
		for (int i=0; i<nodes.getLength(); i++)
		{
			AttributeMap attrMap = new AttributeMap();
			org.w3c.dom.Node child = nodes.item(i);
			String name = child.getNodeName();
			NamedNodeMap map = child.getAttributes();
			put(attrMap, "GraphRef", map);
			put(attrMap, "TextLabel",map);
			put(attrMap, "GraphId", map);
			if ("Graphics".equals(name))
				attrMap.add(map);
			DataNodeState sstate = new DataNodeState(attrMap, model);
			getController().addStateNode(sstate);
		}
	}
	
	private void put(AttributeMap attrMap, String key, NamedNodeMap map)
	{
		org.w3c.dom.Node  named = map.getNamedItem(key);
		attrMap.put(named.getNodeName(), named.getNodeValue());
	}
	
	private void parseInteractions(NodeList edges) 
	{
		Controller controller = getController();
//		System.err.println("-------------------------------------");
//		System.out.println("Edges: "+ edges.getLength());
		for (int i=0; i<edges.getLength(); i++)
		{
			org.w3c.dom.Node xml = edges.item(i);
			Interaction edge = parseGPMLInteraction(xml, model);
			controller.addInteraction(edge);
		}
	}
	boolean verbose = true;
	private void parseShapes(NodeList shapes) {
		for (int i=0; i<shapes.getLength(); i++)
		{
			org.w3c.dom.Node child = shapes.item(i);
			if (verbose)
				System.out.println("");
			DataNode node = parseGPMLDataNode(child, model, "Background");
			if (node != null)
				getController().addShapeNode(node);
		}
	}
	// **-------------------------------------------------------------------------------
	/*
	 * 	convert an org.w3c.dom.Node to a local MNode.  
	 * 
	 */
	public DataNode parseGPMLDataNode(org.w3c.dom.Node datanode, Model m, String activeLayer) {
		DataNode node = new DataNode(m);
		 node.put("Layer", activeLayer);
		node.add(datanode.getAttributes());
		
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
					node.put("BiopaxRef", ref);
			}
			if ("Attribute".equals(name))
			{
				String key = null;
				String val = null;
				for (int j=0; j<child.getAttributes().getLength(); j++)
				{
					org.w3c.dom.Node grandchild = child.getAttributes().item(j);
					if ("Key".equals(grandchild.getNodeName()))
						key = grandchild.getTextContent();
					else 
						val = grandchild.getTextContent();
					System.out.println(grandchild.getNodeName() + " " + key + val );
				}
				if (key != null && val != null)
					 node.put(key, val);
					
			}
			node.add(child.getAttributes());			// NOTE: multiple Attribute elements will get overridden!
//			System.out.println(name);
			node.copyAttributesToProperties();
		}
		String type = node.get("Type");
		if (isFixed(type))  node.put("Resizable", "false");
		node.copyAttributesToProperties();
		return node;
	}
	
	boolean isFixed(String s)
	{
		if (s == null) return false;
		return "GeneProduct Metabolite Protein RNA Pathway".contains(s);
	}
		//----------------------------------------------------------------------------
	public Interaction parseGPMLInteraction(org.w3c.dom.Node edgeML, Model m) {
	try
	{
		List<GPMLPoint> points = new ArrayList<GPMLPoint>();
		List<Anchor> anchors = new ArrayList<Anchor>();
		AttributeMap attrib = new AttributeMap(edgeML.getAttributes());
		NodeList elems = edgeML.getChildNodes();
		String startId="", endId="";
		DataNode endNode = null, startNode = null;
		for (int i=0; i<elems.getLength(); i++)
		{
			org.w3c.dom.Node n = elems.item(i);
			String name = n.getNodeName();
			if ("Graphics".equals(name))
			{
				attrib.add(n.getAttributes());
				NodeList pts = n.getChildNodes();
				boolean sourceAssigned = false;
				for (int j=0; j<pts.getLength(); j++)
				{
					org.w3c.dom.Node pt = pts.item(j);
					if ("Point".equals(pt.getNodeName()))
					{
						GPMLPoint gpt = new GPMLPoint(pt);
						points.add(gpt);
						String key = sourceAssigned ? "targetid" : "sourceid";
						sourceAssigned = true;
						attrib.put(key, gpt.getGraphRef());
						ArrowType type = gpt.getArrowType();
						if (type != null)
							attrib.put("ArrowHead", type.toString());
					}
					if ("Anchor".equals(pt.getNodeName()))
					{
						Anchor anchor = new Anchor(pt, m,attrib.get("GraphId"));
						anchors.add(anchor);
//						getController().addAnchor(anchor);
					}
				}
			}
			else if ("Xref".equals(name))	// suck the Xref element into our attributes
				attrib.add(n.getAttributes());
			else if ("BiopaxRef".equals(name))	// suck the BiopaxRef element into our attributes
				attrib.put("BiopaxRef", n.getTextContent());
		}
		//post parsing
		int z = points.size();
		if (z > 1)
		{
			GPMLPoint startPt = points.get(0);
			startId = startPt.getGraphRef();
			startNode = m.getDataNode(startId);
			attrib.put("sourceid", startId);
			GPMLPoint lastPt = points.get(z-1);
			endId = lastPt.getGraphRef();
			attrib.put("targetid", endId);
			endNode = m.getDataNode(endId);
			double thickness = attrib.getDouble("LineThickness");
			attrib.putDouble("LineThickness", thickness);
//			if (startNode != null && endNode != null) 
//				return interaction;	
//			else if (startPt != null && lastPt != null) 
//				return new Interaction(startPt, lastPt, thickness, model);		// Group
			}
		else
		{
			System.err.println("z = " + z);
			return null;
		}
		Interaction interaction = new Interaction(attrib, m,points, anchors);
		interaction.put("Layer", "Content");
		interaction.add(edgeML.getAttributes());
		interaction.copyAttributesToProperties();
		// copy attributes into properties for tree table editing
		interaction.copyAttributesToProperties();
		
		if (startNode != null && endNode != null)
		{
			interaction.setStartNode(startNode);
			interaction.setEndNode(endNode);
		}
		return interaction;
	}
	catch(Exception e)
	{
		e.printStackTrace();
		return null;
	}
	}	
	
	public DataNode parseGPMLLabel(org.w3c.dom.Node labelNode) {
		DataNode label = new DataNode(model);
		label.setType("Label");
		NodeList elems = labelNode.getChildNodes();
		label.add(labelNode.getAttributes());
		label.setGraphId(label.get("GraphId"));
		String txt = label.get("TextLabel");
		if (txt == null) txt = "Undefined";
		label.setName(txt);
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
					label.put("TextLabel", key + ":\n" + val);
					
				}
			}
			if ("Graphics".equals(name))
				label.add(child.getAttributes());
		}
//		attrMap.put("ShapeType", "Label");
		String shapeType = label.get("ShapeType");
		if (shapeType == null)
			label.put("ShapeType", "None");
				
		return label;
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

	private void parseBiopax(NodeList elements) {
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
	private void parseLabels(NodeList elements) {
		for (int i=0; i<elements.getLength(); i++)
		{
			org.w3c.dom.Node child = elements.item(i);
			String name = child.getNodeName();
			System.out.println(name);
			DataNode label = parseGPMLLabel(child);
			if (label != null)
			{
				model.getController().addLabel(label);
//				label.getStack().toBack();
			}
		}
	}
//	private void add(Node n)		{ 		controller.add(n);		}
//	private NodeFactory getNodeFactory() {		return controller.getNodeFactory();	}
	
	DataNodeGroup parseGPMLGroup(org.w3c.dom.Node child, NamedNodeMap attrs)
	{
		String groupId = "";
		if (attrs.getNamedItem("GroupId") != null)
			groupId = attrs.getNamedItem("GroupId").getNodeValue();
		String style = "";
		if (attrs.getNamedItem("Style") != null)
			style = attrs.getNamedItem("Style").getNodeValue();
		String graphId = "NONE";
		if (attrs.getNamedItem("GraphId") != null)
		{
			graphId = attrs.getNamedItem("GraphId").getNodeValue();
		}
		AttributeMap attrMap = new AttributeMap();
		attrMap.putAll("GraphId", graphId, "GroupId", groupId, "Style", style, "Fill", "808080", "LineStyle", "Broken", "ShapeType", "Octagon");
		DataNodeGroup newGroup = new DataNodeGroup(attrMap,model);
//		newGroup.put("Fill", "808080");
//		newGroup.put("LineStyle", "Broken");
//		newGroup.put("ShapeType", "Octagon");
//		if (!graphId.isEmpty())	newGroup.put("GraphId", graphId);
//		if (!style.isEmpty())	newGroup.put("Style", style);
//		if (!groupId.isEmpty())	newGroup.put("GroupId", groupId);

		newGroup.copyAttributesToProperties();
		newGroup.setName(newGroup.get("Style") + " [" + newGroup.get("GroupId") + "]");
		System.out.println("Making group with name = " + newGroup.getName());
		return newGroup;
	}
	
	//----------------------------------------------------------------------------
	private void parseGroups(NodeList elements) {			//TODO
		for (int i=0; i<elements.getLength(); i++)
		{
//if (i >= 0) continue;			//SKIP
			org.w3c.dom.Node child = elements.item(i);
			NamedNodeMap attrs = child.getAttributes();
			String name = child.getNodeName();
			System.out.println(name);
			if ("#text".equals(name)) continue;
			if ("Group".equals(name))
			{
				DataNodeGroup group = parseGPMLGroup(child, attrs);
				if (group != null)
					getController().addGroup(group);
			}
		}
		for (DataNodeGroup group : model.getGroups())
		{
			String id = group.get("GroupId");
			if (id != null)
			for (DataNode node : model.getNodes())
				if (id.equals(node.get("GroupRef")))
					group.addToGroup(node);
			group.calcBounds();
			VNode stack = group.getStack();
			
			stack.setBounds(group.getBounds());
			model.getController().add(stack);
			Shape shape = group.getStack().getFigure();
			if (shape != null)
			{
				shape.setStyle("-fx-stroke-dash-array: 10 10;");
				shape.setFill(Color.LIGHTGRAY);
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
