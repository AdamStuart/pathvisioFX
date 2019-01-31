package diagrams.pViz.app;

import java.net.URL;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import diagrams.pViz.model.GeneModel;
import diagrams.pViz.model.Model;
import diagrams.pViz.model.edges.Interaction;
import diagrams.pViz.tables.GeneListController;
import diagrams.pViz.tables.PathwayController;
import diagrams.pViz.tables.ReferenceController;
import diagrams.pViz.tables.XrefListController;
import diagrams.pViz.view.PanningCanvas;
import diagrams.pViz.view.SceneGestures;
import gui.Log;
//import edu.stanford.nlp.util.ArrayUtils;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextInputDialog;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Pair;
import model.bio.GeneSetRecord;
import util.FileUtil;

//---------------------------------------------------------------------------------------
	/*
	 * The Application in JavaFX doesn't do much except load a FXML file 
	 * and put it in a window.  Put your model setup code in init, and the steps
	 * to make a new window in start
	 */
	public class App extends Application
	{
	    public static void main(final String[] args) {     Application.launch(args);   }
	    
	    @Override public  void init() 
		{
		    instance = this;
	    	startLogging();
		}
	
	    @Override public  void start(Stage stage) throws Exception 
	    {
	    	theStage = stage;	//  keep a pointer to the stage used to position global dialogs, or set window title
	    	doNew(theStage, null);	
//	    	makeLoader();
	    	
	    }
	    @Override public  void stop() throws Exception 
	    {
	    	stopLogging();
	    }

	 static public App getInstance()	{ return instance;	}
	 static private App instance;
	 private Stage theStage;
	 public Stage getStage() 			{ return theStage;  }
	//---------------------------------------------------------------------------------------
	 public void startLogging()
	 {
	    Log.info("startLogging");
	 }
	//---------------------------------------------------------------------------------------
	 public void stopLogging()
	 {
		 Log.info("stopLogging");
	 }
	//---------------------------------------------------------------------------------------
	static double PHI = 1.618;
	 public Pair<FXMLLoader, Stage> makeLoader()
	{
		try 
		{
			String fullname = "loader.fxml";
		    URL url = getClass().getResource(fullname);		// this gets the fxml file from the same directory as this class
		    if (url == null)
		    {
		    	System.err.println("Bad path to the FXML file");
		    	return null;
		    }

		    Stage loaderStage = new Stage();
			loaderStage.setTitle("Tools");
			FXMLLoader fxmlLoader = new FXMLLoader();
		    fxmlLoader.setLocation(url);
		    BorderPane appPane =  fxmlLoader.load();
		    loaderStage.setScene(new Scene(appPane, 800, 130));
//		    loaderStage.show();
			return new Pair<FXMLLoader, Stage>(fxmlLoader, loaderStage);
		}
		catch (Exception e) { e.printStackTrace();}
		return null;
	}
	 
		//---------------------------------------------------------------------------------------
	public void doNew()		{	doNew(new Stage(), null);	}
	public void doNew(String xml)
	{
		try
		{
			org.w3c.dom.Document doc = FileUtil.convertStringToDocument(xml);
			doNew(new Stage(), doc);
		}
		catch(Exception e)
		{
		}
	}
	
	public void doNew(Stage stage, org.w3c.dom.Document doc)
	{
		if (stage == null)
			stage = new Stage();
		try 
		{
		    stage.setTitle("Pathway Editor");
			FXMLLoader fxmlLoader = new FXMLLoader();
			String fullname = "PViz.fxml";
		    URL url = getClass().getResource(fullname);		// this gets the fxml file from the same directory as this class
		    if (url == null)
		    {
		    	System.err.println("Bad path to the FXML file");
		    	return;
		    }
		    fxmlLoader.setLocation(url);
		    BorderPane appPane =  fxmlLoader.load();
		    
		    if (doc != null)
		   	{
		    	Controller c = (Controller) fxmlLoader.getController();
		    	c.addXMLDoc(doc);	
		    }
		    stage.setScene(new Scene(appPane, 1200, 1000));
		    registerWindow(stage);
		    stage.show();
		}
		catch (Exception e) { e.printStackTrace();}
		
	}
	//---------------------------------------------------------------------------------------
	static private ObservableList<Stage> stageList = FXCollections.observableArrayList();
	private static void registerWindow(Stage stage)  
	{	 	
		stageList.add(stage); 	
		stage.setOnHidden(e -> stageList.remove(stage) );
	}
	
	static public List<MenuItem> getWindowsMenus()
	{
		return stageList.stream().
				map(stage -> { return makeMenu(stage); }).
				collect(Collectors.toList());
	}

	static private MenuItem makeMenu(Stage stage) {
		MenuItem item = new MenuItem(stage.getTitle());
		item.setOnAction(e -> { stage.toFront(); } );
		return item;
	}

//---------------------------------------------------------------------------------------
// TODO show searchable list as browsePathways does
	
	static public  String choosePathway(Controller parent)		
	{
		TextInputDialog dialog = new TextInputDialog("WP430");
		dialog.setTitle("Pathway Information Dialog");
		dialog.setHeaderText("This connects to WikiPathways.org to search its database");
		dialog.setContentText("What is the id number?");

		// Traditional way to get the response value.
		Optional<String> result = dialog.showAndWait();
		if (result.isPresent())
		    return result.get();

		return "WP4";

	}
	
	static public  void browsePathways(Controller parent)		
	{
		Stage browserStage = new Stage();
		try 
		{
			browserStage.setTitle("Pathway Browser");
			FXMLLoader fxmlLoader = new FXMLLoader();
			String fullname = "../gpml/PathwayList.fxml";
		    URL url = getInstance().getClass().getResource(fullname);		// this gets the fxml file from the same directory as this class
		    if (url == null)
		    {
		    	System.err.println("Bad path to the FXML file: " + fullname);
		    	return;
		    }
		    fxmlLoader.setLocation(url);
		    Pane appPane =  fxmlLoader.load();
		    PathwayController pathwayController = (PathwayController) fxmlLoader.getController();
		    pathwayController.setParentController(parent);
		    browserStage.setScene(new Scene(appPane, 800, 800));
		    registerWindow(browserStage);
		    browserStage.show();
		    pathwayController.doSearch();
		}
		catch (Exception e) 
		{ 
			System.err.println("Loading error in browsePathways()");
			e.printStackTrace();
		}
	}
	   //---------------------------------------------------------------------------------------
	static final String GENELIST = "../gpml/GeneList.fxml";
	static final String XREFLIST = "../tables/XRefList.fxml";
	   static public  void doNewGeneList(Model model, GeneModel geneModel) 
		{
//		   GeneSetRecord geneList =   geneModel.getGeneList();
//		   Collection<Interaction>  edgeList =    model.getEdges();
//		   doNewGeneList( geneList,  edgeList, true); 
		   GeneSetRecord geneList =   geneModel.getGeneList();
		   Collection<Interaction>  edgeList =    model.getEdges();
		   doNewNetworkList( model); 
		}
	   
	   static public  void doNewGeneList(GeneSetRecord geneList, Collection<Interaction>  edgeList, boolean extra) 
	   {
	if (geneList == null)
		geneList = new GeneSetRecord("Empty");
	try
	{
		Pair<FXMLLoader, Stage> pair = buildStage("Gene List", GENELIST, 800, 650)	;
		if (pair == null) return;
		GeneListController glc = (GeneListController) pair.getKey().getController();   
		Stage s =  pair.getValue();
	    registerWindow(s);
	    glc.loadTables(geneList, edgeList, true);
		s.show();
	}
	catch (Exception e) { e.printStackTrace();	}
}
//---------------------------------------------------------------------------------------
	   static public  void doNewNetworkList(Model inModel) 
{
	try
	{
		Pair<FXMLLoader, Stage> pair = buildStage("Pathway Components", XREFLIST, 800, 650)	;
		if (pair == null) return;
		XrefListController glc = (XrefListController) pair.getKey().getController();   
		Stage s =  pair.getValue();
	    registerWindow(s);
	    glc.loadTables(inModel);
		s.show();
	}
	catch (Exception e) { e.printStackTrace();	}
}
//---------------------------------------------------------------------------------------
	static final String RESOURCE = "../tables/ReferenceList.fxml";
//    static final String STYLE = "../tables/genelistStyles.css";
	static  public Pair<FXMLLoader, Stage>  openReferenceList(Model model) {
		try
		{
			Stage stage = new Stage();
			URL res = getInstance().getClass().getResource(RESOURCE);
		    FXMLLoader referenceLoader = new FXMLLoader(res);
	        Scene scene = new Scene(referenceLoader.load());
	        ReferenceController ctrl =  (ReferenceController)referenceLoader.getController();
	        ctrl.setModel(model);
//			scene.getStylesheets().add(getInstance().getClass().getResource(STYLE).toExternalForm());
	        stage.setTitle("Reference Window");
	        stage.setX(20);
			stage.setWidth(800);
			stage.setHeight(450);
			stage.setScene(scene);
		    registerWindow(stage);
			stage.show();
			return (new Pair<FXMLLoader, Stage>(referenceLoader, stage));
		}
		catch (Exception e) { 
			System.err.println(RESOURCE + " failed to load");
			e.printStackTrace();	}
		return null;
	}
 	
	//---------------------------------------------------------------------------------------
	static final String MG_RESOURCE = "../tables/MultiGeneList.fxml";
    static final String MG_STYLE = "../tables/genelistStyles.css";
    static public void doNewMultiGeneList() 
	{
		try
		{
			Stage stage = new Stage();
			URL res = getInstance().getClass().getResource(MG_RESOURCE);
		    FXMLLoader geneListLoader = new FXMLLoader(res);
	        Scene scene = new Scene(geneListLoader.load());
			scene.getStylesheets().add(getInstance().getClass().getResource(MG_STYLE).toExternalForm());
	        stage.setTitle("Multi Gene List");
	        stage.setX(20);
			stage.setWidth(800);
			stage.setHeight(650);
			stage.setScene(scene);
		    registerWindow(stage);
			stage.show();
		}
		catch (Exception e) { e.printStackTrace();	}
	}

	public static void doNewGallery()
	{
		Stage theStage = new Stage();
		try 
		{
			theStage.setTitle("Pathway Gallery");
			FXMLLoader fxmlLoader = new FXMLLoader();
			String fullname = "gallery.fxml";
		    URL url = instance.getClass().getResource(fullname);		// this gets the fxml file from the same directory as this class
		    if (url == null)
		    {
		    	System.err.println("Bad path to the FXML file");
		    	return;
		    }
		    fxmlLoader.setLocation(url);
		    VBox appPane =  fxmlLoader.load();
		    
		    theStage.setScene(new Scene(appPane, 500, 700));
		    theStage.show();
		}
		catch (Exception e) { e.printStackTrace();}
		
	}

	//---------------------------------------------------------------------------------------
	public static void showImage(String title, WritableImage wimg) {
		ImageView view = new ImageView(wimg);
		PanningCanvas canvas = new PanningCanvas(null, null);
		canvas.getChildren().add(view);
		view.scaleXProperty().bind(canvas.scaleXProperty());
		view.scaleYProperty().bind(canvas.scaleYProperty());

        SceneGestures sceneGestures = new SceneGestures(canvas);
        view.addEventFilter( MouseEvent.MOUSE_PRESSED, sceneGestures.getOnMousePressedEventHandler());
        view.addEventFilter( MouseEvent.MOUSE_DRAGGED, sceneGestures.getOnMouseDraggedEventHandler());
        view.addEventFilter( ScrollEvent.ANY, sceneGestures.getOnScrollEventHandler());

        Stage stage = buildStage( title, canvas, 800, 650);
        stage.setX(20);
	}

	//---------------------------------------------------------------------------------------
	//---------------------------------------------------------------------------------------
	static public  Stage buildStage(String title, Parent content, int wid, int hght)		
	{
		Stage stage = new Stage();
		stage.setTitle(title);
	    stage.setScene(new Scene(content, wid, hght));
	    registerWindow(stage);
	    stage.show();
	    return stage;
	}
		
	static public  Pair<FXMLLoader, Stage> buildStage(String title, String fxml, int wid, int hght)		
	{
		Stage stage = new Stage();
		try 
		{
			stage.setTitle(title);
			FXMLLoader fxmlLoader = new FXMLLoader();
		    URL url = getInstance().getClass().getResource(fxml);		// this gets the fxml file from the same directory as this class
		    if (url == null)
		    {
		    	System.err.println("Bad path to the FXML file: " + fxml);
		    	return null;
		    }
		    fxmlLoader.setLocation(url);
		    Region appPane =  fxmlLoader.load();
		    stage.setScene(new Scene(appPane, wid, hght));
		    return new Pair<FXMLLoader, Stage>(fxmlLoader, stage);
		}
		catch (Exception e) { e.printStackTrace();}
		return null;
	}
	
}
