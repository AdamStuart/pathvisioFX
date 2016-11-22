package diagrams.pViz.model;

import java.io.File;
import java.util.List;
import java.util.Set;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.NodeList;

import diagrams.pViz.app.Action.ActionType;
import diagrams.pViz.app.Controller;
import diagrams.pViz.app.Tool;
import diagrams.pViz.app.UndoStack;
import diagrams.pViz.view.BasicMouseHandler;
import diagrams.pViz.view.Pasteboard;
import diagrams.pViz.view.ShapeFactory;
import diagrams.pViz.view.VNode;
import gui.Backgrounds;
import javafx.beans.binding.Bindings;
import javafx.beans.property.StringProperty;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.event.EventTarget;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.DataFormat;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Polyline;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.SVGPath;
import javafx.scene.shape.Shape;
import javafx.scene.web.WebView;
import model.AttributeMap;
import model.CSVTableData;
import util.FileUtil;
import util.MacUtil;
import util.StringUtil;

public class NodeFactory
{
/*
 *  NodeFactory has responsibility of creating nodes of all types, whether by command, mouse action, drops,
 *  files being read in, or undo.  The DrawLayer is the "parent" of the NodeFactory, but the term is
 *  not the same as node.parent().  
  */
	int verbose = 0;
	
	public NodeFactory(Pasteboard layer)
	{
		pasteboard = layer;
		shapeFactory = new ShapeFactory(pasteboard, pasteboard.getController().getUndoStack());
	}
	// **-------------------------------------------------------------------------------
	private Pasteboard pasteboard;
	private ShapeFactory shapeFactory;		// refactoring shapes to a new file, because they all have different mouse handlers
	//@formatter:off
	private Model getModel()				{ 	return pasteboard.getController().getDrawModel();	}
	private Controller getController()		{ 	return pasteboard.getController();	}
	public ShapeFactory getShapeFactory()	{ 	return shapeFactory; }
	boolean showId = true;
	private UndoStack getUndoStack()	{ return pasteboard.getController().getUndoStack();	}
	
	//@formatter:on

//	private MNode makeMNode(AttributeMap attrMap)
//	{
	
//		String type = attrMap.get("Type");
//		if (type == null)
//			type = attrMap.get("ShapeType");
//		Tool tool = Tool.lookup(type);
//		if (tool == null) return null;
//		if (tool.isShape())
//			return shapeFactory.makeNewShape(tool, attrMap);
//		
//		if (Tool.isSVG(type))
//			return makeNewSVGPane(attrMap);
//		return makeNewNode(tool, attrMap);
//	}
	
//	private VNode makeNewSVGPane(AttributeMap attrMap) {
//		return null;
//	}
	
		
	// **-------------------------------------------------------------------------------
	/*
	 * 	convert a string representation into a node.  
	 * 
	 * param useCache:  determines whether to look for the node in the resource map.
	 * 					Shapes are generally recreated and not stored in the cache.
	 */
	public MNode parseGPML(String gpml, boolean useCache, Model m )
	{
		AttributeMap attrMap = new AttributeMap();
		attrMap.addDataNodeGPML(gpml);
		return new MNode(attrMap, m, pasteboard);
//		String type = attrMap.get("ShapeType");
//		Tool tool = Tool.fromString(type);
//		if (tool.isShape())		return shapeFactory.parseShape(attrMap);
//		if (tool.isControl())	return makeNewNode(tool, attrMap);
//		return null;
//		
	}
//	@Deprecated
//	public Node parseNode(String s, boolean useCache)
//	{
//		int attributeStart = s.indexOf('[');
//		if (attributeStart < 0) return null;
//		char firstChar = s.charAt(0);
//		int start = Character.isDigit(firstChar) ? 2 : 0;
//		String type = s.substring(start, attributeStart).trim();
//		String attributes = s.substring(attributeStart).trim();
//		if ("Text".equals(type) && attributes.startsWith("[text=\"\"")) return null;  // don't save unused labels
//		
//		
//		AttributeMap attrMap = new AttributeMap(attributes);
//		attrMap.put("type", type);
//		String id = attrMap.getId();
//		if ("null".equals(id))
//			id = attrMap.get("text");
//		if (useCache)
//		{
//			Node cached = getModel().getResource(id);
//			if (cached != null)
//			{
//				setAttributes(cached, attrMap);
//				return cached;
//			}
//		}
//
////		System.out.println("Everything should be cached!!");
//		Tool tool = Tool.fromString(type);
//		if (tool.isShape())		return shapeFactory.parseShape(attrMap);
//		if (tool.isControl())	return makeNewNode(tool, attrMap);
//		return null;
//	}

	public void setAttributes(Node shape, AttributeMap map)
	{
		if (verbose>0) System.out.println(map.toString());
		for (String k : map.keySet())
		{
			String val = map.get(k);
			if (k.equals("GraphId"))			shape.setId(val);
			double d = StringUtil.toDouble(val);			// exception safe:  comes back NaN if val is not a number
			if (shape instanceof Rectangle)
			{
				Rectangle r = (Rectangle) shape;
				if (k.equals("x"))				r.setX(d);
				else if (k.equals("y"))			r.setY(d);
				else if (k.equals("width"))		r.setWidth(d);
				else if (k.equals("height"))	r.setHeight(d);
			}
			if (shape instanceof Circle)
			{
				Circle circ = (Circle) shape;
				if (k.equals("centerX"))		circ.setCenterX(d);
				else if (k.equals("centerY"))	circ.setCenterY(d);
				else if (k.equals("radius"))	circ.setRadius(d);
			}
			if (shape instanceof Polygon)
			{
				Polygon poly = (Polygon) shape;
				if (k.equals("points"))			parsePolygonPoints(poly, map.get(k));
			}
			if (shape instanceof Polyline)
			{
				Polyline poly = (Polyline) shape;
				if (k.equals("points"))			parsePolylinePoints(poly, map.get(k));
			}
			if (shape instanceof Line)
			{
				Line line = (Line) shape;
				if (k.equals("startX"))			line.setStartX(d);
				else if (k.equals("startY"))	line.setStartY(d);
				else if (k.equals("endX"))		line.setEndX(d);
				else if (k.equals("endY"))		line.setEndY(d);
			}
			if (shape instanceof StackPane)
			{
				StackPane r = (StackPane) shape;
				if (k.equals("x"))				r.setLayoutX(d);
				else if (k.equals("y"))			r.setLayoutY(d);
				else if (k.equals("width"))		{ r.setMinWidth(d); r.setMaxWidth(d); r.prefWidth(d); }
				else if (k.equals("height"))	{ r.setMinHeight(d); r.setMaxHeight(d); r.prefHeight(d); }
				else if (k.equals("rotate"))	{ r.setRotate(d); }
				else if (k.equals("fill"))				
				{
					Background b = new Background(new BackgroundFill(Color.web(val), CornerRadii.EMPTY, Insets.EMPTY));
					r.setBackground(b);
				}
			}
			if (shape instanceof Shape)
			try
			{
				Shape sh = (Shape) shape;
				if (k.equals("fill") || k.equals("-fx-fill"))				
				{
					sh.setFill(Color.web(val));
					String lastTwoChars = val.substring(val.length()-2);
					int opac = Integer.parseInt(lastTwoChars, 16);
					shape.setOpacity(opac / 255.);
				}
				else if (k.equals("stroke")  || k.equals("-fx-stroke"))		sh.setStroke(Color.web(val));
				else if (k.equals("strokeWidth")  || k.equals("-fx-stroke-width"))	sh.setStrokeWidth(d);
//				else if (k.equals("selected"))		shape.setSelected(val);
			}
			catch (Exception e) { System.err.println("Parse errors: " + k); }
		}	
	}
	// **-------------------------------------------------------------------------------
	// Polygons and polylines are stored the same, but have different base types
	private void parsePolygonPoints(Polygon poly, String string)
	{
		parsePolyPoints(poly.getPoints(), string);
	}	
	private void parsePolylinePoints(Polyline poly, String string)
	{
		parsePolyPoints(poly.getPoints(), string);
	}	
	private void parsePolyPoints(ObservableList<Double> pts, String string)
	{
		String s = string.trim();
		s = s.substring(1, s.length()-1);
		String[] doubles = s.split(",");
		for (String d : doubles)
			pts.add(StringUtil.toDouble(d));
	}
	// **-------------------------------------------------------------------------------
	// **-------------------------------------------------------------------------------
	// **-------------------------------------------------------------------------------
//	private void fillVNode(MNode model, VNode stack, Node content)
//	{
//		AttributeMap attrMap = model.getAttributeMap();
//		stack.fill(attrMap, content);
//	}
	
//	
//	private VNode makeVNode(StackPane orig, String title, String id)
//	{
//		double x = orig.getLayoutX();
//		double y = orig.getLayoutY();
//		double w = orig.getWidth();
//		double h = orig.getHeight();
//		AttributeMap attrs = new AttributeMap(orig);
//		MNode model = new MNode(attrs, getModel(), pasteboard);
//		VNode stack = new VNode(model, pasteboard);
//		int nKids = orig.getChildren().size();
//		if (nKids == 2)
//		{
//			Node content = orig.getChildren().get(1);
////			System.out.println("content = " + content.toString());
//			stack.fill(x,y,w,h,title, id);
//			if (content instanceof ImageView)
//			{
//				ImageView iview = new ImageView(((ImageView)content).getImage());
//				iview.setMouseTransparent(true);
//				iview.fitWidthProperty().bind(Bindings.subtract(orig.widthProperty(), 20));
//				iview.fitHeightProperty().bind(Bindings.subtract(orig.heightProperty(), 40));
//				iview.setTranslateY(-10);
//				stack.addContent(iview);
//			}
//			if (content instanceof TextArea)
//			{
//				TextArea view = new TextArea(((TextArea)content).getText());
//				view.setPrefColumnCount(60);
//				view.setPrefRowCount(20);
//				view.setMouseTransparent(true);
//				stack.addContent(view);
//			}
//				
//			if (content instanceof WebView)
//			{
//				String location = ((WebView)content).getEngine().getLocation();
//				WebView view = new WebView();
//				view.getEngine().load(location);
//				stack.addContent(view);
//			}
//				
//			if (content instanceof TableView)
//			{
//				TableView<ObservableList<StringProperty>> table = new TableView<ObservableList<StringProperty>>();
//				stack.addContent(table);
//			}
//		}
//		return stack;
//	}
	
	// **-------------------------------------------------------------------------------
	public void addGroupMouseHandlers(Group g)
	{
	}

	private void handleDrop(DragEvent e)
	{	
		Dragboard db = e.getDragboard();
		e.acceptTransferModes(TransferMode.ANY);
		Set<DataFormat> formats = db.getContentTypes();
		formats.forEach(a -> System.out.println("getContentTypes " + a.toString()));
		if (db.hasFiles())
		{
			List<File> files = db.getFiles();
			if (files != null)
			{
//				controller.getUndoStack().push(ActionType.Add, " file");
				int offset = 0;
				for (File f : files)
				{
					offset += 20;
					if (verbose > 2)
						System.out.println("File: " + f.getAbsolutePath());
					if (FileUtil.isCSS(f))
					{
						String path = f.getAbsolutePath();
						int idx = path.indexOf("/know");
						if (idx > 0)
							path = path.substring(idx);
						
						StringBuilder buff = new StringBuilder();
						FileUtil.readFileIntoBuffer(f, buff);
						Node n = (Node) e.getTarget();
						String styl = buff.toString();
//						n.getStyleClass().add(styl);
						if (verbose > 3)
							System.out.println("Style: " + styl);
						n.getScene().getStylesheets().add(path);
				
					}
				}
			}
		}
		e.consume();
	}
// **-------------------------------------------------------------------------------
//	private static final String STYLE_CLASS_SELECTION_BOX = "chart-selection-rectangle";


}