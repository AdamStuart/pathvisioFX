package diagrams.pViz.view;

import gui.Action.ActionType;
import javafx.event.EventTarget;
import javafx.geometry.Point2D;
import javafx.scene.input.MouseEvent;

public class GroupMouseHandler extends BasicMouseHandler {

	public GroupMouseHandler(Pasteboard board) {
			super(board);
		}

	@Override protected void doMousePressed(final MouseEvent event)
	{
//		if (verbose>3)	
//			System.out.println("GroupMousePressedHandler, Target: " + event.getTarget());
		
		EventTarget target = event.getTarget();
		currentPoint = new Point2D(event.getX(), event.getY());
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
		
		dragging = true;
		undoStack.push(ActionType.Move);
		startPoint = local;
		event.consume();
	}
	// **-------------------------------------------------------------------------------
	@Override protected void doMouseDragged(final MouseEvent event)
	{
//		if (verbose>3)	
//			System.out.println("GroupMouseDraggedHandler, Target: " + event.getTarget());
		// do nothing for a right-click
		if (event.isSecondaryButtonDown())			return;
		
		double dx, dy;
		dx = prevPoint.getX() - local.getX();
		dy = prevPoint.getY() - local.getY();
		pasteboard.getSelectionMgr().translate(dx, dy);
		prevPoint = local;
	}	
}
