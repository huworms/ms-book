package com.hugo.api.composite.product;

public class ServiceAddresses {
	
	private final String cmp;
	private final String pro;
	private final String rev;
	private final String rec;
	
	public ServiceAddresses(String cmp, String pro, String rev, String rec) {
		super();
		this.cmp = cmp;
		this.pro = pro;
		this.rev = rev;
		this.rec = rec;
	}
	
	public ServiceAddresses() {
		this.cmp=null;
		this.pro=null;
		this.rev=null;
		this.rec=null;
	}
	
	public String getCmp() {
		return cmp;
	}

	public String getPro() {
		return pro;
	}

	public String getRev() {
		return rev;
	}

	public String getRec() {
		return rec;
	}

	

}
