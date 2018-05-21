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
import com.siperian.sif.message.SiperianResponse;
import com.siperian.sif.message.mrm.SearchQueryRequest;
import com.siperian.sif.message.mrm.SearchQueryResponse;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class PlanningAccountSaveHandler extends AbstractBaseOperationPlugin implements ISaveOperationPlugin {

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
            OperationResult operationResult = validatePlanningAccount(savingObject);
            if (!operationResult.equals(OperationResult.OK)) {
                return operationResult;
            }

            operationResult = validatePlanningAccountBanner(savingObject);
            if (!operationResult.equals(OperationResult.OK)) {
                return operationResult;
            }

            operationResult = validatePlanningAccountMarket(savingObject);
            if (!operationResult.equals(OperationResult.OK)) {
                return operationResult;
            }

            operationResult = validateConsumptionBaseLineArea(savingObject);
            if (!operationResult.equals(OperationResult.OK)) {
                return operationResult;
            }

            operationResult = validateSubPlanningAccount(savingObject);
            if (!operationResult.equals(OperationResult.OK)) {
                return operationResult;
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return OperationResult.OK;
    }

    private OperationResult validatePlanningAccount(BDDObject planningAccount)
            throws ParseException {
        Object planningAccountName = planningAccount.getValue(AppConstants.PLANNING_ACCOUNT_NAME);
        Object jmsSalesOrgCode = planningAccount.getValue(AppConstants.JMS_SALES_ORG_CODE);
        Object effectiveEndDateStr = planningAccount.getValue(AppConstants.PLN_ACCT_END_DATE);
        Object effectiveStartDateStr = planningAccount.getValue(AppConstants.PLN_ACCT_EFF_DATE);
        String oldJmsSalesOrgCode = (String) (planningAccount.getOldValue(AppConstants.JMS_SALES_ORG_CODE) == null ? "" : planningAccount.getOldValue(AppConstants.JMS_SALES_ORG_CODE));
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
        }
        //Validate JMS Sales Organization Code
        if (!oldJmsSalesOrgCode.equals("")) {
            if (jmsSalesOrgCode != null && !oldJmsSalesOrgCode.equals(jmsSalesOrgCode)) {
                return new OperationResult(new OperationExecutionError("SIP-50115", new String[]{planningAccount.getObjectName()}, getLocalizationGate()));
            }
        }
        //Planning Account Name is empty or Spaces
        if (planningAccountName == null || planningAccountName.toString().trim().isEmpty()) {
            return new OperationResult(new OperationExecutionError("SIP-50108", new String[0], getLocalizationGate()));
        }
        // Invalid – start date is later than end date
        if (effectiveEndDate.before(effectiveStartDate)) {

            return new OperationResult(new OperationExecutionError("SIP-50103",
                    new String[]{planningAccount.getObjectName()},
                    getLocalizationGate()));
        }

        return OperationResult.OK;
    }

    private OperationResult validatePlanningAccountBanner(BDDObject planningAccount) throws ParseException {

        Map<String, List<BDDObject>> planningAccountBanners = new HashMap<String, List<BDDObject>>();
        StringBuilder excludedBanners = new StringBuilder();

        AppUtils.splitSimilarObjects(planningAccount.getChildren(AppConstants.PLN_ACCT_CHILD_BANNER), planningAccountBanners, excludedBanners,
                AppConstants.PLN_ACCT_BANNER_ID, AppConstants.PLN_ACCT_BANNER_EFFECTIVE_END_DATE, AppConstants.PLN_ACCT_BANNER_EFFECTIVE_START_DATE);

        for (String bannerId : planningAccountBanners.keySet()) {
            List<BDDObject> bannerList = planningAccountBanners.get(bannerId);
            if (bannerList == null || bannerList.size() == 0) {
                continue;
            }

            Collections.sort(bannerList, new BddDateComparator(AppConstants.PLN_ACCT_BANNER_EFFECTIVE_START_DATE));

            boolean isCurrentAssociationPresent = false;
            BDDObject previousBanner = null;
            Date previousBannerEffectiveStartDate = null;
            Date previousBannerEffectiveEndDate = null;

            for (BDDObject planningAccountBanner : bannerList) {
                Date effectiveStartDate = null;
                Date effectiveEndDate = null;
                Object effectiveEndDateStr = planningAccountBanner.getValue(AppConstants.PLN_ACCT_BANNER_EFFECTIVE_END_DATE);
                Object effectiveStartDateStr = planningAccountBanner.getValue(AppConstants.PLN_ACCT_BANNER_EFFECTIVE_START_DATE);

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

                // Invalid - More than one current association for a banner
                if (effectiveEndDate.equals(DEFAULT_EFFECTIVE_END_DATE)
                        && isCurrentAssociationPresent) {
                    return new OperationResult(new OperationExecutionError(
                            "SIP-50104",
                            new String[]{planningAccount.getObjectName()},
                            getLocalizationGate()));
                }

                if (effectiveEndDate.equals(DEFAULT_EFFECTIVE_END_DATE)) {
                    isCurrentAssociationPresent = true;
                }

                if (null != previousBanner && (effectiveStartDate.equals(previousBannerEffectiveEndDate) || effectiveStartDate.before(previousBannerEffectiveEndDate))) {
                    return new OperationResult(new OperationExecutionError(
                            "SIP-50105",
                            new String[]{planningAccount.getObjectName()},
                            getLocalizationGate()));
                }

                String sqlEffectiveStartDate = AppUtils.getSQLStandardDateStr(effectiveStartDate);
                String sqlEffectiveEndDate = AppUtils.getSQLStandardDateStr(effectiveEndDate);
                String filterCriteria = "((EFFECTIVE_START_DATE <= TO_DATE('" + sqlEffectiveStartDate
                        + "') AND EFFECTIVE_END_DATE >= TO_DATE('" + sqlEffectiveStartDate
                        + "')) OR (EFFECTIVE_START_DATE <= TO_DATE('" + sqlEffectiveEndDate
                        + "') AND EFFECTIVE_END_DATE >= TO_DATE('" + sqlEffectiveEndDate
                        + "')) OR (EFFECTIVE_START_DATE >= TO_DATE('" + sqlEffectiveStartDate
                        + "') AND EFFECTIVE_END_DATE <= TO_DATE('" + sqlEffectiveEndDate
                        + "'))) AND TRIM(BANNER_ID) = '" + bannerId + "'";
                if (isThereDateOverlap(AppConstants.BO_PLN_ACCT_BANNER_REL,
                        filterCriteria,
                        excludedBanners.toString())) {
                    return new OperationResult(new OperationExecutionError(
                            "SIP-50107",
                            new String[]{planningAccount.getObjectName()},
                            getLocalizationGate()));
                }

                //Validation completed for current channel so assign that to previous channesl for next channel validation
                previousBanner = planningAccountBanner;
                previousBannerEffectiveStartDate = effectiveStartDate;
                previousBannerEffectiveEndDate = effectiveEndDate;
            }
        }
        return OperationResult.OK;
    }

    private OperationResult validatePlanningAccountMarket(BDDObject planningAccount) throws ParseException {
        List<BDDObject> marketList = planningAccount.getChildren(AppConstants.PLN_ACCT_CHILD__MARKET);

        String excludedMarkets = AppUtils.getExcludedObjectsId(marketList,
                AppConstants.PLN_ACCT_MARKET_RELATIOSHIP_EFF_STRT_DATE, AppConstants.PLN_ACCT_MARKET_RELATIONSHIP_EFF_END_DATE);

        Collections.sort(marketList, new BddDateComparator(AppConstants.PLN_ACCT_MARKET_RELATIOSHIP_EFF_STRT_DATE));

        boolean isCurrentAssociationPresent = false;
        BDDObject previousMarket = null;
        Date previousMarketEffectiveStartDate = null;
        Date previousMarketEffectiveEndDate = null;

        for (BDDObject planningAccountMarket : marketList) {
            //Market is marked to be deleted so no need to validate
            if (planningAccountMarket.isRemoved()) {
                continue;
            }

            Date effectiveStartDate = null;
            Date effectiveEndDate = null;
            Object effectiveEndDateStr = planningAccountMarket.getValue(AppConstants.PLN_ACCT_MARKET_RELATIONSHIP_EFF_END_DATE);
            Object effectiveStartDateStr = planningAccountMarket.getValue(AppConstants.PLN_ACCT_MARKET_RELATIOSHIP_EFF_STRT_DATE);

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

            // Invalid - More than one current association for a market
            if (effectiveEndDate.equals(DEFAULT_EFFECTIVE_END_DATE)
                    && isCurrentAssociationPresent) {
                return new OperationResult(new OperationExecutionError(
                        "SIP-50104",
                        new String[]{planningAccount.getObjectName()},
                        getLocalizationGate()));
            }

            if (effectiveEndDate.equals(DEFAULT_EFFECTIVE_END_DATE)) {
                isCurrentAssociationPresent = true;
            }

            if (null != previousMarket && (effectiveStartDate.equals(previousMarketEffectiveEndDate) || effectiveStartDate.before(previousMarketEffectiveEndDate))) {
                return new OperationResult(new OperationExecutionError(
                        "SIP-50107",
                        new String[]{planningAccount.getObjectName()},
                        getLocalizationGate()));
            }

            //Validation completed for current channel so assign that to previous channesl for next channel validation
            previousMarket = planningAccountMarket;
            previousMarketEffectiveStartDate = effectiveStartDate;
            previousMarketEffectiveEndDate = effectiveEndDate;
        }
        return OperationResult.OK;
    }

    private OperationResult validateConsumptionBaseLineArea(BDDObject planningAccount) throws ParseException {

        Map<String, List<BDDObject>> consumptionBaseLineAreas = new HashMap<String, List<BDDObject>>();
        StringBuilder excludedConsumptionBaselineArea = new StringBuilder();
        AppUtils.splitSimilarObjects(planningAccount.getChildren(AppConstants.PLN_ACCT_CHILD_CONSUMPTION_BASE_LINE_AREA), consumptionBaseLineAreas, excludedConsumptionBaselineArea,
                AppConstants.CONSUMPTION_BASELINE_AREA_ROWID_OBJECT, AppConstants.PLN_ACCT_CONSUMPTION_BASE_LINE_REL_EFF_END_DATE, AppConstants.PLN_ACCT_CONSUMPTION_BASE_LINE_REL_EFF_STRT_DATE);
        for (String consumptionBaseLineAreaId : consumptionBaseLineAreas.keySet()) {
            List<BDDObject> consumptionBaseLineAreaList = consumptionBaseLineAreas.get(consumptionBaseLineAreaId);
            if (consumptionBaseLineAreaList == null || consumptionBaseLineAreaList.size() == 0) {
                continue;
            }

            Collections.sort(consumptionBaseLineAreaList, new BddDateComparator(AppConstants.PLN_ACCT_CONSUMPTION_BASE_LINE_REL_EFF_STRT_DATE));

            boolean isCurrentAssociationPresent = false;
            BDDObject previousConsumptionBaseLineArea = null;
            Date previousConsumptionBaseLineAreaEffectiveStartDate = null;
            Date previousConsumptionBaseLineAreaEffectiveEndDate = null;

            for (BDDObject consumptionBaseLineArea : consumptionBaseLineAreaList) {
                Date effectiveStartDate = null;
                Date effectiveEndDate = null;
                Object effectiveEndDateStr = consumptionBaseLineArea.getValue(AppConstants.PLN_ACCT_CONSUMPTION_BASE_LINE_REL_EFF_END_DATE);
                Object effectiveStartDateStr = consumptionBaseLineArea.getValue(AppConstants.PLN_ACCT_CONSUMPTION_BASE_LINE_REL_EFF_STRT_DATE);

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
                            new String[]{planningAccount.getObjectName()},
                            getLocalizationGate()));
                }

                // Invalid - More than one current association for a consumption base line area
                if (effectiveEndDate.equals(DEFAULT_EFFECTIVE_END_DATE)
                        && isCurrentAssociationPresent) {
                    return new OperationResult(new OperationExecutionError(
                            "SIP-50104",
                            new String[]{planningAccount.getObjectName()},
                            getLocalizationGate()));
                }

                if (effectiveEndDate.equals(DEFAULT_EFFECTIVE_END_DATE)) {
                    isCurrentAssociationPresent = true;
                }

                if (null != previousConsumptionBaseLineArea && (effectiveStartDate.equals(previousConsumptionBaseLineAreaEffectiveEndDate) || effectiveStartDate.before(previousConsumptionBaseLineAreaEffectiveEndDate))) {
                    return new OperationResult(new OperationExecutionError(
                            "SIP-50105",
                            new String[]{planningAccount.getObjectName()},
                            getLocalizationGate()));
                }
                String sqlEffectiveStartDate = AppUtils.getSQLStandardDateStr(effectiveStartDate);
                String sqlEffectiveEndDate = AppUtils.getSQLStandardDateStr(effectiveEndDate);
                String filterCriteria = "((EFFECTIVE_START_DATE <= TO_DATE('" + sqlEffectiveStartDate
                        + "') AND EFFECTIVE_END_DATE >= TO_DATE('" + sqlEffectiveStartDate
                        + "')) OR (EFFECTIVE_START_DATE <= TO_DATE('" + sqlEffectiveEndDate
                        + "') AND EFFECTIVE_END_DATE >= TO_DATE('" + sqlEffectiveEndDate
                        + "')) OR (EFFECTIVE_START_DATE >= TO_DATE('" + sqlEffectiveStartDate
                        + "') AND EFFECTIVE_END_DATE <= TO_DATE('" + sqlEffectiveEndDate
                        + "'))) AND TRIM(CNSMP_BASLN_AREA_ID) = '" + consumptionBaseLineAreaId.trim() + "'";
                if (isThereDateOverlap(AppConstants.BO_PLAN_BASLN_REL,
                        filterCriteria,
                        excludedConsumptionBaselineArea.toString())) {
                    return new OperationResult(new OperationExecutionError(
                            "SIP-50107",
                            new String[]{planningAccount.getObjectName()},
                            getLocalizationGate()));
                }

                //Validation completed for current channel so assign that to previous channesl for next channel validation
                previousConsumptionBaseLineArea = consumptionBaseLineArea;
                previousConsumptionBaseLineAreaEffectiveStartDate = effectiveStartDate;
                previousConsumptionBaseLineAreaEffectiveEndDate = effectiveEndDate;

            }
        }
        return OperationResult.OK;
    }

    private OperationResult validateSubPlanningAccount(BDDObject planningAccount) throws ParseException {
        Map<String, List<BDDObject>> subPlanningAccounts = new HashMap<String, List<BDDObject>>();
        StringBuilder excludedSubPlanningAccounts = new StringBuilder();

        AppUtils.splitSimilarObjects(planningAccount.getChildren(AppConstants.PLN_ACCT_CHILD_SUB_PLN_ACCT), subPlanningAccounts, excludedSubPlanningAccounts,
                AppConstants.SUBPLANNING_ACCT_ROWID_OBJECT, AppConstants.SUB_PLN_ACCT_COL_RELATIONSHIP_EFF_END_DATE, AppConstants.SUB_PLN_ACCT_COL_RELATIONSHIP_EFF_STRT_DATE);

        for (String subPlanningAccountId : subPlanningAccounts.keySet()) {
            List<BDDObject> subPlanningAccountList = subPlanningAccounts.get(subPlanningAccountId);
            if (subPlanningAccountList == null || subPlanningAccountList.size() == 0) {
                continue;
            }

            Collections.sort(subPlanningAccountList, new BddDateComparator(AppConstants.SUB_PLN_ACCT_COL_RELATIONSHIP_EFF_STRT_DATE));

            boolean isCurrentAssociationPresent = false;
            BDDObject previousSubPlanningAccount = null;
            Date previousSubPlannAccntEffectiveStartDate = null;
            Date previousSubPlannAccntEffectiveEndDate = null;

            for (BDDObject subPlanningAccount : subPlanningAccountList) {
                Date effectiveStartDate = null;
                Date effectiveEndDate = null;
                Object effectiveEndDateStr = subPlanningAccount.getValue(AppConstants.SUB_PLN_ACCT_COL_RELATIONSHIP_EFF_END_DATE);
                Object effectiveStartDateStr = subPlanningAccount.getValue(AppConstants.SUB_PLN_ACCT_COL_RELATIONSHIP_EFF_STRT_DATE);

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
                }// Invalid – start date is later than end date
                if (effectiveEndDate.before(effectiveStartDate)) {
                    return new OperationResult(new OperationExecutionError(
                            "SIP-50103",
                            new String[]{planningAccount.getObjectName()},
                            getLocalizationGate()));
                }

                // Invalid - More than one current association for a sub planning account
                if (effectiveEndDate.equals(DEFAULT_EFFECTIVE_END_DATE)
                        && isCurrentAssociationPresent) {
                    return new OperationResult(new OperationExecutionError(
                            "SIP-50104",
                            new String[]{planningAccount.getObjectName()},
                            getLocalizationGate()));
                }

                if (effectiveEndDate.equals(DEFAULT_EFFECTIVE_END_DATE)) {
                    isCurrentAssociationPresent = true;
                }

                if (null != previousSubPlanningAccount && (effectiveStartDate.equals(previousSubPlannAccntEffectiveEndDate) || effectiveStartDate.before(previousSubPlannAccntEffectiveEndDate))) {
                    return new OperationResult(new OperationExecutionError(
                            "SIP-50105",
                            new String[]{planningAccount.getObjectName()},
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
                        + "'))) AND TRIM(SUB_PLANNING_ACCOUNT_ID) = '" + subPlanningAccountId.trim() + "'";
                if (isThereDateOverlap(AppConstants.BO_PLAN_ACCT_REL,
                        filterCriteria,
                        excludedSubPlanningAccounts.toString())) {
                    return new OperationResult(new OperationExecutionError(
                            "SIP-50107",
                            new String[]{subPlanningAccount.getObjectName()},
                            getLocalizationGate()));
                }

                //Validation completed for current channel so assign that to previous channesl for next channel validation
                previousSubPlanningAccount = subPlanningAccount;
                previousSubPlannAccntEffectiveStartDate = effectiveStartDate;
                previousSubPlannAccntEffectiveEndDate = effectiveEndDate;
            }
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
