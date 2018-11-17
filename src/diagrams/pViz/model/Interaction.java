package diagrams.pViz.model;



import java.util.List;

import diagrams.pViz.gpml.Anchor;
import diagrams.pViz.gpml.GPMLPoint;
import diagrams.pViz.gpml.GPMLPoint.ArrowType;
import diagrams.pViz.view.VNode;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import model.AttributeMap;
import model.bio.XRefable;
import model.stat.RelPosition;
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
	
	
	public Interaction(AttributeMap attr, Model inModel, List<GPMLPoint> pts, List<Anchor> anchors)
	{
		super(attr, inModel, pts, anchors);
		interactionType.set("arrow");
		GPMLPoint.setInteraction(pts, this);
	}

	
//	@Override public void makeEdgeLine( List<GPMLPoint> pts, List<Anchor> anchors) {
//		edgeLine = new EdgeLine(this, pts, anchors);
//	}
	
	public Interaction(Model model, VNode src, Pos srcPosition, 
			VNode vNode, RelPosition targPosition, ArrowType arrow, EdgeType edge) 
	{
		this(model,src,vNode, null);
		setInteractionType(edge.toString());
//		String newId = newEdgeId();
//		put("GraphId", newId);
//		setGraphId(newId);
		GPMLPoint  startPoint = new GPMLPoint(src.getPortPosition(srcPosition));
		startPoint.setRelPosition(srcPosition);
		startPoint.setGraphRef(src.getGraphId());
		edgeLine.setStartPoint(startPoint);
		startPoint.setPos(srcPosition);
		
		GPMLPoint endPoint = new GPMLPoint(vNode.getRelativePosition(targPosition));
		endPoint.setRelX(targPosition.x());
		endPoint.setRelY(targPosition.y());
		edgeLine.setEndPoint(endPoint);
		endPoint.setGraphRef(vNode.getGraphId());
		endPoint.setPos(targPosition);
		endPoint.setArrowType(arrow);
	}	

	
	public Interaction(Model inModel, VNode start, VNode end, AttributeMap attr) 		//, List<GPMLPoint> pts, List<Anchor> anchors
	{
    	super( inModel,  start,  end,  attr);	
     }
	
	@Override public String toString()
	{
		String name =  getName();
		if (StringUtil.isEmpty(name)) name = "#"; 
		String id =  getGraphId();
		if (StringUtil.isEmpty(id)) id = "*"; 
		String str = (edgeLine == null) ? "X" : edgeLine.toString();
		return name + " " + id + " " + str;
	}

	public int compareTo(Interaction other)
	{
		return getName().compareToIgnoreCase(other.getName());
	}
	
	public void rebind() {
		if (isWellConnected())
		{
//			String gid = getStarTtNode().getGraphId();
//			model.getController().rebind(gid);
		}
		else
		{
			System.err.println("Interaction rebind required");
			String src = get("sourceid");
			String targ = get("targetid");
			if (StringUtil.isEmpty(src) ||StringUtil.isEmpty(targ))
			{	
				System.err.println("Interaction rebind failed");
				return;
			}
			startNode = model.findDataNode(src);
			endNode = model.findDataNode(targ);
//			if (endNode == null)
//				endNode = model.findAnchor(targ);
			if (startNode != null)
			{
				String gid = getStartNode().getGraphId();
				Point2D lastPoint = getEdgeLine().lastPoint();
				getEdgeLine().setEndPoint(lastPoint);
//				model.getController().rebind(gid);
			}
			else
			{
				
			}
		}
		
	}
	public void rebind(String sourceId) {
		if (!isWellConnected()) return; 
		setSource(getStartNode().getName());
		setTarget(getEndNode().getName());
		setNameFromState();
		model.getController().getTreeTableView().addBranch(this, sourceId);
//		repostionAnchors();
	}
	
	public boolean isWellConnected() {
		return getStartNode() != null && getEndNode() != null;
	}
	public void setNameFromState() {
		String arrow = get("ArrowHead");
		if (arrow == null) arrow = "arrow";
		else arrow = arrow.toLowerCase();
		setInteractionType(arrow);
		if ("arrow".equals(arrow)) 				arrow = "->";
		else if (arrow.contains("none")) 		arrow = "--";
		else if (arrow.contains("tbar")) 		arrow = "-|";
		else if (arrow.contains("inhibit")) 	arrow = "-|";
		else if (arrow.contains("conver")) 		arrow = ">>";
		else if (arrow.contains("catal")) 		arrow = "-O";
		else if (arrow.contains("bind")) 		arrow = "-{";
		else if (arrow.contains("transcriptiontranslation")) 		arrow = "-#";
		else if (arrow.contains("reg")) 		arrow = "-R";
		String target = getTarget();
		if ( target == null)
		{
			target = get("targetid");
			if ( target == null)
				target = "??";
		}
		setName("--" + arrow + " " + target);
	}
	public String getStartName() {
		return getStartNode() == null ?  get("sourceid") : getStartNode().getName();
	}
	public String getEndName() {
		return getEndNode() == null ? get("targetid") : getEndNode().getName();
	}
//	public void resetEdgeLine() {
//		edgeLine.setStartPoint(getStartNode().getStack().center());  // TODO adjusted pts
//		edgeLine.setEndPoint(getEndNode().getStack().center());
//	}
	public Anchor findAnchorById(String targId) {
		return getModel().findAnchorById(targId);
	}
}
