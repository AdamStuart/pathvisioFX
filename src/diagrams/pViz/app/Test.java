package diagrams.pViz.app;

import java.util.Map;

import diagrams.pViz.model.Model;
import diagrams.pViz.model.edges.Edge;
import diagrams.pViz.model.edges.Interaction;
import diagrams.pViz.model.nodes.DataNode;
import diagrams.pViz.view.Pasteboard;
import gui.Action.ActionType;
import javafx.geometry.Bounds;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;
import javafx.scene.text.Text;
import model.AttributeMap;
import util.StringUtil;

public class Test {

	static protected void test1(Controller c)
	{
		c.getUndoStack().push(ActionType.Test);	
		Model model = c.getModel();
		AttributeMap attrMap = new AttributeMap();
		attrMap.putFillStroke(Color.PINK, Color.INDIGO);
		attrMap.putCircle(new Circle(120, 230, 40));
		attrMap.put("TextLabel", "root");
		DataNode n1 = new DataNode(attrMap, model);
		c.add(n1.getStack());
	
		attrMap.putFillStroke(Color.CORNSILK, Color.BLUE);
		attrMap.putCircle(new Circle(220, 130, 60));
		attrMap.setTool(Tool.Circle.toString()); 
		DataNode circ = new DataNode(attrMap, model);		//, "Eli"
		c.add(circ.getStack());
	
		attrMap.putFillStroke(Color.LIGHTSKYBLUE, Color.DARKOLIVEGREEN);
		attrMap.putCircle(new Circle(220, 330, 60));
		DataNode n3 = new DataNode(attrMap, model);	//, "Fristcut"
		c.add(n3.getStack());
		
		Interaction line1 = model.addInteraction(circ, n3, "Content");		c.addInteraction(line1);
		Interaction line2 = model.addInteraction(circ, n1, "Content");		c.addInteraction(line2);
		
		Rectangle r1 = new Rectangle(290, 230, 60, 60);
		attrMap.setTool(Tool.Rectangle.toString());
		attrMap.putRect(r1);
		attrMap.putFillStroke(Color.CORNSILK, Color.DARKOLIVEGREEN);
		DataNode n4 = new DataNode(attrMap, model);
		c.add(n4.getStack());
	}
	static protected void test2(Controller c)
	{
		Model model = c.getModel();
		c.getUndoStack().push(ActionType.Test);	
		double WIDTH = 20;
		double HEIGHT = 20;
		double RADIUS = 10;
		double spacer = 5 * RADIUS;
//		ShapeFactory f = getNodeFactory().getShapeFactory();
		AttributeMap attrMap = new AttributeMap();
		attrMap.putFillStroke(Color.PINK, Color.INDIGO, 1.0);
		for (int i=0; i<WIDTH; i++)
			for (int j=0; j<HEIGHT; j++)
			{
				Circle c1 = new Circle(i * spacer, j * spacer, RADIUS);
				attrMap.putCircle(c1);
				attrMap.put("ShapeType","Circle");
				attrMap.put("GraphId", i + ", " + j);
				c.add(new DataNode(attrMap, model).getStack());
			}
	}
	
	static protected void test3(Controller c)
	{
//		addAll(new GPML(this).makeTestItems());
	}
	
	// **-------------------------------------------------------------------------------
		static String XMLHEAD = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n";
		static String GraphicsHEAD = "<Graphics BoardWidth=\"%d\" BoardHeight=\"%d\" />\n";
		public static StringBuilder traverseSceneGraph(Pane root, Model model)
		{
			StringBuilder buff = new StringBuilder(XMLHEAD);
			buff.append("<Pathway>\n");
//			Pasteboard board  = controller.getPasteboard();
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
			for (Edge e : model.getEdges())
				buff.append(e.toString() + "\n");
			buff.append("</Pathway>\n");
			return buff;
		}
		
		static private void traverse(StringBuilder buff, Node node, int indent)
		{
//			VNode stack = node.getStack();
			if (Pasteboard.isMarquee(node)) return;
//			if (node instanceof Edge)			buff.append(describe(node));	
			if (node instanceof Shape)			buff.append(StringUtil.spaces(indent) + Model.describe(node) + "\n");	
			if (node instanceof Group)			buff.append(StringUtil.spaces(indent) + Model.describe(node) + "\n");	
			if (node instanceof StackPane)		buff.append(StringUtil.spaces(indent) + Model.describe(node) + "\n");
			if (node instanceof Parent)
				for (Node n : ((Parent) node).getChildrenUnmodifiable())
				{
					String id = n.getId();
//					if (id == null)					continue;			// only propagate thru nodes with ids
					if ("Marquee".equals(id) )		continue;
					
					if (n instanceof Text)
					{
						String txt = ((Text) n).getText();
						if (txt.length() < 1) 	continue;				//System.out.println("Don't stream empty text");
					}
					traverse(buff, n, indent+1);
				}
		}

		public static void dumpViewHierarchy(Model model) {
			String out = "\n" + Test.traverseSceneGraph( model.getController().getPasteboard(), model);
			System.out.println(out);
		}
		
		public static void dumpEdgeTable(Model model) {
			System.out.println("\n" + model.getInteractions().size());
			for (Edge e : model.getEdges())
				System.out.println(e);
			}
		static public void dumpNodeTable(Map<Integer, DataNode> dataNodeMap) {
			System.out.println(dataNodeMap.keySet().size());
			for (int key : dataNodeMap.keySet())
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
				else s = "" + key;
				System.out.println(key + "\n" + s);
			}
		}


}
