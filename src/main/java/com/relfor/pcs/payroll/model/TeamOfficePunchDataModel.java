package com.relfor.pcs.payroll.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class TeamOfficePunchDataModel {
	@JsonProperty("Name")
	private String name;
	@JsonProperty("Empcode")
	private String empCode;
	@JsonProperty("PunchDate")
	private String punchDate;
	@JsonProperty("M_Flag")
	private String mFlag;
	private String mcid;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getEmpCode() {
		return empCode;
	}

	public void setEmpCode(String empCode) {
		this.empCode = empCode;
	}

	public String getPunchDate() {
		return punchDate;
	}

	public void setPunchDate(String punchDate) {
		this.punchDate = punchDate;
	}

	public String getmFlag() {
		return mFlag;
	}

	public void setmFlag(String mFlag) {
		this.mFlag = mFlag;
	}

	public String getMcid() {
		return mcid;
	}

	public void setMcid(String mcid) {
		this.mcid = mcid;
	}
}
