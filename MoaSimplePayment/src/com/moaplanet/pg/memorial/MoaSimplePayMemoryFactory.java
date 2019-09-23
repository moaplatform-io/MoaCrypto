package com.moaplanet.pg.memorial;

public class MoaSimplePayMemoryFactory {


	public static MoaMemorial getMemoryWorker() {
		return new MoaSimplePayMemoryWorker();		
	}
}
