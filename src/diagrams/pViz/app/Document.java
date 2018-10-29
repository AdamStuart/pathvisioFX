package diagrams.pViz.app;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import diagrams.pViz.tables.TableController;
import javafx.collections.FXCollections;
import javafx.print.PrinterJob;
import javafx.stage.FileChooser;
import javafx.util.Pair;
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
	public static GeneListRecord readTabularText(File f, Species species)
	{
		List<String> lines = FileUtil.readFileIntoStringList(f.getAbsolutePath(), 100000);
		TableType type = TableType.TXT;
		if ( FileUtil.isCDT(f)) type = TableType.CDT;
		if ( FileUtil.isTXT(f)) type = TableType.TXT;
		return readTable(f.getName(), type, lines, species);
	}
	
	public static boolean isMappingFile(File f)
	{
		List<String> lines = FileUtil.readFileIntoStringList(f.getAbsolutePath(), 5);
		Pair<Integer, Integer> dims = inferSizes(lines);
		int nHeaders = dims.getValue();
		int nColumns = dims.getKey();
		return (nHeaders == 1 && nColumns == 2);
	}
	
	public static Map<String, String> readMappingFile(File f, TableController<?> table)
	{
		HashMap<String, String> map = new HashMap<String, String>();
		List<String> lines = FileUtil.readFileIntoStringList(f);
		Pair<Integer, Integer> dims = Document.inferSizes(lines);
		int match = 0;
		int skip = dims.getValue();
		int keyIndex = 0;
		String keyName = "";
		String valueName = "";
		if (dims.getKey() != 2) return map;
		
		String[] flds = lines.get(0).split("\t");
		table.dumpColumns();
		for (int i=0; i < 2; i++)
		{
			String fld = flds[i];
			if (table.findColumn(fld) != null)
			{
				keyIndex = i;
				keyName = fld;
				valueName = flds[1-i];
				map.put("\tkey", keyName);
				map.put("\tvalue", valueName);
				match++;
			 }
		}
		if (match == 1)
		{
			for (String line : lines)
			{
				if (--skip >= 0) continue;
				String[] fld = line.split("\t");
				if (fld.length < 2) continue;
				if (StringUtil.isEmpty(fld[0]) || StringUtil.isEmpty(fld[1])) continue;
				map.put(fld[keyIndex], fld[1-keyIndex]);
			}
		}
		System.out.println(map.size() + " / " + lines.size() + " unique entries in the map");
		return map;
	}
	

	public static GeneListRecord readTable(String name, TableType type, List<String> lines, Species species)
	{
		try
		{
			String delimiter = "\t";
			Pair<Integer, Integer> dims = inferSizes(lines);
			int nHeaders = dims.getValue();
			int nColumns = dims.getKey();
			if (nHeaders == 1 && nColumns == 2)
				return null;
				
			List<Gene> geneList = FXCollections.observableArrayList();
			GeneListRecord record = new GeneListRecord(name);
			record.setSpecies(species.common());

			if (lines.size() > 0)
			{
				
				for (int i=0; i< nHeaders; i++)
					record.addHeader(lines.get(i));
				int skip = nHeaders;
				for (String line : lines)
				{
					if (skip>0) { skip--;  continue; }
					Gene g = new Gene(record, type, line.split(delimiter));
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
	
	public static Pair<Integer, Integer> inferSizes(List<String> lines) {
		int nHeaderLines = 0;
		int nColumns = 0;
		if (lines.size() > 2)
		{
			String first = lines.get(0);
			String[] cells0 = first.split("\t");
			String[] cells1 = lines.get(1).split("\t");
			String[] cells2 = lines.get(2).split("\t");
			String[] cells3 = lines.get(3).split("\t");

			nColumns = cells0.length;

			boolean anyNumbers0 = StringUtil.anyNumbers(cells0);
			boolean anyNumbers1 = StringUtil.anyNumbers(cells1);
			boolean anyNumbers2 = StringUtil.anyNumbers(cells2);
			boolean anyNumbers3 = StringUtil.anyNumbers(cells3);
			
			if (anyNumbers3)
			{
				if (anyNumbers2)
				{
					if (anyNumbers1)
						nHeaderLines = (anyNumbers0) ? 0 : 1;
					else nHeaderLines = 2;
				}
				else nHeaderLines = 3;
			}
			else nHeaderLines = 1;
		}
		return new Pair<Integer, Integer>(nColumns, nHeaderLines);
		
	}
	public void open(File f)		
	{ 	
		try
		{
			if (FileUtil.isCDT(f))
				controller.setGeneList(readTabularText(f, controller.getSpecies()));
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
