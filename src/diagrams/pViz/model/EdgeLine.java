package diagrams.pViz.model;

import java.util.ArrayList;
import java.util.List;

import diagrams.pViz.gpml.Anchor;
import diagrams.pViz.gpml.GPMLPoint;
import diagrams.pViz.gpml.GPMLPoint.ArrowType;
import diagrams.pViz.view.Arrow;
import diagrams.pViz.view.VNode;
import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.CubicCurve;
import javafx.scene.shape.Line;
import javafx.scene.shape.Polyline;
import javafx.scene.shape.Shape;
import model.AttributeMap;
import util.LineUtil;

/* 
 * EdgeLine 
 * Isolate the drawing elements from the nodes/attribute definition
 * so that the edge can be switched from simple line to curve to elbow
 * without having to destroy the edge itself.
 */
public class EdgeLine extends Group {

	 //----------------------------------------------------------------------
	public EdgeLine()
	{
	   	polyline = null;
	   	line = null;
	   	head =  tail = null;
		interaction = null;			
		
	}
	public EdgeLine(EdgeType edgeType, double startX, double startY, double endX, double endY)
	{
	   this();
	   srcX = startX;
	   	srcY = startY;
	   	targX = endX;
	    targY = endY;
	    type = edgeType;
		setMouseTransparent(true);
	}

	public EdgeLine(Edge inter, List<GPMLPoint> pts, List<Anchor> anchorList) 
	{
	   	this();
		if (pts != null) 
			points.addAll(pts);
		interaction = (Interaction) inter;
		if (inter != null)
			System.out.println("Interactino id = "+ interaction.get("GraphId"));
		if (anchorList != null)
			anchors.addAll(anchorList);
		for (Anchor anchor : anchors)
			System.out.println("A: " +anchor.toString());
		setMouseTransparent(false);
	}
	 //----------------------------------------------------------------------
	private double srcX, srcY, targX, targY;
	
	public void setEndX(double x) { targX = x; }
	public void setEndY(double y) { targY = y; }
	public void setStartX(double x) { srcX = x; }
	public void setStartY(double y) { srcY = y; }
	
	public double getEndX(double x) { return targX; }
	public double getEndY(double y) { return targY; }
	public double getStartX(double x) { return srcX; }
	public double getStartY(double y) { return srcY; }
	Color stroke = Color.AQUA;
	double strokeWidth = 2;
	public void setStroke(Color c) { stroke = c; }
	public void getStrokeWidth(double w) { strokeWidth = w; }

	 //----------------------------------------------------------------------
	private Interaction interaction;		// the model corresponding to this geometry
	private Polyline polyline;
	private Line line;
	private CubicCurve curve;
	private EdgeType type = EdgeType.simple;
	private List<GPMLPoint> points = new ArrayList<GPMLPoint>();
	private List<Anchor> anchors = new ArrayList<Anchor>();
   public Anchor findAnchor(String graphId)
   {
	  if (graphId == null) return null;
	  for (Anchor a : getAnchors())
		   if (graphId.equals(a.getGraphId()))
			   return a;
	   return null;
   }

	private Point2D arrowPt = new Point2D(0,0);			// the arrowhead is drawn to stop short of the node edge
	public double getArrowX()		{ return arrowPt.getX();	}
	public double getArrowY()		{ return arrowPt.getY();	}
	public void setArrowPt(double x, double y) { arrowPt = new Point2D(x,y); }
	public void setArrowPt(Point2D pt) { arrowPt = pt; }
	public void setAnchorVis(boolean visible)
	{
		for (Anchor a: anchors) 
			a.getShape().setVisible(visible);
	}
	public Polyline getPolyline()	
	{
		if (polyline == null)
		{
			polyline  = new Polyline();
			polyline.setStyle("");		// TODO setStyleClass
			getChildren().add(polyline);
		}
		return polyline;	
	}
	public Line getLine()			
	{
		if (line == null)
		{
			line  = new Line();
			line.setStyle("");		// TODO setStyleClass
			getChildren().add(line);
			line.setOnMouseClicked(e -> { interaction.getEdgeLine().select(true); });
		}
		return line;	
	}

	public CubicCurve getCurve()			
	{
		if (curve == null)
		{
			curve  = new CubicCurve();
			curve.setStyle("");		// TODO setStyleClass
			getChildren().add(curve);
		}
		return curve;	
	}

	private Shape head, tail;
	public Shape getHead()			{ return head;	}
	public Shape getTail()			{ return tail;	}
	public EdgeType getEdgeType()	{ return type;	}
	public void setEdgeType(EdgeType t)	{ type = t;	}

	double relX = 0;		// center + this * width/2  1= right, -1= left
	double relY = 0;
	public double getRelX()		{ return relX;	}
	public double getRelY()		{ return relY;	}
	public void setRelX(double x)		{  relX = x;	}
	public void setRelY(double y)		{  relY = y;	}
	public List<GPMLPoint> getPoints() 	{ 	return points;	}
	public List<Anchor> getAnchors() 	{ 	return anchors;	}
	public void addAnchor(Anchor a) 	{ 	anchors.add(a);	}
	public void addAnchors(List<Anchor> a) 	{ 	if (a != null) anchors.addAll(a);	}
	public void removeAnchor(Anchor a) 	{ 	anchors.remove(a);	}
	public String getLayer()			{ 	return interaction.getLayer();	}
	public boolean getSelected()		{ 	return selected;	}
	public void select(boolean b)		
	{ 	
		selected = b;	
		if (line != null) line.setStroke(lineColor(selected));
	}
	
	Color lineColor(boolean selected)
	{
		return selected ? Color.RED : Color.BLACK; 
	}
	
	boolean selected;
	//----------------------------------------------------------------------
	public Point2D getPointAlongLine(double position) {
		Point2D startPt = getStartPoint();   //firstPoint();
		Point2D endPt = getEndPoint();  //lastPoint();
		System.out.println(String.format("%.2f,  %.2f  --> %.2f %.2f ", startPt.getX(), startPt.getY(), endPt.getX(), endPt.getY())); 
		// TODO Assuming simple connection!!!
		double x = startPt.getX() + ((endPt.getX() - startPt.getX()) * position);
		double y = startPt.getY() + ((endPt.getY() - startPt.getY()) * position);
		return new Point2D(x,y);
	}
	//-----------------------------------------------------
	Double[] strokeDashArray;
	public void setStrokeDashArray(Double[] vals)
	{
		strokeDashArray = vals;
	}

	public void addPoint(Point2D a)
	{
		addPoint(new GPMLPoint(a));
	}
	public void addPoint(GPMLPoint a)
	{
		points.add(a);
	}
	public void setStartPoint(Point2D startPt) {
		if (startPt == null) return;
		if (points.size() < 1)
			points.add(new GPMLPoint(startPt));
//		setPoint(startPt, points.get(0));
		if (line != null)
		{
			line.setStartX(startPt.getX());
			line.setStartY(startPt.getY());
		}
		
	}
	
	public void setEndPoint(Point2D endPt) {
		if (endPt == null) return;
	if (endPt.getX() == 0 && endPt.getY() == 0)
		return;
		if (endPt == null) return;
	if (points.size() < 2)
			points.add(new GPMLPoint(endPt));
		setPoint(endPt, points.get(points.size()-1));
		if (line != null)
		{
			line.setEndX(endPt.getX());
			line.setEndY(endPt.getY());
		}
	}

	
	private void setPoint(Point2D src, GPMLPoint targ)
	{
		targ.setX(src.getX());
		targ.setY(src.getY());
	}
	
	public void dispose()
	{
		getChildren().clear();	
	}
	
	public String toString()
	{
		if (interaction == null || interaction.getStartNode() == null) return "NO START";
		VNode start = interaction.getStartNode().getStack();
		String startID = start == null ? startGraphId() : start.getId();
		if (interaction == null || interaction.getEndNode() == null) return "NO TARGET";
		VNode end = interaction.getEndNode().getStack();
		String endID = end == null ? endGraphId() : end.getId();
//		Line line = edgeline.getLine();
//		double startX = getStartX();
//		double endX = getEndX();
//		double startY = getStartY();
//		double endY = getEndY();
//		Map<String, Shape> shapes = interaction.getModel().getShapes();
//		Shape shape = interaction.getModel().getShapes().get(startGraphId());
//		Bounds b = shape.getLayoutBounds();
		Bounds b = start.getBoundsInParent();
		double startCenterX = 0;
		double startCenterY = 0;
		if (start != null)
		{
			Point2D p = start.boundsCenter();
			startCenterX = p.getX();
			startCenterY = p.getY();
		}
		else
		{
			startCenterX = b.getMinX() + (b.getWidth() / 2);
			startCenterY = b.getMinY() + (b.getHeight() / 2);
		}
		double endCenterX = 0;
		double endCenterY = 0;
		if (end != null)
		{
			Point2D p = end.boundsCenter();
			endCenterX = p.getX();
			endCenterY = p.getY();
		}
		else
		{
			endCenterX = b.getMinX() + (b.getWidth() / 2);
			endCenterY = b.getMinY() + (b.getHeight() / 2);
		}
	
		String s=  String.format(" ?? %s \t(%4.1f, %4.1f) --> %s (%4.1f, %4.1f) %d ", 
				startID, startCenterX, startCenterY, 
				endID, endCenterX, endCenterY, getPoints().size());
				
		return s;
	}
  //----------------------------------------------------------------------
	public void connect()
	{
		for (Anchor a : anchors)
			a.resetPosition(interaction);
		switch (type)
		{
			case polyline:	polylineConnect(); 		break;
			case elbow:		elbowConnect(); 		break;
			case curved:	
				if (curve != null) 	getChildren().remove(curve);
				if (line != null) line.setVisible(false);
					curveConnect(); 	
				if (curve != null) 	getChildren().add(curve);
				break;
			default: 		 linearConnect();
		}
		
		if (head != null) 	getChildren().remove(head);
		head = makeArrowhead(interaction);
	 	if (head != null) 	getChildren().add(head);
//		addTail();			not implemented here
	}
	
	//----------------------------------------------------------------------
	public String startGraphId()
	{
		if (points.size() == 0) return "";
		GPMLPoint pt = points.get(0);
		return pt.getGraphRef();
	}
	public String endGraphId()
	{
		if (points.size() == 0) return "";
		GPMLPoint pt = points.get(points.size()-1);
		return pt.getGraphRef();
	}
	public double getStartX()	{  return  (firstPoint() != null) ? firstPoint().getX() : 0;	}
	public double getStartY()	{  return  (firstPoint() != null) ? firstPoint().getY() : 0;	}
	public double getEndX()		{  return  (lastPoint() != null) ? lastPoint().getX() : 0;	}
	public double getEndY()		{  return  (lastPoint() != null) ? lastPoint().getY() : 0;	}

	public Point2D firstPoint()
	{ 
		if (points.size() == 0) return null;
		return points.get(0).getPoint(); 
		}
	public GPMLPoint firstGPMLPoint()
	{ 
		if (points.size() == 0) return null;
		return points.get(0); 
		}
	public Point2D forelastPoint()
	{ 
		if (points.size() < 2) return firstPoint();
		return points.get(points.size()-2).getPoint(); 
	}
	public Point2D lastPoint()
	{ 
		if (points.size() == 0) return null;
		return points.get(points.size()-1).getPoint(); 
	}
	public GPMLPoint lastGPMLPoint()
	{ 
		if (points.size() == 0) return null;
		return points.get(points.size()-1); 
	}
	public void setLastPoint(Point2D pt)
	{ 
		if (points.size() == 0) return;
		points.set(points.size()-1, new GPMLPoint(pt)); 
	}
	double length()		// TODO assumes straight edges
	{ 
		int sz = points.size();
		double len = 0;
		if (sz < 2) return 0;
		for (int i=0; i<sz-1; i++)
			len += LineUtil.distance(points.get(i).getPoint(), points.get(i+i).getPoint());
		return len;
	}
	public double getClosestPosition(double evX, double evY) {// TODO assumes straight edges
		return LineUtil.toLineCoordinates(getStartPoint(), getEndPoint(), new Point2D(evX, evY));
	}
	//----------------------------------------------------------------------
boolean BADPOINT(Point2D pt)
{
	return pt == null || Double.isNaN(pt.getX()) || Double.isNaN(pt.getY());
}
	private void linearConnect() {
		Point2D lastPt = lastPoint();
//		if (lastPt != null) return;
		
		VNode startNode = interaction.getStartNode() == null ? null : interaction.getStartNode().getStack();
		VNode endNode = interaction.getEndNode() == null ? null : interaction.getEndNode().getStack();
		if (endNode == null) return;
		int shorten = SHORTEN ? 10 : 0;
		if (endNode != null)		// TODO -- and arrowhead??
		{
//			if (endNode.isAnchor()) 	shorten = 0;
//			if (edge.getInteractionType() == MIM.MIM_CATALYSIS) 
//				shorten = false;	
		}
		
		if (shorten > 0) {
			Point2D prev = forelastPoint();
			if (prev != null)
			{
				if (BADPOINT(prev))			return;
				if (BADPOINT(lastPt)) 		return;
				Line refline = new Line(prev.getX(), prev.getY(), lastPt.getX(), lastPt.getY());
				lastPt= LineUtil.getIntersection(refline, endNode, shorten);
			}
		} 
		Point2D start = new Point2D(500,500);
		if (startNode == null)
		{
			String startId = interaction.get("start");
			if (startId != null)
			{
				DataNode modeNode = interaction.getModel().getDataNode(startId);
				if (modeNode != null) 
					startNode =	modeNode.getStack();
			}
		}
		else start = interaction.getAdjustedPoint(startNode, firstGPMLPoint());
		setStartPoint(start);
		Point2D end = interaction.getAdjustedPoint(endNode, lastGPMLPoint());
		setEndPoint(end);
		setArrowPt(end);
		LineUtil.set(getLine(), start, end);
		getLine().setStroke(interaction.getColor());
		double width = interaction.getStrokeWidth();
		getLine().setStrokeWidth(width);
		if (strokeDashArray != null)
		{
			getLine().getStrokeDashArray().setAll(strokeDashArray);
			if (getCurve() != null)
				getCurve().getStrokeDashArray().setAll(strokeDashArray);
			if (getPolyline() != null)
				getPolyline().getStrokeDashArray().setAll(strokeDashArray);
			
		}
	}
	
	  //----------------------------------------------------------------------

	private void curveConnect() {
		getCurve();
		int nPoints = getPoints().size();
		Point2D line1End = new Point2D(0,0);
		for (int i=0; i < nPoints-1; i++)
		{
			Point2D a = points.get(i).getPoint();
			Point2D b = points.get(i+1).getPoint();
			Line line1 = new Line(a.getX(), a.getY(), b.getX(), b.getY());
			Line line2 = new Line(a.getX(), a.getY(), b.getX(), b.getY());
	//		Point2D lastPt = lastPoint();
	        Point2D line1Start = new Point2D(line1.getStartX(), line1.getStartY());
	         line1End = new Point2D(line1.getEndX(), line1.getEndY());
	        Point2D line2Start = new Point2D(line2.getStartX(), line2.getStartY());
	        Point2D line2End = new Point2D(line2.getEndX(), line2.getEndY());
	
	        double line1Length = line1End.subtract(line1Start).magnitude();
	        double line2Length = line2End.subtract(line2Start).magnitude();
	
	        // average length:
	        double averLength = (line1Length + line2Length) / 2 ;
	
	        // extend line1 in direction of line1 for aveLength:
	        Point2D control1 = line1End.add(line1End.subtract(line1Start).normalize().multiply(2));
	        
	        // extend line2 in (reverse) direction of line2 for aveLength:
	        Point2D control2 = line2Start.add(line2Start.subtract(line2End).normalize().multiply(averLength));
		
	        control1 = new Point2D(line1End.getX() + 10, line1End.getY() + 40);
	        control2 = new Point2D(line2End.getX() - 10, line2End.getY() - 40);
	        curve = new CubicCurve(
	                line1End.getX(), line1End.getY(), 
	                control1.getX(), control1.getY(), 
	                control2.getX(), control2.getY(), 
	                line2Start.getX(), line2Start.getY());
	
	        curve.setStroke(Color.BLACK);
	        curve.setFill(null);
	        curve.setStrokeWidth(interaction.getStrokeWidth());
			if (strokeDashArray != null)
				curve.getStrokeDashArray().setAll(strokeDashArray);
		}
		setArrowPt(line1End.getX(), line1End.getY());

	}
	boolean SHORTEN = true;
	  //----------------------------------------------------------------------
	private void polylineConnect() {
		int sz = points.size();
		GPMLPoint last = points.get(sz - 1);
		for (int i = 0; i < sz - 1; i++) {
			GPMLPoint pt = points.get(i);
			polyline.getPoints().addAll(pt.getX() + pt.getRelX(), pt.getY() + pt.getRelY());
		}
		// shorten the last segment if endNode is defined
		Node endNode = interaction.getEndNode().getStack();
		boolean shorten = SHORTEN && endNode != null;			// TODO -- and arrowhead??
		if (shorten) {
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
		polyline.setStroke(interaction.getColor());
		polyline.setStrokeWidth(interaction.getStrokeWidth());
		if (strokeDashArray != null)
			polyline.getStrokeDashArray().setAll(strokeDashArray);
	}

//----------------------------------------------------------------------
	private void elbowConnect() {
		Polyline poly = getPolyline();
		poly.getPoints().clear();
		if (line != null) line.setVisible(false);
		int sz  = points.size();
		for (int i = 0; i < sz - 1; i++) {
			GPMLPoint current = points.get(i);
			GPMLPoint next = points.get(i + 1);
			// mid.setX(next.getX());
			// mid.setY(current.getY());
			poly.getPoints().addAll(current.getX(), current.getY(), next.getX(), current.getY());
		}
		Point2D last = lastPoint();
		Node endNode = interaction.getEndNode() == null ? null : interaction.getEndNode().getStack();
		boolean shorten = SHORTEN && endNode != null;			// TODO -- and arrowhead != null??
		if (shorten) {
			GPMLPoint prev = points.get(sz - 2);
			Line line = new Line(last.getX(), prev.getY(), last.getX(), last.getY());
			Point2D shortStopPt = LineUtil.getIntersection(line, endNode);
			Point2D correctedPt = new Point2D(last.getX(), shortStopPt.getY());			//  hack
			poly.getPoints().addAll(correctedPt.getX(), correctedPt.getY());
			setArrowPt(correctedPt);
		} else
		{
			poly.getPoints().addAll(last.getX(), last.getY());
		}
		poly.setStroke(interaction.getColor());
	}

   //----------------------------------------------------------------------
	public Shape makeArrowhead(AttributeMap attributes)
	{	
		if (points != null && points.size() > 1)
		{
			GPMLPoint last = points.get(points.size()-1);
			Color strokeColor = interaction.getColor();
			ArrowType arrowhead = last.getArrowType();
			if (arrowhead == ArrowType.none) return null;
			Point2D prev = forelastPoint();
			if (type == EdgeType.elbow)
				prev = new Point2D(last.getX(), prev.getY());
//			Point2D lastpoint2D = last.getPoint();
//			System.out.println("makeArrowhead: " + lastpoint2D);
			return makeArrowHead(arrowhead.toString(), prev, last.getPoint(), strokeColor);
		}
		return null;
	}

   private Shape makeArrowHead(String shape, Point2D mid, Point2D last, Color color)
   {
		Shape arrowhead;
		double[] arrowShape = ArrowType.getArrowShape(shape);
		if (ArrowType.isShape(shape))
		{
			arrowhead = new Circle(4);
			arrowhead.setFill(Color.WHITE);
			arrowhead.setStroke(Color.BLACK);
		}
		else 
		{
			Line line = new Line(mid.getX(), mid.getY(), last.getX(), last.getY());
			arrowhead = new Arrow(line, 1.0f, color, arrowShape);
			arrowhead.setFill(color);
		}
		arrowhead.setTranslateX(getArrowX());
		arrowhead.setTranslateY(getArrowY());
		return arrowhead;
   }
   Point2D getStartPoint()	{ return new Point2D(getStartX(), getStartY());  }
   Point2D getEndPoint()	{ return new Point2D(getEndX(), getEndY());  }
   
public void setStrokeWidth(int i) {
	// TODO Auto-generated method stub
	
}


}
