package cis501.submission;
import java.util.ArrayDeque;

import cis501.ArchReg;
import cis501.IMapTable;
import cis501.PhysReg;
import java.util.ArrayDeque;

public class MapTable implements IMapTable {

	public int archRegSize = 50;
	public ArchReg[] archArray = new ArchReg[50];
	public PhysReg[] phyArray = new PhysReg[50];
	public ArrayDeque queue = new ArrayDeque();
	public int queueSize;
	public int pregs;
	
    public MapTable(int pregs) {
    	this.pregs = pregs;
    	
	for(int i=0;i<50;i++)
	{
		archArray[i]= new ArchReg(i);
		//System.out.print(archArray[i].get());
		phyArray[i]= new PhysReg(i);
	}
	
	for(int j=archRegSize ; j<pregs ; j++)
	{
		//System.out.println("here1");
		queue.add(j);
	}
	queueSize = queue.size();
    //System.out.println("size" + queue.size());
    }
    
    @Override
    public int availablePhysRegs() {   
    	queueSize = queue.size();  
    	//System.out.println("method" + queueSize);
    	return queueSize;
    }


    @Override
    public PhysReg allocateReg(ArchReg ar) { 
    	availablePhysRegs();
    	//System.out.println(queueSize);
    	PhysReg k; 
    	if(queueSize==0)
    	{
    		//System.out.println("here");
    		return null;    		
    	}
    	else
    	{
    	int i = ar.get();
    	//System.out.println(i);
    	int firstElement = (int)queue.remove(); 	    	
    	phyArray[i] = new PhysReg(firstElement);
    	k = phyArray[i];        
    	return k;
        }
        
    }
    
    @Override
    public void freeReg(PhysReg pr) {    
    	queue.add(pr.get());    	
    }

    @Override
    public PhysReg a2p(ArchReg ar) {    	
    	int i = ar.get();
    	PhysReg j = phyArray[i];    	   	
        return j;
    }
    
    }