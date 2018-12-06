package diagrams.pViz.gpml;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import diagrams.pViz.app.Controller;
import diagrams.pViz.model.Model;
import diagrams.pViz.model.nodes.DataNode;
import diagrams.pViz.model.nodes.DataNodeGroup;
import diagrams.pViz.model.nodes.DataNodeState;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.scene.control.Label;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTablePosition;
import javafx.scene.control.TreeTableView;
import javafx.scene.control.cell.ComboBoxTreeTableCell;
import javafx.scene.control.cell.TextFieldTreeTableCell;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import model.bio.XRefable;
import model.bio.XRefableSection;

public class GPMLTreeTableView implements ChangeListener {
	
	TreeTableView<XRefable> treeView;
	Controller controller;
	Model model;
	boolean verbose = false;
	public GPMLTreeTableView(TreeTableView<XRefable> theView, Controller c)
	{
		treeView = theView;
		controller = c;
		model = c.getModel();
	}
	TreeItem<XRefable> root = makeTreeItem();
	TreeItem<XRefable> nodes = makeTreeItem();
	TreeItem<XRefable> shapes = makeTreeItem();
	TreeItem<XRefable> labels = makeTreeItem ();
	TreeItem<XRefable> groups = makeTreeItem ();
	TreeItem<XRefable> orphans = makeTreeItem ();
	Label size, select;
	
	TreeItem<XRefable> makeTreeItem()	{	return makeTreeItem(null);	}
	TreeItem<XRefable> makeTreeItem(DataNode node)
	{
		TreeItem<XRefable>  item = new TreeItem<XRefable> (node);
//		item.expandedProperty().addListener( );
		return item;
	}
	@Override
	public void stateChanged(ChangeEvent e) {
System.out.println("stateChanged " + e.toString());		
	}
	public void setup(Label inSize, Label inSelect)
	{
		size = inSize;
		select = inSelect;
		if (treeView != null)
		{
			root = makeTreeItem();
			treeView.setRoot(root);
			nodes.setValue(new XRefableSection("Nodes"));
			shapes.setValue(new XRefableSection("Shapes"));
			labels.setValue(new XRefableSection("Labels"));
			groups.setValue(new XRefableSection("Groups"));
			orphans.setValue(new XRefableSection("Orphans"));
//			root.getChildren().addAll(nodes, shapes, labels, groups);
			root.setExpanded(true);
			model.getNodes().stream().forEach((node) -> {
				nodes.getChildren().add(makeTreeItem(node));
		        });
		       
			treeView.setStyle(Controller.CSS_Gray2);		
	        TreeTableColumn<XRefable, String> nodeCol = new TreeTableColumn<>("Node");
	        TreeTableColumn<XRefable, String> xrefdbCol = new TreeTableColumn<>("Database");
	        TreeTableColumn<XRefable, String> xrefidCol = new TreeTableColumn<>("ID");
	        TreeTableColumn<XRefable, String> graphidCol = new TreeTableColumn<>("Graph Id");
		
	        nodeCol.setPrefWidth(150);
	        xrefdbCol.setPrefWidth(80);
	        xrefidCol.setPrefWidth(125);
	        graphidCol.setPrefWidth(65);
	     
	        nodeCol.setCellValueFactory(
	                (TreeTableColumn.CellDataFeatures<XRefable, String> param) -> 
	        		{
	        			XRefable g = param.getValue().getValue();
	        			String name = g == null ? "All Nodes" : g.getName();
	        	        return  new ReadOnlyStringWrapper(name);
	      		} );
	        graphidCol.setCellValueFactory(
	                (TreeTableColumn.CellDataFeatures<XRefable, String> param) -> 
	                {
	                	XRefable g = param.getValue().getValue();
	        			String s = (g == null) ? "" : g.getGraphId();
	        	        return  new ReadOnlyStringWrapper(s);
	                 }
	                );
	        xrefdbCol.setCellValueFactory(
	                (TreeTableColumn.CellDataFeatures<XRefable, String> param) -> 
	                {
	                	XRefable g = param.getValue().getValue();
	        			String s = g == null ? "" : g.getDatabase();
	        	        return  new ReadOnlyStringWrapper(s);
	                 }
	                );

	        ObservableList<String> dbList = FXCollections.observableArrayList();
	        dbList.addAll("Ensembl","Entrez","HGNC","HMDB","HCA","WikiPathways","GoProcess","GoFunction","CHEBI","Funk&Wagnells");
	        
	        xrefdbCol.setCellFactory(ComboBoxTreeTableCell.forTreeTableColumn(dbList));
	        xrefdbCol.setOnEditCommit(new EventHandler<TreeTableColumn.CellEditEvent<XRefable,String>>() {
				@Override public void handle(TreeTableColumn.CellEditEvent<XRefable, String> event) {
					TreeItem<XRefable> cur = treeView.getTreeItem(event.getTreeTablePosition().getRow());
					cur.getValue().setDatabase(event.getNewValue());
					cur.getValue().put("Database",event.getNewValue());
					}  
	        	}  );

	        xrefidCol.setCellValueFactory(
	                (TreeTableColumn.CellDataFeatures<XRefable, String> param) -> 
	                {
	                	XRefable g = param.getValue().getValue();
	          			String s = g == null ? "" : g.getDbid();
	          	        return  new ReadOnlyStringWrapper(s);
	                   }
	               );
	        xrefidCol.setCellFactory(TextFieldTreeTableCell.forTreeTableColumn());
	        xrefidCol.setOnEditCommit(new EventHandler<TreeTableColumn.CellEditEvent<XRefable,String>>() {
				@Override public void handle(TreeTableColumn.CellEditEvent<XRefable, String> event) {
					TreeItem<XRefable> cur = treeView.getTreeItem(event.getTreeTablePosition().getRow());
					cur.getValue().setDbid(event.getNewValue());
					cur.getValue().put("ID",event.getNewValue());
				}  
	        	}  );
	        treeView.setEditable(true);
			//-------
//			nodeTable.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
	        treeView.getColumns().clear();		// throw away the definition from the fxml !
	        treeView.getColumns().setAll(nodeCol, xrefdbCol, xrefidCol, graphidCol);
//	        treeView.setRowFactory((a) -> {
//			       return new DraggableTreeTableRow<XRefable>(treeView, Controller.GENE_MIME_TYPE, this, controller.getGeneSetRecord());
//				});
	//	
	        treeView.setShowRoot(false);
	        treeView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
	        treeView.getSelectionModel().setCellSelectionEnabled(true);
			// bind two labels:  select and size to the number selected and number of rows, e.g.: (32 / 2092)
//			select.setAlignment(Pos.CENTER_RIGHT);
//			select.setText("0");
//			size.setText("0");
	        treeView.getSelectionModel().getSelectedIndices().addListener(new ListChangeListener<Integer>()
			{
				@Override public void onChanged(Change<? extends Integer> change) 
				{  
					String txt = "" + treeView.getSelectionModel().getSelectedIndices().size();
					if (select != null) select.setText(txt); 
					String siz = "/ " + treeView.getExpandedItemCount();  
					if (size != null) size.setText(siz); 	
					for (int idx : treeView.getSelectionModel().getSelectedIndices())
					{
						TreeItem<XRefable> cur = treeView.getTreeItem(idx);
						if (cur != null)
						{
							String id = cur.getValue().getGraphId();
							DataNode datanode = model.findDataNode(id);
							if (datanode != null)
							{
								System.out.println(datanode.getName());
//								pasteboard.getSelectionMgr().select(datanode.getStack(),true);

							}
						}
					}
				}
			}
		);
			
//			nodeTable.getRoot().getChildren().addListener(new ListChangeListener<Gene>()
//			{
//				@Override public void onChanged(Change<? extends Gene> change) { size.setText("/ " + nodeTable.getItems().size()); }
//			});
	        final KeyCodeCombination keyCodeCopy = new KeyCodeCombination(KeyCode.C, KeyCombination.CONTROL_ANY);
	        treeView.setOnKeyPressed(event -> {
	            if (keyCodeCopy.match(event)) {
	                copySelectionToClipboard();
	            }
	        });		        
//	        treeView.setOnKeyPressed( keyEvent -> {
//					if (keyEvent.getCode().equals(KeyCode.DELETE) || keyEvent.getCode().equals(KeyCode.BACK_SPACE)) {
//						List<Integer> ids = treeView.getSelectionModel().getSelectedIndices();
//						int sz = ids.size();
//						for (int i = sz - 1; i >= 0; i--)
//							treeView.getRoot().getChildren().remove(i);
//						treeView.getSelectionModel().clearSelection();
//					}
//				}
//			});
//			search.setTooltip(new Tooltip(tooltip));
//			searchBox.setTooltip(new Tooltip(tooltip));
		}
	}
	// **-------------------------------------------------------------------------------
	@SuppressWarnings("rawtypes")
	public void copySelectionToClipboard() {
		final StringBuilder strb = new StringBuilder();
	    boolean firstRow = true;
	    for (int idx : treeView.getSelectionModel().getSelectedIndices()) {
	        if (!firstRow) {
	            strb.append('\n');
	        }
	        firstRow = false;
	        boolean firstCol = true;
	        for (final TreeTableColumn<?, ?> column : treeView.getColumns()) {
	            if (!firstCol) {
	                strb.append('\t');
	            }
	            firstCol = false;
	            final Object cellData = column.getCellData(idx);
	            strb.append(cellData == null ? "" : cellData.toString());
	        }
	    }
	    final ClipboardContent clipboardContent = new ClipboardContent();
	    clipboardContent.putString(strb.toString());
	    Clipboard.getSystemClipboard().setContent(clipboardContent);
	}
	boolean disabled = false;
	public void disableUpdates() {	 disabled = true;	}
	public void resumeUpdates() {	 disabled = false;	}
	// **-------------------------------------------------------------------------------
	public void updateTreeTable() 
	{
		if (disabled ) return;
		System.out.println("GPMLTreeTableView.updateTreeTable");
		root.getChildren().clear();
		if (model.getDataNodeMap().size() > 0) { nodes.getChildren().clear();   root.getChildren().add(nodes);  }
		if (model.getShapes().size() > 0)  
		{ 
			shapes.getChildren().clear(); 
			root.getChildren().add(shapes); 
		}
		if (model.getLabels().size() > 0) 
		{ 
			labels.getChildren().clear(); 
			root.getChildren().add(labels);
		}
		if (model.getGroups().size() > 0)  
		{ 
			groups.getChildren().clear(); 
			root.getChildren().add(groups);
		}
		orphans.getChildren().clear(); 		// only add it if it gets children from the interaction processing
		
////		root.getChildren().addAll(nodes, shapes, labels, groups);
//		ObservableList<TreeItem<XRefable>> kids =  nodes.getChildren();
//		kids.clear();
		nodes.setExpanded(true);
		model.getNodes().stream().forEach((node) -> {			addBranch(node);        });
		
		model.getEdges().stream().forEach((interaction) -> {
			String origin = interaction.get("sourceid");
			interaction.setNameFromState();
//			System.out.println("interaction " + interaction.get("GraphId") + " starts at " + origin);

			TreeItem<XRefable> origItem = findDeep(root,origin);
			if (origItem == null)  // source of interaction is an anchor
			{
//				DataNode node = model.findDataNode(origin);
//				System.err.println("origItem  == null");
				addBranch(interaction, orphans);
			}
			else addBranch(interaction, origItem);
        });
		
		if (orphans.getChildren().size() > 0)
			root.getChildren().add(orphans);
		
		model.getStates().stream().forEach((state) -> {
			String origin = state.get("GraphRef");
			state.setName(state.get("TextLabel"));
			TreeItem<XRefable> origItem = findDeep(nodes,origin);
			if (origItem == null)  reportError(state);
			else	addBranch(state, origItem);
        });
		
		model.getEdges().stream().forEach((edge) -> {
			
			List<Anchor> anchors = edge.getAnchors();
			if (anchors == null) return;
			for (Anchor a : anchors)
			{
				String interactionId = a.getInteractionId();
				TreeItem<XRefable> interactionRow = findDeep(nodes,interactionId);
				if (interactionRow == null)
					System.out.println("Error");
				else
				{
					a.setGraphId(a.get("GraphId"));
					addBranch(a, interactionRow);
				}
			}
        });
		model.getGroups().stream().forEach((group) -> {
			addBranch(group);
        });
		 
		System.out.println("======================\n\n\n");
		dumpDeep(nodes, "", 0);
//		model.resetEdgeTable();
	}

	private void reportError(DataNodeState state) {
		// TODO Auto-generated method stub
		
	}
	// **-------------------------------------------------------------------------------
	public boolean columnExists(String s) {

		for (TreeTableColumn<XRefable, ?> col : treeView.getColumns())
			if (s.equals(col.getText()))
			return true;
		return false;
	}
	
	public void addColumn(String s) {

		TreeTableColumn<XRefable, String> col = new TreeTableColumn<XRefable, String>();
		col.setText(s);
		col.setCellValueFactory(
                (TreeTableColumn.CellDataFeatures<XRefable, String> param) -> 
        		{
        			XRefable g = param.getValue().getValue();
        			String name = g == null ? "" : g.get(col.getText());
        	        return  new ReadOnlyStringWrapper(name);
      		} );
		 treeView.getColumns().add(col);
	}
	
	public List<String> getNumericColumns()
	{
		List<String>cols = new ArrayList<String>();
		List<TreeTableColumn<XRefable, ?>>all =  treeView.getColumns();
		for (TreeTableColumn col : all)
			cols.add(col.getText());
//		if (col instanceof TreeTableColumn<XRefable, String>)
//				cols.add(col);
		return cols;	
	}
	
	public void fillDown() {
		ObservableList<TreeTablePosition<XRefable, ?>> selectedCells = treeView.getSelectionModel().getSelectedCells();
		TreeTablePosition<XRefable, ?> tablePosition = (TreeTablePosition<XRefable, ?>) selectedCells.get(0);
		String val = "" + tablePosition.getTableColumn().getCellData(tablePosition.getRow());
		TreeTableColumn<XRefable, ?> col = tablePosition.getTableColumn();
		String colName = col.getText();
		List<Integer> rows = treeView.getSelectionModel().getSelectedCells().stream().map(pos -> pos.getRow()).collect(Collectors.toList());
		for (TreeTablePosition<XRefable, ?> pos : selectedCells) {
			if (pos.getTableColumn() == col && pos.getRow() != tablePosition.getRow()) {
				Object obj = pos.getTableColumn().getCellData(pos.getRow());
				TreeItem<XRefable> rowItem = treeView.getTreeItem(pos.getRow());
//				XRefable row = rowItem.getValue();
				rowItem.getValue().put(colName, val);
				rowItem.getValue().copyAttributesToProperties();
				System.out.println(obj);
			}
		}
	}
	// **-------------------------------------------------------------------------------

	public void addBranch(DataNode node) {
		String type = node.get("Type");
//		node.get("GraphId");
		System.out.println(type + " " + 	node.getGraphId());
		
		if  ("Label".equals(type))  				addBranch(node, labels);
		else if ("Shape".equals(type))  			addBranch(node, shapes);
		else if (node instanceof DataNodeGroup)  	addBranch(node, groups);
		else if (node instanceof DataNode)  		addBranch(node, nodes);
	}

	public void addBranch(XRefable node, String parentID) {
		TreeItem<XRefable> mom = findDeep(nodes, parentID);
		if (mom == null)  System.err.println("Null parent: " + parentID);
		else
			addBranch(node, mom);
	}

	private void addBranch(XRefable node, TreeItem<XRefable> parent) {
		TreeItem<XRefable> anItem = makeTreeItem();
		anItem.setValue(node);
		parent.getChildren().add(anItem);
	}

	// **-------------------------------------------------------------------------------

	public TreeItem<XRefable> findNodeTreeItem(String inId) {		return findNode(inId);	}
	
	public TreeItem<XRefable> findNode(String inId) {
		
		if (inId == null) return null;
		for (TreeItem<XRefable> branch : nodes.getChildren())
		{
			String id = branch.getValue().getGraphId();
			if (inId.equals(id))
				return branch;
		}
		return null;
	}
	private TreeItem<XRefable> findDeep(TreeItem<XRefable> inRoot, String inId) {
		
		if (inId == null) return null;
		if (inId.isEmpty()) return null;
		XRefable val = inRoot.getValue();
//		if (inRoot.getValue() == null) return null;
		if (val != null && inId.equals(val.getGraphId()))
			return inRoot;
		for (TreeItem<XRefable> branch : inRoot.getChildren())
		{
			XRefable xref =  branch.getValue();
			for (TreeItem<XRefable> subbranch : branch.getChildren())
			{	
				TreeItem<XRefable> hit = findDeep(subbranch, inId);
				if (hit != null) 
					return hit;
			}
		}
		return null;
	}

//	private TreeItem<XRefable> findNodeShallow(DataNode origin) {
//		for (TreeItem<XRefable> branch : nodes.getChildren())
//			if (branch.getValue().getGraphId().equals(origin.getGraphId()))
//				return branch;
//		return null;
//	}	
//	
	//---------------------------------------
	private void dumpDeep(TreeItem<XRefable> inRoot, String prefix, int depth) {
		
		XRefable ref = inRoot.getValue();
		if (ref == null) System.out.println("section head ");
		else	dump(prefix, ref, depth);  
		for (TreeItem<XRefable> branch : inRoot.getChildren())
		{
			XRefable xref =  branch.getValue();
//			dump(prefix, xref, depth);
			for (TreeItem<XRefable> subbranch : branch.getChildren())
				dumpDeep(subbranch,">  " + prefix, depth+1);
		}
	}
	private void dump(String prefix, XRefable xref, int depth) 
	{
		String id = xref.getGraphId();
		if (id == null)  id = xref.get("GraphId");
		System.out.println(prefix + xref.getName() + " " +  id + " ");		//xref.get("Type") + " " +
		
	}
}
