package ufrn.imd;

import java.util.function.Function;

public class Range implements Function<Double, Double>
{
	@Override
	public Double apply(Double t) 
	{
		if(t > 10) 
		{
			t = 10.0;
		}
		else if (t < 0)
		{
			t = 0.0;
		}
		
		return t;
	}

}
