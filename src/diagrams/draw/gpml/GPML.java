package diagrams.draw.gpml;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.NodeList;

import diagrams.draw.app.Controller;
import diagrams.draw.model.Edge;
import diagrams.draw.model.MNode;
import diagrams.draw.model.Model;
import diagrams.draw.model.NodeFactory;
import diagrams.draw.view.ShapeFactory;
import diagrams.draw.view.VNode;
import gui.Backgrounds;
import javafx.collections.ObservableMap;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import model.AttributeMap;
import util.FileUtil;
import util.StringUtil;

public class GPML {

	private Controller controller;

	public GPML(Controller c) {
		controller = c;
	}
	//----------------------------------------------------------------------------
	public void addFile(File f)
	{
		Document doc = FileUtil.openXML(f);
		if (doc != null) 
		{
			read(doc);
			controller.finishRead();
		}
	}

	//----------------------------------------------------------------------------
	public void read(org.w3c.dom.Document doc)
	{
		controller.clearComments();
		NodeList nodes = doc.getElementsByTagName("Pathway");
		for (int i=0; i<nodes.getLength(); i++)
		{
			org.w3c.dom.Node domNode = nodes.item(i);
			NamedNodeMap nodemap = domNode.getAttributes();
			String key = "", val = "";
			for (int j=0; j<nodemap.getLength(); j++)
			{
				org.w3c.dom.Node grandchild = nodemap.item(j);
				if (grandchild == null) continue;
				key = grandchild.getNodeName();
				val = grandchild.getNodeValue();
				if ("Name".equals(key) || "Organism".equals(key))
					controller.addComment(key, val);
			}
		
		}
		Label labl = new Label(controller.getComments());
		controller.add(labl);
		labl.setLayoutX(10);
		labl.setLayoutY(10);
		nodes = doc.getElementsByTagName("DataNode");
//		ShapeFactory f = controller.getNodeFactory().getShapeFactory();
		for (int i=0; i<nodes.getLength(); i++)
		{
			org.w3c.dom.Node child = nodes.item(i);
//			String name = child.getNodeName();
//			System.out.println(name);
			MNode node = parseGPML(child, controller);
			controller.getModel().addResource(node.getId(), node);
//			if (node != null)
//			{
//				VNode stack = node.getStack();
//				ObservableMap<Object, Object> map = stack.getProperties();
//				String colorStr = ""+map.get("Color");
//				Color c;
//				try
//				{   
//					c= Color.web(colorStr, 1.0);
//				}
//				catch(Exception e) { c = Color.BLACK;	}
//						
//				Object val = map.get("TextLabel");
//				String label = val == null ? "" : val.toString();
//				if (StringUtil.hasText(label)) {
//					final Label text = f.createLabel(label, c);
////			    	controller.getDependents().put(node, text);
//			    	stack.getChildren().add(text);
//			    	add(stack);
//				}
//				else	
//					add(stack);
//			}
		}
		handleBiopax(doc.getElementsByTagName("Biopax"));
		controller.updateTable();
		handleGroups(doc.getElementsByTagName("Groups"));
		handleLabels(doc.getElementsByTagName("Label"));
//		handleLabels(doc.getElementsByTagName("InfoBox"));
		
		NodeList edges = doc.getElementsByTagName("Interaction");
		for (int i=0; i<edges.getLength(); i++)
		{
			org.w3c.dom.Node child = edges.item(i);
//			String name = child.getNodeName();
//			System.out.println(name);
			
			if ("BiopaxRef".equals(child.getNodeName()))
			{
				
			}
			Edge edge = parseGPMLEdge(child, controller.getDrawModel());
			if (edge != null)
			{
				controller.add(0,edge);
				controller.getModel().addEdge(edge);
			}
		}
		handleShapes(doc.getElementsByTagName("Shape"));

	}
	private void handleShapes(NodeList shapes) {
		for (int i=0; i<shapes.getLength(); i++)
		{
			org.w3c.dom.Node child = shapes.item(i);
			MNode node = parseGPML(child, controller);
			if (node != null)
				controller.add(0, node.getStack());
		}
	}
	// **-------------------------------------------------------------------------------
	/*
	 * 	convert an org.w3c.dom.Node  a local node.  
	 * 
	 */
	public MNode parseGPML(org.w3c.dom.Node datanode, Controller ctrl) {
//		Model m = ctrl.getModel();
		AttributeMap attrMap = new AttributeMap();
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
		return  new MNode(attrMap, ctrl);
	}
		//----------------------------------------------------------------------------
	public Edge parseGPMLEdge(org.w3c.dom.Node edgeML, Model m) {
		AttributeMap attrMap = new AttributeMap();
		attrMap.add(edgeML.getAttributes());
		List<GPMLPoint> points = new ArrayList<GPMLPoint>();
		NodeList elems = edgeML.getChildNodes();
		String startId="", endId="";
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
				}
			}
			if ("Xref".equals(name))	
				attrMap.add(n.getAttributes());
			if ("BiopaxRef".equals(name))	
				attrMap.put("BiopaxRef", n.getTextContent());
		}
		int z = points.size();
		if (z > 1)
		{
			startId = points.get(0).getGraphRef();
			MNode startNode = m.getResource(startId);
			attrMap.put("start", startId);
			GPMLPoint lastPt = points.get(z-1);
			endId = lastPt.getGraphRef();
			attrMap.put("end", endId);
			MNode endNode = m.getResource(endId);
			if (endNode != null) 
				return new Edge(startNode.getStack(), endNode.getStack(), attrMap, points);			
		}

		return new Edge(attrMap, m);
	}	
	
	public Label parseGPMLLabel(org.w3c.dom.Node labelNode) {
		AttributeMap attrMap = new AttributeMap();
		NodeList elems = labelNode.getChildNodes();
		attrMap.add(labelNode.getAttributes());
		String txt = attrMap.get("TextLabel");
		if (txt == null) txt = "Undefined";
		Label label = new Label(txt);
//		label.setManaged(false);
		String name = "";
		for (int i=0; i<elems.getLength(); i++)
		{
			org.w3c.dom.Node child = elems.item(i);
			name = child.getNodeName();
			if (name != null && name.equals("TextLabel")) 
				label.setText(child.getNodeValue());
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
					label.setText(key + ":\n" + val);
					label.setTextFill(Color.CHOCOLATE);
					
				}
			}
			if (name != null && name.equals("Graphics")) 
				applyGraphicsNode(label, child);
		}
		return label;
	}


	private void applyGraphicsNode(Label label, org.w3c.dom.Node child) {
		NamedNodeMap attrs = child.getAttributes();
		String name = "";
		for (int i=0; i<attrs.getLength(); i++)
		{
			org.w3c.dom.Node item = attrs.item(i);
			String val = item.getNodeValue();
			double d = StringUtil.toDouble(val);
			name = item.getNodeName();
			
			if ("CenterX".equals(name)) 		 label.setLayoutX(d);
			else if ("CenterY".equals(name)) 	 label.setLayoutY(d);
			else if ("Width".equals(name)) 		 {	label.maxWidth(d); label.prefWidth(d);}
			else if ("Height".equals(name)) 	{	label.maxHeight(d); label.prefHeight(d);}
			else if ("ZOrder".equals(name)) 	{}
//			else if ("Color".equals(name)) {	label.setBorder(Borders.coloredBorder(val));}
			else if ("Color".equals(name)) 		label.setTextFill(Color.web(val));
			else if ("FillColor".equals(name)) 	label.setBackground(Backgrounds.colored(val));
			else if ("FontSize".equals(name)) 	{}
			else if ("FontWeight".equals(name)) {}
			else if ("Valign".equals(name)) 	{}
//			else if ("ShapeType".equals(name)) 	
//			{	if ("RoundedRectangle".equals(val)) {}		}
		}
		double w = StringUtil.toDouble(attrs.getNamedItem("Width").getNodeValue());
		double h = StringUtil.toDouble(attrs.getNamedItem("Height").getNodeValue());
//		label.getWidth();
//		double h = label.getWidth();
		label.setLayoutX(label.getLayoutX() - w / 2.);
		label.setLayoutY(label.getLayoutY() - h / 2.);
	}

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
					BiopaxRef ref = new BiopaxRef(gchild);
					controller.addRef(ref);
				}
			}
//			System.out.println(ref);					//TODO
		}
	}
	//----------------------------------------------------------------------------
	private void handleLabels(NodeList elements) {
		for (int i=0; i<elements.getLength(); i++)
		{
			org.w3c.dom.Node child = elements.item(i);
			String name = child.getNodeName();
			System.out.println(name);
			Label label = parseGPMLLabel(child);
			if (label != null)
				add(label);
		}
	}
	private void add(Node n)		{ 		controller.add(n);		}
	private NodeFactory getNodeFactory() {		return controller.getNodeFactory();	}
	private void handleGroups(NodeList elements) {			//TODO
		for (int i=0; i<elements.getLength(); i++)
		{
			org.w3c.dom.Node child = elements.item(i);
			String name = child.getNodeName();
			System.out.println(name);
		}
	}
	//----------------------------------------------------------------------------
	static String LINEDELIM = "\n";

	public static String dataNodeToGPML(VNode node)
	{
//		if (node instanceof Edge) 
//			return edgeToGPML((Edge) node);
		ObservableMap<Object, Object> pro = node.getProperties();
		Object o = pro.get("TextLabel");
		String textLabel = o == null ? "" : o.toString();
		o = pro.get("Type");
		String type = o == null ? "" : o.toString();
		String header = "<DataNode TextLabel=\"%s\" GraphId=\"%s\" Type=\"%s\" >\n";
		StringBuilder buffer = new StringBuilder(String.format(header, textLabel, node.getId(), type));

		String[] tokens = node.toString().split(" ");
		String shape = tokens.length > 1 ? tokens[1] : "Error";
//		basic = StringUtil.chopLast(basic);		// chop off "]"
//		int idx = basic.indexOf("[");
//		if (idx <= 0) return "";
			
//		String shape = basic.substring(0, idx);
//		basic = basic.replaceAll(",", "");		// strip commas
		double w = node.getLayoutBounds().getWidth();
		double h = node.getLayoutBounds().getHeight();
		double cx = node.getLayoutX() + w / 2;
		double cy = node.getLayoutY() + h / 2;
		if (node.getShape() instanceof Rectangle)
		{
			Rectangle sh = (Rectangle) node.getShape();
			cx = sh.getX() + w / 2;
			cy = sh.getY() + h / 2;
			if (sh.getArcWidth() > 0)
				shape = "RoundedRectangle";
		}
		String graphics1 = String.format("  <Graphics CenterX=\"%.2f\" CenterY=\"%.2f\" Width=\"%.2f\" Height=\"%.2f\" ZOrder=\"32768\" ", cx, cy, w, h);
		String graphics2 = String.format("FontWeight=\"%s\" FontSize=\"%d\" Valign=\"%s\" ShapeType=\"%s\"", "Bold", 12, "Middle", shape);
		buffer.append(graphics1).append(graphics2).append(" />\n") ;
		buffer.append("  <Xref Database=\"\" ").append("ID=\"\"").append("/>\n") ;
		buffer.append("</DataNode>"+ LINEDELIM);
		return buffer.toString();
	}
	
	public static String edgeToGPML(Edge edge)			// TODO -- Color, line style, etc. are missing
	{
		StringBuilder buffer = new StringBuilder("<Interaction>\n");
		buffer.append("<Graphics ConnectorType=\"Segmented\" ZOrder=\"12288\" LineThickness=\"1.0\">\n");
		buffer.append(edge.getPointsStr());
		buffer.append(edge.getAnchorsStr());
		buffer.append("</Graphics>\n");
		String db = edge.getDatabase();
		String id = edge.getDbId();
		buffer.append(String.format("<XRef Database=\"%s\" ID=\"%s\">\n", db, id));
		buffer.append("</Interaction>\n");
		return buffer.toString();
	}

	//----------------------------------------------------------------------------
	
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
