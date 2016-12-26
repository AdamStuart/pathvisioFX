package diagrams.pViz.view;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import animation.NodeVisAnimator;
import diagrams.pViz.app.Action.ActionType;
import diagrams.pViz.app.Controller;
import diagrams.pViz.app.Selection;
import diagrams.pViz.app.Tool;
import diagrams.pViz.model.MNode;
import diagrams.pViz.model.Model;
import diagrams.pViz.tables.GeneListController;
import diagrams.pViz.tables.PathwayController;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
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
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Polyline;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;
import javafx.scene.transform.Scale;
import javafx.stage.Screen;
import model.AttributeMap;
import model.bio.PathwayRecord;
import util.FileUtil;
import util.LineUtil;
import util.RectangleUtil;

// Pasteboard 
/*
 *  The root Panel of the content.
 *  The primary role of the Pasteboard is to handle mouse events that don't hit any node,
 *  and drop events that will create new nodes depending on the files / data that 
 *  are in the drop.  Key events are also caught.
 *  
 *  The Pasteboard also remembers the state of which tool is active and what default
 *  attributes will be assigned to new nodes.
 */
public class Pasteboard extends Pane
{
	//@formatter:off
	private static final String INFO_LABEL_ID = "infoLabel";
	private static final String ELEMENT_NAME = "Pasteboard";

	
	private Controller controller;
	public Controller getController()		{ return controller; }
	private Selection selectionMgr;
	public Selection getSelectionMgr()		{ return selectionMgr; }
	private Rectangle marquee;
	public Rectangle getMarquee()			{ return marquee;	}
	private Label infoLabel;
	public Label getInfoLabel()				{ return infoLabel;	}

//	SimpleDoubleProperty widthProperty = new SimpleDoubleProperty();
//	SimpleDoubleProperty heightProperty = new SimpleDoubleProperty();
//	public double getWidth()	{ return widthProperty.get();	}
//	public double getHeight()	{ return heightProperty.get();	}
//	public  void setWidth(double d)	{  widthProperty.set(d);	}
//	public void setHeight(double d)	{  heightProperty.set(d);	}
//	public SimpleDoubleProperty widthProperty()	{  return widthProperty;	}
//	public SimpleDoubleProperty heightProperty()	{  return heightProperty;	}

	private VNode activeStack;
	public VNode getActiveStack()		{ return activeStack;	}
	public Shape getActiveShape()		{ return activeStack == null ? null : activeStack.getFigure();	}
	public void setActiveStack(VNode s)	{ activeStack = s;	}
	//@formatter:on
	/**-------------------------------------------------------------------------------
	/**Canvas (Pane pane, Controller ctrl
	 * @param ctrl	-- the Controller that is parent to this object
	 * @param pane
	 *            the pane on which the selection rectangle will be drawn.
	 */
	public Pasteboard(Controller ctrl) 
	{
//		drawPane = pane;
		super();
		setWidth(2000);
		setHeight(2000);
		setId("root");
		controller = ctrl;
//		factory = new NodeFactory(this);
//		edgeFactory = new EdgeFactory(this);
//		shapeFactory = factory.getShapeFactory();
		makeMarquee();
		selectionMgr = new Selection(this);
//		pane.getChildren().add(marquee);
		setupMouseKeyHandlers();
		setupPasteboardDrops();
		infoLabel = new Label("");
		infoLabel.setId(INFO_LABEL_ID);
		getChildren().add(infoLabel);
		StackPane.setAlignment(infoLabel, Pos.TOP_RIGHT);
		infoLabel.setVisible(false);
		layoutBoundsProperty().addListener(e -> { resetGrid(); } ); 
//		turnOnClipping();
	}
	
	public void makeMarquee() {
		marquee = new Rectangle();
		marquee.setId("Marquee");
		marquee.setStroke(Color.GREEN);
		marquee.setFill(Color.TRANSPARENT);
		marquee.setStrokeWidth(1.6);
		marquee.getStrokeDashArray().addAll(3.0, 7.0, 3.0, 7.0);
	}

	public static boolean isMarquee(Node node) {
		return node != null && "Marquee".equals(node.getId());
	}

	public void clear()	{ getChildren().clear();	}
	public void add(VNode vnode)	{ getChildren().add(vnode);	}
	public void add(int idx, Node node)		{ getChildren().add(idx, node);	}
	public void add(int idx, VNode vnode)	{ getChildren().add(idx, vnode);	}
	public void addAll(Node[] n) {		getChildren().addAll(n);	}
	public void addAll(ObservableList<Node> n) {	getChildren().addAll(n);	}
	public void addAllVNodes(VNode[] n) {		getChildren().addAll(n);	}
	public void addAllVNodes(List<VNode> n) {	getChildren().addAll(n);	}

	private Rectangle clipRect = new Rectangle();
	private void turnOnClipping()
	{
		setClip(clipRect);
		heightProperty().addListener((o, oldVal, newVal) -> { clipRect.heightProperty().set((double) newVal);    });
		widthProperty().addListener((o, oldVal, newVal) -> { clipRect.widthProperty().set((double) newVal);    });
	}
	/*
	 * // Handle highlighting the canvas as mouse enters, and resetting as it leaves
	 */
	
	private void setupPasteboardDrops()
	{
		setOnDragEntered(e -> 	{  	highlightPasteboard(true);					e.consume();	});
		setOnDragExited(e -> 	{	highlightPasteboard(false);					e.consume();	});
		setOnDragOver(e -> 		{	e.acceptTransferModes(TransferMode.ANY);	e.consume();  	});
		setOnDragDropped(e ->	{ 	handlePasteboardDrop(e);					e.consume();  	});
	}	

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
	
	private void handlePasteboardDrop(DragEvent e)
	{
		Dragboard db = e.getDragboard();
		e.acceptTransferModes(TransferMode.ANY);
//		Set<DataFormat> formats = db.getContentTypes();
//		formats.forEach(a -> System.out.println("getContentTypes " + a.toString()));
		if (db.hasContent(GeneListController.GENE_MIME_TYPE))
		{
			String id = "" + db.getContent(DataFormat.PLAIN_TEXT);
			int end = id.indexOf(":");
			if (end > 0)
				id = id.substring(0, end);
			System.out.println(id);
		}
		if (db.hasContent(PathwayController.PATHWAY_MIME_TYPE))
		{
			Object o = db.getContent(PathwayController.PATHWAY_MIME_TYPE);
			// Source is the pathway browser
			if (o instanceof Integer)
			{
				String id = "" + db.getContent(DataFormat.PLAIN_TEXT);
				id = id.substring(0, id.indexOf(":"));
				System.out.println(id);
				List<PathwayRecord> results = PathwayController.getPathways(id);
				for (PathwayRecord rec : results)
				{
					System.out.println(rec);

				}
			}
			System.out.println(o.toString());
		}
		
		if (db.hasFiles())
		{
			List<File> files = db.getFiles();
			if (files != null)
			{
				controller.getUndoStack().push(ActionType.Add, " file");
				int offset = 0;
				for (File f : files)
				{
					if (FileUtil.isCDT(f))					// CDT is a genelist format
						controller.open(f);
					if (FileUtil.isGPML(f))					// css files are added to the Scene
						controller.open(f);
					else if (FileUtil.isCSS(f))					// css files are added to the Scene
						controller.addStylesheet(f);
					else if (FileUtil.isDataFile(f))		// data files are applied to the nodes
						controller.assignDataFile(f);
					else
					{
						offset += 20;
						StackPane stack = handleFileDrop(f, e.getX()+offset, e.getY()+offset);
						if (stack != null)
							controller.add(stack);
					}
				}
			}
		}
		else
		{
			Object content = db.getContent(DataFormat.PLAIN_TEXT);
					
			if (content != null)
			{
				String text = content.toString();
	System.out.println("Dropped: " + text);
				AttributeMap attrMap = new AttributeMap();
				attrMap.putDouble("X", e.getX());
				attrMap.putDouble("Y", e.getY());
				attrMap.put("CenterX", "" + e.getX());
				attrMap.put("CenterY", "" + e.getY());
				attrMap.put("Width", "80");
				attrMap.put("Height", "30");
				attrMap.put("ShapeType", text);
				attrMap.put("TextLabel", text);
				MNode node = new MNode(attrMap, getModel());
				getChildren().add(node.getStack());
			}
		}
			
		e.consume();
	}
	// **-------------------------------------------------------------------------------
	public VNode handleFileDrop(File f, double x, double y)
	{
		AttributeMap attrs = new AttributeMap(f, x, y);
		Tool tool = Tool.appropriateTool(f);
		attrs.setTool(tool == null ? "Arrow" : tool.toString());
		MNode model = new MNode(attrs, getModel());
		return model.getStack();
	}
	
	private Model getModel() {		return controller.getDrawModel();	}
	//-------------------------------------------------------------------------------
	public String getState()		
	{
		AttributeMap map = new AttributeMap();
		map.put("", "");
		map.put("tool", getTool().toString());
		map.putBool("infoShown", infoLabel.isVisible());
		map.put("fill", defaultFill.toString());
		map.put("stroke", defaultStroke.toString());
		return map.makeElementString(ELEMENT_NAME);
	}
	
	//-------------------------------------------------------------------------------
	public void setState(String s)		
	{
		AttributeMap map = new AttributeMap(s);
		controller.setTool(Tool.fromString(map.get("tool")));
		infoLabel.setVisible(map.getBool("infoShown")); 
		defaultFill = map.getPaint("fill");
		defaultStroke = map.getPaint("stroke");
	}
	
	//-------------------------------------------------------------------------------
	private Group grid;
	public Group getGrid()	{  return grid;  }
	List<Line> hLines;
	List<Line> vLines;
	
	public void makeGrid(Button toggler, ScrollPane scrlPane)
	{
		grid = new Group();
		grid.setId("grid");
		double res = Screen.getPrimary().getDpi();			// assumes inches
		Parent p  = getParent();
		if (hLines == null) hLines = new ArrayList<Line>();
		if (vLines == null) vLines = new ArrayList<Line>();
		
		if (scrlPane != null)
		{
			double canvasWidth = getWidth();
			double canvasHeight = getHeight();
			double nLines = Math.max(canvasWidth, canvasHeight) / res;
			for (int i = 0; i< nLines; i++)
			{
				Line vert = new Line();
				vert.setStrokeWidth(0.25);
				vLines.add(vert);
				LineUtil.set(vert, res * i, 0, res * i, canvasHeight);
				
				Line horz = new Line();
				horz.setStrokeWidth(0.25);
				hLines.add(horz);
				LineUtil.set(horz, 0, res * i, canvasWidth, res * i);
				grid.getChildren().addAll(vert, horz);
			}		
			grid.setMouseTransparent(true);
			getController().add(grid);
		}
		new NodeVisAnimator(grid, toggler);

	}
	public void showGrid(boolean vis)	{	grid.setVisible(vis);	}
	public boolean isGridVisible()		{	return	grid.isVisible();	}
	public void resetGrid()		
	{	
		double res = 100;  // Screen.getPrimary().getDpi();			// assumes inches
		double canvasWidth = getWidth();
		double canvasHeight = getHeight();
		double nLines = Math.max(canvasWidth, canvasHeight) / res;
		while (hLines.size() < nLines)
			hLines.add(new Line());
		while (vLines.size() < nLines)
			vLines.add(new Line());
		
		for (int i = 0; i< nLines; i++)
		{
			LineUtil.set(vLines.get(i), res * i, 0, res * i, canvasHeight);
			LineUtil.set(hLines.get(i), 0, res * i, canvasWidth, res * i);
		}
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
//		addEventHandler(MouseWheelEvent.WHEEL_UNIT_SCROLL, new MouseWheelHandler());
		addEventHandler(KeyEvent.KEY_RELEASED, new KeyHandler());
        setOnScroll((ScrollEvent event) -> {
            // Adjust the zoom factor as per your requirement
//            boolean shiftDown = event.isShiftDown();
//            boolean controlDown = event.isControlDown();
//        	double x = event.getSceneX();
//            double y = event.getSceneX();
            double deltaY = event.getDeltaY();
        	double zoomFactor = 1.05 * deltaY;
//            double deltaX = shiftDown ? 0 : event.getDeltaY();
            if (deltaY < 0)
                zoomFactor = 1 / zoomFactor;
//            double saveX = getTranslateX(); 
//            double saveY = getTranslateY(); 
////           setTranslateX(x);
//           setTranslateY(y);
//           setScaleX(getScaleX() * zoomFactor);
//           setScaleY(getScaleY() * zoomFactor);
//           setTranslateX(saveX-deltaX);
//           setTranslateY(saveY-deltaY);
//           
//        Scale scale = new Scale(zoomFactor, zoomFactor, 0, x, y, 0);
//       	getTransforms().clear();
//    	getTransforms().add(scale);
        });
		
	}

	private Point2D startPoint = null;		// remember where the mouse was pressed
//	private Point2D offset = null;			// distance from startPoint to the origin of the target
	private Point2D curPoint = null;		// mouse location in current event
	private Line dragLine = null;			// a polyline edge

	static boolean verbose = false;

//
	public void startDragLine(double x, double y) {
		if (dragLine == null)
		{
			dragLine = new Line();
			getChildren().add(dragLine);	
		}
		x -= 90;			// TODO  HACK  difference between scene and pasteboard coords
		y -= 30;
		dragLine.setStartX(x);
		dragLine.setStartY(y);
		dragLine.setEndX(x);
		dragLine.setEndY(y);
	}
	public void removeDragLine() {
		if (dragLine != null)
		{
			getChildren().remove(dragLine);	
			dragLine = null;
		}
	}
//	public void setLastClick(double x, double y) {
//		dragLine.setEndX(x);
//		dragLine.setEndY(y);
//	}
//-----------------------------------------------------------------------------------------------------------
	private final class MousePressedHandler implements EventHandler<MouseEvent> 
	{
		@Override public void handle(final MouseEvent event) 
		{
			if (verbose)
				System.out.println("MousePressedHandler, activeStack: " + activeStack);
			// do nothing for a right-click
			if (event.isSecondaryButtonDown()) 
			{ 
				// TODO popup canvas commands
				event.consume();
				return;	
			}
			if (getTool() == Tool.Polyline)
			{
				startPolyline(event);
				return;
			}
			Shape activeShape = getActiveShape();
			startPoint = new Point2D(event.getX(), event.getY());
			if (activeShape instanceof Polyline)
			{
				if (verbose) System.out.println("MousePressedHandler, Polygon: " );
				Polyline p = getPolyline();
				if (event.getClickCount() > 1)							activeStack = null;
				else if (LineUtil.onVertex(startPoint, p) >= 0)			activeStack = null;
				else p.getPoints().addAll(event.getX(), event.getY());
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
//			controller.getUndoStack().push(ActionType.Select);
			getSelectionMgr().clear();  
	
			if (!getTool().isArrow())
			{
				AttributeMap map = new AttributeMap("ShapeType", getTool().name());
				map.addPoint(event.getX(), event.getY());
				MNode mNode = new MNode(map, getController());
				activeStack = mNode.getStack();	
			}
			
			activeShape = activeStack == null ? null : activeStack.getFigure();
			if (activeShape instanceof Polygon)
			{
				Polygon p = (Polygon) activeShape;
				p.getPoints().addAll(event.getX(), event.getY());
			}
			if (activeShape instanceof Polyline)
			{
				Polyline p = (Polyline) activeShape;
				p.getPoints().addAll(event.getX(), event.getY());
			}
			if (activeShape instanceof Line)
			{
				Line p = (Line) activeShape;
				p.setStartX(event.getX());
				p.setStartY(event.getY());
				p.setEndX(event.getX());
				p.setEndY(event.getY());
			}
			if (activeShape == null) 
			{
				activeShape = marquee;
				RectangleUtil.setRect(marquee, event.getX(), event.getY(), 8,8);
				marquee.setVisible(true);
				if (marquee.getParent() == null)
					controller.add(marquee);
				if (event.getClickCount() > 2)
				{
					addComment(event);
				}
			}
			else
			{
				controller.getUndoStack().push(ActionType.New, " " + getTool().name());
				getChildren().add(activeStack);
				getSelectionMgr().select(activeStack);
			}
			
//			factory.setStartPoint(new Point2D(event.getX(), event.getY()));
			event.consume();
		}

		private void startPolyline(MouseEvent event) {
			if (verbose) System.out.println("MousePressedHandler, Polyline: " );
			if (activeStack == null)
			{
				AttributeMap at = new AttributeMap("ShapeType:" + getTool().name());
				MNode mNode = new MNode(at, controller.getDrawModel());
				activeStack = mNode.getStack();
				getChildren().add(activeStack);

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
			startDragLine(event.getX(), event.getY());
			p.setFill(null);
			event.consume();		}

		private Polyline tempPolyline;
		private Polyline getPolyline() {
			if (tempPolyline == null)
				tempPolyline = new Polyline();
			return tempPolyline;
		}
	}

	/** 
	 *  MouseDraggedHandler
	 */
	private final class MouseDraggedHandler implements EventHandler<MouseEvent> {
		@Override public void handle(final MouseEvent event) {

			if (event.isSecondaryButtonDown())  return;		// do nothing for a right-click drag
			if (verbose)	
				System.out.println("Pasteboard.MouseDraggedHandler, activeShape: " + activeStack);

			// store current cursor position
			curPoint = new Point2D(event.getX(), event.getY());
			if (startPoint == null) 
				startPoint = curPoint;
			if (getTool() == Tool.Arrow)	
			{
				Rectangle r = RectangleUtil.union(startPoint, curPoint);
//				System.out.println(r.toString());

				getSelectionMgr().clear();
				getSelectionMgr().select(r);
//				marquee.setVisible(true);
				RectangleUtil.setRect(marquee, r);
			}
			else setActiveShapeBounds();
				
			event.consume();
		}
		//---------------------------------------------------------------------------
		private void setActiveShapeBounds()
		{
			if (activeStack == null) return;
			double left = Math.min(curPoint.getX(),  startPoint.getX());
			double top = Math.min(curPoint.getY(),  startPoint.getY());
			double w = Math.max(20,Math.abs(curPoint.getX() - startPoint.getX()));
			double h = Math.max(20,Math.abs(curPoint.getY() - startPoint.getY()));
			if (verbose)
				System.out.println("setActiveShapeBounds, activeShape: " + activeStack);
			Shape activeShape = activeStack.getFigure();
			
			if (activeShape instanceof Rectangle)
			{
				Rectangle r = (Rectangle) activeShape;
//				r.setVisible(true);
//				RectangleUtil.setRect(r, left, top ,w, h);
				activeStack.setWidth(w);
				activeStack.setHeight(h);
			}
			if (activeShape instanceof Circle)
			{
				Circle c = (Circle) activeShape;
//				c.setVisible(true);
				c.setCenterX(startPoint.getX());
				c.setCenterY(startPoint.getY());
				double rad = Math.sqrt(w * w + h * h);
				c.setRadius(rad);
				setWidth(2*rad);
				setHeight(2*rad);
			}
			
			if (activeShape instanceof Polygon)
			{
				Polygon p = (Polygon) activeShape;
//				p.setVisible(true);
				int nPts = p.getPoints().size();
				if (nPts > 1)
				{
					p.getPoints().set(nPts-2, curPoint.getX());
					p.getPoints().set(nPts-1, curPoint.getY());
				}
			}
			
			if (activeShape instanceof Polyline)
			{
				Polyline p = (Polyline) activeShape;
//				p.setVisible(true);
				p.setFill(null);
				int nPts = p.getPoints().size();
				if (nPts > 1)
				{
					p.getPoints().set(nPts-2, curPoint.getX());
					p.getPoints().set(nPts-1, curPoint.getY());
				}
			}
			
			if (activeShape instanceof Line)
			{
				Line p = (Line) activeShape;
				p.setEndX(curPoint.getX());
				p.setEndY(curPoint.getY());
			}
		}
	}
	//---------------------------------------------------------------------------
	/** 
	 *  MouseReleasedHandler
	 */

	private final class MouseReleasedHandler implements EventHandler<MouseEvent> 
	{
		@Override public void handle(final MouseEvent event) {
			curPoint = new Point2D(event.getX(), event.getY());
			controller.remove(marquee);
//			if (RectangleUtil.isRectangleSizeTooSmall(startPoint, curPoint)) 		return;
//
//			if (startPoint.distance(curPoint) < 10)
//			{
//				TextArea newText = new TextArea("Comments: ");
//				newText.setBackground(null);
//				newText.setLayoutX(event.getX());
//				newText.setLayoutY(event.getY());
//				controller.add(newText);
//				newText.selectAll();
//				newText.requestFocus();
//				
//			}
			startPoint = curPoint = null;
			requestFocus();		// needed for the key event handler to receive events
			if (!isPoly(getTool()))
				resetTool();
			event.consume();
		}
	}
	//---------------------------------------------------------------------------
	/** 
	 *  MouseClickHandler
	 */

	private final class MouseClickHandler implements EventHandler<MouseEvent> 
	{
		@Override public void handle(final MouseEvent event) 
		{			
			controller.remove(marquee);
			startPoint = curPoint = null;
			requestFocus();		// needed for the key event handler to receive events
			resetTool();
			event.consume();
		}
	}
	//---------------------------------------------------------------------------
	/** 
	 *  MouseMovedHandler
	 */

	private final class MouseMovedHandler implements EventHandler<MouseEvent> 
	{
		@Override public void handle(final MouseEvent event) 
		{			
			if (dragLine != null)
			{
				dragLine.setEndX(event.getX());
				dragLine.setEndY(event.getY());
			}
		}
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
 			
 			if (key.isArrowKey())	getSelectionMgr().translate(key);
			else if (KeyCode.DELETE.equals(key)) 		getSelectionMgr().deleteSelection();		// create an undoable action
			else if (KeyCode.BACK_SPACE.equals(key)) 	getSelectionMgr().deleteSelection();
			
 			else if (KeyCode.R.equals(key)) 	controller.setTool(Tool.Rectangle);
			else if (KeyCode.C.equals(key)) 	controller.setTool(Tool.Circle);
			else if (KeyCode.P.equals(key)) 	controller.setTool(Tool.Polygon);
			else if (KeyCode.L.equals(key)) 	controller.setTool(Tool.Line);
			else if (KeyCode.W.equals(key)) 	controller.setTool(Tool.Polyline);
			
//			else if (KeyCode.X.equals(key)) 	setTool(Tool.Xhair);
			else if (KeyCode.ESCAPE.equals(key)) {		terminatePoly();  	removeDragLine(); }
			else if (KeyCode.SPACE.equals(key)) {		terminatePoly();	removeDragLine(); }
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
	boolean sticky = false;
	public Tool getTool()	{ return controller.getTool();	}
	public void addComment(MouseEvent event) {
		TextArea newText = new TextArea("Comments: ");
		newText.setBackground(null);
		newText.setLayoutX(event.getX());
		newText.setLayoutY(event.getY());
		newText.setPrefColumnCount(20);
		newText.setPrefRowCount(10);
		newText.selectAll();
		newText.requestFocus();
		controller.add(newText);

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
    
//	    
	}


	public void setTool(Tool t)
	{
		if (getTool() == t) sticky = true;
		if (getTool() == Tool.Arrow) sticky = false;
		controller.setTool(t);
		activeStack = null;
		if (verbose) 
			System.out.println("Tool set to: " + t.toString() + (sticky ? "!!" : ""));
	}
	
	public void resetTool()		
	{		
		activeStack = null;
		if (sticky) return;
		if (isPoly(getTool())) 	return;
		if (dragLine != null) 
			removeDragLine();
		controller.setTool(Tool.Arrow);	
	}
	public void resetPoly()		
	{		
		if (dragLine != null && dragLine.isVisible()) 
			removeDragLine();
		if (sticky) return;
		controller.setTool(Tool.Arrow);	
	}
	
	boolean isPoly(Tool t)	{		return (Tool.Polyline == t || Tool.Polygon == t);	}

	//---------------------------------------------------------------------------
	Paint defaultStroke = Color.BLACK;			// TODO  pref
	Paint defaultFill = Color.WHITESMOKE;
	@Override public String toString()	{ return String.format("Pasteboard: %d %s", getChildren().size(), infoLabel.getText());	}
	public Paint getDefaultFill()			{		return 	defaultFill;	}
	public Paint getDefaultStroke()			{		return defaultStroke;	}
	public void setDefaultFill(Paint p)		{		defaultFill = p;	}
	public void setDefaultStroke(Paint p)	{		defaultStroke = p;	}
	public void setZoom(double value) {
		double scale = Math.pow(2.0, value);
		Scale sc = new Scale(scale, scale, 1, 500, 500, 0);
		getTransforms().clear();		
		getTransforms().add(sc);		
//				setScaleX(scale);
//		setScaleY(scale);
//		setTranslateX(0);
//		setTranslateY(0);
	}

	
}

