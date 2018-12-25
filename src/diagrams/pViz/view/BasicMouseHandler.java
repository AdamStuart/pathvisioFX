package diagrams.pViz.view;

import diagrams.pViz.app.Controller;
import gui.Action.ActionType;
import gui.UndoStack;
import javafx.event.EventHandler;
import javafx.event.EventType;
import javafx.geometry.Point2D;
import javafx.scene.control.ContextMenu;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import util.RectangleUtil;

public class BasicMouseHandler implements EventHandler<MouseEvent> {
	protected boolean resizing = false;
	protected boolean dragging = false;
	protected Point2D startPoint, currentPoint, local, prevPoint;
	protected VNode target;
	protected EventType<? extends MouseEvent> type;
	
	protected final Pasteboard pasteboard;
	protected final Controller controller;
	protected final UndoStack undoStack;
	
	public BasicMouseHandler(Pasteboard board)
	{
		pasteboard = board;
		controller = board.getController();
		undoStack = controller.getUndoStack();
	}
	
	@Override
	public void handle(MouseEvent event) {
		if (!(event.getTarget() instanceof VNode))	return;
		target = (VNode) event.getTarget();
		type = (EventType<? extends MouseEvent>) event.getEventType();
		currentPoint = new Point2D(event.getX(), event.getY());
		if (type == MouseEvent.MOUSE_PRESSED)			doMousePressed(event);	
		else if (type == MouseEvent.MOUSE_MOVED)		doMouseMoved(event);	
		else if (type == MouseEvent.MOUSE_RELEASED)		doMouseReleased(event);	
		else if (type ==  MouseEvent.MOUSE_CLICKED)		doMouseClicked(event);	
		else if (type ==  MouseEvent.MOUSE_DRAGGED)		doMouseDragged(event);	
//		else if (type ==  DragEvent.DRAG_DROPPED)		doDragReleased(event);	
	}
	// **-------------------------------------------------------------------------------
	protected void doMouseClicked(final MouseEvent event)
	{
	}
	
	protected void doMouseDragged(final MouseEvent event)
	{
		if (verbose>3)	
			System.out.println("NodeMouseDraggedHandler, Target: " + event.getTarget());
		// do nothing for a right-click
		if (event.isSecondaryButtonDown())			return;
		if (target instanceof StackPane)
		{
			if (resizing)
			{
				VNode r = (VNode) event.getTarget();
				Point2D local = r.localToParent(currentPoint);
				RectangleUtil.setRect(r, startPoint, local);
						
			}
			else if (dragging)
			{
				VNode r = (VNode) event.getTarget();
				Point2D local = r.localToParent(currentPoint);
				double dx = prevPoint.getX() - local.getX();
				double dy = prevPoint.getY() - local.getY();
			
//				System.out.println("Delta: " + dx + ", " + dy);
				pasteboard.getSelectionMgr().translate(dx, dy);
				prevPoint = local;
			}
			event.consume();
		}
	}
	//**-------------------------------------------------------------------------------
	//ABSTRACT HANDLERS
	protected void doMouseMoved(final MouseEvent event)
	{	
	}
		// **-------------------------------------------------------------------------------
	int verbose = 5;
	
	protected void doMousePressed(final MouseEvent event)
	{				
		if (verbose>3)	
				System.out.println("NodeMousePressedHandler, Target: " + event.getTarget());
			
		resizing = false;
		VNode node = (VNode) target;
		local = node.localToParent(currentPoint);
		boolean rightClick = event.isSecondaryButtonDown();

		prevPoint = local;
		boolean altDown = event.isAltDown();
		if (altDown)
			controller.getSelectionManager().cloneSelection(5);
		if (event.isPopupTrigger() || rightClick)	
		{
			doContextMenu(event);
            return;
		}

		boolean wasSelected = pasteboard.getSelectionMgr().isSelected(node);
		if (event.isControlDown())
			pasteboard.getSelectionMgr().select(node, !wasSelected);
		else if (!wasSelected)
			pasteboard.getSelectionMgr().selectX(node);
					
		if (RectangleUtil.inCorner(event))
		{
			resizing = true;
			undoStack.push(ActionType.Resize);
			startPoint = node.localToParent(RectangleUtil.oppositeCorner(event));
		}
		else if (pasteboard.getSelectionMgr().count() > 0)
		{
			dragging = true;
			undoStack.push(ActionType.Move);
			startPoint = local;
		}
		event.consume();
	}

	protected void doContextMenu(MouseEvent event) {
		ContextMenu menu = new ContextMenu();
		menu.getItems().addAll(VNodeGestures.getMenuItems(event, target));
		menu.show(pasteboard, event.getScreenX(), event.getScreenY());		
		event.consume();
	}

	// **-------------------------------------------------------------------------------
    
	public void doMouseReleased(final MouseEvent event)
	{
		startPoint = null;
		resizing = dragging = false;
		pasteboard.requestFocus();	 // needed for the key event handler to receive events
		pasteboard.getSelectionMgr().extract();
		event.consume();
	}
}
