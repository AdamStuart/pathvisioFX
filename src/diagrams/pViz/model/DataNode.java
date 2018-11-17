package diagrams.pViz.model;

import diagrams.pViz.gpml.GPMLPoint;
import diagrams.pViz.view.VNode;
import javafx.geometry.Point2D;
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
 * Parent class XRefable is an AttributeMap with added properties to support binding in tables
 */
@SuppressWarnings("serial")
public class DataNode extends XRefable {

	Model model;
	protected VNode stack;
//	public DataNode(AttributeMap am, Controller c)
//	{
//		this(am,c.getModel());
//	}	
	public DataNode(Model m)
	{
		super();
		model = m;
	}	
	static int counter = 4000;
	static String getNextId()	{ return "id" + counter++; }
	public DataNode(AttributeMap am, Model m)
	{
		super(am);
		model = m;
		String id = get("GraphId");
		if (id == null)
			put("GraphId", id = model.gensym("G"));
		stack = new VNode(this, m.getController().getPasteboard());
	}

	public DataNode(DataNode orig, VNode view)
	{
		super();
		model = orig.model;
		stack = view;
	}
	

	public VNode getStack()					{		return stack;	}
	public void setStack(VNode st)			{		 stack = st;	}
	public Model getModel()					{		return model;	}
	public Object getResource(String id) 	{		return model.getDataNode(id);	}
	public Shape getShape() 				{		return getStack().getFigure();	}
//	public String getGraphId() 				{		return get("GraphId");	}
	public String getShapeType() 			{		return get("ShapeType");	}
	public String getType() 				{		return get("Type");	}
	public String getLabel() 				{		return get("TextLabel");	}
	public void rememberPosition() 			
	{		
		putDouble("X",  stack.getLayoutX());	
		putDouble("Y",  stack.getLayoutY());	
		putDouble("Width",  stack.getWidth());	
		putDouble("Height",  stack.getHeight());	
	}

	public String toString()	{ return "[" + getGraphId() + "] " + getLabel() + ' ' + getShapeType();  }
	public String getInfoStr()	{ return "HTML Template for " + getGraphId() + "\n" + toString();	}
//	@Override public String toString()	{ return getGraphId() + " = " + getName();	}
	//---------------------------------------------------------------------------------------
	public String toGPML()	{ 
		copyPropertiesToAttributes();
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
		String typ = get("Type");
		if ("Shape".equals(typ)) elementType = "Shape";
		else elementType = (isDataNode(typ)) ? "DataNode" : "Label"; 
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
	
	String[] attributeNames = { "CenterX", "CenterY", "Width", "Height", 
			"ShapeType", "ZOrder", "Valign", "FillColor", "Color", 
			"FontSize", "FontWeight", "FontStyle" };
	void buildGraphicsTag(StringBuilder bldr)
	{
		String attributes = attributeList(attributeNames);
		if (StringUtil.hasText(attributes))
			bldr.append( "<Graphics ").append(attributes).append( " >\n");
	}

	public Point2D  getAdjustedPoint(GPMLPoint gpmlPt)
	{
		Point2D center = getStack().center();
		if (gpmlPt == null) return center;
		double relX = gpmlPt.getRelX();
		double relY = gpmlPt.getRelY();
		double width = getStack().getWidth();
		double height = getStack().getHeight();
		double x = center.getX() + relX * width / 2;
		double y = center.getY() + relY * height / 2;
		return new Point2D(x,y);
	}
	
}
