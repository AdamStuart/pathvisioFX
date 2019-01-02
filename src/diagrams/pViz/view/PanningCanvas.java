package diagrams.pViz.view;

import java.util.List;

import animation.NodeVisAnimator;
import diagrams.pViz.app.Controller;
import diagrams.pViz.app.Selection;
import diagrams.pViz.util.GraphEditorProperties;
import gui.Backgrounds;
import javafx.beans.property.DoubleProperty;
import javafx.collections.ObservableList;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;
import javafx.stage.Screen;

public class PanningCanvas extends Pane {
	static public int CANVAS_WIDTH = 5000;
	static public int CANVAS_HEIGHT = 5000;
	protected Controller controller;
	protected Label zoomScale;
	
	public PanningCanvas(Controller ctrl, Label zoom) {
		setWidth(CANVAS_WIDTH);
		setHeight(CANVAS_HEIGHT);
		zoomScale = zoom;
		setId("root");
		controller = ctrl;
		editorProperties = new GraphEditorProperties();
		marquee = ShapeFactory.makeMarquee();
		createBackground();
		getChildren().addAll(getBackgroundLayer(), getGridLayer(), getContentLayer());
		activeLayerName = "Content";
		selectionMgr = new Selection(this);
		
        scaleYProperty().bind(scaleProperty());
        scaleProperty().addListener(e -> resetGrid());
	}
	
	private void createBackground() {
		Rectangle r = new Rectangle(0, 0);
		r.widthProperty().bind(widthProperty());
		r.heightProperty().bind(heightProperty());
		r.setId("Background");
		r.setFill(Backgrounds.whiteGradient);
		r.setMouseTransparent(true);
		r.getProperties().put("Layer", "Background");
		backgroundLayer.add(r);
	}
	
	public void restoreBackgroundOrder() {
		gridLayer.getLayer().toBack();
		backgroundLayer.getLayer().toBack();
//		contentLayer.getLayer().sort();
	}

	public static boolean isMarquee(Node node) {
		return node != null && "Marquee".equals(node.getId());
	}	public Controller getController()		{ return controller; }
	protected Selection selectionMgr;
	public Selection getSelectionMgr()		{ return selectionMgr; }
	protected Rectangle marquee;
	public Rectangle getMarquee()			{ return marquee;	}
	protected Label infoLabel;
	public Label getInfoLabel()				{ return infoLabel;	}
	private LayerRecord backgroundLayer = new LayerRecord("Background");
	private LayerRecord gridLayer = new LayerRecord("Grid");
	private LayerRecord contentLayer = new LayerRecord("Content");

	public Layer getBackgroundLayer()		{ return backgroundLayer.getLayer();	}
	public Layer getGridLayer()				{ return gridLayer.getLayer();	}
	public Layer getContentLayer()			{ return contentLayer.getLayer();	}
	LayerRecord[] allLayers = { contentLayer, backgroundLayer, gridLayer };
	
	public LayerRecord getBackgroundLayerRecord()		{ return backgroundLayer;	}
	public LayerRecord getGridLayerRecord()				{ return gridLayer;	}
	public LayerRecord getContentLayerRecord()			{ return contentLayer;	}
	
	protected VNode activeStack;
	public VNode getActiveStack()		{ return activeStack;	}
	public Shape getActiveShape()		{ return activeStack == null ? null : activeStack.getFigure();	}
	public void setActiveStack(VNode s)	{ activeStack = s;	}

    protected GraphEditorProperties editorProperties = new GraphEditorProperties();
	public GraphEditorProperties getEditorProperties() {	return editorProperties;	}
	//-------------------------------------------------------------------------------
	protected Group grid;
	protected Rectangle vGrid, hGrid;
//	public Group getGrid()	{  return grid;  }
//	List<Line> hLines;
//	List<Line> vLines;

	public void makeGrid(Button toggler, ScrollPane scrlPane)
	{
		grid = new Group();
		grid.setId("grid");
		
		
		//AM a way to make grid without lines, using gradients.
		//Avoids issues with grid lines influencing canvas size
		//Vertical grid lines
		vGrid = new Rectangle(0, 0);
		vGrid.widthProperty().bind(widthProperty());
		vGrid.heightProperty().bind(heightProperty());
		
		
		//Horizontal grid lines
		hGrid = new Rectangle(0, 0);
		hGrid.widthProperty().bind(widthProperty());
		hGrid.heightProperty().bind(heightProperty());
		
		
		grid.getChildren().add(vGrid);
		grid.getChildren().add(hGrid);
		
		
		
//		if (hLines == null) hLines = new ArrayList<Line>();
//		if (vLines == null) vLines = new ArrayList<Line>();
		
		resetGrid();
		grid.setMouseTransparent(true);
		getController().addExternalNode(grid);
		gridLayer.add(grid);
		new NodeVisAnimator(grid, toggler);

	}
	public void showGrid(boolean vis)	{	grid.setVisible(vis);	}
	public boolean isGridVisible()		{	return	grid.isVisible();	}
	
	public void resetGrid()	
	{
		int res = (int) Screen.getPrimary().getDpi();			// assumes inches
		
		//AM for alternate gradient gridline method
		hGrid.setFill(Backgrounds.getGridLinesPaint(res, 1.0 / getScale(), Color.LIGHTGRAY, false));
		vGrid.setFill(Backgrounds.getGridLinesPaint(res, 1.0 / getScale(), Color.LIGHTGRAY, true));
		
		
		//AM using Line objects for the grid is causing the canvas to expand to fit the lines
//		double canvasWidth = getWidth();
//		double canvasHeight = getHeight();
//		int numVLines = (int) (canvasWidth / res);
//		int numHLines = (int) (canvasHeight / res);
//		
//		for (int i = 1; i<=numVLines; i++)
//		{
//			if (i > vLines.size())
//				vLines.add(makeGridLine());
//			LineUtil.set(vLines.get(i-1), res * i, 0, res * i, canvasHeight - 1);
//		}
//		for (int i=1; i<=numHLines; i++) {
//			if (i > hLines.size())
//				hLines.add(makeGridLine());
//			LineUtil.set(hLines.get(i-1), 0, res * i, canvasWidth - 1, res * i);
//		}
	}
	
//	protected Line makeGridLine() {
//		Line line = new Line();
//		grid.getChildren().add(line);
//		line.strokeWidthProperty().bind(new SimpleDoubleProperty(.25).divide(scaleProperty()));
//		return line;
//	}
	
	//-----------------------------------------------------------------------------
	
	//AM scaleY is bound to scaleX in the constructor
	public DoubleProperty scaleProperty() { return scaleXProperty();	}
    public double getScale() 			{        return getScaleX();    }
    public void setScale( double scale) {        setScaleX(scale);  updateScaleFeedback(); }
    public void updateScaleFeedback()	{ 		if (zoomScale != null) zoomScale.setText((int) (getScale()*100) + "%"); }
    

	public void zoomIn() {
		
		if (getScale() < 4)
			setScale(getScale() * 2);		
	}
	public void zoomOut() {
		
		if (getScale() > 0.0125)
			setScale(getScale() / 2);		
	}

	//-----------------------------------------------------------------------------
	public void clear()						
	{ 	
		for (LayerRecord lay : allLayers) 
			if (!lay.getName().contains("Grid")) 
				lay.getLayer().clear();	
	}
	public void clearLayer()				{ 	getActiveLayer().clear();	}
	
	//-----------------------------------------------------------------------------
	public void add(Node node)				{	getActiveLayer().add(node);	}
	public void remove(Node node)			{	getActiveLayer().remove(node);	}

	public void add(int idx, Node node, String layername)		
	{	
		node.getProperties().put("Layer", layername); 	
		getLayer(layername).add(idx, node);	
	}
	public void addAll(Node[] n) 	
	{	
		for (Node node : n )
			node.getProperties().put("Layer", activeLayerName); 
		getChildren().addAll(n);
	}
	
	public void addAllVNodes(VNode[] n) 	{	for (VNode node : n) add(node);	}
	public void addAllVNodes(List<VNode> n) {	for (VNode node : n) add(node);	}
	
	//-----------------------------------------------------------------------------
	private String activeLayerName = "Content";
	public String activeLayerName()			{ 	return activeLayerName; }
	public void setActiveLayer(String s)	{  	activeLayerName = s; }

	Layer getActiveLayer() 					
	{ 	
		LayerRecord rec =  controller.getLayerRecord(activeLayerName);
		if (rec == null)	rec = contentLayer;
		return rec.getLayer();	
	}

	public Layer getLayer(String string) {
		if ("Background".equals(string)) 		return getBackgroundLayer();
		if ("Grid".equals(string))				return getGridLayer();
		LayerRecord rec = controller.getLayerRecord(string);
		return rec == null ? getContentLayer() : rec.getLayer();
	}
	
	public void addLayer(LayerRecord newLayer) {		getChildren().add(newLayer.getLayer());	}

	protected List<Node> getChildrenInLayer(String layername) {
		
		Layer layer =  getLayer(layername);
		return  (layer == null)? null : layer.getNodes();
	}
	
	String getLayerName(Node n)
	{
		if (n == null) return "";
		if (n instanceof VNode) 
			return ((VNode) n).getLayerName();
		Object nodeLayer = getProperties().get("Layer");
		if ( nodeLayer == null) 
		{ 
			System.err.println("getLayerName error: " + n);
			return""; 
		}
		return nodeLayer.toString();
	}

	public void resetLayerOrder(ObservableList<LayerRecord> items) {
		for (LayerRecord rec : items)
		{
			System.err.println("raising layer: " + rec.getName());
			Layer layer = getLayer(rec.getName());
			if (layer != null)
				layer.toFront();
		}
	}

}
