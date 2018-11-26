package diagrams.pViz.model.nodes;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import diagrams.pViz.model.Model;
import diagrams.pViz.view.Pasteboard;
import diagrams.pViz.view.ShapeFactory;
import diagrams.pViz.view.VNode;
import javafx.geometry.BoundingBox;
import model.AttributeMap;


@SuppressWarnings("serial")
public class DataNodeGroup extends DataNode {

	private List<DataNode> members = new ArrayList<DataNode>();
//	public List<DataNode> getChildren() { return children ; } 
	public void addMember(DataNode child)	{		members.add(child);	}
	public void clearMembers() 				{		members.clear();	}
	public List<DataNode> getMembers() 		{		return members;	}
	
	public DataNodeGroup(AttributeMap map, Model m)
	{
		super(map,m);
//		System.out.println(model.traverseSceneGraph(getStack()));
	}	

	// go thru all children and get the enclosing rectangle
	// set the models attributes, and tell the stack to resize its shape
	private double minX = Double.MAX_VALUE;
	private double maxX = Double.MIN_VALUE;
	private double minY = Double.MAX_VALUE;
	private double maxY = Double.MIN_VALUE;
	public void calcBounds()
	{
		System.out.print("Group " + getGraphId() + " has membership of " + members.size() + " (" + stack.getChildren().size() + ")");
//		groupView.getChildren().clear();
		for (DataNode child : members)
		{
			double centerX =child.getDouble("CenterX");
			double centerY =child.getDouble("CenterY");
			double width =child.getDouble("Width");
			double height =child.getDouble("Height");
			double halfwidth = width / 2;
			double halfheight = height / 2;
			if (centerX - halfwidth < minX)  minX = centerX - halfwidth;
			if (centerX + halfwidth > maxX)  maxX = centerX + halfwidth;
			if (centerY - halfheight < minY)  minY = centerY - halfheight;
			if (centerY + halfheight > maxY)  maxY = centerY + halfheight;
		}
		putDouble("X", minX);
		putDouble("Y", minY);
		putDouble("CenterX", (minX + maxX) / 2.0);
		putDouble("CenterY", (minY + maxY) / 2.0);
		putDouble("Width",  maxX - minX);
		putDouble("Height",  maxY - minY);
		getStack().setRect(minX, minY, maxX - minX, maxY - minY);
		ShapeFactory.resizeFigureToNode(getStack());
		System.out.println(String.format(" and Bounds: [ ( %.2f,  %.2f )  %.2f x  %.2f]", minX, minY, maxX - minX, maxY - minY)); 
		}
//---------------------------------------------------------------------------
	public void assignMembers() {
		String groupId = get("GroupId");
		if (groupId == null) 		return;		//ERROR
		clearMembers();
		Map<String, DataNode> nodes = model.getDataNodeMap();
		Pasteboard pasteboard = model.getController().getPasteboard();
		for (String nodeKey : nodes.keySet())
		{
			DataNode nod = nodes.get(nodeKey);
			String groupRef = nod.get("GroupRef");
			if (groupId.equals(groupRef))
				addMember(nod);
		}
		calcBounds();
		double minX = getDouble("X");
		double minY = getDouble("Y");
		int i = -1 * getMembers().size() / 2;
		double myHeight = getDouble("Height");
		for (DataNode node : getMembers())
		{
			VNode childstack = node.getStack();
			childstack.setId(node.getGraphId());
			pasteboard.getContentLayer().remove(childstack);
			stack.getChildren().add(childstack);
			double x = node.getDouble("X");
			double y = node.getDouble("Y");
			double w = node.getDouble("Width");
			double h = node.getDouble("Height");
			childstack.setTranslateX(x-minX);
			childstack.setTranslateY(y-minY-h);
			childstack.setMouseTransparent(true);
		}
		System.out.println("inGroup: " + stack.getChildren().size());
		updateView();
		
	}

	public BoundingBox getBounds() {
		double padding = 5;
		return new BoundingBox(minX-padding,minY-padding,0, maxX-minX + 2 * padding, maxY-minY+ 2 * padding, 0);
	}

	public void updateView() {
		if (stack != null) 
			stack.setRect(getDouble("X"), getDouble("Y"), getDouble("Width"), getDouble("Height"));		
	}

	public VNode getStack()					{		return stack;	}
	public void setStack(VNode st)			{		 stack = st;	}
	public Model getModel()					{		return model;	}
	public String getShapeType() 			{		return "GroupComponent";	}
	public String getType() 				{		return get("Type");	}
	public String getLabel() 				{		return get("TextLabel");	}

	public String getInfoStr()	{ return "GROUP HTML Template for " + getGraphId() + "\n" + toString();	}
//	@Override public String toString()	{ return getGraphId() + " = " + getName();	}
	//---------------------------------------------------------------------------------------
	public String toGPML()	{ 
		StringBuilder bldr = new StringBuilder();
		buildNodeOpen(bldr);
		buildXRefTag(bldr);
//		buildNodeClose(bldr);
		return bldr.toString();
	}
	String elementType = "Group";
	String[]  nodeAttrs = {  "TextLabel", "GroupId", "GraphId", "Style"};
	private void buildNodeOpen(StringBuilder bldr) {
		bldr.append("<Group " + attributeList(nodeAttrs) + " />\n");
	}

}
