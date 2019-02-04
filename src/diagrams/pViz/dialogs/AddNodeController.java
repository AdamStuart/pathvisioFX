package diagrams.pViz.dialogs;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.ResourceBundle;

import diagrams.pViz.app.Controller;
import diagrams.pViz.model.Model;
import diagrams.pViz.model.nodes.DataNode;
import diagrams.pViz.tables.idmap.DataSourceRecord;
import gui.DraggableTableRow;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.TreeItem;
import javafx.scene.control.cell.PropertyValueFactory;
import model.AttributeMap;
import model.AttributeValue;
import model.bio.Gene;
import util.FileUtil;
import util.StringUtil;

public class AddNodeController implements Initializable
{
	@FXML private Button lookup;
	@FXML private Button select;
	@FXML private TextField entry;
	@FXML private ChoiceBox<String> speciesChoices;
	private Model model;
	private AddNodeDialog dialog;
	@FXML private TableColumn<AttributeValue, String> dbCol;
	@FXML private TableColumn<AttributeValue, String> idCol;

	@FXML TableView<AttributeValue> idList;
	//----------------------------------------------------------------------------------
	@Override public void initialize(URL location, ResourceBundle resources)
	{
		String[] organisms = { "Any", "Homo sapiens", "Mus musculus", "Rattus norvegicus", "Canis familiarus", "Box taurus", "Pan troglodytes", "Gallus gallus" };
	if (speciesChoices != null)
		{
		speciesChoices.getItems().addAll(organisms);
		speciesChoices.getSelectionModel().select(1);
		}
//		idList.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
//		targetList.getSelectionModel().selectedItemProperty().addListener(ev->{ targetSelectionChanged(ev);});
     }
	
	public void setup(AddNodeDialog dlog, Model m) {
		model = m;
		dialog = dlog;
		setSpeciesInfo(m.getSpecies().common());
		dialog.getDialogPane().getButtonTypes().addAll(ButtonType.CANCEL);

		dbCol.setCellValueFactory(new PropertyValueFactory<AttributeValue, String>("attribute"));
		idCol.setCellValueFactory(new PropertyValueFactory<AttributeValue, String>("value"));
		dbCol.setPrefWidth(180);
		idCol.setPrefWidth(250);
//		idList.setRowFactory((a) -> {
//		       return new DraggableTableRow<AttributeValue>(idList, Controller.GENE_MIME_TYPE, this, geneListRecord);
//			});
		

          

	} 
//----------------------------------------------------------------------------------
	static String BDB = "http://webservice.bridgedb.org/";
	SimpleStringProperty species = new SimpleStringProperty();

	private void setSpeciesInfo(String newSpecies)
	{
		if (newSpecies.equals("Unspecified"))  newSpecies = "Human";		//TODO  HACK
		species.set(newSpecies);
		readDataSources();	
		readTargetSources(newSpecies);
	}
	public String getSpecies()		{ 		return species.get();} 
//	//--------------------------------------------------------------------------------

    String[] sources = {"Ensembl", "Entrez Gene","HGNC", "Wikigenes"};
    
	@FXML public void doLookup()
	{
		String species = "Human";
		String s = entry.getText();
		if (StringUtil.isEmpty(species)) return;
//		String urlStr = BDB + species + "/H/BRCA1";
		String urlStr = BDB + species + "/xrefs/H/" + s;
		String response = StringUtil.callURL(urlStr, true);
		System.out.println(response);
		String lines[] = response.split("\n");
		idList.getItems().clear();
		for (String line : lines)		
		{
			String [] flds = line.split("\t");
			if (flds.length == 2)
			{
				for (String database : sources)
					if (flds[1].equals(database))
						idList.getItems().add(new AttributeValue(database, flds[0]));
			}
		}
	}

	
	@FXML public void doCancel()
	{
//		System.err.println("Cancel");
		dialog.hide();
	}

	@FXML public void addNode()
	{
		String text = entry.getText();
		AttributeMap map = new AttributeMap("ShapeType", "GeneProduct");
		map.addPoint(100., 100.);
		map.put("TextLabel",  text	);
		map.put("Layer",  "Content"	);
		map.put("Resizable", "false");
		List<AttributeValue> selected = idList.getSelectionModel().getSelectedItems();
		String db = null;
		String id = null;
		if (selected.size() > 0)
		{
			AttributeValue first = selected.get(0);
			db = first.getAttribute();
			id = first.getValue();
			map.put("Database",  db	);
			map.put("ID",   id);
		}
		DataNode mNode = new DataNode(map, model);
		if (db != null) mNode.setDatabase(db);
		if (id != null) mNode.setDbid(id);
		model.getController().addDataNode(mNode);	
	}

	
//	private List<TreeItem<String>> getSelectedTargets() {
//		return idList.getSelectionModel().getSelectedItems();
//	}
	void testStringOnAllPatterns(String test)
	{
		for (DataSourceRecord rec : allDataSources)
			rec.patternMatch(test);
	}

		//----------------------------------------------------------------------------------
	ObservableList<DataSourceRecord> allDataSources = FXCollections.observableArrayList();
	ObservableList<DataSourceRecord> minimalDataSources = FXCollections.observableArrayList();
	ObservableList<DataSourceRecord> matchingDataSources = FXCollections.observableArrayList();
	
	public void readDataSources()
	{
		allDataSources.clear();
		minimalDataSources.clear();
		matchingDataSources.clear();	     
		URL resource = getClass().getResource("datasourcesSubset.txt");
		String absolutePath = resource.toExternalForm().substring("file:".length() );
		List<String> lines = FileUtil.readFileIntoStringList(absolutePath);
		for (String line : lines)			// read in all DataSourceRecords unfiltered
		{
			if (line.trim().length() == 0) continue;
			if (line.startsWith("datasource_name")) continue;		// ignore first line as column heads
			DataSourceRecord rec= new DataSourceRecord(line);
			allDataSources.add(rec);
		}
		buildSourceMap(allDataSources);		// be able to convert system <-> name
//		loadSourceTable();					// filter full set down to a few and populate the table
	}

		//--------------------------------------------------------------------------------
	HashMap<String, String> systemToNameLookup;
	HashMap<String, String> nameToSystemLookup;
	
	private void buildSourceMap(List<DataSourceRecord> sources) {
		systemToNameLookup= new HashMap<String, String>();
		nameToSystemLookup= new HashMap<String, String>();
		for (DataSourceRecord rec : sources)
		{
			String name = rec.getName();
			String sys=rec.getSystem();
			systemToNameLookup.put(sys, name);
			nameToSystemLookup.put(name, sys);
		}
	}
	//--------------------------------------------------------------------------------
	// called when the organism is set, filters down targets for that species
	ObservableList<String> speciesTargets = FXCollections.observableArrayList();
	
	public void readTargetSources(String species)
	{
		species = "Human";
		if (StringUtil.isEmpty(species)) return;
		String urlStr = BDB + species + "/targetDataSources";
		String response = StringUtil.callURL(urlStr, true);
		System.out.println(urlStr + "\n\n" + response+ "\n\n");
		speciesTargets.clear();
		for (String s : response.split("\n"))
			if (!s.trim().isEmpty())
				speciesTargets.add(s);
		Collections.sort(speciesTargets);
	}

	//--------------------------------------------------------------------------------

}
