package diagrams.draw.gpml;

import java.io.File;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.NodeList;

import diagrams.draw.Controller;
import diagrams.draw.Edge;
import diagrams.draw.Model;
import diagrams.draw.NodeCenter;
import diagrams.draw.NodeFactory;
import diagrams.draw.ShapeFactory;
import diagrams.draw.Tool;
import javafx.collections.ObservableMap;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;
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
		if (doc != null) read(doc);
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
		ShapeFactory f = controller.getNodeFactory().getShapeFactory();
		for (int i=0; i<nodes.getLength(); i++)
		{
			org.w3c.dom.Node child = nodes.item(i);
//			String name = child.getNodeName();
//			System.out.println(name);
			Node node = controller.getNodeFactory().parseGPML(child);
			if (node != null)
			{
				ObservableMap<Object, Object> map = node.getProperties();
				String colorStr = ""+map.get("Color");
				Color c;
				try
				{
					c= Color.web(colorStr, 1.0);
				}
				catch(Exception e) { c = Color.BLACK;	}
						
				Object val = map.get("TextLabel");
				String label = val == null ? "" : val.toString();
				if (StringUtil.hasText(label)) {
					final Label text = f.createLabel(label, c);
			    	NodeCenter ctr = new NodeCenter(node);
			    	text.layoutXProperty().bind(ctr.centerXProperty().subtract(text.widthProperty().divide(2.)));
			    	text.layoutYProperty().bind(ctr.centerYProperty().subtract(text.heightProperty().divide(2.)));
			    	controller.getDependents().put(node, text);
					add(node);
					add(text);
				}
				else	controller.add(node);
			}
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
			Edge edge = controller.getEdgeFactory().parseGPML(child);
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
//			String name = child.getNodeName();
//			System.out.println(name);
			Node node = controller.getNodeFactory().parseGPML(child);
			if (node != null)
				controller.add(0, node);
		}
	}
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
			Label label = getNodeFactory().parseGPMLLabel(child);
			if (label != null)
				add(label);
		}
	}
	private void add(Node n)	{ controller.add(n);	}
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

	public static String dataNodeToGPML(Node node)
	{
		if (node instanceof Edge) 
			return edgeToGPML((Edge) node);
		ObservableMap<Object, Object> pro = node.getProperties();
		Object o = pro.get("TextLabel");
		String textLabel = o == null ? "" : o.toString();
		o = pro.get("Type");
		String type = o == null ? "" : o.toString();
		String header = "<DataNode TextLabel=\"%s\" GraphId=\"%s\" Type=\"%s\" >\n";
		StringBuilder buffer = new StringBuilder(String.format(header, textLabel, node.getId(), type));
		String basic = node.toString();
		basic = StringUtil.chopLast(basic);		// chop off "]"
		int idx = basic.indexOf("[");

		String shape = basic.substring(0, idx);
		basic = basic.replaceAll(",", "");		// strip commas
		double w = node.getLayoutBounds().getWidth();
		double h = node.getLayoutBounds().getHeight();
		double cx = node.getLayoutX() + w / 2;
		double cy = node.getLayoutY() + h / 2;
		if (node instanceof Rectangle)
		{
			Rectangle sh = (Rectangle) node;
			cx = sh.getX() + w / 2;
			cy = sh.getY() + h / 2;
			if (sh.getArcWidth() > 0)
			{
				shape = "RoundedRectangle";
			}
		}
		String graphics1 = String.format("  <Graphics CenterX=\"%.2f\" CenterY=\"%.2f\" Width=\"%.2f\" Height=\"%.2f\" ZOrder=\"32768\" ", cx, cy, w, h);
		String graphics2 = String.format("FontWeight=\"%s\" FontSize=\"%d\" Valign=\"%s\" ShapeType=\"%s\"", "Bold", 12, "Middle", shape);
		buffer.append(graphics1).append(graphics2).append(" />\n") ;
		buffer.append("  <Xref Database=\"\" ").append("ID=\"\"").append("/>\n") ;
		buffer.append("</DataNode>"+ LINEDELIM);
		return buffer.toString();
	}
	
	public static String edgeToGPML(Edge edge)
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
	
	public Shape shapeFromGPML(String gpmlStr,  AttributeMap attrMap, boolean addHandlers) {
		String txt = gpmlStr.trim();
		if (txt.startsWith("<DataNode "))
		{
			String attrs = txt.substring(10, txt.indexOf(">"));
			attrMap.addGPML(attrs);
			String graphics =  txt.substring(10 + txt.indexOf("<Graphics "), txt.indexOf("</Graphics>"));
			String xref = txt.substring(10 + txt.indexOf(6 + "<Xref "), txt.indexOf("</Xref>"));
			attrMap.addGPML(graphics);
			attrMap.addGPML(xref);
		}
		String shapeType = attrMap.get("ShapeType");
		Shape newShape = controller.getNodeFactory().getShapeFactory().makeNewShape(shapeType, attrMap, addHandlers); 
		return newShape;
	}

	public static Edge createEdge(String txt, AttributeMap attrMap, Model model) 
	{
		String graphics =  txt.substring(10 + txt.indexOf("<Graphics "), txt.indexOf("</Graphics>"));
		String xref = txt.substring(10 + txt.indexOf(6 + "<Xref "), txt.indexOf("</Xref>"));
		attrMap.addGPMLEdgeInfo(graphics);
		attrMap.addGPML(xref);
		return new Edge(attrMap, model);
	}

	//----------------------------------------------------------------------------
	public Node[] makeTestItems() {
		Node a,b,c,d,e,f,g,h,i,j,k, l;
		ShapeFactory factory = controller.getNodeFactory().getShapeFactory();
		AttributeMap attrMap = new AttributeMap();
		
		attrMap.putCircle(new Circle(150, 150, 60, Color.AQUA));
		attrMap.put("TextLabel", "Primary");
		Label label = factory.createLabel("Primary", Color.AQUA);
		a = label;
		Circle cir = (Circle) factory.makeNewShape(Tool.Circle, attrMap);
		b = cir;
		label.layoutXProperty().bind(cir.centerXProperty().subtract(label.widthProperty().divide(2.)));
		label.layoutYProperty().bind(cir.centerYProperty().subtract(label.heightProperty().divide(2.)));
		
		attrMap = new AttributeMap();
		attrMap.putCircle(new Circle(180, 450, 60, Color.AQUA));
		attrMap.put("TextLabel", "Secondary");
		c = factory.makeNewShape(Tool.Circle, attrMap);
		attrMap = new AttributeMap();
		attrMap.putCircle(new Circle(150, 300, 20, Color.BEIGE));
		attrMap.put("TextLabel", "Tertiary");
		d = factory.makeNewShape(Tool.Circle, attrMap);
		attrMap = new AttributeMap();
		attrMap.putRect(new Rectangle(250, 50, 30, 30));
		e = factory.makeNewShape(Tool.Rectangle, attrMap);
		attrMap = new AttributeMap();
		attrMap.putRect(new Rectangle(250, 450, 30, 50));
		f = factory.makeNewShape(Tool.Rectangle, attrMap);
		
		g = new Edge(b, c).getPolyline();
		h = new Edge(d, b).getPolyline();
		i = new Edge(f, b).getPolyline();
		j = new Edge(f, c).getPolyline();
		k = new Edge(e, f).getPolyline();
		l = new Edge(e, d).getPolyline();
		return new Node[] { a,b,c,d,e,f,g,h,i,j,k,l };
	}

}
