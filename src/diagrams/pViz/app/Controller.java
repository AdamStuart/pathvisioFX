package diagrams.pViz.app;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.ResourceBundle;

import animation.BorderPaneAnimator;
import diagrams.pViz.dialogs.LegendDialog;
import diagrams.pViz.gpml.GPML;
import diagrams.pViz.model.DataNode;
import diagrams.pViz.model.DataNodeGroup;
import diagrams.pViz.model.DataNodeState;
import diagrams.pViz.model.Edge;
import diagrams.pViz.model.EdgeLine;
import diagrams.pViz.model.EdgeType;
import diagrams.pViz.model.GeneModel;
import diagrams.pViz.model.Interaction;
import diagrams.pViz.model.Model;
import diagrams.pViz.tables.LegendRecord;
import diagrams.pViz.tables.PathwayController;
import diagrams.pViz.tables.ReferenceController;
import diagrams.pViz.util.WebUtil;
import diagrams.pViz.view.GroupMouseHandler;
import diagrams.pViz.view.Inspector;
import diagrams.pViz.view.Layer;
import diagrams.pViz.view.LayerController;
import diagrams.pViz.view.LayerRecord;
import diagrams.pViz.view.Pasteboard;
import diagrams.pViz.view.VNode;
import gui.Action;
import gui.Action.ActionType;
import gui.Borders;
import gui.UndoStack;
import icon.FontAwesomeIcons;
import icon.GlyphIcon;
import icon.GlyphIcons;
import icon.GlyphsDude;
import javafx.application.Platform;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.ObservableList;
import javafx.event.EventTarget;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.geometry.Side;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SplitPane;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTableView;
import javafx.scene.image.Image;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DataFormat;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.util.Pair;
import model.AttributeMap;
import model.IController;
import model.bio.BiopaxRecord;
import model.bio.Gene;
import model.bio.GeneSetRecord;
import model.bio.Species;
import model.bio.XRefable;
import util.FileUtil;
import util.StringUtil;

//https://dzone.com/articles/editable-tables-in-javafx

public class Controller implements Initializable, IController 
{
	public static final DataFormat COLUMN_MIME_TYPE = new DataFormat("application/x-java-serialized-column");
	public static final DataFormat GENE_MIME_TYPE = new DataFormat("application/x-java-serialized-gene");

	//@formatter:off
	private Model model;
	public Model getDrawModel()   		{ 		return model;  }
	public Model getModel() 			{		return model;	}	
	private GeneModel geneModel;
	public GeneModel getGeneModel()   		{ 		return geneModel;  }
	private Pasteboard pasteboard;
	public Pasteboard getPasteboard()   { 		return pasteboard;  }
	private UndoStack undoStack;
	public UndoStack getUndoStack()		{		return undoStack;	}
	private Document doc;
	private String species = "Unspecified";

	int verbose = 1;
	private AnchorPane propertyPanel = null;
	private PathwayController pathwayController = null;
	private GeneSetRecord geneSetRecord;		// the model

	//------------------------------------------
	//------------------------------------------
	static Image dragImage;
	@FXML private void dragControl(MouseEvent e)
	{
		EventTarget targ  = e.getTarget();
		Button b = null;
		if (targ instanceof Text)
		{
			Text text = (Text) targ;
			Node parent = text.getParent();
			if (parent instanceof Button)
				b = (Button) parent;
		}
		else if (targ instanceof Button)
			b = (Button) targ;
			
		if (b != null)
		{
			System.out.println("drag " + b.getText());
	        Dragboard db = b.startDragAndDrop(TransferMode.COPY);
	        db.setDragView(b.snapshot(null, null), e.getX(), e.getY());
	        ClipboardContent cc = new ClipboardContent();
	        cc.putString(b.getText());
	        db.setContent(cc);
		}
	}
	// **-------------------------------------------------------------------------------
	// **-------------------------------------------------------------------------------
	// **-------------------------------------------------------------------------------
	@FXML protected TreeTableView<XRefable> nodeTable;
	@FXML protected TreeTableColumn<TreeTableView<XRefable>,XRefable> nodeColumn;
	@FXML protected TreeTableColumn<TreeTableView<XRefable>,XRefable> graphid;
	@FXML protected TreeTableColumn<TreeTableView<XRefable>,XRefable> xrefdb;
	@FXML protected TreeTableColumn<TreeTableView<XRefable>,XRefable> xrefid;
	
	@FXML private void selectProtein()	{ 	pasteboard.selectByType("Protein");	}
	@FXML private void selectPathway()	{ 	pasteboard.selectByType("Pathway");	}
	@FXML private void selectGene()		{ 	pasteboard.selectByType("GeneProduct");		}
	@FXML private void selectMetabolite(){ 	pasteboard.selectByType("Metabolite");		}
	@FXML private void selectRna()		{ 	pasteboard.selectByType("Rna");		}
	@FXML private void selectLabels()	{ 	pasteboard.selectByType("Label");		}
	@FXML private void selectShapes()	{ 	pasteboard.selectByType("Shape");		}
	@FXML private void selectEdges()	{ 	pasteboard.selectByType("Edge");		}
	@FXML private void moveToLayer()	{ 	System.err.println("moveToLayer called, not its subitem");		}
//	@FXML private void addKeyFrame()	{ 	inspector.addKeyFrame();	}

	@FXML private void undo()			{ 	undoStack.undo();	}
	@FXML private void redo()			{ 	undoStack.redo();		}	
	// **-------------------------------------------------------------------------------
	@FXML private void doNew()			{ 	App.getInstance().doNew();			}
	@FXML private void open()			{ 	doc.open();	}			//doc.open();
	public void open(File f)			{ 	doc.open(f);	}			//doc.open();
	public void open(String s)			{ 	doc.open(s);	}			//doc.open();
	@FXML private void save()			{ 	doc.save();	}
	@FXML private void saveas()			{	doc.saveas();		}
	@FXML private void submit() 		{ 	doc.submit();		} 
	@FXML private void close()			
	{ 
		doc.close();	
		if (pasteboard != null)
		{
			Window w = pasteboard.getScene().getWindow();
			if (w instanceof Stage)
				((Stage) w).close();
		}
	}
	static String jupy = "https://nbviewer.jupyter.org/github/CamDavidsonPilon/Probabilistic-Programming-and-Bayesian-Methods-for-Hackers/blob/master/Chapter1_Introduction/Ch1_Introduction_PyMC3.ipynb";
	@FXML private void showJupyter()	{ 	WebUtil.showURL(jupy);	}
	@FXML private void print()			{ 	doc.print();			}
	@FXML private void quit()			{ 	Platform.exit();			}
//	@FXML private void clickDrawPane()	{ 	System.out.println("clicked");			}
	// **-------------------------------------------------------------------------------
	@FXML private  void cut()			{ 	undoStack.push(ActionType.Cut);		getSelectionManager().cut();	}
	@FXML private  void copy()			{ 	getSelectionManager().copy();		}	// not undoable
	@FXML private  void paste()			{ 	undoStack.push(ActionType.Paste);	doPaste();		}
	@FXML private  void clearUndo()		{	undoStack.clear(); 	}
	@FXML private void selectAll()		{ 	undoStack.push(ActionType.Select); getSelectionManager().selectAll(); 		}
	@FXML public void deleteSelection(){ 	undoStack.push(ActionType.Delete);	getSelectionManager().deleteSelection(); 	}
	@FXML public void duplicateSelection(){ undoStack.push(ActionType.Duplicate);	getSelectionManager().cloneSelection(0); 	}
	@FXML public void clear()			{ 	undoStack.push(ActionType.Delete);	getSelectionManager().deleteAll(); 	}
	
	@FXML private void resetEdgeTable()	{		model.resetEdgeTable();	}
	@FXML private void dumpEdgeTable()	{		model.dumpEdgeTable();	}
	@FXML private void dumpViewHierarchy()	{	model.dumpViewHierarchy();	}
	@FXML private void dumpNodeTable()	{		model.dumpNodeTable();	}
	@FXML private void showNodeList()	{ 		}
	// **-------------------------------------------------------------------------------
	@FXML public  void group()			{ 	undoStack.push(ActionType.Group);	getSelectionManager().doGroup();  }
	@FXML public  void ungroup()		{ 	undoStack.push(ActionType.Ungroup);	getSelectionManager().ungroup(); }
	@FXML public  void toFront()		{	undoStack.push(ActionType.Reorder);	getSelectionManager().toFront(); 	}
	@FXML public  void toBack()			{	undoStack.push(ActionType.Reorder);	getSelectionManager().toBack();  pasteboard.restoreBackgroundOrder();  	}
	@FXML public  void zoomIn()			{	undoStack.push(ActionType.Zoom);	pasteboard.zoomIn(); 	}
	@FXML public  void zoomOut()		{	undoStack.push(ActionType.Zoom);	pasteboard.zoomOut();  pasteboard.restoreBackgroundOrder();  	}
	@FXML public  void toLayer()		{	//TODO
	}
	public List<LayerRecord> getLayers()	{	return layerController.getLayers();	}
	public void moveSelectionToLayer(String layername)
	{
		undoStack.push(ActionType.Reorder);	  
		getSelectionManager().setLayer(layername);
		pasteboard.restoreBackgroundOrder();  	
	}
//	@FXML private void hideAnchors()	{ 	model.setAnchorVisibility(false);	}
//	@FXML private void showAnchors()	{ 	model.setAnchorVisibility(true);	}
	@FXML public  void browsePathways()		{	App.browsePathways(this);	}	
	@FXML	private void clearColors() { model.clearColors();}

	public Species getSpecies()	{ return model.getSpecies();	}
	public String getActiveArrowType() {	return activeArrowType;	}
	public String getActiveLineType() {		return activeLineType;	}

	String activeLineType = "Straight";
	String activeArrowType = "Arrow1";

	@FXML private void setStraight()	{		activeLineType = "Straight";	}
	@FXML private void setCurved()		{		activeLineType = "Curved";	}
	@FXML private void setElbowed()		{		activeLineType = "Elbow";	}
	public EdgeType getCurrentLineBend() { return EdgeType.lookup(activeLineType);		}
	@FXML private void setArrow1()		{		activeArrowType = "Arrow1";	}
	@FXML private void setArrow2()		{		activeArrowType = "Arrow2";	}
	@FXML private void setArrow3()		{		activeArrowType = "Arrow3";	}
	@FXML private void setArrow4()		{		activeArrowType = "Arrow4";	}
	@FXML private void setArrow5()		{		activeArrowType = "Arrow5";	}
	@FXML private void setArrow6()		{		activeArrowType = "Arrow6";	}
	
	@FXML private MenuItem submit;			// save to server

	
	@FXML private MenuItem connect;			// TODO bind enableProperty to selection size > 2
	//@formatter:on

	@FXML private void addEdges()		
	{ 	
		if (getSelection().size() >= 2)		
		{
			List<Interaction> edges = getDrawModel().connectSelectedNodes();
			if (edges.size() > 0)
			{
				for (Interaction e: edges)
					addInteraction(e);
				pasteboard.resetGrid();
				for (Edge e : edges)
					e.connect();
			}
		}
	}
	
//	@FXML private VBox east;
	@FXML private VBox south;
	@FXML private Pane drawPane;
	@FXML private ScrollPane scrollPane;
	@FXML private ListView<Action> undoview = null;
//	private GeneListTable geneListTable = null;
	@FXML private BorderPane container;			// root of fxml	
	@FXML private Button tableOptions;
	@FXML private ToggleButton gene;	
	@FXML private ToggleButton metabolite;	
	@FXML private ToggleButton protein;		
	@FXML private ToggleButton pathway;		
	@FXML private ToggleButton rna;	
	@FXML private Button bottomSideBarButton;
	@FXML private Button leftSideBarButton;
	@FXML private Button rightSideBarButton;
	@FXML private Button toggleGridButton;
	
	//-------------------------------------------------------------
	@FXML private CheckBox showGraphId;	
	@FXML private CheckBox snapToGrid;	
	@FXML private CheckBox showZOrder;	
	@FXML private CheckBox showAnchors;	
	@FXML private CheckBox showLocks;	
	@FXML private void	showZOrder() 	{		zOrderVisible.set(showZOrder.isSelected());	}
	@FXML private void	setSnapToGrid() {		snapToGridProperty.set(snapToGrid.isSelected());  }
	@FXML private void	showGraphId() 	{		graphIdsVisible.set(showGraphId.isSelected());	} 
	@FXML private void	showAnchors() 	{		anchorVisibleProperty.set(showAnchors.isSelected());	}
	@FXML private void	showLocks() 	{		lockVisibleProperty.set(showLocks.isSelected());	}

	SimpleBooleanProperty graphIdsVisible = new SimpleBooleanProperty(false);
	public SimpleBooleanProperty graphIdsVisibleProperty() { return graphIdsVisible; }
	SimpleBooleanProperty zOrderVisible = new SimpleBooleanProperty(false);
	public SimpleBooleanProperty zOrderVisibleProperty() { return zOrderVisible; }
	SimpleBooleanProperty snapToGridProperty = new SimpleBooleanProperty(false);
	public SimpleBooleanProperty snapToGridProperty() { return snapToGridProperty; }
	SimpleBooleanProperty anchorVisibleProperty = new SimpleBooleanProperty(false);
	public SimpleBooleanProperty anchorVisibleProperty() { return anchorVisibleProperty; }
	SimpleBooleanProperty lockVisibleProperty = new SimpleBooleanProperty(false);
	public SimpleBooleanProperty lockVisibleProperty() { return lockVisibleProperty; }
//-------------------------------------------------------------
	@FXML private MenuItem annotate;
	@FXML private void	annotate() 	{	model.annotateIdentifiers(); 	}
	@FXML private MenuItem selectEdges;
	@FXML private MenuItem undo;
	@FXML private MenuItem redo;
	@FXML private MenuItem clearundo;
	@FXML private VBox west;
	@FXML private HBox bottomPadding;
	@FXML private BorderPane 	loadContainer;

	//------------------------------------------
	@FXML private Label select;
	@FXML private Label size;
	@FXML private SplitPane hsplitter;
	

	// **-------------------------------------------------------------------------------
	// **-------------------------------------------------------------------------------
	// **-------------------------------------------------------------------------------
	
	@Override public void resetTableColumns() {	}
	@Override public void reorderColumns(int a, int b) {	}

	public static String CSS_Gray2 = "-fx-border-width: 2; -fx-border-color: blue;";
	static String CSS_cellBackground(boolean undone) 	{		return "-fx-background-color: " + (undone ? "GREY; " : "BEIGE; ");	}
	static String ctrlStr = "fx:id=\"%s\" was not injected: check your FXML file '%s'.";
	static String missing(String s)	{		return String.format(ctrlStr, s, "AttributeValueFXML.fxml");	}
	
	// **-------------------------------------------------------------------------------
	public Selection getSelectionManager() 		{ 	return pasteboard.getSelectionMgr();  }
	public ObservableList<VNode> getSelection() { 	return getSelectionManager().getAll();  }
	private Stage stage;
	public AnchorPane getProperties() { return propertyPanel;	}
	private Inspector inspector;
	public Inspector getInspector() { return inspector;	}
	@FXML private void showInspector() {
		bottomSideBarButton.fire();
	}
	// **-------------------------------------------------------------------------------
	GPMLTreeTableView nodeTreeTable;
	public GPMLTreeTableView getTreeTableView() { return nodeTreeTable; }
	@Override public void initialize(URL location, ResourceBundle resources)
	{
		undoStack = new UndoStack(this, null);
		assert(drawPane != null);
		model = new Model(this);
		geneModel = new GeneModel(this);
		pasteboard = new Pasteboard(this);
		drawPane.getChildren().add(pasteboard);
		doc = new Document(this);
		setupInspector();		
		String cssURL = this.getClass().getResource("styles.css").toExternalForm();
		pasteboard.getStylesheets().add(cssURL);
		stage = App.getInstance().getStage();
//		drawContainer.setBorder(Borders.etchedBorder);
		scrollPane.setOnScroll(ev -> {
			ev.consume();
	        if (ev.getDeltaY() == 0)   return;	
		});
//		bottomDash.setBorder(Borders.dashedBorder );
		if (bottomPadding!= null)
			bottomPadding.setPadding(new Insets(3,3,4,4));

		if (west != null) 		west.setBorder(Borders.lineBorder);
		setupPalette();
		setupListviews();
		new BorderPaneAnimator(container, leftSideBarButton, Side.LEFT, false, 135);
		new BorderPaneAnimator(container, bottomSideBarButton, Side.BOTTOM, false, 150);
		rightSideBarButton.setOnAction(event -> {	toggleHSplitter();	});
		rightSideBarButton.fire();
		bottomSideBarButton.fire();
		rightSideBarButton.setGraphic(GlyphsDude.createIcon(FontAwesomeIcons.LOCATION_ARROW, GlyphIcon.DEFAULT_ICON_SIZE));
		rightSideBarButton.setText("");

		pasteboard.makeGrid(toggleGridButton, scrollPane);	
		Pair<FXMLLoader, Stage> loader = App.getInstance().makeLoader();
		
		Pair<FXMLLoader, Stage> pair = App.buildStage("Layers", "../view/Layers.fxml", 250, 300);
		layerController = (LayerController) pair.getKey().getController();
		layerController.setParentController(this);
		Stage stage = pair.getValue();
		layerController.setStage(stage);
		stage.hide();
		stage.setOnCloseRequest(ev -> { stage.hide(); } );
		stage.setAlwaysOnTop(true);

		loader.getValue().toFront();
		boolean startWithShapes = false;
		if (startWithShapes) test1();
		IController.setGraphic(bottomSideBarButton, FontAwesomeIcons.ARROW_CIRCLE_DOWN);
		nodeTreeTable = new GPMLTreeTableView(nodeTable, this);
		nodeTreeTable.setup(size,select);
		resetTableColumns();
        Thread postponed =  new Thread(() -> Platform.runLater(() -> {  loader.getValue().toFront();	} )  );
        postponed.start();
        toggleHSplitter();
	}
	boolean open = true;
	private void toggleHSplitter() {
			if (hsplitter != null) hsplitter.setDividerPosition(0, open ? 0.5 : 1.0);
			open = !open;
	}

	private void setupInspector() {
		URL res = getClass().getResource("../view/Inspector.fxml");
	    FXMLLoader inspectorLoader = new FXMLLoader(res);
	   try
	   {
		   inspectorLoader.load();
		   inspector = inspectorLoader.getController();
		   inspector.setParentController(this);
		   container.setBottom(inspector);
	   }
	   catch (Exception e) 
	   { System.err.println("Inspector failed to load");
	   e.printStackTrace();}

	}

	public void doNewGeneList()	{		App.doNewGeneList(model, geneModel);	}
	

	public void getInfo(DataFormat mimeType, String idx, String colname, MouseEvent ev) {
		try 
		{
			int index = StringUtil.toInteger(idx);
			if (mimeType == Controller.GENE_MIME_TYPE)
			 {
			   String rowName = "";
			   if (index >=0 && model.getDataNodeMap().size() > index)
				   	rowName = model.getDataNodeMap().get(index).getGraphId();
//			   showGeneInfo(rowName);
			   return;
		  }
		  if (mimeType == PathwayController.PATHWAY_MIME_TYPE)
		   {
			   if (pathwayController != null)
			   {
				   pathwayController.viewPathwayByIndex(idx);
				   return;
			   }
			}
		  System.out.println("getInfo wasn't matched: " + mimeType.toString());					//TODO
		}
		catch (Exception e) { e.printStackTrace();}
	}
	// ------------------------------------------------------
	final ContextMenu tableColumnOptions = new ContextMenu();
	String[] colnames = new String[] { "Id", "Reference", "Database", "Authors", "Title", "Source", "Year" };

	@FXML private void doTableOptionsPopup()
	{
		ObservableList<MenuItem> items = tableColumnOptions.getItems();
		if (items.size() == 0)
		{
			for (String c : colnames)
				items.add(new CheckMenuItem(c));
		}
			
		tableColumnOptions.setX(tableOptions.getLayoutX() + stage.getX() - 60); 
		tableColumnOptions.setY(tableOptions.getLayoutY() + stage.getY()+ 40);
		tableColumnOptions.show(stage);
	}
	//-----------------------------------------------------------------------
	@FXML private void doAbout()
	{
//		AboutDialog dlog = new AboutDialog();
//		dlog.showAndWait();
	}
	//-----------------------------------------------------------------------
	@FXML private void addLegend() {		LegendRecord.makeLegend("Legend", "", true, true, false, model, this, true);	}
	@FXML private void doNewMultiGeneList()  { 	App.doNewMultiGeneList(); 	}
	@FXML private void addLegendDlog()
	{
		LegendDialog dlog = new LegendDialog();
		Optional<String> value = dlog.showAndWait();
		if (value.isPresent())
			LegendRecord.makeLegend(dlog.resultProperty(), model, this);
	}
	
	//-----------------------------------------------------------------------
	LayerController layerController;
	@FXML private void showLayers()
	{
		if (layerController.getStage().isShowing())
			layerController.getStage().hide();
		else layerController.getStage().show();
	}
	public LayerRecord getLayerRecord(String name)	{		return layerController.getLayer(name);	}
	@FXML private void addLayer()
	{
		if (layerController != null)
			layerController.addLayer();
	}
	@FXML private void removeLayer()
	{
		if (layerController != null)
			layerController.removeLayer();
	}
	//-----------------------------------------------------------------------
	@FXML private void test1()	{		Test.test1(this);	}
	 @FXML private void test2()	{		Test.test2(this);	}
	 @FXML private void test3()	{		Test.test3(this);	}

	// **-------------------------------------------------------------------------------
	private void setGraphic(ToggleButton b, Tool t, GlyphIcons i)
	{
		b.setGraphic(GlyphsDude.createIcon(i, GlyphIcon.DEFAULT_ICON_SIZE));
		b.setId(t.name());
	}
	// **-------------------------------------------------------------------------------
	
	private ToggleGroup paletteGroup;
	private ToggleGroup lineTypeGroup;
	private ToggleGroup arrowGroup;
	public ToggleGroup getToolGroup()			{ 	return paletteGroup;	}
	public ToggleGroup getArrowGroup()			{ 	return arrowGroup;	}
	public ToggleGroup getLineTypeGroup()		{ 	return lineTypeGroup;	}
	// **-------------------------------------------------------------------------------
		// Tool palette
		@FXML private void setArrow()		{ pasteboard.setTool(Tool.Arrow);	}
		@FXML private void setRectangle()	{ pasteboard.setTool(Tool.Rectangle);}	// TODO capture double click for stickiness
		@FXML private void setText()		{ pasteboard.setTool(Tool.Text);}
		@FXML private void setOval()		{ pasteboard.setTool(Tool.Circle);		}
		@FXML private void setPolygon()		{ pasteboard.setTool(Tool.Polygon);	}
		@FXML private void setPolyline()	{ pasteboard.setTool(Tool.Polyline);	}
		@FXML private void setLine()		{ pasteboard.setTool(Tool.Line);	}
		@FXML private void setShape1()		{ pasteboard.setTool(Tool.Shape1);	}
		@FXML private void setBrace()		{ pasteboard.setTool(Tool.Brace);	}
		@FXML private void setGene()		{ pasteboard.setTool(Tool.GeneProduct);	}
		@FXML private void setMetabolite()	{ pasteboard.setTool(Tool.Metabolite);	}
		@FXML private void setProtein()		{ pasteboard.setTool(Tool.Protein);	}
		@FXML private void setRNA()			{ pasteboard.setTool(Tool.Rna);	}
		@FXML private void setPathway()		{ pasteboard.setTool(Tool.Pathway);	}

		@FXML private ToggleButton arrow;
		@FXML private ToggleButton rectangle;
		@FXML private ToggleButton circle;
		@FXML private ToggleButton text;
		@FXML private ToggleButton polygon;
		@FXML private ToggleButton polyline;
		@FXML private ToggleButton line;
		@FXML private ToggleButton shape1;
//			@FXML private ToggleButton shape2;
		@FXML private ToggleButton straight;
		@FXML private ToggleButton curved;
		@FXML private ToggleButton elbowed;
			
		@FXML private ToggleButton arrow1;
		@FXML private ToggleButton arrow2;
		@FXML private ToggleButton arrow3;
		@FXML private ToggleButton arrow4;
		@FXML private ToggleButton arrow5;
		@FXML private ToggleButton arrow6;

		// **-------------------------------------------------------------------------------

	private void setupPalette()	
	{
		paletteGroup = new ToggleGroup();
		paletteGroup.getToggles().addAll(arrow, rectangle, circle, text, polygon, polyline, line, gene, metabolite, pathway, protein, rna);
		lineTypeGroup = new ToggleGroup();
		lineTypeGroup.getToggles().addAll(straight, curved, elbowed);
		straight.setSelected(true);
		arrowGroup = new ToggleGroup();
		arrowGroup.getToggles().addAll(arrow1, arrow2, arrow3, arrow4, arrow5, arrow6);
		arrow1.setSelected(true);

		setGraphic(arrow, Tool.Arrow, FontAwesomeIcons.LOCATION_ARROW);
		setGraphic(rectangle, Tool.Rectangle, FontAwesomeIcons.SQUARE);
		setGraphic(circle, Tool.Circle, FontAwesomeIcons.CIRCLE);
		setGraphic(polygon, Tool.Polygon, FontAwesomeIcons.STAR);
		setGraphic(polyline, Tool.Polyline, FontAwesomeIcons.PENCIL);
		setGraphic(line, Tool.Line, FontAwesomeIcons.LONG_ARROW_RIGHT);
		setGraphic(shape1, Tool.Shape1, FontAwesomeIcons.HEART);
//		setGraphic(shape2, Tool.Brace, FontAwesomeIcons.BARCODE);
	
		IController.setGraphic(leftSideBarButton, FontAwesomeIcons.ARROW_CIRCLE_O_RIGHT);
		IController.setGraphic(bottomSideBarButton, FontAwesomeIcons.ARROW_CIRCLE_DOWN);
		IController.setGraphic(toggleGridButton, FontAwesomeIcons.TH);
	}
	Tool curTool = Tool.Arrow;
	public Tool getTool() {		return curTool;	}
	
	public void setTool(Tool inTool)
	{
		curTool = inTool;
		ToggleGroup group = getToolGroup();
//		group.selectToggle(value);
		for (Toggle t : group.getToggles())		// TODO this should reduce to a single line.
		{
			if (t instanceof ToggleButton)
			{
				ToggleButton b = (ToggleButton) t;
				if (b.getId().equals(curTool.name()))
				{
					group.selectToggle(t);
					break;
				}
			}
		}
	}	
	// **-------------------------------------------------------------------------------
	public void setState(String s)
	{
		pasteboard.clear();
//		pasteboard.getChildren().addAll(pasteboard.getGrid().getNodes());
		model.setState(s);
	}
	//-----------------------------------------------------------------------------
	public void addXMLDoc(org.w3c.dom.Document doc)
	{
		new GPML(model, getActiveLayerName()).read(doc);
		getPasteboard().restoreBackgroundOrder();
		assignDataNodesToGroups();
		resetEdgeTable();
		Thread postponed =  new Thread(() -> Platform.runLater(() -> 
		{
			redrawAllEdges();	
			nodeTreeTable.updateTreeTable();
		} )  );
		postponed.start();  
	}
			
	public Layer getActiveLayer() 		{		return getLayerRecord(getActiveLayerName()).getLayer();	}
	public String getActiveLayerName() 	{		return pasteboard.activeLayerName();	}

	//-----------------------------------------------------------------------------
	public void doCopy()// TODO 
	{	
		
	}
	//-----------------------------------------------------------------------------
	public void doPaste()// TODO 
	{	}
	//-----------------------------------------------------------------------------
	private void setupListviews()
	{
		if (undoview != null)
		{
			undoview.setCellFactory(list -> {  return new DrawActionCell();  });	
			undoview.setStyle(CSS_Gray2);
		}
	}
	
	//---------------------------------------------------------------------------------
	  static class DrawActionCell extends ListCell<Action> {
		    @Override
		    public void updateItem(Action item, boolean empty) {
		      super.updateItem(item, empty);
		      if (item != null) 
		      {
		    	  String style = CSS_cellBackground(item.isUndone());
		    	  setStyle(style);
		    	  setText(item.toString());
		      }
		      else   {  	  setStyle("");  	  setText("");    }
		    }
		  }
	  
	  static class StyleSheetCell extends ListCell<String> {
		    @Override public void updateItem(String item, boolean empty) {
		      super.updateItem(item, empty);
		      if (item != null) 
		    	  setText(item.toString());
		      else    {     setStyle("");   	  setText("");      }
		    }
		  }
	// **-------------------------------------------------------------------------------
	// **-------------------------------------------------------------------------------
	public void selectAll(List<Node> n)	{		getSelectionManager().selectAll(n);	}
	// **-------------------------------------------------------------------------------
	public void addExternalNode(Node n)							
	{		
		if (n == null) return;
		pasteboard.getChildren().add(n);	
		if ("Marquee".equals(n.getId())) 	return;
	}

	public void add(VNode n)							
	{		
		pasteboard.add(n);	
		DataNode modelNode = n.modelNode();
//		if (n.isAnchor()) return;
		if (n.isLabel()) return;
		AttributeMap map = modelNode;
		Object prop  = map.get("TextLabel");
	}
	public void addAll(List<VNode> n)	{	pasteboard.addAllVNodes(n);	}
	public void addAll(VNode... n)		{	pasteboard.addAllVNodes(n);	}
	
	// **-------------------------------------------------------------------------------
	public void remove(Node n)						
	{		
		pasteboard.getChildren().remove(n);	
//		getTreeTableView().updateTreeTable();
	}
	public void remove(VNode n)						
	{		
		getModel().removeNode(n);
		String layer = n.getLayerName();
		Layer content = pasteboard.getLayer(layer);			// TODO -- layering naive
		content.remove(n);
		getTreeTableView().updateTreeTable();
	}
	// **-------------------------------------------------------------------------------
	public String getState()					{ 	return model.saveState();  }
	public void reportStatus(String string)	 {}	 //		status1.setText(string);	
	// **-------------------------------------------------------------------------------
	public void addStylesheet(File f)
	{
		Scene scene = pasteboard.getScene();
		ObservableList<String> sheets = scene.getStylesheets();
		sheets.add(f.getName());
//		attributeTable.setItems(sheets);
	}
	// **-------------------------------------------------------------------------------
	// TODO add charts
//	public void scatter(TableView content)	{}
//	public void timeseries(TableView content)	{	}
	public void hiliteByReference(String ref) {
//		for (BiopaxRecord biopax : model.getReferences())
//			if (ref.equals(biopax.getRdfid()))
//				refTable.getSelectionModel().select(biopax);
	}
	public void openByReference(String ref) {
		for (BiopaxRecord biopax :  model.getReferences())
			if (ref.equals(biopax.getRdfid()))
			{
				String pmid = biopax.getId();
				if (StringUtil.hasText(pmid))
					ReferenceController.showPMIDInfo(pmid);
			}
	}
	public void assignDataFile(File f) {
			System.out.println("assignDataFile");		
			List<String> lines = FileUtil.readFileIntoStringList(f.getAbsolutePath());
			for (String s : lines)
			{
				String[] parts = s.split("\t");
				if (parts.length == 2)
				{
					DataNode node = model.getResourceByKey(parts[0]);
					if (node != null)
						node.put("value", parts[1]);
				}
			}
			model.setColorByValue();
	}
	
	public void setActiveLayer(String s) {		pasteboard.setActiveLayer(s);	}
	// **-------------------------------------------------------------------------------
	public void resynch(ObservableList<VNode> selectedItems) {
		if (getInspector() != null)
		getInspector().syncInspector();
		if (selectedItems.size() == 0) return;		// don't deselect without selecting
		copySelectionToNodeTable(selectedItems);
	}
	private void copySelectionToNodeTable(ObservableList<VNode> selectedItems) {
		nodeTable.getSelectionModel().clearSelection();
		for (VNode v : selectedItems)
		{
			TreeItem<XRefable> item = nodeTreeTable.findNode(v.getGraphId());
			if (item != null)
				nodeTable.getSelectionModel().select(item);
		}		
	}
	
	public void redrawEdgesToMe(VNode vNode, double dx, double dy) {
		DataNode data = vNode.modelNode();
		List<Interaction> edges = model.findInteractionsByNode(data);
		for (Interaction edge : edges)
			edge.connect();
	}
	public void redrawAllEdges() {
	for (Interaction edge : model.getEdges())
		{
			edge.rebind();
			edge.connect();
		}
	}
	
	public void assignDataNodesToGroups() {
		Map<String, DataNodeGroup> groupMap = model.getGroupMap();
		Map<String, DataNode> nodes = model.getDataNodeMap();
		for (String key : groupMap.keySet())
		{
			List<VNode> vnodes = new ArrayList<VNode>();
			DataNodeGroup groupNode = groupMap.get(key);
			String groupId = groupNode.get("GroupId");
			if (groupId == null) 		return;		//ERROR
			groupNode.getChildren().clear();
			vnodes.add(groupNode.getStack());
			for (String nodeKey : nodes.keySet())
			{
				DataNode nod = nodes.get(nodeKey);
				VNode stack = nod.getStack();
				String groupRef = nod.get("GroupRef");
				if (groupId.equals(groupRef))
				{
					groupNode.addToGroup(nod);
					vnodes.add(stack);
					pasteboard.getContentLayer().remove(stack);
					stack.setMouseTransparent(true);
				}
			}
			groupNode.calcBounds();
			Group group = new Group();
			group.getChildren().addAll(vnodes);
			pasteboard.getContentLayer().add(group);
		}
			// DEBUG
		for (String key : groupMap.keySet())
		{
			DataNodeGroup groupNode = groupMap.get(key);
			System.out.print(groupNode.getGraphId() + " contains ");
			for (DataNode dn : groupNode.getChildren())
				System.out.print(dn + " ");
			System.out.println(". ");

		}
	}

	// **-------------------------------------------------------------------------------
	public void addInteraction(VNode starter, VNode end) {		
		Interaction i = model.addIteraction(starter, end);
		i.rebind();
	}
	// **-------------------------------------------------------------------------------
	// coming from parser:
	
	public void addDataNode(DataNode node) {
		pasteboard.setActiveLayer("Content");	
		 node.setName( node.get("TextLabel"));
		if (node.getStack() == null)
			new VNode(node, pasteboard);
		model.addResource(node);
	}

	public void addShapeNode(DataNode shapeNode) {
	pasteboard.setActiveLayer("Background");	
		shapeNode.setType("Shape");
		shapeNode.put("Type", "Shape");
		shapeNode.setName(shapeNode.get("ShapeType"));
		new VNode(shapeNode, pasteboard);
		model.addResource(shapeNode);
		model.addShape(shapeNode);
		
	}
	public void addLabel(DataNode label) {
		pasteboard.setActiveLayer("Content");	
		label.put("Layer", "Background");
		label.setType("Label");
		label.put("Type", "Label");
		new VNode(label, pasteboard);
		model.addResource(label);
		model.addLabel(label);
	}

	public void addInteraction(Interaction e)							
	{	
		if (e == null ) return;
		e.setName(e.get("Name"));
//		if (!model.containsEdge(e)) 
		model.addEdge(e);
		EdgeLine artifact = e.getEdgeLine();
		pasteboard.add(0, artifact, e.getLayer());
	}

	public void addGroup(DataNodeGroup grp) {
		pasteboard.setActiveLayer("Background");	
		new VNode(grp, pasteboard);
		model.addGroup(grp);
//		nodeTreeTable.addBranch(grp);

	} 
	public void addStateNode(DataNodeState statenode) {
		pasteboard.setActiveLayer("Content");	
		String graphRef = statenode.get("GraphRef");
		DataNode host = model.findDataNode(graphRef);
		if (host != null)
		{
			host.getStack().addState(statenode);
			model.addState(graphRef, statenode);
		}
		
	}

	// **-------------------------------------------------------------------------------
	// GeneSets
	// **-------------------------------------------------------------------------------
	public void setGeneSet(GeneSetRecord rec) {
		geneModel.setGeneList(rec, rec.getGeneSet());
		
	}
	public void addGeneSet(File f, Point2D dropPt) {
		addGeneSet( f, dropPt.getX(), dropPt.getY());
	}
	public void addGeneSet(File f, double dropX, double dropY) {
		GeneSetRecord rec = new GeneSetRecord(f);
//		addGenes(rec.getGeneList());			// ADD UNIQUE
		
		double COLWID = 90;
		double ROWHGHT = 40;
		int NCOLS = (int) Math.ceil(Math.sqrt(rec.getRowCount()));
		
		for (int i=0; i< rec.getGeneSet().size(); i++)
		{
			Gene g = rec.getGeneSet().get(i);
			AttributeMap attrs = new AttributeMap(g);
			attrs.put("CenterX", "" + (dropX + (i % NCOLS) * COLWID));
			attrs.put("CenterY", "" + (dropY + (i / NCOLS) * ROWHGHT));
			attrs.put("Width", "80");
			attrs.put("Height", "30");
			String name = g.getName();
			attrs.put("TextLabel", name);
			attrs.put("Type", "GeneProduct");
			DataNode node = new DataNode(attrs, getModel());
			node.copyAttributesToProperties();
			node.setName(name);
			addDataNode(node);			
		}
	}
//	public void rebind(String gid) {
//System.err.println("REBIND");	
//}

	public GeneSetRecord getGeneSetRecord() {
		return geneSetRecord;
	}
}
