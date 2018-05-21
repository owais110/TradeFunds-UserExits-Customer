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
public class RegionSaveHandler extends AbstractBaseOperationPlugin implements ISaveOperationPlugin {

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
        try {
            Map<String, List<BDDObject>> markets = new HashMap<String, List<BDDObject>>();
            StringBuilder excludedMarkets = new StringBuilder();
            Map<String, List<BDDObject>> planningAccounts = new HashMap<String, List<BDDObject>>();
            StringBuilder excludedPlanningAccounts = new StringBuilder();

            List<BDDObject> childCurrentMarkets = savingObject.getChildren(AppConstants.REGION_CHILD_SALES_MARKET);
            AppUtils.splitParentWithChildSimilarObjects(childCurrentMarkets, markets, excludedMarkets,
                    planningAccounts, excludedPlanningAccounts, AppConstants.REGION_CHILD_PLANNING_ACCOUNT, AppConstants.SAL_REGION_MARKET_COL_ROWID_OBJECT,
                    AppConstants.SAL_SEG_COL_RELATIONSHIP_EFF_STRT_DATE, AppConstants.SAL_SEG_COL_RELATIONSHIP_EFF_END_DATE, AppConstants.SAL_REGION_PLANNING_ACCT_ROWID_OBJECT,
                    AppConstants.SAL_REGION_RELATIOSHIP_COL_EFF_STRT_DATE, AppConstants.SAL_REGION_RELATIONSHIP_COL_EFF_END_DATE);

            OperationResult operationResult = validateRegion(savingObject);
            if (operationResult != OperationResult.OK) {
                return operationResult;
            }
            operationResult = validateSalesRegionDivision(savingObject);
            if (operationResult != OperationResult.OK) {
                return operationResult;
            }
            operationResult = validateMarket(savingObject.getObjectName(), markets, (excludedMarkets.length() == 0 ? null : excludedMarkets.toString()));
            if (operationResult != OperationResult.OK) {
                return operationResult;
            }

            operationResult = validateMarketPlanningAccounts(savingObject.getObjectName(), planningAccounts, (excludedPlanningAccounts.length() == 0 ? null : excludedPlanningAccounts.toString()));
            if (operationResult != OperationResult.OK) {
                return operationResult;
            }

            return operationResult;
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return OperationResult.OK;
    }

    @Override
    public OperationResult afterEverything(BDDObject savedObject) {
        return new OperationResult(new OperationExecutionError("SIP-50101", new String[]{savedObject.getObjectName()}, getLocalizationGate()));
    }

    private OperationResult validateRegion(BDDObject savingObject) {
        Object regionCode = savingObject.getValue(AppConstants.SAL_REGION_COL_HIERARCHY_NODE_CODE);
        Object regionName = savingObject.getValue(AppConstants.SAL_REGION_COL_HIERARCHY_NODE_NAME);
        String oldRegionCode = (String) (savingObject.getOldValue(AppConstants.SAL_REGION_COL_HIERARCHY_NODE_CODE) == null ? "" : savingObject.getOldValue(AppConstants.SAL_REGION_COL_HIERARCHY_NODE_CODE));
        String validateRegionCode = (String) (regionCode);
        if (!oldRegionCode.equals("")) {
            if (regionCode != null && !oldRegionCode.equals(regionCode)) {
                return new OperationResult(new OperationExecutionError("SIP-50113", new String[]{savingObject.getObjectName()}, getLocalizationGate()));
            }
        }
        if (regionCode != null) {
            boolean isCodeValidated = validateRegionCode.matches("^\\d+$");
            if (!isCodeValidated) {
                return new OperationResult(new OperationExecutionError("SIP-50114", new String[]{savingObject.getObjectName()}, getLocalizationGate()));
            }
        }
        if (regionCode != null && regionCode.toString().trim().isEmpty()) {
            return new OperationResult(new OperationExecutionError("SIP-50112", new String[]{AppConstants.SALES_HIERARCHY_LEVEL_SCREEN_FEILD_CODE}, getLocalizationGate()));
        }
        if (regionName == null || regionName.toString().trim().isEmpty()) {
            return new OperationResult(new OperationExecutionError("SIP-50112", new String[]{AppConstants.SALES_HIERARCHY_LEVEL_SCREEN_FEILD_NAME}, getLocalizationGate()));
        }
        if ((regionCode != null)) {
            String filterCriteria = "TRIM(HIERARCHY_NODE_CODE) = '" + regionCode.toString().trim() + "'";
            if (!savingObject.isCreated()) {
                filterCriteria += " AND TRIM(ROWID_OBJECT) NOT IN ( " + savingObject.getRowId().trim() + " )";
            }

            if (objectDBValidation(AppConstants.BO_HIERARCHY_NODE, filterCriteria)) {
                return new OperationResult(new OperationExecutionError(
                        "SIP-50111",
                        new String[]{savingObject.getObjectName()},
                        getLocalizationGate()));
            }
        }

        if (!savingObject.isCreated() && savingObject.getChangedColumns().contains(AppConstants.SAL_REGION_COL_HIERARCHY_NODE_CODE)) {
            String filterCriteria = "TRIM(PARENT_NODE_ID) = '" + savingObject.getRowId().trim()
                    + "' OR TRIM(CHILD_NODE_ID) = '" + savingObject.getRowId().trim() + "'";
            if (objectDBValidation(AppConstants.BO_HIERARCHY_REL, filterCriteria)) {
                return new OperationResult(new OperationExecutionError(
                        "SIP-50109",
                        new String[]{savingObject.getObjectName()},
                        getLocalizationGate()));
            }
        }
        return OperationResult.OK;
    }

    private OperationResult validateMarket(String savingObjectName, Map<String, List<BDDObject>> markets, String excludedMarketIds) {

        for (String marketId : markets.keySet()) {
            List<BDDObject> marketsList = markets.get(marketId);
            if (marketsList == null || marketsList.size() == 0) {
                continue;
            }

            Collections.sort(marketsList, new BddDateComparator(AppConstants.SAL_REGION_COL_RELATIONSHIP_EFF_STRT_DATE));

            boolean isCurrentAssociationPresent = false;
            BDDObject previousMarket = null;
            Date previousMarketEffectiveStartDate = null;
            Date previousMarketEffectiveEndDate = null;

            for (BDDObject market : marketsList) {
                Date effectiveStartDate = null;
                Date effectiveEndDate = null;
                Object effectiveEndDateStr = market.getValue(AppConstants.SAL_REGION_COL_RELATIONSHIP_EFF_END_DATE);
                Object effectiveStartDateStr = market.getValue(AppConstants.SAL_REGION_COL_RELATIONSHIP_EFF_STRT_DATE);

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

                if (null != previousMarket && (effectiveStartDate.equals(previousMarketEffectiveEndDate) || effectiveStartDate.before(previousMarketEffectiveEndDate))) {
                    return new OperationResult(new OperationExecutionError(
                            "SIP-50105",
                            new String[]{savingObjectName},
                            getLocalizationGate()));
                }

                if (objectDBValidation(AppConstants.BO_HIERARCHY_REL,
                        getDateValidationFilterCriteria(AppUtils.getSQLStandardDateStr(effectiveStartDate),
                                AppUtils.getSQLStandardDateStr(effectiveEndDate),
                                marketId,
                                market.getValue(AppConstants.SAL_REGION_COL_HIERARCHY_TYPE_CODE).toString(),
                                market.getValue(AppConstants.SAL_REGION_COL_RELATIONSHIP_TYPE_CODE).toString(),
                                excludedMarketIds)
                )) {
                    return new OperationResult(new OperationExecutionError(
                            "SIP-50107",
                            new String[]{savingObjectName},
                            getLocalizationGate()));
                }

                //Validation completed for current channel so assign that to previous channesl for next channel validation
                previousMarket = market;
                previousMarketEffectiveStartDate = effectiveStartDate;
                previousMarketEffectiveEndDate = effectiveEndDate;
            }
        }

        return OperationResult.OK;
    }

    private OperationResult validateMarketPlanningAccounts(String savingObjectName, Map<String, List<BDDObject>> planningAccounts, String excludedPlanningAccountIds) {

        for (String planningAccountId : planningAccounts.keySet()) {
            List<BDDObject> planningAccountsList = planningAccounts.get(planningAccountId);
            if (planningAccountsList == null || planningAccountsList.size() == 0) {
                continue;
            }

            Collections.sort(planningAccountsList, new BddDateComparator(AppConstants.SAL_REGION_RELATIOSHIP_COL_EFF_STRT_DATE));

            boolean isCurrentAssociationPresent = false;
            BDDObject previousPlanningAccount = null;
            Date previousRegionEffectiveStartDate = null;
            Date previousPlanningAccountEffectiveEndDate = null;

            for (BDDObject planningAccount : planningAccountsList) {
                Date effectiveStartDate = null;
                Date effectiveEndDate = null;
                Object effectiveEndDateStr = planningAccount.getValue(AppConstants.SAL_REGION_RELATIONSHIP_COL_EFF_END_DATE);
                Object effectiveStartDateStr = planningAccount.getValue(AppConstants.SAL_REGION_RELATIOSHIP_COL_EFF_STRT_DATE);

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

                if (null != previousPlanningAccount && (effectiveStartDate.equals(previousPlanningAccountEffectiveEndDate) || effectiveStartDate.before(previousPlanningAccountEffectiveEndDate))) {
                    return new OperationResult(new OperationExecutionError(
                            "SIP-50105",
                            new String[]{savingObjectName},
                            getLocalizationGate()));
                }
                String sqlEffectiveStartDate = AppUtils.getSQLStandardDateStr(effectiveStartDate);
                String sqlEffectiveEndDate = AppUtils.getSQLStandardDateStr(effectiveEndDate);
                String filterCriteria = "((RELATIOSHIP_EFF_STRT_DATE <= TO_DATE('" + sqlEffectiveStartDate
                        + "') AND RELATIONSHIP_EFF_END_DATE >= TO_DATE('" + sqlEffectiveStartDate
                        + "')) OR (RELATIOSHIP_EFF_STRT_DATE <= TO_DATE('" + sqlEffectiveEndDate
                        + "') AND RELATIONSHIP_EFF_END_DATE >= TO_DATE('" + sqlEffectiveEndDate
                        + "')) OR (RELATIOSHIP_EFF_STRT_DATE >= TO_DATE('" + sqlEffectiveStartDate
                        + "') AND RELATIONSHIP_EFF_END_DATE <= TO_DATE('" + sqlEffectiveEndDate
                        + "'))) AND TRIM(PLANNING_ACCT_ID) = '" + planningAccountId.trim() + "'";
                if (isThereDateOverlap(AppConstants.BO_PLN_ACT_NDE_REL,
                        filterCriteria, excludedPlanningAccountIds)) {
                    return new OperationResult(new OperationExecutionError(
                            "SIP-50107",
                            new String[]{planningAccount.getObjectName()},
                            getLocalizationGate()));
                }

                //Validation completed for current channel so assign that to previous channesl for next channel validation
                previousPlanningAccount = planningAccount;
                previousRegionEffectiveStartDate = effectiveStartDate;
                previousPlanningAccountEffectiveEndDate = effectiveEndDate;
            }

        }

        return OperationResult.OK;
    }

    private OperationResult validateSalesRegionDivision(BDDObject division) throws ParseException {
        List<BDDObject> divisionList = division.getChildren(AppConstants.REGION_CHILD_SALES_DIVISION);

        String excludedDivisions = AppUtils.getExcludedObjectsId(divisionList,
                AppConstants.SAL_REGION_COL_RELATIONSHIP_EFF_STRT_DATE, AppConstants.SAL_REGION_COL_RELATIONSHIP_EFF_END_DATE);

        Collections.sort(divisionList, new BddDateComparator(AppConstants.SAL_REGION_COL_RELATIONSHIP_EFF_STRT_DATE));

        boolean isCurrentAssociationPresent = false;
        BDDObject previousDivision = null;
        Date previousDivisionEffectiveStartDate = null;
        Date previousDivisionEffectiveEndDate = null;

        for (BDDObject saleRegionDivision : divisionList) {
            //Market is marked to be deleted so no need to validate
            if (saleRegionDivision.isRemoved()) {
                continue;
            }

            Date effectiveStartDate = null;
            Date effectiveEndDate = null;
            Object effectiveEndDateStr = saleRegionDivision.getValue(AppConstants.SAL_REGION_COL_RELATIONSHIP_EFF_END_DATE);
            Object effectiveStartDateStr = saleRegionDivision.getValue(AppConstants.SAL_REGION_COL_RELATIONSHIP_EFF_STRT_DATE);

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
                        new String[]{division.getObjectName()},
                        getLocalizationGate()));
            }

            // Invalid - More than one current association for a market
            if (effectiveEndDate.equals(DEFAULT_EFFECTIVE_END_DATE)
                    && isCurrentAssociationPresent) {
                return new OperationResult(new OperationExecutionError(
                        "SIP-50104",
                        new String[]{division.getObjectName()},
                        getLocalizationGate()));
            }

            if (effectiveEndDate.equals(DEFAULT_EFFECTIVE_END_DATE)) {
                isCurrentAssociationPresent = true;
            }

            if (null != previousDivision && (effectiveStartDate.equals(previousDivisionEffectiveEndDate) || effectiveStartDate.before(previousDivisionEffectiveEndDate))) {
                return new OperationResult(new OperationExecutionError(
                        "SIP-50107",
                        new String[]{division.getObjectName()},
                        getLocalizationGate()));
            }

            //Validation completed for current channel so assign that to previous channesl for next channel validation
            previousDivision = saleRegionDivision;
            previousDivisionEffectiveStartDate = effectiveStartDate;
            previousDivisionEffectiveEndDate = effectiveEndDate;
        }
        return OperationResult.OK;
    }

    private String getDateValidationFilterCriteria(String effectiveStartDate, String effectiveEndDate, String childNodeId, String hierarchyTypeCode, String relationshipTypeCode, String excludedIds) {
        String filterCriteria = "HIERARCHY_TYPE_CODE = '" + hierarchyTypeCode
                + "' AND RELATIONSHIP_TYPE_CODE = '" + relationshipTypeCode
                + "' AND ((RELATIONSHIP_EFF_STRT_DATE <= TO_DATE('" + effectiveStartDate
                + "') AND RELATIONSHIP_EFF_END_DATE >= TO_DATE('" + effectiveStartDate
                + "')) OR (RELATIONSHIP_EFF_STRT_DATE <= TO_DATE('" + effectiveEndDate
                + "') AND RELATIONSHIP_EFF_END_DATE >= TO_DATE('" + effectiveEndDate
                + "')) OR (RELATIONSHIP_EFF_STRT_DATE >= TO_DATE('" + effectiveStartDate
                + "') AND RELATIONSHIP_EFF_END_DATE <= TO_DATE('" + effectiveEndDate
                + "'))) AND TRIM(CHILD_NODE_ID) = '" + childNodeId.trim() + "'";

        if (null != excludedIds && excludedIds.length() > 0) {
            filterCriteria += " AND TRIM(ROWID_OBJECT) NOT IN ( " + excludedIds + " )";
        }
        return filterCriteria;
    }

    private boolean objectDBValidation(String baseObjectName, String filterCriteria) {

        SearchQueryRequest searchRequest = new SearchQueryRequest();
        searchRequest.setReturnTotal(true);
        searchRequest.setSiperianObjectUid(SiperianObjectType.BASE_OBJECT.makeUid(baseObjectName));
        searchRequest.setFilterCriteria(filterCriteria);
        prepareRequest(searchRequest);
        SiperianResponse response = processRequest(searchRequest);

        if (null != response) {
            SearchQueryResponse entryPointResponse = (SearchQueryResponse) response;

            if (entryPointResponse.getRecordCount() > 0) {
                return true;
            }
        }

        return false;
    }

    private boolean isThereDateOverlap(String baseObjectName, String filterCriteria, String excludedIds) {

        if (null != excludedIds && excludedIds.length() > 0) {
            filterCriteria += " AND TRIM(ROWID_OBJECT) NOT IN ( " + excludedIds + " )";
        }

        SearchQueryRequest request = new SearchQueryRequest();
        request.setReturnTotal(true);
        request.setSiperianObjectUid(baseObjectName);
        request.setFilterCriteria(filterCriteria);
        prepareRequest(request);
        SiperianResponse response = processRequest(request);

        if (null != response) {
            SearchQueryResponse entryPointResponse = (SearchQueryResponse) response;

            if (entryPointResponse.getRecordCount() > 0) {
                return true;
            }
        }

        return false;
    }
}