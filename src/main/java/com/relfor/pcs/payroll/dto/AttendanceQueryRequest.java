package com.relfor.pcs.payroll.dto;

import java.util.List;

/**
 * Request body for dynamic attendance report queries.
 *
 * Designed to support:
 *  - staff filtering (staffIds)
 *  - selective response fields (metrics)
 *  - sorting and pagination
 *
 * Dates are expected in yyyy-MM-dd.
 */
public class AttendanceQueryRequest {
	public Long tenantId;
	public Long storeId;

	/**
	 * Optional. If provided, only these staffIds are returned.
	 */
	public List<String> staffIds;

	/**
	 * Optional. If provided, only these keys are returned (plus staffId for context).
	 * Keys must match output map keys (e.g. "name", "designation", "productiveTime", "totalHours", "leaves", etc).
	 * <p>
	 * Special:
	 * - "shiftSummary": aggregated metrics derived from the "shifts" array
	 */
	public List<String> metrics;

	public DateRange dateRange;
	public SortSpec sort;

	/**
	 * Max rows per page. Default is service-defined.
	 */
	public Integer limit;
	/**
	 * 1-based page number. Default: 1.
	 */
	public Integer page;

	public static class DateRange {
		public String startDate;
		public String endDate;
	}

	public static class SortSpec {
		public String field;
		public String order; // asc | desc (default: desc)
	}
}
