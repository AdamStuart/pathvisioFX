package diagrams.pViz.model;

import diagrams.pViz.app.Controller;
import diagrams.pViz.view.Pasteboard;
import diagrams.pViz.view.VNode;
import javafx.scene.shape.Shape;
import model.AttributeMap;

/*
 * Model Node
 * A data structure that contains all persistent attributes
 * of a node in our graph. 
 * 
 * It is in charge of the VNode which is a StackPane in the view system.
 */
public class MNode {

	AttributeMap attributes;
	VNode stack;
	Model model;
	public MNode(AttributeMap am, Controller c)
	{
		this(am,c.getModel());
	}	
	
	public MNode(AttributeMap am, Model m)
	{
		attributes = am;
		model = m;
		stack = new VNode(this, m.getController().getPasteboard());
	}

	public VNode getStack()					{		return stack;	}
	public Model getModel()					{		return model;	}
	public Object getResource(String id) 	{		return model.getResource(id);	}
	public String getId() 					{		return attributes.get("GraphId");	}
	public Shape getShape() 				{		return getStack().getShapeLayer();	}
	public String getShapeType() 			{		return attributes.get("ShapeType");	}
	public AttributeMap getAttributeMap() 	{		return attributes;	}
	public void rememberPosition() 			
	{		
		attributes.putDouble("X",  stack.getLayoutX());	
		attributes.putDouble("Y",  stack.getLayoutY());	
		attributes.putDouble("Width",  stack.getWidth());	
		attributes.putDouble("Height",  stack.getHeight());	
	}

	public String getInfoStr()	{ return "HTML Template for " + getId() + "\n" + attributes.toString();	}
	@Override public String toString()	{ return getId() + " = " + attributes.toString();	}
}
