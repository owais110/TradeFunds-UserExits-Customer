package com.siperian.bdd.userexits.TradeFunds;

import com.siperian.bdd.userexits.datamodel.BDDObject;
import com.siperian.bdd.userexits.operations.AbstractBaseOperationPlugin;
import com.siperian.bdd.userexits.operations.ISaveOperationPlugin;
import com.siperian.bdd.userexits.operations.OperationExecutionError;
import com.siperian.bdd.userexits.operations.OperationResult;
import com.siperian.bdd.userexits.operations.OperationType;
import java.util.List;

/**
 *
 * @author owais.siraj
 */
public class ConsumptionBaseLineSourceSaveHandler extends AbstractBaseOperationPlugin implements ISaveOperationPlugin {

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
        OperationResult operationResult = validateConsumptionBaseLineSource(savingObject);
        if (!operationResult.equals(OperationResult.OK)) {
            return operationResult;
        }
            
        return OperationResult.OK;
    }

    @Override
    public OperationResult afterEverything(BDDObject arg0) {
        return new OperationResult(new OperationExecutionError("SIP-50101", new String[]{arg0.getObjectName()}, getLocalizationGate()));
    }

    private OperationResult validateConsumptionBaseLineSource(BDDObject consumptionBaselineSource) {
        Object consumptionBaseLineSourceName = consumptionBaselineSource.getValue(AppConstants.CNSMP_BASLN_SRC_NAME);
        if (consumptionBaseLineSourceName == null || consumptionBaseLineSourceName.toString().trim().isEmpty()) {
            return new OperationResult(new OperationExecutionError("SIP-50108", new String[0], getLocalizationGate()));
        }
        
        List<BDDObject> consumptionBaselineAreas = consumptionBaselineSource.getChildren(AppConstants.CONSUMPTION_BASELINE_SOURCE_AREA);        
        for (BDDObject consumptionBaselineArea : consumptionBaselineAreas) {
            OperationResult operationResult = validateConsumptionBaseLineArea(consumptionBaselineArea);
            if (!operationResult.equals(OperationResult.OK)) {
                return operationResult;
        }
            
        }
        return OperationResult.OK;
    }

    private OperationResult validateConsumptionBaseLineArea(BDDObject consumptionBaselineArea) {
        Object consumptionBaseLineAreaName = consumptionBaselineArea.getValue(AppConstants.CNSMP_BSLN_AREA_NAME);
        if (consumptionBaseLineAreaName == null || consumptionBaseLineAreaName.toString().trim().isEmpty()) {
            return new OperationResult(new OperationExecutionError("SIP-50108", new String[0], getLocalizationGate()));
        }
        return OperationResult.OK;
    }

}
