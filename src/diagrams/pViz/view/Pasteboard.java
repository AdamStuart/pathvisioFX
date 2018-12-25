package diagrams.pViz.view;

import java.io.File;
import java.util.List;

import diagrams.pViz.app.Controller;
import diagrams.pViz.app.Tool;
import diagrams.pViz.gpml.GPMLPoint.ArrowType;
import diagrams.pViz.model.Model;
import diagrams.pViz.model.edges.EdgeLine;
import diagrams.pViz.model.edges.EdgeType;
import diagrams.pViz.model.edges.Interaction;
import diagrams.pViz.model.nodes.DataNode;
import diagrams.pViz.tables.PathwayController;
import diagrams.pViz.util.ResizableBox;
import gui.Action.ActionType;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.Skin;
import javafx.scene.control.TextArea;
import javafx.scene.effect.InnerShadow;
import javafx.scene.input.DataFormat;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.Background;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Line;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Polyline;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;
import model.AttributeMap;
import model.bio.PathwayRecord;
import model.stat.RelPosition;
import util.FileUtil;
import util.LineUtil;
import util.RectangleUtil;
import util.StringUtil;

// Pasteboard 
/*
 *  The root Panel of the content.
 *  The primary role of the Pasteboard is to handle mouse events that don't hit any node,
 *  and drop events that will create new nodes depending on the files / data that 
 *  are in the drop.  Key events are also caught.
 *  
 *  The Pasteboard also remembers the state of which tool is active and what default
 *  attributes will be assigned to new nodes.
 *  
 *  The Pasteboard supports layers of items.  Each layer is a group which can be
 *  hidden or mouseTransparent.
 */
public class Pasteboard extends PanningCanvas
{
	//@formatter:off
	private static final String INFO_LABEL_ID = "infoLabel";
	private static final String ELEMENT_NAME = "Pasteboard";
	private SceneGestures zoomScrollGestures;
	private PaletteController palette;
	//@formatter:on
	/**-------------------------------------------------------------------------------
	/**Canvas (Pane pane, Controller ctrl
	 * @param ctrl	-- the Controller that is parent to this object
	 * @param pane
	 *            the pane on which the selection rectangle will be drawn.
	 */
	public Pasteboard(Controller ctrl, Label label) 
	{
		super(ctrl, label);
		palette = ctrl.getPalette();
		setupMouseKeyHandlers();
		setupPasteboardDrops();
//		layoutBoundsProperty().addListener(e -> { resetGrid(); } ); 
        zoomScrollGestures = new SceneGestures(this);
}
	public void bindGridProperties()
	{
		editorProperties.snapToGridProperty().bind(getController().getInspector().snapToGridProperty());
		editorProperties.gridSpacingProperty().set(25);
	
	}
	//---------------------------------------------------------------------------------------

	private Rectangle clipRect = new Rectangle();
	private void turnOnClipping()
	{
		setClip(clipRect);
		heightProperty().addListener((o, oldVal, newVal) -> { clipRect.heightProperty().set((double) newVal);    });
		widthProperty().addListener((o, oldVal, newVal) -> { clipRect.widthProperty().set((double) newVal);    });
	}
	private void setupPasteboardDrops()
	{
		setOnDragEntered(e -> 	{  	highlightPasteboard(true);					e.consume();	});
		setOnDragExited(e -> 	{	highlightPasteboard(false);					e.consume();	});
		setOnDragOver(e -> 		{	e.acceptTransferModes(TransferMode.ANY);	e.consume();  	});
		setOnDragDropped(e ->	{ 	handlePasteboardDrop(e);					e.consume();  	});
	}	

	private void push(ActionType a, String s)	{		controller.getUndoStack().push(a, s);	}
	private void highlightPasteboard(boolean isHighlighted)
	{
		InnerShadow shadow = null;
		if (isHighlighted)
		{
			shadow = new InnerShadow();
			shadow.setOffsetX(2.0);
			shadow.setColor(Color.web("#9F46AF"));
			shadow.setOffsetY(2.0);
		}
		grid.setEffect(shadow);
	}
	
	//---------------------------------------------------------------------------------------
	private void handlePasteboardDrop(DragEvent e)
	{
		Dragboard db = e.getDragboard();
		int offset = 0;
		Point2D dropPt = new Point2D(e.getX()+offset, e.getY()+offset);
		e.acceptTransferModes(TransferMode.ANY);
//		Set<DataFormat> formats = db.getContentTypes();
//		formats.forEach(a -> System.out.println("getContentTypes " + a.toString()));
		if (db.hasContent(Controller.GENE_MIME_TYPE))
		{
			String id = "" + db.getContent(DataFormat.PLAIN_TEXT);
			int end = id.indexOf(":");
			if (end > 0)
				id = id.substring(0, end);
			if (verbose) System.out.println(id);
		}
		if (db.hasContent(PathwayController.PATHWAY_MIME_TYPE))
		{
			Object o = db.getContent(PathwayController.PATHWAY_MIME_TYPE);
			// Source is the pathway browser
			if (o instanceof Integer)
			{
				String id = "" + db.getContent(DataFormat.PLAIN_TEXT);
				id = id.substring(0, id.indexOf(":"));
				if (verbose) System.out.println(id);
				List<PathwayRecord> results = PathwayController.getPathways(id);
				if (verbose) 
					for (PathwayRecord rec : results)   System.out.println(rec);
			}
			if (verbose) System.out.println(o.toString());
		}
		
		if (db.hasFiles())
		{
			List<File> files = db.getFiles();
			if (files != null)
			{
				push(ActionType.Add, " file");
				for (File f : files)
				{
					if (FileUtil.isCDT(f))				controller.open(f);				// CDT is a genelist format
					else if (FileUtil.isGPML(f))		controller.open(f);				// gpml files are parsed 
					else if (FileUtil.isCSS(f))			controller.addStylesheet(f);	// css files are added to the Scene
					else if (FileUtil.isDataFile(f))	controller.assignDataFile(f);	// data files are applied to the nodes
					else if (FileUtil.isTextFile(f))	controller.addGeneSet(f, dropPt);	
					else
					{
						StackPane stack = handleFileDrop(f, dropPt);
						if (stack != null)
							controller.addExternalNode(stack);
					}
					offset += 20;
				}
			}
		}
		else
		{
			Object content = db.getContent(DataFormat.PLAIN_TEXT);
			if (content != null)
			{
				String text = content.toString();
				if (verbose) 	System.out.println("Dropped: " + text);
				if (text.startsWith("SHAPE:"))
				{
					String shapeType = text.substring(6);
					addShapeAt(shapeType, dropPt);
					return;
				}
				String[] lines = text.split("\n");
				if (lines.length > 0)
				{
					for (String line : lines)
					{	
						if (StringUtil.isEmpty(line)) continue;
						addNodeAt(line, dropPt);
						dropPt = dropPt.add(0,40);
						
					}
				}
		}
		e.consume();
	}
	}
	public void addNodeAt(String text, Point2D pt) {
			
		double w = 80;
		double h  = 30;

		AttributeMap attrMap = new AttributeMap();
		attrMap.putDouble("X", pt.getX());
		attrMap.putDouble("Y", pt.getY());
		attrMap.put("CenterX", "" + pt.getX() + w/2);
		attrMap.put("CenterY", "" + pt.getY() + h/2);
		attrMap.putDouble("Width", w);
		attrMap.putDouble("Height", h);
		attrMap.put("Layer", "Content");
		attrMap.put("TextLabel", text);
		attrMap.putBool("Connectable", true);
		attrMap.putBool("Resizable", false);
		DataNode node = new DataNode(attrMap, getModel());
		controller.addDataNode(node);
	}

	public void addShapeAt(String text, Point2D pt) {
		
		double w = 280;
		double h  = 300;
		AttributeMap attrMap = new AttributeMap();
		attrMap.putDouble("X", pt.getX());
		attrMap.putDouble("Y", pt.getY());
		attrMap.put("CenterX", "" + pt.getX() + w/2);
		attrMap.put("CenterY", "" + pt.getY() + h/2);
		attrMap.putDouble("Width", w);
		attrMap.putDouble("Height", h);
		attrMap.put("Type", "Shape");
		attrMap.put("ShapeType", text);
		attrMap.put("Layer", "Content");
		attrMap.put("TextLabel", text);
		attrMap.putBool("Connectable", true);
		attrMap.putBool("Resizable", true);
		DataNode node = new DataNode(attrMap, getModel());
		controller.addDataNode(node);
	}
		
	// **-------------------------------------------------------------------------------
	public VNode handleFileDrop(File f, Point2D dropPt)	{		return handleFileDrop(f,dropPt.getX(), dropPt.getY());		}

	public VNode handleFileDrop(File f, double x, double y)
	{
		AttributeMap attrs = new AttributeMap(f, x, y);
		Tool tool = Tool.appropriateTool(f);
		attrs.setTool(tool == null ? "Arrow" : tool.toString());
		DataNode model = new DataNode(attrs, getModel());
		return model.getStack();
	}
	
	public Model getModel() {		return controller.getDrawModel();	}
	//-------------------------------------------------------------------------------
	public String getState()		
	{
		AttributeMap map = new AttributeMap();
		map.put("", "");
		map.put("tool", getTool().toString());
		map.putBool("infoShown", infoLabel.isVisible());
		map.put("fill", defaultFill.toString());
		map.put("stroke", defaultStroke.toString());
		map.putBool("snapToGrid", editorProperties.isSnapToGridOn());
		map.putDouble("gridSpacing", editorProperties.getGridSpacing());
		return map.makeElementString(ELEMENT_NAME);
	}
	//-------------------------------------------------------------------------------
	public void setState(String s)		
	{
		AttributeMap map = new AttributeMap(s);
		palette.setTool(Tool.fromString(map.get("tool")));
		infoLabel.setVisible(map.getBool("infoShown")); 
		defaultFill = map.getPaint("fill");
		defaultStroke = map.getPaint("stroke");
		editorProperties.setSnapToGrid(map.getBool("snapToGrid" )) ;
		editorProperties.setGridSpacing(map.getDouble("gridSpacing" )) ;
	}
	
	//-------------------------------------------------------------------------------
	public void showInfo(String s) {	infoLabel.setText(s);	infoLabel.setVisible(true);		}
	
/**-------------------------------------------------------------------------------
 *		These are the mouse handlers for the canvas layer 
 *		Nodes will intercept their own events, so this is just to create new nodes
 *		based on the current tool, and draw the marquee if the selection arrow is active
 *		
 */
	private void setupMouseKeyHandlers() {
		addEventHandler(MouseEvent.MOUSE_PRESSED, new MousePressedHandler());
		addEventHandler(MouseEvent.MOUSE_CLICKED, new MouseClickHandler());
		addEventHandler(MouseEvent.MOUSE_MOVED, new MouseMovedHandler());
		addEventHandler(MouseEvent.MOUSE_DRAGGED, new MouseDraggedHandler());
		addEventHandler(MouseEvent.MOUSE_RELEASED, new MouseReleasedHandler());
		addEventHandler(KeyEvent.KEY_RELEASED, new KeyHandler());
		setOnScroll(e -> zoomScrollGestures.getOnScrollEventHandler().handle(e));
	}

	private Point2D startPoint = null;		// remember where the mouse was pressed
	private Point2D curPoint = null;		// mouse location in current event
	private EdgeLine dragLine = null;			// a polyline edge
	private VNode dragLineSource = null;	// the node where the dragLine starts
	private Pos dragLinePosition = null;	// the port of the node where the dragLine starts
	
	public EdgeLine getDragLine() { return dragLine;	}
	public VNode getDragSource() { return dragLineSource;	}
	public Pos getDragSourcePosition () { return dragLinePosition;	}

	static boolean verbose = false;

//
	public void startDragLine(VNode source, Pos srcPosition, double x, double y) {
		EdgeType edgeType = getController().getCurrentLineBend();
		ArrowType arrow = getController().getCurrentArrowType();
		Point2D startPt = new Point2D(x,y);
		if (source != null)
			source.getPortPosition( srcPosition);
		dragLine = new EdgeLine(edgeType, startPt);
//		dragLine.setArrowType(arrow);
		dragLineSource = source;
		dragLinePosition = srcPosition;

		dragLine.setStroke(Color.AQUA);
		dragLine.setStrokeWidth(2);
		add(dragLine); 
	}
	
	public void removeDragLine() {
		if (dragLine != null)
		{
			remove(dragLine);	
			dragLine = null;
			dragLinePosition = null;
			dragLineSource = null;
		}
	}
	
	private void setDragLine(MouseEvent event) {
		if (dragLine != null)
			dragLine.setEndPoint(new Point2D(event.getX(), event.getY())); 
	}
	
	public boolean isMousePanEvent(MouseEvent e) {
		if (e.isSecondaryButtonDown() || e.isMiddleButtonDown())
			return true;
		
		if (e.isControlDown() && e.isAltDown())
			return true;
		
		return false;
	}
	//-----------------------------------------------------------------------------------------------------------
	private final class MousePressedHandler implements EventHandler<MouseEvent> 
	{
		@Override public void handle(final MouseEvent event) 
		{
			if (verbose)
				System.out.println("MousePressedHandler, activeStack: " + activeStack);
			
			if (isMousePanEvent(event))		//AM Panning is handled by the ScrollPane
				return;
			
			zoomScrollGestures.getOnMousePressedEventHandler().handle(event);	
			
			if (event.isConsumed())
				return;
			
			Tool tool = getTool();
			if (tool == Tool.Polyline)
			{
				startPolyline(event);
				return;
			}	
			if (dragLine != null)				// TODO -- tool depenedent?
				removeDragLine();
			
			Shape activeShape = getActiveShape();
			double x = event.getX(), y = event.getY();
			
			startPoint = new Point2D(x, y);
			if (activeShape instanceof Polyline)
			{
				if (verbose) System.out.println("MousePressedHandler, Polygon: " );
				Polyline p = getPolyline();
				if (event.getClickCount() > 1)							activeStack = null;
				else if (LineUtil.onVertex(startPoint, p) >= 0)			activeStack = null;
				else p.getPoints().addAll(x, y);
				event.consume();
				return;
			}
			if (activeShape instanceof Line)
			{
				if (verbose) System.out.println("MousePressedHandler, Line: " );
				activeShape.setVisible(false);
				event.consume();
				return;
			}
		
			List<VNode> prevSelection = null;
			if (event.isAltDown())
				prevSelection = getSelectionMgr().getAll();
			
			getSelectionMgr().clear();  
	
			if (!tool.isArrow())
			{
				AttributeMap map = new AttributeMap("ShapeType", getTool().name());
				map.addPoint(x, y);
				map.put("Layer",  activeLayerName()	);
				if (lockResizable(getTool().name()))
					map.put("Resizable", "false");
				if (lockConnectable(getTool().name()))
					map.put("Connectable", "false");
				DataNode mNode = new DataNode(map, getController().getModel());
				activeStack = mNode.getStack();	
				mNode.put("TextLabel", mNode.get("GraphId"));
				getController().addDataNode(mNode);
			}
			
			activeShape = activeStack == null ? null : activeStack.getFigure();
			ShapeFactory.addPoint(activeShape, x,y);
			if (activeShape == null) 
			{
				activeShape = marquee;
				RectangleUtil.setRect(marquee, x, y, 8,8);
				marquee.setVisible(true);
				if (marquee.getParent() == null)
					controller.addExternalNode(marquee);
				if (event.getClickCount() > 2)
					addComment(event);
			}
			else
			{
				push(ActionType.New, getTool().name());
				getSelectionMgr().select(activeStack);
			}
					
			event.consume();
		}

		private boolean lockResizable(String name) {
			String[] fixedSizes = {"GeneProduct", "Protein", "Metabolite"};		
			for (String s: fixedSizes)
				if (s.equals(name)) return true;
			return false;
		}

		private boolean lockConnectable(String name) {
			String[] unconnectable = {"Shape", "Label"};		
			for (String s: unconnectable)
				if (s.equals(name)) return true;
			return false;
		}

		//-----------------------------------------------------------------------------------------------------------
		private void startPolyline(MouseEvent event) {
			if (verbose) System.out.println("MousePressedHandler, Polyline: " );
			if (activeStack == null)
			{
				AttributeMap at = new AttributeMap("ShapeType:" + getTool().name());
				DataNode mNode = new DataNode(at, controller.getDrawModel());
				activeStack = mNode.getStack();
				add(activeStack);

			}
			Polyline p = getPolyline();
			if (event.getClickCount() > 1)
			{
				activeStack = null;
				removeDragLine();
			}
			else if (LineUtil.onVertex(startPoint, p) == 0)
			{
				activeStack = null;
				removeDragLine();
			}
			else p.getPoints().addAll(event.getX(), event.getY());
			startDragLine(null, Pos.CENTER, event.getX(), event.getY());
			p.setFill(null);
			event.consume();		}

		private Polyline tempPolyline;
		private Polyline getPolyline() {
			if (tempPolyline == null)
				tempPolyline = new Polyline();
			return tempPolyline;
		}
	}

	//-----------------------------------------------------------------------------------------------------------
	private final class MouseDraggedHandler implements EventHandler<MouseEvent> {
		@Override public void handle(final MouseEvent event) {

			if (isMousePanEvent(event))		//AM Panning is handled by the ScrollPane
				return;
			
			
			if (verbose)	
				System.out.println("Pasteboard.MouseDraggedHandler, activeShape: " + activeStack);

			// store current cursor position
			curPoint = new Point2D(event.getX(), event.getY());
			if (startPoint == null) 
				startPoint = curPoint;
			if (getTool() == Tool.Arrow)	
			{
				Rectangle r = RectangleUtil.union(startPoint, curPoint);
				getSelectionMgr().clear();
				getSelectionMgr().select(r);
				RectangleUtil.setRect(marquee, r);
			}
			else if ((activeStack != null) && activeStack.isResizable()) 
				ShapeFactory.setBounds(Pasteboard.this, activeStack, startPoint, curPoint);
//			zoomScrollGestures.getOnMouseDraggedEventHandler().handle(event);			
			event.consume();
		}
	}
	//---------------------------------------------------------------------------
	private final class MouseReleasedHandler implements EventHandler<MouseEvent> 
	{
		@Override public void handle(final MouseEvent event) {
			removeMarquee();
			startPoint = curPoint = null;
			requestFocus();		// needed for the key event handler to receive events
			if (!isPoly(getTool()))
				palette.resetTool();
			event.consume();
		}
	}
	private void removeMarquee()	{	controller.remove(marquee);  }
	//---------------------------------------------------------------------------
	private final class MouseClickHandler implements EventHandler<MouseEvent> 
	{
		@Override public void handle(final MouseEvent event) 
		{			
			removeMarquee();
			startPoint = curPoint = null;
			requestFocus();		// needed for the key event handler to receive events
			palette.resetTool();
			event.consume();
		}
	}
	//---------------------------------------------------------------------------
	private final class MouseMovedHandler implements EventHandler<MouseEvent> 
	{
		@Override public void handle(final MouseEvent event) 	{		setDragLine(event);	}   // TODO report position?
	}
	//---------------------------------------------------------------------------
	/** 
	 *  KeyHandler
	 *  
	 *  unmodified keys switch the tool
	 *  backspace = delete
	 *  escape or space bar to terminate polyline
	 */
	
	private final class KeyHandler implements EventHandler<KeyEvent> {
		@Override
		public void handle(KeyEvent event) {

 			KeyCode key = event.getCode();
 			
 			if (key.isArrowKey())	 { getSelectionMgr().translate(key); event.consume(); } 
			else if (KeyCode.DELETE.equals(key)) 		getSelectionMgr().deleteSelection();		// create an undoable action
			else if (KeyCode.BACK_SPACE.equals(key)) 	getSelectionMgr().deleteSelection();
			
 			else if (KeyCode.R.equals(key)) 	palette.setTool(Tool.Rectangle);
			else if (KeyCode.C.equals(key)) 	palette.setTool(Tool.Circle);
			else if (KeyCode.P.equals(key)) 	palette.setTool(Tool.Polygon);
			else if (KeyCode.L.equals(key)) 	palette.setTool(Tool.Line);
			else if (KeyCode.W.equals(key)) 	palette.setTool(Tool.Polyline);
			
//			else if (KeyCode.X.equals(key)) 	setTool(Tool.Xhair);
			else if (KeyCode.ESCAPE.equals(key)) {		terminatePoly();  	removeDragLine(); }
			else if (KeyCode.SPACE.equals(key)) {		terminatePoly();  	removeDragLine(); }
////				controller.getSelectionManager().connect();
//						getModel().connectSelectedNodes();
		}

		private void terminatePoly() {
			if (getActiveShape() instanceof Polyline)
			{
				Polyline p = (Polyline) getActiveShape();
				terminatePoly(p.getPoints());
			}
				
			if (getActiveShape() instanceof Polygon)		{
				Polygon p = (Polygon) getActiveShape();
					terminatePoly(p.getPoints());
			}
		}
		private void terminatePoly(ObservableList<Double> pts) {
			if (pts.size() > 1) 
				pts.addAll(pts.get(0),pts.get(1));
			setActiveStack(null);
			resetPoly();
		}
	}	
	//---------------------------------------------------------------------------
	public Tool getTool()	{ return palette.getTool();	}
	public void resetTool() {		palette.resetTool();		}

	public void addComment(MouseEvent event) {
		TextArea newText = new TextArea("Comments: ");
		newText.setBackground(null);
		newText.setLayoutX(event.getX());
		newText.setLayoutY(event.getY());
		newText.setPrefColumnCount(20);
		newText.setPrefRowCount(10);
		newText.selectAll();
		newText.requestFocus();
		controller.addExternalNode(newText);

		newText.skinProperty().addListener(new ChangeListener<Skin<?>>() {			// doesn't work!

	        @Override
	        public void changed(
	          ObservableValue<? extends Skin<?>> ov, Skin<?> t, Skin<?> t1) {
	            if (t1 != null && t1.getNode() instanceof Region) {
	                Region r = (Region) t1.getNode();
	                r.setBackground(Background.EMPTY);

	                r.getChildrenUnmodifiable().stream().
	                        filter(n -> n instanceof Region).
	                        map(n -> (Region) n).
	                        forEach(n -> n.setBackground(Background.EMPTY));

	                r.getChildrenUnmodifiable().stream().
	                        filter(n -> n instanceof Control).
	                        map(n -> (Control) n).
	                        forEach(c -> c.skinProperty().addListener(this)); // *
	            }
	        }
	    });
		newText.setBackground(Background.EMPTY);
	}


	public void resetPoly()		
	{		
		if (dragLine != null && dragLine.isVisible()) 
			removeDragLine();
		palette.setTool(Tool.Arrow);	
	}
	
	boolean isPoly(Tool t)	{		return (Tool.Polyline == t || Tool.Polygon == t);	}

	//---------------------------------------------------------------------------
	Paint defaultStroke = Color.BLACK;			// TODO  pref
	Paint defaultFill = Color.WHITESMOKE;
	@Override public String toString()		{ 	return String.format("Pasteboard: %d %s", getChildren().size(), infoLabel.getText());	}
	public Paint getDefaultFill()			{	return 	defaultFill;	}
	public Paint getDefaultStroke()			{	return defaultStroke;	}
	public void setDefaultFill(Paint p)		{	defaultFill = p;	}
	public void setDefaultStroke(Paint p)	{	defaultStroke = p;	}
	
//	double currentZoom = 1.0;
//	public void setZoom(double value) {
//		double scale = Math.pow(2.0, value);
//		Scale sc = new Scale(scale, scale, 1, 500, 500, 0);
//		getTransforms().clear();		
//		getTransforms().add(sc);
//		currentZoom = value;
//	}
		//---------------------------------------------------------------------------
	public void resetLayerVisibility(String layername, boolean vis) {
		if (layername == null) return;
		List<Node> layerNodes = getChildrenInLayer(layername);
		for (Node n : layerNodes)
			n.setVisible(vis);
	}
	public void resetLayerLock(String layername, boolean lock) {
		if (layername == null) return;
		List<Node> layerNodes = getChildrenInLayer(layername);
		for (Node n : layerNodes)
		{
			n.setMouseTransparent(lock);
			n.setOpacity(lock ? 0.5 : 1.0);
		}
	}
	public void selectByType(String s) {	System.out.println("selectByType " + s); 			}

	public void connectTo(ResizableBox vNode, RelPosition relPos ) {
		if (dragLine == null) return;  	// shouldn't happen
		VNode src = getDragSource();
		Pos srcPos = getDragSourcePosition();
		if (src == null) return;  	// shouldn't happen
		
		if (relPos.isInside()) 
			relPos = relPos.moveToEdge();
		
		if (src != vNode)
		{
			ArrowType arrow = getController().getCurrentArrowType();
			EdgeType edge = getController().getCurrentLineBend();
			Interaction i = new Interaction(getModel(), src, srcPos, vNode, relPos, arrow, edge );
			controller.addInteraction(i);
//			i.rebind();
			removeDragLine();
			controller.redrawEdgesToMe((VNode) vNode);
			controller.modelChanged();
		}
	}

}