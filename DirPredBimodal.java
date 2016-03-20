package cis501.submission;

import cis501.Direction;
import cis501.IDirectionPredictor;

public class DirPredBimodal implements IDirectionPredictor {
	
    private int lengthPredictor;
	private int index;
	private int indexFinal;
	private static int max = 3;
	private long addressPC;
	private int length;
    private int[] predictor = new int[10000000];        
	
    public DirPredBimodal(int indexBits) {
    	
    	//to find the index of the table and the length of the predictor array
    	index = indexBits;
    	length = (int) Math.pow(2.0, indexBits);
    	
    	//to create the predictor table and initialize it to strongly notTaken
       // System.out.println(length);
        for (int i=0; i < length; i++)
        {
        	predictor[i] = 0;
        }
    }

    @Override
    public Direction predict(long pc) {
    	
    	//to find the final index to the table
    	addressPC = pc;
    	indexFinal = (int)addressPC % length;
    	
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
    	
    	indexFinal = (int)addressPC%length;
    	
    	if(actual== Direction.Taken)
    	{
    		if(predictor[indexFinal] < max)
    		{
    			predictor[indexFinal]+=1;
    		}
    	}
    	else
    	{
    		if(predictor[indexFinal] > 0)
    		{
    			predictor[indexFinal]-=1;
    		}
    	}
    }

} // class braces
