/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
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
public class JmsSalesOrganizationSaveHandler extends AbstractBaseOperationPlugin implements ISaveOperationPlugin {

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
        this.logger.debug("Owais::::: ");
        this.logger.debug("inside savingObject : validateJmsSalesOrganization");
        return validateJmsSalesOrganization(savingObject);
    }

    @Override
    public OperationResult afterEverything(BDDObject arg0) {
        return new OperationResult(new OperationExecutionError("SIP-50101", new String[]{arg0.getObjectName()}, getLocalizationGate()));
    }

    private OperationResult validateJmsSalesOrganization(BDDObject savingObject) {
        Object jmsSalesOrganizationCode = savingObject.getValue(AppConstants.JMS_SALES_ORGANIZATION_CODE);
        Object jmsSalesOrganizationName = savingObject.getValue(AppConstants.JMS_SALES_ORGANIZATION_NAME);
        if (jmsSalesOrganizationCode == null || jmsSalesOrganizationCode.toString().trim().isEmpty()
                || jmsSalesOrganizationName == null || jmsSalesOrganizationName.toString().trim().isEmpty()) {
            return new OperationResult(new OperationExecutionError("SIP-50108", new String[0], getLocalizationGate()));
        }
        return OperationResult.OK;
    }

}