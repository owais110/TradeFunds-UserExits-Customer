package com.siperian.bdd.userexits.TradeFunds;

import java.text.ParseException;
import java.util.Date;
import java.util.List;

import com.siperian.bdd.userexits.datamodel.BDDObject;
import com.siperian.bdd.userexits.operations.ISaveOperationPlugin;
import com.siperian.bdd.userexits.operations.OperationExecutionError;
import com.siperian.bdd.userexits.operations.OperationResult;
import com.siperian.bdd.userexits.operations.OperationType;
import com.siperian.sif.message.SiperianResponse;
import com.siperian.sif.message.mrm.SearchQueryRequest;
import com.siperian.sif.message.mrm.SearchQueryResponse;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static com.siperian.bdd.userexits.TradeFunds.AppConstants.DEFAULT_EFFECTIVE_END_DATE;
import com.siperian.bdd.userexits.operations.AbstractBaseOperationPlugin;
import com.siperian.sif.message.Field;
import com.siperian.sif.message.Record;
import com.siperian.sif.message.RecordKey;
import com.siperian.sif.message.SiperianObjectType;
import com.siperian.sif.message.mrm.PutRequest;
import com.siperian.sif.message.mrm.PutResponse;
import java.util.Arrays;

public class DivisionSaveHandler extends AbstractBaseOperationPlugin implements ISaveOperationPlugin {

    @Override
    public OperationType getOperationType() {

        return OperationType.SAVE_OPERATION;
    }

    @Override
    public OperationResult afterEverything(BDDObject arg0) {

        return new OperationResult(new OperationExecutionError("SIP-50101", new String[]{arg0.getObjectName()}, getLocalizationGate()));
    }

    @Override
    public OperationResult afterSave(BDDObject savedObject) {
        return OperationResult.OK;
    }

    @Override
    public OperationResult beforeSave(BDDObject arg0) {

        return OperationResult.OK;
    }

    @Override
    public OperationResult beforeEverything(BDDObject savingObject) {
        Map<String, List<BDDObject>> regions = new HashMap<String, List<BDDObject>>();
        StringBuilder excludedRegions = new StringBuilder();
        Map<String, List<BDDObject>> markets = new HashMap<String, List<BDDObject>>();
        StringBuilder excludedMarkets = new StringBuilder();
        List<BDDObject> childRegions = savingObject.getChildren(AppConstants.DIV_CHILD_REGION);
        AppUtils.splitParentWithChildSimilarObjects(childRegions, regions, excludedRegions, markets, excludedMarkets,
                AppConstants.DIV_CHILD_REGION_MARKET, AppConstants.DIV_COL_CHILD_NODE_ID, AppConstants.DIV_COL_RELATIONSHIP_EFF_END_DATE, AppConstants.DIV_COL_RELATIONSHIP_EFF_STRT_DATE,
                AppConstants.DIV_COL_CHILD_NODE_ID, AppConstants.DIV_COL_RELATIONSHIP_EFF_END_DATE, AppConstants.DIV_COL_RELATIONSHIP_EFF_STRT_DATE);
        
        OperationResult operationResult = validateDivision(savingObject, childRegions, 0);
        if (operationResult != OperationResult.OK) {
            return operationResult;
        }
        operationResult = validateSalesSegment(savingObject);
        if (operationResult != OperationResult.OK) {
            return operationResult;
        }
        operationResult = validateDivRegions(savingObject.getObjectName(), regions, excludedRegions.toString());
        if (operationResult != OperationResult.OK) {
            return operationResult;
        }

        operationResult = validateDivRegionMarkets(savingObject.getObjectName(), markets, excludedMarkets.toString());

        return operationResult;
    }

    private OperationResult validateDivision(BDDObject savingObject, List<BDDObject> childAllRegions, int numOfRegions) {
        Object divisionCode = savingObject.getValue(AppConstants.DIV_COL_HIERARCHY_NODE_CODE);
        Object divisionName = savingObject.getValue(AppConstants.DIV_COL_HIERARCHY_NODE_NAME);
        if (divisionCode != null && divisionCode.toString().trim().isEmpty()) {
            return new OperationResult(new OperationExecutionError("SIP-50112", new String[]{AppConstants.SALES_HIERARCHY_LEVEL_SCREEN_FEILD_CODE}, getLocalizationGate()));
        }
        if (divisionName == null || divisionName.toString().trim().isEmpty()) {
            return new OperationResult(new OperationExecutionError("SIP-50112", new String[]{AppConstants.SALES_HIERARCHY_LEVEL_SCREEN_FEILD_NAME}, getLocalizationGate()));
        }

        if ((divisionCode != null)) {
            String filterCriteria = "TRIM(HIERARCHY_NODE_CODE) = '" + divisionCode.toString().trim() + "'";
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
        if (!savingObject.isCreated() && savingObject.getChangedColumns().contains(AppConstants.DIV_COL_HIERARCHY_NODE_CODE)) {
            String filterCriteria = "TRIM(PARENT_NODE_ID) = '" + savingObject.getRowId().trim()
                    + "' OR TRIM(CHILD_NODE_ID) = '" + savingObject.getRowId().trim() + "'";
            if (objectDBValidation(AppConstants.BO_HIERARCHY_REL, filterCriteria)) {

                return new OperationResult(new OperationExecutionError(
                        "SIP-50109",
                        new String[]{savingObject.getObjectName()},
                        getLocalizationGate()));
            }
        }
        if (numOfRegions == 0 && childAllRegions.size() > 0) {
            for (BDDObject childAllChannel : childAllRegions) {
                if (!childAllChannel.isCreated()) {
                    numOfRegions++;
                }
            }
        }
        List<String> changedCols = savingObject.getChangedColumns();
        if (!savingObject.isCreated() && changedCols.size() > 0 && changedCols.contains(AppConstants.DIV_COL_HIERARCHY_NODE_CODE) && numOfRegions > 0) {
            return new OperationResult(new OperationExecutionError(
                    "SIP-50109",
                    new String[]{savingObject.getObjectName()},
                    getLocalizationGate()));
        }

        return OperationResult.OK;
    }

    private OperationResult validateDivRegions(String savingObjectName, Map<String, List<BDDObject>> regions, String excludedRegionIds) {

        for (String regionId : regions.keySet()) {
            List<BDDObject> regionsList = regions.get(regionId);
            if (regionsList == null || regionsList.size() == 0) {
                continue;
            }

            Collections.sort(regionsList, new BddDateComparator(AppConstants.DIV_COL_RELATIONSHIP_EFF_STRT_DATE));

            boolean isCurrentAssociationPresent = false;
            BDDObject previousRegion = null;
            Date previousRegionEffectiveStartDate = null;
            Date previousRegionEffectiveEndDate = null;

            for (BDDObject region : regionsList) {
                Date effectiveStartDate = null;
                Date effectiveEndDate = null;
                Object effectiveEndDateStr = region.getValue(AppConstants.DIV_COL_RELATIONSHIP_EFF_END_DATE);
                Object effectiveStartDateStr = region.getValue(AppConstants.DIV_COL_RELATIONSHIP_EFF_STRT_DATE);

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
                if (objectDBValidation(AppConstants.BO_HIERARCHY_REL,
                        getDateValidationFilterCriteria(AppUtils.getSQLStandardDateStr(effectiveStartDate),
                                AppUtils.getSQLStandardDateStr(effectiveEndDate),
                                regionId,
                                region.getValue(AppConstants.DIV_COL_HIERARCHY_TYPE_CODE).toString(),
                                region.getValue(AppConstants.DIV_COL_RELATIONSHIP_TYPE_CODE).toString(),
                                excludedRegionIds)
                )) {

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

    private OperationResult validateDivRegionMarkets(String savingObjectName, Map<String, List<BDDObject>> markets, String excludedMarketIds) {

        for (String marketId : markets.keySet()) {
            List<BDDObject> marketsList = markets.get(marketId);
            if (marketsList == null || marketsList.size() == 0) {
                continue;
            }

            Collections.sort(marketsList, new BddDateComparator(AppConstants.DIV_COL_RELATIONSHIP_EFF_STRT_DATE));

            boolean isCurrentAssociationPresent = false;
            BDDObject previousMarket = null;
            Date previousMarketEffectiveStartDate = null;
            Date previousMarketEffectiveEndDate = null;

            for (BDDObject market : marketsList) {
                Date effectiveStartDate = null;
                Date effectiveEndDate = null;
                Object effectiveEndDateStr = market.getValue(AppConstants.DIV_COL_RELATIONSHIP_EFF_END_DATE);
                Object effectiveStartDateStr = market.getValue(AppConstants.DIV_COL_RELATIONSHIP_EFF_STRT_DATE);

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
                                market.getValue(AppConstants.DIV_COL_HIERARCHY_TYPE_CODE).toString(),
                                market.getValue(AppConstants.DIV_COL_RELATIONSHIP_TYPE_CODE).toString(),
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

    private OperationResult validateSalesSegment(BDDObject salesSegment) {
        List<BDDObject> salesSegmentList = salesSegment.getChildren(AppConstants.DIV_CHILD_SALES_SEGMENT);

        String excludedSalesSegments = AppUtils.getExcludedObjectsId(salesSegmentList,
                AppConstants.DIV_COL_RELATIONSHIP_EFF_STRT_DATE, AppConstants.DIV_COL_RELATIONSHIP_EFF_END_DATE);

        Collections.sort(salesSegmentList, new BddDateComparator(AppConstants.DIV_COL_RELATIONSHIP_EFF_STRT_DATE));

        boolean isCurrentAssociationPresent = false;
        BDDObject previousSalesSegment = null;
        Date previousSalesSegmentEffectiveStartDate = null;
        Date previousSalesSegmentEffectiveEndDate = null;

        for (BDDObject salesSegments : salesSegmentList) {
            if (salesSegments.isRemoved()) {
                continue;
            }

            Date effectiveStartDate = null;
            Date effectiveEndDate = null;
            Object effectiveEndDateStr = salesSegments.getValue(AppConstants.DIV_COL_RELATIONSHIP_EFF_END_DATE);
            Object effectiveStartDateStr = salesSegments.getValue(AppConstants.DIV_COL_RELATIONSHIP_EFF_STRT_DATE);

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
                        new String[]{salesSegment.getObjectName()},
                        getLocalizationGate()));
            }

            // Invalid - More than one current association for a market
            if (effectiveEndDate.equals(DEFAULT_EFFECTIVE_END_DATE)
                    && isCurrentAssociationPresent) {
                return new OperationResult(new OperationExecutionError(
                        "SIP-50104",
                        new String[]{salesSegment.getObjectName()},
                        getLocalizationGate()));
            }

            if (effectiveEndDate.equals(DEFAULT_EFFECTIVE_END_DATE)) {
                isCurrentAssociationPresent = true;
            }

            if (null != previousSalesSegment && (effectiveStartDate.equals(previousSalesSegmentEffectiveEndDate) || effectiveStartDate.before(previousSalesSegmentEffectiveEndDate))) {
                return new OperationResult(new OperationExecutionError(
                        "SIP-50107",
                        new String[]{salesSegment.getObjectName()},
                        getLocalizationGate()));
            }

            //Validation completed for current channel so assign that to previous channesl for next channel validation
            previousSalesSegment = salesSegments;
            previousSalesSegmentEffectiveStartDate = effectiveStartDate;
            previousSalesSegmentEffectiveEndDate = effectiveEndDate;
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
}