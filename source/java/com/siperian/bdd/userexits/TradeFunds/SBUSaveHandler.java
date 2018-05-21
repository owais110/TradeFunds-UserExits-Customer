package com.siperian.bdd.userexits.TradeFunds;

import java.text.ParseException;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.siperian.bdd.userexits.datamodel.BDDObject;
import com.siperian.bdd.userexits.operations.ISaveOperationPlugin;
import com.siperian.bdd.userexits.operations.OperationExecutionError;
import com.siperian.bdd.userexits.operations.OperationResult;
import com.siperian.bdd.userexits.operations.OperationType;
import com.siperian.sif.message.SiperianResponse;
import com.siperian.sif.message.mrm.SearchQueryRequest;
import com.siperian.sif.message.mrm.SearchQueryResponse;

import static com.siperian.bdd.userexits.TradeFunds.AppConstants.DEFAULT_EFFECTIVE_END_DATE;
import com.siperian.bdd.userexits.operations.AbstractBaseOperationPlugin;
import com.siperian.sif.message.Field;
import com.siperian.sif.message.Record;
import com.siperian.sif.message.RecordKey;
import com.siperian.sif.message.SiperianObjectType;
import com.siperian.sif.message.mrm.PutRequest;
import com.siperian.sif.message.mrm.PutResponse;
import java.util.Arrays;

public class SBUSaveHandler extends AbstractBaseOperationPlugin implements ISaveOperationPlugin {

    @Override
    public OperationType getOperationType() {
        return OperationType.SAVE_OPERATION;
    }

    @Override
    public OperationResult afterEverything(BDDObject savedObject) {
        return new OperationResult(new OperationExecutionError("SIP-50101", new String[]{savedObject.getObjectName()}, getLocalizationGate()));

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
        Map<String, List<BDDObject>> channels = new HashMap<String, List<BDDObject>>();
        StringBuilder excludedChannels = new StringBuilder();
        Map<String, List<BDDObject>> divisions = new HashMap<String, List<BDDObject>>();
        StringBuilder excludedDivisions = new StringBuilder();

        List<BDDObject> childChannels = savingObject.getChildren(AppConstants.SBU_CHILD_CHANNEL);

        AppUtils.splitParentWithChildSimilarObjects(childChannels, channels, excludedChannels, divisions, excludedDivisions,
                AppConstants.SBU_CHILD_CHANNEL_DIVISION, AppConstants.SBU_COL_CHILD_NODE_ID, AppConstants.SBU_COL_RELATIONSHIP_EFF_END_DATE, AppConstants.SBU_COL_RELATIONSHIP_EFF_STRT_DATE,
                AppConstants.SBU_COL_CHILD_NODE_ID, AppConstants.SBU_COL_RELATIONSHIP_EFF_END_DATE, AppConstants.SBU_COL_RELATIONSHIP_EFF_STRT_DATE);
        OperationResult operationResult = validateSbu(savingObject, childChannels, 0);
        if (operationResult != OperationResult.OK) {
            return operationResult;
        }

        operationResult = validateSbaChannels(savingObject.getObjectName(), channels, excludedChannels.toString());
        if (operationResult != OperationResult.OK) {
            return operationResult;
        }

        operationResult = validateSbaChannelDivision(savingObject.getObjectName(), divisions, excludedDivisions.toString());

        return operationResult;
    }

    private OperationResult validateSbu(BDDObject savingObject, List<BDDObject> childAllChannels, int numOfChannels) {
        Object sbuCode = savingObject.getValue(AppConstants.SBU_COL_HIERARCHY_NODE_CODE);
        Object sbuName = savingObject.getValue(AppConstants.SBU_COL_HIERARCHY_NODE_NAME);
        if (sbuCode != null && sbuCode.toString().trim().isEmpty()) {
            return new OperationResult(new OperationExecutionError("SIP-50112", new String[]{AppConstants.SALES_HIERARCHY_LEVEL_SCREEN_FEILD_CODE}, getLocalizationGate()));
        }
        if (sbuName == null || sbuName.toString().trim().isEmpty()) {
            return new OperationResult(new OperationExecutionError("SIP-50112", new String[]{AppConstants.SALES_HIERARCHY_LEVEL_SCREEN_FEILD_NAME}, getLocalizationGate()));
        }
        if ((sbuCode != null)) {
            String filterCriteria = "TRIM(HIERARCHY_NODE_CODE) = '" + sbuCode.toString().trim() + "'";
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
        if (numOfChannels == 0 && childAllChannels.size() > 0) {
            for (BDDObject childAllChannel : childAllChannels) {
                if (!childAllChannel.isCreated()) {
                    numOfChannels++;
                }
            }
        }
        List<String> changedCols = savingObject.getChangedColumns();
        if (!savingObject.isCreated() && changedCols.size() > 0 && changedCols.contains(AppConstants.SBU_COL_HIERARCHY_NODE_CODE) && numOfChannels > 0) {
            return new OperationResult(new OperationExecutionError(
                    "SIP-50109",
                    new String[]{savingObject.getObjectName()},
                    getLocalizationGate()));
        }

        return OperationResult.OK;
    }

    private OperationResult validateSbaChannels(String savingObjectName, Map<String, List<BDDObject>> channels, String excludedChannelIds) {

        for (String channelId : channels.keySet()) {
            List<BDDObject> channelsList = channels.get(channelId);
            if (channelsList == null || channelsList.size() == 0) {
                continue;
            }

            Collections.sort(channelsList, new BddDateComparator(AppConstants.SBU_COL_RELATIONSHIP_EFF_STRT_DATE));

            boolean isCurrentAssociationPresent = false;
            BDDObject previousChannel = null;
            Date previousChannelEffectiveStartDate = null;
            Date previousChannelEffectiveEndDate = null;

            for (BDDObject channel : channelsList) {
                Date effectiveStartDate = null;
                Date effectiveEndDate = null;
                Object effectiveEndDateStr = channel.getValue(AppConstants.SBU_COL_RELATIONSHIP_EFF_END_DATE);
                Object effectiveStartDateStr = channel.getValue(AppConstants.SBU_COL_RELATIONSHIP_EFF_STRT_DATE);

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

                if (null != previousChannel && (effectiveStartDate.equals(previousChannelEffectiveEndDate) || effectiveStartDate.before(previousChannelEffectiveEndDate))) {
                    return new OperationResult(new OperationExecutionError(
                            "SIP-50105",
                            new String[]{savingObjectName},
                            getLocalizationGate()));
                }
                if (objectDBValidation(AppConstants.BO_HIERARCHY_REL,
                        getDateValidationFilterCriteria(AppUtils.getSQLStandardDateStr(effectiveStartDate),
                                AppUtils.getSQLStandardDateStr(effectiveEndDate),
                                channelId,
                                channel.getValue(AppConstants.SBU_COL_HIERARCHY_TYPE_CODE).toString(),
                                channel.getValue(AppConstants.SBU_COL_RELATIONSHIP_TYPE_CODE).toString(),
                                excludedChannelIds)
                )) {
                    return new OperationResult(new OperationExecutionError(
                            "SIP-50107",
                            new String[]{savingObjectName},
                            getLocalizationGate()));
                }

                //Validation completed for current channel so assign that to previous channesl for next channel validation
                previousChannel = channel;
                previousChannelEffectiveStartDate = effectiveStartDate;
                previousChannelEffectiveEndDate = effectiveEndDate;
            }

        }

        return OperationResult.OK;
    }

    private OperationResult validateSbaChannelDivision(String savingObjectName, Map<String, List<BDDObject>> divisions, String excludedDivisionIds) {

        for (String divisionId : divisions.keySet()) {
            List<BDDObject> divisionsList = divisions.get(divisionId);
            if (divisionsList == null || divisionsList.size() == 0) {
                continue;
            }

            Collections.sort(divisionsList, new BddDateComparator(AppConstants.SBU_COL_RELATIONSHIP_EFF_STRT_DATE));

            boolean isCurrentAssociationPresent = false;
            BDDObject previousDivision = null;
            Date previousDivisionEffectiveStartDate = null;
            Date previousDivisionEffectiveEndDate = null;

            for (BDDObject division : divisionsList) {
                Date effectiveStartDate = null;
                Date effectiveEndDate = null;
                Object effectiveEndDateStr = division.getValue(AppConstants.SBU_COL_RELATIONSHIP_EFF_END_DATE);
                Object effectiveStartDateStr = division.getValue(AppConstants.SBU_COL_RELATIONSHIP_EFF_STRT_DATE);

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
                                division.getValue(AppConstants.SBU_COL_HIERARCHY_TYPE_CODE).toString(),
                                division.getValue(AppConstants.SBU_COL_RELATIONSHIP_TYPE_CODE).toString(),
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