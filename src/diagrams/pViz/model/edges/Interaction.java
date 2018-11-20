package diagrams.pViz.model.edges;



import java.util.List;

import diagrams.pViz.gpml.Anchor;
import diagrams.pViz.gpml.GPMLPoint;
import diagrams.pViz.gpml.GPMLPoint.ArrowType;
import diagrams.pViz.model.Model;
import diagrams.pViz.view.VNode;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
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
		putDouble("LineThickness", 1.5);
		
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
	public void rebind(String sourceId) {
		if (!isWellConnected()) return; 
		setSource(getStartNode().getName());
		setTarget(getEndNode().getName());
		setNameFromState();
		model.getController().getTreeTableView().addBranch(this, sourceId);
//		repostionAnchors();
	}
	   //------------------------------------------------------------------------------------------
    public String toGPML()
   {
		StringBuffer b = new StringBuffer(String.format("<Interaction GraphId=\"%s\" >\n", getSafe("GraphId")));
		b.append("<Graphics ");
		b.append(attributeList(new String[]{"ConnectorType", "ZOrder","LineStyle","LineThickness"}));
		b.append (" >\n");
		
		b.append (getPointsStr());
		b.append (getAnchorsStr());
		b.append("</Graphics>\n");
		String db = get("database");
		String dbid =  get("dbid");
		if (db != null && dbid != null)
			b.append(String.format("<Xref Database=\"%s\" ID=\"%s\" />\n", db, dbid));
		b.append("</Interaction>\n");
		return b.toString();
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
		else if (arrow.contains("activ")) 		arrow = "->";
		else if (arrow.contains("conver")) 		arrow = ">>";
		else if (arrow.contains("catal")) 		arrow = "-O";
		else if (arrow.contains("bind")) 		arrow = "-{";
		else if (arrow.contains("stimu")) 		arrow = "+>";
		else if (arrow.contains("modif")) 		arrow = "~>";
		else if (arrow.contains("cleav")) 		arrow = "-\\";
		else if (arrow.contains("coval")) 		arrow = "::";
		else if (arrow.contains("transcriptiontranslation")) 		arrow = "-#";
		else if (arrow.contains("translat")) 	arrow = "-X";
		else if (arrow.contains("gap")) 		arrow = " >";
	else if (arrow.contains("reg")) 			arrow = "-R";
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


	public Anchor findAnchorById(String targId) {
		return getModel().findAnchorById(targId);
	}
}
