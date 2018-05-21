package com.siperian.bdd.userexits.TradeFunds;

import com.siperian.bdd.userexits.datamodel.BDDObject;
import com.siperian.bdd.userexits.operations.AbstractBaseOperationPlugin;
import com.siperian.bdd.userexits.operations.ISaveOperationPlugin;
import com.siperian.bdd.userexits.operations.OperationExecutionError;
import com.siperian.bdd.userexits.operations.OperationResult;
import com.siperian.bdd.userexits.operations.OperationType;

/**
 *
 * @author owais.siraj
 */
public class CorporareCustomerSaveHandler extends AbstractBaseOperationPlugin implements ISaveOperationPlugin {

    @Override
    public OperationType getOperationType() {
        return OperationType.SAVE_OPERATION;
    }

    @Override
    public OperationResult beforeSave(BDDObject arg0) {
        return OperationResult.OK;
    }

    @Override
    public OperationResult afterSave(BDDObject arg0) {
        return OperationResult.OK;
    }

    @Override
    public OperationResult beforeEverything(BDDObject savingObject) {

        return validateCorporateCustomer(savingObject);
    }

    @Override
    public OperationResult afterEverything(BDDObject arg0) {
        return new OperationResult(new OperationExecutionError("SIP-50101", new String[]{arg0.getObjectName()}, getLocalizationGate()));
    }

    private OperationResult validateCorporateCustomer(BDDObject savingObject) {
        Object corporateCustomerName = savingObject.getValue(AppConstants.CORPORATE_CUSTOMER_NAME);
        if (corporateCustomerName == null || corporateCustomerName.toString().trim().isEmpty()) {
            return new OperationResult(new OperationExecutionError("SIP-50108", new String[0], getLocalizationGate()));
        }
        return OperationResult.OK;
    }

}