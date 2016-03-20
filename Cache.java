package cis501.submission;

import cis501.ICache;
import java.util.Arrays;

public class Cache implements ICache {

   private static final boolean TRUE = false; 
   private static final boolean FALSE = false;
   int indexBits;
   int ways;
   int blockOffsetBits;
   int hitLatency;
   int cleanMissLatency;
   int dirtyMissLatency;
   int setIndex;
	
   int latency = 0;
  
   int offset;
   int sets;
   int tag;
   int size; 
   //int tagArraylength = (int)Math.pow(2.0,indexBits);
   int[] tagArray = new int[262144];
   int[] validArray = new int[262144];
   int[] dirtyArray = new int[262144];
   int[] lruArray = new int[262144];
   int x;
   /*public Integer tagArray[];

   public int validArray[];
   public int dirtyArray[];
   public int lruArray[];*/
   boolean miss;
   int hit = 0;
   int setBitSize;
   int ref;
   int change;
   int noOfSets;
   
    public Cache(int indexBits, int ways, int
    		blockOffsetBits,
                 final int hitLatency, final int cleanMissLatency, final int dirtyMissLatency) {
        assert indexBits >= 0;
        assert ways > 0;
        assert blockOffsetBits >= 0;
        assert indexBits + blockOffsetBits < 64;
        assert hitLatency >= 0;
        assert cleanMissLatency >= 0;
        assert dirtyMissLatency >= 0;
        this.indexBits = indexBits;
        this.ways = ways;
        this.blockOffsetBits = blockOffsetBits;
        this.hitLatency = hitLatency;
        this.cleanMissLatency = cleanMissLatency;
        this.dirtyMissLatency = dirtyMissLatency;
        
    }
   
    //procedure to get offset bits
    public int getOffsetBits(long pc, int offset){
    	//int offsetBits1;
    	int offsetBits;
    	int length = (int)Math.pow(2.0,offset);
    	//int length = (64 - offset);
    	offsetBits = (int)(pc % length);
    	//offsetBits1 = pc << length;
    	//offsetBits = (int) ((offsetBits1) & (0XFFFFFFFFFFFFFFFF));
    	return offsetBits;
    }
    
    //procedure to get set bits
    public int getSets(long pc,int offset, int sizewayBits){
    	int indexBits;
    	indexBits = (int) pc >> offset;
    	int length = (int)Math.pow(2.0,sizewayBits);
    	indexBits = (int)(indexBits % length);    	
    	return indexBits;    	
    }
    
    public int getSetsNegative(long pc,int offset, int sizewayBits){
    	try{
    	int indexBits;
    	indexBits = (int) pc >> offset;
    	int length = (int)Math.pow(2.0,sizewayBits);
    	indexBits = (int)(indexBits % length);  
    	}
    	catch (ArithmeticException ae) {
            System.out.println("ArithmeticException occured!");
        }
    	return indexBits;    	
    }
    
    //procedure to get tag bits
    public int gettags(long pc, int size){
    	int tagBits;
    	tagBits = (int)pc >> size;
    	return tagBits;
    }
    
    //procedure to get number of horizontal lines in the cache
    public int getlines(int index){
    	int length = (int)Math.pow(2.0,index);
    	return length;
    }
    
    
    //procedure to get number of vertical lines in the cache
    public int getoffset(int offset){
    	int length = (int)Math.pow(2.0, offset);
    	return length;
    }
        
    
    //procedure to increment the LRU on hit
    public void getLRUhit(int startPoint, int endPoint, int position){    	
       	for(int i=startPoint;i<endPoint;i++)
       	{
       		if(i!=position)
       		{
       			lruArray[i]=lruArray[i]+1;
       		}
       		else
       		{
       			lruArray[i]=0;
       		}
       	}
    }        
    
    //procedure for get the LRU index for LRU miss
    public int getLRUmiss(int startpoint, int endpoint){
    	int largest = lruArray[startpoint];  
    	int pos = startpoint;
    	for(int i=startpoint;i<endpoint;i++)
    	{
    		if (lruArray[i] > largest)
    		{
    			largest =lruArray[i];
    			pos = i;
    		}
    	}
		if(dirtyArray[pos]==1)
		{
			hit=2;
		}
    	return pos;
    }
    
    @Override
    public int access(boolean load, long address) {

    	long add = address;
    	long y;
    	if(address < 0)
    	{
    		y = ~add;
    		y = y+1;
    		add = y;
    	}
    	else
    	{
    		add = address;
    	}
        //to find the number of sets in cache
    	setBitSize = (int)(Math.log10(ways)/Math.log10(2.0));
        //System.out.println("noOfSets   "+noOfSets);
        
        x = indexBits - setBitSize;
        //System.out.println("x   "+x);
        
        if(address < 0)
        {
        setIndex = getSetsNegative(add,blockOffsetBits,x);  
        }
        else
        {
        setIndex = getSets(add,blockOffsetBits,x);      
        }
        //System.out.println("setIndex  "+setIndex);
        
        size = blockOffsetBits + x;
        tag = gettags(add,size);
        //System.out.println("tag   "+tag);
        
        int position = setIndex*ways;
        //System.out.println("position   "+position);
        
        for(int i=0; i<ways; i++)
        {
        	if(tag == tagArray[position+i])
        	{
        		if(validArray[position+i]==1)
        		{
        			hit=1;
        			ref = position+i;
        			getLRUhit(position,position+ways,ref);
        		}
        	}
        }   
    if(hit==0)
    {
    	change = getLRUmiss(position,position+ways);
    	if(dirtyArray[change]==1)
    	{
    		hit=2;
    	}
    	else
    	{
    		hit=3;
    	}
    	tagArray[change]= tag;
    	validArray[change] = 1; 
        if(load==FALSE)
        {
        	dirtyArray[change] = 1;
        }
    }
        
    if(hit==1)
    {
    	latency = hitLatency;
    	hit=0;
    }
    else if(hit==2)
    {
    	latency = dirtyMissLatency;
    	hit=0;
    }
    else if(hit==3)
    {
    	latency = cleanMissLatency;
    	hit=0;
    }
    
   
    //System.out.println("latency   "+latency);
    //System.out.println("  ");
    return latency;
     
    }//method braces
}//class braces