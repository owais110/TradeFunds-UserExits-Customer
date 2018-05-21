/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.siperian.bdd.userexits.TradeFunds;

import static com.siperian.bdd.userexits.TradeFunds.AppConstants.DEFAULT_EFFECTIVE_END_DATE;
import com.siperian.bdd.userexits.datamodel.BDDObject;
import com.siperian.bdd.userexits.operations.AbstractBaseOperationPlugin;
import com.siperian.bdd.userexits.operations.ISaveOperationPlugin;
import com.siperian.bdd.userexits.operations.OperationExecutionError;
import com.siperian.bdd.userexits.operations.OperationResult;
import com.siperian.bdd.userexits.operations.OperationType;
import com.siperian.sif.message.Field;
import com.siperian.sif.message.Record;
import com.siperian.sif.message.RecordKey;
import com.siperian.sif.message.SiperianObjectType;
import com.siperian.sif.message.SiperianResponse;
import com.siperian.sif.message.mrm.PutRequest;
import com.siperian.sif.message.mrm.PutResponse;
import com.siperian.sif.message.mrm.SearchQueryRequest;
import com.siperian.sif.message.mrm.SearchQueryResponse;
import java.text.ParseException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author owais.siraj
 */
public class MarketSaveHandler extends AbstractBaseOperationPlugin implements ISaveOperationPlugin {

    @Override
    public OperationType getOperationType() {
        return OperationType.SAVE_OPERATION;
    }

    @Override
    public OperationResult beforeSave(BDDObject bddo) {
        return OperationResult.OK;
    }

    @Override
    public OperationResult afterSave(BDDObject savedObject) {
        return OperationResult.OK;
    }

    @Override
    public OperationResult beforeEverything(BDDObject savingObject) {
        Map<String, List<BDDObject>> plannintAccounts = new HashMap<String, List<BDDObject>>();
        StringBuilder excludedPlanningAccounts = new StringBuilder();
        Map<String, List<BDDObject>> subPlanningAccounts = new HashMap<String, List<BDDObject>>();
        StringBuilder excludedSubPlanningAccounts = new StringBuilder();

        List<BDDObject> childPlanningAccounts = savingObject.getChildren(AppConstants.MARKET_CHILD_PLANNING_ACCOUNT);

        AppUtils.splitParentWithChildSimilarObjects(childPlanningAccounts, plannintAccounts, excludedPlanningAccounts,
                subPlanningAccounts, excludedSubPlanningAccounts, AppConstants.MARKET_CHILD_SUB_PLANNING_ACCOUNT, AppConstants.MARKET_COL_PLANNING_ACCT_ROWID_OBJECT,
                AppConstants.MARKET_COL_PLN_ACT_NOD_REL_EFF_STRT_DATE, AppConstants.MARKET_COL_PLN_ACT_NOD_REL_EFF_END_DATE, AppConstants.MARKET_COL_PLANNING_ACCT_ROWID_OBJECT,
                AppConstants.MARKET_COL_PLN_ACT_NOD_REL_EFF_STRT_DATE, AppConstants.MARKET_COL_PLN_ACT_NOD_REL_EFF_END_DATE);

        OperationResult operationResult = validateMarket(savingObject);
        if (operationResult != OperationResult.OK) {
            return operationResult;
        }
        operationResult = validateSalesRegions(savingObject);
        if (operationResult != OperationResult.OK) {
            return operationResult;
        }
        operationResult = validatePlanningAccounts(savingObject.getObjectName(), plannintAccounts, (excludedPlanningAccounts.length() == 0 ? null : excludedPlanningAccounts.toString()));
        if (operationResult != OperationResult.OK) {
            return operationResult;
        }

        operationResult = validateSubPlanningAccounts(savingObject.getObjectName(), subPlanningAccounts, (excludedSubPlanningAccounts.length() == 0 ? null : excludedSubPlanningAccounts.toString()));
        if (operationResult != OperationResult.OK) {
            return operationResult;
        }

        return operationResult;
    }

    @Override
    public OperationResult afterEverything(BDDObject savedObject) {
        return new OperationResult(new OperationExecutionError("SIP-50101", new String[]{savedObject.getObjectName()}, getLocalizationGate()));
    }

    private OperationResult validateMarket(BDDObject savingObject) {
        Object marketCode = savingObject.getValue(AppConstants.MARKET_COL_HIERARCHY_NODE_CODE);
        Object marketName = savingObject.getValue(AppConstants.MARKET_COL_HIERARCHY_NODE_NAME);
        String oldMarketCode = (String) (savingObject.getOldValue(AppConstants.MARKET_COL_HIERARCHY_NODE_CODE) == null ? "" : savingObject.getOldValue(AppConstants.MARKET_COL_HIERARCHY_NODE_CODE));
        String validateMarketCode = (String) (marketCode);
        if (!oldMarketCode.equals("")) {
            if (marketCode != null && !oldMarketCode.equals(marketCode)) {
                return new OperationResult(new OperationExecutionError("SIP-50113", new String[]{savingObject.getObjectName()}, getLocalizationGate()));
            }
        }
        if (marketCode != null) {
            boolean isCodeValidated = validateMarketCode.matches("^\\d+$");
            if (!isCodeValidated) {
                return new OperationResult(new OperationExecutionError("SIP-50114", new String[]{savingObject.getObjectName()}, getLocalizationGate()));
            }
        }
        if (marketCode != null && marketCode.toString().trim().isEmpty()) {
            return new OperationResult(new OperationExecutionError("SIP-50112", new String[]{AppConstants.SALES_HIERARCHY_LEVEL_SCREEN_FEILD_CODE}, getLocalizationGate()));
        }
        if (marketName == null || marketName.toString().trim().isEmpty()) {
            return new OperationResult(new OperationExecutionError("SIP-50112", new String[]{AppConstants.SALES_HIERARCHY_LEVEL_SCREEN_FEILD_NAME}, getLocalizationGate()));
        }
        if ((marketCode != null)) {
            String filterCriteria = "TRIM(HIERARCHY_NODE_CODE) = '" + marketCode + "'";
            if (!savingObject.isCreated()) {
                filterCriteria += " AND TRIM(ROWID_OBJECT) NOT IN ( " + savingObject.getRowId().trim() + " )";
            }

            if (objectDBValidation(AppConstants.BO_HIERARCHY_NODE, filterCriteria, null)) {

                return new OperationResult(new OperationExecutionError(
                        "SIP-50111",
                        new String[]{savingObject.getObjectName()},
                        getLocalizationGate()));
            }
        }
        if (!savingObject.isCreated() && savingObject.getChangedColumns().contains(AppConstants.MARKET_COL_HIERARCHY_NODE_CODE)) {
            String filterCriteria = "TRIM(PARENT_NODE_ID) = '" + savingObject.getRowId().trim()
                    + "' OR TRIM(CHILD_NODE_ID) = '" + savingObject.getRowId().trim() + "'";
            if (objectDBValidation(AppConstants.BO_HIERARCHY_REL, filterCriteria, null)) {

                return new OperationResult(new OperationExecutionError(
                        "SIP-50109",
                        new String[]{savingObject.getObjectName()},
                        getLocalizationGate()));
            }
            filterCriteria = "TRIM(HIERARCHY_NODE_ID) = '" + savingObject.getRowId().trim() + "'";
            if (objectDBValidation(AppConstants.BO_PLN_ACT_NDE_REL, filterCriteria, null)) {

                return new OperationResult(new OperationExecutionError(
                        "SIP-50109",
                        new String[]{savingObject.getObjectName()},
                        getLocalizationGate()));
            }

        }
        return OperationResult.OK;
    }

    private OperationResult validatePlanningAccounts(String savingObjectName, Map<String, List<BDDObject>> planningAccounts, String excludedPlanningAccounts) {

        for (String planningAccountId : planningAccounts.keySet()) {
            List<BDDObject> planningAccountsList = planningAccounts.get(planningAccountId);
            if (planningAccountsList == null || planningAccountsList.isEmpty()) {
                continue;
            }

            Collections.sort(planningAccountsList, new BddDateComparator(AppConstants.MARKET_COL_PLN_ACT_NOD_REL_EFF_STRT_DATE));

            boolean isCurrentAssociationPresent = false;
            BDDObject previousPlanningAccount = null;
            Date previousPlanAccntEffectiveStartDate = null;
            Date previousPlanAccntEffectiveEndDate = null;

            for (BDDObject planningAccount : planningAccountsList) {
                Date effectiveStartDate = null;
                Date effectiveEndDate = null;
                Object effectiveEndDateStr = planningAccount.getValue(AppConstants.MARKET_COL_PLN_ACT_NOD_REL_EFF_END_DATE);
                Object effectiveStartDateStr = planningAccount.getValue(AppConstants.MARKET_COL_PLN_ACT_NOD_REL_EFF_STRT_DATE);

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
                            new String[]{savingObjectName},
                            getLocalizationGate()));
                }

                // Invalid - More than one current association for a planning account
                if (effectiveEndDate.equals(DEFAULT_EFFECTIVE_END_DATE)
                        && isCurrentAssociationPresent) {

                    return new OperationResult(new OperationExecutionError(
                            "SIP-50104",
                            new String[]{savingObjectName},
                            getLocalizationGate()));
                }

                if (effectiveEndDate.equals(DEFAULT_EFFECTIVE_END_DATE)) {
                    isCurrentAssociationPresent = true;
                }

                if (null != previousPlanningAccount && (effectiveStartDate.equals(previousPlanAccntEffectiveEndDate) || effectiveStartDate.before(previousPlanAccntEffectiveEndDate))) {

                    return new OperationResult(new OperationExecutionError(
                            "SIP-50105",
                            new String[]{savingObjectName},
                            getLocalizationGate()));
                }
                String effectiveStartDateSQLStr = AppUtils.getSQLStandardDateStr(effectiveStartDate);
                String effectiveEndDateSQLStr = AppUtils.getSQLStandardDateStr(effectiveEndDate);

                String filterCriteria = "((RELATIOSHIP_EFF_STRT_DATE <= TO_DATE('" + effectiveStartDateSQLStr
                        + "') AND RELATIONSHIP_EFF_END_DATE >= TO_DATE('" + effectiveStartDateSQLStr
                        + "')) OR (RELATIOSHIP_EFF_STRT_DATE <= TO_DATE('" + effectiveEndDateSQLStr
                        + "') AND RELATIONSHIP_EFF_END_DATE >= TO_DATE('" + effectiveEndDateSQLStr
                        + "')) OR (RELATIOSHIP_EFF_STRT_DATE >= TO_DATE('" + effectiveStartDateSQLStr
                        + "') AND RELATIONSHIP_EFF_END_DATE <= TO_DATE('" + effectiveEndDateSQLStr
                        + "'))) AND TRIM(PLANNING_ACCT_ID) = '" + planningAccountId.trim() + "'";
                if (objectDBValidation(AppConstants.BO_PLN_ACT_NDE_REL,
                        filterCriteria,
                        excludedPlanningAccounts)) {

                    return new OperationResult(new OperationExecutionError(
                            "SIP-50107",
                            new String[]{savingObjectName},
                            getLocalizationGate()));
                }

                //Validation completed for current Planning account so assign that to previous Planning account for next Planning account validation
                previousPlanningAccount = planningAccount;
                previousPlanAccntEffectiveStartDate = effectiveStartDate;
                previousPlanAccntEffectiveEndDate = effectiveEndDate;
            }
        }

        return OperationResult.OK;
    }

    private OperationResult validateSubPlanningAccounts(String savingObjectName, Map<String, List<BDDObject>> subPlanningAccounts, String excludedSubPlanningAccounts) {

        for (String subPlanningAccountId : subPlanningAccounts.keySet()) {
            List<BDDObject> subPlanningAccount = subPlanningAccounts.get(subPlanningAccountId);
            if (subPlanningAccount == null || subPlanningAccount.size() == 0) {
                continue;
            }

            Collections.sort(subPlanningAccount, new BddDateComparator(AppConstants.MARKET_COL_PLN_ACT_REL_EFF_STRT_DATE));

            boolean isCurrentAssociationPresent = false;
            BDDObject previousRegion = null;
            Date previousRegionEffectiveStartDate = null;
            Date previousRegionEffectiveEndDate = null;

            for (BDDObject region : subPlanningAccount) {
                Date effectiveStartDate = null;
                Date effectiveEndDate = null;
                Object effectiveEndDateStr = region.getValue(AppConstants.MARKET_COL_PLN_ACT_REL_EFF_END_DATE);
                Object effectiveStartDateStr = region.getValue(AppConstants.MARKET_COL_PLN_ACT_REL_EFF_STRT_DATE);

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
                            new String[]{savingObjectName},
                            getLocalizationGate()));
                }

                // Invalid - More than one current association for a channel
                if (effectiveEndDate.equals(DEFAULT_EFFECTIVE_END_DATE)
                        && isCurrentAssociationPresent) {

                    return new OperationResult(new OperationExecutionError(
                            "SIP-50104",
                            new String[]{savingObjectName},
                            getLocalizationGate()));
                }

                if (effectiveEndDate.equals(DEFAULT_EFFECTIVE_END_DATE)) {
                    isCurrentAssociationPresent = true;
                }

                if (null != previousRegion && (effectiveStartDate.equals(previousRegionEffectiveEndDate) || effectiveStartDate.before(previousRegionEffectiveEndDate))) {

                    return new OperationResult(new OperationExecutionError(
                            "SIP-50105",
                            new String[]{savingObjectName},
                            getLocalizationGate()));
                }
                String effectiveStartDateSQLStr = AppUtils.getSQLStandardDateStr(effectiveStartDate);
                String effectiveEndDateSQLStr = AppUtils.getSQLStandardDateStr(effectiveEndDate);

                String filterCriteria = "((RELATIONSHIP_EFF_STRT_DATE <= TO_DATE('" + effectiveStartDateSQLStr
                        + "') AND RELATIONSHIP_EFF_END_DATE >= TO_DATE('" + effectiveStartDateSQLStr
                        + "')) OR (RELATIONSHIP_EFF_STRT_DATE <= TO_DATE('" + effectiveEndDateSQLStr
                        + "') AND RELATIONSHIP_EFF_END_DATE >= TO_DATE('" + effectiveEndDateSQLStr
                        + "')) OR (RELATIONSHIP_EFF_STRT_DATE >= TO_DATE('" + effectiveStartDateSQLStr
                        + "') AND RELATIONSHIP_EFF_END_DATE <= TO_DATE('" + effectiveEndDateSQLStr
                        + "'))) AND TRIM(SUB_PLANNING_ACCOUNT_ID) = '" + subPlanningAccountId.trim() + "'";
                if (objectDBValidation(AppConstants.BO_PLAN_ACCT_REL,
                        filterCriteria,
                        excludedSubPlanningAccounts)) {

                    return new OperationResult(new OperationExecutionError(
                            "SIP-50107",
                            new String[]{savingObjectName},
                            getLocalizationGate()));
                }

                //Validation completed for current channel so assign that to previous channesl for next channel validation
                previousRegion = region;
                previousRegionEffectiveStartDate = effectiveStartDate;
                previousRegionEffectiveEndDate = effectiveEndDate;
            }

        }

        return OperationResult.OK;
    }

    private OperationResult validateSalesRegions(BDDObject savingMarket) {
        List<BDDObject> salesRegions = savingMarket.getChildren(AppConstants.MARKET_CHILD_SALES_REGION);

        String excludedSalesRegions = AppUtils.getExcludedObjectsId(salesRegions,
                AppConstants.MARKET_COL_HIERARCHY_REL_EFF_STRT_DATE, AppConstants.MARKET_COL_HIERARCHY_REL_EFF_END_DATE);

        Collections.sort(salesRegions, new BddDateComparator(AppConstants.MARKET_COL_HIERARCHY_REL_EFF_STRT_DATE));

        boolean isCurrentAssociationPresent = false;
        BDDObject previousSalesRegion = null;
        Date previousSalesRegionEffectiveStartDate = null;
        Date previousSalesRegionEffectiveEndDate = null;

        for (BDDObject salesRegion : salesRegions) {
            //Sales Region is marked to be deleted so no need to validate
            if (salesRegion.isRemoved()) {
                continue;
            }

            Date effectiveStartDate = null;
            Date effectiveEndDate = null;
            Object effectiveEndDateStr = salesRegion.getValue(AppConstants.MARKET_COL_HIERARCHY_REL_EFF_END_DATE);
            Object effectiveStartDateStr = salesRegion.getValue(AppConstants.MARKET_COL_HIERARCHY_REL_EFF_STRT_DATE);

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
                        new String[]{savingMarket.getObjectName()},
                        getLocalizationGate()));
            }

            // Invalid - More than one current association for a Sales Region
            if (effectiveEndDate.equals(DEFAULT_EFFECTIVE_END_DATE)
                    && isCurrentAssociationPresent) {

                return new OperationResult(new OperationExecutionError(
                        "SIP-50104",
                        new String[]{savingMarket.getObjectName()},
                        getLocalizationGate()));
            }

            if (effectiveEndDate.equals(DEFAULT_EFFECTIVE_END_DATE)) {
                isCurrentAssociationPresent = true;
            }

            if (null != previousSalesRegion && (effectiveStartDate.equals(previousSalesRegionEffectiveEndDate) || effectiveStartDate.before(previousSalesRegionEffectiveEndDate))) {

                return new OperationResult(new OperationExecutionError(
                        "SIP-50107",
                        new String[]{savingMarket.getObjectName()},
                        getLocalizationGate()));
            }

            //Validation completed for current channel so assign that to previous channesl for next channel validation
            previousSalesRegion = salesRegion;
            previousSalesRegionEffectiveStartDate = effectiveStartDate;
            previousSalesRegionEffectiveEndDate = effectiveEndDate;
        }
        return OperationResult.OK;
    }

    private boolean objectDBValidation(String baseObjectName, String filterCriteria, String excludedIds) {

        if (null != excludedIds && excludedIds.length() > 0) {
            filterCriteria += " AND TRIM(ROWID_OBJECT) NOT IN ( " + excludedIds + " )";
        }

        SearchQueryRequest searchRequest = new SearchQueryRequest();
        searchRequest.setReturnTotal(true);
        searchRequest.setSiperianObjectUid(SiperianObjectType.BASE_OBJECT.makeUid(baseObjectName));
        searchRequest.setFilterCriteria(filterCriteria);
        prepareRequest(searchRequest);
        SiperianResponse response = processRequest(searchRequest);

        if (null != response) {
            SearchQueryResponse entryPointResponse = (SearchQueryResponse) response;

            this.logger.debug("entryPointResponse.getRecordCount()" + entryPointResponse.getRecordCount());
            if (entryPointResponse.getRecordCount() > 0) {
                return true;
            }
        }

        return false;
    }
}