package com.siperian.bdd.userexits.TradeFunds;

import java.text.ParseException;
import java.util.Date;
import java.util.List;

import com.siperian.bdd.userexits.datamodel.BDDObject;
import com.siperian.bdd.userexits.operations.ISaveOperationPlugin;
import com.siperian.bdd.userexits.operations.OperationExecutionError;
import com.siperian.bdd.userexits.operations.OperationResult;
import com.siperian.bdd.userexits.operations.OperationType;

import static com.siperian.bdd.userexits.TradeFunds.AppConstants.DEFAULT_EFFECTIVE_END_DATE;
import com.siperian.bdd.userexits.operations.AbstractBaseOperationPlugin;

public class WholeSaleSaveHandler extends AbstractBaseOperationPlugin implements ISaveOperationPlugin {

    @Override
    public OperationType getOperationType() {
        return OperationType.SAVE_OPERATION;
    }

    @Override
    public OperationResult afterEverything(BDDObject arg0) {
        return new OperationResult(new OperationExecutionError("SIP-50101", new String[]{arg0.getObjectName()}, getLocalizationGate()));
    }

    @Override
    public OperationResult afterSave(BDDObject arg0) {
        return OperationResult.OK;
    }

    @Override
    public OperationResult beforeSave(BDDObject arg0) {
        return OperationResult.OK;
    }

    @Override
    public OperationResult beforeEverything(BDDObject savingObject) {
        try {
            return validateWholeSaleIndirect(savingObject);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return OperationResult.OK;
    }

    private OperationResult validateWholeSaleIndirect(BDDObject wholeSale) throws ParseException {
        List<BDDObject> wholeSaleIndirectList = wholeSale.getChildren(AppConstants.WHOLE_SALE_CHILD_INDIRECT);
        for (BDDObject wholeSaleIndirect : wholeSaleIndirectList) {
            Date effectiveStartDate = null;
            Date effectiveEndDate = null;
            Object effectiveEndDateStr = wholeSaleIndirect.getValue(AppConstants.WHOLE_SALE_COL_RELATIONSHIP_EFFECTIVE_END_DATE);
            Object effectiveStartDateStr = wholeSaleIndirect.getValue(AppConstants.WHOLE_SALE_COL_RELATIONSHIP_EFFECTIVE_START_DATE);

            try {
                effectiveStartDate = AppUtils.convertObjectToDate(effectiveStartDateStr);
            } catch (ParseException e1) {
            }
            if (effectiveEndDateStr == null) {
                effectiveEndDate = DEFAULT_EFFECTIVE_END_DATE;
            } else {
                try {
                    effectiveEndDate = AppUtils
                            .convertObjectToDate(effectiveEndDateStr);
                } catch (ParseException e1) {
                }
            }
            // Invalid – start date is later than end date
            if (effectiveEndDate.before(effectiveStartDate)) {
                return new OperationResult(new OperationExecutionError(
                        "SIP-50103",
                        new String[]{wholeSale.getObjectName()},
                        getLocalizationGate()));
            }
        }
        return OperationResult.OK;
    }

}