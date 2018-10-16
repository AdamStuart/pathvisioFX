package diagrams.pViz.model;

import diagrams.pViz.app.Controller;
import diagrams.pViz.view.Pasteboard;
import diagrams.pViz.view.VNode;
import javafx.scene.shape.Shape;
import model.AttributeMap;
import util.StringUtil;

/*
 * Model Node
 * A data structure that contains all persistent attributes
 * of a node in our graph. 
 * 
 * It is in charge of the VNode which is a StackPane in the view system.
 */
public class MNode {

	String id;
	AttributeMap attributes;
	protected VNode stack;
	protected Model model;
	public MNode(AttributeMap am, Controller c)
	{
		this(am,c.getModel());
	}	
	static int counter = 4000;
	static String getNextId()	{ return "id" + counter++; }
	public MNode(AttributeMap am, Model m)
	{
		id = am.get("GraphId");
		if (id == null) id = am.get("id");
		if (id == null) id = getNextId();
		attributes = am;
		model = m;
		stack = new VNode(this, m.getController().getPasteboard());
	}

	public MNode(MNode orig)
	{
		id = getNextId();
		attributes = new AttributeMap(orig.getAttributeMap());
		model = orig.model;
		stack = new VNode(this, model.getController().getPasteboard());
	}

	public VNode getStack()					{		return stack;	}
	public Model getModel()					{		return model;	}
	public Object getResource(String id) 	{		return model.getResource(id);	}
	public String getId() 					{		return id;  }   //attributes.get("GraphId");	}
	public Shape getShape() 				{		return getStack().getFigure();	}
	public String getGraphId() 				{		return attributes.get("GraphId");	}
	public String getShapeType() 			{		return attributes.get("ShapeType");	}
	public String getType() 				{		return attributes.get("Type");	}
	public String getLabel() 				{		return attributes.get("TextLabel");	}
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
	//---------------------------------------------------------------------------------------
	public String toGPML()	{ 
		StringBuilder bldr = new StringBuilder();
		buildNodeOpen(bldr);
		buildAttributeTag(bldr);
		buildGraphicsTag(bldr);
		buildXRefTag(bldr);
		buildNodeClose(bldr);
		return bldr.toString();
	}
	String elementType;
	String[]  nodeAttrs = {  "TextLabel", "GraphId", "Type"};
	String[]  dataNodeTypes = {  "Gene", "GeneProduct", "Protein", "Metabolite", "RNA"};
	boolean isDataNode(String typ) {
		if (typ == null) return false;
		for (String t : dataNodeTypes)
			if (t.equals(typ))  return true;
		return false;
	}
	private void buildNodeOpen(StringBuilder bldr) {
		String typ = attributes.get("Type");
		elementType = (isDataNode(typ)) ? "DataNode" : "Label";
		bldr.append("<" + elementType + " " + attributeList(nodeAttrs) + ">\n");
	}
	private void buildNodeClose(StringBuilder bldr) {
		bldr.append("</" + elementType + ">\n");
	}

	String[]  attrs = {  "Key", "Value"};
	void buildAttributeTag(StringBuilder bldr)
	{
		String attributes = attributeList(attrs);
		if (StringUtil.hasText(attributes))
			bldr.append( "<Attribute ").append(attributes).append( " >\n");
	}
	String[]  xrefattrs = {  "Database", "ID"};
	void buildXRefTag(StringBuilder bldr)
	{
		String attributes = attributeList(xrefattrs);
		if (StringUtil.hasText(attributes))
			bldr.append( "<Xref ").append(attributes).append( " >\n");
	}
	
	private String attributeList(String[] strs)
	{
		StringBuilder bldr = new StringBuilder();
		for (String attr : strs)
		{
			String val = attributes.get(attr);
			if (val != null)
				bldr.append(attr + "=\"" + val + "\" ");
		}
		return bldr.toString();
	}
	
	String[] attributeNames = { "CenterX", "CenterY", "Width", "Height", 
			"ShapeType", "ZOrder", "Valign", "FillColor", "Color", 
			"FontSize", "FontWeight", "FontStyle" };
	void buildGraphicsTag(StringBuilder bldr)
	{
		String attributes = attributeList(attributeNames);
		if (StringUtil.hasText(attributes))
			bldr.append( "<Graphics ").append(attributes).append( " >\n");
	}
}
