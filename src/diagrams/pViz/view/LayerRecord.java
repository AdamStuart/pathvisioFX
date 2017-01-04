package diagrams.pViz.view;

import diagrams.pViz.app.GPMLRecord;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.ListChangeListener;
import javafx.scene.Node;
import javafx.scene.input.DataFormat;

public class LayerRecord implements GPMLRecord {

	public LayerRecord(Layer layer, boolean visibl, boolean locked)
	{
		theLayer = layer;
		setVis(visibl);
		setLock(locked);
		setName(layer.getName());
		theLayer.getChildren().addListener(listen);
	}
	public LayerRecord(String name)
	{
		this(new Layer(name), true, false);
	}
	
	ListChangeListener<Node> listen = new ListChangeListener<Node>()
	{
		@Override public void onChanged(Change<? extends Node> c)	
		{ 
			System.out.println("ListChangeListener");
			count.set(theLayer.getChildren().size());	
		}
	};
	
	private Layer theLayer = null;
	public void setLayer(Layer lay ) { theLayer = lay;	}
	public Layer getLayer() { return theLayer;	}
	
	@Override
	public void getInfo(DataFormat mimetype, String a, String b) {

	}
	public boolean toggleLock() 	{ setLock(!getLock());	return getLock(); }
	public boolean toggleVisible() { setVis(!getVis());		return getVis(); }

	BooleanProperty vis = new SimpleBooleanProperty();
	BooleanProperty lock = new SimpleBooleanProperty();
	StringProperty name = new SimpleStringProperty();
	IntegerProperty count = new SimpleIntegerProperty();

	public BooleanProperty  vis()  { return vis;}
	public Boolean getVis()  { return vis.get();}
	public void setVis(Boolean b)  { vis.set(b);}

	public BooleanProperty  lock()  { return lock;}
	public Boolean getLock()  { return lock.get();}
	public void setLock(Boolean b)  { lock.set(b);}

	public StringProperty  nameProperty()  { return name;}
	public String getName()  { return name.get();}
	public void setName(String s)  { name.set(s);}

	public IntegerProperty  countProperty()  { return count;}
	public int getCount()  { return count.get();}
	public void setCount(int s)  { count.set(s);}

	public void add(int index, Node node) 	{ 	theLayer.add(index, node); 	}
	public void add(Node node) 				{ 	theLayer.add(node); 	}
	public void remove(VNode node) 			{ 	theLayer.remove(node); 	}
	public void remove(Node node) 			{ 	theLayer.remove(node); 	}

}
