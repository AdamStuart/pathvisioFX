package diagrams.pViz.app;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import javafx.collections.FXCollections;
import javafx.print.PrinterJob;
import javafx.stage.FileChooser;
import model.TableType;
import model.bio.Gene;
import model.bio.GeneListRecord;
import model.bio.Species;
import util.FileUtil;
import util.StringUtil;
/*
 * Document takes care of reading the state from a file, or writing it to one.
 */
public class Document
{
	
	private Controller controller;
	private File file = null;
	private int verbose = 0;
	
	public Document(Controller inDC)
	{
		controller = inDC;
	}
	// **-------------------------------------------------------------------------------
	public void open(String s)		
	{ 	
		if (StringUtil.isXML(s))
		try
		{
			org.w3c.dom.Document doc = FileUtil.convertStringToDocument(s);	//  parse string to XML
			if (doc != null)
				controller.addXMLDoc(doc);					
		}
		catch (Exception e) 
		{
			e.printStackTrace();
		}
		else System.err.println("open expected xml: " + s.substring(40));
	}
	// **-------------------------------------------------------------------------------
	public static GeneListRecord readCDT(File f, Species species)
	{
		try
		{
			TableType type = TableType.TXT;
			if ( FileUtil.isCDT(f)) type = TableType.CDT;
			if ( FileUtil.isTXT(f)) type = TableType.TXT;
			List<Gene> geneList = FXCollections.observableArrayList();
			GeneListRecord record = new GeneListRecord(f.getName());
			record.setSpecies(species.common());
			record.setName(f.getName());
			List<String> lines = FileUtil.readFileIntoStringList(f.getAbsolutePath());
			if (lines.size() > 0)
			{
				for (int i=0; i< type.getNHeaderRows(); i++)
					record.addHeader(lines.get(i));
				int skip = type.getNHeaderRows();
				for (String line : lines)
				{
					if (skip>0) { skip--;  continue; }
					Gene g = new Gene(record, type, line);
//					Gene existing = Model.findInList(geneList, g.getName());		// slow?
//					if (existing == null)
						geneList.add(g);
				}
			}
			record.setColumnList();
			record.setGeneList(geneList);
			return record;
		}
		catch (Exception e) 
		{
			e.printStackTrace();
		}
		return null;

	}
	
	public void open(File f)		
	{ 	
		try
		{
			if (FileUtil.isCDT(f))
				controller.setGeneList(readCDT(f, controller.getSpecies()));
			else if (FileUtil.isGPML(f) || FileUtil.isXML(f))
			{
				org.w3c.dom.Document doc = FileUtil.openXML(f);
				if (doc != null)
					controller.addXMLDoc(doc);		//  parse XML to Model and MNodes
			}
		}
		catch (Exception e) 
		{
			e.printStackTrace();
		}
		
	}
	// **-------------------------------------------------------------------------------
	public void open()			// ask for the file
	{ 	
		FileChooser chooser = new FileChooser();	
		chooser.setTitle("Open Drawing");
		file = chooser.showOpenDialog(App.getInstance().getStage());
		if (file != null)				// dialog wasn't canceled
			open(file);			
	}
	// **-------------------------------------------------------------------------------
	public void save()		
	{ 	
		if (file == null)
		{
			FileChooser chooser = new FileChooser();	
			chooser.setTitle("Save Drawing");
			file = chooser.showSaveDialog(App.getInstance().getStage());
			if (file == null) return;
			App.getInstance().getStage().setTitle(file.getName());
		}
		if (verbose > 0) System.out.println("about to do the save traversal");
		String buff =  controller.getModel().saveState();
		if (verbose > 0) System.out.println(buff);
		 try (FileOutputStream out = new FileOutputStream(file)) 
		 {
		    out.write( buff.getBytes());
			 out.close();
		 } 
		 catch (IOException e) {     e.printStackTrace();  }
	}
	
	public void saveas()		
	{ 	
		file = null;
		save();	
	}
	// **-------------------------------------------------------------------------------
	boolean fileDirty = false;
	
	public void close()		
	{ 	
//		if (fileDirty)	askToSave();
		file = null;
	}
	// **-------------------------------------------------------------------------------
	// TODO:  only prints first page, without scaling it.
	
	public void print()
	{
		PrinterJob job = PrinterJob.createPrinterJob();
		if (job == null) return;
		boolean success = job.printPage(controller.getPasteboard());
		if (success)
			job.endJob();
	}
}
