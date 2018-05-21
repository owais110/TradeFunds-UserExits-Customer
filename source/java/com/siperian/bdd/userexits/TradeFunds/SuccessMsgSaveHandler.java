package com.siperian.bdd.userexits.TradeFunds;

import com.siperian.bdd.userexits.datamodel.BDDObject;
import com.siperian.bdd.userexits.operations.AbstractBaseOperationPlugin;
import com.siperian.bdd.userexits.operations.ISaveOperationPlugin;
import com.siperian.bdd.userexits.operations.OperationExecutionError;
import com.siperian.bdd.userexits.operations.OperationResult;
import com.siperian.bdd.userexits.operations.OperationType;

public class SuccessMsgSaveHandler extends AbstractBaseOperationPlugin implements ISaveOperationPlugin {

    public OperationResult afterSave(BDDObject savedObject) {
        return OperationResult.OK;
    }

    public OperationType getOperationType() {
        return OperationType.SAVE_OPERATION;
    }

    public OperationResult afterEverything(BDDObject arg0) {
        return new OperationResult(new OperationExecutionError("SIP-50101", new String[]{arg0.getObjectName()}, getLocalizationGate()));
    }

    public OperationResult beforeEverything(BDDObject arg0) {
        return OperationResult.OK;
    }

    public OperationResult beforeSave(BDDObject savingObject) {
        return OperationResult.OK;
    }
}