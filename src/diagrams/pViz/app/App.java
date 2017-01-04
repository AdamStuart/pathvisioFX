package diagrams.pViz.app;

import java.net.URL;

import diagrams.pViz.tables.GeneListController;
import diagrams.pViz.tables.PathwayController;
//import edu.stanford.nlp.util.ArrayUtils;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SplitPane;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.stage.Stage;
import javafx.util.Pair;
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
		}
	
	    @Override public  void start(Stage stage) throws Exception 
	    {
	    	theStage = stage;	//  keep a pointer to the stage used to position global dialogs, or set window title
	    	doNew(theStage, null);	
//	    	makeLoader();
	    	
	    }

	 static public App getInstance()	{ return instance;	}
	 static private App instance;
	 private Stage theStage;
	 public Stage getStage() 			{ return theStage;  }

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
			loaderStage.setTitle("CyteGeist");
			FXMLLoader fxmlLoader = new FXMLLoader();
		    fxmlLoader.setLocation(url);
		    BorderPane appPane =  fxmlLoader.load();
		    loaderStage.setScene(new Scene(appPane, 360, 130));
		    loaderStage.show();
			return new Pair<FXMLLoader, Stage>(fxmlLoader, loaderStage);
		}
		catch (Exception e) { e.printStackTrace();}
		return null;
	}
	 
	 public void doNew()		{		doNew(new Stage(), null);	}
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
		    stage.setTitle("A PathVisio Mockup");
			FXMLLoader fxmlLoader = new FXMLLoader();
			String fullname = "Visio.fxml";
		    URL url = getClass().getResource(fullname);		// this gets the fxml file from the same directory as this class
		    if (url == null)
		    {
		    	System.err.println("Bad path to the FXML file");
		    	return;
		    }
		    fxmlLoader.setLocation(url);
		    SplitPane appPane =  fxmlLoader.load();
		    
		    if (doc != null)
		   	{
		    	Controller c = (Controller) fxmlLoader.getController();
		    	c.addXMLDoc(doc);	
		    }
		    stage.setScene(new Scene(appPane, 1000, 800));
		    stage.show();
		}
		catch (Exception e) { e.printStackTrace();}
		
	}
 //---------------------------------------------------------------------------------------
	static public  void browsePathways(Controller parent)		
	{
		Stage browserStage = new Stage();
		try 
		{
			browserStage.setTitle("Pathway Browser");
			FXMLLoader fxmlLoader = new FXMLLoader();
			String fullname = "../tables/PathwayList.fxml";
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
		    browserStage.show();
		    pathwayController.doSearch();
		}
		catch (Exception e) 
		{ 
			System.err.println("Loading error in browsePathways()");
			e.printStackTrace();
		}
	}
	static final String GENELIST = "../gpml/GeneList.fxml";
	   static public  void doNewGeneList(GeneListRecord geneList) 
		{
			if (geneList == null)
				geneList = new GeneListRecord("Empty");
			try
			{
				Pair<FXMLLoader, Stage> pair = buildStage("Gene List Window", GENELIST, 800, 650)	;
				if (pair == null) return;
				GeneListController glc = (GeneListController) pair.getKey().getController();   
				pair.getValue().show();
			    glc.loadTables(geneList, true);
			}
			catch (Exception e) { e.printStackTrace();	}
		}
		static public  Stage buildStage(String title, Region content, int wid, int hght)		
		{
			Stage stage = new Stage();
			stage.setTitle(title);
		    stage.setScene(new Scene(content, wid, hght));
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
	
	
	static final String RESOURCE = "../tables/ReferenceList.fxml";
    static final String STYLE = "genelistStyles.css";
	static  public Pair<FXMLLoader, Stage>  openReferenceList() {
		try
		{
			Stage stage = new Stage();
			URL res = getInstance().getClass().getResource(RESOURCE);
		    FXMLLoader referenceLoader = new FXMLLoader(res);
	        Scene scene = new Scene(referenceLoader.load());
//			scene.getStylesheets().add(getInstance().getClass().getResource(STYLE).toExternalForm());
	        stage.setTitle("Reference Window");
	        stage.setX(20);
			stage.setWidth(800);
			stage.setHeight(450);
			stage.setScene(scene);
			stage.show();
			return (new Pair<FXMLLoader, Stage>(referenceLoader, stage));
		}
		catch (Exception e) { e.printStackTrace();	}
		return null;
}

 	
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
	        stage.setTitle("Multi Gene List Window");
	        stage.setX(20);
			stage.setWidth(800);
			stage.setHeight(650);
			stage.setScene(scene);
			stage.show();
		}
		catch (Exception e) { e.printStackTrace();	}
	}

	public static void showImage(String title, WritableImage wimg) {
		Stage stage = new Stage();
		ScrollPane scroller = new ScrollPane();
		ImageView view = new ImageView(wimg);
//		view.prefWidth(300);
//		view.minWidth(300);
//		view.prefHeight(300);
//		view.minHeight(300);
		scroller.setContent(view);
        Scene scene = new Scene(scroller);
        stage.setTitle(title);
        stage.setX(20);
		stage.setWidth(800);
		stage.setHeight(650);
		stage.setScene(scene);
		stage.show();
	}

}
