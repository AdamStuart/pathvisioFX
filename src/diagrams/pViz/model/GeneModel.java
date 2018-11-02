package diagrams.pViz.model;

import java.io.File;
import java.util.List;

import diagrams.pViz.app.Controller;
import javafx.collections.FXCollections;
import javafx.scene.control.TableColumn;
import model.bio.Gene;
import model.bio.GeneSetRecord;
import model.bio.Species;
import services.bridgedb.BridgeDbIdMapper;
import services.bridgedb.MappingSource;
import util.FileUtil;
import util.StringUtil;

public class GeneModel {
	// **-------------------------------------------------------------------------------
	List<Gene> genes = FXCollections.observableArrayList();
	GeneSetRecord geneListRecord = null;
	Controller controller = null;

	public GeneModel(Controller ct)
	{
		controller = ct;
	}
	public GeneSetRecord getGeneList() 
	{	
		if (geneListRecord == null)
		{
			geneListRecord = new GeneSetRecord("GeneList");
			geneListRecord.setGeneSet(genes);
			List<TableColumn<Gene, ?>>	allColumns = geneListRecord.getAllColumns();

//			List<TableColumn<Gene, ?>> parentColumns = parent.getAllColumns();
		}
		return	geneListRecord ;
	}
	public void setGeneList(GeneSetRecord rec, List<Gene> gs) {		genes = gs;	geneListRecord = rec; }
	public void addGeneList(GeneSetRecord rec) 				{		genes.addAll(rec.getGeneSet()); 	}
	
	public int getNGenes() 				{	return	genes.size();	}
	public void addGene(Gene g) 		{	genes.add(g);	}
	public void clearGenes() 			{	genes.clear();	}
	public List<Gene> getGenes() 		{	return genes;	}
//	public List<Gene> getGeneList() 		{		return new GeneList(genes, getSpecies());	}
	public Gene findGene(String string) {
		if (StringUtil.isEmpty(string)) return null;
		for (Gene g : getGenes())
			if (string.equals(g.getName()))
				return g;
		return null;
	}
	public Gene findGene(Gene other) {
		if (other == null) return null;
		return findGene(other.getName());
	}
	public boolean add(Gene g)
	{
		if ( find(g.getName()) == null) 
			return genes.add(g);
		return false;
	}
	
	public boolean add(GeneSetRecord geneRec)
	{
		boolean anyTrue = false;
		for (Gene gene : geneRec.getGeneSet())
			anyTrue |= add(gene);
		return anyTrue;
	}
	
	public List<Gene> intersection(List<Gene> other)
	{
		List<Gene> intersection = FXCollections.observableArrayList();
		for (Gene g : genes)
			if (findInList(other, g.getName()) == null)
				intersection.add(g);
		return intersection;
	}
	private Species species;

	static String TAB = "\t";
	static String NL = "\n";
	public static String BDB = "http://webservice.bridgedb.org/";
	public void fillIdlist()
	{
		if (species == null) 
			species = Species.Unspecified;
		StringBuilder str = new StringBuilder();
		for (Gene g : genes)
		{
			if (StringUtil.hasText(g.getIdlist())) continue;
			String name = g.getName();
			MappingSource sys = MappingSource.guessSource(species, name);
			str.append(name + TAB + sys.system() + NL);
		}
		try
		{
			List<String> output = BridgeDbIdMapper.post(BDB, species.common(), "xrefsBatch", "", str.toString());
			for (String line : output)
			{
				String [] flds = line.split("\t");
				String name = flds[0];
				String allrefs = flds[2];
				for (Gene g : genes)
				{
					if (!g.getName().equals(name)) continue;
//					System.out.println("setting ids for " + name );	
					g.setIdlist(allrefs);
					g.setEnsembl(BridgeDbIdMapper.getEnsembl(allrefs));
				}
			}
		}
		catch(Exception ex) 
		{ 
			System.err.println(ex.getMessage());	
		}
	}
	
	public List<Gene> union(List<Gene> other)
	{
		List<Gene> union = FXCollections.observableArrayList();
		union.addAll(genes);
		for (Gene g : other)
			if (findInList(genes, g.getName()) == null)
				union.add(g);
		return union;
	}
	public Gene find(Gene g)	{		return find(g.getName());	}

	public void addGenes(List<Gene> inList) {
		for (Gene g: inList)
			if (null == findInList(genes, g.getName()))
				genes.add(g);
	}
	static public Gene findInList(List<Gene> list, String nameOrId)
	{
		if (nameOrId == null) return null;
		String name = nameOrId.trim();
		for (Gene g : list)
		{
			if (name.equals(g.getName())) return g;			//IgnoreCase
//			if (name.equalsIgnoreCase(g.getId())) return g;
		}
		return null;
	}
	
	
	public Gene find(String nameOrId)
	{
		if (nameOrId == null) return null;
		String name = nameOrId.trim();
		for (Gene g : genes)
		{
			if (name.equalsIgnoreCase(g.getName())) return g;
			if (name.equalsIgnoreCase(g.getGraphId())) return g;
		}
		return null;
	}
	static public boolean isGeneSet(File f)
	{
		List<String> strs = FileUtil.readFileIntoStringList(f.getAbsolutePath());
		int sz = strs.size();
		if (sz < 10) return false;
		String DELIM = "\t";
		String firstRow = strs.get(0);
		int nCols = firstRow.split(DELIM).length;
		for (int i = 1; i< sz; i++)
		{
			String row = strs.get(i);
			if (row.split(DELIM).length != nCols)
				return false;
		}
		return true;
	}


}
