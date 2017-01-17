package diagrams.pViz.gpml;

import java.util.ArrayList;
import java.util.List;

import diagrams.pViz.app.Controller;
import diagrams.pViz.model.MNode;
import javafx.geometry.BoundingBox;
import model.AttributeMap;

public class GPMLGroup extends MNode {
	private double minX = Double.MAX_VALUE;
	private double maxX = Double.MIN_VALUE;
	private double minY = Double.MAX_VALUE;
	private double maxY = Double.MIN_VALUE;
	private List<MNode> children = new ArrayList<MNode>();
	
	public GPMLGroup(AttributeMap am, Controller c)
	{
		super(am, c);
	}
	
	public void addToGroup(MNode child)
	{
		children.add(child);
	}
	
	public void calcBounds()
	{
		for (MNode child : children)
		{
			double centerX =child.getAttributeMap().getDouble("CenterX");
			double centerY =child.getAttributeMap().getDouble("CenterY");
			double width =child.getAttributeMap().getDouble("Width");
			double height =child.getAttributeMap().getDouble("Height");
			double halfwidth = width / 2;
			double halfheight = height / 2;
			if (centerX - halfwidth < minX)  minX = centerX - halfwidth;
			if (centerX + halfwidth > maxX)  maxX = centerX + halfwidth;
			if (centerY - halfheight < minY)  minY = centerY - halfheight;
			if (centerY + halfheight > maxY)  maxY = centerY + halfheight;
		}
	}

	public BoundingBox getBounds() {
		double padding = 5;
		return new BoundingBox(minX-padding,minY-padding,0, maxX-minX + 2 * padding, maxY-minY+ 2 * padding, 0);
	}

}
