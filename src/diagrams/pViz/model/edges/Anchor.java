package diagrams.pViz.model.edges;

import diagrams.pViz.model.CXObject;
import diagrams.pViz.model.Model;
import diagrams.pViz.model.nodes.DataNode;
import javafx.geometry.Point2D;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Shape;
import model.AttributeMap;

public class Anchor extends DataNode {		// extends DataNode

	/**
	 *  An Anchor is a DataNode that lives along an edge of a line.  
	 *  Catalysis and Inhibition interactions connect to anchors
	 */
	
	private static final long serialVersionUID = 1L;
	private Circle myShape;
	private Interaction myInteraction;
//	private String interId;

	
	public Interaction getInteraction() 				{  return myInteraction;	}
	public void setInteraction(Edge e) 		
	{  
		myInteraction = (Interaction) e;  
		setInteractionId (myInteraction == null ? 0 : myInteraction.getInteger("GraphId"));	
	}
	public int getInteractionId() 				{  return  getInteger("InteractionId");	}
	public void setInteractionId(int e) 			{  putInteger("InteractionId", e);	}
//	
	public double getAnchorPosition()				{  return getDouble("Position");	}
	public void setPosition(double d)				{   putDouble("Position", d);	}
	public Shape getShape()							{	return myShape;}

	public Anchor(org.w3c.dom.Node node, Model m, int interactionId)
	{
		this(new AttributeMap(node.getAttributes()), m, interactionId);	
	}	
	
	public Anchor(AttributeMap attr, Model m, int interactionId)
	{
		super(attr, m);
		myShape = new Circle();
		myShape.setRadius(4);
		myShape.visibleProperty().bind(m.getController().getInspector().anchorVisibleProperty());
		myShape.setFill(Color.BISQUE);
		myShape.setStroke(Color.DARKOLIVEGREEN);
		setName(String.format("Anchor @ %.2f", getAnchorPosition()));
		setInteractionId(interactionId);
	}
	
	public String toString()
	{
		String type =  getType();
		if (type == null) type = get("Type") ;		
		if (type == null) type = "Unspecified";		
		return String.format("Anchor of type=\"%s\" />", type);  // On Edge from %s to %s   , getStartName(), getEndName()
	}
	
	public String toGPML()
	{
		String shape = get("Shape");
		if (shape == null) shape = "Oval";
		return String.format("<Anchor Position=\"%.2f\" Shape=\"%s\" GraphId=\"%s\" />\n", getAnchorPosition(), shape, getGraphId());
	}
	
	public void toCX(CXObject cx)
	{
		String shape = get("Shape");
		cx.addAnchor(this);
	}

//	public void setAnchorPosition(Interaction i) {		resetPosition(i);	}
	public void resetPosition(Edge caller)
	{
		double position = getAnchorPosition();
		if (Double.isNaN(position))
			position = 0.5;
		if (caller == null) return;
		EdgeLine edgeLine = caller.getEdgeLine();
		Point2D pt = edgeLine.getPointAlongLine(position);
		if (!edgeLine.getChildren().contains(myShape))
			edgeLine.getChildren().add(myShape);
//		String myId = get("GraphId");
//		System.out.println(String.format("position: %.2f id: %s @ [ %.2f, %.2f] ",position, myId,  pt.getX(), pt.getY()));
		if (myShape instanceof Circle) 
		{
			Circle c = ((Circle)(myShape));
			c.setCenterX(pt.getX());
			c.setCenterY(pt.getY());
		}
		Interaction e  = getInteraction();
		if (e != null && e != caller)
			e.connect();
	}
}
