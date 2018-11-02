package diagrams.pViz.model;

import diagrams.pViz.app.Controller;
import diagrams.pViz.view.VNode;
import javafx.scene.shape.Shape;
import model.AttributeMap;
import model.bio.XRefable;
import util.StringUtil;

/*
 * Model Node
 * A data structure that contains all persistent attributes
 * of a node in our graph. 
 * 
 * It is in charge of the VNode which is a StackPane in the view system.
 */
public class DataNodeState extends XRefable {

	protected VNode hoststack;
	protected Model model;
//	public DataNode(AttributeMap am, Controller c)
//	{
//		this(am,c.getModel());
//	}	
	public DataNodeState(Model m)
	{
		super();
		model = m;
	}	


	public DataNodeState(AttributeMap am, Model m)
	{
		super(am);
		model = m;
		copyAttributesToProperties();
		setName("State: " + get("TextLabel"));
		
//		stack = new VNode(this, m.getController().getPasteboard());
	}

	public Model getModel()					{		return model;	}
	public String getShapeType() 			{		return get("ShapeType");	}
	public String getType() 				{		return get("Type");	}
	public String getLabel() 				{		return get("TextLabel");	}

	public String getInfoStr()	{ return "HTML Template for " + getGraphId() + "\n" + toString();	}
//	@Override public String toString()	{ return getGraphId() + " = " + getName();	}
	//---------------------------------------------------------------------------------------
	public String toGPML()	{ 
		StringBuilder bldr = new StringBuilder();
		buildNodeOpen(bldr);
		buildXRefTag(bldr);
		buildNodeClose(bldr);
		return bldr.toString();
	}
	String elementType = "State";
	String[]  nodeAttrs = {  "TextLabel", "GraphId", "Type"};
	private void buildNodeOpen(StringBuilder bldr) {
		bldr.append("<" + attributeList(nodeAttrs) + ">\n");
	}
	private void buildNodeClose(StringBuilder bldr) {
		bldr.append("</" + elementType + ">\n");
	}	
}
