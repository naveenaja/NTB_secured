package com.neb.service;

import com.neb.dto.EmployeeBankDetailsRequest;
import com.neb.dto.EmployeeBankDetailsResponse;

public interface EmployeeBankDetailsService {
	public  EmployeeBankDetailsResponse addBankDetails(Long employeeId,EmployeeBankDetailsRequest request);
	public EmployeeBankDetailsResponse UpdateBankDetails(Long id, EmployeeBankDetailsRequest request);
	public EmployeeBankDetailsResponse getBankDetailsByEmployeeId(Long id);
}
