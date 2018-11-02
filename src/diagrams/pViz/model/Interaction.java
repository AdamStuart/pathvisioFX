package diagrams.pViz.model;



import java.util.List;

import diagrams.pViz.gpml.Anchor;
import diagrams.pViz.gpml.GPMLPoint;
import diagrams.pViz.view.VNode;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.ObservableList;
import javafx.scene.control.TreeItem;
import model.AttributeMap;
import model.bio.XRefable;
import util.StringUtil;

public class Interaction extends Edge implements Comparable<Interaction>
{
	/**
	 * 		Interaction is the biological wrapper around Edge
	 */
	private static final long serialVersionUID = 1L;
//	GeneListRecord geneListRecord;
	protected XRefable source;
	protected XRefable target;

	private SimpleStringProperty interactionType = new SimpleStringProperty();		
	public StringProperty  interactionTypeProperty()  { return interactionType;}
	public void setInteractionType(String s)  { interactionType.set(s);}
	public  String getInterType()  { return interactionType.get();}

	public void dump()	{ System.out.println(get("GraphId") + toString());	}
	public Interaction(Model inModel)
	{
		super(inModel);
//		if (!inModel.containsEdge(this))
//			inModel.addEdge(this);
		interactionType.set("arrow");
	}
	
	
	public Interaction(AttributeMap attr, Model inModel)
	{
		super(attr, inModel);
		interactionType.set("arrow");
	}

	
	@Override public void makeEdgeLine( List<GPMLPoint> pts, List<Anchor> anchors) {
		edgeLine = new EdgeLine(this, pts, anchors);
	}
	public Interaction(Model model, VNode src, VNode vNode) {
		this(model,src,vNode, null, null, null);
	}	
	
	public Interaction(Model inModel, VNode start, VNode end, AttributeMap attr, List<GPMLPoint> pts, List<Anchor> anchors) 
	{
    	super( inModel,  start,  end,  attr,  pts,  anchors);
    	
    	if (pts != null)
    	{	
    		int siz = pts.size();
    		if (siz >= 2)
    		{
    			GPMLPoint endPt = pts.get(siz-1);
        		interactionType.set(endPt.getArrowType().toString());
    		}
    	}
    }
	@Override public String toString()
	{
		return getName() + " " + getGraphId() + " " + (edgeLine == null ? "" : edgeLine.toString());
	}
	   public Interaction(GPMLPoint startPt, GPMLPoint endPt, double thickness, Model inModel) 
	    {
			super(startPt, endPt, thickness, inModel);
			model.addEdge(this);
	      }


	public int compareTo(Interaction other)
	{
		return getName().compareToIgnoreCase(other.getName());
	}
	
	public void rebind() {
		if (isWellConnected())
		{
			String gid = getStartNode().getGraphId();
			model.getController().rebind(gid);
		}
		else
		{
			String src = get("sourceid");
			String targ = get("targetid");
			if (StringUtil.isEmpty(src) ||StringUtil.isEmpty(targ))
				System.err.println("Interaction rebind failed");
			startNode = model.findDataNode(src);
			endNode = model.findDataNode(targ);
//			if (endNode == null)
//				endNode = model.findAnchor(targ);
			String gid = getStartNode().getGraphId();
			model.getController().rebind(gid);
		}
		
	}
	public void rebind(String sourceId) {
		if (!isWellConnected()) return; 
		setSource(getStartNode().getName());
		setTarget(getEndNode().getName());
		setNameFromState();
		model.getController().getTreeTableView().addBranch(this, sourceId);
	}
	
	public boolean isWellConnected() {
		return getStartNode() != null && getEndNode() != null;
	}
	public void setNameFromState() {
		String arrow = get("ArrowHead");
		if (arrow == null) arrow = "arrow";
		else arrow = arrow.toLowerCase();
		setInteractionType(arrow);
		if ("arrow".equals(arrow)) 		arrow = "->";
		if (arrow.contains("inhibit")) 	arrow = "-|";
		if (arrow.contains("conver")) 	arrow = ">>";
		if (arrow.contains("catal")) 	arrow = "-O";
		if (arrow.contains("reg")) 		arrow = " regs";
		String targPropertyValue = getTarget();
		if (targPropertyValue == null)
		setName("--" + arrow + " " + getEndName());
	}
	public String getStartName() {
		return getStartNode() == null ?  get("sourceid") : getStartNode().getName();
	}
	public String getEndName() {
		return getEndNode() == null ? get("targetid") : getEndNode().getName();
	}
	public void resetEdgeLine() {
		edgeLine.setStartPoint(getStartNode().getStack().center());
		edgeLine.setEndPoint(getEndNode().getStack().center());
		
	}
}

//----------------------------------------------------------------------
//GPML specific
	
//public void setAttributes(AttributeMap attr) {
//		if (attr == null || attr.isEmpty()) return;
//		attributes.addAll(attr); 
//		for (String key : attributes.keySet())
//		{
//			String val = attributes.get(key);
//			if ("LineThickness".equals(key))
//				edgeLine.setStrokeWidth(StringUtil.toDouble(val));
//			
//			else if ("GraphId".equals(key))		{	graphId = val;}
//			else if ("ZOrder".equals(key))		{	zOrder = StringUtil.toInteger(val);	}
//			else if ("ConnectorType".equals(key))		
//			{
//				if ("Elbow".equals(val))
//					edgeLine.setEdgeType(EdgeType.elbow);
//			}
//			else if ("Color".equals(key))		
//			{
//				if (val != null)
//					setColor(Color.valueOf(val));
//				edgeLine.setStroke(color);
//			}
//			else if ("LineStyle".equals(key))		
//			{
//				Double[] vals = {10.0, 5.0};
//				edgeLine.setStrokeDashArray("Broken".equals(val) ? vals : null);
//			}
//			else if ("ArrowHead".equals(key))
//				attributes.put("ArrowHead", val);
//		}
//	}
