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
	public Model getModel()		{ 	return model;	}
	Model model = null;
	private int zOrder;
	public int getz() 				{	return zOrder;	}
	public void setz(int i) 		{	zOrder = i;	}
	public double getStrokeWidth() {	return attributes.getDouble("LineThickness");	}
	public String getLayer()		{ 	return getAttributes().get("Layer");	}
	private Color color = Color.BLACK;
	public Color getColor() 		{	return color;	}
	public void setColor(Color c) 	{	color = c;	}
//	public Model getModel()			{ 	return getStartNode().getModel().getModel();	}
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
//	public Edge(VNode start, VNode end) 
//    {
//		this(start, end, null, null, null);
//    }
//	
	public Edge(Model inModel, VNode start, VNode end, AttributeMap attr, List<GPMLPoint> pts, List<Anchor> anchors) 
	{
    	startNode = start;
    	endNode = end;
		edgeLine = new EdgeLine(this, pts, anchors);
		model = inModel;
		if (attr != null) attributes.addAll(attr);
		init();
    }
    public Edge(AttributeMap attr, Model inModel) 
    {
		model = inModel;
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
	public Point2D  getAdjustedPoint(VNode vNode, GPMLPoint gpmlPt)
	{
		if (vNode == null)
			return new Point2D(100,100);
		Point2D center = vNode.center();
		if (gpmlPt == null) return center;
		double relX = gpmlPt.getRelX();
		double relY = gpmlPt.getRelY();
		double width = vNode.getWidth();
		double height = vNode.getHeight();
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
		edgeLine.setStartPoint(getAdjustedPoint(startNode, getEdgeLine().firstGPMLPoint()));
		pt = new Point2D(0, 0);
		if (endNode == null) {
			String val = getAttributes().get("end");
			MNode mNode = startNode.modelNode().getModel().getResourceByKey(val);
			if (mNode != null)	
				endNode = mNode.getStack();

			Shape shape = endNode == null ? 
					startNode.modelNode().getModel().findShape(edgeLine.endGraphId()) : endNode.getFigure();
			if (shape != null) 
				pt = boundsCenter(shape);
			else 
				System.out.println("no shape");
		} else
			pt = getAdjustedPoint(endNode, getEdgeLine().lastGPMLPoint());
		edgeLine.setEndPoint(pt);

//		for (Anchor a : edgeLine.getAnchors()) {  happens in edgeLIne.connect
//			a.resetPosition(this);
//		}
		edgeLine.connect();
		if (verbose)
		{
			String startStr = startNode == null ? "NULL" : startNode.getText();
			String endStr = endNode == null ? "NULL" : endNode.getText();
			System.out.println("connect " + startStr + " to " + endStr);
		}
	}
	boolean verbose = true;
	
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
   public boolean references(String state)
   {
	   if (state.equals(attributes.get("GraphRef"))) return true;
	   for (GPMLPoint pt : edgeLine.getPoints())
		   if (state.equals(pt.getGraphRef()))
			   return true;
	   return false;
   }
	public boolean touches(String graphId)
	{
		if (graphId.equals(attributes.get("start")))	return true;
		if (graphId.equals(attributes.get("end")))		return true;
		for (Anchor a : edgeLine.getAnchors())
			if (graphId.equals(a.getId())) 
				return true;
		return false;
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
		if (endNode == null){
			String endId = getAttributes().get("end");
			if (endId != null)
			{
				MNode mnode = getModel().getResource(endId);
				if (mnode != null)
					endNode = mnode.getStack();
			}
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
   @Override public String toString()
    {
    	EdgeLine eLine = getEdgeLine();
    	Point2D endpt = eLine.lastPoint();
    	return "Edge from " + (startNode == null ? "Null" : (startNode.getText() + " @ " + StringUtil.asString(eLine.firstPoint())  ) +
    			" to "  + (endNode == null ? "Null" : endNode.getText())  + " @ " + StringUtil.asString(endpt));
    }

   //------------------------------------------------------------------------------------------
     public String toGPML()
    {
		StringBuffer b = new StringBuffer(String.format("<Interaction GraphId=\"%s\" >\n", attributes.getSafe("GraphId")));
		b.append("<Graphics ");
		b.append(attributeList(new String[]{"ConnectorType", "ZOrder","LineStyle","LineThickness"}));
		b.append (" >\n");
		b.append (getPointsStr());
		b.append (getAnchorsStr());
		b.append("</Graphics>\n");
		String db = attributes.get("Database");
		String dbid =  attributes.get("ID");
		if (db != null && dbid != null)
			b.append(String.format("<Xref Database=\"%s\" ID=\"%s\" />\n", db, dbid));
		b.append("</Interaction>\n");
		return b.toString();
    }
    
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
			builder.append(a.toGPML());
		return builder.toString();
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
	

}

