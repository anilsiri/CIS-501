package cis501.submission;


import cis501.BranchPredictor;
import cis501.IDirectionPredictor;
import cis501.IBranchTargetBuffer;
import cis501.Direction;
import cis501.IInorderPipeline;
import cis501.MemoryOp;
import cis501.Uop;
import cis501.ICache;
import cis501.IInorderPipeline;
import cis501.Uop;

enum Stage {
    FETCH(0), DECODE(1), EXECUTE(2), MEMORY(3), WRITEBACK(4);

    private final int index;

    private Stage(int idx) {
        this.index = idx;
    }

    /** Returns the index of this stage within the pipeline */
    public int i() {
        return index;
    }
}

public class InorderPipeline<T extends Uop> implements IInorderPipeline<T> {
	//Assignment 4
	public long branchAddress;
	public int gHistory;
	private long startAddress;
	private long destAddress;
	private IBranchTargetBuffer buffer;
	private BranchPredictor branchpredictor;
	private long predictedValue;
	private long obtainedArray;
	private ICache instructionCache;
	private ICache dataCache;
	private BranchPredictor bpCache;
	private int iCacheLatency;
	private int dCacheLatency;
	
	public Uop[] uopArray=new Uop[5];
	int MemLatency=0;
	long numOfInsns=0;
	long numOfCycles=0;
	
    @Override
    public String[] groupMembers() {
        return new String[]{"Siri", "Sindhu"};
    }

    /**
     * Create a new pipeline with the given additional memory latency.
     * @param additionalMemLatency The number of extra cycles mem uops require in the M stage. If 0,
     *                             mem uops require just 1 cycle in the M stage, like all other
     *                             uops. If x, mem uops require 1+x cycles in the M stage.
     */
    public InorderPipeline(int additionalMemLatency) {
    	this.MemLatency=additionalMemLatency;
    }

    /** ctor for HW4: Branch Prediction */
    public InorderPipeline(int additionalMemLatency, BranchPredictor bp) {
    	MemLatency = additionalMemLatency;
    	branchpredictor = bp;
    }

    /** ctor for HW5: Caches */
    public InorderPipeline(BranchPredictor bp, ICache ic, ICache dc) {
    	branchpredictor = bp;
    	instructionCache = ic;
    	dataCache = dc;
    }

    @Override
    public void run(Iterable<T> ui) {
    	
    	for(Uop uop : ui)
    	{
    		if(uop.uopId==1)
    		{
    			numOfInsns++;
    		}    		
    		iCacheLatency = instructionCache.access(true,uop.instructionAddress);
    		numOfCycles = numOfCycles + iCacheLatency;
    		pipeline(uop);
    		putIcache();
    		callPredictor();
    		LoadDependency();
    		MemoryLatency();
    		getObtainedValue();	
    		trainPredictor();
    		
    	}
    	
    	while(!isPipeEmpty())
    	{
    		LoadDependency();
    		MemoryLatency();
    		getObtainedValue();
    		trainPredictor();
    		ShiftPipeLine(0);
    		
    	}
		
    	}
    
    
    public void LoadDependency()
    {
    	Uop atDecode=uopArray[1];
    	Uop atExecute=uopArray[2];
    	if(atDecode!=null&&atExecute!=null)
    	{
    	if(atDecode.srcReg1==atExecute.dstReg||atDecode.srcReg2==atExecute.dstReg)
		{
			ShiftPipeLine(2);
		}
    	}
    }

    public void MemoryLatency()
    {
    	Uop atMemory=uopArray[3];
    	if(atMemory!=null)
    	{
    		
    		if(atMemory.mem==MemoryOp.Store)
    		{
    		dCacheLatency = dataCache.access(false, atMemory.instructionAddress);
  	        numOfCycles = numOfCycles + dCacheLatency;
    		}
    		else if(atMemory.mem==MemoryOp.Load)
    		{
        		dCacheLatency = dataCache.access(true, atMemory.instructionAddress);
      	        numOfCycles = numOfCycles + dCacheLatency;	
    		}
    	  //System.out.println(atMemory.instructionAddress);
    	if(atMemory.mem==MemoryOp.Load||atMemory.mem==MemoryOp.Store)
    	{    		
    		numOfCycles=numOfCycles+MemLatency;	 
    	}
    	}
    }
    
    
    public boolean isPipeEmpty()
    {
    	if(uopArray[0]==null&&uopArray[1]==null&&uopArray[2]==null&&uopArray[3]==null&&uopArray[4]!=null)
    		return true;
    	return false;
    }
    
    public void pipeline(Uop uop)
    {
    	if(uopArray[0]==null)
    	{
    	  numOfCycles++;
    	  uopArray[0]=uop;
    	  return;
    	}
    	if(uopArray[0]!=null)
    	{
    		ShiftPipeLine(0);
    		uopArray[0]=uop;
    	}
    	
    	
    }
    
    public void ShiftPipeLine(int code)
    {
    	
    	for(int i=4;i>code;i--)
    		uopArray[i]=uopArray[i-1];
    	
    	uopArray[code]=null;
    	numOfCycles++;
    	    	
    }
    
    
   public void callPredictor()
    {
		predictedValue = branchpredictor.predict(uopArray[0].instructionAddress, uopArray[0].fallthroughPC);		
    }
   
   public void getObtainedValue()
   {
		if(uopArray[3]!=null)
		{
			//System.out.println("j   "+j);
			if(uopArray[3].branch==Direction.Taken)
			{
				obtainedArray=uopArray[3].targetAddressTakenBranch;
				if(obtainedArray !=predictedValue)
				{
					numOfCycles = numOfCycles + 2;
				}
		    }
			else
			{
				obtainedArray=uopArray[3].fallthroughPC;
			}
			
		}	
    }
    
    
    public void trainPredictor()
    {
    	if(uopArray[3]!=null)
    	{
    	if(uopArray[3].branch == Direction.Taken)
    	{
    		branchpredictor.train(uopArray[3].instructionAddress, uopArray[3].targetAddressTakenBranch, uopArray[3].branch);	
    	}
    	else 
    	{
    		branchpredictor.train(uopArray[3].instructionAddress, uopArray[3].fallthroughPC, uopArray[3].branch);	
    	}
    	}
    }
    
    public void putIcache(){
    	
    }
    
    @Override
    public long getInsns() {
    	//System.out.println(numOfInsns);
        return numOfInsns;
    }

    @Override
    public long getCycles() {
        return numOfCycles+1;
    }
}
