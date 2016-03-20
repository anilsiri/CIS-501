package cis501.submission;
import cis501.BranchPredictor;
import cis501.ICache;
import cis501.IMapTable;
import cis501.IOOOPipeline;
import cis501.Uop;
import cis501.BranchPredictor;
import cis501.IDirectionPredictor;
import cis501.IBranchTargetBuffer;
import cis501.Direction;
import cis501.Flags;
import cis501.IInorderPipeline;
import cis501.MemoryOp;
import cis501.Uop;
import cis501.ICache;
import cis501.IInorderPipeline;
import cis501.Uop;

import java.util.ArrayDeque;

import cis501.ArchReg;
import cis501.PhysReg;
import java.util.Iterator;

public class OOOPipeline<T extends Uop> implements IOOOPipeline<T> {
	int numOfInsns=1;
	int numOfCycles;	
	short src1;
	short src2;
	short dst;
	public ArchReg archreg1,archreg2,archreg3, archregdst1, archregdst2;
	public PhysReg physreg1,physreg2, physreg3, physregdst1, physregdst2;
	int pregs;
	int width;
	int robSize;
	int size;
	MapTable mt;
	robParam microOp;
	int i=1;
	public int fetchReady=0;
	BranchPredictor gshare;
	public long predictionAddress;
	public Direction branchDirection;
	public long actualAddress;
	public long currentCycle;
	public int[] scoreArray;
	private ICache instructionCache; 
	private ICache dataCache;
	public int dCacheLatency;
	public int iCacheLatency;
	int N=1; 
	int flag;
	long nextPredictionAddress=4336769;
	int setBit;
	
	ArrayDeque actualQueue = new ArrayDeque();
	
    @Override
    public String[] groupMembers() {
        return new String[]{"Siri Anil", "Sindhu"};
    }

    public OOOPipeline(int pregs, int width, int robSize, BranchPredictor bp, ICache ic, ICache dc) {
    this.pregs = pregs;
    this.width = width;
    this.robSize = robSize;
    mt= new MapTable(pregs);
     gshare=bp;
    instructionCache = ic;
    dataCache = dc;
    scoreArray = new int[pregs];
    }

    
    @Override
    public void run(Iterable<T> uiter) {	
    		while(((Iterator) uiter).hasNext()){
       			commit();
        		issue();
        		fetch(uiter);
        		advanceToNextCycle();		
        	}
    } 
    
    
   public void fetch(Iterable<T> uiter){		
	   for(i=0;i<width;i++)
   	{
   		if(fetchReady > 0 && flag==0)
   		{
   			fetchReady=fetchReady-1;
   			break;
   		}
   		
   		if(actualQueue.size() > robSize){
   			break;
   		}
   		
   		if(fetchReady ==-1 && flag==1)
   			{
   				Uop uop=(Uop) ((Iterator) uiter).next();
   	 			fetchAndRename(uop);
   	 			numOfInsns++;
   	 			flag=0;
   	 			break;
       }
   		if(fetchReady >0 && flag==1)
			{
   			Uop uop=(Uop) ((Iterator) uiter).next();
	 			fetchAndRename(uop);
	 			numOfInsns++;
	 			flag=0;
	 			fetchReady=fetchReady-1;
	 			break;
   }
   		
   		if(fetchReady == 0){
   			
   			Uop uop=(Uop) ((Iterator) uiter).next();
    			fetchAndRename(uop);
    			numOfInsns++;
    			if(uop.branch==Direction.Taken)
    			{
    				break;
    			}
   		}
   			
   	} 
   }
    
    
public void fetchAndRename(Uop uop)
 {	    	
	microOp = new robParam();
	microOp.sequenceNo=numOfInsns;
   	renaming(microOp,uop);  
   	
   	if(uop.uopId==1)
   	{
   		oldMisPred(uop,microOp);
   	}
   		branchPredictionMethod(uop, microOp);	
   		addtoQueue(microOp);
 }	


public void oldMisPred(Uop uop, robParam microOp)
   {
		long nextActualAddress=uop.instructionAddress;
		iCacheLatency=instructionCache.access(true, nextPredictionAddress);
		microOp.insnsCacheLatency=iCacheLatency;
		
		
	   	microOp.insnsCacheLatency=iCacheLatency;
			if(nextPredictionAddress!=nextActualAddress)
			{
				microOp.flagMisPred=true;
				
				
			}
			
			else
			{
				microOp.flagMisPred=false;
			}
			nextPredictionAddress = gshare.predict(uop.instructionAddress, uop.fallthroughPC);	
  }
	
//for the current instruction - change fetch ready and flag here - to perform branch stalls 
public void branchPredictionMethod(Uop uop, robParam microOp){
	
	if(uop.branch!=null)
	{
		//System.out.println(microOp.sequenceNo + " its a taken branch " + " current cycle " +currentCycle);
		
	//doing branch prediction from gshare
	predictionAddress = gshare.predict(uop.instructionAddress, uop.fallthroughPC);
	
	if(uop.branch==Direction.Taken){
	//	System.out.println(microOp.sequenceNo + " its a taken branch " + " current cycle " +currentCycle);
		actualAddress=uop.targetAddressTakenBranch;
	}
	else{
	//	System.out.println(microOp.sequenceNo + " its a taken branch " + " current cycle " +currentCycle);
		actualAddress=uop.fallthroughPC;
	}
	
		microOp.address=uop.instructionAddress;		
	   	
	if(predictionAddress!=actualAddress){
		microOp.isMisPredictedBranch=true;
		flag=1;
		fetchReady=-1;
	//	System.out.println(microOp.sequenceNo + " branch mis prediction detected " + " current cycle " +currentCycle);
		
				
}
	else		
	 {
	microOp.isMisPredictedBranch=false;
  }
	}
}




public void renaming(robParam microOp, Uop uop){
	src1 = uop.srcReg1;
	src2 = uop.srcReg2;
	dst = uop.dstReg;
	
	
	if(src1!=-1)
	{    	
    	archreg1 = mt.archArray[src1];
		physreg1 = mt.a2p(archreg1);
		microOp.source1=archreg1;
		
	}
	
    if (src1==-1)
    {
    	physreg1=null;
    	microOp.source1=null;
    }
    
    
	if(src2!=-1)
	{
		archreg2 = mt.archArray[src2];
		physreg2 = mt.a2p(archreg2);  
		microOp.source2=archreg2;
	}
	
	
	if (src2==-1)
    {
    	physreg2=null;
    	microOp.source2=null;
    	
    }
    
	 
	if(dst!=-1)
	{
		
		archregdst1 = mt.archArray[dst];
		microOp.toFree1=mt.a2p(archregdst1);
		physregdst1 = mt.allocateReg(archregdst1);
		microOp.dest1=archregdst1;
	}   

	
	if(dst==-1){
		physregdst1 =null;
		microOp.dest1=null;
	}
	
	
	if(uop.flags.equals(Flags.ReadFlags))
	{
		archreg3 = mt.archArray[49];
		physreg3 = mt.a2p(archreg3);	
		physregdst2=null;
		microOp.source3=archreg3;
		microOp.dest2=null;
	}
	
	
	if(uop.flags.equals(Flags.WriteFlags))
	{		
		archregdst2 = mt.archArray[49];	
		microOp.toFree2=mt.a2p(archregdst2);
		physregdst2 = mt.allocateReg(archregdst2);
		physreg3=null;
		microOp.source3=null;
		microOp.dest2=archregdst2;
	}
	
	if(uop.flags.equals(Flags.IgnoreFlags))
	{
		physregdst2=null;
		physreg3=null;
		microOp.source3=null;
		microOp.dest2=null;
	
	}
	microOp.srcPhy1 = physreg1;
	microOp.srcPhy2 = physreg2;
	microOp.dstPhy1 = physregdst1;
	microOp.srcPhy3 = physreg3;
	microOp.dstPhy2 = physregdst2;
	microOp.fetchCycle = currentCycle;
	microOp.done=false;
	microOp.issue=false;
	microOp.doneCycle=Long.MAX_VALUE;
	microOp.issueCycle=Long.MAX_VALUE;
	microOp.address=uop.instructionAddress;
	microOp.nextAdress=uop.fallthroughPC;
	microOp.target=uop.targetAddressTakenBranch;
	microOp.branch=uop.branch;
	microOp.micro=uop.microOperation;
	microOp.macro=uop.macroOperation;
	microOp.myMemory=uop.mem;
	microOp.fallThrough=uop.fallthroughPC;
	microOp.DataAddress=uop.dataAddress;
	//System.out.println("During fetch " + microOp.sequenceNo + "  "+ microOp.DataAddress);
}
		



public void addtoQueue(robParam microOp)
{
	actualQueue.add(microOp);
	int q, r;
	if(microOp.dstPhy1!=null){
	q=microOp.dstPhy1.get();
	sbFetch(q);}
	
	if(microOp.dstPhy2!=null){
		r=microOp.dstPhy2.get();
		sbFetch(r);
		}   	
}		

public void sbFetch(int s)
{
	scoreArray[s]=-1;
}

    
public void issue(){
	int count=0;
	robParam temp; 
	

	
	for (Iterator itr = actualQueue.iterator();itr.hasNext();)
	{
    	temp = (robParam)itr.next();
    	
    	int a=0, b=0, c=0;
    	if(temp.srcPhy1!=null)
		a=temp.srcPhy1.get();
		if(temp.srcPhy1==null)
		a=-1;
		
		if(temp.srcPhy2!=null)
			b=temp.srcPhy2.get();
			if(temp.srcPhy2==null)
			b=-1;
			
			if(temp.srcPhy3!=null)
			c=temp.srcPhy3.get();
			if(temp.srcPhy3==null)
			c=-1;  
			
			boolean ready;
			
			ready=isReady(a,b,c);
			
			
			
			//TODO: finish this part 
			if(ready==true && temp.issue==false)
			{		
				boolean noMemDep;
				noMemDep=loadStoreDependance(temp);
				
				if(noMemDep == true){
					canBeIssued(temp);
					count=count+1;

				}
			}
			//System.out.println("width is " +width);
	if(count==width)
	{
		break;
	}
	}
	
	}

public boolean loadStoreDependance(robParam temp){
	robParam temp1;
	if(temp.myMemory==MemoryOp.Load)
	{
	//	System.out.println(temp.sequenceNo +"first");
		for (Iterator itr1 = actualQueue.iterator();itr1.hasNext();){
			temp1 = (robParam)itr1.next();
			
			if(temp1.myMemory==MemoryOp.Store && (temp1.sequenceNo < temp.sequenceNo) && (temp1.doneCycle >= currentCycle)){
		//		System.out.println(temp.sequenceNo +"  entered here");
				if(temp1.DataAddress == temp.DataAddress)
				{
			//		System.out.println("entered next");
					return false;
				}
		}
	}
	}
	return true;
}

public boolean isReady(int m, int n, int o)
{
	int ready1, ready2, ready3;
	ready1=isReadyinside(m);
	ready2=isReadyinside(n);
	ready3=isReadyinside(o);
	
	if(ready1!=1 || ready2!=1 || ready3!=1)
		return false;
	else 
		return true;
	
}

public int isReadyinside(int m){
	int isReady;
	if(m==-1)
		isReady=1;
	else 
		{
		if(scoreArray[m]==0)
		isReady=1;
		else 
		isReady=0;
		}
		return isReady;
}



public void canBeIssued(robParam temp) {
				
				int g;
				int h;
				int latency;
			
				temp.issueCycle=currentCycle;
			
				if(temp.myMemory==MemoryOp.Store)
				{
					temp.dCacheLatency=dataCache.access(false, temp.DataAddress);
				}
				
				if(temp.myMemory==MemoryOp.Load)
				{
					temp.dCacheLatency=dataCache.access(true, temp.DataAddress); 	
				}
				
				if(temp.myMemory==null){
					temp.dCacheLatency=0;
				}
				
				
				trainPredictor(temp);				
				if(temp.dCacheLatency!=0){
					temp.dcachemiss="d$miss";
				}
				if(temp.insnsCacheLatency!=0){
					temp.icachemiss="i$miss";
				}
				
				
				temp.doneCycle=currentCycle+temp.insnsCacheLatency+temp.dCacheLatency+1;
				latency=1+temp.insnsCacheLatency+temp.dCacheLatency;
						
				temp.issue=true;

				if(temp.isMisPredictedBranch==true)		
				{
					
					branchMispredicted(latency);
				//	System.out.println("branch mis prediction detected in issue: insns no:  " +temp.sequenceNo +"current cycle "  +currentCycle + "latency " +latency);
					//System.out.println("branch mis prediction detected in issue: insns no:  " +temp.sequenceNo +"done cycle  "  +temp.doneCycle + "latency " +latency);
					//System.out.println("branch mis prediction detected in issue  " +temp.sequenceNo + "fetchReady " +fetchReady);
					
				}		
			
				
					if(temp.dstPhy1!=null){
					g=temp.dstPhy1.get();
					sbIssue(g,latency);}
					
					if(temp.dstPhy2!=null){
					h=temp.dstPhy2.get();
					
					sbIssue(h,latency);
				}
}


public void trainPredictor(robParam var){
	if(var.branch!=null){
		gshare.train(var.address, var.nextAdress, Direction.NotTaken);
	}
	if(var.branch==Direction.Taken){
		gshare.train(var.address, var.fallThrough, Direction.Taken);
	}
	if(var.branch==Direction.NotTaken){
		gshare.train(var.address, var.target, Direction.NotTaken);
	}
	
}
		
public void sbIssue(int t, int k)
{
	scoreArray[t]=k;
}



public void branchMispredicted(int lat){
	fetchReady=lat+3;
}

    

public void commit(){
	robParam a;	
	
		for(i=0;i<width;i++){
	
			if(actualQueue.size()!=0)
			{
	a=(robParam) actualQueue.getFirst();
	
	
	long m;
	m=a.doneCycle;

		if(m<=currentCycle){
			if(a.toFree1!=null){
			mt.freeReg(a.toFree1);
		}
		if(a.toFree2!=null){
			mt.freeReg(a.toFree2);
		}
			
		actualQueue.removeFirst();
		a.commitCycle=currentCycle;
		
//printMethod(a);
		}
		}
	}
}
    

public void printMethod(robParam a){
	System.out.print(+a.sequenceNo+ ": " +a.fetchCycle + " " + a.issueCycle + " "+ a.doneCycle + " " +a.commitCycle + ", ");
	if(a.source1!=null)
	{
		System.out.print(a.source1.toString() + " -> " + a.srcPhy1.toString() + ", ");
	}
	if(a.source2!=null){
		System.out.print(a.source2.toString() + " -> " + a.srcPhy2.toString() + ", ");
	}
	if(a.source3!=null){
		System.out.print(a.source3.toString() + " -> " + a.srcPhy3.toString() + ", ");
	}
	if(a.dest1!=null){
		System.out.print(a.dest1.toString() + " -> " + a.dstPhy1.toString() + " ");
		System.out.print("["+a.toFree1 + "]");
	}
	
	
	if(a.dest2!=null){
		System.out.print(a.dest2.toString() + " -> " + a.dstPhy2.toString()+ " ");
		System.out.print(" ["+a.toFree2 + "] ");
	}
	
	if(a.icachemiss==null && a.dcachemiss==null && a.isMisPredictedBranch==false){
	System.out.println(" | " +a.macro + " "+ a.micro);
	}
	else
	{
		System.out.print(" | " +a.macro + " "+ a.micro);
	}
	
		if(a.icachemiss!=null){
			
	System.out.println(" " +a.icachemiss);
	}
		
		if(a.dcachemiss!=null){
			
		System.out.println(" "+a.dcachemiss);
		}	
		
		if(a.isMisPredictedBranch==true){
			System.out.println(" "+ "bmispred");
		}
}


    public void advanceToNextCycle()
	{
    	sbNext();
    	currentCycle=currentCycle+1;	 		
	}
 
    
    public void sbNext()
	{
    	
		for(int i=0;i<pregs;i++)
		{
			if(scoreArray[i]>0)
			scoreArray[i]=scoreArray[i]-1;
		}
	}
   
    
    @Override
    public long getInsns() {
        return numOfInsns;
    }

    @Override
    public long getCycles() {
        return currentCycle;
    }
}