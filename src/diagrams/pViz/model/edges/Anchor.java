package diagrams.pViz.model.edges;

import java.util.List;

import diagrams.pViz.model.CXObject;
import diagrams.pViz.model.Model;
import diagrams.pViz.model.nodes.DataNode;
import diagrams.pViz.view.VNode;
import javafx.geometry.Point2D;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Shape;
import model.AttributeMap;

public class Anchor extends DataNode {	

	/**
	 *  An Anchor is a DataNode that lives along an edge of a line.  
	 *  Catalysis and Inhibition interactions connect to anchors
	 */
	
	private static final long serialVersionUID = 1L;
	private Interaction myInteraction;


	public Anchor(org.w3c.dom.Node node, Model m, int interactionId)
	{
		this(new AttributeMap(node.getAttributes()), m, interactionId);	
	}	
	
	public Anchor(AttributeMap attr, Model m, int interactionId)
	{
		super(attr, m);
		getStack().getFigure().visibleProperty().bind(m.getController().getInspector().anchorVisibleProperty());
		setName(String.format("Anchor @ %.2f", getAnchorPosition()));
		setInteraction(m.findInteractionById(interactionId));
	}

	//=========================================================================
	
	public boolean isAnchor() 						{	return true;}
	public Interaction getInteraction() 			{  	return myInteraction;	}
	public void setInteraction(Edge e) 		
	{  
		myInteraction = (Interaction) e;  
		setInteractionId (e == null ? -1 : e.getId());	
	}
	public int getInteractionId() 					{  	return  getInteger("InteractionId");	}
	public void setInteractionId(int e) 			{  	putInteger("InteractionId", e);	}
//	
	public double getAnchorPosition()				{  	return getDouble("Position");	}
	public void setPosition(double d)				{   putDouble("Position", d);	}

	//=========================================================================
	public void resetPosition(Edge caller)
	{
		VNode stack = getStack();
		Shape myShape = stack.getFigure();
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
			double radius = c.getRadius();
			stack.setLayoutX(pt.getX()-radius);
			stack.setLayoutY(pt.getY()-radius);
			putDouble("CenterX", pt.getX());
			putDouble("CenterY", pt.getY());
			putDouble("X", pt.getX()-radius);
			putDouble("Y", pt.getY()-radius);
		}
		List< Interaction>  links = model.findInteractionsByNode(this);
		for (Interaction e : links)
			if (e != caller)
				e.connect();
	}
	
//=========================================================================
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
		if (shape == null) shape = "Circle";
		return String.format("<Anchor Position=\"%.2f\" Shape=\"%s\" GraphId=\"%s\" />\n", getAnchorPosition(), shape, getGraphId());
	}
	
	public void toCX(CXObject cx)	{		cx.addAnchor(this);	}

}
