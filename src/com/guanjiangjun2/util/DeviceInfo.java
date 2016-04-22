package com.guanjiangjun2.util;

public class DeviceInfo {
	private String nickname;
	private String imei;
	
	public String GetNickName(){
		return nickname;
	}
	
	public String GetImei(){
		return imei;
	}
	
	public void SetNickName(String nickname){
		this.nickname=nickname;
	}
	
	public void SetImei(String imei){
		this.imei=imei;
	}
}
