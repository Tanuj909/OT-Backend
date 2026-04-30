package com.ot.service;

public interface TransferService {

	void markReadyForIpdTransfer(Long operationId);

	void markAcceptedByIpd(Long operationId);

}
