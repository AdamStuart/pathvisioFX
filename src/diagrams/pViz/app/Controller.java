package diagrams.pViz.app;

import java.io.File;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.ResourceBundle;

import animation.BorderPaneAnimator;
import diagrams.pViz.dialogs.EnrichmentController;
import diagrams.pViz.dialogs.EnrichmentDialog;
import diagrams.pViz.dialogs.LegendDialog;
import diagrams.pViz.gpml.GPML;
import diagrams.pViz.gpml.GPMLPoint.ArrowType;
import diagrams.pViz.model.GeneModel;
import diagrams.pViz.model.Model;
import diagrams.pViz.model.edges.Edge;
import diagrams.pViz.model.edges.EdgeLine;
import diagrams.pViz.model.edges.EdgeType;
import diagrams.pViz.model.edges.Interaction;
import diagrams.pViz.model.nodes.DataNode;
import diagrams.pViz.model.nodes.DataNodeGroup;
import diagrams.pViz.tables.GPMLTreeTableView;
import diagrams.pViz.tables.LegendRecord;
import diagrams.pViz.tables.PathwayController;
import diagrams.pViz.tables.ReferenceController;
import diagrams.pViz.util.WebUtil;
import diagrams.pViz.view.GroupMouseHandler;
import diagrams.pViz.view.Inspector;
import diagrams.pViz.view.Layer;
import diagrams.pViz.view.LayerController;
import diagrams.pViz.view.LayerRecord;
import diagrams.pViz.view.PaletteController;
import diagrams.pViz.view.Pasteboard;
import diagrams.pViz.view.VNode;
import gui.Action;
import gui.Action.ActionType;
import gui.Borders;
import gui.UndoStack;
import icon.FontAwesomeIcons;
import icon.GlyphIcon;
import icon.GlyphsDude;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.geometry.Side;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTableView;
import javafx.scene.input.Clipboard;
import javafx.scene.input.DataFormat;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
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
	@FXML private void getInfo() 		{ getSelectionManager().getInfo();	} 
	
	
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
	@FXML private void openInCytoscape(){ 	System.out.println("openInCytoscape");		}
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
	@FXML public  void group()			{ 	undoStack.push(ActionType.Group);	getSelectionManager().doGroup(false);  }
	@FXML public  void compoundNode()	{ 	undoStack.push(ActionType.Group);	getSelectionManager().doGroup(true);  }
	@FXML public  void ungroup()		{ 	undoStack.push(ActionType.Ungroup);	getSelectionManager().ungroup(); }
	@FXML public  void forward()		{	undoStack.push(ActionType.Reorder);	getSelectionManager().forward(); 	}
	@FXML public  void backward()		{	undoStack.push(ActionType.Reorder);	getSelectionManager().backward(); 	}
	@FXML public  void toFront()		{	undoStack.push(ActionType.Reorder);	getSelectionManager().toFront(); 	}
	@FXML public  void toBack()			{	undoStack.push(ActionType.Reorder);	getSelectionManager().toBack();  pasteboard.restoreBackgroundOrder();  	}
	@FXML public  void zoomIn()			{	undoStack.push(ActionType.Zoom);	pasteboard.zoomIn(); 	}
	@FXML public  void zoomOut()		{	undoStack.push(ActionType.Zoom);	pasteboard.zoomOut();  pasteboard.restoreBackgroundOrder();  	}
	@FXML public  void toLayer()		{	//TODO
	}
	@FXML private VBox south;
	@FXML private Pane drawPane;
	@FXML private ScrollPane scrollPane;
//	@FXML private ListView<Action> undoview = null;
//	private GeneListTable geneListTable = null;
	@FXML private BorderPane container;			// root of fxml	
	@FXML private Button tableOptions;
	@FXML private MenuItem enrich;
	@FXML private Button bottomSideBarButton;
	@FXML private Button leftSideBarButton;
	@FXML private Button rightSideBarButton;
	@FXML private Button toggleGridButton;

//-------------------------------------------------------------
	@FXML private MenuItem annotate;
	@FXML private void	annotate() 	{	model.annotateIdentifiers(); 	}
	@FXML private MenuItem selectEdges;
	@FXML private MenuItem undo;
	@FXML private MenuItem redo;
	@FXML private MenuItem clearundo;
	@FXML private HBox bottomPadding;
	@FXML private BorderPane 	loadContainer;

	//------------------------------------------
	@FXML private Label select;
	@FXML private Label size;
	@FXML private Label scale;
	@FXML private SplitPane hsplitter;
	
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
//	private Stage stage;
	public AnchorPane getProperties() 		{ 	return propertyPanel;	}
	private Inspector inspector;
	public Inspector getInspector()		 	{ 	return inspector;	}
	@FXML private void showInspector() 		{	bottomSideBarButton.fire();	}
	@FXML private PaletteController palette;
	public PaletteController getPalette()		 		{	 return palette;	}
	@FXML private void showPalette() 		{	leftSideBarButton.fire();	}
	// **-------------------------------------------------------------------------------
	public void modelChanged() 				{		updateTreeTable(); }
		
	private GPMLTreeTableView nodeTreeTable;
	public GPMLTreeTableView getTreeTableView() { return nodeTreeTable; }
	public void updateTreeTable()			{ 	nodeTreeTable.updateTreeTable();  }
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
	
	public ArrowType getCurrentArrowType() { return palette.getActiveArrowType();		}
	
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
	
	// **-------------------------------------------------------------------------------
	@Override public void initialize(URL location, ResourceBundle resources)
	{
		undoStack = new UndoStack(this, null);
		assert(drawPane != null);
		model = new Model(this);
		geneModel = new GeneModel(this);
		setupPalette();
		pasteboard = new Pasteboard(this, scale);
		palette.setParentController(this);
		palette.getView().setBorder(Borders.redBorder);
		palette.getView().setPrefWidth(150);
		palette.getView().setPrefHeight(750);
		setupInspector();		
		pasteboard.bindGridProperties();
		drawPane.getChildren().add(pasteboard);
		doc = new Document(this);
		String cssURL = this.getClass().getResource("styles.css").toExternalForm();
		pasteboard.getStylesheets().add(cssURL);
//		stage = App.getInstance().getStage();
//		drawContainer.setBorder(Borders.etchedBorder);
		scrollPane.setOnScroll(ev -> {
			ev.consume();
	        if (ev.getDeltaY() == 0)   return;	
		});
//		bottomDash.setBorder(Borders.dashedBorder );
		if (bottomPadding!= null)
			bottomPadding.setPadding(new Insets(3,3,4,4));

		IController.setGraphic(leftSideBarButton, FontAwesomeIcons.ARROW_CIRCLE_O_RIGHT);
		IController.setGraphic(bottomSideBarButton, FontAwesomeIcons.ARROW_CIRCLE_DOWN);
		IController.setGraphic(toggleGridButton, FontAwesomeIcons.TH);
//		setupListviews();
		new BorderPaneAnimator(container, leftSideBarButton, Side.LEFT, false, 150);
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

	// **-------------------------------------------------------------------------------
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


	private void setupPalette() {
		URL res = getClass().getResource("../view/Palette.fxml");
	    FXMLLoader paletteLoader = new FXMLLoader(res);
	   try
	   {
		   paletteLoader.load();
		   palette = paletteLoader.getController();
		   container.setLeft(palette.getView());
	   }
	   catch (Exception e) 
	   { System.err.println("Palette failed to load");
	   e.printStackTrace();}

	}
	public void doNewGeneList()	{		App.doNewGeneList(model, geneModel);	}
	

	// **-------------------------------------------------------------------------------
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
//	final ContextMenu tableColumnOptions = new ContextMenu();
//	String[] colnames = new String[] { "Id", "Reference", "Database", "Authors", "Title", "Source", "Year" };
//
//	@FXML private void doTableOptionsPopup()
//	{
//		ObservableList<MenuItem> items = tableColumnOptions.getItems();
//		if (items.size() == 0)
//		{
//			for (String c : colnames)
//				items.add(new CheckMenuItem(c));
//		}
//			
//		tableColumnOptions.setX(tableOptions.getLayoutX() + stage.getX() - 60); 
//		tableColumnOptions.setY(tableOptions.getLayoutY() + stage.getY()+ 40);
//		tableColumnOptions.show(stage);
//	}
	//-----------------------------------------------------------------------
	@FXML private void doAbout()
	{
//		AboutDialog dlog = new AboutDialog();
//		dlog.showAndWait();
	}
	//-----------------------------------------------------------------------
	@FXML private void doEnrich() 
	{		
		System.out.println("Enrich");	
		EnrichmentDialog dlog = new EnrichmentDialog(model);
		EnrichmentController ctrol = dlog.getController();
		@SuppressWarnings("unchecked")
		Optional<ButtonType> result = dlog.showAndWait();
		if ("Enrich".equals(result.get().getText()))
			ctrol.doSearch();	
	}
	@FXML private void addLegend() {		LegendRecord.makeLegend("Legend", "", true, true, false, model, this, true);	}
	@FXML private void doNewMultiGeneList()  { 	App.doNewMultiGeneList(); 	}
	@FXML private void addLegendDlog()
	{
		LegendDialog dlog = new LegendDialog();
		@SuppressWarnings("unchecked")
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
	 @FXML private void hiliteEnter()	{	System.out.println("entering continuous variable space");		}
	 @FXML private void hiliteExit()	{	System.out.println("exiting continuous variable space");	}
	// **-------------------------------------------------------------------------------
	public void setState(String s)
	{
		try
		{
			pasteboard.clear();
			org.w3c.dom.Document doc = FileUtil.convertStringToDocument(s);	//  parse string to XML
			if (doc != null)
				addXMLDoc(doc);					
		}
		catch (Exception e) 		{ 	e.printStackTrace();		}
	}
	//-----------------------------------------------------------------------------
	public void addXMLDoc(org.w3c.dom.Document doc)
	{
		nodeTreeTable.disableUpdates();
		new GPML(model, getActiveLayerName()).read(doc);
		getPasteboard().restoreBackgroundOrder();
		assignDataNodesToGroups();
		resetEdgeTable();
//		redrawAllEdges();	
		nodeTreeTable.resumeUpdates();
		
		Thread postponed =  new Thread(() -> Platform.runLater(() -> 
		{
			modelChanged();
		} )  );
		postponed.start();  
	}
			
	public Layer getActiveLayer() 		{		return getLayerRecord(getActiveLayerName()).getLayer();	}
	public String getActiveLayerName() 	{		return pasteboard.activeLayerName();	}

	@FXML public void fillDown() {		nodeTreeTable.fillDown();	}
	//-----------------------------------------------------------------------------
	public void doCopy()// TODO 
	{	
	}
	//-----------------------------------------------------------------------------
	public void doPaste()// TODO 
	{	
	     final Clipboard clipboard = Clipboard.getSystemClipboard();
	     if (clipboard.hasString())
	     {
	    	String s =  clipboard.getString();
	    	String[] lines = s.split("\n|\r");
	    	int nLines = lines.length;
	    	if (nLines > s.length() / 10)		// looks like a gene set
	    	{
    			Point2D pt = new Point2D(200,200);
    			for (String line : lines)
    			{
    				pasteboard.addNodeAt(line, pt);
    				pt = pt.add(0,40);
    			}

	    	}	
	     }
	}
	//-----------------------------------------------------------------------------
//	private void setupListviews()
//	{
//		if (undoview != null)
//		{
//			undoview.setCellFactory(list -> {  return new DrawActionCell();  });	
//			undoview.setStyle(CSS_Gray2);
//		}
//	}
	
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
	}
	public void remove(VNode n)						
	{		
		n.modelNode().removeSelf();
		getModel().removeNode(n);
		String layer = n.getLayerName();
		Layer content = pasteboard.getLayer(layer);			// TODO -- layering naive
		content.remove(n);
	}
	public void remove(Edge e)						
	{		
		getModel().removeEdge(e);
		String layer = e.getLayer();
		Layer content = pasteboard.getLayer(layer);			// TODO -- layering naive
		content.remove(e.getEdgeLine());
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
			System.out.println("assignDataFile " + f.getName());		
			List<String> lines = FileUtil.readFileIntoStringList(f.getAbsolutePath());
			for (String s : lines)
			{
				String[] parts = s.split("\t");
				if (parts.length == 2)
				{
					List<DataNode> nodes = model.getResourceByKey(parts[0]);
					for (DataNode node : nodes)
						node.put("value", parts[1]);
				}
			}
			GPMLTreeTableView table = getTreeTableView();
				if (!table.columnExists("value"))
					table.addColumn("value");
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
	
	public void redrawEdgesToMe(VNode vNode) {
		DataNode data = vNode.modelNode();
		List<Interaction> edges = model.findInteractionsByNode(data);
		for (Interaction edge : edges)
			edge.connect();
	}
	public void redrawAllEdges() {
	for (Interaction edge : model.getEdges())
			edge.connect();
	}
	
	public void assignDataNodesToGroups() {
//		if (true) return;
		Map<String, DataNodeGroup> groupMap = model.getGroupMap();
		for (String key : groupMap.keySet())
		{
			DataNodeGroup groupNode = groupMap.get(key);
			groupNode.assignMembers();
		}
	}

	// **-------------------------------------------------------------------------------

	// **-------------------------------------------------------------------------------
	// coming from parser:
	
	public void addDataNode(DataNode node) {
		pasteboard.setActiveLayer("Content");	
		node.setGraphId(node.get("GraphId"));
		 node.setName( node.get("TextLabel"));
		if (node.getStack() == null)
			new VNode(node, pasteboard);
		model.addResource(node);
		modelChanged();
	}

	public void addInteraction(Interaction e)							
	{	
		if (e == null ) return;
		e.setName(e.get("Name"));
//		if (!model.containsEdge(e)) 
		model.addEdge(e);
		EdgeLine edgeLine = e.getEdgeLine();
		pasteboard.add(0, edgeLine, e.getLayer());
	}

	public void addInteraction(VNode starter, VNode end) {		
		Interaction i = model.addIteraction(starter, end);
//		i.rebind();
		modelChanged();
	}
	
	public void addGroup(DataNodeGroup grp) {		// from parser
		if (grp == null) return;
		grp.getStack().addEventHandler(MouseEvent.ANY, new GroupMouseHandler(pasteboard));
		model.addGroup(grp);
//		BoundingBox bounds = grp.getBounds();
//		grp.getStack().setBounds(bounds);
		pasteboard.getContentLayer().remove(grp.getStack());
		pasteboard.getContentLayer().add(5, grp.getStack());
	}
	
	public DataNodeGroup addGroup(List<VNode> selectedItems, boolean isCompound) {// from GUI selection
		getUndoStack().push(ActionType.Group);

		pasteboard.setActiveLayer("Background");	
		AttributeMap map = new AttributeMap();
		map.put("Type", "Group");
		map.put("ShapeType", "Octagon");
		map.put("Color", "DDDDDD");
		map.put("TextLabel", "Complex");
		map.put("Name", "Complex");
		map.put("Layer", "Background");
		DataNodeGroup group = new DataNodeGroup(map, getModel(), isCompound);
		VNode stack = group.getStack();
		for (VNode item : selectedItems)
		{
			item.setMouseTransparent(true);
			item.modelNode().rememberPosition();
			group.addMember(item.modelNode());
		}
		group.calcBounds();
		double groupWidth = group.getDouble("Width");
		double groupHeight = group.getDouble("Height");
		double minX = group.getDouble("X");
		double minY = group.getDouble("Y");
		for (VNode item : selectedItems)
		{
			stack.getChildren().add(item);
			double hw = item.getWidth() /2;
			double hh = item.getHeight()/2;
			item.setTranslateX(item.getLayoutX() - minX - groupWidth /4);
			item.setTranslateY(item.getLayoutY() - minY- groupHeight /4 );
			item.deselect();
//			AnchorPane.setLeftAnchor(item, item.getLayoutX() - minX);
//			AnchorPane.setTopAnchor(item, item.getLayoutY() - minY);
		}
		group.setName("Complex");
		group.setGraphId(group.get("GraphId"));
		addGroup(group);
		return group;

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

	public GeneSetRecord getGeneSetRecord() {		return geneSetRecord;	}
	public EdgeType getCurrentLineBend() {		return palette.getCurrentLineBend();	}
	public Tool getTool() {		return palette.getTool();	}
}
