package diagrams.draw;

import java.util.ArrayList;
import java.util.List;

import diagrams.draw.gpml.GPMLAnchor;
import diagrams.draw.gpml.GPMLPoint;
import diagrams.draw.gpml.GPMLPoint.ArrowType;
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

	private Edge edge;		// the model corresponding to this geometry
	private Polyline polyline;
	private Line line;
	private CubicCurve curve;
	private EdgeType type = EdgeType.simple;
	private List<GPMLPoint> points = new ArrayList<GPMLPoint>();
	private List<GPMLAnchor> anchors = new ArrayList<GPMLAnchor>();
	
	private Point2D arrowPt = new Point2D(0,0);			// the arrowhead is drawn to stop short of the node edge
	public double getArrowX()		{ return arrowPt.getX();	}
	public double getArrowY()		{ return arrowPt.getY();	}
	public void setArrowPt(double x, double y) { arrowPt = new Point2D(x,y); }
	public void setArrowPt(Point2D pt) { arrowPt = pt; }

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
	public List<GPMLAnchor> getAnchors() 	{ 	return anchors;	}

	 //----------------------------------------------------------------------
	public EdgeLine(Edge container, List<GPMLPoint> pts) 
	{
    	polyline = null;
    	line = null;
    	head =  tail = null;
		points = pts;
		edge = container;
		setMouseTransparent(true);

	}
	//----------------------------------------------------------------------
//	public void setStrokeWidth(double b)
//	{
//		if (line != null) line.setStrokeWidth(b);
//		if (polyline != null) polyline.setStrokeWidth(b);
//	}
//	public void setStroke(Color col)
//	{
////		if (line != null) line.setStroke(col);
////		if (polyline != null) polyline.setStroke(col);
//	}
	public void setStrokeDashArray(Double[] vals)
	{
		if (vals == null) {
			if (line != null)		line.getStrokeDashArray().removeAll();
			if (polyline != null)	polyline.getStrokeDashArray().removeAll();
		} else {
			if (line != null)		line.getStrokeDashArray().setAll(vals);
			if (polyline != null)	polyline.getStrokeDashArray().setAll(vals);
		}
	}
	public void setStartPoint(Point2D center) {
		if (points.size() < 1) return;
		points.get(0).setX(center.getX());
		points.get(0).setY(center.getY());
	}
	
	public void setEndPoint(Point2D center) {
		int size = points.size();
		if (size < 2) return;
		points.get(size-1).setX(center.getX());
		points.get(size-1).setY(center.getY());
		
	}
  //----------------------------------------------------------------------
	public void connect()
	{
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
	Point2D firstPoint()
	{ 
		if (points == null || points.size() == 0) return null;
		return points.get(0).getPoint(); 
		}
	Point2D forelastPoint()
	{ 
		if (points == null || points.size() < 2) return null;
		return points.get(points.size()-2).getPoint(); 
	}
	Point2D lastPoint()
	{ 
		if (points == null || points.size() == 0) return null;
		return points.get(points.size()-1).getPoint(); 
	}
	//----------------------------------------------------------------------

	private void linearConnect() {
		Point2D lastPt = lastPoint();
		Node endNode = edge.getEndNode();
		boolean shorten = endNode != null;			// TODO -- and arrowhead??
		if (endNode != null && shorten) {
			Point2D prev = forelastPoint();
			Line line = new Line(prev.getX(), prev.getY(), lastPt.getX(), lastPt.getY());
			lastPt= LineUtil.getIntersection(line, endNode);
		} 
		setArrowPt(lastPt);
		LineUtil.set(getLine(), firstPoint(), lastPt);
		getLine().setStroke(edge.getColor());
	}
	
	  //----------------------------------------------------------------------

	private void curveConnect() {
		getCurve();
		Line line1 = new Line(firstPoint().getX(), firstPoint().getY(), lastPoint().getX(), lastPoint().getY());
		Line line2 = new Line(firstPoint().getX(), firstPoint().getY(), lastPoint().getX(), lastPoint().getY());
//		Point2D lastPt = lastPoint();
        Point2D line1Start = new Point2D(line1.getStartX(), line1.getStartY());
        Point2D line1End = new Point2D(line1.getEndX(), line1.getEndY());
        Point2D line2Start = new Point2D(line2.getStartX(), line2.getStartY());
        Point2D line2End = new Point2D(line2.getEndX(), line2.getEndY());

        double line1Length = line1End.subtract(line1Start).magnitude();
        double line2Length = line2End.subtract(line2Start).magnitude();

        // average length:
        double aveLength = (line1Length + line2Length) / 2 ;

        // extend line1 in direction of line1 for aveLength:
        Point2D control1 = line1End.add(line1End.subtract(line1Start).normalize().multiply(10));
        
        // extend line2 in (reverse) direction of line2 for aveLength:
        Point2D control2 = line2Start.add(line2Start.subtract(line2End).normalize().multiply(100));
	
        control1 = new Point2D(line1End.getX() + 10, line1End.getY() + 40);
        control2 = new Point2D(line2End.getX() - 10, line2End.getY() - 40);
        curve = new CubicCurve(
                line1End.getX(), line1End.getY(), 
                control1.getX(), control1.getY(), 
//                10, 10,
//                100, 100,
                control2.getX(), control2.getY(), 
                line2Start.getX(), line2Start.getY());

        curve.setStroke(Color.BLACK);
        curve.setFill(null);

	}
	
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
		boolean shorten = endNode != null;			// TODO -- and arrowhead??
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
		polyline.setStroke(edge.getColor());
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
		boolean shorten = endNode != null;			// TODO -- and arrowhead??
		if (endNode != null && shorten) {
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
			Point2D prev = forelastPoint();
			if (type == EdgeType.elbow)
				prev = new Point2D(last.getX(), prev.getY());
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

}
