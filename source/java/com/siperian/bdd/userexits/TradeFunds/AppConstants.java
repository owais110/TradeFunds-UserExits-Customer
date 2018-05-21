package com.siperian.bdd.userexits.TradeFunds;

import java.util.Date;

public class AppConstants {

    //253402214400000l Milliseconds of 9999-12-31 00:00:00.000
    public final static Date DEFAULT_EFFECTIVE_END_DATE = new Date(253402214400000l);

    //Date formats
    public final static String DATE_FORMATE_HYPHEN = "yyyy-MM-dd HH:mm:ss.S";
    public final static String DATE_FORMATE_SPACE = "EEE MMM dd HH:mm:ss z yyyy";
    public final static String DATE_FORMATE_SQL = "dd-MMM-yyyy";

    //Base Objects Names
    public final static String BO_WHOLESALER_REL = "C_MSB_BO_WHOLESALER_REL";
    public final static String BO_PLAN_ACCT_REL = "C_MSB_BO_PLAN_ACCT_REL";
    public final static String BO_PLN_SHIPTO_REL = "C_MSB_BO_PLN_SHIPTO_REL";
    public final static String BO_PLN_ACCT_BANNER_REL = "C_MSB_BO_PLN_ACCT_BANNER";
    public final static String BO_PLN_ACT_NDE_REL = "C_MSB_BO_PLN_ACT_NDE_REL";
    public final static String BO_PLAN_BASLN_REL = "C_MSB_BO_PLAN_BASLN_REL";
    public final static String BO_HIERARCHY_NODE = "C_MSB_BO_HIERARCHY_NODE";
    public final static String BO_HIERARCHY_REL = "C_MSB_BO_HIERARCHY_REL";

    //Base Object Hierarchy Node columns
    public final static String BO_HIERARCHY_NODE_HIERARCHY_NODE_CODE = "HIERARCHY_NODE_CODE";

    //SBU Node Names
    public final static String SBU_CHILD_CHANNEL = "SAC_SHLVL2";
    public final static String SBU_CHILD_CHANNEL_DIVISION = "SAGC_SHLVL3";

    //SBU Hierarchy Relationship Column Names
    public final static String SBU_COL_RELATIONSHIP_EFF_END_DATE = "C_MSB_BO_HIERARCHY_REL|RELATIONSHIP_EFF_END_DATE";
    public final static String SBU_COL_RELATIONSHIP_EFF_STRT_DATE = "C_MSB_BO_HIERARCHY_REL|RELATIONSHIP_EFF_STRT_DATE";
    public final static String SBU_COL_CHILD_NODE_ID = "C_MSB_BO_HIERARCHY_NODE|ROWID_OBJECT";
    public final static String SBU_COL_HIERARCHY_TYPE_CODE = "C_MSB_BO_HIERARCHY_REL|HIERARCHY_TYPE_CODE";
    public final static String SBU_COL_HIERARCHY_NODE_CODE = "C_MSB_BO_HIERARCHY_NODE|HIERARCHY_NODE_CODE";
    public final static String SBU_COL_HIERARCHY_NODE_NAME = "C_MSB_BO_HIERARCHY_NODE|HIERARCHY_NODE_NAME";
    public final static String SBU_COL_RELATIONSHIP_TYPE_CODE = "C_MSB_BO_HIERARCHY_REL|RELATIONSHIP_TYPE_CODE";
    public final static String SBU_COL_HIERARCHY_NODE_TYPE_CODE = "C_MSB_BO_HIERARCHY_NODE|HIERARCHY_NODE_TYPE_CODE";
    public final static String SBU_SALES_SEGMENT_NODE_TYPE = "SHLVL2";
    public final static String SBU_SALES_DIVISION_NODE_TYPE = "SHLVL3";

    //Sales Segement Node Names
    public final static String SAL_SEG_CHILD_SALES_BUSINESS_UNIT = "SAC_SHLVL1";
    public final static String SAL_SEG_CHILD_SALES_DIVISION = "SAC_SHLVL3";
    public final static String SAL_SEG_CHILD_SALES_REGION = "SAGC_SHLVL4";
//  public final static String SAL_SEG_CHILD_ALL_SALES_DIVISION = "SAC_AllSHLVL3";
//  public final static String SAL_SEG_CHILD_ALL_SALES_REGION = "SAGC_AllSHLVL4";

    //Sales Segement Column Names
    public final static String SAL_SEG_COL_HIERARCHY_NODE_CODE = "C_MSB_BO_HIERARCHY_NODE|HIERARCHY_NODE_CODE";
    public final static String SAL_SEG_COL_HIERARCHY_NODE_NAME = "C_MSB_BO_HIERARCHY_NODE|HIERARCHY_NODE_NAME";
    public final static String SAL_SEG_COL_RELATIONSHIP_EFF_END_DATE = "C_MSB_BO_HIERARCHY_REL|RELATIONSHIP_EFF_END_DATE";
    public final static String SAL_SEG_COL_RELATIONSHIP_EFF_STRT_DATE = "C_MSB_BO_HIERARCHY_REL|RELATIONSHIP_EFF_STRT_DATE";
    public final static String SAL_SEG_COL_CHILD_NODE_ID = "C_MSB_BO_HIERARCHY_NODE|ROWID_OBJECT";
    public final static String SAL_SEG_COL_HIERARCHY_TYPE_CODE = "C_MSB_BO_HIERARCHY_REL|HIERARCHY_TYPE_CODE";
    public final static String SAL_SEG_COL_RELATIONSHIP_TYPE_CODE = "C_MSB_BO_HIERARCHY_REL|RELATIONSHIP_TYPE_CODE";

    //Division Node Names
    public final static String DIV_CHILD_REGION = "SAC_SHLVL4";
    public final static String DIV_CHILD_REGION_MARKET = "SAGC_SHLVL5";
//  public final static String DIV_CHILD_ALL_REGIONS = "SAC_AllSHLVL4";
//  public final static String DIV_CHILD_ALL_REGION_MARKET = "SAGC_AllSHLVL5";
    public final static String DIV_CHILD_SALES_SEGMENT = "SAC_SHLVL2";

    //Division Hierarchy Relationship Column Names
    public final static String DIV_COL_RELATIONSHIP_EFF_END_DATE = "C_MSB_BO_HIERARCHY_REL|RELATIONSHIP_EFF_END_DATE";
    public final static String DIV_COL_RELATIONSHIP_EFF_STRT_DATE = "C_MSB_BO_HIERARCHY_REL|RELATIONSHIP_EFF_STRT_DATE";
    public final static String DIV_COL_CHILD_NODE_ID = "C_MSB_BO_HIERARCHY_NODE|ROWID_OBJECT";
    public final static String DIV_COL_HIERARCHY_TYPE_CODE = "C_MSB_BO_HIERARCHY_REL|HIERARCHY_TYPE_CODE";
    public final static String DIV_COL_HIERARCHY_NODE_CODE = "C_MSB_BO_HIERARCHY_NODE|HIERARCHY_NODE_CODE";
    public final static String DIV_COL_HIERARCHY_NODE_NAME = "C_MSB_BO_HIERARCHY_NODE|HIERARCHY_NODE_NAME";
    public final static String DIV_COL_RELATIONSHIP_TYPE_CODE = "C_MSB_BO_HIERARCHY_REL|RELATIONSHIP_TYPE_CODE";
    public final static String DIV_COL_HIERARCHY_NODE_TYPE_CODE = "C_MSB_BO_HIERARCHY_NODE|HIERARCHY_NODE_TYPE_CODE";
    public final static String DIV_SALES_REGION_NODE_TYPE = "SHLVL4";
    public final static String DIV_SALES_MARKET_NODE_TYPE = "SHLVL5";

    //Sales Region Node Names
    public final static String REGION_CHILD_SALES_DIVISION = "SAC_SHCLVL3";
    public final static String REGION_CHILD_SALES_MARKET = "SAC_SHLVL5";
    public final static String REGION_CHILD_PLANNING_ACCOUNT = "SAGC_PlanningAccount";

    //Sales Region Column Names
    public final static String SAL_REGION_COL_HIERARCHY_NODE_CODE = "C_MSB_BO_HIERARCHY_NODE|HIERARCHY_NODE_CODE";
    public final static String SAL_REGION_COL_HIERARCHY_NODE_NAME = "C_MSB_BO_HIERARCHY_NODE|HIERARCHY_NODE_NAME";
    public final static String SAL_REGION_RELATIONSHIP_COL_EFF_END_DATE = "C_MSB_BO_PLN_ACT_NDE_REL|RELATIONSHIP_EFF_END_DATE";
    public final static String SAL_REGION_RELATIOSHIP_COL_EFF_STRT_DATE = "C_MSB_BO_PLN_ACT_NDE_REL|RELATIOSHIP_EFF_STRT_DATE";
    public final static String SAL_REGION_COL_RELATIONSHIP_EFF_END_DATE = "C_MSB_BO_HIERARCHY_REL|RELATIONSHIP_EFF_END_DATE";
    public final static String SAL_REGION_COL_RELATIONSHIP_EFF_STRT_DATE = "C_MSB_BO_HIERARCHY_REL|RELATIONSHIP_EFF_STRT_DATE";
    public final static String SAL_REGION_MARKET_COL_ROWID_OBJECT = "C_MSB_BO_HIERARCHY_NODE|ROWID_OBJECT";
    public final static String SAL_REGION_PLANNING_ACCT_ROWID_OBJECT = "C_MSB_BO_PLANNING_ACCT|ROWID_OBJECT";
    public final static String SAL_REGION_COL_RELATIONSHIP_TYPE_CODE = "C_MSB_BO_HIERARCHY_REL|RELATIONSHIP_TYPE_CODE";
    public final static String SAL_REGION_COL_HIERARCHY_TYPE_CODE = "C_MSB_BO_HIERARCHY_REL|HIERARCHY_TYPE_CODE";

    //Sales Market Node Names
    public final static String MARKET_CHILD_SALES_REGION = "SAC_SHLVL4";
    public final static String MARKET_CHILD_PLANNING_ACCOUNT = "SAC_PlanningAccount";
    public final static String MARKET_CHILD_SUB_PLANNING_ACCOUNT = "SAGC_SubPlanningAccount";

    //Market Column Names
    public final static String MARKET_COL_HIERARCHY_NODE_CODE = "C_MSB_BO_HIERARCHY_NODE|HIERARCHY_NODE_CODE";
    public final static String MARKET_COL_HIERARCHY_NODE_NAME = "C_MSB_BO_HIERARCHY_NODE|HIERARCHY_NODE_NAME";
    public final static String MARKET_COL_PLN_ACT_NOD_REL_EFF_END_DATE = "C_MSB_BO_PLN_ACT_NDE_REL|RELATIONSHIP_EFF_END_DATE";
    public final static String MARKET_COL_PLN_ACT_NOD_REL_EFF_STRT_DATE = "C_MSB_BO_PLN_ACT_NDE_REL|RELATIOSHIP_EFF_STRT_DATE";
    public final static String MARKET_COL_PLN_ACT_REL_EFF_END_DATE = "C_MSB_BO_PLAN_ACCT_REL|RELATIONSHIP_EFF_END_DATE";
    public final static String MARKET_COL_PLN_ACT_REL_EFF_STRT_DATE = "C_MSB_BO_PLAN_ACCT_REL|RELATIONSHIP_EFF_STRT_DATE";
    public final static String MARKET_COL_HIERARCHY_REL_EFF_END_DATE = "C_MSB_BO_HIERARCHY_REL|RELATIONSHIP_EFF_END_DATE";
    public final static String MARKET_COL_HIERARCHY_REL_EFF_STRT_DATE = "C_MSB_BO_HIERARCHY_REL|RELATIONSHIP_EFF_STRT_DATE";
    public final static String MARKET_COL_HIERARCHY_ROWID_OBJECT = "C_MSB_BO_HIERARCHY_NODE|ROWID_OBJECT";
    public final static String MARKET_COL_PLANNING_ACCT_ROWID_OBJECT = "C_MSB_BO_PLANNING_ACCT|ROWID_OBJECT";
    public final static String MARKET_COL_RELATIONSHIP_TYPE_CODE = "C_MSB_BO_HIERARCHY_REL|RELATIONSHIP_TYPE_CODE";
    public final static String MARKET_COL_HIERARCHY_TYPE_CODE = "C_MSB_BO_HIERARCHY_REL|HIERARCHY_TYPE_CODE";

    //Planning Account Node Names
    public final static String PLANNING_ACCT = "SA_PlanningAccount";
    public final static String PLN_ACCT_CHILD_BANNER = "SAC_Banner";
    public final static String PLN_ACCT_CHILD__MARKET = "SAC_SHLVL5";
    public final static String PLN_ACCT_CHILD_CONSUMPTION_BASE_LINE_AREA = "SAC_ConsumptionRetailArea";
    public final static String PLN_ACCT_CHILD_SUB_PLN_ACCT = "SAC_SubPlanningAccount";

    //Planning Account Column Names
    public final static String PLANNING_ACCOUNT_NAME = "C_MSB_BO_PLANNING_ACCT|PLANNING_ACCOUNT_NAME";
    public final static String PLN_ACCT_END_DATE = "C_MSB_BO_PLANNING_ACCT|PLANNING_ACCT_END_DATE";
    public final static String PLN_ACCT_EFF_DATE = "C_MSB_BO_PLANNING_ACCT|PLANNING_ACCT_EFF_DATE";
    public final static String PLN_ACCT_BANNER_ID = "C_MSB_BO_PLN_ACCT_BANNER|BANNER_ID";
    public final static String PLN_ACCT_BANNER_EFFECTIVE_END_DATE = "C_MSB_BO_PLN_ACCT_BANNER|EFFECTIVE_END_DATE";
    public final static String PLN_ACCT_BANNER_EFFECTIVE_START_DATE = "C_MSB_BO_PLN_ACCT_BANNER|EFFECTIVE_START_DATE";
    public final static String MARKET_ROWID_OBJECT = "C_MSB_BO_HIERARCHY_NODE|ROWID_OBJECT";
    public final static String PLN_ACCT_MARKET_RELATIONSHIP_EFF_END_DATE = "C_MSB_BO_PLN_ACT_NDE_REL|RELATIONSHIP_EFF_END_DATE";
    public final static String PLN_ACCT_MARKET_RELATIOSHIP_EFF_STRT_DATE = "C_MSB_BO_PLN_ACT_NDE_REL|RELATIOSHIP_EFF_STRT_DATE";
    public final static String CONSUMPTION_BASELINE_AREA_ROWID_OBJECT = "C_MSB_BO_CNSMP_BSLN_AREA|ROWID_OBJECT";
    public final static String PLN_ACCT_CONSUMPTION_BASE_LINE_REL_EFF_END_DATE = "C_MSB_BO_PLAN_BASLN_REL|EFFECTIVE_END_DATE";
    public final static String PLN_ACCT_CONSUMPTION_BASE_LINE_REL_EFF_STRT_DATE = "C_MSB_BO_PLAN_BASLN_REL|EFFECTIVE_START_DATE";
    public final static String SUBPLANNING_ACCT_ROWID_OBJECT = "C_MSB_BO_PLANNING_ACCT|ROWID_OBJECT";
    public final static String SUB_PLN_ACCT_COL_RELATIONSHIP_EFF_END_DATE = "C_MSB_BO_PLAN_ACCT_REL|RELATIONSHIP_EFF_END_DATE";
    public final static String SUB_PLN_ACCT_COL_RELATIONSHIP_EFF_STRT_DATE = "C_MSB_BO_PLAN_ACCT_REL|RELATIONSHIP_EFF_STRT_DATE";
    public final static String SUB_PLN_ACCT_COL_ACCOUNT_TYPE = "C_MSB_BO_PLANNING_ACCT|PLANNING_ACCT_TYPE_CODE";
    public final static String SUB_PLN_ACCOUNT_PLANNING_TYPE = "PLANNING";
    public final static String JMS_SALES_ORG_CODE = "C_MSB_BO_PLANNING_ACCT|JMS_SALES_ORG_CODE";

    //Sub Planning Account Node Names
    public final static String SUBPLANNING_ACCT = "SA_SubPlanningAccount";
    public final static String SUBPLANNING_ACCT_SHIPTO = "SAC_ShipTo";
    public final static String SUBPLANNING_ACCT_PLANNING_ACCT = "SAC_PlanningAccount";

    //Sub Planning Account Column Names
    public final static String SUB_PLANNING_ACCOUNT_NAME = "C_MSB_BO_PLANNING_ACCT|PLANNING_ACCOUNT_NAME";
    public final static String SUB_PLN_ACCT_COL_EFF_END_DATE = "C_MSB_BO_PLANNING_ACCT|PLANNING_ACCT_END_DATE";
    public final static String SUB_PLN_ACCT_COL_EFF_START_DATE = "C_MSB_BO_PLANNING_ACCT|PLANNING_ACCT_EFF_DATE";
    public final static String SUB_PLN_ACCT_SHIP_TO_ROWID_OBJECT = "C_MSB_BO_SHIP_TO_CUST|ROWID_OBJECT";
    public final static String SUB_PLN_ACCT_COL_SHIP_TO_REL_EFF_END_DATE = "C_MSB_BO_PLN_SHIPTO_REL|RELATIONSHIP_EFF_END_DATE";
    public final static String SUB_PLN_ACCT_COL_SHIP_TO_REL_EFF_START_DATE = "C_MSB_BO_PLN_SHIPTO_REL|RELATIONSHIP_EFF_STRT_DATE";
    public final static String SUB_PLN_ACCT_PLNACCT_ROWID_OBJECT = "C_MSB_BO_PLANNING_ACCT|ROWID_OBJECT";
    public final static String SUB_PLN_ACCT_COL_PLNACCT_REL_EFF_END_DATE = "C_MSB_BO_PLAN_ACCT_REL|RELATIONSHIP_EFF_END_DATE";
    public final static String SUB_PLN_ACCT_COL_PLN_ACCT_REL_EFF_START_DATE = "C_MSB_BO_PLAN_ACCT_REL|RELATIONSHIP_EFF_STRT_DATE";
    public final static String SUB_PLN_ACCT_COL_JMS_SALES_ORG_CODE = "C_MSB_BO_PLANNING_ACCT|JMS_SALES_ORG_CODE";

    //Whole Sale Node Names
    public final static String WHOLE_SALE_CHILD_INDIRECT = "SAC_Indirect";

    //Whole Sale Column Names
    public final static String WHOLE_SALE_COL_RELATIONSHIP_EFFECTIVE_END_DATE = "C_MSB_BO_WHOLESALER_REL|EFFECTIVE_END_DATE";
    public final static String WHOLE_SALE_COL_RELATIONSHIP_EFFECTIVE_START_DATE = "C_MSB_BO_WHOLESALER_REL|EFFECTIVE_START_DATE";
    public final static String WHOLE_SALE_INDIRECT_COL_ROWID = "C_MSB_BO_PLANNING_ACCT|ROWID_OBJECT";
    public final static String WHOLE_SALE_COL_INDIRECT_FLAG = "C_MSB_BO_PLANNING_ACCT|INDIRECT_FLAG";
    public final static String WHOLE_SALE_COL_INDIRECT_FLAG_VALUE = "Y";

    //ShipTo Node Names
    public final static String SHIP_TO_SUB_PLN_ACCT = "SAC_SubPlanningAccount";
    public final static String SHIP_TO_SUB_PLN_ACCT_PLN_ACCT = "SAGC_PlanningAccount";

    //ShipTo Column Names    
    public final static String SHIP_TO_SUB_PLN_ACCT_COL_ROWID = "C_MSB_BO_PLANNING_ACCT|ROWID_OBJECT";
    public final static String SHIP_TO_COL_RELATIONSHIP_EFFECTIVE_END_DATE = "C_MSB_BO_PLN_SHIPTO_REL|RELATIONSHIP_EFF_END_DATE";
    public final static String SHIP_TO_COL_RELATIONSHIP_EFFECTIVE_START_DATE = "C_MSB_BO_PLN_SHIPTO_REL|RELATIONSHIP_EFF_STRT_DATE";
    public final static String SHIP_TO_PLN_ACCT_COL_ROWID = "C_MSB_BO_PLANNING_ACCT|ROWID_OBJECT";
    public final static String SHIP_TO_PLN_ACCT_COL_RELATIONSHIP_EFFECTIVE_END_DATE = "C_MSB_BO_PLAN_ACCT_REL|RELATIONSHIP_EFF_END_DATE";
    public final static String SHIP_TO_PLN_ACCT_COL_RELATIONSHIP_EFFECTIVE_START_DATE = "C_MSB_BO_PLAN_ACCT_REL|RELATIONSHIP_EFF_STRT_DATE";
    public final static String SHIP_TO_PLN_ACCT_COL_ACCOUNT_TYPE = "C_MSB_BO_PLANNING_ACCT|PLANNING_ACCT_TYPE_CODE";
    public final static String SHIP_TO_PLANNING_ACCOUNT_TYPE = "PLANNING";
    public final static String SHIP_TO_SUB_PLANNING_ACCOUNT_TYPE = "SUB PLANNING";

    //JMS Sales Organization Column Names    
    public final static String JMS_SALES_ORGANIZATION_CODE = "C_MSB_BO_JMS_SALES_ORG|JMS_SALES_ORG_CODE";
    public final static String JMS_SALES_ORGANIZATION_NAME = "C_MSB_BO_JMS_SALES_ORG|JMS_SALES_ORG_NAME";

    //RGM Designated Baseline Column Names    
    public final static String RGM_DESG_BASLN_NAME = "C_MSB_BO_RGM_DESG_BASLN|RGM_DESG_BASELINE_NAME";

    //Consumption Baseline Area Column Names    
    public final static String CNSMP_BSLN_AREA_NAME = "C_MSB_BO_CNSMP_BSLN_AREA|CNSMP_BASLN_SRC_AREA_NAME";

    //Consumption Baseline Source Column Names    
    public final static String CNSMP_BASLN_SRC_NAME = "C_MSB_BO_CNSMP_BASLN_SRC|CNSMP_BASLN_SRC_NAME";

    //ConsumptionBaseLineSource Node Names
    public final static String CONSUMPTION_BASELINE_SOURCE_AREA = "SAC_ConsumptionRetailArea";

    //Corporate Customer Column Names    
    public final static String CORPORATE_CUSTOMER_NAME = "C_MSB_BO_CORP_CUSTOMER|CORPORATE_CUSTOMER_NAME";

    //Banner Column Names    
    public final static String BANNER_NAME = "C_MSB_BO_BANNER|BANNER_NAME";

    //Sales Hierarchy Levels Screen fields
    public final static String SALES_HIERARCHY_LEVEL_SCREEN_FEILD_CODE = "Code";
    public final static String SALES_HIERARCHY_LEVEL_SCREEN_FEILD_NAME = "Name";

}
