package com.relfor.pcs.payroll.exceptions;

public class ResourceNotFoundException extends RuntimeException {
	
	private static final long serialVersionUID = 8358783444607176166L;

	public ResourceNotFoundException(String msg) {
	    super(msg);
	  }

}