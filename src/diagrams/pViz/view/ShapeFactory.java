package diagrams.pViz.view;

import java.io.File;
import java.util.List;
import java.util.Set;

import diagrams.pViz.app.Controller;
import diagrams.pViz.app.Selection;
import diagrams.pViz.app.Tool;
import diagrams.pViz.gpml.CellShapeFactory;
import diagrams.pViz.model.DataNode;
import gui.Action.ActionType;
import gui.Effects;
import gui.UndoStack;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.event.EventTarget;
import javafx.event.EventType;
import javafx.geometry.Point2D;
import javafx.scene.Cursor;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.input.DataFormat;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Ellipse;
import javafx.scene.shape.Line;
import javafx.scene.shape.Path;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Polyline;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;
import model.AttributeMap;
import util.FileUtil;
import util.LineUtil;
import util.RectangleUtil;
import util.StringUtil;

public class ShapeFactory {
	public static final double MARGIN = 8;
	public static final double MARGIN2 = 16;
	private Pasteboard drawLayer;
	boolean verbose = true;

	/*
	 * The ShapeFactory is responsible for creating new nodes that are shapes.
	 * Largely this is about defining the mouse event and drop handlers for the
	 * shapes.
	 * 
	 * makeMarquee creates the selection rectangle that is added on canvas drags.
	 */
	public ShapeFactory(Pasteboard l, UndoStack u) {
		drawLayer = l;
	}

	/*
	 * makeNewShape returns the shape but also adds it to stack's children
	 */
	static public Shape makeNewShape(String s, DataNode modelNode, VNode stack) {
		Tool tool = Tool.lookup(s);
//		if (tool == null || !tool.isShape())
//			tool = Tool.Rectangle;
		Shape newShape= null;
		AttributeMap attrMap = modelNode;
//		attrMap.put("stroke-width", "3");  	
		switch (tool)
		{
			case Cell:				newShape = new Rectangle();	break;
			case Nucleus:			newShape = new Ellipse();	break;
			case Metabolite:		newShape = new Rectangle();	break;
			case Protein:			newShape = new Rectangle();	break;
			case Pathway:			newShape = new Rectangle();	break;
			case GroupComponent:	newShape = new Rectangle();	break;

			case Rectangle:			newShape = new Rectangle();	break;
			case RoundedRectangle:	newShape = new Rectangle();	break;
			case Polygon:			newShape = new Polygon();	break;
			case Polyline:			newShape = new Polyline();	break;
			case Line:				newShape = new Line();		break;
			case Shape1:			newShape = Shape1.getHeartPath();	break;
			case Oval:				newShape = new Ellipse();	break;
			case Circle:			newShape = new Ellipse();	break;
			default:	 
				if (Tool.contains(Tool.customShapes,s))
					newShape = CellShapeFactory.makeCustomShape(s); 
		}
		if (newShape == null) 					return null;

//		makeHandlers(newShape);
		String id = attrMap.get("GraphId");
		if (id == null)
		{
			id = modelNode.getModel().gensym("" + s.charAt(0));
			attrMap.put("GraphId", id);
		}
		setDefaultAttributes(newShape);
		setAttributes(newShape, attrMap);
//		newShape.setManaged(false);
//		StackPane.setMargin(newShape, new Insets(1));
		if ("Line".equals(s))
		{
			Arrow a = new Arrow((Line) newShape, 0.7f);
			a.setFill(Color.BEIGE);
		}
		stack.setId(id);
		stack.getProperties().putAll(attrMap);
		if (tool == Tool.Mitochondria)
			stack.setScale(0.25);
		stack.getChildren().add(0, newShape);
		return newShape;
	}
	
	static private void setDefaultAttributes(Shape newShape) {
		newShape.setFill(Color.WHITE);
		newShape.setStroke(Color.BLACK);
		newShape.setStrokeWidth(0.5f);
	}
	public static void setAttributes(Shape shape, AttributeMap map) {
		// if (verbose>0) System.out.println(map.toString());		if (shape == null)	return;
		
		String shapeType = map.get("ShapeType");
		if ("{".equals(shapeType))
		{
			map.put("ShapeType", "Brace");
			map.put("Fill", "None");
			map.put("TextLabel", "");
		}
		if (shape instanceof Rectangle)
		{
			Rectangle r = ((Rectangle)shape);
			if ( "RoundedRectangle".equals(shapeType))
			{	
				r.setArcWidth(10);
				r.setArcHeight(10);
			}
			if (shape instanceof Rectangle && "Cell".equals(shapeType))
			{	
				r.setArcWidth(100);
				r.setArcHeight(100);
				r.setStroke(Color.GOLD);
				r.setStrokeWidth(8.0);
			}
			if (shape instanceof Rectangle && "Pathway".equals(shapeType))
			{	
				r.setStroke(Color.AQUAMARINE);
				r.setStrokeWidth(5.0);
				r.setWidth(120); 	r.setHeight(45);
			}
			if (shape instanceof Rectangle && "Gene".equals(shapeType))
			{	
				r.setStrokeWidth(1.0);
				r.setWidth(80); 	r.setHeight(30);
			}
	
			if (shape instanceof Rectangle && "Metabolite".equals(shapeType))
			{	
				r.setStroke(Color.NAVY);
				r.setStrokeWidth(2.0);
				r.setWidth(120); 	r.setHeight(20);
			}
			
			for (String k : map.keySet()) 
			{
				String val = map.get(k);
				k = k.toLowerCase();
				if (k.equals("graphid"))   			shape.setId(val);
	
	//			if (k.equals("textlabel"))   
	//				setText(val);
				if (k.equals("fontsize"))			;// TODO	k = "-fx-font-size"??;
				if (k.equals("fontweight"))			;// TODO	k = "-fx-font-size"??;
				if (k.equals("valign"))				;// TODO	
				if (k.equals("zorder"))				;// TODO	
				if (k.equals("stroke"))				k = "-fx-stroke";
				if (k.equals("strokewidth"))		k = "-fx-stroke-weight";
				if (k.equals("linethickness"))		k = "-fx-stroke-weight";
				if (k.equals("graphid"))			shape.setId(val);
				double d = StringUtil.toDouble(val); // exception safe: comes back
														// NaN if val is not a
														// number
				if (shape instanceof Rectangle) {
					if (k.equals("centerx"))		r.setX(d+MARGIN);	
					else if (k.equals("centery"))	r.setY(d+MARGIN);
					else if (k.equals("x"))			r.setX(d+MARGIN);
					else if (k.equals("y"))			r.setY(d+MARGIN); 
					else if (k.equals("width"))		r.setWidth(d+MARGIN2);
					else if (k.equals("height"))	r.setHeight(d+MARGIN2);
				}
				if (shape instanceof Circle) {
					Circle circ = (Circle) shape;
					if (k.equals("centerx"))		circ.setCenterX(d);
					else if (k.equals("centery"))	circ.setCenterY(d);
					else if (k.equals("x"))			circ.setCenterX(d);
					else if (k.equals("y"))			circ.setCenterY(d);
					else if (k.equals("width"))		circ.setRadius(d/2);
					else if (k.equals("radius"))	circ.setRadius(d);
				}
				if (shape instanceof Polygon) {
					Polygon poly = (Polygon) shape;
					if (k.equals("points"))			parsePolygonPoints(poly, map.get(k));
				}
				if (shape instanceof Polyline) {
					Polyline poly = (Polyline) shape;
					if (k.equals("points"))			parsePolylinePoints(poly, map.get(k));
				}
				if (shape instanceof Line) {
					Line line = (Line) shape;
					if (k.equals("points"))			parseLinePoints(line, map.get(k));
					if (k.equals("stroke-width"))	line.setStrokeWidth(d);
					
				}
				try {
					Shape sh = shape;   
					if (k.equals("fill") || k.equals("-fx-fill") || k.equals("fillcolor")) 
						sh.setFill(Color.web(val));
					else if (k.equals("-fx-stroke") || k.equals("color"))	
						sh.setStroke(Color.web(val));
					else if (k.equals("-fx-stroke-weight") || k.equals("linethickness"))
						sh.setStrokeWidth(d);
				} 
				catch (Exception e) {		System.err.println("Parse errors: " + k);	}
			}
		}
	}

	private static void parsePolygonPoints(Polygon poly, String string) {	parsePoints(poly.getPoints(), string);	}

	private static void parsePolylinePoints(Polyline poly, String string) {parsePoints(poly.getPoints(), string);	}

	private static void parsePoints(ObservableList<Double> points, String string) {
		String s = string.trim();
		s = s.substring(1, s.length());
		String[] doubles = s.split(",");
		for (String d : doubles)
			points.add(Double.parseDouble(d));
	}

	private static void parseLinePoints(Line line, String string) {
		String s = string.trim();
		s = s.substring(1, s.length());
		String[] doubles = s.split(",");
		assert(doubles.length == 4);
		line.setStartX(Double.parseDouble(doubles[0]));
		line.setStartY(Double.parseDouble(doubles[1]));
		line.setEndX(Double.parseDouble(doubles[2]));
		line.setEndY(Double.parseDouble(doubles[3]));
	}

	// **-------------------------------------------------------------------------------
	// MouseEvents and DragEvents
	public void makeHandlers(Shape s) {
		if (s == null)		return;
		if (s instanceof Circle)			new CircleMouseHandler((Circle) s, drawLayer);
		else if (s instanceof Rectangle)	new RectMouseHandler((Rectangle) s, drawLayer);
		else if (s instanceof Polygon)		new PolygonMouseHandler((Polygon) s, drawLayer);
		else if (s instanceof Polyline)		new PolylineMouseHandler((Polyline) s, drawLayer);
		else if (s instanceof Line)			new LineMouseHandler((Line) s, drawLayer);
		else if (s instanceof Shape)		new ShapeMouseHandler((Shape) s, drawLayer);
//		if (s instanceof Shape2)		new ShapeMouseHandler((Shape2) s, drawLayer);

		s.setOnDragEntered(e -> {	s.setEffect(null);			e.consume();		});  // Effects.sepia
		s.setOnDragExited(e -> 	{	s.setEffect(null);					e.consume();		});
		s.setOnDragOver(e -> 	{	e.acceptTransferModes(TransferMode.ANY); 	e.consume();	});
		s.setOnDragDropped(e -> { 	e.acceptTransferModes(TransferMode.ANY); 	handleDrop(s, e);	e.consume();	});
	}
	// **-------------------------------------------------------------------------------
	private void handleDrop(Shape s, DragEvent e) {
		Dragboard db = e.getDragboard();
		Set<DataFormat> formats = db.getContentTypes();
		formats.forEach(a -> System.out.println("getContentTypes " + a.toString()));
		Shape shape = (Shape) e.getTarget();
		if (db.hasString()) {
			String q = db.getString();
			if (q.contains("-fx-")) {
				AttributeMap attrMap = new AttributeMap(q);
				setAttributes(shape, attrMap);
				Selection sel = drawLayer.getController().getSelectionManager();
				if (sel.isSelected(shape))
					sel.setAttributes(attrMap);
			}
//			if (verbose) System.out.println(q);
		}

		if (db.hasFiles()) {
			List<File> files = db.getFiles();
			if (files != null) {
				// controller.getUndoStack().push(ActionType.Add, " file");
//				int offset = 0;
				for (File f : files) {
//					offset += 20;
					if (verbose) System.out.println("File: " + f.getAbsolutePath());
					if (FileUtil.isCSS(f)) {
						StringBuilder buff = new StringBuilder();
						FileUtil.readFileIntoBuffer(f, buff);
						String styl = buff.toString();
						shape.getStyleClass().add(styl);
						if (verbose) System.out.println("S: " + styl);
					}
				}
			}
		}
	}
	// **-------------------------------------------------------------------------------
	private void doDoubleClick(final MouseEvent event) {
		
		EventTarget target = event.getTarget();
		if (target instanceof Rectangle)
		{
			Rectangle rect = (Rectangle) target;
			Object obj = rect.getProperties().get("BiopaxRef");
			if (obj instanceof String)
			{
				drawLayer.getController().openByReference("" + obj);
			}
		}
	}
	// **-------------------------------------------------------------------------------
	// SHAPE MOUSE HANDLERS

	private class RectMouseHandler extends NodeMouseHandler {
		public RectMouseHandler(Rectangle r, Pasteboard d) {
			super(d);
			r.addEventHandler(MouseEvent.MOUSE_PRESSED, this);
			r.addEventHandler(MouseEvent.MOUSE_DRAGGED, this);
			r.addEventHandler(MouseEvent.MOUSE_MOVED, this);
			r.addEventHandler(MouseEvent.MOUSE_ENTERED, this);
			r.addEventHandler(MouseEvent.MOUSE_EXITED, this);
			r.addEventHandler(MouseEvent.MOUSE_RELEASED, this);
		}

		@Override
		protected void handleMousePressed(final MouseEvent event) {
			if (drawLayer.getTool() != Tool.Arrow) return;
			if (((Node) event.getTarget()).getParent() instanceof Group)
				return;
			super.handleMousePressed(event);
			if (event.getClickCount() > 1) {
				doDoubleClick(event);
				return;
			}
			startPoint = currentPoint;

			if (RectangleUtil.inCorner(event)) {
				resizing = true;
				dragging = false;
				startPoint = RectangleUtil.oppositeCorner(event);
				undoStack.push(ActionType.Resize);
				System.out.println("Pressed: " + currentPoint + " is opposite " + startPoint.toString());
			}
		}

		@Override
		protected void handleMouseDragged(final MouseEvent event) {
			if (drawLayer.getTool() != Tool.Arrow) return;
			if (((Node) event.getTarget()).getParent() instanceof Group)
				return;
			if (verbose > 3)
				System.out.println("RectMouseDraggedHandler, Target: " + event.getTarget());
			super.handleMouseDragged(event);

			if (resizing) {
//				System.out.println("startPoint: " + startPoint.toString());
//				System.out.println("CurrentPoint: " + currentPoint.toString());
				double x, y, width, height;
				x = Math.min(startPoint.getX(), currentPoint.getX());
				y = Math.min(startPoint.getY(), currentPoint.getY());
				width = Math.abs(startPoint.getX() - currentPoint.getX());
				height = Math.abs(startPoint.getY() - currentPoint.getY());

//				System.out.println("( " + x + ", " + y + ") Width = " + width + " height = " + height);

				if (event.getTarget() instanceof Rectangle) {
					Rectangle r = (Rectangle) event.getTarget();
					RectangleUtil.setRect(r, x, y, width, height);
				}
			}
		}

		@Override
		protected void handleMouseMoved(final MouseEvent event) {
			if (((Node) event.getTarget()).getParent() instanceof Group)
				return;
			super.handleMouseMoved(event);
			if (event.getTarget() instanceof Rectangle) {
				Rectangle r = (Rectangle) event.getTarget();
				r.setCursor(RectangleUtil.inCorner(currentPoint, r) ? Cursor.H_RESIZE : Cursor.HAND);
			}
		}

	}
	// **-------------------------------------------------------------------------------

	private class ShapeMouseHandler extends NodeMouseHandler {
		public ShapeMouseHandler(Shape s, Pasteboard d) {
			super(d);
			s.addEventHandler(MouseEvent.MOUSE_PRESSED, this);
			s.addEventHandler(MouseEvent.MOUSE_DRAGGED, this);
			s.addEventHandler(MouseEvent.MOUSE_MOVED, this);
			s.addEventHandler(MouseEvent.MOUSE_RELEASED, this);
		}

		@Override
		protected void handleMousePressed(final MouseEvent event) {
			if (drawLayer.getTool() != Tool.Arrow) return;
			if (((Node) event.getTarget()).getParent() instanceof Group)
				return;
			super.handleMousePressed(event);
		}

		@Override
		protected void handleMouseDragged(final MouseEvent event) {
			if (drawLayer.getTool() != Tool.Arrow) return;
//			if (((Node) event.getTarget()).getParent() instanceof Group)
//				return;
			if (verbose > 3)
				System.out.println("RectMouseDraggedHandler, Target: " + event.getTarget());
			super.handleMouseDragged(event);

//			if (resizing) {
////				System.out.println("startPoint: " + startPoint.toString());
////				System.out.println("CurrentPoint: " + currentPoint.toString());
//				double x, y, width, height;
//				x = Math.min(startPoint.getX(), currentPoint.getX());
//				y = Math.min(startPoint.getY(), currentPoint.getY());
//				width = Math.abs(startPoint.getX() - currentPoint.getX());
//				height = Math.abs(startPoint.getY() - currentPoint.getY());
//
////				System.out.println("( " + x + ", " + y + ") Width = " + width + " height = " + height);
//
//				if (event.getTarget() instanceof Rectangle) {
//					Rectangle r = (Rectangle) event.getTarget();
//					RectangleUtil.setRect(r, x, y, width, height);
//				}
//			}
		}

		@Override
		protected void handleMouseMoved(final MouseEvent event) {
			if (((Node) event.getTarget()).getParent() instanceof Group)
				return;
			super.handleMouseMoved(event);
//			if (event.getTarget() instanceof Rectangle) {
//				Rectangle r = (Rectangle) event.getTarget();
//				r.setCursor(RectangleUtil.inCorner(currentPoint, r) ? Cursor.H_RESIZE : Cursor.HAND);
//			}
		}
	}
	// **-------------------------------------------------------------------------------
	// **-------------------------------------------------------------------------------

	protected class CircleMouseHandler extends NodeMouseHandler {
		public CircleMouseHandler(Circle c, Pasteboard d) {
			super(d);
			c.addEventHandler(MouseEvent.MOUSE_PRESSED, this);
			c.addEventHandler(MouseEvent.MOUSE_DRAGGED, this);
			c.addEventHandler(MouseEvent.MOUSE_MOVED, this);
			c.addEventHandler(MouseEvent.MOUSE_RELEASED, this);
		}

		@Override
		protected void handleMousePressed(final MouseEvent event) {
			if (drawLayer.getTool() == Tool.Line) 
				{
//				create the line from the center
//				return;
				
				}
			if (drawLayer.getTool() != Tool.Arrow) return;
			if (((Node) event.getTarget()).getParent() instanceof Group)			return;
			super.handleMousePressed(event);
			Circle c = (Circle) event.getTarget();
			if (LineUtil.onEdge(currentPoint, c)) {
				resizing = true;
				dragging = false;
				undoStack.push(ActionType.Resize);
			}
		}

		@Override
		protected void handleMouseDragged(final MouseEvent event) {
			if (drawLayer.getTool() != Tool.Arrow) return;
			if (((Node) event.getTarget()).getParent() instanceof Group)		return;
			super.handleMouseDragged(event);
			if (verbose > 3)
				System.out.println("RectMouseDraggedHandler, Target: " + event.getTarget());

			if (resizing) {
				Circle c = (Circle) target;
				double dx = c.getCenterX() - currentPoint.getX();
				double dy = c.getCenterY() - currentPoint.getY();

				double newRadius = Math.sqrt(dx * dx + dy * dy);
				c.setRadius(newRadius);
			}
		}

		@Override
		protected void handleMouseMoved(final MouseEvent event) {
			super.handleMouseMoved(event);
			Circle c = (Circle) target;
			if (LineUtil.onEdge(currentPoint, c))	c.setCursor(Cursor.H_RESIZE);
			else							c.setCursor(Cursor.HAND);
		}
	}


	// **-------------------------------------------------------------------------------
	protected class PolygonMouseHandler extends NodeMouseHandler {
		public PolygonMouseHandler(Polygon p, Pasteboard d) {
			super(d);
			assignMouseHandlers( p, this);
		}

		@Override
		protected void handleMousePressed(final MouseEvent event) {
			super.handleMousePressed(event);
			if (event.getTarget() == drawLayer.getActiveStack()) {
				drawLayer.setActiveStack(null);
				event.consume();
				return;
			}
			if (verbose > 3)
				System.out.println("PolygonMousePressedHandler: " + event.getTarget());
			Polygon p = (Polygon) target;
			int idx = LineUtil.onVertex(currentPoint, p);
			// System.out.println("" + idx);
			if (idx >= 0)	activeIndex = idx;
			else			dragging = true;
		}

		int activeIndex = -1;

		@Override
		protected void handleMouseDragged(final MouseEvent event) {
			super.handleMouseDragged(event);
			if (verbose > 1)
				System.out.println("Index: " + activeIndex);
			Polygon p = (Polygon) target;
			if (activeIndex >= 0) {
				p.getPoints().set(activeIndex, currentPoint.getX());
				p.getPoints().set(activeIndex + 1, currentPoint.getY());
			}
		}

		@Override
		protected void handleMouseMoved(final MouseEvent event) {
			super.handleMouseMoved(event);
			Polygon p = (Polygon) target;
			if (LineUtil.onVertex(currentPoint, p) >= 0)	p.setCursor(Cursor.H_RESIZE);
			else	p.setCursor(Cursor.HAND);
		}
	}

	// **-------------------------------------------------------------------------------
	// **-------------------------------------------------------------------------------
	// **-------------------------------------------------------------------------------
	// **-------------------------------------------------------------------------------
	// **-------------------------------------------------------------------------------
	protected class PolylineMouseHandler extends NodeMouseHandler {
		public PolylineMouseHandler(Polyline p, Pasteboard d) {
			super(d);
			assignMouseHandlers( p, this);
		}
//		SimpleDoubleProperty mouseX, mouseY;
		

		@Override
		protected void handleMousePressed(final MouseEvent event) {
			super.handleMousePressed(event);
			Polyline p = (Polyline) target;
			int idx = LineUtil.onVertex(currentPoint, p);
			if (event.getTarget() == drawLayer.getActiveStack() && idx == 0) {
				drawLayer.setActiveStack(null);
				activeIndex = p.getPoints().size();
				p.getPoints().addAll(currentPoint.getX(), currentPoint.getY());
				event.consume();
				drawLayer.removeDragLine();
				drawLayer.resetTool();
				return;
			}
			if (verbose > 3)
				System.out.println("PolylineMousePressedHandler: " + event.getTarget());
			// System.out.println("" + idx);
			if (idx >= 0)		activeIndex = idx;
			else				dragging = true;

			p.getPoints().set(activeIndex, currentPoint.getX());
			p.getPoints().set(activeIndex+1, currentPoint.getY());
		}
		int activeIndex = -1;

		@Override
		protected void handleMouseDragged(final MouseEvent event) {
			super.handleMouseDragged(event);
			if (verbose > 1)				System.out.println("Index: " + activeIndex);
			Polyline p = (Polyline) target;
			if (activeIndex >= 0) {
				p.getPoints().set(activeIndex, currentPoint.getX());
				p.getPoints().set(activeIndex + 1, currentPoint.getY());
			}
			if (dragLine != null) 	
			{
				dragLine.setEndX(event.getX());
				dragLine.setEndY(event.getY());
			}
//			if (mouseX != null) 	mouseX.set(event.getX());
//			if (mouseY != null) 	mouseY.set(event.getY());
		}

		@Override
		protected void handleMouseMoved(final MouseEvent event) {
			super.handleMouseMoved(event);
//			drawLayer.setLastClick(event.getX(), event.getY());
			Polyline p = (Polyline) target;
			if (LineUtil.onVertex(currentPoint, p.getPoints()) >= 0)			p.setCursor(Cursor.H_RESIZE);
			else p.setCursor(Cursor.HAND);
			if (dragLine != null) 	
			{
				dragLine.setEndX(event.getX());
				dragLine.setEndY(event.getY());
			}
//			if (mouseY != null) 	
		}
		@Override
		protected void handleMouseReleased(final MouseEvent event) {
			
//			drawLayer.getPane().getChildren().remove(dragLine);
//			dragLine = null;
		}

	}
	// **-------------------------------------------------------------------------------
	protected class LineMouseHandler extends NodeMouseHandler {
		public LineMouseHandler(Line p, Pasteboard d) {
			super(d);
			assignMouseHandlers( p, this);
		}
//		SimpleDoubleProperty mouseX, mouseY;
		

		@Override
		protected void handleMousePressed(final MouseEvent event) {
			super.handleMousePressed(event);
			if (event.getTarget() == drawLayer.getActiveStack()) {
				drawLayer.setActiveStack(null);
				event.consume();
				return;
			}
			if (verbose > 3)
				System.out.println("LineMousePressedHandler: " + event.getTarget());
			Line p = (Line) target;
			activeIndex = LineUtil.onEndpoint(currentPoint, p);
			// System.out.println("" + idx);
//			if (idx >= 0)		activeIndex = idx;
//			else				dragging = true;
			
		}
		int activeIndex = -1;

		@Override
		protected void handleMouseDragged(final MouseEvent event) {
			super.handleMouseDragged(event);
			if (verbose > 3)				System.out.println("Index: " + activeIndex);
			Line p = (Line) target;
			if (activeIndex == 0) {
				p.setStartX(currentPoint.getX());
				p.setStartY(currentPoint.getY());
			}
			else if (activeIndex == 1) {
				p.setEndX(currentPoint.getX());
				p.setEndY(currentPoint.getY());
			}
			else super.handleMouseDragged(event);
		}

//		@Override
//		protected void handleMouseMoved(final MouseEvent event) {
//			super.handleMouseMoved(event);
//			drawLayer.setLastClick(event.getX(), event.getY());
//			Line p = (Line) target;
//			if (onEndpoint(currentPoint, p) >= 0)	p.setCursor(Cursor.H_RESIZE);
//			else p.setCursor(Cursor.HAND);
//			if (mouseX != null) 	mouseX.set(event.getX());
//			if (mouseY != null) 	mouseY.set(event.getY());
//			p.setEndX(event.getX());
//			p.setEndY(event.getY());
//		}
	}

	// -----------------------------------------------------------------------------------
	public void makeNodeMouseHandler(Node n) {
		NodeMouseHandler h = new NodeMouseHandler(drawLayer);
		assignMouseHandlers(n, h);
	}
	
	private void assignMouseHandlers(Node n, NodeMouseHandler h)
	{
		n.addEventHandler(MouseEvent.MOUSE_PRESSED, h);
		n.addEventHandler(MouseEvent.MOUSE_DRAGGED, h);
		n.addEventHandler(MouseEvent.MOUSE_MOVED, h);
		n.addEventHandler(MouseEvent.MOUSE_RELEASED, h);

	}
	// -----------------------------------------------------------------------------------
		private class NodeMouseHandler extends BasicMouseHandler {
			public NodeMouseHandler(Pasteboard d) {
				super(d);
			}

		@Override
		public void handle(MouseEvent e) {
			target = e.getTarget();
			// scenePoint = new Point2D(e.getSceneX(), e.getSceneY());
			currentPoint = new Point2D(e.getX(), e.getY());
			if (verbose > 4)
				System.out.println((int) currentPoint.getX() + ", " + (int) currentPoint.getY());
			EventType<?> type = e.getEventType();

			if (type == MouseEvent.MOUSE_MOVED)				handleMouseMoved(e);
			else if (type == MouseEvent.MOUSE_PRESSED)		handleMousePressed(e);
			else if (type == MouseEvent.MOUSE_DRAGGED)		handleMouseDragged(e);
			else if (type == MouseEvent.MOUSE_RELEASED)		handleMouseReleased(e);
			prevPoint = currentPoint;
		}

		// **-------------------------------------------------------------------------------
		protected Point2D startPoint, currentPoint, prevPoint, offset;

		protected int verbose = 3;
		protected boolean dragging = false;
		protected boolean resizing = false;
		protected EventTarget target;
		Line dragLine = new Line();
		private ContextMenu menu;

		// **-------------------------------------------------------------------------------
		protected void handleMouseMoved(MouseEvent event) {
			event.consume();
		}

		protected void handleMousePressed(MouseEvent event) {
			if (verbose >= 3)
				System.out.println("NodeMousePressedHandler, Target: " + event.getTarget());
			if (drawLayer.getTool().isArrow())
			{
				resizing = false;
				prevPoint = currentPoint;
				boolean altDown = event.isAltDown();
				// boolean leftClick = event.isPrimaryButtonDown();
				boolean rightClick = event.isSecondaryButtonDown();
				if (altDown)
					pasteboard.getController().getSelectionManager().cloneSelection(5);
				// do nothing for a right-click
				// if (event.isSecondaryButtonDown()) return;// TODO -- popup up
				// Node menu
				if (event.isPopupTrigger() || rightClick) {
					if (menu == null)
						menu = buildContextMenu();
					if (menu != null)
						menu.show(pasteboard, event.getScreenX(), event.getScreenY());
					return;
				}
				Selection sel = drawLayer.getSelectionMgr();
				if (target instanceof VNode)
				{
					VNode node = (VNode) target;
					boolean wasSelected = sel.isSelected(node);
					if (event.isControlDown())			sel.select(node, !wasSelected);
					else if ((event.isShiftDown()))		sel.select(node);
					else if (!wasSelected)				sel.selectX(node);
				}
				if (sel.count() > 0) {
					dragging = true;
					pasteboard.getController().getUndoStack().push(ActionType.Move);
					startPoint = currentPoint;
				}
				event.consume();
			}
	}
		//--------------------------------------------------------------------------------------------
		private ContextMenu buildContextMenu() {
			menu = new ContextMenu();
			Controller c = pasteboard.getController();
//			MenuItem cut = 		makeItem("Cut", a -> 			{	c.cutSelection();	});
//			MenuItem copy = 		makeItem("Copy", a -> 			{	c.copySelection();	});
			MenuItem dup = 		makeItem("Duplicate", a -> 		{	c.duplicateSelection();	});
			MenuItem del = 		makeItem("Delete", a -> 		{	c.deleteSelection();	});
			MenuItem toFront = 	makeItem("Bring To Front", a -> {	c.toFront();	});
			MenuItem toBack = 	makeItem("Send To Back", a -> 	{	c.toBack();	});
			MenuItem group = 	makeItem("Group", a -> 			{	c.group();	});
			MenuItem ungroup = 	makeItem("Ungroup", a -> 		{	c.ungroup();	});
			menu.getItems().addAll(toFront, toBack, group, ungroup, dup, del);   //cut, copy
			return menu;
		}
		
		private MenuItem makeItem(String name, EventHandler<ActionEvent> foo)
		{
			MenuItem item = new MenuItem(name);	
			item.setOnAction(foo);
			return item;
		}
		//--------------------------------------------------------------------------------------------

		protected void handleMouseDragged(MouseEvent event) {
		if (verbose > 3)
				System.out.println("NodeMouseDraggedHandler, Target: " + event.getTarget());
			// do nothing for a right-click
			if (event.isSecondaryButtonDown())
				return;
			if (drawLayer.getTool() == Tool.Arrow)
			{
				if (dragging) {
					double dx, dy;
					dx = prevPoint.getX() - currentPoint.getX();
					dy = prevPoint.getY() - currentPoint.getY();
	
					// System.out.println("Delta: " + dx + ", " + dy);
					drawLayer.getSelectionMgr().translate(dx, dy);
					prevPoint = currentPoint;
				}
			}
			event.consume();
		}

		protected void handleMouseReleased(MouseEvent event) {
			startPoint = null;
//			dragLine = null;
			resizing = dragging = false;
			drawLayer.requestFocus(); 
			event.consume();
//			drawLayer.getController().refreshZoomPane();
		}
	}
		protected void handleMouseEntered(MouseEvent event) 
		{
		}
	
		protected void handleMouseExited(MouseEvent event) 
		{
		}

}

// -----------------------------------------------------------------------
// -----------------------------------------------------------------------
////
//public MNode makeNewMNode(AttributeMap attrMap, Model m, Pasteboard pasteboard) {
//	String type = attrMap.get("ShapeType");
//	Tool tool = Tool.lookup(type);
//	if (tool  != null)
//		return makeNewMNode(tool, attrMap, m, pasteboard);
//	return null;
//}

//// **-------------------------------------------------------------------------------
//public MNode makeNewMNode(Tool type, AttributeMap attrMap, Model m, Pasteboard pasteboard) {
//	MNode modelNode = new MNode(attrMap, m, pasteboard);
//	VNode view = new VNode(modelNode, pasteboard);
////	makeNewShape(attrMap.getToolName(), modelNode, view);
//	view.setId("V" + attrMap.get("GraphId"));
//	if (view.getShapeLayer() instanceof  Circle)
//	{
//		double rad = attrMap.getDouble("Radius");
//		if (Double.isNaN(rad))
//			rad = attrMap.getDouble("Width") / 2;
//		((Circle) view.getShapeLayer()).setRadius(rad);
//	}
//	return modelNode;
//}
//
//public VNode makeVNode(MNode modelNode) {
//	String s = modelNode.getShapeType();
//	VNode stack = new VNode(modelNode);
//	 makeNewShape(s, modelNode, stack);
//	 return stack;
//}

//public VNode makeNewShape(String s, MNode modelNode) {
//	VNode stack = new VNode(modelNode);
//	 makeNewShape(s, modelNode, stack);
//	 return stack;
//}
//public StackPane makeLabeledShapePane(Tool tool, AttributeMap attrMap, String s) {
//Shape newShape = makeNewShape(tool, attrMap);
//StackPane stack = new StackPane();
//final Label text = createLabel(s);
//text.setTranslateX(attrMap.getDouble("centerX"));
//text.setTranslateY(attrMap.getDouble("centerY"));
//StackPane.setAlignment(newShape, Pos.CENTER);
//StackPane.setAlignment(text, Pos.CENTER);
//stack.getChildren().addAll(newShape, text);
//makeNodeMouseHandler(stack);
//return stack;
//}

//public Group makeLabeledShapeGroup(Tool tool, AttributeMap attrMap, String s) {
//Shape newShape = makeNewShape(tool, attrMap);
//Group group = new Group();
//final Label text = createLabel(s);
//text.setTranslateX(attrMap.getDouble("centerX"));
//text.setTranslateY(attrMap.getDouble("centerY"));
//StackPane.setAlignment(newShape, Pos.CENTER);
//StackPane.setAlignment(text, Pos.CENTER);
//group.getChildren().addAll(newShape, text);
//makeNodeMouseHandler(group);
//return group;
//}
//public Label createLabel(String s, Color c) {
//Label label = createLabel(s);
//label.setTextFill(c);
//return label;
//}
//
//public static Label createLabel(String s) {
//final Label text = new Label(s);
//text.setFont(new Font(12));
////text.setBoundsType(TextBoundsType.VISUAL);
//text.setMouseTransparent(true);
//return text;
//}
// -----------------------------------------------------------------------
//public MNode parseShape(AttributeMap attrMap, Model m) {
//return makeNewMNode( attrMap, m);
//}
//
