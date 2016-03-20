package cis501.submission;

import cis501.ICache;

public class Cache implements ICache {

	public int hitLatency=0;
	public int cleanMissLatency=0;
	public int dirtyMissLatency=0;
	public int indexBits=0;
    public int ways;
    public int blockOffsetBits=0;
	public TagArray Cache[][];
	String LRUTag="";
	public String[] LRU;
	
	//public int indexOFEvictedBlock=0;
    public Cache(int indexBits, int ways, int blockOffsetBits,
        
        final int hitLatency, final int cleanMissLatency, final int dirtyMissLatency) {
    	
    	assert indexBits >= 0;
        assert ways > 0;
        assert blockOffsetBits >= 0;
        assert indexBits + blockOffsetBits < 64;
        assert hitLatency >= 0;
        assert cleanMissLatency >= 0;
        assert dirtyMissLatency >= 0;
        
        this.indexBits=indexBits;
        this.ways=ways;
        this.blockOffsetBits=blockOffsetBits;
        
        this.hitLatency=hitLatency;
        this.cleanMissLatency=cleanMissLatency;
        this.dirtyMissLatency=dirtyMissLatency;
        
        Cache=new TagArray[(int) Math.pow(2, indexBits)][ways];
		
        LRU=new String[(int) Math.pow(2, indexBits)];
        
		for(int i=0;i<(int) Math.pow(2, indexBits);i++) LRU[i]="";

        for(int index=0;index<(int) Math.pow(2, indexBits);index++)
           	for(int i=0;i<ways;i++)
			       //System.out.println(index);
          		Cache[index][i]=new TagArray();
	 }

    @Override
    public int access(boolean load, long address) {
    	
    	if (isHit(load, address))  
    		{
			//System.out.println(here);
    		 return hitLatency;
    		}
    	else {
    		if (isCleanMiss(load, address)) 
    			{
				//System.out.println(her1);
    			   return cleanMissLatency;
    			}
    		else 
    			{
				//System.out.println(here2);
    			    return dirtyMissLatency;
    			}
    	}
    }
    
    public boolean isCleanMiss(boolean load, long address) {
    	long tag = getTag(address);
    	
		TagArray EvictedBlock=getEvictBlock(address);
    	boolean wasEvictedBlockDirty=EvictedBlock.isDirty();
		
		EvictedBlock.setTag(tag);
		EvictedBlock.setValid(true);
		if(load) EvictedBlock.setDirty(false);
		else EvictedBlock.setDirty(true);
	
		return !wasEvictedBlockDirty;
    }
    
    
    public boolean isHit(boolean load, long address) {
    	long tag = getTag(address);
		int index = getIndex(address);
		
		for(int i=0;i<ways;i++) {
		
			TagArray cell=Cache[index][i];
		
		if(cell.getValid()) {
				if(cell.getTag()==tag) {
					
					if(!load) cell.setDirty(true);
					else cell.setDirty(false);
					
					updateLRU(index,i);
					return true;
				}
			}
		}
		return false;
    	
    }
    
    public int getLRU(int ind){
    	
    	String GLRU=LRU[ind];
    	try
    	{
    		return ((int)(GLRU.charAt(ways-1))-65);
    	}
    	catch (Exception Ex)
    	{
    		return 0;
    	}
    }
    
    public TagArray getEvictBlock(long address) {
    	int index = getIndex(address);
    	    for(int i=0;i<ways;i++){
    		TagArray Cell=Cache[index][i];
    		if(!Cell.getValid()) {	
    				updateLRU(index,i);
    				return Cell;
    			}
    	}
    	TagArray toReturn=Cache[index][getLRU(index)];
    	updateLRU(index,getLRU(index));
    	return toReturn;
    }

    
    public int getTag(long address){
    	return (int) address >>> (indexBits+blockOffsetBits);
    }

    public int getIndex(long address){
    	int indexMask = 0;
		indexMask = ((int) Math.pow(2, indexBits)-1) << blockOffsetBits;
		//System.out.println(indexMask);
    	return (int) ((address & indexMask) >>> blockOffsetBits);
    }



public void updateLRU(int ind, int way) {
	//indexOFEvictedBlock=way;
	String ULRU=LRU[ind];
	int id=ULRU.indexOf(way+65);
	//System.out.println(id);
	if(id!=-1)
		ULRU=String.valueOf((char)(65+way))+ULRU.substring(0, id)+ULRU.substring(id+1, ULRU.length());
	else
		ULRU=	String.valueOf((char)(65+way))+ULRU;
	
	if(ULRU.length()>ways) System.out.println("OOPS");
	LRU[ind]=ULRU;
}
}

class TagArray
{
	long tag;
	boolean isDirty;
	boolean valid;
		
	public TagArray(){
		tag=0;
		isDirty=false;
		valid=false;
	}
	
	public void setTag(long t){
	//System.out.println(tag);
		tag = t;
		//System.out.println(tag);
	}

	public long getTag(){
		return tag;
	}
	
	public void setValid(boolean val){
	
		valid = val;
	}

	public boolean getValid(){
	//System.out.println(valid);
		return valid;
	}
	
    public void setDirty(boolean newDirty){
	//System.out.println(newDirty);
		isDirty = newDirty;
	}

	public boolean isDirty(){
		return isDirty;
	}
	
	public boolean isClean(){
		return !isDirty;
	}
}