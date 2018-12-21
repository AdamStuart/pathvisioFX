package diagrams.pViz.model.edges;

import java.util.List;

import diagrams.pViz.gpml.Anchor;
import diagrams.pViz.gpml.GPMLPoint;
import diagrams.pViz.gpml.GPMLPoint.ArrowType;
import diagrams.pViz.model.Model;
import diagrams.pViz.model.nodes.DataNode;
import diagrams.pViz.view.VNode;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.scene.paint.Color;
import javafx.scene.shape.Shape;
import model.AttributeMap;
import model.bio.MIM;
import model.bio.XRefable;
import util.StringUtil;

/*
 *  Edge
 *  This is the entry in the edge table, not the actual Shapes on the screen
 *  see EdgeLine for the skin
 */
@SuppressWarnings("serial")
abstract public class Edge extends XRefable {


	//----------------------------------------------------------------------
//	public Edge(VNode start, VNode end) 
//    {
//		this(start, end, null, null, null);
//    }
//	
	public Edge(Model inModel) 
    {
		model = inModel;
	}
	
	// from parser
    public Edge(AttributeMap attr, Model inModel, List<GPMLPoint> pts, List<Anchor> anchors) 
    {
		this(inModel);
		addAll(attr);
		init(pts, anchors);
		DataNode start = model.getDataNode(get("sourceid"));
    	if (start != null) 
    	{
    		startNode = start;
        	setSource(start.getLabel()); 
        	setSourceid(start.getGraphId());
    	}
    	DataNode target = model.getDataNode(get("targetid"));
    	if (target == null)
    	{
    		String ref = get("targetid");
    		if (startNode != null)
    		{
    			Anchor anch = model.findAnchorByRef(ref);
        		if (anch != null)
        			setTargetid(anch.getGraphId());
    		}
  		}
    	if (target != null) 
    	{
    		endNode = target;
    		setTarget(target.getLabel()); 
    		setTargetid(target.getGraphId());
    	}
    	
      }
 
//    public Edge(GPMLPoint startPt, GPMLPoint endPt, double thickness, Model inModel) 
//    {
//		model = inModel;
//    	endNode = startNode = null;
//		init(null, null);
//		edgeLine.addPoint(startPt);
//		edgeLine.addPoint(endPt);
////		model.addEdge(this);
//      }
 
	public Edge(Model inModel, VNode start, VNode end, AttributeMap attr) 		//, List<GPMLPoint> pts, List<Anchor> anchors
	{
		this(inModel);
		if (attr != null)    	addAll(attr);
    	startNode = start.modelNode();  
    	put("source", start.modelNode().getLabel());
    	put("sourceid", start.modelNode().getGraphId());
    	setSource(start.modelNode().getLabel()); 
    	setSourceid(start.modelNode().getGraphId());
    	
    	endNode = end.modelNode();	
    	put("target", end.modelNode().getLabel());
    	put("targetid", end.modelNode().getGraphId());
    	setTarget(endNode.getLabel()); 
    	setTargetid(endNode.getGraphId());
		if (StringUtil.isEmpty(getGraphId()))
		{	
			String id = model.gensym("E");
			setGraphId(id);
			put("GraphId", id);
		}
		init(null, null);		//pts, anchors
    }
	
//	abstract protected void init( List<GPMLPoint> pts, List<Anchor> anchors);

	//------------------------------------------------------------------------------------------
	public void init( List<GPMLPoint> pts, List<Anchor> anchors)
    {
		
		edgeLine = new EdgeLine(this, pts, anchors);
		setInteractionProperty(get("ArrowHead"));
	   EdgeType edgeType = EdgeType.simple;
		String type = get("ConnectorType");
		if (type != null)  
			edgeType = EdgeType.lookup(type);
		edgeLine.setEdgeType(edgeType);
//		if (anchors != null && anchors.size() > 0)
//			System.out.println("ANCHORS");
//		edgeLine.addAnchors(anchors);
		if (anchors != null)
			for (Anchor a : anchors)
				a.setInteraction(this);
		String colStr = get("Color");
		if (colStr != null)
		{
			Color c= Color.valueOf(colStr);
			if (c != null) 		
				setColor(c);
		}
		if ("Broken".equals(get("LineStyle")))		
		{
			Double[] vals = {10.0, 5.0};
			edgeLine.setStrokeDashArray(vals);
		}
		addListeners();
		
			//			connect(false);		// edgeLine.setStartPoint(startNode.center());
//			connect(true);		//edgeLine.setEndPoint(endNode.center());
			
			// TODO listen to layoutY too?
//		}
    }

	public void connect() {
		if (edgeLine == null)
			return;  // error?
		GPMLPoint gpt = getEdgeLine().firstGPMLPoint();
		if (gpt == null) return;
		Point2D startPt = gpt.getPoint();
		if (startNode != null)
			startPt = startNode.getAdjustedPoint(gpt);
		edgeLine.setStartPoint(startPt);
			
		GPMLPoint lastpt = getEdgeLine().lastGPMLPoint();
		Point2D endpt = lastpt.getPoint();
		if (endNode != null)
		 endpt = endNode.getAdjustedPoint(lastpt);
		edgeLine.setEndPoint(endpt);
//		ArrowType typ = lastpt.getArrowType();
//		edgeLine.setArrowType(lastpt.getArrowType());
		edgeLine.connect();

	}
//		
//		
//		if (endNode == null) {
//			String val = get("targetid");
//			DataNode mNode = startNode.getModel().getResourceByKey(val);
//			if (mNode != null)	
//				endNode = mNode;
//		if (endNode == null)
//		{
//			Anchor anch = startNode.getModel().findAnchorById(val);
//			System.out.println("anch " + anch);
//		}
//			Shape shape = getEdgeLine().getHead();  //getShape();  //endNode == null ? null : endNode.getStack().getFigure();
////					startNode.getModel().findShape(edgeLine.endGraphId()) : 
//			if (shape != null) 
//				pt = boundsCenter(shape);
//			else 
//				System.out.println("no shape");
//		} else
//		if (pt.getX() < 1)
//		{
////			edgeLine.setVisible(false);
//			System.out.println("zerro");
//		}
////		System.out.println(String.format("End: [ %.2f, %.2f]",pt.getX(), pt.getY()));
////		Shape head = edgeLine.makeArrowhead();
//
//
//		if (verbose)
//		{
//			String startStr = startNode == null ? "NULL" : startNode.getStack().getText();
//			String endStr = endNode == null ? "NULL" : endNode.getStack().getText();
//			System.out.println("connect " + startStr + " to " + endStr);
//		}
//	}
	boolean verbose = false;
	
	public Point2D boundsCenter(Shape s)	{
		Bounds b = s.getBoundsInParent();
		double x = (b.getMinX() + b.getMaxX()) / 2;
		double y = (b.getMinY() + b.getMaxY()) / 2;
		return new Point2D(x, y);		
	}

//   public void connect(boolean atEnd)
//   {
//	   if (startNode == null || endNode == null || edgeLine == null)
//		   return;
//		if (atEnd)		edgeLine.setEndPoint(endNode.getStack().center());
//   		else 			edgeLine.setStartPoint(startNode.getStack().center());
//   		edgeLine.connect();
//   		System.out.println("connect");
//   }
   public boolean references(String state)
   {
	   if (state.equals(get("GraphRef"))) return true;
	   for (GPMLPoint pt : edgeLine.getPoints())
		   if (state.equals(pt.getGraphRef()))
			   return true;
	   return false;
   }
	public boolean touches(String graphId)
	{
		if (graphId == null) return false;
		if (graphId.equals(get("sourceid")))	return true;
		if (graphId.equals(get("targetid")))		return true;
		for (Anchor a : edgeLine.getAnchors())
			if (graphId.equals(a.getGraphId())) 
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


    //------------------------------------------------------------------------------------------
   public boolean isStart(DataNode n)	{  return n == startNode;	}
    public boolean isEnd(DataNode n)	{  return n == endNode;	}
    public boolean isEndpoint(DataNode n)	{  return isStart(n) || isEnd(n);	}

	//------------------------------------------------------------------------------------------
	
	ChangeListener<Bounds> startbounds = new ChangeListener<Bounds>()
	{ 
		@Override public void changed(ObservableValue<? extends Bounds> observable, Bounds oldValue, Bounds newValue)
		{ connect(); }  //false
	};

	ChangeListener<Bounds> endbounds = new ChangeListener<Bounds>()
	{ 
		@Override public void changed(ObservableValue<? extends Bounds> observable, Bounds oldValue, Bounds newValue)
		{ connect(); }   //true
	};

	ChangeListener<Number> startposition = new ChangeListener<Number>()
	{ 
		@Override public void changed(ObservableValue<? extends Number> observable,Number oldValue, Number newValue)
		{ connect(); }   //false
	};

	ChangeListener<Number> endposition = new ChangeListener<Number>()
	{ 
		@Override public void changed(ObservableValue<? extends Number> observable,Number oldValue, Number newValue)
		{ connect(); }   //true
	};

	
	void addListeners()
	{
		if (startNode != null)
		{
			startNode.getStack().layoutBoundsProperty().addListener(startbounds);
			startNode.getStack().layoutXProperty().addListener(startposition);
		}
		if (endNode == null){
			String endId = get("end");
			if (endId != null && getModel() != null)
			{
				DataNode mnode = getModel().getDataNode(endId);
				if (mnode != null)
					endNode = mnode;
			}
		}
		if (endNode != null)
		{	
			endNode.getStack().layoutBoundsProperty().addListener(endbounds);
			endNode.getStack().layoutXProperty().addListener(endposition);
		}
	}
	public void removeListeners() 
	{
		if (startNode != null)
		{
			startNode.getStack().layoutBoundsProperty().removeListener(startbounds);
			startNode.getStack().layoutXProperty().removeListener(startposition);
		}
		if (endNode != null)
		{	
			endNode.getStack().layoutBoundsProperty().removeListener(endbounds);
			endNode.getStack().layoutXProperty().removeListener(endposition);
		}
	}
    //------------------------------------------------------------------------------------------
   @Override public String toString()
    {
    	EdgeLine eLine = getEdgeLine();
    	if (eLine == null) return "NO EDGELINE";
    	Point2D endpt = eLine.lastPoint();
    	return "Edge from " + (startNode == null ? "Null" : (startNode.getStack().getText() + " @ " + StringUtil.asString(eLine.firstPoint())  ) +
    			" to "  + (endNode == null ? "Null" : endNode.getStack().getText())  + " @ " + StringUtil.asString(endpt));
    }

   public boolean isSelected()  	 {	   return getEdgeLine().isSelected();   }
   public void select(boolean on)   {	   getEdgeLine().select(on);   }
    //------------------------------------------------------------------------------------------
    public String getPointsStr()
	{
		if (edgeLine == null) return "";
		List<GPMLPoint> pts = edgeLine.getPoints();
		if (pts == null) return "";
		StringBuilder builder = new StringBuilder();
		for (GPMLPoint pt : pts)
			builder.append(pt.toGPML());
		return builder.toString();
	}
    
    public List<Anchor> getAnchors() { return edgeLine.getAnchors();  }

    public String getAnchorsStr ()
	{
		List<Anchor> anchors = edgeLine.getAnchors();
		StringBuilder builder = new StringBuilder();
		for (Anchor a : anchors)
			builder.append(a.toGPML());
		return builder.toString();
	}
	//----------------------------------------------------------------------
//	protected AttributeMap attributes = new AttributeMap();
//	public AttributeMap getAttributes() {		return attributes;	}
	
	protected EdgeLine edgeLine;
	public EdgeLine getEdgeLine() 	
	{
		if (edgeLine.getLine() != null)
			return edgeLine;	
		return null;
	}
	
	protected DataNode startNode=null, endNode=null;
	public void setStartNode(DataNode dn)	{ 	startNode = dn;	}
	public void setEndNode(DataNode dn)		{  endNode = dn;	}
	public DataNode getStartNode()		{ 	return startNode;	}
	public DataNode getEndNode()		{ 	return endNode;	}

	public Model getModel()		{ 	return model;	}
	protected Model model = null;
	protected int zOrder;
	public int getz() 				{	return zOrder;	}
	public void setz(int i) 		{	zOrder = i;	}
	
	
	public double getStrokeWidth() 
	{	String s =  get("LineThickness"); 
		if (s == null) return 1.4;
		if (StringUtil.isNumber(s)) return StringUtil.toDouble(s);	
		return 1.4;
	}
	public String getLayer()		{ 	return get("Layer");	}
	private Color color = Color.BLACK;
	public Color getColor() 		{	return color;	}
	public void setColor(Color c) 	{	color = c;	}
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
	
	private SimpleStringProperty source = new SimpleStringProperty();
	public StringProperty  sourceProperty()  { return source;}
	public String getSource()  { return source.get();}
	public void setSource(String s)  { source.set(s);}

	private SimpleStringProperty target = new SimpleStringProperty();	
	public StringProperty  targetProperty()  { return target;}
	public String getTarget()  { return target.get();}
	public void setTarget(String s)  { target.set(s);}
	
	private SimpleStringProperty sourceid = new SimpleStringProperty();	
	public StringProperty  sourceidProperty()  { return sourceid;}
	public String getSourceid()  { return sourceid.get();}
	public void setSourceid(String s)  { sourceid.set(s);}
	
	private SimpleStringProperty targetid = new SimpleStringProperty();	
	public StringProperty  targetidProperty()  { return targetid;}
	public String getTargetid()  { return targetid.get();}
	public void setTargetid(String s)  { targetid.set(s);}
	
	private SimpleStringProperty interaction = new SimpleStringProperty();	
	public StringProperty  interactionProperty()  { return interaction;}
	public String getInteraction()  { return interaction.get();}
	public void setInteractionProperty(String s)  { interaction.set(s);}

}

