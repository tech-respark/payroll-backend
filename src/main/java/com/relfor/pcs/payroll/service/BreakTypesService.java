package com.relfor.pcs.payroll.service;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.relfor.pcs.payroll.entity.BreakTypes;
import com.relfor.pcs.payroll.repository.BreakTypesRepository;

@Service
public class BreakTypesService {
//	@Autowired
//    protected RequestContext requestContext;

	@Autowired
	BreakTypesRepository breakTypesRepo;

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	public BreakTypes createBreakTypes(BreakTypes breakType) {
		BreakTypes breakTypes = null;
		try {
			breakTypes = breakTypesRepo.save(breakType);
			return breakTypes;
		} catch (Exception e) {
			logger.error(e.getClass().getName(), e);
			throw e;
		}
	}

	public List<BreakTypes> getBreakTypes(Long storeId, Long tenantId) {
		List<BreakTypes> breakTypes = new ArrayList<>();
		try {
			breakTypes = breakTypesRepo.getBreakTypes(storeId, tenantId);
			return breakTypes;
		} catch (Exception e) {
			logger.error(e.getClass().getName(), e);
			throw e;
		}
	}
}