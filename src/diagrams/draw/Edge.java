package diagrams.draw;

import java.util.ArrayList;
import java.util.List;

import diagrams.draw.gpml.GPMLAnchor;
import diagrams.draw.gpml.GPMLPoint;
import diagrams.draw.gpml.GPMLPoint.ArrowType;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.Polyline;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;
import model.AttributeMap;
import util.LineUtil;
import util.StringUtil;

// started out from this reference, but there's much more administrative cruft now
//http://stackoverflow.com/questions/19748744/javafx-how-to-connect-two-nodes-by-a-line
//
// Edge maintains BOTH a line and a polyline to connect nodes.  The line is only
// there to make binding easier.  The polyline should be used exclusively (IMHO).
// but CubicCurve is the next big unfulfilled requirement.

public class Edge extends Group {

	//----------------------------------------------------------------------
	private Shape head, tail;
	private List<GPMLPoint> points = new ArrayList<GPMLPoint>();
	private List<GPMLAnchor> anchors = new ArrayList<GPMLAnchor>();
	private AttributeMap attributes = new AttributeMap();
	private Node startNode=null, endNode=null;
	private Polyline polyline;
	private Line line;
	private Point2D arrowPt;			// the arrowhead is drawn to stop short of the node edge
	public double getArrowX()		{ return arrowPt.getX();	}
	public double getArrowY()		{ return arrowPt.getY();	}
	public void setArrowPt(double x, double y) { arrowPt = new Point2D(x,y); }
	public void setArrowPt(Point2D pt) { arrowPt = pt; }
	private String graphId;
	public String getGraphId()	{ return graphId;	}
	private int zOrder;
	private Color color = Color.BLACK;
	public Color getColor() 	{	return color;	}
	private boolean useElbowConnection = false;
	public Node getStartNode()	{ return startNode;	}
	public Node getEndNode()	{ return endNode;	}
	public Polyline getPolyline()	{ return polyline;	}
	public Line getLine()	{ return line;	}
	public Shape getHead()		{ return head;	}
	public Shape getTail()		{ return tail;	}
	public String getDatabase() { return attributes.get("Database");	}
	public String getDbId() 	{ return attributes.get("ID");	}
	public List<GPMLPoint> getPoints() 		{ 	return points;	}
	public List<GPMLAnchor> getAnchors() 	{ 	return anchors;	}
	//----------------------------------------------------------------------
	public Edge(Node start, Node end) 
    {
		this(start, end, null, null);
    }
	
	public Edge(Node start, Node end, AttributeMap attr, List<GPMLPoint> pts) 
	    {
    	startNode = start;
    	endNode = end;
    	head = null;
    	tail = null;
    	polyline = new Polyline();
    	line = new Line();
		points = pts;
		setAttributes(attr);
		init();
    }
    	
	public Edge(Double startX, Double startY, Double endX, Double endY) 
    {
		polyline = new Polyline(startX, startY, endX, endY);
    	line = new Line(startX, startY, endX, endY);
		startNode =  endNode = null;
		head =  tail = null;
     }
    	
	public Edge(AttributeMap attrs, List<GPMLPoint> pts) 
    {
		polyline = new Polyline();
    	line = new Line();
 		startNode =  endNode = null;
		head =  tail = null;
		points = pts;
		setAttributes(attrs);
		init();
     }

    public Edge(ReadOnlyDoubleProperty startX, ReadOnlyDoubleProperty startY, ReadOnlyDoubleProperty endX, ReadOnlyDoubleProperty endY) 
    {
    	bind(startX, startY, endX, endY);
    }
    	
    public Edge(AttributeMap attr, Model model) 
    {
    	String startNodeId = attr.get("start");
    	startNode = model.getResource(startNodeId);
    	assert(startNode != null);
    	String endNodeId = attr.get("end");
    	endNode = model.getResource(endNodeId);
    	assert(endNode != null);
    	setAttributes(attr);
    	init();
    }
 
	//------------------------------------------------------------------------------------------
   boolean shorten = true;
   private void init()
    {
    	if (startNode != null && endNode != null)
       {   
    	   	NodeCenter s = new NodeCenter(startNode);
    	   	NodeCenter e = new NodeCenter(endNode);
	   		bind(s.centerXProperty(), s.centerYProperty(), e.centerXProperty(), e.centerYProperty());
       }
		if (points != null && points.size() > 1)
		{
			GPMLPoint last = points.get(points.size()-1);
			GPMLPoint mid = points.get(points.size()-2);
			setArrowPt(last.getX(), last.getY());
			if (useElbowConnection)
				elbowConnect();
			else 
				polylineConnect();

			polyline.setVisible(true);
			line.setVisible(false);

//			if (endNode == null)
//			{
//				GPMLPoint first = points.get(0);
//				LineUtil.set(line, first.getX(), first.getY(), last.getX(), last.getY());
//				line.setVisible(true);
//				polyline.setVisible(false);
//			}
			String shape = attributes.get("ArrowHead");
			double[] arrowShape = ArrowType.getArrowShape(shape);
			if (useElbowConnection)
				mid.setX(last.getX());		//hack to straighten arrow on elbows, see 207
			if (ArrowType.isShape(shape))
			{
				head = new Circle(4);
				head.setFill(Color.WHITE);
				head.setStroke(Color.BLACK);
			}
			else 
			{
				head = new Arrow(new Line(mid.getX(), mid.getY(), last.getX(), last.getY()), 1.0f, color, arrowShape);
				head.setFill(color);
			}
			head.setTranslateX(getArrowX());
			head.setTranslateY(getArrowY());
		}
		getChildren().addAll(line, polyline);
		if (head !=null) getChildren().add(head);
   }
   //----------------------------------------------------------------------

	private void polylineConnect() {
		int sz  =points.size();
		GPMLPoint last = points.get(sz - 1);
		for (int i = 0; i < sz - 1; i++) {
			GPMLPoint pt = points.get(i);
			polyline.getPoints().addAll(pt.getX() + pt.getRelX(), pt.getY() + pt.getRelY());
		}
		// shorten the last segment if endNode is defined
		if (endNode != null && shorten) {
			GPMLPoint prev = points.get(sz - 2);
			Line line = new Line(prev.getX(), prev.getY(), last.getX(), last.getY()+ last.getRelY());
			Point2D shortStopPt = LineUtil.getIntersection(line, endNode);

			polyline.getPoints().addAll(shortStopPt.getX()+ last.getRelX(), shortStopPt.getY()+ last.getRelY());
			setArrowPt(shortStopPt);
		} else
		{
			polyline.getPoints().addAll(last.getX(), last.getY());
			setArrowPt(last.getX(), last.getY());
		}
	}

//----------------------------------------------------------------------
	private void elbowConnect() {
		polyline.getPoints().clear();
		int sz  = points.size();
		for (int i = 0; i < sz - 1; i++) {
			GPMLPoint current = points.get(i);
			GPMLPoint next = points.get(i + 1);
			// mid.setX(next.getX());
			// mid.setY(current.getY());
			polyline.getPoints().addAll(current.getX(), current.getY(), next.getX(), current.getY());
		}
		GPMLPoint last = points.get(sz - 1);
		if (endNode != null && shorten) {
			GPMLPoint prev = points.get(sz - 2);
			Line line = new Line(prev.getX(), prev.getY(), last.getX(), last.getY());
			Point2D shortStopPt = LineUtil.getIntersection(line, endNode);
			Point2D correctedPt = new Point2D(last.getX(), shortStopPt.getY());			//  hack
			polyline.getPoints().addAll(correctedPt.getX(), correctedPt.getY());
			setArrowPt(correctedPt);
		} else
		{
			polyline.getPoints().addAll(last.getX(), last.getY());
		}
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
	
   private void setAttributes(AttributeMap attr) {
 		if (attr == null || attr.isEmpty()) return;
 		attributes.addAll(attr); 
 		for (String key : attributes.keySet())
 		{
 			String val = attributes.get(key);
			if ("LineThickness".equals(key))
 			{
 				double d = StringUtil.toDouble(val);
 				if (line != null) line.setStrokeWidth(d);
 				if (polyline != null) polyline.setStrokeWidth(d);
 			}
 			else if ("GraphId".equals(key))		{	graphId = val;}
 			else if ("ZOrder".equals(key))		{	zOrder = StringUtil.toInteger(val);	}
 			else if ("ConnectorType".equals(key))		
 			{
 				if ("Elbow".equals(val))
 					useElbowConnection = true;
 			}
 			else if ("Color".equals(key))		
 			{
 				color = Color.valueOf(val);
 				if (line != null) line.setStroke(color);
 				if (polyline != null) polyline.setStroke(color);
 			}
 			else if ("LineStyle".equals(key))		
 			{
 				if ("Broken".equals(val))
 				{	
 				   polyline.getStrokeDashArray().setAll(10.0, 5.0);
 				   line.getStrokeDashArray().setAll(10.0, 5.0);
 				}
 				else
 				{
  				   polyline.getStrokeDashArray().removeAll();
  				   line.getStrokeDashArray().removeAll();
 				}
 			}
 		}
 		if (points != null && points.size() > 1)
 		{
 			GPMLPoint last = points.get(points.size()-1);
 			ArrowType arrowhead = last.getArrowType();
 			if (arrowhead != null)
 				attributes.put("ArrowHead", arrowhead.toString());
 		}
 	}

    //------------------------------------------------------------------------------------------
    public boolean isStart(Node n)	{  return n == startNode;	}
    public boolean isEnd(Node n)	{  return n == endNode;	}
    public boolean isEndpoint(Node n)	{  return isStart(n) || isEnd(n);	}
    //------------------------------------------------------------------------------------------
    private void  bind(ReadOnlyDoubleProperty startX, ReadOnlyDoubleProperty startY, ReadOnlyDoubleProperty endX, ReadOnlyDoubleProperty endY)
    {
    	if (line != null)
    	{
    		line.startXProperty().bind(startX);
    		line.startYProperty().bind(startY);
    		line.endXProperty().bind(endX);
    		line.endYProperty().bind(endY);
    	}
		double lastX  = endX.get(), lastY = endY.get();
		if (shorten)		// && !useElbowConnection
		{
		    Point2D shortened = LineUtil.getIntersection(line, getEndNode());
		    lastX = shortened.getX(); 
		    lastY = shortened.getY(); 
		}
    	if (polyline != null)
    	{
    		polyline.getPoints().addAll(startX.get(), startY.get());
		    if (useElbowConnection)
			    polyline.getPoints().addAll(lastX, startY.get());
		    polyline.getPoints().addAll(lastX, lastY);
    	}
    }
    
    @Override public String toString()
    {
    	  StringBuffer b = new StringBuffer(String.format("<Interaction GraphId=\"%s\" >\n", getGraphId()));
    	 
    	  String graphics = attributes.makeElementStartString("Graphics");
    	  int index = 1 + graphics.lastIndexOf("\n");
    	  graphics =  StringUtil.insertAt(graphics, index, getPointsStr());
    	  b.append(graphics);
    	  b.append("</Graphics>\n");
    	  b.append(String.format("<Xref Database=\"%s\" ID=\"%s\" />\n", 
    			  attributes.getSafe("Database"), attributes.getSafe("ID")));
    	  b.append("</Interaction>\n");
   	  return b.toString();
    }
    //------------------------------------------------------------------------------------------
    public String getPointsStr ()
	{
		List<GPMLPoint> pts = getPoints();
		StringBuilder builder = new StringBuilder();
		if (pts != null)
			for (GPMLPoint pt : pts)
				builder.append(pt.toString());
		return builder.toString();
	}
    
    public String getAnchorsStr ()
	{
		List<GPMLAnchor> anchors = getAnchors();
		StringBuilder builder = new StringBuilder();
		for (GPMLAnchor a : anchors)
			builder.append(a.toString());
		return builder.toString();
	}
}