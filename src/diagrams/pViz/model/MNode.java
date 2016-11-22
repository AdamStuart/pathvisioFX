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
		this(am,c.getModel(), c.getPasteboard());
	}	
	
	public MNode(AttributeMap am, Model m, Pasteboard p)
	{
		attributes = am;
		model = m;
		stack = new VNode(this, p);
		stack.setLayoutX(am.getDouble("X"));
		stack.setLayoutY(am.getDouble("Y"));
	}

	public VNode getStack()					{		return stack;	}
	public Model getModel()					{		return model;	}
	public Object getResource(String id) 	{		return model.getResource(id);	}
	public String getId() 					{		return attributes.get("GraphId");	}
	public Shape getShape() 				{		return getStack().getShapeLayer();	}
	public String getShapeType() 			{		return attributes.get("ShapeType");	}
	public AttributeMap getAttributeMap() 	{		return attributes;	}
	
	@Override public String toString()	{ return getId() + " = " + attributes.toString();	}
}
