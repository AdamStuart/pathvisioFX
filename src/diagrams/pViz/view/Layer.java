package diagrams.pViz.view;

import java.util.List;

import javafx.scene.Group;
import javafx.scene.Node;

public class Layer extends Group {
	private String name;
	private boolean vis;
	private boolean locked;
	
	public String getName() 			{ return name;	}
	public boolean isVis()				{ return vis;	}
	public void setVis(boolean b)		{ vis = b;	}
	public boolean isLocked() 			{ return locked;	}
	public void setLocked(boolean b)	{ locked = b;	}
	public void lock()					{ locked = true;	}
	public void unlock()				{ locked = false;	}
	
	public int getCount() 				{ return getChildren().size();	}
	public List<Node> getNodes() 		{ return getChildren();	}

	public void clear()					{ getChildren().clear();	}
	public void sort()					{ getChildren().sort(null);	}
	
	public Layer(String s)
	{
		name = s;
		setId(s);
		vis = true;
		locked = false;
	}
	public void add(int index, Node node)
	{
		if (locked) return;
		getChildren().add(index, node);
		node.setVisible(vis);
	}
	public void add(Node node)
	{
		if (locked) return;
		if (getChildren().contains(node)) 
		{
			return;		//TODO   if STRICT throwo exception
		}
		getChildren().add(node);
		node.setVisible(vis);
	}
	
	public void remove(VNode node)
	{
		if (locked) return;
		getChildren().remove(node);
		node.setVisible(false);
	}
	
	public void remove(Node node)
	{
		if (locked) return;
		getChildren().remove(node);
		node.setVisible(vis);
	}
	
}
