package diagrams.pViz.app;

import diagrams.pViz.model.Interaction;
import diagrams.pViz.model.DataNode;
import diagrams.pViz.model.Model;
import gui.Action.ActionType;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import model.AttributeMap;

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
}
