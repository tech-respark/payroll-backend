package com.relfor.pcs.payroll.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class TeamOfficeResponseModel {
	@JsonProperty("Error")
	private Boolean error;
	@JsonProperty("Msg")
	private String msg;
	@JsonProperty("IsAdmin")
	private Boolean isAdmin;
	@JsonProperty("PunchData")
	private List<TeamOfficePunchDataModel> punchDataModelList;

	public String getMsg() {
		return msg;
	}

	public void setMsg(String msg) {
		this.msg = msg;
	}

	public Boolean getError() {
		return error;
	}

	public void setError(Boolean error) {
		this.error = error;
	}

	public Boolean getIsAdmin() {
		return isAdmin;
	}

	public void setIsAdmin(Boolean admin) {
		isAdmin = admin;
	}

	public List<TeamOfficePunchDataModel> getPunchDataModelList() {
		return punchDataModelList;
	}

	public void setPunchDataModelList(List<TeamOfficePunchDataModel> punchDataModelList) {
		this.punchDataModelList = punchDataModelList;
	}
}
