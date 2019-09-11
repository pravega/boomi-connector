package com.boomi;

import com.boomi.connector.api.GetRequest;
import com.boomi.connector.api.OperationResponse;
import com.boomi.connector.util.BaseGetOperation;

public class PravegaGetOperation extends BaseGetOperation {

    protected PravegaGetOperation(PravegaConnection conn) {
        super(conn);
    }

	@Override
	protected void executeGet(GetRequest request, OperationResponse response) {
		// TODO Auto-generated method stub
	}

	@Override
    public PravegaConnection getConnection() {
        return (PravegaConnection) super.getConnection();
    }
}