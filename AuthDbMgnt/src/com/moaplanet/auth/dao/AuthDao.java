package com.moaplanet.auth.dao;

import com.moaplanet.auth.dto.AuthVo;

public interface AuthDao {


	public AuthVo getUserAuth(String member_id);
	public int updateUserAuth(AuthVo authVo);
	public int deleteUserAuth(AuthVo authVo);
	public int addUserAuth(AuthVo authVo);
}
