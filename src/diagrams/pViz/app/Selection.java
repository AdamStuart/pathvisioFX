package diagrams.pViz.app;

import java.util.ArrayList;
import java.util.List;

import diagrams.pViz.model.MNode;
import diagrams.pViz.model.Model;
import diagrams.pViz.view.GroupMouseHandler;
import diagrams.pViz.view.Layer;
import diagrams.pViz.view.Pasteboard;
import diagrams.pViz.view.Shape1;
import diagrams.pViz.view.VNode;
import gui.Action.ActionType;
import gui.UndoStack;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
import javafx.geometry.Bounds;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.ImageView;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Polyline;
import javafx.scene.shape.Rectangle;
import model.AttributeMap;
import util.LineUtil;
import util.RectangleUtil;
import util.StringUtil;

public class Selection
{
	public Selection(Pasteboard layer)
	{
		root = layer;
		items = FXCollections.observableArrayList(); 
		items.addListener( (ListChangeListener<Node>)c -> { root.getController().getInspector().syncInspector();	});
	}
	private Model getModel()	{ return getController().getModel();  } 
	private Controller getController() { return root.getController();	}
//	private NodeFactory getNodeFactory() { return getController().getNodeFactory();	}
	private Pasteboard root;
	private ObservableList<VNode> items;
	//--------------------------------------------------------------------------
	public ObservableList<VNode> getAll()				{ return items;	}

	public VNode first()			{ return count() == 0 ? null : items.get(0);	}
	public void clear()				{ for (int i= items.size()-1; i>= 0; i--)
											deselect(items.get(i));  
									}
	public int count()				{ return items.size();	}
	public boolean isGroupable()	{ return count() > 1;	}

	//--------------------------------------------------------------------------
	public void selectX(VNode s)		{ clear(); select(s);	}
	public void select(VNode s)		
	{
		if ("Marquee".equals(s.getId())) return;
//		if (s.isLayerLocked()) return;
		items.add(s);	
		s.setEffect(new DropShadow()); 
		s.showPorts(true);
		
		ObservableMap<Object, Object> properties = s.getProperties(); 
		BooleanProperty selectedProperty = (BooleanProperty) properties.get("selected"); 
		if (selectedProperty == null)
		{
			selectedProperty = new SimpleBooleanProperty(Boolean.TRUE);
			properties.put("selected", selectedProperty );
		}
		else selectedProperty.set(Boolean.TRUE);			
		Object ref =  properties.get("BiopaxRef");
		if (ref != null)
			getController().hiliteByReference("" + ref);
		if (verbose ) 	System.out.println(s.toString());
	}
	boolean verbose = false;

	public void select(VNode s, boolean b)	{  if (b) select(s); else deselect(s);	}
	public void deselect(VNode s)	
	{ 
		items.remove(s);	
		s.setEffect(null);	
		s.showPorts(false);
		ObservableMap<Object, Object> properties = s.getProperties(); 
		BooleanProperty selectedProperty = (BooleanProperty) properties.get("selected"); 
		if (selectedProperty == null)
		{
			selectedProperty = new SimpleBooleanProperty(Boolean.FALSE);
			properties.put("selected", selectedProperty );
		}
		else selectedProperty.set(Boolean.FALSE);	
	}
	public boolean isSelected(Node s)		{ return items.contains(s);	}
	//--------------------------------------------------------------------------
	
	public void selectAll()		{		selectAll(null); 	}
	public void selectAll(List<Node> kids)	
	{	
		if (kids == null) kids = root.getChildrenUnmodifiable();
		for (Node n : kids) 
		{	
			if ("grid".equals(n.getId())) continue;
			if (!(n instanceof VNode)) continue;
			if (((VNode)n).getShape() instanceof Polyline) continue;
			if (items.contains(n)) continue;
			select((VNode)n); 
		}
	}

	//--------------------------------------------------------------------------
	public void deleteSelection()	
	{
		for (int i= items.size()-1; i>= 0; i--)
		{
			VNode node = items.get(i);
			if ("grid".equals(node.getId())) continue;
			items.remove(node);
			getController().remove(node);
		}
	}
	//--------------------------------------------------------------------------
	public void deleteAll()	
	{
		selectAll();
		deleteSelection();
	}
	// **-------------------------------------------------------------------------------
	// TODO -- this doesn't make the clone into the active selection (correctly)
	public void cloneSelection(int offset)
	{
		List<VNode> sel = getAll();
		List<VNode> newSelection = new ArrayList<VNode>();
		Controller controller = getController();
		for (VNode n : sel)
		{
			if ("Marquee".equals(n.getId()))	continue;
			if (!(n instanceof VNode))		continue;
			VNode node = (VNode) n;
			node.rememberPositionEtc(offset);
			AttributeMap newAttrs = new AttributeMap(node.getAttributes());
			String oldId = newAttrs.get("GraphId");
			String newId = controller.getModel().cloneResourceId(oldId);
			newAttrs.put("GraphId", newId);
			newAttrs.incrementZOrder();
			MNode clone = new MNode(newAttrs, controller);
			controller.add(clone.getStack());
			newSelection.add(clone.getStack());
		}
//		items.removeAll();
		clear();
		for (VNode vnode : newSelection)
			select(vnode);  
	}
	//--------------------------------------------------------------------------
	public void doGroup()
	{
		getUndoStack().push(ActionType.Group);
		Group group = new Group();
		group.addEventHandler(MouseEvent.ANY, new GroupMouseHandler(root));
		group.getChildren().addAll(items);
		deleteSelection();
		getController().add(group);
		group.setTranslateX(10);
	}
	private UndoStack getUndoStack() {		return getController().getUndoStack();	}
	//--------------------------------------------------------------------------
	public void applyStyle(String styleSettings)
	{
		for (VNode n : items)
		{
			String id = n.getId();
			if ((id == null) || ("Marquee".equals(id)))	continue;
			AttributeMap attr = new AttributeMap(styleSettings, true);
			String fill = attr.get("-fx-fill");
			if (fill != null)
				attr.put("-fx-background-color", fill);
			String stroke = attr.get("-fx-stroke");
			if (stroke != null)
				attr.put("-fx-border-color", stroke);
			String w = attr.get("-fx-stroke-width");
			if (w != null)
				attr.put("-fx-border-width", w);
			styleSettings = attr.getStyleString();
				
			if (n != null)
				n.setStyle(styleSettings);
			if (n.getShape() != null)
				n.getShape().setStyle(styleSettings);
			
			String opacStr = "-fx-opacity: ";
			int index = styleSettings.indexOf(opacStr);
			if (index > 0)
			{
				int start = index + opacStr.length();
				int end = styleSettings.indexOf(";", start);
				double d = StringUtil.toDouble(styleSettings.substring(start, end));
				if (!Double.isNaN(d))
					n.setOpacity(d);
			}
		}
	}
	//--------------------------------------------------------------------------
	public void setAttributes(AttributeMap styleSettings)
	{
		getController().getUndoStack().push(ActionType.Property);	
		for (Node n : items)
			getModel().setAttributes(n, styleSettings);
	}
	//--------------------------------------------------------------------------
	public void translate(KeyCode key)		
	{
		double amount = 30;
		double dx = 0, dy = 0;
		if (key == KeyCode.LEFT)		dx = amount;
		else if (key == KeyCode.RIGHT)	dx = -amount;
		else if (key == KeyCode.UP)		dy = amount;
		else if (key == KeyCode.DOWN)	dy = -amount;
		translate(dx, dy);
	}	
	
	public void translate(double dx, double dy)		
	{		
		getUndoStack().push(ActionType.Move);	
		for (Node n : items)
		{
			if (n.getParent() instanceof Group)
				n = n.getParent();
			if (n instanceof Rectangle)
			{
				Rectangle r = (Rectangle) n;
				double width = r.getWidth();
				double height = r.getHeight();
				double x = r.getX() - dx;
				double y = r.getY() - dy;
				RectangleUtil.setRect(r, x, y, width, height);
			}
			if (n instanceof Shape1)
			{
				Shape1 r = (Shape1) n;
				r.setTranslateX(r.getTranslateX() - dx);
				r.setTranslateY(r.getTranslateY() - dy);
			}			
//			if (n instanceof Shape2)
//			{
//				Shape2 r = (Shape2) n;
//				r.setTranslateX(r.getTranslateX() - dx);
//				r.setTranslateY(r.getTranslateY() - dy);
//			}
			if (n instanceof Circle)
			{
				Circle c = (Circle) n;
				c.setCenterX(c.getCenterX() - dx);
				c.setCenterY(c.getCenterY() - dy);
			}
			if (n instanceof Polygon)
			{
				Polygon c = (Polygon) n;
				for ( int i = 0; i < c.getPoints().size(); i += 2)
				{
					c.getPoints().set(i, c.getPoints().get(i) - dx);
					c.getPoints().set(i+1, c.getPoints().get(i+1) - dy);
				}
			}
			if (n instanceof Polyline)
			{
				Polyline c = (Polyline) n;
				for ( int i = 0; i < c.getPoints().size(); i += 2)
				{
					c.getPoints().set(i, c.getPoints().get(i) - dx);
					c.getPoints().set(i+1, c.getPoints().get(i+1) - dy);
				}
			}
			if (n instanceof Group)
			{
				n.setLayoutX(n.getLayoutX() - dx);
				n.setLayoutY(n.getLayoutY() - dy);
			}
			
			if (n instanceof ImageView)
			{
				ImageView r = (ImageView) n;
				double width = r.getFitWidth();
				double height = r.getFitHeight();
				double x = r.getX() - dx;
				double y = r.getY() - dy;
				RectangleUtil.setRect(r, x, y, width, height);
			}
			if (n instanceof StackPane)
			{
				StackPane r = (StackPane) n;
//				double width = r.getWidth();
//				double height = r.getHeight();
				double x = r.getLayoutX() - dx;
				double y = r.getLayoutY() - dy;
				r.setLayoutX(x);
				r.setLayoutY(y);
//				RectangleUtil.setRect(r, x, y, width, height);
			}
			if (n instanceof VBox)
			{
				StackPane r = (StackPane) (n.getParent());
				double width = r.getWidth();
				double height = r.getHeight();
				double x = r.getLayoutX() - dx;
				double y = r.getLayoutY() - dy;
				RectangleUtil.setRect(r, x, y, width, height);
			}
			if (n instanceof Line)
			{
				LineUtil.translateLine((Line) n, -dx, -dy);
			}
		}
	}

	//--------------------------------------------------------------------------
	public void select(Rectangle r)	
	{	
		if (r == null || r.getWidth() <= 0 || r.getHeight() <= 0)			return;
	
		for (Node n : root.getChildrenUnmodifiable()) 
		{
			if (n.isMouseTransparent())	 continue;
			if (n instanceof Layer)
			{
				Layer layer = (Layer) n;
				for (Node node : layer.getChildren()) 
				{
					if (!(node instanceof VNode))	 continue;
					Bounds bounds = node.boundsInParentProperty().get();
					if (bounds.intersects(r.getX(), r.getY(), r.getWidth(), r.getHeight()))
						select((VNode)node); 
				}
			}
			if (!(n instanceof VNode))	 continue;
			Bounds bounds = n.boundsInParentProperty().get();
			if (bounds.intersects(r.getX(), r.getY(), r.getWidth(), r.getHeight()))
				select((VNode)n); 
		}
	}
	
	//--------------------------------------------------------------------------
	public void setStyle(String settings)
	{
		for (Node n : items)
			if (n != null) 
				n.setStyle(settings);
	}	

	//--------------------------------------------------------------------------
	public void cut()		{		copy();		deleteSelection();	}	
	public void copy()		{	   toClipboard(getState());		}	
	public void paste()		{	  		}	
	//--------------------------------------------------------------------------
	public void toFront()	{	for (Node n : items)	n.toFront();	}
	public void toBack()	{	for (Node n : items)	n.toBack();		}
	
	//--------------------------------------------------------------------------
//	public void group()
//	{
//		Group group = new Group();
//		for (Node n : items)
//			group.getChildren().addAll(n);
//		
//		deleteSelection();
//		AttributeMap attr = new AttributeMap("ShapeType:Group");
//		MNode model = new MNode(attr, getController().getDrawModel());
//		getController().add(model.getStack());
//		select(model.getStack());
//	}	
	
	public void ungroup()
	{
		// TODO items.stream().filter(n instanceof Group).forEach
		for (VNode n : items)
			if (n.isGroup())
			{
				Controller c = getController();
				List<VNode> kids = n.ungroup();
				c.addAll(kids);
//				c.selectAll(kids);
			}
	}
	boolean NO_UNDO = false;
	//--------------------------------------------------------------------------
	public String getState()
	{
		if (NO_UNDO) return "";
		StringBuilder b = new StringBuilder();
		for (Node n : items)
			if (n != null && !n.getId().equals("Marquee"))
				if (n instanceof VNode)
					b.append(Model.describe(((VNode)n).modelNode()));
		return b.toString();
	}

//--------------------------------------------------------------------------
	public void toClipboard(String s)
	{
		final Clipboard clipboard = Clipboard.getSystemClipboard();
		final ClipboardContent content = new ClipboardContent();
		content.putString(s);
		clipboard.setContent(content);
	}
	
	//--------------------------------------------------------------------------
	@Override	public String toString()	{		return items.size() + " selected";	}
	public void applyLocks(boolean movable, boolean resizable, boolean editable) {
		for (Node n : items)
			if (n instanceof VNode)
				((VNode)n).applyLocks(movable, resizable, editable);
	}
	public void setMovable(boolean b)	{ 
		for (Node n : items)
			if (n instanceof VNode)
				((VNode)n).setMovable(b);	
		}
	public void setEditable(boolean b)	{ 
		for (Node n : items)
			if (n instanceof VNode)
				((VNode)n).setEditable(b);	
	}
	public void setResizable(boolean b){
		for (Node n : items)
			if (n instanceof VNode)
				((VNode)n).setResize(b);	
	}
	public void setLayer(String layername) {
		for (Node n : items)
			if (n instanceof VNode)
				((VNode)n).setLayer(layername);	
		}

	public void putColor(String string, Color value) {
		for (Node n : items)
			if (n instanceof VNode)
				((VNode)n).getAttributes().putColor(string, value);	
	}
	public void putDouble(String string, Double value) {
		for (Node n : items)
			if (n instanceof VNode)
				((VNode)n).getAttributes().putDouble(string, value);	
	}
	public void put(String string, String value) {
		for (Node n : items)
			if (n instanceof VNode)
				((VNode)n).getAttributes().put(string, value);	
	}
	public void extract() {
		for (Node n : items)
			if (n instanceof VNode)
				((VNode)n).extractPosition();	
	}



}
