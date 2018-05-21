package com.siperian.bdd.userexits.TradeFunds;

import static com.siperian.bdd.userexits.TradeFunds.AppConstants.DEFAULT_EFFECTIVE_END_DATE;
import java.text.ParseException;
import java.util.Date;

import com.siperian.bdd.userexits.datamodel.BDDObject;
import com.siperian.bdd.userexits.operations.AbstractBaseOperationPlugin;
import com.siperian.bdd.userexits.operations.ISaveOperationPlugin;
import com.siperian.bdd.userexits.operations.OperationExecutionError;
import com.siperian.bdd.userexits.operations.OperationResult;
import com.siperian.bdd.userexits.operations.OperationType;

import com.siperian.sif.message.SiperianResponse;
import com.siperian.sif.message.mrm.SearchQueryRequest;
import com.siperian.sif.message.mrm.SearchQueryResponse;
import java.util.Collections;
import java.util.List;

public class ShipToSaveHandler extends AbstractBaseOperationPlugin implements ISaveOperationPlugin {

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
            OperationResult operationResult = validateSubPlanningAccount(savingObject.getObjectName(), savingObject.getChildren(AppConstants.SHIP_TO_SUB_PLN_ACCT),
                    AppUtils.getExcludedObjectsId(savingObject.getChildren(AppConstants.SHIP_TO_SUB_PLN_ACCT),
                            AppConstants.SHIP_TO_COL_RELATIONSHIP_EFFECTIVE_START_DATE, AppConstants.SHIP_TO_COL_RELATIONSHIP_EFFECTIVE_END_DATE));
            if (!operationResult.equals(OperationResult.OK)) {
                return operationResult;
            }

        } catch (ParseException e) {
            e.printStackTrace();
        }
        return OperationResult.OK;
    }

    private OperationResult validateSubPlanningAccount(String savingObjectName, List<BDDObject> subPlanningAccounts, String excludedSubPlanningAccountIds) throws ParseException {
        Collections.sort(subPlanningAccounts, new BddDateComparator(AppConstants.SHIP_TO_COL_RELATIONSHIP_EFFECTIVE_START_DATE));

        boolean isCurrentAssociationPresent = false;
        BDDObject previousSubPlanningAccount = null;
        Date previousSubPlanningAccountEffectiveStartDate = null;
        Date previousSubPlanningAccountEffectiveEndDate = null;

        for (BDDObject shiptoSubPlanningAccount : subPlanningAccounts) {
            //SubPlanning account is marked to be deleted so no need to validate
            if (shiptoSubPlanningAccount.isRemoved()) {
                continue;
            }

            Date effectiveStartDate = null;
            Date effectiveEndDate = null;
            Object effectiveEndDateStr = shiptoSubPlanningAccount.getValue(AppConstants.SHIP_TO_COL_RELATIONSHIP_EFFECTIVE_END_DATE);
            Object effectiveStartDateStr = shiptoSubPlanningAccount.getValue(AppConstants.SHIP_TO_COL_RELATIONSHIP_EFFECTIVE_START_DATE);

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
                        new String[]{savingObjectName},
                        getLocalizationGate()));
            }

            // Invalid - More than one current association for a Sub Planning Account
            if (effectiveEndDate.equals(DEFAULT_EFFECTIVE_END_DATE) && isCurrentAssociationPresent) {
                return new OperationResult(new OperationExecutionError(
                        "SIP-50104",
                        new String[]{savingObjectName},
                        getLocalizationGate()));
            }

            if (effectiveEndDate.equals(DEFAULT_EFFECTIVE_END_DATE)) {
                isCurrentAssociationPresent = true;
            }

            if (null != previousSubPlanningAccount && (effectiveStartDate.equals(previousSubPlanningAccountEffectiveEndDate) || effectiveStartDate.before(previousSubPlanningAccountEffectiveEndDate))) {
                return new OperationResult(new OperationExecutionError(
                        "SIP-50107",
                        new String[]{savingObjectName},
                        getLocalizationGate()));
            }

            //Validation completed for current channel so assign that to previous channesl for next channel validation
            previousSubPlanningAccount = shiptoSubPlanningAccount;
            previousSubPlanningAccountEffectiveStartDate = effectiveStartDate;
            previousSubPlanningAccountEffectiveEndDate = effectiveEndDate;

        }
        return OperationResult.OK;
    }

    private boolean isThereDateOverlap(String baseObjectName, String filterCriteria, String excludedIds) {

        if (null != excludedIds && excludedIds.length() > 0) {
            filterCriteria += " AND TRIM(ROWID_OBJECT) NOT IN ( " + excludedIds + " )";
        }

        SearchQueryRequest entryPointRequest = new SearchQueryRequest();
        entryPointRequest.setReturnTotal(true);
        entryPointRequest.setSiperianObjectUid(baseObjectName);
        entryPointRequest.setFilterCriteria(filterCriteria);
        prepareRequest(entryPointRequest);
        SiperianResponse response = processRequest(entryPointRequest);

        if (null != response) {
            SearchQueryResponse entryPointResponse = (SearchQueryResponse) response;

            if (entryPointResponse.getRecordCount() > 0) {
                return true;
            }
        }

        return false;
    }

}