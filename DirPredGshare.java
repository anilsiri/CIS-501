package cis501.submission;

import cis501.Direction;

public class DirPredGshare extends DirPredBimodal {

	//private int index;
	private int indexFinal;
	private static int max = 3;
	private long addressPC;
	private int length;
    private int[] predictor = new int[10000000]; 
    private long history;
    private int bitsHistory;
    private int LHistory;
	private long history_mask;
	
    public DirPredGshare(int indexBits, int historyBits) {
        super(indexBits);
        
    	//to find the index of the table and the length of the predictor array
    	//index = indexBits;    	
        length = (int) Math.pow(2.0, indexBits);
        LHistory = (int) Math.pow(2.0, historyBits);
        bitsHistory = historyBits;
    	
    	//to create the predictor table and initialize it to strongly notTaken
       //System.out.println(length);
        for (int i=0; i < length; i++)
        {
        	predictor[i] = 0;
        }        
        for (int i=0;i<bitsHistory;i++)
        {
        	history_mask = history_mask << 1;
        	history_mask = history_mask + 1;
        }
    }

    @Override
    public Direction predict(long pc) {
    	
    	
    	addressPC = pc;
    	indexFinal = (int)((addressPC^history)% length);

    	Direction value = null;
    	
    	//to predict the direction
    	if (predictor[indexFinal] > 1)
    	{
    		value = Direction.Taken;
    	}
    	else 
    	{
    		value = Direction.NotTaken;
    	}

        return value;
    }

    @Override
    public void train(long pc, Direction actual) {
    	
    	addressPC = pc;
    	//Direction value1 = actual;
    	indexFinal = (int)((addressPC ^ history)%LHistory);
    	
        if(actual== Direction.Taken)
    	{
    		if(predictor[indexFinal] < max)
    		{
    			predictor[indexFinal]= predictor[indexFinal] + 1;
    		}
    	}
    	else
    	{
    		if(predictor[indexFinal] > 0)
    		{
    			predictor[indexFinal]-=1;
    		}
    	}
        
        history = history << 1;
        if(actual==Direction.Taken)
        {
        	history = history + 1;
        }
        else
        {
        	history = history + 0;
        }
        history = history & history_mask;
    }
    
}//class braces
