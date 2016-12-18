package diagrams.pViz.model;

import java.util.List;

import diagrams.pViz.gpml.Anchor;
import diagrams.pViz.gpml.GPMLPoint;
import diagrams.pViz.gpml.GPMLPoint.ArrowType;
import diagrams.pViz.view.VNode;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.scene.shape.Polyline;
import javafx.scene.shape.Shape;
import model.AttributeMap;
import model.bio.MIM;
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
	public double getStrokeWidth() {	return attributes.getDouble("LineThickness");	}

	private Color color = Color.BLACK;
	public Color getColor() 		{	return color;	}
	public void setColor(Color c) 	{	color = c;	}
	public Model getModel()			{ 	return getStartNode().getModel().getModel();	}
	private String graphId;
	public String getGraphId()		{ return graphId;	}
	public String getDatabase() 	{ 	return attributes.get("Database");	}
	public String getDbId() 		{ 	return attributes.get("ID");	}
	public MIM getInteractionType()			// TODO
	{ 	
		GPMLPoint point = edgeLine.lastGPMLPoint();
		if (point != null)
		{	
			ArrowType at = point.getArrowType();
			if (ArrowType.mimcatalysis == at)	return MIM.MIM_CATALYSIS;
			if (ArrowType.miminhibition == at)	return MIM.MIM_INHIBITION;
			if (ArrowType.mimbinding == at)	return MIM.MIM_BINDING;
		}
		return MIM.MIM_STIMULATION;
	}
	//----------------------------------------------------------------------
	public Edge(VNode start, VNode end) 
    {
		this(start, end, null, null, null);
    }
	
	public Edge(VNode start, VNode end, AttributeMap attr, List<GPMLPoint> pts, List<Anchor> anchors) 
	{
    	startNode = start;
    	endNode = end;
		edgeLine = new EdgeLine(this, pts, anchors);
		attributes.addAll(attr);
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
		edgeLine = new EdgeLine(this, null, null);
		attributes = attr;
		init();
      }
 
	//------------------------------------------------------------------------------------------
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
 			if ("Broken".equals(attributes.get("LineStyle")))		
 			{
 				Double[] vals = {10.0, 5.0};
 				edgeLine.setStrokeDashArray(vals);
 			}

		}
		addListeners();
			//			connect(false);		// edgeLine.setStartPoint(startNode.center());
//			connect(true);		//edgeLine.setEndPoint(endNode.center());
			
			// TODO listen to layoutY too?
//		}
    }
	private Point2D  getAdjustedPoint(VNode startNode)
	{
		Point2D center = startNode.center();
		double relX = startNode.getAttributes().getDouble("RelX");
		double relY = startNode.getAttributes().getDouble("RelY");
		double width = startNode.getWidth();
		double height = startNode.getHeight();
		double x = center.getX() + relX * width / 2;
		double y = center.getY() + relY * height / 2;
		return new Point2D(x,y);
	}
	
	public void connect() {
		if (startNode == null || edgeLine == null)
			return;
		Point2D pt = new Point2D(0, 0);
//		if (startNode == null) {
//			Shape shape = endNode.getModel().getModel().findShape(edgeLine.startGraphId());
//			pt = boundsCenter(shape);
//		} else
		edgeLine.setStartPoint(getAdjustedPoint(startNode));
		pt = new Point2D(0, 0);
		if (endNode == null) {
			String val = getAttributes().get("end");
			MNode mNode = startNode.getModel().getModel().getResourceByKey(val);
			if (mNode != null)
				endNode = mNode.getStack();

			Shape shape = startNode.getModel().getModel().findShape(edgeLine.endGraphId());
			if (shape != null) 
				pt = boundsCenter(shape);
			else 
				System.out.println("no shape");
		} else
			pt = getAdjustedPoint(endNode);
		edgeLine.setEndPoint(pt);

		for (Anchor a : edgeLine.getAnchors()) {
			a.resetPosition();
		}
		edgeLine.connect();
		System.out.println("connect " + getGraphId());
	}
	public Point2D boundsCenter(Shape s)	{
		Bounds b = s.getBoundsInParent();
		double x = (b.getMinX() + b.getMaxX()) / 2;
		double y = (b.getMinY() + b.getMaxY()) / 2;
		return new Point2D(x, y);		
	}

   public void connect(boolean atEnd)
   {
//	   if (startNode == null || endNode == null || edgeLine == null)
//		   return;
		if (atEnd)		edgeLine.setEndPoint(endNode.center());
   		else 			edgeLine.setStartPoint(startNode.center());
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
//	public static String edgeToGPML(Edge edge)			// TODO -- Color, line style, etc. are missing
//	{
//		StringBuilder buffer = new StringBuilder("<Interaction>\n");
//		buffer.append("<Graphics ConnectorType=\"Segmented\" ZOrder=\"12288\" LineThickness=\"1.0\">\n");
//		buffer.append(edge.getPointsStr());
//		buffer.append(edge.getAnchorsStr());
//		buffer.append("</Graphics>\n");
//		String db = edge.getDatabase();
//		String id = edge.getDbId();
//		buffer.append(String.format("<XRef Database=\"%s\" ID=\"%s\">\n", db, id));
//		buffer.append("</Interaction>\n");
//		return buffer.toString();
//	}


    //------------------------------------------------------------------------------------------
   public boolean isStart(Node n)	{  return n == startNode;	}
    public boolean isEnd(Node n)	{  return n == endNode;	}
    public boolean isEndpoint(Node n)	{  return isStart(n) || isEnd(n);	}
    //------------------------------------------------------------------------------------------
    public String getPointsStr()
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
		List<Anchor> anchors = edgeLine.getAnchors();
		StringBuilder builder = new StringBuilder();
		for (Anchor a : anchors)
			builder.append(a.toString());
		return builder.toString();
	}
	public Polyline getPolyline() {		return edgeLine.getPolyline();	}
	
// 
//	public void changed(ObservableValue o,Object oldVal, Object newVal){
//		System.out.println("Electric bill has changed!");  });

//}

//	ChangeListener<? super Double> startposition = { (obs, oldX, newX) -> connect(false); }  
//	ChangeListener<? super Bounds> endconnecter = { (obs, oldX, newX) -> connect(true); }  
//	ChangeListener<? super Double> endposition = { (obs, oldX, newX) -> connect(true); }  
	
	ChangeListener<Bounds> startbounds = new ChangeListener<Bounds>()
	{ 
		@Override public void changed(ObservableValue<? extends Bounds> observable, Bounds oldValue, Bounds newValue)
		{ connect(false); }  
	};

	ChangeListener<Bounds> endbounds = new ChangeListener<Bounds>()
	{ 
		@Override public void changed(ObservableValue<? extends Bounds> observable, Bounds oldValue, Bounds newValue)
		{ connect(true); }  
	};

	ChangeListener<Number> startposition = new ChangeListener<Number>()
	{ 
		@Override public void changed(ObservableValue<? extends Number> observable,Number oldValue, Number newValue)
		{ connect(false); }  
	};

	ChangeListener<Number> endposition = new ChangeListener<Number>()
	{ 
		@Override public void changed(ObservableValue<? extends Number> observable,Number oldValue, Number newValue)
		{ connect(true); }  
	};

	
	void addListeners()
	{
		if (startNode != null)
		{
			startNode.layoutBoundsProperty().addListener(startbounds);
			startNode.layoutXProperty().addListener(startposition);
		}
		if (endNode != null)
		{	
			endNode.layoutBoundsProperty().addListener(endbounds);
			endNode.layoutXProperty().addListener(endposition);
		}
	}
	public void removeListeners() 
	{
		if (startNode != null)
			{
			startNode.layoutBoundsProperty().removeListener(startbounds);
			startNode.layoutXProperty().removeListener(startposition);
			}
			if (endNode != null)
			{	
				endNode.layoutBoundsProperty().removeListener(endbounds);
				endNode.layoutXProperty().removeListener(endposition);
			}
		}
    //------------------------------------------------------------------------------------------
    public String toGPML()
    {
		StringBuffer b = new StringBuffer(String.format("<Interaction GraphId=\"%s\" >\n", getGraphId()));
		String graphics = attributes.makeElementStartString("Graphics");
		int index = 1 + graphics.lastIndexOf("\n");
		graphics = StringUtil.insertAt(graphics, index, getPointsStr());
		b.append(graphics);
		b.append("</Graphics>\n");
		b.append(String.format("<Xref Database=\"%s\" ID=\"%s\" />\n", attributes.getSafe("Database"), attributes.getSafe("ID")));
		b.append(getAnchorsStr());
// TODO more state here
		b.append("</Interaction>\n");
		return b.toString();
    }
 
    @Override public String toString()
    {
    	EdgeLine eLine = getEdgeLine();
    	Point2D endpt = eLine.lastPoint();
    	return "Edge from " + (startNode == null ? "Null" : (startNode.getText() + " @ " + StringUtil.asString(eLine.firstPoint())  ) +
    			" to "  + (endNode == null ? "Null" : endNode.getText()  + " @ " + StringUtil.asString(endpt)));
    }


}

