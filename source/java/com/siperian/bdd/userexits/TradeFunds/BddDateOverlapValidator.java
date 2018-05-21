package com.siperian.bdd.userexits.TradeFunds;

import com.siperian.bdd.userexits.operations.AbstractBaseOperationPlugin;

import com.siperian.bdd.userexits.operations.OperationType;
import com.siperian.sif.message.SiperianResponse;
import com.siperian.sif.message.mrm.SearchQueryRequest;
import com.siperian.sif.message.mrm.SearchQueryResponse;

public class BddDateOverlapValidator extends AbstractBaseOperationPlugin {

    private String hierarchyTypeCode;
    private String relationshipTypeCode;
    private String excludedIds;

    public BddDateOverlapValidator(String herarchyTypeCode, String relationshipTypeCode, String excludidIds) {
        this.hierarchyTypeCode = herarchyTypeCode;
        this.relationshipTypeCode = relationshipTypeCode;
        this.excludedIds = excludidIds;
    }

    @Override
    public OperationType getOperationType() {
        return OperationType.SAVE_OPERATION;
    }

    public boolean isThereDateOverlap(String effectiveStartDate, String childNodeId) {

        String filterCriteria = "HIERARCHY_TYPE_CODE = '" + this.hierarchyTypeCode
                + "' AND RELATIONSHIP_TYPE_CODE = '" + this.relationshipTypeCode
                + "' AND RELATIONSHIP_EFF_END_DATE < '" + effectiveStartDate
                + "' AND TRIM(CHILD_NODE_ID) = '" + childNodeId + "'";

        if (null != excludedIds) {
            filterCriteria += " AND TRIM(ROWID_OBJECT) NOT IN ( " + excludedIds + " )";
        }

        SearchQueryRequest entryPointRequest = new SearchQueryRequest();
        entryPointRequest.setReturnTotal(true);
        entryPointRequest.setSiperianObjectUid("BASE_OBJECT.C_MSB_BO_HIERARCHY_REL");
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