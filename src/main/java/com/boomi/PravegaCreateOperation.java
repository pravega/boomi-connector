package com.boomi;

import com.boomi.connector.api.OperationResponse;
import com.boomi.connector.api.UpdateRequest;
import com.boomi.connector.util.BaseUpdateOperation;

public class PravegaCreateOperation extends BaseUpdateOperation {

	protected PravegaCreateOperation(PravegaConnection conn) {
		super(conn);
	}

	@Override
	protected void executeUpdate(UpdateRequest request, OperationResponse response) {
		// TODO Auto-generated method stub
	}

	@Override
    public PravegaConnection getConnection() {
        return (PravegaConnection) super.getConnection();
    }
}