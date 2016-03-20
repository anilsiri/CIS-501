package cis501.submission;

import cis501.ArchReg;
import cis501.Direction;
import cis501.MemoryOp;
import cis501.PhysReg;
import cis501.Uop;

public class robParam {
	public PhysReg srcPhy1;
	public PhysReg srcPhy2;
	public PhysReg srcPhy3;
	public PhysReg dstPhy1;
	public PhysReg dstPhy2;	
	
	public PhysReg toFree1;
	public PhysReg toFree2;
	
	public long address;
	public long DataAddress;
	
	public ArchReg source1;
	public ArchReg source2;
	public ArchReg source3;
	public ArchReg dest1;
	public ArchReg dest2;
	
	public String macro;
	public String micro;
	
	public long fetchCycle;
	public long issueCycle;
	public long doneCycle;
	public long commitCycle;
	
	public boolean done=false;
	public boolean issue=false;
	public boolean isMisPredictedBranch=false;
	public boolean flagMisPred;
	
	public long target;
	public long nextAdress;
	public long fallThrough;
	
	public Direction branch;
	public MemoryOp myMemory;
	
	public int sequenceNo;
	public int insnsCacheLatency;
	public int dCacheLatency;
	
	public String icachemiss;
	public String dcachemiss;
	
	
}