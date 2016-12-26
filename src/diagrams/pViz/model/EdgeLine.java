package diagrams.pViz.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
import model.bio.MIM;
import util.LineUtil;

/* 
 * EdgeLine 
 * Isolate the drawing elements from the nodes/attribute definition
 * so that the edge can be switched from simple line to curve to elbow
 * without having to destroy the edge itself.
 */
public class EdgeLine extends Group {

	private Edge edge;		// the model corresponding to this geometry
	private Polyline polyline;
	private Line line;
	private CubicCurve curve;
	private EdgeType type = EdgeType.simple;
	private List<GPMLPoint> points = new ArrayList<GPMLPoint>();
	private List<Anchor> anchors = new ArrayList<Anchor>();
	
	private Point2D arrowPt = new Point2D(0,0);			// the arrowhead is drawn to stop short of the node edge
	public double getArrowX()		{ return arrowPt.getX();	}
	public double getArrowY()		{ return arrowPt.getY();	}
	public void setArrowPt(double x, double y) { arrowPt = new Point2D(x,y); }
	public void setArrowPt(Point2D pt) { arrowPt = pt; }
	public void setAnchorVis(boolean visible)
	{
		for (Anchor a: anchors) 
			a.getStack().setVisible(visible);
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

	public List<GPMLPoint> getPoints() 		{ 	return points;	}
	public List<Anchor> getAnchors() 	{ 	return anchors;	}
	public void addAnchor(Anchor a) 	{ 	anchors.add(a);	}
	public void removeAnchor(Anchor a) 	{ 	anchors.remove(a);	}

	 //----------------------------------------------------------------------
	public EdgeLine(Edge container, List<GPMLPoint> pts, List<Anchor> anchorList) 
	{
    	polyline = null;
    	line = null;
    	head =  tail = null;
		if (pts != null) 
			points.addAll(pts);
		edge = container;
			
		if (anchorList != null)
			anchors.addAll(anchorList);
		for (Anchor anchor : anchors)
			anchor.setEdge(edge);
		setMouseTransparent(true);
	}
	//----------------------------------------------------------------------
	public Point2D getPointAlongLine(double position) {
		Point2D startPt = firstPoint();
		Point2D endPt = lastPoint();
		
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
	public void setStartPoint(Point2D startPt) {
		if (points.size() < 1)
			points.add(new GPMLPoint(startPt));
		setPoint(startPt, points.get(0));
	}
	
	public void setEndPoint(Point2D endPt) {
		if (points.size() < 2)
			points.add(new GPMLPoint(endPt));
		setPoint(endPt, points.get(points.size()-1));
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
		VNode start = edge.getStartNode();
		String startID = start == null ? startGraphId() : start.getId();
		VNode end = edge.getEndNode();
		String endID = end == null ? endGraphId() : end.getId();
//		Line line = edgeline.getLine();
//		double startX = getStartX();
//		double endX = getEndX();
//		double startY = getStartY();
//		double endY = getEndY();
		Map<String, Shape> shapes = edge.getModel().getShapes();
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
			Shape shape = edge.getModel().getShapes().get(startGraphId());
			if (shape != null)
			{
				Bounds b = shape.getLayoutBounds();
				startCenterX = b.getMinX() + (b.getWidth() / 2);
				startCenterY = b.getMinY() + (b.getHeight() / 2);
			}
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
			Shape shape = shapes.get(endGraphId());
			if (shape != null)
			{
				Bounds b = shape.getLayoutBounds();
				endCenterX = b.getMinX() + (b.getWidth() / 2);
				endCenterY = b.getMinY() + (b.getHeight() / 2);
			}
		}
	
		String s=  String.format("%s \t(%4.1f, %4.1f) %s (%4.1f, %4.1f)  ", 
				startID, startCenterX, startCenterY, 
				endID, endCenterX, endCenterY);
				
		return s;
	}
  //----------------------------------------------------------------------
	public void connect()
	{
		for (Anchor a : anchors)
			a.resetPosition(edge);
		switch (type)
		{
			case polyline:	polylineConnect(); 		break;
			case elbow:		elbowConnect(); 		break;
			case curved:	
				if (curve != null) 	getChildren().remove(curve);
				curveConnect(); 	
				if (curve != null) 	getChildren().add(curve);
				break;
			default: 		linearConnect();
		}
		
		if (head != null) 	getChildren().remove(head);
		head = makeArrowhead(edge.getAttributes());
		if (head != null) 	getChildren().add(head);
//		addTail();			not implemented here
	}
	
	//----------------------------------------------------------------------
	public String startGraphId()
	{
		if (points == null || points.size() == 0) return "";
		GPMLPoint pt = points.get(0);
		return pt.getGraphRef();
	}
	public String endGraphId()
	{
		if (points == null || points.size() == 0) return "";
		GPMLPoint pt = points.get(points.size()-1);
		return pt.getGraphRef();
	}
	public double getStartX()	{  return  (firstPoint() != null) ? firstPoint().getX() : 0;	}
	public double getStartY()	{  return  (firstPoint() != null) ? firstPoint().getY() : 0;	}
	public double getEndX()		{  return  (lastPoint() != null) ? lastPoint().getX() : 0;	}
	public double getEndY()		{  return  (lastPoint() != null) ? lastPoint().getY() : 0;	}

	public Point2D firstPoint()
	{ 
		if (points == null || points.size() == 0) return null;
		return points.get(0).getPoint(); 
		}
	public GPMLPoint firstGPMLPoint()
	{ 
		if (points == null || points.size() == 0) return null;
		return points.get(0); 
		}
	public Point2D forelastPoint()
	{ 
		if (points == null || points.size() < 2) return firstPoint();
		return points.get(points.size()-2).getPoint(); 
	}
	public Point2D lastPoint()
	{ 
		if (points == null || points.size() == 0) return null;
		return points.get(points.size()-1).getPoint(); 
	}
	public GPMLPoint lastGPMLPoint()
	{ 
		if (points == null || points.size() == 0) return null;
		return points.get(points.size()-1); 
	}
	public void setLastPoint(Point2D pt)
	{ 
		if (points == null || points.size() == 0) return;
		points.set(points.size()-1, new GPMLPoint(pt)); 
	}
	double length()		// TODO assumes straight edges
	{ 
		int sz = points.size();
		double len = 0;
		if (points == null || sz < 2) return 0;
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
		VNode startNode = edge.getStartNode();
		VNode endNode = edge.getEndNode();
		if (endNode == null) return;
		int shorten = SHORTEN ? 10 : 0;
		if (endNode != null)		// TODO -- and arrowhead??
		{
			if (endNode.isAnchor()) 	shorten = 0;
//			if (edge.getInteractionType() == MIM.MIM_CATALYSIS) 
//				shorten = false;	
		}
		
		if (shorten > 0) {
			Point2D prev = forelastPoint();
			if (prev != null)
			{
				if (BADPOINT(prev)) 
					return;
				if (BADPOINT(lastPt)) 
					return;
				Line refline = new Line(prev.getX(), prev.getY(), lastPt.getX(), lastPt.getY());
				lastPt= LineUtil.getIntersection(refline, endNode, shorten);
			}
		} 
		Point2D start = new Point2D(500,500);
		if (startNode == null)
		{
			String startId = edge.getAttributes().get("start");
			if (startId != null)
			{
				MNode modeNode = edge.getModel().getResource(startId);
				if (modeNode != null) 
					startNode =	modeNode.getStack();
			}
		}
		else start = edge.getAdjustedPoint(startNode, firstGPMLPoint());
		setStartPoint(start);
		Point2D end = edge.getAdjustedPoint(endNode, lastGPMLPoint());
		setEndPoint(end);
		setArrowPt(end);
		LineUtil.set(getLine(), start, end);
		getLine().setStroke(edge.getColor());
		getLine().setStrokeWidth(edge.getStrokeWidth());
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
	        curve.setStrokeWidth(edge.getStrokeWidth());
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
		Node endNode = edge.getEndNode();
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
		polyline.setStroke(edge.getColor());
		polyline.setStrokeWidth(edge.getStrokeWidth());
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
		Node endNode = edge.getEndNode();
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
		poly.setStroke(edge.getColor());
	}

   //----------------------------------------------------------------------
	public Shape makeArrowhead(AttributeMap attributes)
	{	
		if (points != null && points.size() > 1)
		{
			GPMLPoint last = points.get(points.size()-1);
			Color strokeColor = edge.getColor();
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


}
