package cis501.submission;

import cis501.IDirectionPredictor;
import cis501.Direction;

public class DirPredTournament extends DirPredBimodal {

	private int index;
	private IDirectionPredictor NT;
	private IDirectionPredictor T;
	private Direction NTResult;
	private Direction TResult;
	private int length;
    private int[] predictor = new int[10000000];
	private long addressPC;
	private int indexFinal;
	private static int max = 3;
	
    public DirPredTournament(int chooserIndexBits, IDirectionPredictor predictorNT, IDirectionPredictor predictorT) {
        super(chooserIndexBits); // re-use DirPredBimodal as the chooser table
        
        index = chooserIndexBits;
        NT = predictorNT;
        T = predictorT;
        
    	length = (int) Math.pow(2.0, index);
    	
    	//to create the predictor table and initialize it to strongly notTaken
       //System.out.println(length);
        for (int i=0; i < length; i++)
        {
        	predictor[i] = 0;
        }       
    }

    @Override
    public Direction predict(long pc) {
    	
    	//to find the final index to the table
    	addressPC = pc;
    	//indexFinal = (int)(addressPC &(0b11));
    	indexFinal = (int)(addressPC %length);
    	Direction valueNT = null;
    	Direction valueT = null;
    	Direction value = null;
    	
    	valueNT = NT.predict(pc);
    	valueT = T.predict(pc);
    	
    	if (predictor[indexFinal] > 1)
    	{
    		value = valueT;
    	}
    	else 
    	{
    		value = valueNT;
    	}
        
        return value;
    }

    @Override
    public void train(long pc, Direction actual) {
    	
    	addressPC = pc;
    	indexFinal = (int)addressPC%length;
    	
    	Direction valueNT = null;
    	Direction valueT = null;
    	
    	valueNT = NT.predict(pc);
    	valueT = T.predict(pc);
    	
    	if(valueNT != valueT)
    	{
    		if(actual==Direction.Taken)
    		{
    			if(predictor[indexFinal] < max)
    			{
    			predictor[indexFinal]+=1;
    			System.out.println("chooser counter" + predictor[indexFinal] );
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
    	}
    
}


