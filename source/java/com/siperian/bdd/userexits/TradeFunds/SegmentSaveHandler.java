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
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author owais.siraj
 */
public class SegmentSaveHandler extends AbstractBaseOperationPlugin implements ISaveOperationPlugin {

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
            Map<String, List<BDDObject>> divisions = new HashMap<String, List<BDDObject>>();
            StringBuilder excludedDivisions = new StringBuilder();
            Map<String, List<BDDObject>> regions = new HashMap<String, List<BDDObject>>();
            StringBuilder excludedRegions = new StringBuilder();

            List<BDDObject> childDivisions = savingObject.getChildren(AppConstants.SAL_SEG_CHILD_SALES_DIVISION);

            AppUtils.splitParentWithChildSimilarObjects(childDivisions, divisions, excludedDivisions,
                    regions, excludedRegions, AppConstants.SAL_SEG_CHILD_SALES_REGION, AppConstants.SAL_SEG_COL_CHILD_NODE_ID,
                    AppConstants.SAL_SEG_COL_RELATIONSHIP_EFF_STRT_DATE, AppConstants.SAL_SEG_COL_RELATIONSHIP_EFF_END_DATE, AppConstants.SAL_SEG_COL_CHILD_NODE_ID,
                    AppConstants.SAL_SEG_COL_RELATIONSHIP_EFF_STRT_DATE, AppConstants.SAL_SEG_COL_RELATIONSHIP_EFF_END_DATE);

            OperationResult operationResult = validateSalesSegment(savingObject);
            if (operationResult != OperationResult.OK) {
                return operationResult;
            }
            operationResult = validateSalesSegmentSBU(savingObject);
            if (operationResult != OperationResult.OK) {
                return operationResult;
            }
            operationResult = validateDivision(savingObject.getObjectName(), divisions, (excludedDivisions.length() == 0 ? null : excludedDivisions.toString()));
            if (operationResult != OperationResult.OK) {
                return operationResult;
            }

            operationResult = validateDivisionRegions(savingObject.getObjectName(), regions, (excludedRegions.length() == 0 ? null : excludedRegions.toString()));
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

    private OperationResult validateSalesSegment(BDDObject savingObject) {
        Object salSegmentCode = savingObject.getValue(AppConstants.SAL_SEG_COL_HIERARCHY_NODE_CODE);
        Object salSegmentName = savingObject.getValue(AppConstants.SAL_SEG_COL_HIERARCHY_NODE_NAME);
        if (salSegmentCode != null && salSegmentCode.toString().trim().isEmpty()) {
            return new OperationResult(new OperationExecutionError("SIP-50112", new String[]{AppConstants.SALES_HIERARCHY_LEVEL_SCREEN_FEILD_CODE}, getLocalizationGate()));
        }
        if (salSegmentName == null || salSegmentName.toString().trim().isEmpty()) {
            return new OperationResult(new OperationExecutionError("SIP-50112", new String[]{AppConstants.SALES_HIERARCHY_LEVEL_SCREEN_FEILD_NAME}, getLocalizationGate()));
        }
        if (salSegmentCode != null) {
            String filterCriteria = "TRIM(HIERARCHY_NODE_CODE) = '" + salSegmentCode.toString().trim() + "'";
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
        if (!savingObject.isCreated() && savingObject.getChangedColumns().contains(AppConstants.SAL_SEG_COL_HIERARCHY_NODE_CODE)) {
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

    private OperationResult validateDivision(String savingObjectName, Map<String, List<BDDObject>> divisions, String excludedDivisionIds) {

        for (String divisionId : divisions.keySet()) {
            List<BDDObject> divisionsList = divisions.get(divisionId);
            if (divisionsList == null || divisionsList.size() == 0) {
                continue;
            }

            Collections.sort(divisionsList, new BddDateComparator(AppConstants.SAL_SEG_COL_RELATIONSHIP_EFF_STRT_DATE));

            boolean isCurrentAssociationPresent = false;
            BDDObject previousDivision = null;
            Date previousDivisionEffectiveStartDate = null;
            Date previousDivisionEffectiveEndDate = null;

            for (BDDObject division : divisionsList) {
                Date effectiveStartDate = null;
                Date effectiveEndDate = null;
                Object effectiveEndDateStr = division.getValue(AppConstants.SAL_SEG_COL_RELATIONSHIP_EFF_END_DATE);
                Object effectiveStartDateStr = division.getValue(AppConstants.SAL_SEG_COL_RELATIONSHIP_EFF_STRT_DATE);

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

                if (null != previousDivision && (effectiveStartDate.equals(previousDivisionEffectiveEndDate) || effectiveStartDate.before(previousDivisionEffectiveEndDate))) {
                    return new OperationResult(new OperationExecutionError(
                            "SIP-50105",
                            new String[]{savingObjectName},
                            getLocalizationGate()));
                }

                if (objectDBValidation(AppConstants.BO_HIERARCHY_REL,
                        getDateValidationFilterCriteria(AppUtils.getSQLStandardDateStr(effectiveStartDate),
                                AppUtils.getSQLStandardDateStr(effectiveEndDate),
                                divisionId,
                                division.getValue(AppConstants.SAL_SEG_COL_HIERARCHY_TYPE_CODE).toString(),
                                division.getValue(AppConstants.SAL_SEG_COL_RELATIONSHIP_TYPE_CODE).toString(),
                                excludedDivisionIds)
                )) {
                    return new OperationResult(new OperationExecutionError(
                            "SIP-50107",
                            new String[]{savingObjectName},
                            getLocalizationGate()));
                }

                //Validation completed for current channel so assign that to previous channesl for next channel validation
                previousDivision = division;
                previousDivisionEffectiveStartDate = effectiveStartDate;
                previousDivisionEffectiveEndDate = effectiveEndDate;
            }
        }

        return OperationResult.OK;
    }

    private OperationResult validateDivisionRegions(String savingObjectName, Map<String, List<BDDObject>> regions, String excludedChannelIds) {

        for (String regionId : regions.keySet()) {
            List<BDDObject> regionsList = regions.get(regionId);
            if (regionsList == null || regionsList.size() == 0) {
                continue;
            }

            Collections.sort(regionsList, new BddDateComparator(AppConstants.SAL_SEG_COL_RELATIONSHIP_EFF_STRT_DATE));

            boolean isCurrentAssociationPresent = false;
            BDDObject previousRegion = null;
            Date previousRegionEffectiveStartDate = null;
            Date previousRegionEffectiveEndDate = null;

            for (BDDObject region : regionsList) {
                Date effectiveStartDate = null;
                Date effectiveEndDate = null;
                Object effectiveEndDateStr = region.getValue(AppConstants.SAL_SEG_COL_RELATIONSHIP_EFF_END_DATE);
                Object effectiveStartDateStr = region.getValue(AppConstants.SAL_SEG_COL_RELATIONSHIP_EFF_STRT_DATE);

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
                                region.getValue(AppConstants.SAL_SEG_COL_HIERARCHY_TYPE_CODE).toString(),
                                region.getValue(AppConstants.SAL_SEG_COL_RELATIONSHIP_TYPE_CODE).toString(),
                                excludedChannelIds)
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

    private OperationResult validateSalesSegmentSBU(BDDObject sbu) throws ParseException {
        List<BDDObject> sbuList = sbu.getChildren(AppConstants.SAL_SEG_CHILD_SALES_BUSINESS_UNIT);

        String excludedSBUs = AppUtils.getExcludedObjectsId(sbuList,
                AppConstants.SAL_SEG_COL_RELATIONSHIP_EFF_STRT_DATE, AppConstants.SAL_SEG_COL_RELATIONSHIP_EFF_END_DATE);

        Collections.sort(sbuList, new BddDateComparator(AppConstants.SAL_SEG_COL_RELATIONSHIP_EFF_STRT_DATE));

        boolean isCurrentAssociationPresent = false;
        BDDObject previousSBU = null;
        Date previousSBUEffectiveStartDate = null;
        Date previousSBUEffectiveEndDate = null;

        for (BDDObject salesSegmentSBU : sbuList) {
            //Market is marked to be deleted so no need to validate
            if (salesSegmentSBU.isRemoved()) {
                continue;
            }

            Date effectiveStartDate = null;
            Date effectiveEndDate = null;
            Object effectiveEndDateStr = salesSegmentSBU.getValue(AppConstants.SAL_SEG_COL_RELATIONSHIP_EFF_END_DATE);
            Object effectiveStartDateStr = salesSegmentSBU.getValue(AppConstants.SAL_SEG_COL_RELATIONSHIP_EFF_STRT_DATE);

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
                        new String[]{sbu.getObjectName()},
                        getLocalizationGate()));
            }

            // Invalid - More than one current association for a market
            if (effectiveEndDate.equals(DEFAULT_EFFECTIVE_END_DATE)
                    && isCurrentAssociationPresent) {
                return new OperationResult(new OperationExecutionError(
                        "SIP-50104",
                        new String[]{sbu.getObjectName()},
                        getLocalizationGate()));
            }

            if (effectiveEndDate.equals(DEFAULT_EFFECTIVE_END_DATE)) {
                isCurrentAssociationPresent = true;
            }

            if (null != previousSBU && (effectiveStartDate.equals(previousSBUEffectiveEndDate) || effectiveStartDate.before(previousSBUEffectiveEndDate))) {
                return new OperationResult(new OperationExecutionError(
                        "SIP-50107",
                        new String[]{sbu.getObjectName()},
                        getLocalizationGate()));
            }

            //Validation completed for current channel so assign that to previous channesl for next channel validation
            previousSBU = salesSegmentSBU;
            previousSBUEffectiveStartDate = effectiveStartDate;
            previousSBUEffectiveEndDate = effectiveEndDate;
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