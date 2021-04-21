package ufrn.imd.serverless;

import java.util.function.Function;

public class VerifyRange implements Function<Double, Double>
{

	@Override
	public Double apply(Double t) 
	{
		System.out.println("Trabalho de corno");
		if(t > 10) 
		{
			t = 10.0;
		}
		else if(t < 0) 
		{
			t = 0.0;
		}
		
		return t;
	}
}
