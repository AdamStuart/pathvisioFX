package diagrams.pViz.model.nodes;

import diagrams.pViz.gpml.GPMLPoint;
import diagrams.pViz.model.Model;
import diagrams.pViz.view.VNode;
import javafx.collections.ObservableMap;
import javafx.geometry.Point2D;
import javafx.scene.shape.Rectangle;
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

	protected Model model;
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
		if (id == null) id = model.gensym("C");
		put("GraphId", id);
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
		double width =  stack.getWidth();
		double height =  stack.getHeight();
		
		putDouble("X",  stack.getLayoutX());	
		putDouble("Y",  stack.getLayoutY());	
		putDouble("CenterX",  stack.getLayoutX() + width / 2);	
		putDouble("CenterY",  stack.getLayoutY() + height / 2);	
		putDouble("Width", width);	
		putDouble("Height",  height);	
	}
	public void removeSelf() {	}		// this allows groups to do more disposal

	
	public String toString()	{ return "[" + getGraphId() + "] " + getLabel() + ' ' + getShapeType();  }
	public String getInfoStr()	{ return "HTML Template for " + getGraphId() + "\n" + toString();	}
//	@Override public String toString()	{ return getGraphId() + " = " + getName();	}
	
	//--------------------------------------------------------
	public String asGPML()
	{
		ObservableMap<Object, Object> pro = getStack().getProperties();
		Object o = pro.get("TextLabel");
		String textLabel = o == null ? "" : o.toString();
		o = pro.get("Type");
		String type = o == null ? "" : o.toString();
		String header = "<DataNode TextLabel=\"%s\" GraphId=\"%s\" Type=\"%s\" >\n";
		StringBuilder buffer = new StringBuilder(String.format(header, textLabel, getGraphId(), type));

		String[] tokens = toString().split(" ");
		String shape = tokens.length > 1 ? tokens[1] : "Error";
		double w = getStack().getWidth();
		double h = getStack().getHeight();
		double cx = getStack().getLayoutX() + w / 2;
		double cy = getStack().getLayoutY() + h / 2;
		if (getShape() instanceof Rectangle)
		{
			Rectangle sh = (Rectangle) getShape();
			cx = sh.getX() + w / 2;
			cy = sh.getY() + h / 2;
			if (sh.getArcWidth() > 0)
				shape = "RoundedRectangle";
		}
		String graphics1 = String.format("  <Graphics CenterX=\"%.2f\" CenterY=\"%.2f\" Width=\"%.2f\" Height=\"%.2f\" ZOrder=\"32768\" ", cx, cy, w, h);
		String graphics2 = String.format("FontWeight=\"%s\" FontSize=\"%d\" Valign=\"%s\" ShapeType=\"%s\"", "Bold", 12, "Middle", shape);
		buffer.append(graphics1).append(graphics2).append(" />\n") ;
		buffer.append("  <Xref Database=\"\" ").append("ID=\"\"").append("/>\n") ;
		buffer.append("</DataNode>\n");
		return buffer.toString();
	}
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
	String[]  nodeAttrs = {  "TextLabel", "GraphId", "GroupRef", "Type"};
	String[]  dataNodeTypes = {  "gene", "geneproduct", "protein", "metabolite", "rna"};
	boolean isDataNode(String typ) {
		if (typ == null) return false;
		typ = typ.toLowerCase();
		for (String t : dataNodeTypes)
			if (t.equals(typ))  return true;
		return false;
	}
	private void buildNodeOpen(StringBuilder bldr) {
		String typ = get("Type");
		String shapetyp = get("ShapeType");
		String label = get("TextLabel");
		if ("GraphicalLine".equals(shapetyp)) elementType = "GraphicalLine";
		else if ("Shape".equals(typ)) elementType = "Shape";
		else if ("Label".equals(typ)) elementType = "Label";
//		else if ("Pathway".equals(typ)) elementType = "Pathway";
		else if (isDataNode(typ))		elementType =  "DataNode";
		else
			elementType ="Unknown";
		
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
			bldr.append( "<Attribute ").append(attributes).append( "/>\n");
	}
	
	String[] attributeNames = { "CenterX", "CenterY", "Width", "Height", 
			"ShapeType", "ZOrder", "Valign", "FillColor", "Color", 
			"FontSize", "FontWeight", "FontStyle" };
	void buildGraphicsTag(StringBuilder bldr)
	{
		String attributes = attributeList(attributeNames);
		if (StringUtil.hasText(attributes))
			bldr.append( "<Graphics ").append(attributes).append( " />\n");
	}

	public Point2D  getAdjustedPoint(GPMLPoint gpmlPt)
	{
		if (gpmlPt == null) return new Point2D(0,0);
		double relX = gpmlPt.getRelX();
		double relY = gpmlPt.getRelY();
		double cx = getDouble("CenterX");
		double cy = getDouble("CenterY");
		double width = getDouble("Width", 0);
		double height = getDouble("Height", 0);
		double x = cx + relX * width / 2;
		double y = cy + relY * height / 2;
		return new Point2D(x, y);
	}
	// **-------------------------------------------------------------------------------
	public boolean isResizable() 	{	return getBool("Resizable", true);	}
	public boolean isConnectable() 	{	return getBool("Connectable", true);	}
	public boolean isSelectable() 	{	return getBool("Selectable", true);	}
	public boolean isMovable() 		{	return getBool("Movable", true);	}
	
	public void setResizable(boolean b) {		 putBool("Resizable",b);	}
	public void setConnectable(boolean b) {		 putBool("Connectable",b);	}
	public void setSelectable(boolean b) {		 putBool("Selectable",b);	}
	public void setMovable(boolean b)	 {		 putBool("Movable",b);	}

	public void applyLocks(boolean mov, boolean resiz, boolean edit, boolean connect) {
		setMovable(mov);
		setResizable(resiz);
		setSelectable(edit);
		setConnectable(connect);
	}

}
