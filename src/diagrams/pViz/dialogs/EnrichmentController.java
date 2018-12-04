package diagrams.pViz.dialogs;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.ResourceBundle;

import diagrams.pViz.model.Model;
import diagrams.pViz.tables.idmap.DataSourceRecord;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.ButtonBar.ButtonData;
import model.QuadValue;
import util.FileUtil;
import util.StringUtil;

public class EnrichmentController implements Initializable
{
	@FXML private Button search;
	private Model model;
	private EnrichmentDialog dialog;

	@FXML ListView<String> targetList;
	//----------------------------------------------------------------------------------
	@Override public void initialize(URL location, ResourceBundle resources)
	{
		targetList.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
//		targetList.getSelectionModel().selectedItemProperty().addListener(ev->{ targetSelectionChanged(ev);});
     }
	
	public void setup(EnrichmentDialog dlog, Model m) {
		model = m;
		dialog = dlog;
		setSpeciesInfo(m.getSpecies().common());
		ButtonType enrichButton = new ButtonType("Enrich", ButtonData.OK_DONE);
		dialog.getDialogPane().getButtonTypes().addAll(enrichButton, ButtonType.CANCEL);
		
	} 
//----------------------------------------------------------------------------------
	static String BDB = "http://webservice.bridgedb.org/";
	SimpleStringProperty species = new SimpleStringProperty();

	private void setSpeciesInfo(String newSpecies)
	{
		species.set(newSpecies);
		readDataSources();	
		readTargetSources(newSpecies);
	}
	public String getSpecies()		{ 		return species.get();} 
//	//--------------------------------------------------------------------------------
	@FXML public void doCancel()
	{
//		System.err.println("Cancel");
		dialog.hide();
	}

	@FXML public void doSearch()
	{
		String error = "";
		List<String> targets = getSelectedTargets();	if (targets.isEmpty()) 			error += " NoTarget";
		if (!StringUtil.isEmpty(error))
		{
			System.err.println("doSearch encounted errors: " + error);
			return;
		}
		List<QuadValue> records = new ArrayList<QuadValue>();
		String input = model.getXRefs();
		String lines[] = input.split("\n");
		for (String line : lines)		
		{
			if (!line.trim().isEmpty())
			{
				String[] fields = line.split("\t");
//				testStringOnAllPatterns(line);
				String source = fields[0];
				if (StringUtil.isEmpty(source))		continue;
				String val = fields[1];
				if (StringUtil.isEmpty(val))		continue;
				String urlStr = BDB + species.get() + "/xrefs/" + nameToSystemLookup.get(source) + "/" + val;
				String response = StringUtil.callURL(urlStr, true);
				System.out.println(urlStr);
				System.out.println(response);
				for (String target : targets)
				{	
					String mappedId = lookup(response, target);
					if (mappedId != null)
					{	
						QuadValue record = new QuadValue(source, val, target, mappedId);
						records.add(record);
					}
				}
			}
		}
		model.addFields(records);
	}

	private String lookup(String response, String target) {
		String lines[] = response.split("\n");
		for (String line : lines)		
		{
			String [] flds = line.split("\t");
			if (flds.length == 2)
				if (flds[1].equals(target))
					return flds[0];
		}
		return null;
	}
	
	private List<String> getSelectedTargets() {
		return targetList.getSelectionModel().getSelectedItems();
	}
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
		if (StringUtil.isEmpty(species)) return;
		String urlStr = BDB + species + "/targetDataSources";
		String response = StringUtil.callURL(urlStr, true);
		System.out.println(urlStr + "\n\n" + response+ "\n\n");
		speciesTargets.clear();
		targetList.getItems().clear();
		for (String s : response.split("\n"))
			if (!s.trim().isEmpty())
				speciesTargets.add(s);
		targetList.getItems().addAll(speciesTargets);
	}

	//--------------------------------------------------------------------------------

}
