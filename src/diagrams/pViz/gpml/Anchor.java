package diagrams.pViz.gpml;

import java.util.List;

import diagrams.pViz.model.Edge;
import diagrams.pViz.model.Interaction;
import diagrams.pViz.model.DataNode;
import diagrams.pViz.model.Model;
import javafx.geometry.Point2D;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Shape;
import model.AttributeMap;

public class Anchor extends DataNode {

	private Interaction inter;
	public Interaction getInteraction() 				{  return inter;	}
	public void setInteraction(Interaction e) 		{  inter = e;	}
//	
//	public String getGraphId()			{  return get("getGraphId");	}
	public double getAnchorPosition()			{  return getDouble("Position");	}
	public void setPosition(double d)	{   putDouble("Position", d);	}

	
	public Anchor(org.w3c.dom.Node node, Model m, Interaction inter)
	{
		super(new AttributeMap(node.getAttributes(), "ShapeType", "Circle"), m);
		copyAttributesToProperties();
		setType("Anchor");
		setName("Anchor: " + get("GraphId"));
//		setGraphId(get("GraphId"));
		setInteraction(inter);

		putDouble("CenterX", 0);
		putDouble("CenterY", 0);
		putDouble("Radius", 2);		
	}
	
	String getInteractionGraphId()	{ return inter.getGraphId();		}
	String getStartName()	{ return inter.getStartName();		}
	String getEndName()		{ return inter.getEndName();		}
	public String toString()
	{
		String type =  getType();
		if (type == null) type = get("Type") ;		
		if (type == null) type = "Unspecified";		
		return String.format("Anchor Type=\"%s\" On Edge from %s to %s/>", type, getStartName(), getEndName());
	}
	public String toGPML()
	{
		String shape =  get("Shape");
		if (shape == null) shape = "None";
		
		return String.format("<Anchor Position=(%.2f, %.2f) Shape=\"%s\" GraphId=\"%s\" />\n", getPosition().getX(), getPosition().getY(), shape, getGraphId());
	}

	public void resetPosition(Edge caller)
	{
		double position = getAnchorPosition();
		Point2D pt = inter.getEdgeLine().getPointAlongLine(position);
		String myId = getGraphId();
		getStack().setCenter(pt);
		getStack().setWidth(4);
		getStack().setHeight(4);
		Shape fig = getStack().getFigure();
		if (fig instanceof Circle) 
			((Circle)(fig)).setRadius(2);
		List<Interaction> edges =  getModel().getInteractionList(myId);
		for (Interaction e : edges)
			if (e != caller)
				e.connect();
		
	}

	public Shape getShape()
	{
		Shape sh = getStack().getShape();
		if (sh == null)
			getStack().setShape(sh = new Circle(4));
		sh.setStrokeWidth(0);
		sh.setFill(Color.MAGENTA);
//		sh.setFill(Color.TRANSPARENT);
		return sh;
	}
	public void setShape(Shape p)		{  getStack().setShape(p);	}


}
