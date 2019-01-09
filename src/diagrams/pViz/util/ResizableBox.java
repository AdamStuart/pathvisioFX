/*
 * Copyright (C) 2005 - 2014 by TESIS DYNAware GmbH
 */
package diagrams.pViz.util;

import java.util.ArrayList;
import java.util.List;

import diagrams.pViz.app.Tool;
import diagrams.pViz.view.DragContext;
import diagrams.pViz.view.Pasteboard;
import diagrams.pViz.view.ShapeFactory;
import diagrams.pViz.view.VNode;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.effect.InnerShadow;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;
import model.stat.RelPosition;

/**
 * A draggable, resizable box that can display children.
 *
 * <p>
 * See {@link DraggableBox} for more information.
 * </p>
 */
abstract public class ResizableBox extends DraggableBox {

    private static final int DEFAULT_RESIZE_BORDER_TOLERANCE = 18;

    private final BooleanProperty resizeEnabledNorthProperty = new SimpleBooleanProperty(true);
    private final BooleanProperty resizeEnabledSouthProperty = new SimpleBooleanProperty(true);
    private final BooleanProperty resizeEnabledEastProperty = new SimpleBooleanProperty(true);
    private final BooleanProperty resizeEnabledWestProperty = new SimpleBooleanProperty(true);

    private int resizeBorderTolerance = DEFAULT_RESIZE_BORDER_TOLERANCE;

    private double lastWidth;
    private double lastHeight;

    private RectangleMouseRegion lastMouseRegion;

    protected boolean mouseInPositionForResize;

    /**
     * Creates an empty resizable box.
     */
    public ResizableBox(Pasteboard p) {
    	super(p);
        addEventHandler(MouseEvent.MOUSE_ENTERED, this::processMousePosition);
        addEventHandler(MouseEvent.MOUSE_MOVED, this::processMousePosition);
        addEventHandler(MouseEvent.MOUSE_EXITED, event -> {
        	processMousePosition(event);
            if (!event.isPrimaryButtonDown()) setCursor(null);
        });
    }

    /**
     * Gets whether or not the box is resizable in the north (top) direction.
     *
     * @return {@code true} if the box is resizable in the north direction, {@code false} if not
     */
    public boolean isResizeEnabledNorth() {
        return resizeEnabledNorthProperty.get();
    }

    /**
     * Sets whether the box is resizable in the north (top) direction.
     *
     * @param resizeEnabledNorth {@code true} if the box is resizable in the north direction, {@code false} if not
     */
    public void setResizeEnabledNorth(final boolean resizeEnabledNorth) {
        resizeEnabledNorthProperty.set(resizeEnabledNorth);
    }

    /**
     * Gets whether or not the box is resizable in the south (bottom) direction.
     *
     * @return {@code true} if the box is resizable in the south direction, {@code false} if not
     */
    public boolean isResizeEnabledSouth() {
        return resizeEnabledSouthProperty.get();
    }

    /**
     * Sets whether the box is resizable in the south (bottom) direction.
     *
     * @param resizeEnabledSouth {@code true} if the box is resizable in the south direction, {@code false} if not
     */
    public void setResizeEnabledSouth(final boolean resizeEnabledSouth) {
        resizeEnabledSouthProperty.set(resizeEnabledSouth);
    }

    /**
     * Gets whether or not the box is resizable in the east (right) direction.
     *
     * @return {@code true} if the box is resizable in the east direction, {@code false} if not
     */
    public boolean isResizeEnabledEast() {
        return resizeEnabledEastProperty.get();
    }

    /**
     * Sets whether the box is resizable in the east (top) direction.
     *
     * @param resizeEnabledEast {@code true} if the box is resizable in the east direction, {@code false} if not
     */
    public void setResizeEnabledEast(final boolean resizeEnabledEast) {
        resizeEnabledEastProperty.set(resizeEnabledEast);
    }

    /**
     * Gets whether or not the box is resizable in the west (left) direction.
     *
     * @return {@code true} if the box is resizable in the east direction, {@code false} if not
     */
    public boolean isResizeEnabledWest() {
        return resizeEnabledWestProperty.get();
    }

    /**
     * Sets whether the node is resizable in the west (left) direction.
     *
     * @param resizeEnabledWest {@code true} if the node is resizable in the west direction, {@code false} if not
     */
    public void setResizeEnabledWest(final boolean resizeEnabledWest) {
        resizeEnabledWestProperty.set(resizeEnabledWest);
    }

    /**
     * Gets the border tolerance for the purposes of resizing.
     *
     * <p>
     * Drag events that take place within this distance of the rectangle border will be intepreted as resize events.
     * Further inside the rectangle, they will be treated as regular drag events.
     * </p>
     *
     * @return an integer specifying the resize border tolerance
     */
    public int getResizeBorderTolerance() {
        return resizeBorderTolerance;
    }

    /**
     * Sets the border tolerance for the purposes of resizing.
     *
     * <p>
     * Drag events that take place within this distance of the rectangle border will be intepreted as resize events.
     * Further inside the rectangle, they will be treated as regular drag events.
     * </p>
     *
     * @param resizeBorderTolerance an integer specifying the resize border tolerance
     */
    public void setResizeBorderTolerance(final int resizeBorderTolerance) {
        this.resizeBorderTolerance = resizeBorderTolerance;
    }

    /**
     * Gets whether or not the current mouse position would lead to a resize operation.
     *
     * @return {@code true} if the mouse is near the edge of the rectangle so that a resize would occur
     */
    public boolean isMouseInPositionForResize() {
        return mouseInPositionForResize;
    }

//    protected void getInfo() {		System.out.println("getInfo");}  //TODO  abstract
//	protected void doContextMenu(final MouseEvent event) {		System.out.println("doContextMenu");}  //TODO  abstract
	
//    DragContext nodeDragContext = new DragContext();
//	@Override
//    protected void handleMousePressed(final MouseEvent event) {
//
//        if (event.getClickCount() > 1)	   {	getInfo(); 	event.consume();   return; }
//        super.handleMousePressed(event);
//
//        if (!(getParent() instanceof Region)) {
//            return;
//        } else if (!event.getButton().equals(MouseButton.PRIMARY)) {
//            setCursor(null);
//            return;
//        }
//
//        storeClickValuesForResize(event.getX(), event.getY());
//
//        nodeDragContext.mouseAnchorX = event.getSceneX();
//        nodeDragContext.mouseAnchorY = event.getSceneY();
//
//        Node node = (Node) event.getSource();
//
//        nodeDragContext.translateAnchorX = node.getTranslateX();
//        nodeDragContext.translateAnchorY = node.getTranslateY();
//	   }
//
//    @Override
//    protected void handleMouseDragged(final MouseEvent event) {
//
////        if (!(getParent() instanceof Region)) {
////            return;
////        } else 
//        	if (!event.getButton().equals(MouseButton.PRIMARY)) {
//            setCursor(null);
//            return;
//        }
//        Point2D evPt = new Point2D(event.getX(), event.getY());
//        Point2D local =  sceneToLocal(evPt);
//        	storeClickValuesForResize(event.getX(), event.getY());
//        if (!dragActive) {
//            storeClickValuesForDrag(event.getX(), event.getY());
//            storeClickValuesForResize(event.getX(), event.getY());
//        }
//
//        if (RectangleMouseRegion.INSIDE.equals(lastMouseRegion)) 
//        {
//        	if (isMovable())	
//            	super.handleMouseDragged(event);
//        	
//        }
//       else if (!RectangleMouseRegion.OUTSIDE.equals(lastMouseRegion)) 
//            if (canResize())	
//            	handleResize(event.getSceneX(), event.getSceneY());
//
//        dragActive = true;
//        event.consume();
//    }
	public void addPortHandlers(Shape port)
	{		
		port.addEventHandler(MouseEvent.MOUSE_MOVED, e ->  { 	if (pasteboard.getDragLine() != null) return; 	port.setFill(portFillColor( EState.ACTIVE)); });
		port.addEventHandler(MouseEvent.MOUSE_EXITED, e -> {	port.setFill(portFillColor( EState.OFF)); } );
		port.addEventHandler(MouseEvent.MOUSE_CLICKED, e -> {  	port.setFill(portFillColor( EState.STANDBY));  	finishDragLine(port, this); });
		port.addEventHandler(MouseEvent.MOUSE_PRESSED, e -> { 	startPortDrag(e, (VNode)this, port);} );
		port.addEventHandler(MouseEvent.MOUSE_DRAGGED, e -> { 	portDrag(e, (VNode)this, port);} );
	}

	public void startPortDrag(MouseEvent e, VNode node, Shape port) 
	{
		if (pasteboard.getDragLine()!= null)  // finish an ongoing drag
		{
			RelPosition pos = port == null ? RelPosition.ZERO : RelPosition.idToRelPosition(port.getId());
			pasteboard.connectTo(node,pos);
			port.setFill(portFillColor(EState.FILLED)); 
			e.consume();
			return;
		}
		
		if (pasteboard.getTool() == Tool.Polyline)
		{
			port.setFill(Color.AQUAMARINE); 
			String id = port.getId();
			Pos pos = RelPosition.idToPosition(id);
			pasteboard.startDragLine(node, pos, node, pos, node.getLayoutX(), node.getLayoutY());
		}
		else if (pasteboard.getTool().isArrow())
			handleResize(e);
		
		e.consume();
	}
	
	
	abstract public void finishDragLine(Node port, ResizableBox target);
	abstract public void finishDragLine(MouseEvent event);	
	
	
	public void portDrag(MouseEvent e, VNode node, Shape port) 
	{
		if (pasteboard.getTool().isArrow())
			handleResize(e);
		e.consume();
	}
	private boolean resizing = false;
	public boolean isResizing()	{	return resizing;	}
	public void stopResizing()	{	resizing = false;	}
	public void handleResize(MouseEvent event)
	{
		if (canResize())
		{
			resizing = true;
		    lastWidth = getWidth();
		    lastHeight = getHeight();
		    lastMouseRegion = getMouseRegion(event.getX(), event.getY());
			handleResize(event.getX(), event.getY());
			ShapeFactory.resizeFigureToNode((VNode)this);
		}
	}

	public void setResize(boolean enable)
	{
		setResizeEnabledNorth(enable);
		setResizeEnabledEast(enable);
		setResizeEnabledWest(enable);
		setResizeEnabledSouth(enable);
//		resizable = enable;	
	}
	private List<Shape> ports = new ArrayList<Shape>();

  	public void addPorts()
	{
		for (int i=0; i< 9 ; i++)
		{
			if (i == 4) continue;			//skip center
			Pos pos = Pos.values()[i];
			Shape port = null;
			if (i % 2 == 0) 
				port = new Rectangle(5,5);
			else  port = new Circle(5);
			port.setFill(portFillColor(EState.STANDBY));
			port.setStroke(Color.MEDIUMAQUAMARINE);
			addPortHandlers(port);
			setAlignment(port, pos);
			getChildren().add(port);
			ports.add(port);
			port.setVisible(false);
			port.setId(""+(i+1));
		}
	}
	
	public void showPorts(boolean vis)
	{
		if (getWidth() < 40 || getHeight() < 18)
			vis = false;
		for (Shape p : ports)
			p.setVisible(vis);
	}
	
	public boolean ptInCorner(double x, double y) {
		
		boolean xOnEdge = near(x, getLayoutX()) || near(x, getLayoutX() + getWidth());
		boolean yOnEdge = near(y, getLayoutY()) || near(y, getLayoutY() + getHeight());
		return xOnEdge && yOnEdge;
	}

	private boolean near(double a, double b)	{		return Math.abs(a-b) < 10;   }


	// **-------------------------------------------------------------------------------
	  /**
     * Handles mouse events.
     * @param event {@link MouseEvent}
     */
	enum EState { OFF, ACTIVE, STANDBY, FILLED, MISMATCH }; 
	
	public Color portFillColor(EState state)
	{
		if (state == EState.OFF)		return  Color.WHITE;
		if (state == EState.STANDBY)	return Color.YELLOW;
		if (state == EState.FILLED)		return Color.BLACK;
		if (state == EState.MISMATCH)	return  Color.RED;
		return Color.GREEN;
	}
//
//    @Override
//    protected void handleMouseReleased(final MouseEvent event) {
//
//        super.handleMouseReleased(event);
//        if (event.getButton().equals(MouseButton.PRIMARY)) 
//            processMousePosition(event);
//    }

    /**
     * Processes the current mouse position, updating the cursor accordingly.
     *
     * @param event the latest {@link MouseEvent} for the mouse entering or moving inside the rectangle
     */
	static InnerShadow effect = new InnerShadow();
	
	   protected void processMousePosition(final MouseEvent event) {

	    if (event.isPrimaryButtonDown())            return;
	    final RectangleMouseRegion mouseRegion = getMouseRegion(event.getX(), event.getY());
//	    System.out.println("" + mouseRegion);
	    mouseInPositionForResize = !mouseRegion.equals(RectangleMouseRegion.INSIDE);
	    updateCursor(mouseRegion);
	}
     /**
     * Handles a resize event to the given cursor position.
     *
     * @param x the cursor scene-x position
     * @param y the cursor scene-y position
     */
    public void handleResize(final double x, final double y) {

    	if (!canResize()) return;
    	if (lastMouseRegion == null )return;
//        System.out.println(lastMouseRegion);
    	switch (lastMouseRegion) 
    	{
	        case NORTHEAST:    handleResizeNorth(y);        handleResizeEast(x);    break;
	        case NORTHWEST:    handleResizeNorth(y);        handleResizeWest(x);    break;
	        case SOUTHEAST:    handleResizeSouth(y);        handleResizeEast(x);    break;
	        case SOUTHWEST:    handleResizeSouth(y);        handleResizeWest(x);    break;
	        case NORTH:        handleResizeNorth(y);       	break;
	        case SOUTH:        handleResizeSouth(y);      	break;
	        case EAST:         handleResizeEast(x);         break;
	        case WEST:         handleResizeWest(x);         break;
	        case INSIDE:
	        case OUTSIDE:							        break;
        }
    }

    /**
     * Handles a resize event in the north (top) direction to the given cursor y position.
     *
     * @param y the cursor scene-y position
     */
    private void handleResizeNorth(final double localY) {

        final double scaleFactor = getLocalToSceneTransform().getMyy();
        double sceneY = localY + getLayoutY();
        final double yDragDistance = (sceneY - lastMouseY) / scaleFactor;
        final double minResizeHeight = Math.max(getMinHeight(), 0);

        double newLayoutY = lastLayoutY + yDragDistance;
        double newHeight = lastHeight - yDragDistance;
        GraphEditorProperties editorProperties =  pasteboard.getEditorProperties();
        // Snap-to-grid logic here.
        if (editorProperties.isSnapToGridOn()) {

            // The -1 here is to put the rectangle border exactly on top of a grid line.
            final double roundedLayoutY = roundToGridSpacing(newLayoutY) - 1;
            newHeight = newHeight - roundedLayoutY + newLayoutY;
            newLayoutY = roundedLayoutY;
        } else {

            // Even if snap-to-grid is off, we use Math.round to ensure drawing 'on-pixel' when zoomed in past 100%.
            final double roundedLayoutY = Math.round(newLayoutY);
            newHeight = Math.round(newHeight - roundedLayoutY + newLayoutY);
            newLayoutY = roundedLayoutY;
        }

        // Min & max resize logic here.
        if (editorProperties.isNorthBoundActive() && newLayoutY < editorProperties.getNorthBoundValue()) {
            newLayoutY = editorProperties.getNorthBoundValue();
            newHeight = lastLayoutY + lastHeight - editorProperties.getNorthBoundValue();
        } else if (newHeight < minResizeHeight) {
            newLayoutY = lastLayoutY + lastHeight - minResizeHeight;
            newHeight = minResizeHeight;
        }

        setLayoutY(newLayoutY);
        setHeight(newHeight);
//        System.out.println(String.format("%.2f, %.2f",  newLayoutY, newHeight));
    }

    /**
     * Handles a resize event in the south (bottom) direction to the given cursor y position.
     *
     * @param y the cursor scene-y position
     */
    private void handleResizeSouth(final double y) {

        GraphEditorProperties editorProperties =  pasteboard.getEditorProperties();
      final double scaleFactor = getLocalToSceneTransform().getMyy();

        final double yDragDistance = (y - lastMouseY) / scaleFactor;
        final double parentHeight = getParent().getLayoutBounds().getHeight();

        final double maxParentHeight = editorProperties.isSouthBoundActive() ? parentHeight : absoluteMaxHeight;

        final double minResizeHeight = Math.max(getMinHeight(), 0);
        final double maxAvailableHeight = maxParentHeight - getLayoutY() - editorProperties.getSouthBoundValue();

        double newHeight = lastHeight + yDragDistance;

        // Snap-to-grid logic here.
        if (editorProperties.isSnapToGridOn()) {
            newHeight = roundToGridSpacing(newHeight + lastLayoutY) - lastLayoutY;
        } else {
            // Even if snap-to-grid is off, we use Math.round to ensure drawing 'on-pixel' when zoomed in past 100%.
            newHeight = Math.round(newHeight);
        }

        // Min & max resize logic here.
        if (newHeight > maxAvailableHeight) {
            newHeight = maxAvailableHeight;
        } else if (newHeight < minResizeHeight) {
            newHeight = minResizeHeight;
        }

        setHeight(newHeight);
    }

    /**
     * Handles a resize event in the east (right) direction to the given cursor x position.
     *
     * @param x the cursor scene-x position
     */
    private void handleResizeEast(final double x) {

        GraphEditorProperties editorProperties =  pasteboard.getEditorProperties();
       final double scaleFactor = getLocalToSceneTransform().getMxx();

        final double xDragDistance = (x - lastMouseX) / scaleFactor;
        final double parentWidth = getParent().getLayoutBounds().getWidth();

        final double maxParentWidth = editorProperties.isEastBoundActive() ? parentWidth : absoluteMaxWidth;

        final double minResizeWidth = Math.max(getMinWidth(), 0);
        final double maxAvailableWidth = maxParentWidth - getLayoutX() - editorProperties.getEastBoundValue();

        double newWidth = lastWidth + xDragDistance;

        // Snap-to-grid logic here.
        if (editorProperties.isSnapToGridOn()) {
            newWidth = roundToGridSpacing(newWidth + lastLayoutX) - lastLayoutX;
        } else {
            // Even if snap-to-grid is off, we use Math.round to ensure drawing 'on-pixel' when zoomed in past 100%.
            newWidth = Math.round(newWidth);
        }

        // Min & max resize logic here.
        if (newWidth > maxAvailableWidth)             newWidth = maxAvailableWidth;
        else if (newWidth < minResizeWidth)           newWidth = minResizeWidth;
  System.out.println(" setWidth from " + lastWidth + " --> " + newWidth);     
  setWidth(newWidth);
    }

    /**
     * Handles a resize event in the west (left) direction to the given cursor x position.
     *
     * @param x the cursor scene-x position
     */
    private void handleResizeWest(final double x) {

        GraphEditorProperties editorProperties =  pasteboard.getEditorProperties();
       final double scaleFactor = getLocalToSceneTransform().getMxx();

        final double xDragDistance = (x - lastMouseX) / scaleFactor;
        final double minResizeWidth = Math.max(getMinWidth(), 0);

        double newLayoutX = lastLayoutX + xDragDistance;
        double newWidth = lastWidth - xDragDistance;

        // Snap-to-grid logic here.
        if (editorProperties.isSnapToGridOn()) {

            // The -1 here is to put the rectangle border exactly on top of a grid line.
            final double roundedLayoutX = roundToGridSpacing(newLayoutX) - 1;
            newWidth = newWidth - roundedLayoutX + newLayoutX;
            newLayoutX = roundedLayoutX;
        } else {

            // Even if snap-to-grid is off, we use Math.round to ensure drawing 'on-pixel' when zoomed in past 100%.
            final double roundedLayoutX = Math.round(newLayoutX);
            newWidth = Math.round(newWidth - roundedLayoutX + newLayoutX);
            newLayoutX = roundedLayoutX;
        }

        // Min & max resize logic here.
        if (editorProperties.isWestBoundActive() && newLayoutX < editorProperties.getWestBoundValue()) {
            newLayoutX = editorProperties.getWestBoundValue();
            newWidth = lastLayoutX + lastWidth - editorProperties.getWestBoundValue();
        } else if (newWidth < minResizeWidth) {
            newLayoutX = lastLayoutX + lastWidth - minResizeWidth;
            newWidth = minResizeWidth;
        }

        setLayoutX(newLayoutX);
        setWidth(newWidth);
    }

    /**
     * Gets the particular sub-region of the rectangle that the given cursor position is in.
     *
     * @param x the x cursor position
     * @param y the y cursor position
     *
     * @return the {@link RectangleMouseRegion} that the cursor is located in
     */
    protected RectangleMouseRegion getMouseRegion(final double x, final double y) {

        final double width = getWidth();
        final double height = getHeight();
//        System.out.println(String.format("%.1f, %.1f", x, y));
        if (x < -10 || y < -10)				return RectangleMouseRegion.OUTSIDE;
        if (x > width + 20 || y > height + 20) 	
        	return RectangleMouseRegion.OUTSIDE;
 
        final boolean isNorth = y < resizeBorderTolerance;
        final boolean isSouth = y > height - resizeBorderTolerance;
        final boolean isEast = x > width - resizeBorderTolerance;
        final boolean isWest = x < resizeBorderTolerance;

        if (isNorth && isEast)  return RectangleMouseRegion.NORTHEAST;
        if (isNorth && isWest)  return RectangleMouseRegion.NORTHWEST;
        if (isSouth && isEast)  return RectangleMouseRegion.SOUTHEAST;
        if (isSouth && isWest)  return RectangleMouseRegion.SOUTHWEST;
        if (isNorth)           	return RectangleMouseRegion.NORTH;
        if (isSouth)            return RectangleMouseRegion.SOUTH;
        if (isEast)            	return RectangleMouseRegion.EAST;
        if (isWest)             return RectangleMouseRegion.WEST;
        return RectangleMouseRegion.INSIDE;
    }

    /**
     * Updates the cursor style.
     *
     * <p>
     * This should occur for example when the cursor is near the border of the rectangle, to indicate that resizing is
     * allowed.
     * </p>
     *
     * @param mouseRegion the {@link RectangleMouseRegion} where the cursor is located
     */
    protected boolean canResize() { return true;	}
    protected void updateCursor(final RectangleMouseRegion mouseRegion) {

    	if (!canResize())  return;
        switch (mouseRegion) {

        case NORTHEAST:     setCursor(Cursor.NE_RESIZE);        break;
        case NORTHWEST:     setCursor(Cursor.NW_RESIZE);        break;
        case SOUTHEAST:     setCursor(Cursor.SE_RESIZE);        break;
        case SOUTHWEST:     setCursor(Cursor.SW_RESIZE);        break;
        case NORTH:         setCursor(Cursor.N_RESIZE);         break;
        case SOUTH:         setCursor(Cursor.S_RESIZE);         break;
        case EAST:          setCursor(Cursor.E_RESIZE);         break;
        case WEST:          setCursor(Cursor.W_RESIZE);         break;
        case INSIDE:        // Set to null instead of Cursor.DEFAULT so it doesn't overwrite cursor settings of parent.
        case OUTSIDE: 		setCursor(null);     				break;
        }
    }

    /**
     * The set of possible regions around the border of a rectangle.
     *
     * <p>
     * Used during mouse hover and drag events on a {@link ResizableBox}.
     * </p>
     */
    protected enum RectangleMouseRegion {
        NORTH, NORTHEAST, EAST, SOUTHEAST, SOUTH, SOUTHWEST, WEST, NORTHWEST, INSIDE, OUTSIDE;
    }

	// **-------------------------------------------------------------------------------

//	public double getCenterX() {		return modelNode().getDouble("CenterX");	}
//	public double getCenterY() {		return modelNode().getDouble("CenterY");	}
//	public void setCenter(double x, double y)
//	{
//		getAttributes().putDouble("CenterX", x);
//		getAttributes().putDouble("CenterY", y);
//		installPosition();
//	}
	public Point2D center() {
    	Bounds bounds = getBoundsInParent();
    	double x = (bounds.getMinX() + bounds.getWidth()  / 2);
    	double y = (bounds.getMinY() + bounds.getHeight() / 2);
    	return new Point2D(x,y);
	}

	public void setCenter(Point2D pt)	{		setCenter(pt.getX(), pt.getY());	}
	public void setCenter(double x, double y)	
	{
		setLayoutX(x - getWidth()/2);	
		setLayoutY(y - getHeight()/2);	
    }

	public Point2D getPortPosition(Pos pos) {
		Point2D center = center();
		double x = getAdjustmentX(pos, getWidth());
		double y = getAdjustmentY(pos, getHeight());
		return new Point2D(center.getX()+x, center.getY()+y);
	}

	public Point2D getRelativePosition(double relX, double relY) {
		Point2D center = center();
		double dx = relX * getWidth() / 2;
		double dy = relY * getHeight() / 2;
		return new Point2D(center.getX()+dx, center.getY()+dy);
	}

	public Point2D getRelativePosition(RelPosition rel) {
		return getRelativePosition(rel.x(), rel.y());
	}

	private double getAdjustmentX(Pos srcPosition, double nodeWidth) {
		String s = srcPosition.name();
		if (s.contains("LEFT")) 	return -nodeWidth / 2;
		if (s.contains("RIGHT")) 	return nodeWidth / 2;
		return 0;
	}
	private double getAdjustmentY(Pos srcPosition, double nodeHeight) {
		String s = srcPosition.name();
		if (s.contains("TOP")) 		return -nodeHeight / 2;
		if (s.contains("BOTTOM")) 	return nodeHeight / 2;
		return 0;
	}

	public String getGraphId() {
		return "UNASSIGNED";
	} 


}
