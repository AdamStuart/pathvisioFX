package diagrams.pViz.gpml;

import diagrams.pViz.model.Model;
import diagrams.pViz.model.edges.Edge;
import diagrams.pViz.model.edges.EdgeLine;
import diagrams.pViz.model.edges.Interaction;
import javafx.geometry.Point2D;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Shape;
import model.AttributeMap;
import model.bio.XRefable;

public class Anchor extends XRefable {		// extends DataNode

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
		super(new AttributeMap(node.getAttributes()));
		myShape = new Circle();
		myShape.setRadius(4);
		myShape.visibleProperty().bind(m.getController().getInspector().anchorVisibleProperty());
		myShape.setFill(Color.BISQUE);
		myShape.setStroke(Color.DARKOLIVEGREEN);
		setName(String.format("Anchor @ %.2f", getAnchorPosition()));
//		setInteraction(inter);
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
		if (e != caller)
			e.connect();
	}
}
