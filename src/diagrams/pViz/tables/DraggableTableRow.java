package diagrams.pViz.tables;

import diagrams.pViz.app.IController;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DataFormat;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import model.bio.TableRecord;


public class DraggableTableRow<IRecord> extends TableRow<IRecord> {
	private TableView<IRecord> table;
	private TableRow<IRecord> thisRow;
	private IController controller;
	private TableRecord tableRecord;
	public DraggableTableRow(TableView<IRecord> inTable, DataFormat mimeType, IController cntrlr, TableRecord rec)
	{
		table = inTable;
		controller = cntrlr;
		tableRecord = rec;
		setOnDragDetected(event -> {
        if (! isEmpty()) {
            Integer index = getIndex();
            IRecord r = table.getItems().get(index);
            String id = "";
            if (r != null)
            	id = r.toString();
            Dragboard db = startDragAndDrop(TransferMode.MOVE);
            db.setDragView(snapshot(null, null));
            ClipboardContent cc = new ClipboardContent();
            cc.put(mimeType, index);
            cc.put(DataFormat.PLAIN_TEXT, id);
            db.setContent(cc);
            event.consume();
            thisRow = (TableRow<IRecord>) this;
        }
    });

    setOnDragEntered(event -> {
        Dragboard db = event.getDragboard();
        if (db.hasContent(mimeType)) {
            
        	Object obj = db.getContent(mimeType);
        	event.acceptTransferModes(TransferMode.MOVE);
            event.consume();
            
//            if (obj instanceof Integer)
//        	{
//	        	if (getIndex() != ((Integer)obj).intValue()) {
//	                event.acceptTransferModes(TransferMode.MOVE);
//	                event.consume();
//	                thisRow = thisRow;
//	//              if (thisRow != null) 
//	//             	   thisRow.setOpacity(0.3);
//	            }
//        	}
        }
    });

    setOnDragExited(event -> {
        if (event.getGestureSource() != thisRow &&
                event.getDragboard().hasString()) {
//           if (thisRow != null) 
//        	   thisRow.setOpacity(1);
           thisRow = null;
        }
    });

    setOnDragOver(event -> {
        Dragboard db = event.getDragboard();
        if (db.hasContent(mimeType)) {
//            if (getIndex() != ((Integer)db.getContent(mimeType)).intValue()) {
                event.acceptTransferModes(TransferMode.MOVE);
                event.consume();
//            }
        }
    });

      setOnMouseClicked(event -> {
          int idx = getIndex();
          String colName = getColumnId(event.getX());
          if (controller != null) 
          {
        	  if (event.getClickCount() == 2)
              	controller.getInfo(mimeType, "" + idx, colName, event);	//r.getId()
              event.consume();
         }
    });

    setOnDragDropped(event -> {
        Dragboard db = event.getDragboard();
        if (db.hasContent(mimeType)) {
            Object obj = db.getContent(mimeType);
            if (obj instanceof Integer)
            {
	            int draggedIndex = (Integer) obj;
	            reorderRecords(draggedIndex, getIndex());
	            event.setDropCompleted(true);
	            event.consume();
	//            if (thisRow != null) 
	//         	   thisRow.setOpacity(1);
	            thisRow = null;
	            controller.resetTableColumns();
            }
            if (obj instanceof TableColumn)
            {
            	TableColumn col = (TableColumn) obj;
            	System.out.println("TableColumn");
            	
            }
       }
    });

	}
	private void reorderRecords(int draggedIndex, int index) {
        IRecord draggedNode = table.getItems().remove(draggedIndex);
        int  dropIndex = index; // (isEmpty()) ? table.getItems().size() : getIndex();
        table.getItems().add(dropIndex, draggedNode);
        table.getSelectionModel().clearAndSelect(dropIndex);
        controller.reorderColumns(draggedIndex,  index);
//        if (tableRecord != null)
//        	tableRecord.reorderColumns(draggedIndex,  index);
	}
	public  String getColumnId(double x)
    {
    	for (TableColumn col : table.getColumns())
    	{
    		double width = col.getWidth();
    		if (width > x) return col.getId();
    		x -= width;
    	}
     	return "";
    }

}
