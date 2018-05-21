package com.siperian.bdd.userexits.TradeFunds;

import com.siperian.bdd.userexits.datamodel.BDDObject;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

public class AppUtils {

    public static Date convertObjectToDate(Object dateStringObject) throws ParseException {

        if (dateStringObject == null) {
            return null;
        }
        String dateString = dateStringObject.toString();
        DateFormat inputformat = null;
        if (dateString.indexOf("-") > 0) {
            inputformat = new SimpleDateFormat(AppConstants.DATE_FORMATE_HYPHEN);
        } else {
            inputformat = new SimpleDateFormat(AppConstants.DATE_FORMATE_SPACE);
        }

        Calendar cal = Calendar.getInstance();
        cal.setTime(inputformat.parse(dateString));
        cal.set(Calendar.HOUR_OF_DAY, cal.getActualMinimum(Calendar.HOUR_OF_DAY));
        cal.set(Calendar.MINUTE, cal.getActualMinimum(Calendar.MINUTE));
        cal.set(Calendar.SECOND, cal.getActualMinimum(Calendar.SECOND));
        cal.set(Calendar.MILLISECOND, cal.getActualMinimum(Calendar.MILLISECOND));

        return cal.getTime();
    }

    public static String getSQLStandardDateStr(Date date) {
        SimpleDateFormat dateFormat = new SimpleDateFormat(AppConstants.DATE_FORMATE_SQL);

        return dateFormat.format(new Date(date.getTime()));
    }

    public static String getRowIds(Map<String, BDDObject> excludedObjects) {
        StringBuilder rowIdsList = new StringBuilder();

        for (String key : excludedObjects.keySet()) {
            BDDObject deletedObject = excludedObjects.get(key);
            rowIdsList.append("'").append(deletedObject.getRowId().trim()).append("',");
        }

        if (rowIdsList.length() > 0) {
            return rowIdsList.substring(0, rowIdsList.length() - 1);
        }

        return null;
    }

    public static void splitSimilarObjects(List<BDDObject> bddObjects,
            Map<String, List<BDDObject>> identicalObjects,
            Map<String, BDDObject> excludedObjects,
            String nodeId, String effectiveEndDateCol, String effectiveStartDateCol) {

        for (BDDObject bddObject : bddObjects) {
            if (bddObject.isRemoved() && !excludedObjects.containsKey(bddObject.getRowId().trim())) {
                excludedObjects.put(bddObject.getRowId().trim(), bddObject);
            } else if (bddObject.isCreated()) {
                AppUtils.getListFromMap(identicalObjects, bddObject.getValue(nodeId).toString().trim()).add(bddObject);
            } else if (bddObject.isChanged()) {
                List<String> changesCols = bddObject.getChangedColumns();
                if (changesCols.contains(effectiveEndDateCol) || changesCols.contains(effectiveStartDateCol)) {
                    if (!excludedObjects.containsKey(bddObject.getRowId().trim())) {
                        AppUtils.getListFromMap(identicalObjects, bddObject.getValue(nodeId).toString().trim()).add(bddObject);
                        excludedObjects.put(bddObject.getRowId().trim(), bddObject);
                    }
                }
            }
        }

    }

    public static void splitParentWithChildSimilarObjects(List<BDDObject> bddObjects,
            Map<String, List<BDDObject>> identicalParentObjects,
            StringBuilder excludedParentObjects,
            Map<String, List<BDDObject>> identicalChildObjects,
            StringBuilder excludedChildObjects,
            String childRelName, String parentNodeId, String parentEffectiveStartDateCol, String parentEffectiveEndDateCol,
            String childNodeId, String childEffectiveStartDateCol, String childEffectiveEndDateCol) {

        if (excludedParentObjects.length() > 0) {
            excludedParentObjects.append(",");
        }
        for (BDDObject parentBddObject : bddObjects) {
            if (parentBddObject.isRemoved() && excludedParentObjects.indexOf(parentBddObject.getRowId().trim()) == -1) {
                excludedParentObjects.append("'").append(parentBddObject.getRowId().trim()).append("',");
            } else if (parentBddObject.isCreated()) {
                AppUtils.getListFromMap(identicalParentObjects, parentBddObject.getValue(parentNodeId).toString().trim()).add(parentBddObject);

            } else if (parentBddObject.isChanged()) {
                List<String> changesCols = parentBddObject.getChangedColumns();
                if (changesCols.contains(parentEffectiveEndDateCol) || changesCols.contains(parentEffectiveStartDateCol)) {
                    if (excludedParentObjects.indexOf(parentBddObject.getRowId().trim()) == -1) {
                        AppUtils.getListFromMap(identicalParentObjects, parentBddObject.getValue(parentNodeId).toString().trim()).add(parentBddObject);
                        excludedParentObjects.append("'").append(parentBddObject.getRowId().trim()).append("',");

                    }
                }
            }
            if (excludedChildObjects.length() > 0) {
                excludedChildObjects.append(",");
            }
            //splitMarkets(childRegion.getChildren(childRelName), markets, excludedMarkets);
            AppUtils.splitSimilarObjects(parentBddObject.getChildren(childRelName), identicalChildObjects, excludedChildObjects,
                    childNodeId, childEffectiveEndDateCol, childEffectiveStartDateCol);
        }
        if (excludedParentObjects.length() > 0) {
            excludedParentObjects.setLength(excludedParentObjects.length() - 1);
        }

    }

    public static void splitSimilarObjects(List<BDDObject> bddObjects,
            Map<String, List<BDDObject>> identicalObjects,
            StringBuilder excludedObjects,
            String nodeId, String effectiveEndDateCol, String effectiveStartDateCol) {

        for (BDDObject bddObject : bddObjects) {
            if (bddObject.isRemoved() && excludedObjects.indexOf(bddObject.getRowId().trim()) == -1) {
                excludedObjects.append("'").append(bddObject.getRowId().trim()).append("',");
            } else if (bddObject.isCreated()) {
                AppUtils.getListFromMap(identicalObjects, bddObject.getValue(nodeId).toString().trim()).add(bddObject);
            } else if (bddObject.isChanged()) {
                List<String> changesCols = bddObject.getChangedColumns();
                if (changesCols.contains(effectiveEndDateCol) || changesCols.contains(effectiveStartDateCol)) {
                    if (excludedObjects.indexOf(bddObject.getRowId().trim()) == -1) {
                        AppUtils.getListFromMap(identicalObjects, bddObject.getValue(nodeId).toString().trim()).add(bddObject);
                        excludedObjects.append("'").append(bddObject.getRowId().trim()).append("',");
                    }
                }
            }
        }
        if (excludedObjects.length() > 0) {
            excludedObjects.setLength(excludedObjects.length() - 1);

        }
    }

    public static List<BDDObject> getListFromMap(Map<String, List<BDDObject>> objectsMap, String nodeId) {
        List<BDDObject> objectsList = objectsMap.get(nodeId);
        if (null == objectsList) {
            objectsList = new ArrayList<BDDObject>();
        }

        objectsMap.put(nodeId, objectsList);

        return objectsList;
    }

    public static String getExcludedObjectsId(List<BDDObject> bddObjects, String effectiveStartDateCol, String effectiveEndDateCol) {
        StringBuilder excludedObjects = new StringBuilder();
        for (BDDObject bddObject : bddObjects) {
            if (bddObject.isRemoved() && excludedObjects.indexOf(bddObject.getRowId().trim()) == -1) {
                excludedObjects.append("'").append(bddObject.getRowId().trim()).append("',");
            } else if (bddObject.isChanged()) {
                List<String> changesCols = bddObject.getChangedColumns();
                if (excludedObjects.indexOf(bddObject.getRowId().trim()) == -1
                        && (changesCols.contains(effectiveEndDateCol) || changesCols.contains(effectiveStartDateCol))) {
                    excludedObjects.append("'").append(bddObject.getRowId().trim()).append("',");
                }
            }
        }
        if (excludedObjects.length() > 0) {
            excludedObjects.setLength(excludedObjects.length() - 1);
            return excludedObjects.toString();
        } else {
            return null;
        }

    }
}