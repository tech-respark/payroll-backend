package com.relfor.pcs.payroll.exceptions;

import java.time.Instant;

public class ApiExceptionResponse {
	
	private String timeStamp;
	private Integer status;
	private String error;
	private String message;
	
	public ApiExceptionResponse() {
		timeStamp = Instant.now().toString();
	}
	
	public ApiExceptionResponse(Integer status, String error, String message) {
		super();
		timeStamp = Instant.now().toString();
		this.status = status;
		this.error = error;
		this.message = message;
	}
	public String getTimeStamp() {
		return timeStamp;
	}
	public void setTimeStamp(String timeStamp) {
		this.timeStamp = timeStamp;
	}
	public Integer getStatus() {
		return status;
	}
	public void setStatus(Integer status) {
		this.status = status;
	}
	public String getError() {
		return error;
	}
	public void setError(String error) {
		this.error = error;
	}
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}

}