package diagrams.pViz.view;

import diagrams.pViz.app.Controller;
import diagrams.pViz.app.Selection;
import diagrams.pViz.app.Tool;
import diagrams.pViz.model.nodes.DataNodeGroup;
import gui.Action.ActionType;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.input.MouseEvent;

/**
 * Listeners for making the nodes draggable via left mouse button. Considers if parent is zoomed.
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

    //-------------------------------------------------------------------------------
    private EventHandler<MouseEvent> mousePressedHandler = new EventHandler<MouseEvent>() {

        public void handle(MouseEvent event) {

            // left mouse button => dragging
            if( event.isSecondaryButtonDown())
            {
            	vNode.doContextMenu(event);
            	return;
            }

            nodeDragContext.mouseAnchorX = event.getSceneX();
            nodeDragContext.mouseAnchorY = event.getSceneY();

            Node node = (Node) event.getSource();

            nodeDragContext.translateAnchorX = node.getTranslateX();
            nodeDragContext.translateAnchorY = node.getTranslateY();
    		if (event.getClickCount() > 1)	   {	vNode.getInfo(); 	event.consume();   return; }
    		if (event.isPopupTrigger())	   {	vNode.doContextMenu(event); return;	   }
    		Tool curTool = canvas.getTool();
    		vNode.finishDragLine(event);
    	   if (curTool != null && !curTool.isArrow()) return;
    	   
    	   Selection selection = getController().getSelectionManager();
    	   if (event.isAltDown())
    		   selection.duplicateSelection();
    	   
    	   prevMouseX = event.getSceneX();
    	   prevMouseY = event.getSceneY();
    	   boolean inCorner = vNode.ptInCorner(prevMouseX, prevMouseY);
    	   if (inCorner)
    		   vNode.handleResize(event);
    	   else
    	   {
    		boolean wasSelected = selection.isSelected(vNode);
    		if (event.isControlDown() || event.isShiftDown())			//TODO -- configurable?
    			selection.select(vNode, !wasSelected);
    		else if (!wasSelected)
    			selection.selectX(vNode);
    	   }
    	   event.consume();
        }
    };

    private EventHandler<MouseEvent> mouseDraggedHandler = new EventHandler<MouseEvent>() {
        public void handle(MouseEvent event) {

            // left mouse button => dragging
//    		if (!vNode.isMovable()) return;
            if( !event.isPrimaryButtonDown())
                return;

            double scale = canvas.getScale();

            Node node = (Node) event.getSource();
            node.setTranslateX(nodeDragContext.translateAnchorX + (( event.getSceneX() - nodeDragContext.mouseAnchorX) / scale));
            node.setTranslateY(nodeDragContext.translateAnchorY + (( event.getSceneY() - nodeDragContext.mouseAnchorY) / scale));

        	double ex = event.getX();
    		double ey = event.getY();
    				
        	if (event.isShiftDown())
        	{
        		// TODO  if there are edges connected to this node, constrain the coordinates to the other sides
        		ex = ey;
        	}
//    		super.handleMouseDragged(event);
//    		 vNode.handleResize(ex, ey);
    		 if (vNode.isSelected())
            {	
               	double dx = 0;
            	double dy = 0;
            	if (localLastMouseX > 0 && localLastMouseY > 0)
            	{
            		dx = localLastMouseX - ex;
            		dy = localLastMouseY - ey;
            	}
            	
        		Selection sele = getController().getSelectionManager();
        		sele.translate(dx,dy, vNode);
            	sele.extract();
            	localLastMouseX = ex;
            	localLastMouseY = ey;
            	if (vNode.modelNode() instanceof DataNodeGroup)
            		((DataNodeGroup)vNode.modelNode()).moveMembers(dx, dy);
            	
            	if (vNode.isInCompoundNode())
            	{
            		System.out.println("compund node recalc");
            		vNode.getGroup().calcBounds();
            	}
            }
            event.consume();

        }
    };
    
//protected void handleMouseDragged(final MouseEvent event) {
//		
//		}


    double localLastMouseX = -1; 
	double localLastMouseY = -1; 
	

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
    }

	private Controller getController() {
		return vNode.modelNode().getModel().getController();
	}

	double prevMouseX, prevMouseY;
	
	// **------------------------------------------------------------------------------
}
