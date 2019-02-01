package diagrams.pViz.view;

import java.util.List;
import java.util.Map;

import diagrams.pViz.app.Controller;
import diagrams.pViz.app.Selection;
import diagrams.pViz.app.Tool;
import diagrams.pViz.model.nodes.DataNodeGroup;
import gui.Action.ActionType;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.TableView;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import model.stat.RelPosition;

/**
 * Listeners for making the nodes draggable via left mouse button. Considers if parent is zoomed.
 * Listen for right click to build a context sensitive menu for this node
 */
public class VNodeGestures {

     DragContext nodeDragContext = new DragContext();
     VNode vNode;
    Pasteboard canvas;

    public VNodeGestures(VNode inNode, Pasteboard canvas) {
        this.canvas = canvas;
        vNode = inNode;

    }

    public EventHandler<MouseEvent> getOnMousePressedEventHandler() {   return mousePressedHandler;    }
    public EventHandler<MouseEvent> getOnMouseDraggedEventHandler() {   return mouseDraggedHandler;  }
    public EventHandler<MouseEvent> getOnMouseClickedEventHandler() {   return mouseClickedHandler;  }

    //-------------------------------------------------------------------------------
    private EventHandler<MouseEvent> mouseClickedHandler = new EventHandler<MouseEvent>() {

        public void handle(MouseEvent event) {
        	event.consume();
        }
       };
    
    private EventHandler<MouseEvent> mousePressedHandler = new EventHandler<MouseEvent>() {

        public void handle(MouseEvent event) {

    		Tool curTool = canvas.getTool();
    		if (curTool.isComponent()) return;
          // left mouse button => dragging
            if( event.isSecondaryButtonDown())
            {
            	doContextMenu(event, vNode);
            	return;
            }
            if (vNode.modelNode().isLocked())	return;
//            nodeDragContext.mouseAnchorX = event.getSceneX();
//            nodeDragContext.mouseAnchorY = event.getSceneY();

            Node node = (Node) event.getSource();
//			System.out.println(String.format("=============== %.1f, %.1f", event.getSceneX(), event.getSceneY()));

//            nodeDragContext.translateAnchorX = node.getTranslateX();
//            nodeDragContext.translateAnchorY = node.getTranslateY();
    		if (event.getClickCount() > 1)	   {	vNode.getInfo(); 	event.consume();   return; }
    		if (event.isPopupTrigger())	   {	doContextMenu(event, vNode); return;	   }
    		vNode.finishDragLine(event);
    	   if (curTool.isInteraction()) 
    	   {
    		   canvas.startDragLine(vNode, Pos.CENTER);
    		   return;
    	   }
    
    	   
    	   Selection selection = getController().getSelectionManager();
      		boolean wasSelected = selection.isSelected(vNode);
    	   if (event.isAltDown())
    		   selection.duplicateSelection();
    	   
    		double prevMouseX = event.getSceneX();
    		double  prevMouseY = event.getSceneY();
    	   boolean inCorner = vNode.ptInCorner(prevMouseX, prevMouseY);
    	   if (inCorner)
    		   vNode.handleResize(event);
    	   else
    	   {
    		if (event.isControlDown() || event.isShiftDown())			//TODO -- configurable?
    			selection.select(vNode, !wasSelected);
    		else if (!wasSelected)
    			selection.selectX(vNode);
    	   }
    	   event.consume();
        }
    };

    double prevX = -1; 
 	double prevY = -1; 

 	private EventHandler<MouseEvent> mouseDraggedHandler = new EventHandler<MouseEvent>() {
        public void handle(MouseEvent event) {

            // left mouse button => dragging
//    		if (!vNode.isMovable()) return;
            if( !event.isPrimaryButtonDown())
                return;
            if (vNode.modelNode().isLocked())	
            	return;
            Tool t = vNode.getPasteboard().getTool();
            if (t.isComponent()) return;
            double scale = canvas.getScale();
//
//            Node node = (Node) event.getSource();
//            node.setTranslateX(nodeDragContext.translateAnchorX + (( event.getSceneX() - nodeDragContext.mouseAnchorX) / scale));
//            node.setTranslateY(nodeDragContext.translateAnchorY + (( event.getSceneY() - nodeDragContext.mouseAnchorY) / scale));
//
        	double ex = event.getSceneX();
    		double ey = event.getSceneY();
//    				System.out.println(String.format("%.1f, %.1f", ex, ey));
    		if (vNode.isResizing())
    		{
    			vNode.handleResize(event);
    			event.consume();
    			return;
    		}
        	if (event.isShiftDown())
        	{
        		// TODO  if there are edges connected to this node, constrain the coordinates to the other sides
        		ex = ey;
        	}
//    		super.handleMouseDragged(event);
    		 if (vNode.isSelected())
            {	
               	double dx = 0;
            	double dy = 0;
            	if (prevX > 0 && prevY > 0)
            	{
            		dx = ex - prevX;
            		dy = ey - prevY;
        			if (Math.abs(dy) > 10|| Math.abs(dy) > 10)
                		System.out.println(String.format("BIG JUMP:   %.1f, %.1f", dx, dy));
        			Selection sele = getController().getSelectionManager();
                	if (dx != 0 || dy != 0)
                	{
                		sele.translate(dx,dy, vNode);
                    	sele.extract();
//                    		System.out.println(String.format("%.1f, %.1f", dx, dy));
                	}
            	}
            	prevX = ex;
            	prevY = ey;
            	if (vNode.modelNode() instanceof DataNodeGroup)
            		((DataNodeGroup)vNode.modelNode()).moveMembers(dx, dy);
            	
        		int groupId = vNode.modelNode().getInteger("GroupRef");
            	DataNodeGroup gp = vNode.modelNode().getGroup();
        		Map<Integer, DataNodeGroup> map = vNode.modelNode().getModel().getGroupMap();
        		gp = map.get(groupId);
        		if (gp != null)
            		gp.calcBounds();
            }
            event.consume();

        }
    };
    

    //-------------------------------------------------------------------------------
 
	protected void handleMouseReleased(final MouseEvent event) {
		if (canvas.getDragLine() != null && vNode.modelNode().isConnectable()) {
			VNode starter = canvas.getDragSource();
			if (starter != vNode)
			{	
				getController().getUndoStack().push(ActionType.AddEdge);
				getController().addInteraction(starter, vNode);
				canvas.removeDragLine();
			}
			event.consume();
			return;
		}
		getController().getUndoStack().push(ActionType.Move);
		canvas.getSelectionMgr().extract();
		vNode.stopResizing();
	    prevX = prevY = -1; 
    }

	private Controller getController() {
		return vNode.modelNode().getModel().getController();
	}

	// **-------------------------------------------------------------------------------
	protected void doContextMenu(MouseEvent event, VNode vNode) {
		ContextMenu menu = new ContextMenu();
		menu.getItems().addAll(getMenuItems(event, vNode));
		menu.show(vNode.getPasteboard(), event.getScreenX(), event.getScreenY());	
		event.consume();
	}

	public static List<MenuItem> getMenuItems(MouseEvent event, VNode vNode) {
		ObservableList<MenuItem> list = FXCollections.observableArrayList();
		//System.out.println("ContextMenu");
		int nKids = vNode.getChildren().size();
		Controller controller = vNode.getPasteboard().getController();
		if (nKids > 0)
		{
			Node content = vNode.getChildren().get(0);
			if (content instanceof TableView)
			{
				MenuItem scatter = makeItem("Make Scatter Chart", e -> {});
				MenuItem timeseries = makeItem("Make Time Series", e -> {});
				SeparatorMenuItem sep = new SeparatorMenuItem();
				list.addAll(new MenuItem[] {scatter, timeseries, sep});
			}
		}
		MenuItem toFront = 	makeItem("Bring To Front", e -> {   controller.toFront();   });
		MenuItem toBack = 	makeItem("Send To Back", e -> {   	controller.toBack();   });
		Menu toLayer = 		makeLayerMenu(controller);
		MenuItem dup = 		makeItem("Duplicate", a -> 	{		controller.duplicateSelection();	});
		MenuItem del = 		makeItem("Delete", a -> 	{		controller.deleteSelection();	});
		MenuItem lock = 	makeItem("Lock", a -> 		{		vNode.applyEditable(false, false, false, false); controller.lock(true);	});
		MenuItem unlock = 	makeItem("Unlock", a -> 	{		vNode.applyEditable(true, true, true, true); controller.lock(false);	});
		list.addAll(toFront, toBack, toLayer, dup, del, lock, unlock); 

		MenuItem group = 	makeItem("Group", e ->		{		controller.group();    });
		MenuItem ungroup = 	makeItem("Ungroup", a -> 	{		controller. ungroup();	});
		if (controller.getSelectionManager().isGroupable())	list.add(group);   
		if (vNode.isGroup())					list.add(ungroup);   

		return list;
	}

	private static MenuItem makeItem(String name, EventHandler<ActionEvent> foo) {
		MenuItem item = new MenuItem(name);
		item.setOnAction(foo);
		return item;
	}

	private static Menu makeLayerMenu(Controller controller) {
		Menu menu = new Menu("Move To Layer");
		for (LayerRecord layer : controller.getLayers())
		{
			MenuItem item = new MenuItem(layer.getName());
			menu.getItems().add(item);
			item.setOnAction(e -> 	{ 	controller.moveSelectionToLayer(layer.getName());  	});
		}
		return menu;
	}

}
