/*
*  Copyright 2019 Accenture. All Rights Reserved.
*  The trademarks used in these materials are the properties of their respective owners.
*  This work is protected by copyright law and contains valuable trade secrets and
*  confidential information.
*/

package com.boomi;

import java.util.Map;
/**
 * 
 * @author kritika.b.verma
 *
 */
public class OperationResponseDetails {

	int totalNumberOfRecords = 0;
	int numberOfRecordsProcessedSuccessfully = 0;
	int numberOfFailedRecords = 0;
	Map<String, String> failedRecordDetails = null;
/**
 * 
 * @param totalNumberOfRecords
 * @param failedRecordDetails
 */
	public OperationResponseDetails(int totalNumberOfRecords, Map<String, String> failedRecordDetails) {
		super();
		this.totalNumberOfRecords = totalNumberOfRecords;
		this.failedRecordDetails = failedRecordDetails;
	}
/**
 * 
 * @param record
 * @param errMsg
 */
	public void updateFailedRecordInResponse(String record, String errMsg) {
		numberOfFailedRecords++;
		if (null != getFailedRecordDetails()) {
			getFailedRecordDetails().put(record, errMsg);
		}
	}

	public int getTotalNumberOfRecords() {
		return totalNumberOfRecords;
	}

	public void setTotalNumberOfRecords(int totalNumberOfRecords) {
		this.totalNumberOfRecords = totalNumberOfRecords;
	}

	public int getNumberOfRecordsProcessedSuccessfully() {
		numberOfRecordsProcessedSuccessfully = getTotalNumberOfRecords() - getNumberOfFailedRecords();
		return numberOfRecordsProcessedSuccessfully;
	}

	public int getNumberOfFailedRecords() {
		return numberOfFailedRecords;
	}

	public Map<String, String> getFailedRecordDetails() {
		return failedRecordDetails;
	}

	public void setFailedRecordDetails(Map<String, String> failedRecordDetails) {
		this.failedRecordDetails = failedRecordDetails;
	}

}
