package diagrams.pViz.view;

import java.util.ArrayList;
import java.util.List;

import animation.NodeVisAnimator;
import diagrams.pViz.app.Controller;
import diagrams.pViz.app.Selection;
import diagrams.pViz.util.GraphEditorProperties;
import gui.Backgrounds;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.collections.ObservableList;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;
import javafx.stage.Screen;
import util.LineUtil;

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
	}
	
	private void createBackground() {
		Rectangle r = new Rectangle(-10, -10, CANVAS_WIDTH + 20, CANVAS_HEIGHT + 20);
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
//	public Group getGrid()	{  return grid;  }
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
			getController().addExternalNode(grid);
		}
		gridLayer.add(grid);
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
	
	//-----------------------------------------------------------------------------

    DoubleProperty myScale = new SimpleDoubleProperty(1.0);

  
    public void updateScaleFeedback()	{ 		if (zoomScale != null) zoomScale.setText((int) (getScale()*100) + "%"); }
    public double getScale() 			{        return myScale.get();    }
    public void setScale( double scale) {        myScale.set(scale);  updateScaleFeedback(); }

    public void setPivot( double x, double y) {
        setTranslateX(getTranslateX()-x);
        setTranslateY(getTranslateY()-y);
    }

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
