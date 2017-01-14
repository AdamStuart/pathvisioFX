package diagrams.pViz.tables;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import animation.BorderPaneAnimator;
import diagrams.pViz.app.Action.ActionType;
import diagrams.pViz.app.Controller;
import diagrams.pViz.app.IController;
import diagrams.pViz.app.UndoStack;
import gui.DropUtil;
import icon.FontAwesomeIcons;
import icon.GlyphIcon;
import icon.GlyphsDude;
import javafx.collections.ListChangeListener;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Side;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.DataFormat;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.util.Callback;
import model.bio.Species;
import model.bio.TableRecord;

abstract public class TableController<ROWTYPE extends Map> implements IController, Initializable {

	abstract protected void processFile(File f);
	abstract void createTableRecord();

	public DataFormat getMimeType() {		return DataFormat.PLAIN_TEXT;	}
	protected void doSearch() {			}		// default is to do nother

	protected TableRecord<ROWTYPE> tableRecord = null;
	
	@FXML protected ChoiceBox<String> species;
	@FXML protected Button search;
	@FXML protected TextField searchBox;
	@FXML protected BorderPane container;
	@FXML protected TableView<TableColumn> columnTable;
	@FXML protected TableColumn<TableColumn, String> typeColumn;
	@FXML protected TableColumn<TableColumn, String> colNameColumn;
	@FXML protected TableColumn<TableColumn, Double> widthColumn;
	@FXML protected TableView<ROWTYPE> theTable;
	private  UndoStack undoStack;

	public static final DataFormat  COLUMN_MIME_TYPE = new DataFormat("application/x-java-index-of-column");
	@Override public void initialize(URL location, ResourceBundle resources) {
		createTableRecord();
		undoStack = new UndoStack(this, null);
	
		Controller.setGraphic(westSidebar, FontAwesomeIcons.ARROW_CIRCLE_O_RIGHT);
		new BorderPaneAnimator(container, westSidebar, Side.LEFT, false, 250);
		westSidebar.fire();		// start with columns hidden
		//-------
		typeColumn.setSortable(false);
		colNameColumn.setSortable(false);
		widthColumn.setSortable(false);
		typeColumn.setCellValueFactory(new PropertyValueFactory<TableColumn, String>("type"));
		colNameColumn.setCellValueFactory(new PropertyValueFactory<TableColumn, String>("text"));
		widthColumn.setCellValueFactory(new PropertyValueFactory<TableColumn, Double>("width"));
		

		typeColumn.setCellValueFactory(c);
		
		columnTable.setRowFactory((a) -> {
			  return new DraggableTableRow<TableColumn>(columnTable, COLUMN_MIME_TYPE, this, tableRecord);
		  });
		columnTable.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
		// inherit table with columnEditing
		
		if (species != null)
		{
			// TODO make species control
			String[] organisms = { "Any", "Homo sapiens", "Mus musculus", "Rattus norvegicus", "Canis familiarus", "Box taurus", "Pan troglodytes", "Gallus gallus" };
			species.getItems().addAll(organisms);
			species.getSelectionModel().select(1);
		}
		if (search != null)
		{
			search.setGraphic(GlyphsDude.createIcon(FontAwesomeIcons.SEARCH, GlyphIcon.DEFAULT_ICON_SIZE));
			search.setText("");
			searchBox.setText("");
			searchBox.addEventHandler(KeyEvent.KEY_PRESSED, e -> 
			{  if (e.getCode() == KeyCode.ENTER) doSearch();	} );
		}
		DropUtil.makeFileDropPane(theTable, e -> { doDrag(e); });
		String url = "";				// TODO
	//	findGenes(url);
		columnTable.getItems().addListener(resetColumnOrder);
	}	

	Callback c = new Callback< TableColumn.CellDataFeatures<TableColumn.CellDataFeatures, String>, TableCell<TableColumn.CellDataFeatures, String>>() 
	{
		@Override public TableCell<TableColumn.CellDataFeatures, String> call(TableColumn.CellDataFeatures<TableColumn.CellDataFeatures, String> param)
		{ return tableCell;}
	};
	TableCell<TableColumn.CellDataFeatures, String> tableCell = new TableCell<TableColumn.CellDataFeatures, String>() {
		@Override public void updateItem(String s, boolean empty)
		{
			if (s != null)
			{
				setGraphic(GlyphsDude.createIcon(FontAwesomeIcons.ADJUST));
			}
		}
	};
	
//	Callback< TableColumn<?, String>, TableCell<?, String>> factory = new CellValueFactory< ?, String>, TableCell<?, String>>() 
//	{
//		@Override public TableCell<?, String> call(TableColumn<?, String> param)	{ return tableCell;}
//	};
	
	public void doDrag(DragEvent e)
	{
		getUndoStack().push(ActionType.New);
		Dragboard db = e.getDragboard();
		importing = true;
		for (File f : db.getFiles())
			processFile(f);
		e.consume();
		resetTableColumns();
		importing = false;
	}

	private UndoStack getUndoStack() {
		
		return undoStack;
	}

	@FXML private Button westSidebar;
	private TableColumn<ROWTYPE, String> separatorColumn = new TableColumn<ROWTYPE, String>();
	protected List<TableColumn<ROWTYPE, ?>> allColumns = new ArrayList<TableColumn<ROWTYPE, ?>>();
	protected void makeSeparatorColumn() {
		allColumns.add(separatorColumn);  
		separatorColumn.setPrefWidth(0);  
		separatorColumn.setText("--------");		// TODO horizontal line in TableCell
	}
	public Species getSpecies() {
		String name = species.getSelectionModel().getSelectedItem();
		return Species.lookup(name);
	}

	public void resetTableColumns() {
		System.err.println("resetTableColumns");
		columnTable.getItems().clear();
		columnTable.getItems().addAll(allColumns);
		
		theTable.getColumns().clear();	
		List<String> vis = getVisColumns();
		for (String name : vis)
		{
			TableColumn<ROWTYPE, ?> col = findColumn(name);
			if (col != null) 
				theTable.getColumns().add(col);	
		}
	}
	public void setVisColumns(List<String> strs)
	{
		
	}
	public List<String> getVisColumns()
	{
		List<String> colNames = new ArrayList<String>();
		for (TableColumn col : allColumns)
		{
			String s = col.getText();
			if (s.startsWith("---")) break;
			colNames.add(s);
		}
		return colNames;
	}


	private TableColumn<ROWTYPE, ?> findColumn(String name) {
		for (TableColumn<ROWTYPE, ?> column : allColumns)
			if (column.getText().equals(name))
				return column;
		return null;
	}
	protected boolean importing = false;

	ListChangeListener<TableColumn> resetColumnOrder = new ListChangeListener<TableColumn>()
	{
		@Override public void onChanged(Change<? extends TableColumn> c)	
		{
			if (allColumns == null) return;		// this gets called too early in initialization
			if (importing) return;
//			if (allColumns != null) return;		// this gets called too early in initialization
			System.out.println("resetColumnOrder");
			theTable.getColumns().clear();
			List<String> vis = getVisColumns();
			for (String colName : vis)
			{
				TableColumn<ROWTYPE, ?> column =  findColumnByName(allColumns, colName);
				if (column != null)
					theTable.getColumns().add(column);
			}
			// readjust widths, etc
		}

	};

	private TableColumn<ROWTYPE, ?> findColumnByName(List<TableColumn<ROWTYPE, ?>> cols, String name) 
	{
		for (TableColumn<ROWTYPE, ?> column : cols)
		{
			String txt = column.getText();
			if (txt.equals(name))
				return column;
			if (txt.equals("Label") && name.equals("Name"))
				return column;
		}
		return null;
	}
	//	ListChangeListener<TableColumn> userReorderedColumns = new ListChangeListener<TableColumn>()
//	{
//		@Override public void onChanged(Change<? extends TableColumn> c)	
//		{
//			System.out.println("columnDragged " + allColumns.size());
////			if (allColumns.size() < 3393) return;
//			ObservableList<? extends TableColumn> list = c.getList();
////			String[] colNames = getColumnNames(list);		// names of columns show in gene table
//			String[] colNames = new String[list.size()];
//			for (int i=0; i<list.size(); i++)
//				colNames[i] = list.get(i).getText();
//			int idx = 0;
//			for (; idx < colNames.length; idx++)
//			{
//				int targetIdx = findColumnIndex(allColumns, colNames[idx]);
//				if (targetIdx > idx)
//				{
//					TableColumn<Gene,?> tc = allColumns.get(targetIdx);
//					allColumns.remove(tc);
//					allColumns.add(idx, tc);
//				}
//			}
//			if (allColumns.remove(separatorColumn))
//				allColumns.set(idx, separatorColumn);
//		}
//
//		int findColumnIndex(List<TableColumn<Gene,?>> cols, String t)
//		{
//			for (int i=0; i<cols.size(); i++)
//				if ( cols.get(i).getText().equals(t))
//					return i;
//			return -1;
//		}
//
//		private List<String> getColumnNames(ObservableList<? extends TableColumn> list) {
//			List<String> strs = new ArrayList<String>();
//			for (int i=0; i<list.size(); i++)
//				if (list.get(i).isVisible())
//					strs.add(list.get(i).getText());
//			return strs;
//		}
//		
//	};
}
