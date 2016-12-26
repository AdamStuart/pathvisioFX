package diagrams.pViz.gpml;

import java.util.List;

import diagrams.pViz.model.Edge;
import diagrams.pViz.model.MNode;
import diagrams.pViz.model.Model;
import javafx.geometry.Point2D;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Shape;
import model.AttributeMap;

public class Anchor extends MNode {

//	private double position;
//	private String type = "None";		
//	private String graphId = "aa";
//	private VNode stack;
//	private Shape shape;
	private Edge edge;
	public Edge getEdge() 				{  return edge;	}
	public void setEdge(Edge e) 		{  edge = e;	}
//	
	public String getGraphId()			{  return getAttributeMap().get("getGraphId");	}
	public double getPosition()			{  return getAttributeMap().getDouble("Position");	}
	public void setPosition(double d)	{   getAttributeMap().putDouble("Position", d);	}
//	public void setPosition(double p)	{  position = p;	}
//
//	public String getType()				{ return type;	}
//	public void setType(String p)		{  type = p;	}
//	
//	public VNode getStack()				{ return stack;	}
//	public void setStack(VNode p)		{  stack = p;	}
//	
//	public String getGraphId()			{ return graphId;	}
//	public void setGraphId(String p)	{  graphId = p;	}
	
	
	public Anchor(org.w3c.dom.Node node, Model m)
	{
		super(new AttributeMap(node.getAttributes(), "ShapeType", "Circle"), m);
		getAttributeMap().putDouble("CenterX", 200);
		getAttributeMap().putDouble("CenterY", 200);
		getAttributeMap().putDouble("Radius", 2);
		m.addResource(this);
//		for (int i=0; i<node.getAttributes().getLength(); i++)
//		{
//			org.w3c.dom.Node child = node.getAttributes().item(i);
//			String name = child.getNodeName();
//			if ("Position".equals(name))  		position = StringUtil.toDouble(child.getNodeValue());
//			else if ("ArrowHead".equals(name))  type = child.getNodeValue();
//			else if ("GraphId".equals(name))  	graphId = child.getNodeValue();
//			else if ("Shape".equals(name))  	type = child.getNodeValue();
//		}
		
	}
	String getStartName()	{ return edge.getStartNode().getText();		}
	String getEndName()		{ return edge.getEndNode().getText();		}
	public String toString()
	{
		String type =  getAttributeMap().get("Type");
		if (type == null) type = "Unspecified";		
		return String.format("<Anchor Position=\"%.2f\" Type=\"%s\" On Edge from %s to %s/>", getPosition(), type, getStartName(), getEndName());
	}
	public String toGPML()
	{
		String shape =  getAttributeMap().get("Shape");
		if (shape == null) shape = "None";
		return String.format("<Anchor Position=\"%.2f\" Shape=\"%s\" GraphId=\"%s\" />\n", getPosition(), shape, getGraphId());
	}

	public void resetPosition(Edge caller)
	{
		double position = getPosition();
		Point2D pt = edge.getEdgeLine().getPointAlongLine(position);
		String myId = getId();
		getStack().setCenter(pt);
		getStack().setWidth(4);
		getStack().setHeight(4);
		Shape fig = getStack().getFigure();
		if (fig instanceof Circle) 
			((Circle)(fig)).setRadius(2);
		List<Edge> edges =  getModel().getEdgeList(myId);
		for (Edge e : edges)
			if (e != caller)
				e.connect();
		
	}
		

		

//		System.out.println("Setting Anchor position to " + StringUtil.asString(pt));
//		
//		Shape shape = getStack().getShapeLayer();
//		double r = 10;
//		if (shape instanceof Circle)
//		{
//			double x = pt.getX();
//			double y = pt.getY();
//			Circle c = ((Circle)shape);
//			c.setCenterX(x);
//			c.setCenterY(y);
//			c.setRadius(r);
//			VNode stack = getStack();
//			MNode mnode = stack.getModel();
//			AttributeMap am = mnode.getAttributeMap();
//			am.putDouble("CenterX", x);
//			am.putDouble("CenterY", y);
//			am.putDouble("X", x-r);
//			am.putDouble("Y", y-r);
//			stack.setWidth(r * 2);
//			stack.setHeight(r * 2);
////			edge.getEdgeLine().setLastPoint(pt);
//			System.out.println("reset postion of " + getId() + " to " + StringUtil.asString(pt));
//		}
//		else 
//			System.out.println("shape is not a circle");

	public Shape getShape()
	{
		Shape sh = getStack().getShape();
		if (sh == null)
			getStack().setShape(sh = new Circle(40));
		sh.setStrokeWidth(0);
		sh.setFill(Color.TRANSPARENT);
		return sh;
	}
	public void setShape(Shape p)		{  getStack().setShape(p);	}


}
