package com.relfor.pcs.payroll.model;

import java.util.List;

public class RegularizationRequestsOutputModel {
	private List<AttendanceRequestsDTO> regularizationRequests;
	private PageModel pageModel;

	public List<AttendanceRequestsDTO> getRegularizationRequests() {
		return regularizationRequests;
	}

	public void setRegularizationRequests(List<AttendanceRequestsDTO> regularizationRequests) {
		this.regularizationRequests = regularizationRequests;
	}

	public PageModel getPageModel() {
		return pageModel;
	}

	public void setPageModel(PageModel pageModel) {
		this.pageModel = pageModel;
	}
}
