package diagrams.draw.model;

import java.util.List;

import diagrams.draw.gpml.GPMLAnchor;
import diagrams.draw.gpml.GPMLPoint;
import diagrams.draw.view.VNode;
import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.scene.shape.Polyline;
import model.AttributeMap;
import util.StringUtil;

/*
 *  Edge
 *  This is the entry in the edge table, not the actual Shapes on the screen
 *  see EdgeLine for the skin
 */
public class Edge  {

	//----------------------------------------------------------------------
	private AttributeMap attributes = new AttributeMap();
	public AttributeMap getAttributes() {		return attributes;	}
	
	private EdgeLine edgeLine;
	public EdgeLine getEdgeLine() 	{	return edgeLine;	}
	
	private VNode startNode=null, endNode=null;
	public VNode getStartNode()		{ 	return startNode;	}
	public VNode getEndNode()		{ 	return endNode;	}

	private int zOrder;
	public int getz() 				{	return zOrder;	}
	public void setz(int i) 		{	zOrder = i;	}

	private Color color = Color.BLACK;
	public Color getColor() 		{	return color;	}
	public void setColor(Color c) 	{	color = c;	}

	private String graphId;
	public String getGraphId()		{ return graphId;	}
	public String getDatabase() 	{ 	return attributes.get("Database");	}
	public String getDbId() 		{ 	return attributes.get("ID");	}
	//----------------------------------------------------------------------
	public Edge(VNode start, VNode end) 
    {
		this(start, end, null, null);
    }
	
	public Edge(VNode start, VNode end, AttributeMap attr, List<GPMLPoint> pts) 
	{
    	startNode = start;
    	endNode = end;
		edgeLine = new EdgeLine(this, pts);
		attributes = attr;
		init();
    }
    	
    public Edge(AttributeMap attr, Model model) 
    {
    	MNode start = model.getResource(attr.get("start"));
    	if (start != null) 
    		startNode = start.getStack();
    	MNode target = model.getResource(attr.get("end"));
    	if (target != null) 
    		endNode = target.getStack();
		edgeLine = new EdgeLine(this, null);
		attributes = attr;
		init();
      }
 
	//------------------------------------------------------------------------------------------
   boolean shorten = true;
   private void init()
    {
		if (attributes != null)
		{
			String type = attributes.get("ConnectorType");
			edgeLine.setEdgeType(EdgeType.lookup(type));
			String colStr = attributes.get("Color");
			if (colStr != null)
			{
				Color c= Color.valueOf(colStr);
				if (c != null) 		
					setColor(c);
			}

		}
		if (startNode != null && endNode != null)
		{
			startNode.layoutBoundsProperty().addListener((obs, oldX, newX) -> connect(false));
			endNode.layoutBoundsProperty().addListener((obs, oldX, newX) -> connect(true));
		}
		edgeLine.setStartPoint(startNode.center());
		edgeLine.setEndPoint(endNode.center());
    }
   private void connect(boolean atEnd)
   {
	   if (startNode != null && endNode != null && edgeLine != null)
   		if (atEnd)
   			edgeLine.setEndPoint(endNode.center());
   		else
   			edgeLine.setStartPoint(startNode.center());
	
   		edgeLine.connect();
//   		System.out.println("connect");
   }
   
//
//    private void dumpPoints(GPMLPoint a, GPMLPoint b, Point2D c) {
//    	if (a != null) System.out.println(String.format("a: (%.2f, %.2f )", a.getX(), a.getY()));
//    	if (b != null) System.out.println(String.format("b: (%.2f, %.2f )", b.getX(), b.getY()));
//    	if (c != null) System.out.println(String.format("c: (%.2f, %.2f )", c.getX(), c.getY()));
//}
 //----------------------------------------------------------------------
 //----------------------------------------------------------------------
// GPML specific
	
//   public void setAttributes(AttributeMap attr) {
// 		if (attr == null || attr.isEmpty()) return;
// 		attributes.addAll(attr); 
// 		for (String key : attributes.keySet())
// 		{
// 			String val = attributes.get(key);
//			if ("LineThickness".equals(key))
//				edgeLine.setStrokeWidth(StringUtil.toDouble(val));
// 			
// 			else if ("GraphId".equals(key))		{	graphId = val;}
// 			else if ("ZOrder".equals(key))		{	zOrder = StringUtil.toInteger(val);	}
// 			else if ("ConnectorType".equals(key))		
// 			{
// 				if ("Elbow".equals(val))
// 					edgeLine.setEdgeType(EdgeType.elbow);
// 			}
// 			else if ("Color".equals(key))		
// 			{
// 				if (val != null)
// 					setColor(Color.valueOf(val));
// 				edgeLine.setStroke(color);
// 			}
// 			else if ("LineStyle".equals(key))		
// 			{
// 				Double[] vals = {10.0, 5.0};
// 				edgeLine.setStrokeDashArray("Broken".equals(val) ? vals : null);
// 			}
// 			else if ("ArrowHead".equals(key))
//				attributes.put("ArrowHead", val);
//		}
// 	}

    //------------------------------------------------------------------------------------------
    public boolean isStart(Node n)	{  return n == startNode;	}
    public boolean isEnd(Node n)	{  return n == endNode;	}
    public boolean isEndpoint(Node n)	{  return isStart(n) || isEnd(n);	}
    //------------------------------------------------------------------------------------------
    @Override public String toString()
    {
		StringBuffer b = new StringBuffer(String.format("<Interaction GraphId=\"%s\" >\n", getGraphId()));

		String graphics = attributes.makeElementStartString("Graphics");
		int index = 1 + graphics.lastIndexOf("\n");
		graphics = StringUtil.insertAt(graphics, index, getPointsStr());
		b.append(graphics);
		b.append("</Graphics>\n");
		b.append(String.format("<Xref Database=\"%s\" ID=\"%s\" />\n", attributes.getSafe("Database"),
				attributes.getSafe("ID")));
		b.append("</Interaction>\n");
		return b.toString();
    }
    //------------------------------------------------------------------------------------------
    public String getPointsStr ()
	{
		List<GPMLPoint> pts = edgeLine.getPoints();
		StringBuilder builder = new StringBuilder();
		if (pts != null)
			for (GPMLPoint pt : pts)
				builder.append(pt.toString());
		return builder.toString();
	}
    
    public String getAnchorsStr ()
	{
		List<GPMLAnchor> anchors = edgeLine.getAnchors();
		StringBuilder builder = new StringBuilder();
		for (GPMLAnchor a : anchors)
			builder.append(a.toString());
		return builder.toString();
	}
	public Polyline getPolyline() {		return edgeLine.getPolyline();
	}
}

