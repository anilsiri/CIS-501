package cis501.submission;

import cis501.IBranchTargetBuffer;

public class BranchTargetBuffer implements IBranchTargetBuffer {

	private long[] tag = new long[10000000];
	private long[] target = new long[10000000];
	private int index;
	private int length;
	private int indexFinal;
   	private int row;
	
    public BranchTargetBuffer(int indexBits) {
       
    	//to find the index of the table and the length of the predictor array
    	index = indexBits;
    	length = (int) Math.pow(2.0, indexBits);
    	
    }

    @Override
    public long predict(long pc) {
    	
    	long address = pc;
       	indexFinal = (int)(address % length);
       	long value = 0;
       	//long valueFinal = 0;
       	
       	//System.out.println("length" + length);
       	
       	for(int i=0;i < length;i++)
       	{
       		//System.out.println("tag[i]" + tag[i]);
       		
       		
       		if(pc==tag[i])
       		{
       			value = target[i];
       			
       			//System.out.println(value);
       			
       			return value;
       		}
       		
       	}
       	
       	//System.out.println(value);
       
       	return value;
    }

    @Override
    public void train(long pc, long actual) {
        
    	long address = pc;
       	indexFinal = (int)(address % length);
       	long finalAddress = actual;
       //	System.out.println("train pc      " + pc);
       	//System.out.println("train indexfinal     " + indexFinal);
       	tag[indexFinal] = pc;
       	target[indexFinal]=finalAddress;
       	//System.out.println("tag[indexFinal]" + tag[indexFinal]);
       	//System.out.println("target[indexFinal]" + target[indexFinal]);
       	
    }
}
