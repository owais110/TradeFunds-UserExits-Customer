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
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SubPlanningAccountSaveHandler extends AbstractBaseOperationPlugin implements ISaveOperationPlugin {

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
            OperationResult operationResult = validateSubPlanningAccount(savingObject);
            if (!operationResult.equals(OperationResult.OK)) {
                return operationResult;
            }

            operationResult = validateShipTo(savingObject);
            if (!operationResult.equals(OperationResult.OK)) {
                return operationResult;
            }

            operationResult = validatePlanningAccount(savingObject);
            if (!operationResult.equals(OperationResult.OK)) {
                return operationResult;
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return OperationResult.OK;
    }

    private OperationResult validateSubPlanningAccount(BDDObject subPlanningAccount) throws ParseException {
        Object subPlanningAccountName = subPlanningAccount.getValue(AppConstants.SUB_PLANNING_ACCOUNT_NAME);
        Object effectiveEndDateStr = subPlanningAccount.getValue(AppConstants.SUB_PLN_ACCT_COL_EFF_END_DATE);
        Object effectiveStartDateStr = subPlanningAccount.getValue(AppConstants.SUB_PLN_ACCT_COL_EFF_START_DATE);
        Object jmsSalesOrgCode = subPlanningAccount.getValue(AppConstants.SUB_PLN_ACCT_COL_JMS_SALES_ORG_CODE);
        String oldJmsSalesOrgCode = (String) (subPlanningAccount.getOldValue(AppConstants.SUB_PLN_ACCT_COL_JMS_SALES_ORG_CODE) == null ? "" : subPlanningAccount.getOldValue(AppConstants.SUB_PLN_ACCT_COL_JMS_SALES_ORG_CODE));
        Date effectiveStartDate = null;
        Date effectiveEndDate = null;

        try {
            effectiveStartDate = AppUtils.convertObjectToDate(effectiveStartDateStr);
        } catch (ParseException e1) {
        }
        if (effectiveEndDateStr == null) {
            effectiveEndDate = DEFAULT_EFFECTIVE_END_DATE;
        } else {
            try {
                effectiveEndDate = AppUtils.convertObjectToDate(effectiveEndDateStr);
            } catch (ParseException e1) {
            }
            //Validate JMS Sales Organization Code
            if (!oldJmsSalesOrgCode.equals("")) {
                if (jmsSalesOrgCode != null && !oldJmsSalesOrgCode.equals(jmsSalesOrgCode)) {
                    return new OperationResult(new OperationExecutionError("SIP-50115", new String[]{subPlanningAccount.getObjectName()}, getLocalizationGate()));
                }
            }
            //Sub Planning Account Name is empty or Spaces
            if (subPlanningAccountName == null || subPlanningAccountName.toString().trim().isEmpty()) {
                return new OperationResult(new OperationExecutionError("SIP-50108", new String[0], getLocalizationGate()));
            }
        }
        // Invalid – start date is later than end date
        if (effectiveEndDate.before(effectiveStartDate)) {
            return new OperationResult(new OperationExecutionError("SIP-50103", new String[]{subPlanningAccount.getObjectName()}, getLocalizationGate()));
        }

        return OperationResult.OK;
    }

    private OperationResult validateShipTo(BDDObject subPlanningAccount) throws ParseException {
        Map<String, List<BDDObject>> shipTos = new HashMap<String, List<BDDObject>>();
        StringBuilder excludedShipTo = new StringBuilder();

        AppUtils.splitSimilarObjects(subPlanningAccount.getChildren(AppConstants.SUBPLANNING_ACCT_SHIPTO), shipTos, excludedShipTo,
                AppConstants.SUB_PLN_ACCT_SHIP_TO_ROWID_OBJECT, AppConstants.SUB_PLN_ACCT_COL_SHIP_TO_REL_EFF_END_DATE, AppConstants.SUB_PLN_ACCT_COL_SHIP_TO_REL_EFF_START_DATE);

        for (String shipToId : shipTos.keySet()) {
            List<BDDObject> shipToList = shipTos.get(shipToId);
            if (shipToList == null || shipToList.size() == 0) {
                continue;
            }

            Collections.sort(shipToList, new BddDateComparator(AppConstants.SUB_PLN_ACCT_COL_SHIP_TO_REL_EFF_START_DATE));

            boolean isCurrentAssociationPresent = false;
            BDDObject previousShipTo = null;
            Date previousShipToEffectiveStartDate = null;
            Date previousShipToEffectiveEndDate = null;

            for (BDDObject shipTo : shipToList) {
                Date effectiveStartDate = null;
                Date effectiveEndDate = null;
                Object effectiveEndDateStr = shipTo.getValue(AppConstants.SUB_PLN_ACCT_COL_SHIP_TO_REL_EFF_END_DATE);
                Object effectiveStartDateStr = shipTo.getValue(AppConstants.SUB_PLN_ACCT_COL_SHIP_TO_REL_EFF_START_DATE);

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
                            new String[]{subPlanningAccount.getObjectName()},
                            getLocalizationGate()));
                }

                // Invalid - More than one current association for a consumption base line area
                if (effectiveEndDate.equals(DEFAULT_EFFECTIVE_END_DATE)
                        && isCurrentAssociationPresent) {
                    return new OperationResult(new OperationExecutionError(
                            "SIP-50104",
                            new String[]{subPlanningAccount.getObjectName()},
                            getLocalizationGate()));
                }

                if (effectiveEndDate.equals(DEFAULT_EFFECTIVE_END_DATE)) {
                    isCurrentAssociationPresent = true;
                }

                if (null != previousShipTo && (effectiveStartDate.equals(previousShipToEffectiveEndDate) || effectiveStartDate.before(previousShipToEffectiveEndDate))) {
                    return new OperationResult(new OperationExecutionError(
                            "SIP-50105",
                            new String[]{subPlanningAccount.getObjectName()},
                            getLocalizationGate()));
                }

                String sqlEffectiveStartDate = AppUtils.getSQLStandardDateStr(effectiveStartDate);
                String sqlEffectiveEndDate = AppUtils.getSQLStandardDateStr(effectiveEndDate);
                String filterCriteria = "((RELATIONSHIP_EFF_STRT_DATE <= TO_DATE('" + sqlEffectiveStartDate
                        + "') AND RELATIONSHIP_EFF_END_DATE >= TO_DATE('" + sqlEffectiveStartDate
                        + "')) OR (RELATIONSHIP_EFF_STRT_DATE <= TO_DATE('" + sqlEffectiveEndDate
                        + "') AND RELATIONSHIP_EFF_END_DATE >= TO_DATE('" + sqlEffectiveEndDate
                        + "')) OR (RELATIONSHIP_EFF_STRT_DATE >= TO_DATE('" + sqlEffectiveStartDate
                        + "') AND RELATIONSHIP_EFF_END_DATE <= TO_DATE('" + sqlEffectiveEndDate
                        + "'))) AND TRIM(SHIP_TO_CUSTOMER_ID) = '" + shipToId.trim() + "'";
                if (isThereDateOverlap(AppConstants.BO_PLN_SHIPTO_REL,
                        filterCriteria,
                        excludedShipTo.toString())) {
                    return new OperationResult(new OperationExecutionError(
                            "SIP-50107",
                            new String[]{subPlanningAccount.getObjectName()},
                            getLocalizationGate()));
                }

                //Validation completed for current channel so assign that to previous channesl for next channel validation
                previousShipTo = shipTo;
                previousShipToEffectiveStartDate = effectiveStartDate;
                previousShipToEffectiveEndDate = effectiveEndDate;

            }
        }
        return OperationResult.OK;
    }

    private OperationResult validatePlanningAccount(BDDObject subPlanningAccount) throws ParseException {
        List<BDDObject> planningAccountList = subPlanningAccount.getChildren(AppConstants.SUBPLANNING_ACCT_PLANNING_ACCT);
        String excludedPlanningAccountIds = AppUtils.getExcludedObjectsId(planningAccountList,
                AppConstants.SUB_PLN_ACCT_COL_PLN_ACCT_REL_EFF_START_DATE, AppConstants.SUB_PLN_ACCT_COL_PLNACCT_REL_EFF_END_DATE);

        Collections.sort(planningAccountList, new BddDateComparator(AppConstants.SUB_PLN_ACCT_COL_PLN_ACCT_REL_EFF_START_DATE));

        boolean isCurrentAssociationPresent = false;
        BDDObject previousPlanningAccount = null;
        Date previousPlannAccntEffectiveStartDate = null;
        Date previousPlannAccntEffectiveEndDate = null;

        for (BDDObject planningAccount : planningAccountList) {
            //Planning account is marked to be deleted so no need to validate
            if (planningAccount.isRemoved()) {
                continue;
            }

            Date effectiveStartDate = null;
            Date effectiveEndDate = null;
            Object effectiveEndDateStr = planningAccount.getValue(AppConstants.SUB_PLN_ACCT_COL_PLNACCT_REL_EFF_END_DATE);
            Object effectiveStartDateStr = planningAccount.getValue(AppConstants.SUB_PLN_ACCT_COL_PLN_ACCT_REL_EFF_START_DATE);

            try {
                effectiveStartDate = AppUtils.convertObjectToDate(effectiveStartDateStr);
            } catch (ParseException e1) {
            }
            if (effectiveEndDateStr == null) {
                effectiveEndDate = DEFAULT_EFFECTIVE_END_DATE;
            } else {
                try {
                    effectiveEndDate = AppUtils.convertObjectToDate(effectiveEndDateStr);
                } catch (ParseException e1) {
                }
            }
            // Invalid – start date is later than end date
            if (effectiveEndDate.before(effectiveStartDate)) {
                return new OperationResult(new OperationExecutionError(
                        "SIP-50103",
                        new String[]{planningAccount.getObjectName()},
                        getLocalizationGate()));
            }

            // Invalid - More than one current association for a sub planning account
            if (effectiveEndDate.equals(DEFAULT_EFFECTIVE_END_DATE) && isCurrentAssociationPresent) {
                return new OperationResult(new OperationExecutionError(
                        "SIP-50104",
                        new String[]{planningAccount.getObjectName()},
                        getLocalizationGate()));
            }

            if (effectiveEndDate.equals(DEFAULT_EFFECTIVE_END_DATE)) {
                isCurrentAssociationPresent = true;
            }

            if (null != previousPlanningAccount && (effectiveStartDate.equals(previousPlannAccntEffectiveEndDate) || effectiveStartDate.before(previousPlannAccntEffectiveEndDate))) {
                return new OperationResult(new OperationExecutionError(
                        "SIP-50107",
                        new String[]{planningAccount.getObjectName()},
                        getLocalizationGate()));
            }
            //Validation completed for current channel so assign that to previous channesl for next channel validation
            previousPlanningAccount = planningAccount;
            previousPlannAccntEffectiveStartDate = effectiveStartDate;
            previousPlannAccntEffectiveEndDate = effectiveEndDate;
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
