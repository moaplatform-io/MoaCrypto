package com.moaplanet.pg.memorial;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class MoaSimplePayMemoryWorker implements MoaMemorial {
	private static Map<String, MoaMemoryVo> hashmap = new ConcurrentHashMap<String, MoaMemoryVo>();
	public static Map<String, MoaMemoryVo> getAuthMem() {
		return hashmap;
	}
	
	@Override
	public void put(Object key, Object val) {
		hashmap.put((String)key, (MoaMemoryVo)val);
	}
	@Override
	public MoaMemoryVo get(Object key) {
		return (MoaMemoryVo) hashmap.get((String)key);
	}

	@Override
	public void remove(Object key) {
		hashmap.remove((String)key);
	}
	
}
